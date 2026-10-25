package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static hu.dlaszlo.mos6510.AddressingMode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IllegalOpcodeTest extends CpuTestBase {

    private static Stream<Arguments> opcodesOf(String... mnemonics) {
        return Opcodes.OPCODE_MAP.values().stream()
                .filter(o -> Stream.of(mnemonics).anyMatch(m -> o.getTemplate().startsWith(m + " ") || o.getTemplate().equals(m)))
                .sorted(Comparator.comparing(Opcode::getOpcode))
                .map(o -> Arguments.of(String.format("%02X %s", o.getOpcode(), o.getTemplate()), o));
    }

    /**
     * Writes the operands for the addressing mode of the opcode, X = $04, Y = $08
     *
     * @return the effective address
     */
    private int prepareOperand(Opcode opcode) {
        AddressResolver mode = opcode.getAddressResolver();
        reg.setXr(0x04);
        reg.setYr(0x08);
        if (mode == ZP) {
            code(opcode.getOpcode(), 0x40);
            return 0x0040;
        } else if (mode == ZPX) {
            code(opcode.getOpcode(), 0x40);
            return 0x0044;
        } else if (mode == ZPY) {
            code(opcode.getOpcode(), 0x40);
            return 0x0048;
        } else if (mode == IZX) {
            code(opcode.getOpcode(), 0x40);
            poke(0x44, 0x45, 0x23);
            return 0x2345;
        } else if (mode == IZY) {
            code(opcode.getOpcode(), 0x50);
            poke(0x50, 0x40, 0x23);
            return 0x2348;
        } else if (mode == ABS) {
            code(opcode.getOpcode(), 0x45, 0x23);
            return 0x2345;
        } else if (mode == ABX) {
            code(opcode.getOpcode(), 0x40, 0x23);
            return 0x2344;
        } else if (mode == ABY) {
            code(opcode.getOpcode(), 0x40, 0x23);
            return 0x2348;
        }
        throw new IllegalArgumentException("Unexpected addressing mode");
    }

    // read-modify-write combinations

    static Stream<Arguments> readModifyWrite() {
        return opcodesOf("slo", "rla", "sre", "rra", "dcp", "isc");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readModifyWrite")
    void readModifyWriteUsesTheRightAddress(String name, Opcode opcode) {
        // memory $40, carry clear: the expected new memory value per instruction
        Map<String, Integer> expected = Map.of(
                "slo", 0x80, "rla", 0x80, "sre", 0x20, "rra", 0x20, "dcp", 0x3f, "isc", 0x41);
        int target = prepareOperand(opcode);
        poke(target, 0x40);
        reg.setCarry(false);
        assertEquals(opcode.getCyclesMin(), step());
        assertEquals(hex2(expected.get(name.substring(3, 6))), hex2(mem.getByte(target)));
        assertPc(CODE + opcode.getLength());
    }

    static Stream<Arguments> combinations() {
        // name, undocumented opcode, the two documented opcodes it combines (all zeropage)
        return Stream.of(
                Arguments.of("slo = asl + ora", 0x07, 0x06, 0x05),
                Arguments.of("rla = rol + and", 0x27, 0x26, 0x25),
                Arguments.of("sre = lsr + eor", 0x47, 0x46, 0x45),
                Arguments.of("rra = ror + adc", 0x67, 0x66, 0x65),
                Arguments.of("dcp = dec + cmp", 0xc7, 0xc6, 0xc5),
                Arguments.of("isc = inc + sbc", 0xe7, 0xe6, 0xe5)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("combinations")
    void behavesLikeTheTwoDocumentedInstructions(String name, int undocumented, int first, int second) {
        Mos6510 ref = new Mos6510();
        Registers refReg = ref.getRegisters();
        Memory refMem = ref.getMemory();
        refMem.setByte(CODE, first);
        refMem.setByte(CODE + 1, 0x10);
        refMem.setByte(CODE + 2, second);
        refMem.setByte(CODE + 3, 0x10);
        poke(CODE, undocumented, 0x10);
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                for (int flags = 0; flags < 4; flags++) {
                    boolean carry = (flags & 1) != 0;
                    boolean decimal = (flags & 2) != 0;
                    for (Registers r : new Registers[]{reg, refReg}) {
                        r.setPc(CODE);
                        r.setAc(a);
                        r.setXr(0x5a);
                        r.setCarry(carry);
                        r.setDecimal(decimal);
                        r.setOverflow(false);
                    }
                    mem.setByte(0x10, m);
                    refMem.setByte(0x10, m);
                    step();
                    step(ref);
                    step(ref);
                    String what = String.format("A=%02X M=%02X C=%b D=%b", a, m, carry, decimal);
                    assertEquals(hex2(refMem.getByte(0x10)), hex2(mem.getByte(0x10)), what + " memory");
                    assertEquals(hex2(refReg.getAc()), hex2(reg.getAc()), what + " A");
                    assertEquals(hex2(refReg.getStatusFlag()), hex2(reg.getStatusFlag()), what + " flags");
                }
            }
        }
    }

    // loads and stores

    @ParameterizedTest(name = "{0}")
    @MethodSource("laxOpcodes")
    void lax(String name, Opcode opcode) {
        int target = prepareOperand(opcode);
        poke(target, 0x80);
        assertEquals(opcode.getCyclesMin(), step());
        assertEquals(0x80, reg.getAc());
        assertEquals(0x80, reg.getXr());
        assertFlags("N---");
        assertPc(CODE + opcode.getLength());
    }

    static Stream<Arguments> laxOpcodes() {
        return opcodesOf("lax");
    }

    @Test
    void laxPageCrossing() {
        code(0xbf, 0xf0, 0x20);                 // lax $20f0,y
        reg.setYr(0x20);
        poke(0x2110, 0x00);
        reg.setAc(0x11);
        assertEquals(5, step());
        assertEquals(0x00, reg.getXr());
        assertFlags("--Z-");

        code(0xb3, 0x10);                       // lax ($10),y
        poke(0x10, 0xf0, 0x20);
        assertEquals(6, step());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("saxOpcodes")
    void sax(String name, Opcode opcode) {
        int target = prepareOperand(opcode);    // X = $04
        poke(target, 0xff);
        reg.setAc(0xfb);
        reg.setSign(true);
        assertEquals(opcode.getCyclesMin(), step());
        assertEquals(0x00, mem.getByte(target)); // $fb & $04
        assertFlags("N---");                    // flags are not changed
        assertPc(CODE + opcode.getLength());
    }

    @Test
    void saxStoresAAndX() {
        code(0x87, 0x10);                       // sax $10
        reg.setAc(0xf0);
        reg.setXr(0x3c);
        step();
        assertEquals(0x30, mem.getByte(0x10));
        assertEquals(0xf0, reg.getAc());
        assertEquals(0x3c, reg.getXr());
    }

    static Stream<Arguments> saxOpcodes() {
        return opcodesOf("sax");
    }

    @Test
    void las() {
        code(0xbb, 0x00, 0x20);                 // las $2000,y
        poke(0x2000, 0xf3);
        reg.setSp(0xbf);
        assertEquals(4, step());
        assertEquals(0xb3, reg.getAc());
        assertEquals(0xb3, reg.getXr());
        assertEquals(0xb3, reg.getSp());
        assertFlags("N---");

        code(0xbb, 0xf0, 0x20);                 // las $20f0,y
        reg.setYr(0x20);
        poke(0x2110, 0x40);
        assertEquals(5, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");
    }

    // immediate operations

    @Test
    void anc() {
        for (int op : new int[]{0x0b, 0x2b}) {
            code(op, 0xc0);                     // anc #$c0
            reg.setAc(0x81);
            assertEquals(2, step());
            assertEquals(0x80, reg.getAc());
            assertFlags("N--C");

            code(op, 0x40);
            reg.setAc(0x7f);
            step();
            assertEquals(0x40, reg.getAc());
            assertFlags("----");
        }
    }

    @Test
    void alrBehavesLikeAndPlusLsr() {
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                code(0x4b, m);                  // alr #m
                reg.setAc(a);
                reg.setCarry(false);
                assertEquals(2, step());
                int and = a & m;
                String what = String.format("alr %02X & %02X", a, m);
                assertEquals(hex2(and >> 1), hex2(reg.getAc()), what);
                assertEquals((and & 1) != 0, reg.isCarry(), what + " C");
                assertEquals((and >> 1) == 0, reg.isZero(), what + " Z");
                assertEquals(false, reg.isSign(), what + " N");
            }
        }
    }

    @Test
    void arrBinary() {
        // reference: AND + ROR, C = bit 6, V = bit 6 XOR bit 5 of the result
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                for (int c = 0; c < 2; c++) {
                    code(0x6b, m);              // arr #m
                    reg.setAc(a);
                    reg.setCarry(c != 0);
                    assertEquals(2, step());
                    int res = ((a & m) >> 1) | (c << 7);
                    String what = String.format("arr %02X & %02X c=%d", a, m, c);
                    assertEquals(hex2(res), hex2(reg.getAc()), what);
                    assertEquals((res & 0x80) != 0, reg.isSign(), what + " N");
                    assertEquals(res == 0, reg.isZero(), what + " Z");
                    assertEquals((res & 0x40) != 0, reg.isCarry(), what + " C");
                    assertEquals((((res >> 6) ^ (res >> 5)) & 1) != 0, reg.isOverflow(), what + " V");
                }
            }
        }
    }

    @Test
    void arrDecimal() {
        // reference: the decimal ARR description in 64doc
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                for (int c = 0; c < 2; c++) {
                    code(0x6b, m);              // arr #m
                    reg.setDecimal(true);
                    reg.setAc(a);
                    reg.setCarry(c != 0);
                    step();
                    int t = a & m;
                    int ah = t >> 4;
                    int al = t & 0x0f;
                    int res = (t >> 1) | (c << 7);
                    boolean z = res == 0;
                    boolean v = ((t ^ res) & 0x40) != 0;
                    if (al + (al & 1) > 5) {
                        res = (res & 0xf0) | ((res + 6) & 0x0f);
                    }
                    boolean carry = ah + (ah & 1) > 5;
                    if (carry) {
                        res = (res + 0x60) & 0xff;
                    }
                    String what = String.format("arr %02X & %02X c=%d", a, m, c);
                    assertEquals(hex2(res), hex2(reg.getAc()), what);
                    assertEquals(c != 0, reg.isSign(), what + " N");
                    assertEquals(z, reg.isZero(), what + " Z");
                    assertEquals(carry, reg.isCarry(), what + " C");
                    assertEquals(v, reg.isOverflow(), what + " V");
                }
            }
        }
    }

    @Test
    void sbx() {
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                code(0xcb, m);                  // sbx #m
                reg.setAc(a);
                reg.setXr(0xcf);
                reg.setDecimal(true);           // decimal mode is ignored
                reg.setCarry(false);            // carry is not used as borrow
                reg.setOverflow(true);
                assertEquals(2, step());
                int t = a & 0xcf;
                int res = (t - m) & 0xff;
                String what = String.format("sbx %02X & CF - %02X", a, m);
                assertEquals(hex2(res), hex2(reg.getXr()), what);
                assertEquals(hex2(a), hex2(reg.getAc()), what + " A");
                assertEquals(t >= m, reg.isCarry(), what + " C");
                assertEquals(res == 0, reg.isZero(), what + " Z");
                assertEquals((res & 0x80) != 0, reg.isSign(), what + " N");
                assertEquals(true, reg.isOverflow(), what + " V unchanged");
            }
        }
    }

    @Test
    void usbcBehavesLikeSbc() {
        Mos6510 ref = new Mos6510();
        Registers refReg = ref.getRegisters();
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                for (int flags = 0; flags < 4; flags++) {
                    code(0xeb, m);              // usbc #m
                    ref.getMemory().setByte(CODE, 0xe9);
                    ref.getMemory().setByte(CODE + 1, m);
                    for (Registers r : new Registers[]{reg, refReg}) {
                        r.setPc(CODE);
                        r.setAc(a);
                        r.setCarry((flags & 1) != 0);
                        r.setDecimal((flags & 2) != 0);
                    }
                    assertEquals(2, step());
                    step(ref);
                    String what = String.format("usbc %02X - %02X flags=%d", a, m, flags);
                    assertEquals(hex2(refReg.getAc()), hex2(reg.getAc()), what);
                    assertEquals(hex2(refReg.getStatusFlag()), hex2(reg.getStatusFlag()), what + " flags");
                }
            }
        }
    }

    @Test
    void ane() {
        code(0x8b, 0xff);                       // ane #$ff
        reg.setAc(0x00);
        reg.setXr(0xff);
        assertEquals(2, step());
        // unstable on real hardware, VICE uses (A | $EF) & X & value
        assertEquals(0xef, reg.getAc());
        assertEquals(0xff, reg.getXr());
        assertFlags("N---");

        code(0x8b, 0x10);
        reg.setAc(0x00);
        reg.setXr(0xff);
        step();
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");
    }

    @Test
    void lxa() {
        code(0xab, 0xff);                       // lxa #$ff
        reg.setAc(0x00);
        assertEquals(2, step());
        // unstable on real hardware, VICE uses (A | $EE) & value
        assertEquals(0xee, reg.getAc());
        assertEquals(0xee, reg.getXr());
        assertFlags("N---");

        code(0xab, 0x11);
        reg.setAc(0x00);
        step();
        assertEquals(0x00, reg.getXr());
        assertFlags("--Z-");
    }

    // unstable stores: value AND (high byte of the base address + 1)

    @Test
    void shaAbsoluteY() {
        code(0x9f, 0x00, 0x20);                 // sha $2000,y
        reg.setAc(0xff);
        reg.setXr(0x0f);
        reg.setYr(0x10);
        assertEquals(5, step());
        assertEquals(0x01, mem.getByte(0x2010)); // $0f & $21

        code(0x9f, 0x00, 0x20);
        reg.setAc(0xff);
        reg.setXr(0xff);
        step();
        assertEquals(0x21, mem.getByte(0x2010));
    }

    @Test
    void shaAbsoluteYPageCrossing() {
        code(0x9f, 0xf0, 0x20);                 // sha $20f0,y
        reg.setAc(0xff);
        reg.setXr(0x0f);
        reg.setYr(0x20);
        assertEquals(5, step());
        // the high byte of the target address is replaced by the stored value
        assertEquals(0x01, mem.getByte(0x0110));
        assertEquals(0x00, mem.getByte(0x2110));
    }

    @Test
    void shaIndirectY() {
        code(0x93, 0x10);                       // sha ($10),y
        poke(0x10, 0x00, 0x20);
        reg.setAc(0x0f);
        reg.setXr(0xff);
        reg.setYr(0x10);
        assertEquals(6, step());
        assertEquals(0x01, mem.getByte(0x2010));

        code(0x93, 0x10);
        poke(0x10, 0xf0, 0x20);
        reg.setYr(0x20);
        step();
        assertEquals(0x01, mem.getByte(0x0110));
    }

    @Test
    void shx() {
        code(0x9e, 0x00, 0x20);                 // shx $2000,y
        reg.setXr(0xff);
        reg.setYr(0x10);
        assertEquals(5, step());
        assertEquals(0x21, mem.getByte(0x2010));

        code(0x9e, 0xf0, 0x20);
        reg.setXr(0x0f);
        reg.setYr(0x20);
        step();
        assertEquals(0x01, mem.getByte(0x0110));
    }

    @Test
    void shy() {
        code(0x9c, 0x00, 0x20);                 // shy $2000,x
        reg.setYr(0xff);
        reg.setXr(0x10);
        assertEquals(5, step());
        assertEquals(0x21, mem.getByte(0x2010));

        code(0x9c, 0xf0, 0x20);
        reg.setYr(0x0f);
        reg.setXr(0x20);
        step();
        assertEquals(0x01, mem.getByte(0x0110));
    }

    @Test
    void tas() {
        code(0x9b, 0x00, 0x20);                 // tas $2000,y
        reg.setAc(0xff);
        reg.setXr(0x0f);
        reg.setYr(0x10);
        reg.setSign(true);
        assertEquals(5, step());
        assertEquals(0x0f, reg.getSp());
        assertEquals(0x01, mem.getByte(0x2010));
        assertFlags("N---");                    // flags are not changed
    }

    @Test
    void shaWithHighestPage() {
        code(0x9f, 0x00, 0xff);                 // sha $ff00,y
        reg.setAc(0xff);
        reg.setXr(0xff);
        reg.setYr(0x10);
        step();
        assertEquals(0x00, mem.getByte(0xff10)); // $ff & ($ff + 1)
    }

    // NOPs and JAMs

    static Stream<Arguments> nopOpcodes() {
        return opcodesOf("nop");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nopOpcodes")
    void nop(String name, Opcode opcode) {
        int target = opcode.getAddressResolver() == IMP || opcode.getAddressResolver() == IMM
                ? -1 : prepareOperand(opcode);
        if (target < 0) {
            code(opcode.getOpcode(), 0x40);
        }
        reg.setAc(0x12);
        reg.setSp(0xf0);
        int status = reg.getStatusFlag();
        assertEquals(opcode.getCyclesMin(), step());
        assertPc(CODE + opcode.getLength());
        assertEquals(0x12, reg.getAc());
        assertEquals(0xf0, reg.getSp());
        assertEquals(status, reg.getStatusFlag());
    }

    @Test
    void nopAbsoluteXPageCrossing() {
        for (int op : new int[]{0x1c, 0x3c, 0x5c, 0x7c, 0xdc, 0xfc}) {
            code(op, 0xf0, 0x20);               // nop $20f0,x
            reg.setXr(0x20);
            assertEquals(5, step(), String.format("%02X", op));
            assertPc(CODE + 3);
        }
    }

    static Stream<Arguments> jamOpcodes() {
        return opcodesOf("jam");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("jamOpcodes")
    void jam(String name, Opcode opcode) {
        poke(CODE, opcode.getOpcode());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> cpu.run(CODE, CODE + 1));
        assertTrue(e.getMessage().contains("$1000"), e.getMessage());
    }

    @Test
    void allJamOpcodes() {
        assertEquals(12, jamOpcodes().count());
        assertEquals(28, nopOpcodes().count()); // $EA and the 27 undocumented ones
    }

    @Test
    void verboseOutput() {
        PrintStream out = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            cpu.setVerbose(true);
            run(0xa7, 0x10,                     // lax $10
                    0xcb, 0x01,                 // sbx #$01
                    0xdf, 0x00, 0x20);          // dcp $2000,x
        } finally {
            System.setOut(out);
        }
        String trace = buffer.toString();
        assertTrue(trace.contains(".1000   A7 10        lax $10"), trace);
        assertTrue(trace.contains(".1002   CB 01        sbx #$01"), trace);
        assertTrue(trace.contains(".1004   DF 00 20     dcp $2000,x"), trace);
    }

}

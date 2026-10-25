package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlFlowTest extends CpuTestBase {

    static Stream<Arguments> branches() {
        BiConsumer<Registers, Boolean> sign = Registers::setSign;
        BiConsumer<Registers, Boolean> overflow = Registers::setOverflow;
        BiConsumer<Registers, Boolean> carry = Registers::setCarry;
        BiConsumer<Registers, Boolean> zero = Registers::setZero;
        // opcode, flag, flag value when the branch is taken
        return Stream.of(
                Arguments.of("bpl", 0x10, sign, false),
                Arguments.of("bmi", 0x30, sign, true),
                Arguments.of("bvc", 0x50, overflow, false),
                Arguments.of("bvs", 0x70, overflow, true),
                Arguments.of("bcc", 0x90, carry, false),
                Arguments.of("bcs", 0xb0, carry, true),
                Arguments.of("bne", 0xd0, zero, false),
                Arguments.of("beq", 0xf0, zero, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("branches")
    void branch(String name, int opcode, BiConsumer<Registers, Boolean> flag, boolean takenWhen) {
        // not taken: 2 cycles
        code(opcode, 0x10);
        flag.accept(reg, !takenWhen);
        assertEquals(2, step());
        assertPc(CODE + 2);

        // taken, same page: 3 cycles
        code(opcode, 0x10);
        flag.accept(reg, takenWhen);
        assertEquals(3, step());
        assertPc(CODE + 2 + 0x10);

        // taken backwards, same page
        codeAt(CODE + 0x20, opcode, 0xfe);
        flag.accept(reg, takenWhen);
        assertEquals(3, step());
        assertPc(CODE + 0x20);

        // taken, the target is on the next page: 4 cycles
        codeAt(0x10fc, opcode, 0x05);
        flag.accept(reg, takenWhen);
        assertEquals(4, step());
        assertPc(0x1103);

        // taken backwards to the previous page: 4 cycles
        codeAt(0x1000, opcode, 0x80);
        flag.accept(reg, takenWhen);
        assertEquals(4, step());
        assertPc(0x0f82);

        // the program counter wraps around at $FFFF
        codeAt(0xfff0, opcode, 0x7f);
        flag.accept(reg, takenWhen);
        step();
        assertPc(0x0071);
    }

    @Test
    void jmpAbsolute() {
        code(0x4c, 0x34, 0x12);                 // jmp $1234
        assertEquals(3, step());
        assertPc(0x1234);
    }

    @Test
    void jmpIndirect() {
        code(0x6c, 0x00, 0x30);                 // jmp ($3000)
        poke(0x3000, 0x34, 0x12);
        assertEquals(5, step());
        assertPc(0x1234);
    }

    @Test
    void jmpIndirectPageBug() {
        code(0x6c, 0xff, 0x30);                 // jmp ($30ff)
        poke(0x30ff, 0x00);
        poke(0x3000, 0x40);                     // the high byte comes from $3000, not $3100
        poke(0x3100, 0x50);
        step();
        assertPc(0x4000);
    }

    @Test
    void jsr() {
        code(0x20, 0x00, 0x20);                 // jsr $2000
        assertEquals(6, step());
        assertPc(0x2000);
        assertEquals(0xfd, reg.getSp());
        // the return address on the stack is the last byte of the JSR instruction
        assertEquals(0x10, mem.getByte(0x01ff));
        assertEquals(0x02, mem.getByte(0x01fe));
    }

    @Test
    void rts() {
        code(0x20, 0x00, 0x20);                 // jsr $2000
        poke(0x2000, 0x60);                     // rts
        step();
        assertEquals(6, step());
        assertPc(CODE + 3);
        assertEquals(0xff, reg.getSp());
    }

    @Test
    void rtsToPushedAddress() {
        // jump table trick: push (target - 1), then rts
        code(0xa9, 0x20,                        // lda #$20
                0x48,                           // pha
                0xa9, 0xff,                     // lda #$ff
                0x48,                           // pha
                0x60);                          // rts
        for (int i = 0; i < 5; i++) {
            step();
        }
        assertPc(0x2100);
        assertEquals(0xff, reg.getSp());
    }

    @Test
    void brk() {
        code(0x00, 0xea);                       // brk + padding byte
        poke(0xfffe, 0x00, 0x30);               // IRQ/BRK vector: $3000
        reg.setCarry(true);
        assertEquals(7, step());
        assertPc(0x3000);
        assertTrue(reg.isInterrupt());
        assertEquals(0xfc, reg.getSp());
        // return address skips the padding byte
        assertEquals(0x10, mem.getByte(0x01ff));
        assertEquals(0x02, mem.getByte(0x01fe));
        // pushed status has the break flag and bit 5 set, interrupt disable is still clear
        assertEquals(hex2(0b0011_0001), hex2(mem.getByte(0x01fd)));
    }

    @Test
    void rti() {
        code(0x00, 0xea);                       // brk
        poke(0xfffe, 0x00, 0x30);
        poke(0x3000, 0x40);                     // rti
        reg.setCarry(true);
        step();
        reg.setCarry(false);
        assertEquals(6, step());
        // RTI does not add 1 to the address, unlike RTS
        assertPc(CODE + 2);
        assertTrue(reg.isCarry());
        assertFalse(reg.isInterrupt());
        assertEquals(0xff, reg.getSp());
    }

    @Test
    void nop() {
        code(0xea);                             // nop
        reg.setAc(0x12);
        assertEquals(2, step());
        assertPc(CODE + 1);
        assertEquals(0x12, reg.getAc());
        assertFlags("----");
    }

}

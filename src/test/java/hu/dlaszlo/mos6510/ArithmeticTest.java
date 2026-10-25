package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArithmeticTest extends CpuTestBase {

    private static int toBcd(int value) {
        return ((value / 10) << 4) | (value % 10);
    }

    private void assertAdcLike(String what, int a, int m, int c, int sum) {
        int r = sum & 0xff;
        assertEquals(hex2(r), hex2(reg.getAc()), what);
        assertEquals(sum > 0xff, reg.isCarry(), what + " C");
        assertEquals(r == 0, reg.isZero(), what + " Z");
        assertEquals((r & 0x80) != 0, reg.isSign(), what + " N");
        // overflow: the sign of the result differs from the sign of both operands
        assertEquals(((a ^ r) & (m ^ r) & 0x80) != 0, reg.isOverflow(), what + " V");
    }

    @Test
    void adcBinaryAllCombinations() {
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                for (int c = 0; c < 2; c++) {
                    code(0x69, m);              // adc #m
                    reg.setAc(a);
                    reg.setCarry(c != 0);
                    assertEquals(2, step());
                    assertAdcLike(String.format("adc %02X+%02X+%d", a, m, c), a, m, c, a + m + c);
                }
            }
        }
    }

    @Test
    void sbcBinaryAllCombinations() {
        for (int a = 0; a < 0x100; a++) {
            for (int m = 0; m < 0x100; m++) {
                for (int c = 0; c < 2; c++) {
                    code(0xe9, m);              // sbc #m
                    reg.setAc(a);
                    reg.setCarry(c != 0);
                    assertEquals(2, step());
                    // A - M - (1 - C) == A + ~M + C
                    int inv = m ^ 0xff;
                    assertAdcLike(String.format("sbc %02X-%02X c=%d", a, m, c), a, inv, c, a + inv + c);
                }
            }
        }
    }

    @Test
    void adcOverflowSamples() {
        int[][] cases = {
                // a, m, expected V
                {0x50, 0x10, 0},
                {0x50, 0x50, 1},                // 80 + 80 = 160
                {0xf0, 0x20, 0},                // -16 + 32 = 16
                {0xd0, 0x90, 1},                // -48 + -112 = -160
                {0x80, 0xff, 1},
        };
        for (int[] t : cases) {
            code(0x69, t[1]);
            reg.setAc(t[0]);
            reg.setCarry(false);
            step();
            assertEquals(t[2] != 0, reg.isOverflow(), String.format("adc %02X+%02X", t[0], t[1]));
        }
    }

    @Test
    void adcDecimalAllBcdValues() {
        for (int a = 0; a < 100; a++) {
            for (int m = 0; m < 100; m++) {
                for (int c = 0; c < 2; c++) {
                    code(0x69, toBcd(m));       // adc #m
                    reg.setDecimal(true);
                    reg.setAc(toBcd(a));
                    reg.setCarry(c != 0);
                    step();
                    int sum = a + m + c;
                    String what = String.format("adc %d+%d+%d", a, m, c);
                    assertEquals(hex2(toBcd(sum % 100)), hex2(reg.getAc()), what);
                    assertEquals(sum >= 100, reg.isCarry(), what + " C");
                    // NMOS: Z comes from the binary sum
                    assertEquals(((toBcd(a) + toBcd(m) + c) & 0xff) == 0, reg.isZero(), what + " Z");
                }
            }
        }
    }

    @Test
    void adcDecimalNmosFlags() {
        code(0xf8, 0x69, 0x01);                 // sed, adc #$01
        reg.setAc(0x99);
        step();
        step();
        assertEquals(0x00, reg.getAc());
        // N comes from the intermediate result ($A0), Z from the binary sum ($9A)
        assertFlags("N--C");

        code(0x69, 0x00);                       // adc #$00
        reg.setAc(0x79);
        reg.setCarry(true);
        step();
        assertEquals(0x80, reg.getAc());
        assertFlags("NV--");
    }

    @Test
    void sbcDecimalAllBcdValues() {
        for (int a = 0; a < 100; a++) {
            for (int m = 0; m < 100; m++) {
                for (int c = 0; c < 2; c++) {
                    code(0xe9, toBcd(m));       // sbc #m
                    reg.setDecimal(true);
                    reg.setAc(toBcd(a));
                    reg.setCarry(c != 0);
                    step();
                    int diff = a - m - (1 - c);
                    String what = String.format("sbc %d-%d c=%d", a, m, c);
                    assertEquals(hex2(toBcd((diff + 100) % 100)), hex2(reg.getAc()), what);
                    assertEquals(diff >= 0, reg.isCarry(), what + " C");
                    // NMOS: N, V, Z come from the binary difference
                    int bin = (toBcd(a) - toBcd(m) - (1 - c)) & 0xff;
                    assertEquals(bin == 0, reg.isZero(), what + " Z");
                    assertEquals((bin & 0x80) != 0, reg.isSign(), what + " N");
                    assertEquals(((toBcd(a) ^ bin) & (toBcd(a) ^ toBcd(m)) & 0x80) != 0, reg.isOverflow(), what + " V");
                }
            }
        }
    }

    @Test
    void sbcDecimalBorrow() {
        code(0xf8, 0x38, 0xe9, 0x01);           // sed, sec, sbc #$01
        reg.setAc(0x00);
        step();
        step();
        step();
        assertEquals(0x99, reg.getAc());
        assertFlags("N---");
    }

    private void compareAll(int opcode, java.util.function.IntConsumer setRegister) {
        for (int r = 0; r < 0x100; r++) {
            for (int m = 0; m < 0x100; m++) {
                code(opcode, m);
                setRegister.accept(r);
                reg.setOverflow(true);
                step();
                String what = String.format("%02X cmp %02X", r, m);
                assertEquals(r >= m, reg.isCarry(), what + " C");
                assertEquals(r == m, reg.isZero(), what + " Z");
                assertEquals(((r - m) & 0x80) != 0, reg.isSign(), what + " N");
                assertEquals(true, reg.isOverflow(), what + " V unchanged");
            }
        }
    }

    @Test
    void cmp() {
        compareAll(0xc9, reg::setAc);           // cmp #m
        code(0xc9, 0x03);
        reg.setAc(0x05);
        assertEquals(2, step());
        assertEquals(0x05, reg.getAc());
        assertFlags("-V-C");
    }

    @Test
    void cpx() {
        compareAll(0xe0, reg::setXr);           // cpx #m
        code(0xec, 0x00, 0x20);                 // cpx $2000
        poke(0x2000, 0x10);
        reg.setXr(0x10);
        assertEquals(4, step());
        assertFlags("-VZC");
    }

    @Test
    void cpy() {
        compareAll(0xc0, reg::setYr);           // cpy #m
        code(0xc4, 0x10);                       // cpy $10
        poke(0x10, 0x20);
        reg.setYr(0x10);
        assertEquals(3, step());
        assertFlags("NV--");
    }

    @Test
    void inc() {
        code(0xe6, 0x10);                       // inc $10
        poke(0x10, 0xff);
        assertEquals(5, step());
        assertEquals(0x00, mem.getByte(0x10));
        assertFlags("--Z-");

        code(0xee, 0x00, 0x20);                 // inc $2000
        poke(0x2000, 0x7f);
        assertEquals(6, step());
        assertEquals(0x80, mem.getByte(0x2000));
        assertFlags("N---");
    }

    @Test
    void dec() {
        code(0xc6, 0x10);                       // dec $10
        poke(0x10, 0x00);
        assertEquals(5, step());
        assertEquals(0xff, mem.getByte(0x10));
        assertFlags("N---");

        code(0xce, 0x00, 0x20);                 // dec $2000
        poke(0x2000, 0x01);
        assertEquals(6, step());
        assertEquals(0x00, mem.getByte(0x2000));
        assertFlags("--Z-");
    }

    @Test
    void inx() {
        code(0xe8);                             // inx
        reg.setXr(0xff);
        assertEquals(2, step());
        assertEquals(0x00, reg.getXr());
        assertFlags("--Z-");
    }

    @Test
    void iny() {
        code(0xc8);                             // iny
        reg.setYr(0x7f);
        assertEquals(2, step());
        assertEquals(0x80, reg.getYr());
        assertFlags("N---");
    }

    @Test
    void dex() {
        code(0xca);                             // dex
        reg.setXr(0x00);
        assertEquals(2, step());
        assertEquals(0xff, reg.getXr());
        assertFlags("N---");
    }

    @Test
    void dey() {
        code(0x88);                             // dey
        reg.setYr(0x01);
        assertEquals(2, step());
        assertEquals(0x00, reg.getYr());
        assertFlags("--Z-");
    }

}

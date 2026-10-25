package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackFlagTest extends CpuTestBase {

    @Test
    void pha() {
        code(0x48);                             // pha
        reg.setAc(0x42);
        assertEquals(3, step());
        assertEquals(0x42, mem.getByte(0x01ff));
        assertEquals(0xfe, reg.getSp());
    }

    @Test
    void pla() {
        code(0x68);                             // pla
        reg.setSp(0xfe);
        poke(0x01ff, 0x80);
        assertEquals(4, step());
        assertEquals(0x80, reg.getAc());
        assertEquals(0xff, reg.getSp());
        assertFlags("N---");
    }

    @Test
    void stackWrapsAround() {
        code(0x48, 0x68);                       // pha, pla
        reg.setSp(0x00);
        reg.setAc(0x33);
        step();
        assertEquals(0x33, mem.getByte(0x0100));
        assertEquals(0xff, reg.getSp());
        reg.setAc(0x00);
        step();
        assertEquals(0x33, reg.getAc());
        assertEquals(0x00, reg.getSp());
    }

    @Test
    void php() {
        code(0x08);                             // php
        reg.setCarry(true);
        reg.setSign(true);
        assertEquals(3, step());
        // the break flag and the unused bit 5 are always set in the pushed copy
        assertEquals(hex2(0b1011_0001), hex2(mem.getByte(0x01ff)));
        assertFalse(reg.isBreak_());
        assertEquals(0xfe, reg.getSp());
    }

    @Test
    void plp() {
        code(0x28);                             // plp
        reg.setSp(0xfe);
        poke(0x01ff, 0b1100_1111);
        assertEquals(4, step());
        assertFlags("NVZC");
        assertTrue(reg.isDecimal());
        assertTrue(reg.isInterrupt());
        assertEquals(0xff, reg.getSp());
    }

    @Test
    void clcSec() {
        code(0x38, 0x18);                       // sec, clc
        assertEquals(2, step());
        assertTrue(reg.isCarry());
        assertEquals(2, step());
        assertFalse(reg.isCarry());
    }

    @Test
    void cldSed() {
        code(0xf8, 0xd8);                       // sed, cld
        assertEquals(2, step());
        assertTrue(reg.isDecimal());
        assertEquals(2, step());
        assertFalse(reg.isDecimal());
    }

    @Test
    void cliSei() {
        code(0x78, 0x58);                       // sei, cli
        assertEquals(2, step());
        assertTrue(reg.isInterrupt());
        assertEquals(2, step());
        assertFalse(reg.isInterrupt());
    }

    @Test
    void clv() {
        code(0xb8);                             // clv
        reg.setOverflow(true);
        assertEquals(2, step());
        assertFalse(reg.isOverflow());
        assertPc(CODE + 1);
    }

}

package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogicShiftTest extends CpuTestBase {

    @Test
    void and() {
        code(0x29, 0x0f);                       // and #$0f
        reg.setAc(0xf0);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");

        code(0x25, 0x10);                       // and $10
        poke(0x10, 0xc3);
        reg.setAc(0x81);
        assertEquals(3, step());
        assertEquals(0x81, reg.getAc());
        assertFlags("N---");
    }

    @Test
    void ora() {
        code(0x09, 0x80);                       // ora #$80
        reg.setAc(0x01);
        assertEquals(2, step());
        assertEquals(0x81, reg.getAc());
        assertFlags("N---");

        code(0x0d, 0x00, 0x20);                 // ora $2000
        poke(0x2000, 0x00);
        reg.setAc(0x00);
        assertEquals(4, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");
    }

    @Test
    void eor() {
        code(0x49, 0xff);                       // eor #$ff
        reg.setAc(0xff);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");

        code(0x45, 0x10);                       // eor $10
        poke(0x10, 0xaa);
        reg.setAc(0x55);
        assertEquals(3, step());
        assertEquals(0xff, reg.getAc());
        assertFlags("N---");
    }

    @Test
    void bit() {
        code(0x24, 0x10);                       // bit $10
        poke(0x10, 0xc0);
        reg.setAc(0x01);
        assertEquals(3, step());
        assertEquals(0x01, reg.getAc());
        assertFlags("NVZ-");

        code(0x2c, 0x00, 0x20);                 // bit $2000
        poke(0x2000, 0x01);
        assertEquals(4, step());
        assertFlags("----");
    }

    @Test
    void asl() {
        code(0x0a);                             // asl
        reg.setAc(0x81);
        assertEquals(2, step());
        assertEquals(0x02, reg.getAc());
        assertFlags("---C");

        code(0x06, 0x10);                       // asl $10
        poke(0x10, 0x40);
        assertEquals(5, step());
        assertEquals(0x80, mem.getByte(0x10));
        assertFlags("N---");

        code(0x0e, 0x00, 0x20);                 // asl $2000
        poke(0x2000, 0x80);
        assertEquals(6, step());
        assertEquals(0x00, mem.getByte(0x2000));
        assertFlags("--ZC");
    }

    @Test
    void lsr() {
        code(0x4a);                             // lsr
        reg.setAc(0x01);
        reg.setSign(true);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--ZC");

        code(0x46, 0x10);                       // lsr $10
        poke(0x10, 0xfe);
        assertEquals(5, step());
        assertEquals(0x7f, mem.getByte(0x10));
        assertFlags("----");
    }

    @Test
    void rol() {
        code(0x2a);                             // rol
        reg.setAc(0x80);
        reg.setCarry(false);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--ZC");

        code(0x2a);                             // rol: carry goes into bit 0
        reg.setAc(0x40);
        assertEquals(2, step());
        assertEquals(0x81, reg.getAc());
        assertFlags("N---");

        code(0x26, 0x10);                       // rol $10
        poke(0x10, 0xc0);
        reg.setCarry(true);
        assertEquals(5, step());
        assertEquals(0x81, mem.getByte(0x10));
        assertFlags("N--C");
    }

    @Test
    void ror() {
        code(0x6a);                             // ror
        reg.setAc(0x01);
        reg.setCarry(false);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--ZC");

        code(0x6a);                             // ror: carry goes into bit 7
        reg.setAc(0x02);
        assertEquals(2, step());
        assertEquals(0x81, reg.getAc());
        assertFlags("N---");

        code(0x66, 0x10);                       // ror $10
        poke(0x10, 0x03);
        reg.setCarry(false);
        assertEquals(5, step());
        assertEquals(0x01, mem.getByte(0x10));
        assertFlags("---C");
    }

}

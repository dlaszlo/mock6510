package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadStoreTransferTest extends CpuTestBase {

    @Test
    void lda() {
        code(0xa9, 0x00);                       // lda #$00
        reg.setAc(0x55);
        reg.setSign(true);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");
        assertPc(CODE + 2);

        code(0xad, 0x34, 0x12);                 // lda $1234
        poke(0x1234, 0x80);
        assertEquals(4, step());
        assertEquals(0x80, reg.getAc());
        assertFlags("N---");
        assertPc(CODE + 3);
    }

    @Test
    void ldx() {
        code(0xa2, 0x81);                       // ldx #$81
        assertEquals(2, step());
        assertEquals(0x81, reg.getXr());
        assertFlags("N---");

        code(0xa6, 0x10);                       // ldx $10
        poke(0x10, 0x00);
        assertEquals(3, step());
        assertEquals(0x00, reg.getXr());
        assertFlags("--Z-");
    }

    @Test
    void ldy() {
        code(0xa0, 0x7f);                       // ldy #$7f
        assertEquals(2, step());
        assertEquals(0x7f, reg.getYr());
        assertFlags("----");

        code(0xac, 0x00, 0x20);                 // ldy $2000
        poke(0x2000, 0xff);
        assertEquals(4, step());
        assertEquals(0xff, reg.getYr());
        assertFlags("N---");
    }

    @Test
    void sta() {
        code(0x8d, 0x00, 0x20);                 // sta $2000
        reg.setAc(0x00);
        reg.setSign(true);
        assertEquals(4, step());
        assertEquals(0x00, mem.getByte(0x2000));
        assertFlags("N---");                    // flags are not changed
        assertPc(CODE + 3);

        code(0x85, 0x10);                       // sta $10
        reg.setAc(0x42);
        assertEquals(3, step());
        assertEquals(0x42, mem.getByte(0x10));
    }

    @Test
    void stx() {
        code(0x86, 0x10);                       // stx $10
        reg.setXr(0x99);
        assertEquals(3, step());
        assertEquals(0x99, mem.getByte(0x10));
        assertFlags("----");

        code(0x8e, 0x00, 0x20);                 // stx $2000
        assertEquals(4, step());
        assertEquals(0x99, mem.getByte(0x2000));
    }

    @Test
    void sty() {
        code(0x84, 0x10);                       // sty $10
        reg.setYr(0x77);
        assertEquals(3, step());
        assertEquals(0x77, mem.getByte(0x10));
        assertFlags("----");

        code(0x8c, 0x00, 0x20);                 // sty $2000
        assertEquals(4, step());
        assertEquals(0x77, mem.getByte(0x2000));
    }

    @Test
    void tax() {
        code(0xaa);                             // tax
        reg.setAc(0x80);
        assertEquals(2, step());
        assertEquals(0x80, reg.getXr());
        assertFlags("N---");
        assertPc(CODE + 1);
    }

    @Test
    void txa() {
        code(0x8a);                             // txa
        reg.setAc(0x55);
        reg.setXr(0x00);
        assertEquals(2, step());
        assertEquals(0x00, reg.getAc());
        assertFlags("--Z-");
    }

    @Test
    void tay() {
        code(0xa8);                             // tay
        reg.setAc(0x00);
        reg.setYr(0x12);
        assertEquals(2, step());
        assertEquals(0x00, reg.getYr());
        assertFlags("--Z-");
    }

    @Test
    void tya() {
        code(0x98);                             // tya
        reg.setYr(0xc0);
        assertEquals(2, step());
        assertEquals(0xc0, reg.getAc());
        assertFlags("N---");
    }

    @Test
    void tsx() {
        code(0xba);                             // tsx
        reg.setSp(0xf0);
        assertEquals(2, step());
        assertEquals(0xf0, reg.getXr());
        assertFlags("N---");
    }

    @Test
    void txs() {
        code(0x9a);                             // txs
        reg.setXr(0x00);
        assertEquals(2, step());
        assertEquals(0x00, reg.getSp());
        assertFlags("----");                    // TXS does not change the flags
    }

}

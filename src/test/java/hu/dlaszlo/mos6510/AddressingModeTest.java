package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddressingModeTest extends CpuTestBase {

    @Test
    void immediate() {
        code(0xa9, 0x42);                       // lda #$42
        assertEquals(2, step());
        assertEquals(0x42, reg.getAc());
        assertPc(CODE + 2);
    }

    @Test
    void zeropage() {
        code(0xa5, 0x10);                       // lda $10
        poke(0x10, 0x42);
        assertEquals(3, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void zeropageX() {
        code(0xb5, 0x10);                       // lda $10,x
        reg.setXr(0x05);
        poke(0x15, 0x42);
        assertEquals(4, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void zeropageXWrapsWithinZeropage() {
        code(0xb5, 0xf0);                       // lda $f0,x
        reg.setXr(0x20);
        poke(0x10, 0x42);
        poke(0x110, 0x99);
        assertEquals(4, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void zeropageYWrapsWithinZeropage() {
        code(0xb6, 0xf0);                       // ldx $f0,y
        reg.setYr(0x20);
        poke(0x10, 0x42);
        assertEquals(4, step());
        assertEquals(0x42, reg.getXr());
    }

    @Test
    void indexedIndirect() {
        code(0xa1, 0x20);                       // lda ($20,x)
        reg.setXr(0x04);
        poke(0x24, 0x00, 0x20);
        poke(0x2000, 0x42);
        assertEquals(6, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void indexedIndirectPointerWrapsWithinZeropage() {
        code(0xa1, 0xf0);                       // lda ($f0,x)
        reg.setXr(0x0f);
        poke(0xff, 0x00);
        poke(0x00, 0x20);                       // high byte of the pointer comes from $00
        poke(0x2000, 0x42);
        step();
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void indirectIndexed() {
        code(0xb1, 0x20);                       // lda ($20),y
        reg.setYr(0x10);
        poke(0x20, 0x00, 0x20);
        poke(0x2010, 0x42);
        assertEquals(5, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void indirectIndexedPageCrossing() {
        code(0xb1, 0x20);                       // lda ($20),y
        reg.setYr(0x20);
        poke(0x20, 0xf0, 0x20);
        poke(0x2110, 0x42);
        assertEquals(6, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void indirectIndexedPointerWrapsWithinZeropage() {
        code(0xb1, 0xff);                       // lda ($ff),y
        reg.setYr(0x01);
        poke(0xff, 0x00);
        poke(0x00, 0x20);
        poke(0x2001, 0x42);
        step();
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void indirectIndexedWrapsAt64k() {
        code(0xb1, 0x20);                       // lda ($20),y
        reg.setYr(0x01);
        poke(0x20, 0xff, 0xff);
        poke(0x0000, 0x42);
        assertEquals(6, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void absolute() {
        code(0xad, 0x34, 0x12);                 // lda $1234
        poke(0x1234, 0x42);
        assertEquals(4, step());
        assertEquals(0x42, reg.getAc());
        assertPc(CODE + 3);
    }

    @Test
    void absoluteX() {
        code(0xbd, 0x00, 0x20);                 // lda $2000,x
        reg.setXr(0x10);
        poke(0x2010, 0x42);
        assertEquals(4, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void absoluteXPageCrossing() {
        code(0xbd, 0xf0, 0x20);                 // lda $20f0,x
        reg.setXr(0x20);
        poke(0x2110, 0x42);
        assertEquals(5, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void absoluteXWrapsAt64k() {
        code(0xbd, 0xff, 0xff);                 // lda $ffff,x
        reg.setXr(0x01);
        poke(0x0000, 0x42);
        assertEquals(5, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void absoluteY() {
        code(0xb9, 0x00, 0x20);                 // lda $2000,y
        reg.setYr(0x10);
        poke(0x2010, 0x42);
        assertEquals(4, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void absoluteYPageCrossing() {
        code(0xb9, 0xf0, 0x20);                 // lda $20f0,y
        reg.setYr(0x20);
        poke(0x2110, 0x42);
        assertEquals(5, step());
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void absoluteYWrapsAt64k() {
        code(0xb9, 0xfe, 0xff);                 // lda $fffe,y
        reg.setYr(0x03);
        poke(0x0001, 0x42);
        step();
        assertEquals(0x42, reg.getAc());
    }

    @Test
    void storeWrapsAt64k() {
        code(0x9d, 0xff, 0xff);                 // sta $ffff,x
        reg.setXr(0x02);
        reg.setAc(0x42);
        step();
        assertEquals(0x42, mem.getByte(0x0001));
    }

    @Test
    void storeHasNoPageCrossingPenalty() {
        code(0x9d, 0xf0, 0x20);                 // sta $20f0,x
        reg.setXr(0x20);
        assertEquals(5, step());

        code(0x99, 0x00, 0x20);                 // sta $2000,y
        reg.setYr(0x00);
        assertEquals(5, step());

        code(0x91, 0x20);                       // sta ($20),y
        poke(0x20, 0x00, 0x20);
        assertEquals(6, step());
    }

    @Test
    void readModifyWriteHasNoPageCrossingPenalty() {
        code(0xfe, 0xf0, 0x20);                 // inc $20f0,x
        reg.setXr(0x20);
        poke(0x2110, 0x41);
        assertEquals(7, step());
        assertEquals(0x42, mem.getByte(0x2110));
    }

}

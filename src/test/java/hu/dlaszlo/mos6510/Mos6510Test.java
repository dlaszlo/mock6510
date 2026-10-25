package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Mos6510Test extends CpuTestBase {

    @Test
    void loadUsesTheAddressInTheHeader() {
        cpu.load(new byte[]{0x01, 0x08, 0x0b, 0x08});
        assertEquals(0x0b, mem.getByte(0x0801));
        assertEquals(0x08, mem.getByte(0x0802));
    }

    @Test
    void loadToHighAddress() {
        cpu.load(new byte[]{(byte) 0x80, (byte) 0xc0, (byte) 0xea, (byte) 0x60});
        assertEquals(0xea, mem.getByte(0xc080));
        assertEquals(0x60, mem.getByte(0xc081));
    }

    @Test
    void runStopsAtEndAddress() {
        run(0xa2, 0x05,                         // ldx #$05
                0xca,                           // loop: dex
                0xd0, 0xfd);                    // bne loop
        assertEquals(0x00, reg.getXr());
        assertPc(CODE + 5);
    }

    @Test
    void invalidOpcode() {
        poke(CODE, 0x02);
        assertThrows(IllegalArgumentException.class, () -> cpu.run(CODE, CODE + 1));
    }

    @Test
    void verboseOutputShowsOperandsAsWritten() {
        PrintStream out = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            cpu.setVerbose(true);
            reg.setYr(0x94);
            poke(0x10, 0x00, 0x30);
            run(0x49, 0xff,                     // eor #$ff
                    0xbe, 0x00, 0x22,           // ldx $2200,y
                    0xb1, 0x10,                 // lda ($10),y
                    0xea,                       // nop
                    0xd0, 0x00);                // bne $100a
        } finally {
            System.setOut(out);
        }
        String trace = buffer.toString();
        assertTrue(trace.contains(".1000   49 FF        eor #$FF"), trace);
        assertTrue(trace.contains(".1002   BE 00 22     ldx $2200,y"), trace);
        assertTrue(trace.contains(".1005   B1 10        lda ($10),y"), trace);
        assertTrue(trace.contains(".1007   EA           nop"), trace);
        assertTrue(trace.contains(".1008   D0 00        bne $100A"), trace);
    }

    @Test
    void verboseOutputDoesNotChangeTheResult() {
        PrintStream out = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            cpu.setVerbose(true);
            run(0xa9, 0x10,                     // lda #$10
                    0x69, 0x20,                 // adc #$20
                    0x85, 0x40,                 // sta $40
                    0xe6, 0x40);                // inc $40
        } finally {
            System.setOut(out);
        }
        assertEquals(0x30, reg.getAc());
        assertEquals(0x31, mem.getByte(0x40));
    }

    @Test
    void multiplicationRoutine() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/mul.prg")) {
            assertNotNull(in, "mul.prg");
            cpu.load(in.readAllBytes());
        }
        for (int x = -128; x < 128; x++) {
            for (int y = -128; y < 128; y++) {
                if (x == -128 && y == -128) {
                    continue;                   // the routine does not handle 16384
                }
                mem.setByte(0x02, x);
                mem.setByte(0x03, y);
                cpu.run(0x1008, 0x100b);        // jsr mul
                int expected = (x * y) & 0xffff;
                int result = mem.getByte(0x04) + mem.getByte(0x05) * 0x100;
                assertEquals(expected, result, "x = " + x + ", y = " + y);
            }
        }
    }

}

package hu.dlaszlo;

import hu.dlaszlo.mos6510.Mos6510;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Mos6510 mos6510 = new Mos6510();
        byte[] program = Test.class.getResourceAsStream("/mul.prg").readAllBytes();
        mos6510.load(program);
        mos6510.setVerbose(false);
        for (int x = -128; x < 128; x++) {
            System.out.println(x);
            for (int y = -128; y < 128; y++) {
                if (x == -128 && y == -128) {
                    continue;
                }
                mos6510.setVerbose(x == 15 && y == -123);
                mos6510.getMemory().setByte(0x02, x);
                mos6510.getMemory().setByte(0x03, y);
                mos6510.run(0x1008, 0x100b);
                int expected = (x * y) & 0b1111_1111_1111_1111;
                int result = mos6510.getMemory().getByte(0x04) +
                        mos6510.getMemory().getByte(0x05) * 0x100;
                if (expected != result) {
                    throw new IllegalArgumentException("Hiba: x = " + x + ", y = " + y + ", expected = " + expected + ", result = " + result);
                }
            }
        }

    }

}

package hu.dlaszlo.cpu6510;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Cpu6510 cpu6510 = new Cpu6510();
        byte[] program = Test.class.getResourceAsStream("/mul.prg").readAllBytes();
        cpu6510.load(program);
        cpu6510.setVerbose(false);
        for (int x = -128; x < 128; x++) {
            System.out.println(x);
            for (int y = -128; y < 128; y++) {
                if (x == -128 && y == -128) {
                    continue;
                }
                cpu6510.setMemory(2, x);
                cpu6510.setMemory(3, y);
                cpu6510.run(0x1008, 0x100b);
                int expected = (x * y) & 0b1111_1111_1111_1111;
                int result = cpu6510.getMemory(4) + cpu6510.getMemory(5) * 256;
                if (expected != result) {
                    throw new IllegalArgumentException("Hiba: x = " + x + ", y = " + y + ", expected = " + expected + ", result = " + result);
                }
            }
        }

    }

}

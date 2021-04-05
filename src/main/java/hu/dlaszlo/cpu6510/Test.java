package hu.dlaszlo.cpu6510;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Mock6510 mock6510 = new Mock6510();
        byte[] program = Test.class.getResourceAsStream("/mul.prg").readAllBytes();
        mock6510.load(program);
        mock6510.setVerbose(false);
        for (int x = -128; x < 128; x++) {
            System.out.println(x);
            for (int y = -128; y < 128; y++) {
                if (x == -128 && y == -128) {
                    continue;
                }
                mock6510.setMemory(2, x);
                mock6510.setMemory(3, y);
                mock6510.run(0x1008, 0x100b);
                int expected = (x * y) & 0b1111_1111_1111_1111;
                int result = mock6510.getMemory(4) + mock6510.getMemory(5) * 256;
                if (expected != result) {
                    throw new IllegalArgumentException("Hiba: x = " + x + ", y = " + y + ", expected = " + expected + ", result = " + result);
                }
            }
        }

    }

}

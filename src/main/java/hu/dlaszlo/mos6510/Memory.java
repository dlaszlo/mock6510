package hu.dlaszlo.mos6510;

import java.util.Arrays;

public class Memory {

    private final byte[] ram = new byte[65536];

    public void setByte(int addr, int b) {
        ram[addr] = (byte) (b & 0xff);
    }

    public int getByte(int addr) {
        int val = ram[addr];
        return val & 0xff;
    }

    public Memory copy() {
        Memory memory = new Memory();
        System.arraycopy(ram, 0, memory.ram, 0, ram.length);
        return memory;
    }

}

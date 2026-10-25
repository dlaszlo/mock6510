package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class CpuTestBase {

    protected static final int CODE = 0x1000;

    protected Mos6510 cpu;
    protected Registers reg;
    protected Memory mem;

    @BeforeEach
    void setUpCpu() {
        cpu = new Mos6510();
        cpu.setVerbose(false);
        reg = cpu.getRegisters();
        mem = cpu.getMemory();
    }

    protected void poke(int addr, int... bytes) {
        for (int i = 0; i < bytes.length; i++) {
            mem.setByte(addr + i, bytes[i]);
        }
    }

    /**
     * Puts the code at CODE and points the program counter to it
     */
    protected void code(int... bytes) {
        codeAt(CODE, bytes);
    }

    protected void codeAt(int addr, int... bytes) {
        poke(addr, bytes);
        reg.setPc(addr);
    }

    /**
     * Executes one instruction at the program counter
     *
     * @return cycles used by the instruction
     */
    protected int step() {
        int bc = mem.getByte(reg.getAndIncPc());
        Opcode opcode = Opcodes.OPCODE_MAP.get(bc);
        assertNotNull(opcode, String.format("Invalid opcode: 0x%02X", bc));
        return opcode.getInstruction().run(opcode, reg, mem);
    }

    /**
     * Puts the code at CODE and executes it until its end
     */
    protected void run(int... bytes) {
        poke(CODE, bytes);
        cpu.run(CODE, CODE + bytes.length);
    }

    protected void assertFlags(String nvzc) {
        String actual = (reg.isSign() ? "N" : "-")
                + (reg.isOverflow() ? "V" : "-")
                + (reg.isZero() ? "Z" : "-")
                + (reg.isCarry() ? "C" : "-");
        assertEquals(nvzc, actual, "flags NVZC");
    }

    protected void assertPc(int expected) {
        assertEquals(hex4(expected), hex4(reg.getPc()), "PC");
    }

    protected static String hex2(int value) {
        return String.format("$%02X", value);
    }

    protected static String hex4(int value) {
        return String.format("$%04X", value);
    }

}

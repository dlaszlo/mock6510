package hu.dlaszlo.mos6510;

public class Mos6510 {

    private boolean verbose = true;

    private Registers registers = new Registers();
    private Memory memory = new Memory();

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Registers getRegisters() {
        return registers;
    }

    public Memory getMemory() {
        return memory;
    }


    public void load(byte[] program) {
        int d = program[0] + program[1] * 0x100;
        for (int s = 2; s < program.length; s++) {
            memory.setByte(d, program[s]);
            d++;
        }
    }

    private void print(Opcode opcode) {
        if (verbose) {
            int pc = registers.getPc();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(".%04X   ", pc - 1));
            sb.append(String.format("%02X ", opcode.getOpcode()));
            int p1 = memory.getByte(pc);
            int p2 = memory.getByte(pc + 1);
            int w = p1 + p2 * 0x100;
            int r = (pc + 1) + p1;
            sb.append(opcode.getLength() > 1 ? String.format("%02X ", p1) : "   ");
            sb.append(opcode.getLength() > 2 ? String.format("%02X ", p2) : "   ");
            sb.append("    ");
            String ostr = opcode.getTemplate()
                    .replace("$BYTE", String.format("%02X ", p1))
                    .replace("$WORD", String.format("%04X ", w))
                    .replace("$REL", String.format("%04X ", r));
            sb.append(ostr);
            System.out.println(sb);
        }
    }

    private Opcode getOpcode(int bc) {
        Opcode opcode = Opcodes.OPCODE_MAP.get(bc);
        if (opcode == null) {
            throw new IllegalArgumentException(String.format("Invalid opcode: 0x%02X ", bc));
        }
        print(opcode);
        return opcode;
    }

    private void run(Opcode opcode) {
        int cycles = opcode.getInstruction().run(opcode, registers, memory);
        if (verbose) {
            printCycleInfo(opcode, cycles);
            System.out.println(registers.toString());
        }
    }

    private void printCycleInfo(Opcode opcode, int cycles) {
        StringBuilder sb = new StringBuilder("Cycles: ");
        sb.append(cycles);
        if (opcode.getCyclesMin() != opcode.getCyclesMax()) {
            if (cycles == opcode.getCyclesMax()) {
                sb.append("!");
            }
            sb.append(" (min: ")
                    .append(opcode.getCyclesMin())
                    .append(", max: ")
                    .append(opcode.getCyclesMax())
                    .append(")");
        }
        System.out.println(sb);
    }

    public void run(int start, int end) {
        registers.setPc(start);
        if (verbose) {
            System.out.println(registers.toString());
        }
        while (registers.getPc() != end) {
            int bc = memory.getByte(registers.getAndIncPc());
            run(getOpcode(bc));
        }
    }

}


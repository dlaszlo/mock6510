package hu.dlaszlo.mos6510;

import org.apache.commons.lang3.StringUtils;

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
            Registers reg = registers.copy();
            Memory mem = memory.copy();
            int pc = reg.getPc();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(".%04X   ", pc - 1));
            sb.append(String.format("%02X ", opcode.getOpcode()));

            Address address = null;
            if (opcode.getLength() > 1) {
                address = opcode.getAddressResolver().getAddress(reg, mem);
            }
            sb.append(opcode.getLength() > 1 ? String.format("%02X ", mem.getByte(pc)) : "   ");
            sb.append(opcode.getLength() > 2 ? String.format("%02X ", mem.getByte(pc + 1)) : "   ");
            sb.append("    ");

            String ostr = opcode.getTemplate();
            if (address != null) {
                ostr = ostr.replace("$BYTE", String.format("%02X", address.getAddress()));
                ostr = ostr.replace("$WORD", String.format("%04X", address.getAddress()));
                ostr = ostr.replace("$REL", String.format("%04X", address.getAddress()));
            }
            sb.append(ostr);

            sb.append(StringUtils.repeat(" ", 40 - sb.length()));

            reg = registers.copy();

            int cycles = opcode.getInstruction().run(opcode, reg, mem);
            sb.append("Cycles: ").append(cycles);
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
            System.out.println(registers.toString());
        }
    }

    public void run(int start, int end) {
        registers.setPc(start);
        if (verbose) {
            System.out.println(registers.toString());
        }
        while (registers.getPc() != end) {
            int bc = memory.getByte(registers.getAndIncPc());
            Opcode opcode = getOpcode(bc);
            run(opcode);
        }
    }

}


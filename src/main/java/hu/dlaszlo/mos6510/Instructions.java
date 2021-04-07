package hu.dlaszlo.mos6510;

public class Instructions {

    public final static Instruction ORA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        int tmp = registers.getAc() | value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setAc(tmp);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction AND = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getAc() & value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setAc(tmp);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction EOR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getAc() ^ value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setAc(tmp);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction ADC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        boolean prevSign = (registers.getAc() & 0x80) != 0;
        int value = memory.getByte(addr.getAddress());
        int tmp;
        if (registers.isDecimal()) {
            tmp = (registers.getAc() & 0x0f) + (value & 0x0f) + (registers.isCarry() ? 1 : 0);
            if (tmp > 9) {
                tmp += 6;
            }
            if (tmp <= 0x0f) {
                tmp = (tmp & 0x0f) + (registers.getAc() & 0xf0) + (value & 0xf0);
            } else {
                tmp = (tmp & 0x0f) + (registers.getAc() & 0xf0) + (value & 0xf0) + 0x10;
            }
            registers.setZero(((registers.getAc() + value + (registers.isCarry() ? 1 : 0)) & 0xff) == 0);
            registers.setSign((tmp & 0x80) != 0);
            if ((tmp & 0x01f0) > 0x90) {
                tmp += 0x60;
            }
            registers.setCarry((tmp & 0x0ff0) > 0xf0);
        } else {
            tmp = registers.getAc() + value + (registers.isCarry() ? 1 : 0);
            registers.setZero((tmp & 0xff) == 0);
            registers.setSign((tmp & 0x80) != 0);
            registers.setCarry((tmp & ~0xff) != 0);
        }
        registers.setAc(tmp);
        registers.setOverflow(prevSign != registers.isSign());
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction SBC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        boolean prevSign = (registers.getAc() & 0x80) != 0;
        int value = memory.getByte(addr.getAddress());
        int tmp;
        if (registers.isDecimal()) {
            tmp = (registers.getAc() & 0x0f) - (value & 0x0f) + (registers.isCarry() ? 1 : 0) - 1;
            if ((tmp & 0x10) != 0) {
                tmp = ((tmp - 6) & 0x0f) | ((registers.getAc() & 0xf0) - (value & 0xf0) - 0x10);
            } else {
                tmp = (tmp & 0x0f) | ((registers.getAc() & 0xf0) - (value & 0xf0));
            }
            if ((tmp & 0x100) != 0) {
                tmp -= 0x60;
            }
        } else {
            tmp = registers.getAc() - value + (registers.isCarry() ? 1 : 0) - 1;
        }
        registers.setZero((tmp & 0xff) == 0);
        registers.setSign((tmp & 0x80) != 0);
        registers.setCarry((tmp & ~0xff) == 0);
        registers.setOverflow(prevSign != registers.isSign());
        registers.setAc(tmp);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction CMP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getAc() - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) != 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction CPX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getXr() - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) != 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction CPY = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getYr() - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) != 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction DEC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        tmp = (tmp - 1) & 0xff;
        memory.setByte(addr.getAddress(), tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction DEX = (opcode, registers, memory) -> {
        int tmp = (registers.getXr() - 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setXr(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction DEY = (opcode, registers, memory) -> {
        int tmp = (registers.getYr() - 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setYr(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction INC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        tmp = (tmp + 1) & 0xff;
        memory.setByte(addr.getAddress(), tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction INX = (opcode, registers, memory) -> {
        int tmp = (registers.getXr() + 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setXr(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction INY = (opcode, registers, memory) -> {
        int tmp = (registers.getYr() + 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setYr(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction ASL_A = (opcode, registers, memory) -> {
        int tmp = registers.getAc();
        registers.setCarry((tmp & 0x80) != 0);
        tmp = (tmp << 1) & 0xff;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction ASL = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setCarry((tmp & 0x80) != 0);
        tmp = (tmp << 1) & 0xff;
        memory.setByte(addr.getAddress(), tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction ROL_A = (opcode, registers, memory) -> {
        var tmp = registers.getAc() << 1;
        tmp |= registers.isCarry() ? 1 : 0;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & 0x100) != 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction ROL = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        tmp <<= 1;
        tmp |= registers.isCarry() ? 1 : 0;
        memory.setByte(addr.getAddress(), tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & 0x100) != 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction LSR_A = (opcode, registers, memory) -> {
        int tmp = registers.getAc();
        registers.setCarry((tmp & 0x01) != 0);
        tmp >>= 1;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction LSR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setCarry((tmp & 0x01) != 0);
        tmp >>= 1;
        memory.setByte(addr.getAddress(), tmp);
        registers.setSign(false);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction ROR_A = (opcode, registers, memory) -> {
        int tmp = registers.getAc();
        boolean newCarry = (tmp & 0x01) != 0;
        tmp = (tmp >> 1) | (registers.isCarry() ? 0x80 : 0);
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setCarry(newCarry);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction ROR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        boolean newCarry = (tmp & 0x01) != 0;
        tmp = (tmp >> 1) | (registers.isCarry() ? 0x80 : 0);
        memory.setByte(addr.getAddress(), tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setCarry(newCarry);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction TAX = (opcode, registers, memory) -> {
        int tmp = registers.getAc();
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction TXA = (opcode, registers, memory) -> {
        int tmp = registers.getXr();
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction TAY = (opcode, registers, memory) -> {
        int tmp = registers.getAc();
        registers.setYr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction TYA = (opcode, registers, memory) -> {
        int tmp = registers.getYr();
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction TSX = (opcode, registers, memory) -> {
        int tmp = registers.getSp();
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction TXS = (opcode, registers, memory) -> {
        int tmp = registers.getXr();
        registers.setSp(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction PLA = (opcode, registers, memory) -> {
        int tmp = memory.getByte(registers.incAndGetSp() + 0x100);
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction PHA = (opcode, registers, memory) -> {
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getAc());
        return opcode.getCyclesMin();
    };

    public final static Instruction PLP = (opcode, registers, memory) -> {
        int tmp = memory.getByte(registers.incAndGetSp() + 0x100);
        registers.setStatusFlag(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction PHP = (opcode, registers, memory) -> {
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getStatusFlag());
        return opcode.getCyclesMin();
    };

    public final static Instruction BPL = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (!registers.isSign()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BMI = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (registers.isSign()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BVC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (!registers.isOverflow()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BVS = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (registers.isOverflow()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BCC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (!registers.isCarry()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BCS = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (registers.isCarry()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BNE = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (!registers.isZero()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BEQ = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (registers.isZero()) {
            registers.setPc(addr.getAddress());
            return opcode.getCyclesMax();
        }
        return opcode.getCyclesMin();
    };

    public final static Instruction BIT = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setSign((tmp & 0x80) != 0);
        registers.setOverflow((tmp & 0x40) != 0);
        registers.setZero((tmp & registers.getAc()) == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction CLC = (opcode, registers, memory) -> {
        registers.setCarry(false);
        return opcode.getCyclesMin();
    };

    public final static Instruction SEC = (opcode, registers, memory) -> {
        registers.setCarry(true);
        return opcode.getCyclesMin();
    };

    public final static Instruction CLD = (opcode, registers, memory) -> {
        registers.setDecimal(false);
        return opcode.getCyclesMin();
    };

    public final static Instruction SED = (opcode, registers, memory) -> {
        registers.setDecimal(true);
        return opcode.getCyclesMin();
    };

    public final static Instruction CLI = (opcode, registers, memory) -> {
        registers.setInterrupt(false);
        return opcode.getCyclesMin();
    };

    public final static Instruction SEI = (opcode, registers, memory) -> {
        registers.setInterrupt(true);
        return opcode.getCyclesMin();
    };

    public final static Instruction CLV = (opcode, registers, memory) -> {
        registers.setOverflow(false);
        return opcode.getCyclesMin();
    };

    public final static Instruction LDA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction STA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        memory.setByte(addr.getAddress(), registers.getAc());
        return opcode.getCyclesMin();
    };

    public final static Instruction LDX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction STX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        memory.setByte(addr.getAddress(), registers.getXr());
        return opcode.getCyclesMin();
    };

    public final static Instruction LDY = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setYr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction STY = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        memory.setByte(addr.getAddress(), registers.getYr());
        return opcode.getCyclesMin();
    };

    public final static Instruction BRK = (opcode, registers, memory) -> {

        registers.getAndIncPc();
        memory.setByte(registers.getAndDecSp() + 0x100, (registers.getPc() >> 8) & 0xff);
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getPc() & 0xff);
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getStatusFlag());

        registers.setInterrupt(true);
        registers.setPc(0xfffe);
        return opcode.getCyclesMin();
    };

    public final static Instruction RTI = (opcode, registers, memory) -> {
        // plp
        int tmp = memory.getByte(registers.incAndGetSp() + 0x100);
        registers.setStatusFlag(tmp);

        // rts
        int tmp2 = memory.getByte(registers.incAndGetSp() + 0x100);
        tmp2 |= memory.getByte(registers.incAndGetSp() + 0x100) << 8;
        registers.setPc(tmp2);

        return opcode.getCyclesMin();
    };

    public final static Instruction NOP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        return opcode.getCyclesMin();
    };

    public final static Instruction JSR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        memory.setByte(registers.getAndDecSp() + 0x100, (registers.getPc() >> 8) & 0xff);
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getPc() & 0xff);
        registers.setPc(addr.getAddress());
        return opcode.getCyclesMin();
    };

    public final static Instruction RTS = (opcode, registers, memory) -> {
        int tmp = memory.getByte(registers.incAndGetSp() + 0x100);
        tmp |= memory.getByte(registers.incAndGetSp() + 0x100) << 8;
        registers.setPc(tmp);
        return opcode.getCyclesMin();
    };

    public final static Instruction JMP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        registers.setPc(addr.getAddress());
        return opcode.getCyclesMin();
    };

}

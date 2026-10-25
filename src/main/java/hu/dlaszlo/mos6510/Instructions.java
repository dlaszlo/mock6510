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
        int ac = registers.getAc();
        int value = memory.getByte(addr.getAddress());
        int carry = registers.isCarry() ? 1 : 0;
        int tmp;
        if (registers.isDecimal()) {
            tmp = (ac & 0x0f) + (value & 0x0f) + carry;
            if (tmp > 9) {
                tmp += 6;
            }
            if (tmp <= 0x0f) {
                tmp = (tmp & 0x0f) + (ac & 0xf0) + (value & 0xf0);
            } else {
                tmp = (tmp & 0x0f) + (ac & 0xf0) + (value & 0xf0) + 0x10;
            }
            // NMOS: zero flag comes from the binary sum, N and V from the intermediate BCD result
            registers.setZero(((ac + value + carry) & 0xff) == 0);
            registers.setSign((tmp & 0x80) != 0);
            registers.setOverflow(((ac ^ tmp) & 0x80) != 0 && ((ac ^ value) & 0x80) == 0);
            if ((tmp & 0x01f0) > 0x90) {
                tmp += 0x60;
            }
            registers.setCarry((tmp & 0x0ff0) > 0xf0);
        } else {
            tmp = ac + value + carry;
            registers.setZero((tmp & 0xff) == 0);
            registers.setSign((tmp & 0x80) != 0);
            // overflow: both operands have the same sign, and the result's sign differs
            registers.setOverflow(((ac ^ value) & 0x80) == 0 && ((ac ^ tmp) & 0x80) != 0);
            registers.setCarry((tmp & ~0xff) != 0);
        }
        registers.setAc(tmp);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction SBC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int ac = registers.getAc();
        int value = memory.getByte(addr.getAddress());
        int borrow = registers.isCarry() ? 0 : 1;
        // NMOS: all flags come from the binary difference, even in decimal mode
        int tmp = ac - value - borrow;
        registers.setZero((tmp & 0xff) == 0);
        registers.setSign((tmp & 0x80) != 0);
        registers.setCarry((tmp & ~0xff) == 0);
        // overflow: the operands have different signs, and the result's sign differs from A
        registers.setOverflow(((ac ^ tmp) & 0x80) != 0 && ((ac ^ value) & 0x80) != 0);
        if (registers.isDecimal()) {
            int tmpDec = (ac & 0x0f) - (value & 0x0f) - borrow;
            if ((tmpDec & 0x10) != 0) {
                tmpDec = ((tmpDec - 6) & 0x0f) | ((ac & 0xf0) - (value & 0xf0) - 0x10);
            } else {
                tmpDec = (tmpDec & 0x0f) | ((ac & 0xf0) - (value & 0xf0));
            }
            if ((tmpDec & 0x100) != 0) {
                tmpDec -= 0x60;
            }
            registers.setAc(tmpDec);
        } else {
            registers.setAc(tmp);
        }
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction CMP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getAc() - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) == 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction CPX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getXr() - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) == 0);
        return opcode.getCyclesMin();
    };

    public final static Instruction CPY = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int value = memory.getByte(addr.getAddress());
        var tmp = registers.getYr() - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) == 0);
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
        // the pushed copy always has the break flag set
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getStatusFlag() | 0b0001_0000);
        return opcode.getCyclesMin();
    };

    /**
     * Taken branch: +1 cycle, +2 cycles if the target is on another page
     */
    private static int branch(Opcode opcode, Registers registers, Memory memory, boolean condition) {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        if (!condition) {
            return opcode.getCyclesMin();
        }
        registers.setPc(addr.getAddress());
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin() + 1;
    }

    public final static Instruction BPL = (opcode, registers, memory) ->
            branch(opcode, registers, memory, !registers.isSign());

    public final static Instruction BMI = (opcode, registers, memory) ->
            branch(opcode, registers, memory, registers.isSign());

    public final static Instruction BVC = (opcode, registers, memory) ->
            branch(opcode, registers, memory, !registers.isOverflow());

    public final static Instruction BVS = (opcode, registers, memory) ->
            branch(opcode, registers, memory, registers.isOverflow());

    public final static Instruction BCC = (opcode, registers, memory) ->
            branch(opcode, registers, memory, !registers.isCarry());

    public final static Instruction BCS = (opcode, registers, memory) ->
            branch(opcode, registers, memory, registers.isCarry());

    public final static Instruction BNE = (opcode, registers, memory) ->
            branch(opcode, registers, memory, !registers.isZero());

    public final static Instruction BEQ = (opcode, registers, memory) ->
            branch(opcode, registers, memory, registers.isZero());

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
        registers.setBreak_(true);
        memory.setByte(registers.getAndDecSp() + 0x100, registers.getStatusFlag());

        registers.setInterrupt(true);
        registers.setPc(memory.getByte(0xfffe) + memory.getByte(0xffff) * 0x100);
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

    public final static Instruction NOP = (opcode, registers, memory) -> opcode.getCyclesMin();

    public final static Instruction JSR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        // the return address pushed is the last byte of the JSR instruction (next instruction - 1)
        int ret = (registers.getPc() - 1) & 0xffff;
        memory.setByte(registers.getAndDecSp() + 0x100, (ret >> 8) & 0xff);
        memory.setByte(registers.getAndDecSp() + 0x100, ret & 0xff);
        registers.setPc(addr.getAddress());
        return opcode.getCyclesMin();
    };

    public final static Instruction RTS = (opcode, registers, memory) -> {
        int tmp = memory.getByte(registers.incAndGetSp() + 0x100);
        tmp |= memory.getByte(registers.incAndGetSp() + 0x100) << 8;
        registers.setPc(tmp + 1);
        return opcode.getCyclesMin();
    };

    public final static Instruction JMP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        registers.setPc(addr.getAddress());
        return opcode.getCyclesMin();
    };

}

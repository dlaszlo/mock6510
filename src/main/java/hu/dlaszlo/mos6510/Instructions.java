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
        addWithCarry(registers, memory.getByte(addr.getAddress()));
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction SBC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        subtractWithBorrow(registers, memory.getByte(addr.getAddress()));
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    private static void addWithCarry(Registers registers, int value) {
        int ac = registers.getAc();
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
    }

    private static void subtractWithBorrow(Registers registers, int value) {
        int ac = registers.getAc();
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
    }

    public final static Instruction CMP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        compare(registers, registers.getAc(), memory.getByte(addr.getAddress()));
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction CPX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        compare(registers, registers.getXr(), memory.getByte(addr.getAddress()));
        return opcode.getCyclesMin();
    };

    public final static Instruction CPY = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        compare(registers, registers.getYr(), memory.getByte(addr.getAddress()));
        return opcode.getCyclesMin();
    };

    private static void compare(Registers registers, int register, int value) {
        var tmp = register - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) == 0);
    }

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

    public final static Instruction NOP = (opcode, registers, memory) -> {
        if (opcode.getAddressResolver() == AddressingMode.IMP) {
            return opcode.getCyclesMin();
        }
        // the undocumented NOPs read their operand, page crossing costs an extra cycle
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    public final static Instruction JSR = (opcode, registers, memory) -> {
        int addrLo = memory.getByte(registers.getAndIncPc());
        // the return address pushed is the last byte of the JSR instruction (next instruction - 1)
        int ret = registers.getPc();
        memory.setByte(registers.getAndDecSp() + 0x100, (ret >> 8) & 0xff);
        memory.setByte(registers.getAndDecSp() + 0x100, ret & 0xff);
        // the high byte of the target is fetched only after the push, so the push can overwrite it
        int addrHi = memory.getByte(ret);
        registers.setPc(addrLo + addrHi * 0x100);
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

    // Undocumented (illegal) opcodes of the NMOS 6510, as implemented in VICE

    /**
     * ASL + ORA
     */
    public final static Instruction SLO = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setCarry((tmp & 0x80) != 0);
        tmp = (tmp << 1) & 0xff;
        memory.setByte(addr.getAddress(), tmp);
        int ac = registers.getAc() | tmp;
        registers.setAc(ac);
        registers.setSign((ac & 0x80) != 0);
        registers.setZero(ac == 0);
        return opcode.getCyclesMin();
    };

    /**
     * ROL + AND
     */
    public final static Instruction RLA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = (memory.getByte(addr.getAddress()) << 1) | (registers.isCarry() ? 1 : 0);
        registers.setCarry((tmp & 0x100) != 0);
        tmp &= 0xff;
        memory.setByte(addr.getAddress(), tmp);
        int ac = registers.getAc() & tmp;
        registers.setAc(ac);
        registers.setSign((ac & 0x80) != 0);
        registers.setZero(ac == 0);
        return opcode.getCyclesMin();
    };

    /**
     * LSR + EOR
     */
    public final static Instruction SRE = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setCarry((tmp & 0x01) != 0);
        tmp >>= 1;
        memory.setByte(addr.getAddress(), tmp);
        int ac = registers.getAc() ^ tmp;
        registers.setAc(ac);
        registers.setSign((ac & 0x80) != 0);
        registers.setZero(ac == 0);
        return opcode.getCyclesMin();
    };

    /**
     * ROR + ADC
     */
    public final static Instruction RRA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int src = memory.getByte(addr.getAddress());
        int tmp = (src >> 1) | (registers.isCarry() ? 0x80 : 0);
        registers.setCarry((src & 0x01) != 0);
        memory.setByte(addr.getAddress(), tmp);
        addWithCarry(registers, tmp);
        return opcode.getCyclesMin();
    };

    /**
     * DEC + CMP
     */
    public final static Instruction DCP = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = (memory.getByte(addr.getAddress()) - 1) & 0xff;
        memory.setByte(addr.getAddress(), tmp);
        compare(registers, registers.getAc(), tmp);
        return opcode.getCyclesMin();
    };

    /**
     * INC + SBC
     */
    public final static Instruction ISC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = (memory.getByte(addr.getAddress()) + 1) & 0xff;
        memory.setByte(addr.getAddress(), tmp);
        subtractWithBorrow(registers, tmp);
        return opcode.getCyclesMin();
    };

    /**
     * Stores A AND X, flags are not changed
     */
    public final static Instruction SAX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        memory.setByte(addr.getAddress(), registers.getAc() & registers.getXr());
        return opcode.getCyclesMin();
    };

    /**
     * LDA + LDX
     */
    public final static Instruction LAX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress());
        registers.setAc(tmp);
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    /**
     * Memory AND SP into A, X and SP
     */
    public final static Instruction LAS = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = memory.getByte(addr.getAddress()) & registers.getSp();
        registers.setSp(tmp);
        registers.setXr(tmp);
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return addr.isPageCrossed() ? opcode.getCyclesMax() : opcode.getCyclesMin();
    };

    /**
     * AND, then carry = bit 7 of the result
     */
    public final static Instruction ANC = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = registers.getAc() & memory.getByte(addr.getAddress());
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setCarry(registers.isSign());
        return opcode.getCyclesMin();
    };

    /**
     * AND + LSR A
     */
    public final static Instruction ALR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = registers.getAc() & memory.getByte(addr.getAddress());
        registers.setCarry((tmp & 0x01) != 0);
        tmp >>= 1;
        registers.setAc(tmp);
        registers.setSign(false);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    /**
     * AND + ROR A, with its own carry and overflow rules, and a BCD fixup in decimal mode
     */
    public final static Instruction ARR = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = registers.getAc() & memory.getByte(addr.getAddress());
        int ror = (tmp | (registers.isCarry() ? 0x100 : 0)) >> 1;
        if (registers.isDecimal()) {
            registers.setSign(registers.isCarry());
            registers.setZero(ror == 0);
            registers.setOverflow(((ror ^ tmp) & 0x40) != 0);
            if ((tmp & 0x0f) + (tmp & 0x01) > 0x05) {
                ror = (ror & 0xf0) | ((ror + 0x06) & 0x0f);
            }
            if ((tmp & 0xf0) + (tmp & 0x10) > 0x50) {
                ror = (ror & 0x0f) | ((ror + 0x60) & 0xf0);
                registers.setCarry(true);
            } else {
                registers.setCarry(false);
            }
        } else {
            registers.setSign((ror & 0x80) != 0);
            registers.setZero(ror == 0);
            registers.setCarry((ror & 0x40) != 0);
            registers.setOverflow(((ror & 0x40) ^ ((ror & 0x20) << 1)) != 0);
        }
        registers.setAc(ror);
        return opcode.getCyclesMin();
    };

    /**
     * X = (A AND X) - value, carry as in CMP, decimal mode is ignored
     */
    public final static Instruction SBX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = (registers.getAc() & registers.getXr()) - memory.getByte(addr.getAddress());
        registers.setCarry((tmp & ~0xff) == 0);
        tmp &= 0xff;
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    /**
     * Unstable on real hardware: A = (A OR magic) AND X AND value, VICE uses magic = $EF
     */
    public final static Instruction ANE = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = (registers.getAc() | 0xef) & registers.getXr() & memory.getByte(addr.getAddress());
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    /**
     * Unstable on real hardware: A = X = (A OR magic) AND value, VICE uses magic = $EE
     */
    public final static Instruction LXA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        int tmp = (registers.getAc() | 0xee) & memory.getByte(addr.getAddress());
        registers.setAc(tmp);
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        return opcode.getCyclesMin();
    };

    /**
     * SHA, SHX, SHY, TAS: the stored value is ANDed with (high byte of the base address + 1),
     * and on page crossing that value also replaces the high byte of the target address
     */
    private static void storeAndHigh(Memory memory, Address addr, int index, int value) {
        int base = (addr.getAddress() - index) & 0xffff;
        int tmp = value & ((base >> 8) + 1);
        int target = addr.getAddress();
        if (addr.isPageCrossed()) {
            target = (target & 0xff) | (tmp << 8);
        }
        memory.setByte(target, tmp);
    }

    public final static Instruction SHA = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        storeAndHigh(memory, addr, registers.getYr(), registers.getAc() & registers.getXr());
        return opcode.getCyclesMin();
    };

    public final static Instruction SHX = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        storeAndHigh(memory, addr, registers.getYr(), registers.getXr());
        return opcode.getCyclesMin();
    };

    public final static Instruction SHY = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        storeAndHigh(memory, addr, registers.getXr(), registers.getYr());
        return opcode.getCyclesMin();
    };

    /**
     * SP = A AND X, then stores like SHA
     */
    public final static Instruction TAS = (opcode, registers, memory) -> {
        Address addr = opcode.getAddressResolver().getAddress(registers, memory);
        storeAndHigh(memory, addr, registers.getYr(), registers.getAc() & registers.getXr());
        registers.setSp(registers.getAc() & registers.getXr());
        return opcode.getCyclesMin();
    };

    /**
     * Halts the processor, only a reset restarts it
     */
    public final static Instruction JAM = (opcode, registers, memory) -> {
        throw new IllegalStateException(String.format("CPU jammed by opcode 0x%02X at $%04X",
                opcode.getOpcode(), (registers.getPc() - 1) & 0xffff));
    };

}

package hu.dlaszlo.mos6510;

public class AddressingMode {

    /**
     * Implied
     */
    public final static AddressResolver IMP = (registers, memory) -> {
        throw new IllegalStateException("Implied mode");
    };

    /**
     * Immediate addressing: LDA #64
     */
    public final static AddressResolver IMM = (registers, memory) -> {
        return new Address(registers.getAndIncPc(), false, false);
    };

    /**
     * Zeropage addressing: LDA $FE
     */
    public final static AddressResolver ZP = (registers, memory) -> {
        return new Address(memory.getByte(registers.getAndIncPc()), false, false);
    };

    /**
     * Indexed zeropage addressing: LDA $A0,X
     */
    public final static AddressResolver ZPX = (registers, memory) -> {
        int idx = registers.getXr();
        int addr = (memory.getByte(registers.getAndIncPc()) + idx);
        boolean pageBug = addr != (addr & 0xff);
        return new Address(addr & 0xff, false, pageBug);
    };

    /**
     * Indexed zeropage addressing: LDA $A0,Y
     */
    public final static AddressResolver ZPY = (registers, memory) -> {
        int idx = registers.getYr();
        int addr = (memory.getByte(registers.getAndIncPc()) + idx);
        boolean pageBug = addr != (addr & 0xff);
        return new Address(addr & 0xff, false, pageBug);
    };

    /**
     * Indexed-indirect addressing: LDA ($02,X)
     */
    public final static AddressResolver IZX = (registers, memory) -> {
        int idx = registers.getXr();
        int base0 = (memory.getByte(registers.getAndIncPc()) + idx);
        int base = base0 & 0xff;
        boolean pageBug = base0 != base || base + 1 > 0xff;
        int addr = memory.getByte(base) + (memory.getByte((base + 1) & 0xff)) * 0x100;
        return new Address(addr, false, pageBug);
    };

    /**
     * Indirect-indexed addressing: LDA ($02),Y
     */
    public final static AddressResolver IZY = (registers, memory) -> {
        int idx = registers.getYr();
        int base = memory.getByte(registers.getAndIncPc());
        boolean pageBug = base + 1 > 0xff;
        int addr0 = memory.getByte(base)
                + memory.getByte((base + 1) & 0xff) * 0x100;
        int addr = addr0 + idx;
        boolean pageCrossed = (addr0 & 0xff00) != (addr & 0xff00);
        return new Address(addr, pageCrossed, pageBug);
    };

    /**
     * Absolute addressing: LDA $1234
     */
    public final static AddressResolver ABS = (registers, memory) -> {
        return new Address(memory.getByte(registers.getAndIncPc())
                + (memory.getByte(registers.getAndIncPc())) * 0x100, false, false);
    };

    /**
     * Indexed absolute addressing: LDA $8000,X
     */
    public final static AddressResolver ABX = (registers, memory) -> {
        int idx = registers.getXr();
        int base = memory.getByte(registers.getAndIncPc())
                + (memory.getByte(registers.getAndIncPc())) * 0x100;
        int addr = base + idx;
        boolean pageCrossed = (base & 0xff00) != (addr & 0xff00);
        return new Address(addr, pageCrossed, false);
    };

    /**
     * Indexed absolute addressing: LDA $8000,Y
     */
    public final static AddressResolver ABY = (registers, memory) -> {
        int idx = registers.getYr();
        int base = memory.getByte(registers.getAndIncPc())
                + (memory.getByte(registers.getAndIncPc())) * 0x100;
        int addr = base + idx;
        boolean pageCrossed = (base & 0xff00) != (addr & 0xff00);
        return new Address(addr, pageCrossed, false);
    };

    /**
     * Absolute-indirect addressing: JMP ($F000)
     */
    public final static AddressResolver IND = (registers, memory) -> {
        int addrLo = memory.getByte(registers.getAndIncPc());
        int addrHi = memory.getByte(registers.getAndIncPc());
        int addr1 = addrLo + addrHi * 0x100;
        int addr2 = ((addrLo + 1) & 0xff) + addrHi * 0x100;
        boolean pageBug = ((addr1 + 1) & 0xff00) != (addr2 & 0xff00);
        return new Address(memory.getByte(addr1)
                + memory.getByte(addr2) * 0x100, false, pageBug);
    };

    /**
     * Relative addressing
     */
    public final static AddressResolver REL = (registers, memory) -> {
        return new Address((registers.getPc() + 1)
                + memory.getByte(registers.getAndIncPc()), false, false);
    };

}

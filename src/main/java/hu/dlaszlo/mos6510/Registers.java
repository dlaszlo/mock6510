package hu.dlaszlo.mos6510;

import org.apache.commons.lang3.StringUtils;

public class Registers {

    // Register: A
    private int ac = 0x00;

    // Register: X
    private int xr = 0x00;

    // Register: Y
    private int yr = 0x00;

    // Register: Stack pointer
    private int sp = 0xff;

    // Program counter
    private int pc = 0x00;

    // Status flags
    private boolean carry = false;  // carry
    private boolean zero = false;  // zero
    private boolean interrupt = false;  // interrupt disable
    private boolean decimal = false;  // decimal mode
    private boolean break_ = false;  // interrupt triggered by break
    private boolean overflow = false;  // overflow
    private boolean sign = false;  // negative

    public Registers copy() {
        Registers reg = new Registers();
        reg.setAc(this.getAc());
        reg.setXr(this.getXr());
        reg.setYr(this.getYr());
        reg.setSp(this.getSp());
        reg.setPc(this.getPc());
        reg.setCarry(this.isCarry());
        reg.setZero(this.isZero());
        reg.setInterrupt(this.isInterrupt());
        reg.setDecimal(this.isDecimal());
        reg.setBreak_(this.isBreak_());
        reg.setOverflow(this.isOverflow());
        reg.setSign(this.isSign());
        return reg;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac & 0xff;
    }

    public int getXr() {
        return xr;
    }

    public void setXr(int xr) {
        this.xr = xr & 0xff;
    }

    public int getYr() {
        return yr;
    }

    public void setYr(int yr) {
        this.yr = yr & 0xff;
    }

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp & 0xff;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc & 0xffff;
    }

    public boolean isCarry() {
        return carry;
    }

    public void setCarry(boolean carry) {
        this.carry = carry;
    }

    public boolean isZero() {
        return zero;
    }

    public void setZero(boolean zero) {
        this.zero = zero;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public boolean isDecimal() {
        return decimal;
    }

    public void setDecimal(boolean decimal) {
        this.decimal = decimal;
    }

    public boolean isBreak_() {
        return break_;
    }

    public void setBreak_(boolean break_) {
        this.break_ = break_;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }

    public int getStatusFlag() {
        return (isCarry() ? 0b0000_0001 : 0)
                + (isZero() ? 0b0000_0010 : 0)
                + (isInterrupt() ? 0b0000_0100 : 0)
                + (isDecimal() ? 0b0000_1000 : 0)
                + (isBreak_() ? 0b0001_0000 : 0)
                + 0b0010_0000
                + (isOverflow() ? 0b0100_0000 : 0)
                + (isSign() ? 0b1000_0000 : 0);
    }

    public void setStatusFlag(int statusFlag) {
        setCarry((statusFlag & 0b0000_0001) != 0);
        setZero((statusFlag & 0b0000_0010) != 0);
        setInterrupt((statusFlag & 0b0000_0100) != 0);
        setDecimal((statusFlag & 0b0000_1000) != 0);
        setBreak_((statusFlag & 0b0001_0000) != 0);
        setOverflow((statusFlag & 0b0100_0000) != 0);
        setSign((statusFlag & 0b1000_0000) != 0);
    }

    public int getAndIncPc() {
        int tmp = pc;
        pc = (pc + 1) & 0xffff;
        return tmp;
    }

    public int getAndDecSp() {
        int tmp = sp;
        sp = (sp - 1) & 0xff;
        return tmp;
    }

    public int incAndGetSp() {
        sp = (sp + 1) & 0xff;
        return sp;
    }


    @Override
    public String toString() {
        return StringUtils.repeat(" ", 40) + "A  X  Y  S  P    NV-BDIZC \n" +
                StringUtils.repeat(" ", 40) + String.format("%02X ", ac) +
                String.format("%02X ", xr) +
                String.format("%02X ", yr) +
                String.format("%02X ", sp) +
                String.format("%04X ", pc) +
                (sign ? "1" : "0") +
                (overflow ? "1" : "0") +
                "1" +
                (break_ ? "1" : "0") +
                (decimal ? "1" : "0") +
                (interrupt ? "1" : "0") +
                (zero ? "1" : "0") +
                (carry ? "1" : "0");
    }

}

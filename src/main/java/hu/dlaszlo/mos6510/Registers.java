package hu.dlaszlo.mos6510;

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
        return "A=" + String.format("%02X", ac) +
                ", X=" + String.format("%02X", xr) +
                ", Y=" + String.format("%02X", yr) +
                ", S=" + String.format("%02X", sp) +
                ", P=" + String.format("%04X", pc) +
                ", c=" + (carry ? "1" : "0") +
                ", z=" + (zero ? "1" : "0") +
                ", i=" + (interrupt ? "1" : "0") +
                ", d=" + (decimal ? "1" : "0") +
                ", b=" + (break_ ? "1" : "0") +
                ", v=" + (overflow ? "1" : "0") +
                ", n=" + (sign ? "1" : "0");
    }

}

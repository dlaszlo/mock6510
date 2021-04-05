package hu.dlaszlo.cpu6510;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Cpu6510 {

    private final byte[] memory = new byte[65536];

    private final Map<Opcode, Runnable> opcodes = new HashMap<>();

    private boolean verbose = true;

    private Registers registers = new Registers();

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public byte[] getMemory() {
        return memory;
    }

    public void setMemory(int pos, int b) {
        memory[pos] = (byte) (b & 0xff);
    }

    public int getMemory(int pos) {
        int val = memory[pos];
        return val & 0xff;
    }

    /**
     * Immediate addressing: LDA #64
     *
     * @return address
     */
    private int addrImmediate() {
        return registers.getAndIncPc();
    }

    /**
     * Zeropage addressing: LDA $FE
     *
     * @return address
     */
    private int addrZeropage() {
        return memory[registers.getAndIncPc()];
    }

    /**
     * Indexed zeropage addressing: LDA $A0,X, LDA $A0,Y
     *
     * @param regValue value of X or Y register
     * @return address
     */
    private int addrIndexedZeropage(int regValue) {
        int idx = regValue;
        return (memory[registers.getAndIncPc()] + idx) & 0xff;
    }

    /**
     * Indexed-indirect addressing: LDA ($02,X)
     *
     * @return address
     */
    private int addrIndexedIndirect() {
        int idx = registers.getXr();
        int addr = (memory[registers.getAndIncPc()] + idx) & 0xff;
        return memory[addr]
                + (memory[(addr + 1) & 0xff]) * 0x100;
    }

    /**
     * Indirect-indexed addressing: LDA ($02),Y
     *
     * @return address
     */
    private int addrIndirectIndexed() {
        int idx = registers.getYr();
        int addr = memory[registers.getAndIncPc()];
        return memory[addr]
                + memory[(addr + 1) & 0xff] * 0x100
                + idx;
    }

    /**
     * Absolute addressing: LDA $1234
     *
     * @return address
     */
    private int addrAbsolute() {
        return memory[registers.getAndIncPc()]
                + (memory[registers.getAndIncPc()]) * 0x100;
    }

    /**
     * Indexed absolute addressing: LDA $8000,X
     *
     * @param regValue value of X or Y register
     * @return address
     */
    private int addrAbsoluteIndexed(int regValue) {
        return memory[registers.getAndIncPc()]
                + (memory[registers.getAndIncPc()]) * 0x100
                + regValue;
    }

    /**
     * Absolute-indirect addressing: JMP ($F000)
     *
     * @return address
     */
    private int addrAbsoluteIndirect() {
        int addrLo = memory[registers.getAndIncPc()] & 0xff;
        int addrHi = memory[registers.getAndIncPc()] & 0xff;
        int addr1 = addrLo + addrHi * 0x100;
        int addr2 = ((addrLo + 1) & 0xff) + addrHi * 0x100;
        return memory[addr1] + memory[addr2];
    }

    /**
     * Relative addressing
     *
     * @return address
     */
    private int addrRelative() {
        return (registers.getPc() + 1) + memory[registers.getAndIncPc()];
    }

    private void ora(int addr) {
        var value = memory[addr] & 0xff;
        var tmp = registers.getAc() | value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setAc(tmp);
    }

    private void and(int addr) {
        int value = memory[addr] & 0xff;
        var tmp = registers.getAc() & value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setAc(tmp);
    }

    private void eor(int addr) {
        int value = memory[addr] & 0xff;
        var tmp = registers.getAc() ^ value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setAc(tmp);
    }

    private void adc(int addr) {
        boolean prevSign = (registers.getAc() & 0x80) != 0;
        int value = memory[addr] & 0xff;
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
    }

    private void sbc(int addr) {
        boolean prevSign = (registers.getAc() & 0x80) != 0;
        int value = memory[addr] & 0xff;
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
    }

    private void cmp(int addr, int regValue) {
        int value = memory[addr] & 0xff;
        var tmp = regValue - value;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & ~0xff) != 0);
    }

    private void dec(int addr) {
        int tmp = memory[addr] & 0xff;
        tmp = (tmp - 1) & 0xff;
        memory[addr] = (byte) tmp;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void dex() {
        int tmp = (registers.getXr() - 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setXr(tmp);
    }

    private void dey() {
        int tmp = (registers.getYr() - 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setYr(tmp);
    }

    private void inc(int addr) {
        int tmp = memory[addr] & 0xff;
        tmp = (tmp + 1) & 0xff;
        memory[addr] = (byte) tmp;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void inx() {
        int tmp = (registers.getXr() + 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setXr(tmp);
    }

    private void iny() {
        int tmp = (registers.getYr() + 1) & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
        registers.setYr(tmp);
    }

    private void asl_a() {
        int tmp = registers.getAc();
        registers.setCarry((tmp & 0x80) != 0);
        tmp = (tmp << 1) & 0xff;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void asl(int addr) {
        int tmp = memory[addr] & 0xff;
        registers.setCarry((tmp & 0x80) != 0);
        tmp = (tmp << 1) & 0xff;
        memory[addr] = (byte) tmp;
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void rol_a() {
        var tmp = registers.getAc() << 1;
        tmp |= registers.isCarry() ? 1 : 0;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & 0x100) != 0);
    }

    private void rol(int addr) {
        int tmp = (memory[addr] & 0xff) << 1;
        tmp |= registers.isCarry() ? 1 : 0;
        memory[addr] = (byte) (tmp & 0xff);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
        registers.setCarry((tmp & 0x100) != 0);
    }

    private void lsr_a() {
        int tmp = registers.getAc();
        registers.setCarry((tmp & 0x01) != 0);
        tmp >>= 1;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void lsr(int addr) {
        int tmp = memory[addr] & 0xff;
        registers.setCarry((tmp & 0x01) != 0);
        tmp >>= 1;
        memory[addr] = (byte) tmp;
        registers.setSign(false);
        registers.setZero(tmp == 0);
    }

    private void ror_a() {
        int tmp = registers.getAc();
        boolean newCarry = (tmp & 0x01) != 0;
        tmp = (tmp >> 1) | (registers.isCarry() ? 0x80 : 0);
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setCarry(newCarry);
        registers.setZero(tmp == 0);
    }

    private void ror(int addr) {
        int tmp = memory[addr] & 0xff;
        boolean newCarry = (tmp & 0x01) != 0;
        tmp = (tmp >> 1) | (registers.isCarry() ? 0x80 : 0);
        memory[addr] = (byte) tmp;
        registers.setSign((tmp & 0x80) != 0);
        registers.setCarry(newCarry);
        registers.setZero(tmp == 0);
    }

    private void tax() {
        int tmp = registers.getAc();
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void txa() {
        int tmp = registers.getXr();
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void tay() {
        int tmp = registers.getAc();
        registers.setYr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void tya() {
        int tmp = registers.getYr();
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void tsx() {
        int tmp = registers.getSp();
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void txs() {
        int tmp = registers.getXr();
        registers.setSp(tmp);
    }

    private int pop() {
        return memory[registers.incAndGetSp() + 0x100] & 0xff;
    }

    private void push(int value) {
        memory[registers.getAndDecSp() + 0x100] = (byte) (value & 0xff);
    }

    private void pla() {
        int tmp = pop();
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero((tmp & 0xff) == 0);
    }

    private void pha() {
        push(registers.getAc());
    }

    private void plp() {
        int tmp = pop();
        registers.setCarry((tmp & 0b0000_0001) != 0);
        registers.setZero((tmp & 0b0000_0010) != 0);
        registers.setInterrupt((tmp & 0b0000_0100) != 0);
        registers.setDecimal((tmp & 0b0000_1000) != 0);
        registers.setBreak_((tmp & 0b0001_0000) != 0);
        registers.setOverflow((tmp & 0b0100_0000) != 0);
        registers.setSign((tmp & 0b1000_0000) != 0);
    }

    private void php() {
        int tmp = (registers.isCarry() ? 0b0000_0001 : 0)
                + (registers.isZero() ? 0b0000_0010 : 0)
                + (registers.isInterrupt() ? 0b0000_0100 : 0)
                + (registers.isDecimal() ? 0b0000_1000 : 0)
                + (registers.isBreak_() ? 0b0001_0000 : 0)
                + 0b0010_0000
                + (registers.isOverflow() ? 0b0100_0000 : 0)
                + (registers.isSign() ? 0b1000_0000 : 0);
        push(tmp);
    }

    private void bpl(int addr) {
        if (!registers.isSign()) {
            registers.setPc(addr);
        }
    }

    private void bmi(int addr) {
        if (registers.isSign()) {
            registers.setPc(addr);
        }
    }

    private void bvc(int addr) {
        if (!registers.isOverflow()) {
            registers.setPc(addr);
        }
    }

    private void bvs(int addr) {
        if (registers.isOverflow()) {
            registers.setPc(addr);
        }
    }

    private void bcc(int addr) {
        if (!registers.isCarry()) {
            registers.setPc(addr);
        }
    }

    private void bcs(int addr) {
        if (registers.isCarry()) {
            registers.setPc(addr);
        }
    }

    private void bne(int addr) {
        if (!registers.isZero()) {
            registers.setPc(addr);
        }
    }

    private void beq(int addr) {
        if (registers.isZero()) {
            registers.setPc(addr);
        }
    }

    private void bit(int addr) {
        int tmp = memory[addr] & 0xff;
        registers.setSign((tmp & 0x80) != 0);
        registers.setOverflow((tmp & 0x40) != 0);
        registers.setZero((tmp & registers.getAc()) == 0);
    }

    private void clc() {
        registers.setCarry(false);
    }

    private void sec() {
        registers.setCarry(true);
    }

    private void cld() {
        registers.setDecimal(false);
    }

    private void sed() {
        registers.setDecimal(true);
    }

    private void cli() {
        registers.setInterrupt(false);
    }

    private void sei() {
        registers.setInterrupt(true);
    }

    private void clv() {
        registers.setOverflow(false);
    }

    private void lda(int addr) {
        int tmp = memory[addr] & 0xff;
        registers.setAc(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void sta(int addr) {
        memory[addr] = (byte) registers.getAc();
    }

    private void ldx(int addr) {
        int tmp = memory[addr] & 0xff;
        registers.setXr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void stx(int addr) {
        memory[addr] = (byte) registers.getXr();
    }

    private void ldy(int addr) {
        int tmp = memory[addr] & 0xff;
        registers.setYr(tmp);
        registers.setSign((tmp & 0x80) != 0);
        registers.setZero(tmp == 0);
    }

    private void sty(int addr) {
        memory[addr] = (byte) registers.getYr();
    }

    private void brk() {
        jsr(registers.getAndIncPc());
        php();
        registers.setInterrupt(true);
        registers.setPc(0xfffe);
    }

    private void rti() {
        plp();
        rts();
    }

    private void nop() {
        // no-op
    }

    private void jsr(int addr) {
        push((registers.getPc() >> 8) & 0xff);
        push(registers.getPc() & 0xff);
        registers.setPc(addr);
    }

    private void rts() {
        int tmp = pop();
        tmp |= pop() << 8;
        registers.setPc(tmp);
    }

    private void jmp(int addr) {
        registers.setPc(addr);
    }

    public Cpu6510() {
        opcodes.put(Opcode.ORA_IMM, () -> ora(addrImmediate()));
        opcodes.put(Opcode.ORA_ZP, () -> ora(addrZeropage()));
        opcodes.put(Opcode.ORA_ZPI, () -> ora(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.ORA_ZPIX, () -> ora(addrIndexedIndirect()));
        opcodes.put(Opcode.ORA_ZPIY, () -> ora(addrIndirectIndexed()));
        opcodes.put(Opcode.ORA_ABS, () -> ora(addrAbsolute()));
        opcodes.put(Opcode.ORA_ABSX, () -> ora(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.ORA_ABSY, () -> ora(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.AND_IMM, () -> and(addrImmediate()));
        opcodes.put(Opcode.AND_ZP, () -> and(addrZeropage()));
        opcodes.put(Opcode.AND_ZPI, () -> and(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.AND_ZPIX, () -> and(addrIndexedIndirect()));
        opcodes.put(Opcode.AND_ZPIY, () -> and(addrIndirectIndexed()));
        opcodes.put(Opcode.AND_ABS, () -> and(addrAbsolute()));
        opcodes.put(Opcode.AND_ABSX, () -> and(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.AND_ABSY, () -> and(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.EOR_IMM, () -> eor(addrImmediate()));
        opcodes.put(Opcode.EOR_ZP, () -> eor(addrZeropage()));
        opcodes.put(Opcode.EOR_ZPI, () -> eor(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.EOR_ZPIX, () -> eor(addrIndexedIndirect()));
        opcodes.put(Opcode.EOR_ZPIY, () -> eor(addrIndirectIndexed()));
        opcodes.put(Opcode.EOR_ABS, () -> eor(addrAbsolute()));
        opcodes.put(Opcode.EOR_ABSX, () -> eor(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.EOR_ABSY, () -> eor(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.ADC_IMM, () -> adc(addrImmediate()));
        opcodes.put(Opcode.ADC_ZP, () -> adc(addrZeropage()));
        opcodes.put(Opcode.ADC_ZPI, () -> adc(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.ADC_ZPIX, () -> adc(addrIndexedIndirect()));
        opcodes.put(Opcode.ADC_ZPIY, () -> adc(addrIndirectIndexed()));
        opcodes.put(Opcode.ADC_ABS, () -> adc(addrAbsolute()));
        opcodes.put(Opcode.ADC_ABSX, () -> adc(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.ADC_ABSY, () -> adc(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.SBC_IMM, () -> sbc(addrImmediate()));
        opcodes.put(Opcode.SBC_ZP, () -> sbc(addrZeropage()));
        opcodes.put(Opcode.SBC_ZPI, () -> sbc(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.SBC_ZPIX, () -> sbc(addrIndexedIndirect()));
        opcodes.put(Opcode.SBC_ZPIY, () -> sbc(addrIndirectIndexed()));
        opcodes.put(Opcode.SBC_ABS, () -> sbc(addrAbsolute()));
        opcodes.put(Opcode.SBC_ABSX, () -> sbc(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.SBC_ABSY, () -> sbc(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.CMP_IMM, () -> cmp(addrImmediate(), registers.getAc()));
        opcodes.put(Opcode.CMP_ZP, () -> cmp(addrZeropage(), registers.getAc()));
        opcodes.put(Opcode.CMP_ZPI, () -> cmp(addrIndexedZeropage(registers.getXr()), registers.getAc()));
        opcodes.put(Opcode.CMP_ZPIX, () -> cmp(addrIndexedIndirect(), registers.getAc()));
        opcodes.put(Opcode.CMP_ZPIY, () -> cmp(addrIndirectIndexed(), registers.getAc()));
        opcodes.put(Opcode.CMP_ABS, () -> cmp(addrAbsolute(), registers.getAc()));
        opcodes.put(Opcode.CMP_ABSX, () -> cmp(addrAbsoluteIndexed(registers.getXr()), registers.getAc()));
        opcodes.put(Opcode.CMP_ABSY, () -> cmp(addrAbsoluteIndexed(registers.getYr()), registers.getAc()));
        opcodes.put(Opcode.CPX_IMM, () -> cmp(addrImmediate(), registers.getXr()));
        opcodes.put(Opcode.CPX_ZP, () -> cmp(addrZeropage(), registers.getXr()));
        opcodes.put(Opcode.CPX_ABS, () -> cmp(addrAbsolute(), registers.getXr()));
        opcodes.put(Opcode.CPY_IMM, () -> cmp(addrImmediate(), registers.getYr()));
        opcodes.put(Opcode.CPY_ZP, () -> cmp(addrZeropage(), registers.getYr()));
        opcodes.put(Opcode.CPY_ABS, () -> cmp(addrAbsolute(), registers.getYr()));
        opcodes.put(Opcode.DEC_ZP, () -> dec(addrZeropage()));
        opcodes.put(Opcode.DEC_ZPI, () -> dec(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.DEC_ABS, () -> dec(addrAbsolute()));
        opcodes.put(Opcode.DEC_ABSX, () -> dec(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.DEX, () -> dex());
        opcodes.put(Opcode.DEY, () -> dey());
        opcodes.put(Opcode.INC_ZP, () -> inc(addrZeropage()));
        opcodes.put(Opcode.INC_ZPI, () -> inc(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.INC_ABS, () -> inc(addrAbsolute()));
        opcodes.put(Opcode.INC_ABSX, () -> inc(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.INX, () -> inx());
        opcodes.put(Opcode.INY, () -> iny());
        opcodes.put(Opcode.ASL, () -> asl_a());
        opcodes.put(Opcode.ASL_ZP, () -> asl(addrZeropage()));
        opcodes.put(Opcode.ASL_ZPI, () -> asl(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.ASL_ABS, () -> asl(addrAbsolute()));
        opcodes.put(Opcode.ASL_ABSX, () -> asl(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.ROL, () -> rol_a());
        opcodes.put(Opcode.ROL_ZP, () -> rol(addrZeropage()));
        opcodes.put(Opcode.ROL_ZPI, () -> rol(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.ROL_ABS, () -> rol(addrAbsolute()));
        opcodes.put(Opcode.ROL_ABSX, () -> rol(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.LSR, () -> lsr_a());
        opcodes.put(Opcode.LSR_ZP, () -> lsr(addrZeropage()));
        opcodes.put(Opcode.LSR_ZPI, () -> lsr(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.LSR_ABS, () -> lsr(addrAbsolute()));
        opcodes.put(Opcode.LSR_ABSX, () -> lsr(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.ROR, () -> ror_a());
        opcodes.put(Opcode.ROR_ZP, () -> ror(addrZeropage()));
        opcodes.put(Opcode.ROR_ZPI, () -> ror(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.ROR_ABS, () -> ror(addrAbsolute()));
        opcodes.put(Opcode.ROR_ABSX, () -> ror(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.TAX, () -> tax());
        opcodes.put(Opcode.TXA, () -> txa());
        opcodes.put(Opcode.TAY, () -> tay());
        opcodes.put(Opcode.TYA, () -> tya());
        opcodes.put(Opcode.TSX, () -> tsx());
        opcodes.put(Opcode.TXS, () -> txs());
        opcodes.put(Opcode.PLA, () -> pla());
        opcodes.put(Opcode.PHA, () -> pha());
        opcodes.put(Opcode.PLP, () -> plp());
        opcodes.put(Opcode.PHP, () -> php());
        opcodes.put(Opcode.BPL, () -> bpl(addrRelative()));
        opcodes.put(Opcode.BMI, () -> bmi(addrRelative()));
        opcodes.put(Opcode.BVC, () -> bvc(addrRelative()));
        opcodes.put(Opcode.BVS, () -> bvs(addrRelative()));
        opcodes.put(Opcode.BCC, () -> bcc(addrRelative()));
        opcodes.put(Opcode.BCS, () -> bcs(addrRelative()));
        opcodes.put(Opcode.BNE, () -> bne(addrRelative()));
        opcodes.put(Opcode.BEQ, () -> beq(addrRelative()));
        opcodes.put(Opcode.BIT_ZP, () -> bit(addrZeropage()));
        opcodes.put(Opcode.BIT_ABS, () -> bit(addrAbsolute()));
        opcodes.put(Opcode.CLC, () -> clc());
        opcodes.put(Opcode.SEC, () -> sec());
        opcodes.put(Opcode.CLD, () -> cld());
        opcodes.put(Opcode.SED, () -> sed());
        opcodes.put(Opcode.CLI, () -> cli());
        opcodes.put(Opcode.SEI, () -> sei());
        opcodes.put(Opcode.CLV, () -> clv());
        opcodes.put(Opcode.LDA_IMM, () -> lda(addrImmediate()));
        opcodes.put(Opcode.LDA_ZP, () -> lda(addrZeropage()));
        opcodes.put(Opcode.LDA_ZPI, () -> lda(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.LDA_ZPIX, () -> lda(addrIndexedIndirect()));
        opcodes.put(Opcode.LDA_ZPIY, () -> lda(addrIndirectIndexed()));
        opcodes.put(Opcode.LDA_ABS, () -> lda(addrAbsolute()));
        opcodes.put(Opcode.LDA_ABSX, () -> lda(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.LDA_ABSY, () -> lda(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.STA_ZP, () -> sta(addrZeropage()));
        opcodes.put(Opcode.STA_ZPI, () -> sta(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.STA_ZPIX, () -> sta(addrIndexedIndirect()));
        opcodes.put(Opcode.STA_ZPIY, () -> sta(addrIndirectIndexed()));
        opcodes.put(Opcode.STA_ABS, () -> sta(addrAbsolute()));
        opcodes.put(Opcode.STA_ABSX, () -> sta(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.STA_ABSY, () -> sta(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.LDX_IMM, () -> ldx(addrImmediate()));
        opcodes.put(Opcode.LDX_ZP, () -> ldx(addrZeropage()));
        opcodes.put(Opcode.LDX_ZPI, () -> ldx(addrIndexedZeropage(registers.getYr())));
        opcodes.put(Opcode.LDX_ABS, () -> ldx(addrAbsolute()));
        opcodes.put(Opcode.LDX_ABSY, () -> ldx(addrAbsoluteIndexed(registers.getYr())));
        opcodes.put(Opcode.STX_ZP, () -> stx(addrZeropage()));
        opcodes.put(Opcode.STX_ZPI, () -> stx(addrIndexedZeropage(registers.getYr())));
        opcodes.put(Opcode.STX_ABS, () -> stx(addrAbsolute()));
        opcodes.put(Opcode.LDY_IMM, () -> ldy(addrImmediate()));
        opcodes.put(Opcode.LDY_ZP, () -> ldy(addrZeropage()));
        opcodes.put(Opcode.LDY_ZPI, () -> ldy(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.LDY_ABS, () -> ldy(addrAbsolute()));
        opcodes.put(Opcode.LDY_ABSX, () -> ldy(addrAbsoluteIndexed(registers.getXr())));
        opcodes.put(Opcode.STY_ZP, () -> sty(addrZeropage()));
        opcodes.put(Opcode.STY_ZPI, () -> sty(addrIndexedZeropage(registers.getXr())));
        opcodes.put(Opcode.STY_ABS, () -> sty(addrAbsolute()));
        opcodes.put(Opcode.BRK, () -> brk());
        opcodes.put(Opcode.RTI, () -> rti());
        opcodes.put(Opcode.JSR, () -> jsr(addrAbsolute()));
        opcodes.put(Opcode.RTS, () -> rts());
        opcodes.put(Opcode.JMP, () -> jmp(addrAbsolute()));
        opcodes.put(Opcode.JMP_ABSI, () -> jmp(addrAbsoluteIndirect()));
        opcodes.put(Opcode.NOP, () -> nop());

        Set<Byte> b = new HashSet<>();
        for (Opcode o : Opcode.values()) {
            if (!b.add(o.getOpcode())) {
                throw new IllegalArgumentException("Duplicated opcode: " + o.name());
            }
            if (!opcodes.containsKey(o)) {
                throw new IllegalArgumentException("Missing implementation: " + o.name());
            }
        }

    }

    public void load(byte[] program) {
        int d = program[0] + program[1] * 0x100;
        for (int s = 2; s < program.length; s++) {
            memory[d] = program[s];
            d++;
        }
    }

    private void print(Opcode opcode) {
        if (verbose) {
            int pc = registers.getPc();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(".%04X   ", pc - 1));
            sb.append(String.format("%02X ", opcode.getOpcode()));
            int p1 = memory[pc] & 0xff;
            int p2 = memory[pc + 1] & 0xff;
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
            System.out.println(sb.toString());
        }
    }

    private Opcode getOpcode(byte bc) {
        Opcode opcode = Opcode.getOpcode(bc);
        if (opcode == null) {
            throw new IllegalArgumentException(String.format("Invalid opcode: 0x%02X ", bc));
        }
        print(opcode);
        return opcode;
    }

    private void run(Opcode opcode) {
        Runnable r = opcodes.get(opcode);
        if (r == null) {
            throw new IllegalArgumentException("Not implemented: " + opcode.name());
        }
        r.run();
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
            byte bc = memory[registers.getAndIncPc()];
            Opcode opcode = getOpcode(bc);
            run(opcode);
        }
    }

}


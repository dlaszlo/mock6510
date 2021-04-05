package hu.dlaszlo.cpu6510;

import java.util.*;

public class Mock6510 {

    private final byte[] memory = new byte[65536];

    private final Map<Opcode, Runnable> opcodes = new HashMap<>();

    private boolean verbose = true;

    // Register: A
    private int A = 0x00;

    // Register: X
    private int X = 0x00;

    // Register: Y
    private int Y = 0x00;

    // Register: Stack pointer
    private int S = 0xff;

    // Program counter
    private int P = 0x00;

    // Status flags
    private boolean c = false;  // carry
    private boolean z = false;  // zero
    private boolean i = false;  // interrupt disable
    private boolean d = false;  // decimal mode
    private boolean b = false;  // interrupt triggered by BRK
    private boolean v = false;  // overflow
    private boolean n = false;  // negative

    public int getA() {
        return A;
    }

    public void setA(int a) {
        A = a & 0b1111_11111;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x & 0b1111_11111;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y & 0b1111_11111;
    }

    public int getS() {
        return S;
    }

    public void setS(int s) {
        S = s & 0b1111_11111;
    }

    public int getP() {
        return P;
    }

    public void setP(int p) {
        P = p;
    }

    public boolean isC() {
        return c;
    }

    public void setC(boolean c) {
        this.c = c;
    }

    public boolean isZ() {
        return z;
    }

    public void setZ(boolean z) {
        this.z = z;
    }

    public boolean isI() {
        return i;
    }

    public void setI(boolean i) {
        this.i = i;
    }

    public boolean isD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public boolean isV() {
        return v;
    }

    public void setV(boolean v) {
        this.v = v;
    }

    public boolean isN() {
        return n;
    }

    public void setN(boolean n) {
        this.n = n;
    }

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
        memory[pos] = (byte) (b & 0b1111_1111);
    }

    public int getMemory(int pos) {
        int val = memory[pos];
        return val & 0b1111_1111;
    }

    /**
     * Immediate addressing: LDA #64
     *
     * @return address
     */
    private int addrImmediate() {
        return P++;
    }

    /**
     * Zeropage addressing: LDA $FE
     *
     * @return address
     */
    private int addrZeropage() {
        return memory[P++];
    }

    /**
     * Indexed zeropage addressing: LDA $A0,X, LDA $A0,Y
     *
     * @param xy value of X or Y register
     * @return address
     */
    private int addrIndexedZeropage(int xy) {
        int idx = xy & 0b1111_1111;
        return memory[P++] + idx;
    }

    /**
     * Indexed-indirect addressing: LDA ($02,X)
     *
     * @return address
     */
    private int addrIndexedIndirect() {
        int idx = X & 0b1111_1111;
        int addr0 = memory[P++] + idx;
        return memory[addr0] + (memory[addr0 + 1]) * 256;
    }

    /**
     * Indirect-indexed addressing: LDA ($02),Y
     *
     * @return address
     */
    private int addrIndirectIndexed() {
        int idx = Y & 0b1111_1111;
        int addr0 = memory[P++];
        return memory[addr0] + memory[addr0 + 1] * 256 + idx;
    }

    /**
     * Absolute addressing: LDA $1234
     *
     * @return address
     */
    private int addrAbsolute() {
        return memory[P++] + (memory[P++]) * 256;
    }

    /**
     * Indexed absolute addressing: LDA $8000,X
     *
     * @param xy value of X or Y register
     * @return address
     */
    private int addrAbsoluteIndexed(int xy) {
        int idx = xy & 0b1111_1111;
        return memory[P++] + (memory[P++]) * 256 + idx;
    }

    /**
     * Absolute-indirect addressing: JMP ($F000)
     *
     * @return address
     */
    private int addrAbsoluteIndirect() {
        int addr0 = memory[P++] + memory[P++] * 256;
        return memory[addr0] + memory[addr0 + 1] * 256;
    }

    /**
     * Relative addressing
     *
     * @return address
     */
    private int addrRelative() {
        return (P + 1) + memory[P++];
    }

    private void ora(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        A = (A | $) & 0b1111_11111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void and(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        A = (A & $) & 0b1111_11111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void eor(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        A = (A ^ $) & 0b1111_11111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void adc(int addr) {
        boolean np = (A & 0b1000_0000) != 0;
        int $ = memory[addr] & 0b1111_1111;
        if (d) {
            throw new IllegalStateException("Not implemented");
        }
        var r = A + $ + (c ? 1 : 0);
        A = (r & 0b1111_1111);
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
        c = (r & ~0b1111_1111) != 0;
        v = np != n;
    }

    private void sbc(int addr) {
        boolean np = (A & 0b1000_0000) != 0;
        int $ = memory[addr] & 0b1111_1111;
        if (d) {
            throw new IllegalStateException("Not implemented");
        }
        var r = A - $ + (c ? 1 : 0) - 1;
        A = (r & 0b1111_1111);
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
        c = (r & ~0b1111_1111) == 0;
        v = np != n;
    }

    private void cmp(int addr, int axy) {
        int $ = memory[addr] & 0b1111_1111;
        var r = axy - $;
        n = (r & 0b1000_0000) != 0;
        z = r == 0;
        c = (r & ~0b1111_1111) != 0;
    }

    private void dec(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        $ = $ - 1;
        var r = (byte) ($ & 0b1111_1111);
        memory[addr] = r;
        n = (r & 0b1000_0000) != 0;
        z = r == 0;
    }

    private void dex() {
        int $ = X - 1;
        X = $ & 0b1111_1111;
        n = (X & 0b1000_0000) != 0;
        z = X == 0;
    }

    private void dey() {
        int $ = Y - 1;
        X = $ & 0b1111_1111;
        n = (Y & 0b1000_0000) != 0;
        z = Y == 0;
    }

    private void inc(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        $ = $ + 1;
        var r = (byte) ($ & 0b1111_1111);
        memory[addr] = r;
        n = (r & 0b1000_0000) != 0;
        z = r == 0;
    }

    private void inx() {
        int $ = X + 1;
        Y = $ & 0b1111_1111;
        n = (X & 0b1000_0000) != 0;
        z = X == 0;
    }

    private void iny() {
        int $ = Y + 1;
        Y = $ & 0b1111_1111;
        n = (Y & 0b1000_0000) != 0;
        z = Y == 0;
    }

    private void asl_a() {
        c = (A & 0b1000_0000) != 0;
        var r = A * 2;
        A = r & 0b1111_1111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void asl(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        c = ($ & 0b1000_0000) != 0;
        var r = $ * 2;
        $ = r & 0b1111_1111;
        n = ($ & 0b1000_0000) != 0;
        z = $ == 0;
        memory[addr] = (byte) $;
    }

    private void rol_a() {
        var ct = (A & 0b1000_0000) != 0;
        var r = A * 2 + (c ? 1 : 0);
        A = r & 0b1111_1111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
        c = ct;
    }

    private void rol(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        var ct = ($ & 0b1000_0000) != 0;
        var r = $ * 2 + (c ? 1 : 0);
        $ = r & 0b1111_1111;
        n = ($ & 0b1000_0000) != 0;
        z = $ == 0;
        c = ct;
        memory[addr] = (byte) $;
    }

    private void lsr_a() {
        c = (A & 0b0000_0001) != 0;
        A = (A / 2) & 0b1111_11111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void lsr(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        c = ($ & 0b0000_0001) != 0;
        var r = $ / 2;
        $ = r & 0b1111_1111;
        n = ($ & 0b1000_0000) != 0;
        z = $ == 0;
        memory[addr] = (byte) $;
    }

    private void ror_a() {
        var ct = (A & 0b0000_0001) != 0;
        var r = A / 2 + (c ? 128 : 0);
        A = r & 0b1111_1111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
        c = ct;
    }

    private void ror(int addr) {
        int $ = memory[addr] & 0b1111_1111;
        var ct = ($ & 0b0000_0001) != 0;
        var r = $ / 2 + (c ? 128 : 0);
        $ = r & 0b1111_1111;
        n = ($ & 0b1000_0000) != 0;
        z = $ == 0;
        c = ct;
        memory[addr] = (byte) $;
    }

    private void tax() {
        X = A;
        n = (X & 0b1000_0000) != 0;
        z = X == 0;
    }

    private void txa() {
        A = X;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void tay() {
        Y = A;
        n = (Y & 0b1000_0000) != 0;
        z = Y == 0;
    }

    private void tya() {
        A = Y;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void tsx() {
        X = S;
        n = (X & 0b1000_0000) != 0;
        z = X == 0;
    }

    private void txs() {
        S = X;
    }

    private void pla() {
        S++;
        S = S & 0b1111_1111;
        A = memory[S + 0x100];
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void pha() {
        memory[S + 0x100] = (byte) A;
        S--;
        S = S & 0b1111_1111;
    }

    private void plp() {
        S++;
        S = S & 0b1111_1111;
        var sb = memory[S + 0x100];
        c = (sb & 0b0000_0001) != 0;
        z = (sb & 0b0000_0010) != 0;
        i = (sb & 0b0000_0100) != 0;
        d = (sb & 0b0000_1000) != 0;
        b = (sb & 0b0001_0000) != 0;
        v = (sb & 0b0100_0000) != 0;
        n = (sb & 0b1000_0000) != 0;
    }

    private void php() {
        int sb = (c ? 0b0000_0001 : 0)
                + (z ? 0b0000_0010 : 0)
                + (i ? 0b0000_0100 : 0)
                + (d ? 0b0000_1000 : 0)
                + (b ? 0b0001_0000 : 0)
                + 0b0010_0000
                + (v ? 0b0100_0000 : 0)
                + (n ? 0b1000_0000 : 0);
        memory[S + 0x100] = (byte) sb;
        S--;
        S = S & 0b1111_1111;
    }

    private void bpl(int addr) {
        if (!n) {
            P = addr;
        }
    }

    private void bmi(int addr) {
        if (n) {
            P = addr;
        }
    }

    private void bvc(int addr) {
        if (!v) {
            P = addr;
        }
    }

    private void bvs(int addr) {
        if (v) {
            P = addr;
        }
    }

    private void bcc(int addr) {
        if (!c) {
            P = addr;
        }
    }

    private void bcs(int addr) {
        if (c) {
            P = addr;
        }
    }

    private void bne(int addr) {
        if (!z) {
            P = addr;
        }
    }

    private void beq(int addr) {
        if (z) {
            P = addr;
        }
    }

    private void bit(int addr) {
        boolean np = (A & 0b1000_0000) != 0;
        int $ = memory[addr] & 0b1111_1111;
        int r = $ & A;
        n = (r & 0b1000_0000) != 0;
        z = r == 0;
        v = np != n;
    }

    private void clc() {
        c = false;
    }

    private void sec() {
        c = true;
    }

    private void cld() {
        d = false;
    }

    private void sed() {
        d = true;
    }

    private void cli() {
        i = false;
    }

    private void sei() {
        i = true;
    }

    private void clv() {
        v = false;
    }

    private void lda(int addr) {
        A = memory[addr] & 0b1111_1111;
        n = (A & 0b1000_0000) != 0;
        z = A == 0;
    }

    private void sta(int addr) {
        memory[addr] = (byte) A;
    }

    private void ldx(int addr) {
        X = memory[addr] & 0b1111_1111;
        n = (X & 0b1000_0000) != 0;
        z = X == 0;
    }

    private void stx(int addr) {
        memory[addr] = (byte) X;
    }

    private void ldy(int addr) {
        Y = memory[addr] & 0b1111_1111;
        n = (Y & 0b1000_0000) != 0;
        z = Y == 0;
    }

    private void sty(int addr) {
        memory[addr] = (byte) Y;
    }

    private void brk() {
        jsr(P++);
        php();
        P = 0xfffe;
    }

    private void rti() {
        plp();
        rts();
    }

    private void nop() {
        // no-op
    }

    private void jsr(int addr) {
        memory[S + 0x100] = (byte) (P >> 8);
        S--;
        S = S & 0b1111_1111;
        memory[S + 0x100] = (byte) (P & 0b1111_1111);
        S--;
        S = S & 0b1111_1111;
        P = addr;
    }

    private void rts() {
        S++;
        S = S & 0b1111_1111;
        byte pl = memory[S + 0x100];
        S++;
        S = S & 0b1111_1111;
        byte ph = memory[S + 0x100];
        P = ph * 256 + pl;
    }

    private void jmp(int addr) {
        P = addr;
    }

    private void init() {
        opcodes.put(Opcode.ORA_IMM, () -> ora(addrImmediate()));
        opcodes.put(Opcode.ORA_ZP, () -> ora(addrZeropage()));
        opcodes.put(Opcode.ORA_ZPI, () -> ora(addrIndexedZeropage(X)));
        opcodes.put(Opcode.ORA_ZPIX, () -> ora(addrIndexedIndirect()));
        opcodes.put(Opcode.ORA_ZPIY, () -> ora(addrIndirectIndexed()));
        opcodes.put(Opcode.ORA_ABS, () -> ora(addrAbsolute()));
        opcodes.put(Opcode.ORA_ABSX, () -> ora(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.ORA_ABSY, () -> ora(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.AND_IMM, () -> and(addrImmediate()));
        opcodes.put(Opcode.AND_ZP, () -> and(addrZeropage()));
        opcodes.put(Opcode.AND_ZPI, () -> and(addrIndexedZeropage(X)));
        opcodes.put(Opcode.AND_ZPIX, () -> and(addrIndexedIndirect()));
        opcodes.put(Opcode.AND_ZPIY, () -> and(addrIndirectIndexed()));
        opcodes.put(Opcode.AND_ABS, () -> and(addrAbsolute()));
        opcodes.put(Opcode.AND_ABSX, () -> and(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.AND_ABSY, () -> and(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.EOR_IMM, () -> eor(addrImmediate()));
        opcodes.put(Opcode.EOR_ZP, () -> eor(addrZeropage()));
        opcodes.put(Opcode.EOR_ZPI, () -> eor(addrIndexedZeropage(X)));
        opcodes.put(Opcode.EOR_ZPIX, () -> eor(addrIndexedIndirect()));
        opcodes.put(Opcode.EOR_ZPIY, () -> eor(addrIndirectIndexed()));
        opcodes.put(Opcode.EOR_ABS, () -> eor(addrAbsolute()));
        opcodes.put(Opcode.EOR_ABSX, () -> eor(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.EOR_ABSY, () -> eor(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.ADC_IMM, () -> adc(addrImmediate()));
        opcodes.put(Opcode.ADC_ZP, () -> adc(addrZeropage()));
        opcodes.put(Opcode.ADC_ZPI, () -> adc(addrIndexedZeropage(X)));
        opcodes.put(Opcode.ADC_ZPIX, () -> adc(addrIndexedIndirect()));
        opcodes.put(Opcode.ADC_ZPIY, () -> adc(addrIndirectIndexed()));
        opcodes.put(Opcode.ADC_ABS, () -> adc(addrAbsolute()));
        opcodes.put(Opcode.ADC_ABSX, () -> adc(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.ADC_ABSY, () -> adc(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.SBC_IMM, () -> sbc(addrImmediate()));
        opcodes.put(Opcode.SBC_ZP, () -> sbc(addrZeropage()));
        opcodes.put(Opcode.SBC_ZPI, () -> sbc(addrIndexedZeropage(X)));
        opcodes.put(Opcode.SBC_ZPIX, () -> sbc(addrIndexedIndirect()));
        opcodes.put(Opcode.SBC_ZPIY, () -> sbc(addrIndirectIndexed()));
        opcodes.put(Opcode.SBC_ABS, () -> sbc(addrAbsolute()));
        opcodes.put(Opcode.SBC_ABSX, () -> sbc(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.SBC_ABSY, () -> sbc(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.CMP_IMM, () -> cmp(addrImmediate(), A));
        opcodes.put(Opcode.CMP_ZP, () -> cmp(addrZeropage(), A));
        opcodes.put(Opcode.CMP_ZPI, () -> cmp(addrIndexedZeropage(X), A));
        opcodes.put(Opcode.CMP_ZPIX, () -> cmp(addrIndexedIndirect(), A));
        opcodes.put(Opcode.CMP_ZPIY, () -> cmp(addrIndirectIndexed(), A));
        opcodes.put(Opcode.CMP_ABS, () -> cmp(addrAbsolute(), A));
        opcodes.put(Opcode.CMP_ABSX, () -> cmp(addrAbsoluteIndexed(X), A));
        opcodes.put(Opcode.CMP_ABSY, () -> cmp(addrAbsoluteIndexed(Y), A));
        opcodes.put(Opcode.CPX_IMM, () -> cmp(addrImmediate(), X));
        opcodes.put(Opcode.CPX_ZP, () -> cmp(addrZeropage(), X));
        opcodes.put(Opcode.CPX_ABS, () -> cmp(addrAbsolute(), X));
        opcodes.put(Opcode.CPY_IMM, () -> cmp(addrImmediate(), Y));
        opcodes.put(Opcode.CPY_ZP, () -> cmp(addrZeropage(), Y));
        opcodes.put(Opcode.CPY_ABS, () -> cmp(addrAbsolute(), Y));
        opcodes.put(Opcode.DEC_ZP, () -> dec(addrZeropage()));
        opcodes.put(Opcode.DEC_ZPI, () -> dec(addrIndexedZeropage(X)));
        opcodes.put(Opcode.DEC_ABS, () -> dec(addrAbsolute()));
        opcodes.put(Opcode.DEC_ABSX, () -> dec(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.DEX, () -> dex());
        opcodes.put(Opcode.DEY, () -> dey());
        opcodes.put(Opcode.INC_ZP, () -> inc(addrZeropage()));
        opcodes.put(Opcode.INC_ZPI, () -> inc(addrIndexedZeropage(X)));
        opcodes.put(Opcode.INC_ABS, () -> inc(addrAbsolute()));
        opcodes.put(Opcode.INC_ABSX, () -> inc(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.INX, () -> inx());
        opcodes.put(Opcode.INY, () -> iny());
        opcodes.put(Opcode.ASL, () -> asl_a());
        opcodes.put(Opcode.ASL_ZP, () -> asl(addrZeropage()));
        opcodes.put(Opcode.ASL_ZPI, () -> asl(addrIndexedZeropage(X)));
        opcodes.put(Opcode.ASL_ABS, () -> asl(addrAbsolute()));
        opcodes.put(Opcode.ASL_ABSX, () -> asl(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.ROL, () -> rol_a());
        opcodes.put(Opcode.ROL_ZP, () -> rol(addrZeropage()));
        opcodes.put(Opcode.ROL_ZPI, () -> rol(addrIndexedZeropage(X)));
        opcodes.put(Opcode.ROL_ABS, () -> rol(addrAbsolute()));
        opcodes.put(Opcode.ROL_ABSX, () -> rol(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.LSR, () -> lsr_a());
        opcodes.put(Opcode.LSR_ZP, () -> lsr(addrZeropage()));
        opcodes.put(Opcode.LSR_ZPI, () -> lsr(addrIndexedZeropage(X)));
        opcodes.put(Opcode.LSR_ABS, () -> lsr(addrAbsolute()));
        opcodes.put(Opcode.LSR_ABSX, () -> lsr(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.ROR, () -> ror_a());
        opcodes.put(Opcode.ROR_ZP, () -> ror(addrZeropage()));
        opcodes.put(Opcode.ROR_ZPI, () -> ror(addrIndexedZeropage(X)));
        opcodes.put(Opcode.ROR_ABS, () -> ror(addrAbsolute()));
        opcodes.put(Opcode.ROR_ABSX, () -> ror(addrAbsoluteIndexed(X)));
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
        opcodes.put(Opcode.LDA_ZPI, () -> lda(addrIndexedZeropage(X)));
        opcodes.put(Opcode.LDA_ZPIX, () -> lda(addrIndexedIndirect()));
        opcodes.put(Opcode.LDA_ZPIY, () -> lda(addrIndirectIndexed()));
        opcodes.put(Opcode.LDA_ABS, () -> lda(addrAbsolute()));
        opcodes.put(Opcode.LDA_ABSX, () -> lda(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.LDA_ABSY, () -> lda(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.STA_ZP, () -> sta(addrZeropage()));
        opcodes.put(Opcode.STA_ZPI, () -> sta(addrIndexedZeropage(X)));
        opcodes.put(Opcode.STA_ZPIX, () -> sta(addrIndexedIndirect()));
        opcodes.put(Opcode.STA_ZPIY, () -> sta(addrIndirectIndexed()));
        opcodes.put(Opcode.STA_ABS, () -> sta(addrAbsolute()));
        opcodes.put(Opcode.STA_ABSX, () -> sta(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.STA_ABSY, () -> sta(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.LDX_IMM, () -> ldx(addrImmediate()));
        opcodes.put(Opcode.LDX_ZP, () -> ldx(addrZeropage()));
        opcodes.put(Opcode.LDX_ZPI, () -> ldx(addrIndexedZeropage(Y)));
        opcodes.put(Opcode.LDX_ABS, () -> ldx(addrAbsolute()));
        opcodes.put(Opcode.LDX_ABSY, () -> ldx(addrAbsoluteIndexed(Y)));
        opcodes.put(Opcode.STX_ZP, () -> stx(addrZeropage()));
        opcodes.put(Opcode.STX_ZPI, () -> stx(addrIndexedZeropage(Y)));
        opcodes.put(Opcode.STX_ABS, () -> stx(addrAbsolute()));
        opcodes.put(Opcode.LDY_IMM, () -> ldy(addrImmediate()));
        opcodes.put(Opcode.LDY_ZP, () -> ldy(addrZeropage()));
        opcodes.put(Opcode.LDY_ZPI, () -> ldy(addrIndexedZeropage(X)));
        opcodes.put(Opcode.LDY_ABS, () -> ldy(addrAbsolute()));
        opcodes.put(Opcode.LDY_ABSX, () -> ldy(addrAbsoluteIndexed(X)));
        opcodes.put(Opcode.STY_ZP, () -> sty(addrZeropage()));
        opcodes.put(Opcode.STY_ZPI, () -> sty(addrIndexedZeropage(X)));
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

    public Mock6510() {
        init();
    }

    public void load(byte[] program) {
        int d = program[0] + program[1] * 256;
        for (int s = 2; s < program.length; s++) {
            memory[d] = program[s];
            d++;
        }
    }

    private void print(Opcode opcode) {
        if (verbose) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(".%04X   ", P - 1));
            sb.append(String.format("%02X ", opcode.getOpcode()));
            int p1 = memory[P] & 0b1111_1111;
            int p2 = memory[P + 1] & 0b1111_1111;
            int w = p1 + p2 * 256;
            int r = (P + 1) + p1;
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
            System.out.println(toString());
        }
        checkRegisters(opcode);
    }

    private void checkRegisters(Opcode opcode) {
        if (A < 0 || A > 255 || X < 0 || X > 255 || Y < 0 || Y > 255 || S < 0 || S > 255) {
            throw new IllegalArgumentException("Invalid register value: " + opcode.name() + ", " + toString());
        }
    }

    public void run(int start, int end) {
        P = start;
        if (verbose) {
            System.out.println(toString());
        }
        while (P != end) {
            byte bc = memory[P++];
            Opcode opcode = getOpcode(bc);
            run(opcode);
        }
    }

    @Override
    public String toString() {
        return "A=" + String.format("%02X", A) +
                ", X=" + String.format("%02X", X) +
                ", Y=" + String.format("%02X", Y) +
                ", S=" + String.format("%02X", S) +
                ", P=" + String.format("%04X", P) +
                ", c=" + (c ? "1" : "0") +
                ", z=" + (z ? "1" : "0") +
                ", i=" + (i ? "1" : "0") +
                ", d=" + (d ? "1" : "0") +
                ", b=" + (b ? "1" : "0") +
                ", v=" + (v ? "1" : "0") +
                ", n=" + (n ? "1" : "0");
    }
}


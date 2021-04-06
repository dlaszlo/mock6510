package hu.dlaszlo.mos6510;

public enum Opcode {

    ORA_IMM(0x09, 2, 2, 2, "ora #$BYTE"),  // immediate
    ORA_ZP(0x05, 2, 3, 3, "ora $$BYTE"),   // zero page
    ORA_ZPI(0x15, 2, 4, 4, "ora $$BYTE,x"),  // indexed zero page
    ORA_ZPIX(0x01, 2, 6, 6, "ora ($$BYTE,x)"), // indirect indexed X
    ORA_ZPIY(0x11, 2, 5, 6, "ora ($$BYTE),y"), // indexed indirect Y
    ORA_ABS(0x0d, 3, 4, 4, "ora $$WORD"),  // absolute
    ORA_ABSX(0x1d, 3, 4, 5, "ora $$WORD,x"), // absolute indexed X
    ORA_ABSY(0x19, 3, 4, 5, "ora $$WORD,y"), // absolute indexed X
    AND_IMM(0x29, 2, 2, 2, "and #$BYTE"),
    AND_ZP(0x25, 2, 3, 3, "and $$BYTE"),
    AND_ZPI(0x35, 2, 4, 4, "and $$BYTE,x"),
    AND_ZPIX(0x21, 2, 6, 6, "and ($$BYTE,x)"),
    AND_ZPIY(0x31, 2, 5, 6, "and ($$BYTE),y"),
    AND_ABS(0x2d, 3, 4, 4, "and $$WORD"),
    AND_ABSX(0x3d, 3, 4, 5, "and $$WORD,x"),
    AND_ABSY(0x39, 3, 4, 5, "and $$WORD,y"),
    EOR_IMM(0x49, 2, 2, 2, "eor #$BYTE"),
    EOR_ZP(0x45, 2, 3, 3, "eor $$BYTE"),
    EOR_ZPI(0x55, 2, 4, 4, "eor $$BYTE,x"),
    EOR_ZPIX(0x41, 2, 6, 6, "eor ($$BYTE,x)"),
    EOR_ZPIY(0x51, 2, 5, 6, "eor ($$BYTE),y"),
    EOR_ABS(0x4d, 3, 4, 4, "eor $$WORD"),
    EOR_ABSX(0x5d, 3, 4, 5, "eor $$WORD,x"),
    EOR_ABSY(0x59, 3, 4, 5, "eor $$WORD,y"),
    ADC_IMM(0x69, 2, 2, 2, "adc #$BYTE"),
    ADC_ZP(0x65, 2, 3, 3, "adc $$BYTE"),
    ADC_ZPI(0x75, 2, 4, 4, "adc $$BYTE,x"),
    ADC_ZPIX(0x61, 2, 6, 6, "adc ($$BYTE,x)"),
    ADC_ZPIY(0x71, 2, 5, 6, "adc ($$BYTE),y"),
    ADC_ABS(0x6d, 3, 4, 4, "adc $$WORD"),
    ADC_ABSX(0x7d, 3, 4, 5, "adc $$WORD,x"),
    ADC_ABSY(0x79, 3, 4, 5, "adc $$WORD,y"),
    SBC_IMM(0xe9, 2, 2, 2, "sbc #$BYTE"),
    SBC_ZP(0xe5, 2, 3, 3, "sbc $$BYTE"),
    SBC_ZPI(0xf5, 2, 4, 4, "sbc $$BYTE,x"),
    SBC_ZPIX(0xe1, 2, 6, 6, "sbc ($$BYTE,x)"),
    SBC_ZPIY(0xf1, 2, 5, 6, "sbc ($$BYTE),y"),
    SBC_ABS(0xed, 3, 4, 4, "sbc $$WORD"),
    SBC_ABSX(0xfd, 3, 4, 5, "sbc $$WORD,x"),
    SBC_ABSY(0xf9, 3, 4, 5, "sbc $$WORD,y"),
    CMP_IMM(0xc9, 2, 2, 2, "cmp #$BYTE"),
    CMP_ZP(0xc5, 2, 3, 3, "cmp $$BYTE"),
    CMP_ZPI(0xd5, 2, 4, 4, "cmp $$BYTE,x"),
    CMP_ZPIX(0xc1, 2, 6, 6, "cmp ($$BYTE,x)"),
    CMP_ZPIY(0xd1, 2, 5, 6, "cmp ($$BYTE),y"),
    CMP_ABS(0xcd, 3, 4, 4, "cmp $$WORD"),
    CMP_ABSX(0xdd, 3, 4, 5, "cmp $$WORD,x"),
    CMP_ABSY(0xd9, 3, 4, 5, "cmp $$WORD,y"),
    CPX_IMM(0xe0, 2, 2, 2, "cpx #$BYTE"),
    CPX_ZP(0xe4, 2, 3, 3, "cpx $$BYTE"),
    CPX_ABS(0xec, 3, 4, 4, "cpx $$WORD"),
    CPY_IMM(0xc0, 2, 2, 2, "cpy #$BYTE"),
    CPY_ZP(0xc4, 2, 3, 3, "cpy $$BYTE"),
    CPY_ABS(0xcc, 3, 4, 4, "cpy $$WORD"),
    DEC_ZP(0xc6, 2, 5, 5, "dec $$BYTE"),
    DEC_ZPI(0xd6, 2, 6, 6, "dec $$BYTE,x"),
    DEC_ABS(0xce, 3, 6, 6, "dec $$WORD"),
    DEC_ABSX(0xde, 3, 7, 7, "dec $$WORD,x"),
    DEX(0xca, 1, 2, 2, "dex"),
    DEY(0x88, 1, 2, 2, "dey"),
    INC_ZP(0xe6, 2, 5, 5, "inc $$BYTE"),
    INC_ZPI(0xf6, 2, 6, 6, "inc $$BYTE,x"),
    INC_ABS(0xee, 3, 6, 6, "inc $$WORD"),
    INC_ABSX(0xfe, 3, 7, 7, "inc $$WORD,x"),
    INX(0xe8, 1, 2, 2, "inx"),
    INY(0xc8, 1, 2, 2, "iny"),
    ASL(0x0a, 1, 2, 2, "asl"),
    ASL_ZP(0x06, 2, 5, 5, "asl $$BYTE"),
    ASL_ZPI(0x16, 2, 6, 6, "asl $$BYTE,x"),
    ASL_ABS(0x0e, 3, 6, 6, "asl $$WORD"),
    ASL_ABSX(0x1e, 3, 7, 7, "asl $$WORD,x"),
    ROL(0x2a, 1, 2, 2, "rol"),
    ROL_ZP(0x26, 2, 5, 5, "rol $$BYTE"),
    ROL_ZPI(0x36, 2, 6, 6, "rol $$BYTE,x"),
    ROL_ABS(0x2e, 3, 6, 6, "rol $$WORD"),
    ROL_ABSX(0x3e, 3, 7, 7, "rol $$WORD,x"),
    LSR(0x4a, 1, 2, 2, "lsr"),
    LSR_ZP(0x46, 2, 5, 5, "lsr $$BYTE"),
    LSR_ZPI(0x56, 2, 6, 6, "lsr $$BYTE,x"),
    LSR_ABS(0x4e, 3, 6, 6, "lsr $$WORD"),
    LSR_ABSX(0x5e, 3, 7, 7, "lsr $$WORD,x"),
    ROR(0x6a, 1, 2, 2, "ror "),
    ROR_ZP(0x66, 2, 5, 5, "ror $$BYTE"),
    ROR_ZPI(0x76, 2, 6, 6, "ror $$BYTE,x"),
    ROR_ABS(0x6e, 3, 6, 6, "ror $$WORD"),
    ROR_ABSX(0x7e, 3, 7, 7, "ror $$WORD,x"),
    TAX(0xaa, 1, 2, 2, "tax"),
    TXA(0x8a, 1, 2, 2, "txa"),
    TAY(0xa8, 1, 2, 2, "tay"),
    TYA(0x98, 1, 2, 2, "tya"),
    TSX(0xba, 1, 2, 2, "tsx"),
    TXS(0x9a, 1, 2, 2, "txs"),
    PLA(0x68, 1, 2, 2, "pla"),
    PHA(0x48, 1, 2, 2, "pha"),
    PLP(0x28, 1, 2, 2, "plp"),
    PHP(0x08, 1, 2, 2, "php"),
    BPL(0x10, 1, 2, 3, "bpl $$REL"),
    BMI(0x30, 1, 2, 3, "bmi $$REL"),
    BVC(0x50, 1, 2, 3, "bvc $$REL"),
    BVS(0x70, 1, 2, 3, "bvs $$REL"),
    BCC(0x90, 1, 2, 3, "bcc $$REL"),
    BCS(0xb0, 1, 2, 3, "bcs $$REL"),
    BNE(0xd0, 1, 2, 3, "bne $$REL"),
    BEQ(0xf0, 1, 2, 3, "beq $$REL"),
    BIT_ZP(0x24, 2, 3, 3, "bit $$BYTE"),
    BIT_ABS(0x2c, 3, 4, 4, "bit $$WORD"),
    CLC(0x18, 1, 2, 2, "clc"),
    SEC(0x38, 1, 2, 2, "sec"),
    CLD(0xd8, 1, 2, 2, "cld"),
    SED(0xf8, 1, 2, 2, "sed"),
    CLI(0x58, 1, 2, 2, "cli"),
    SEI(0x78, 1, 2, 2, "sei"),
    CLV(0xb8, 1, 2, 2, "clv"),
    LDA_IMM(0xa9, 2, 2, 2, "lda #$BYTE"),
    LDA_ZP(0xa5, 2, 3, 3, "lda $$BYTE"),
    LDA_ZPI(0xb5, 2, 4, 4, "lda $$BYTE,x"),
    LDA_ZPIX(0xa1, 2, 6, 6, "lda ($$BYTE,x)"),
    LDA_ZPIY(0xb1, 2, 5, 6, "lda ($$BYTE),y"),
    LDA_ABS(0xad, 3, 4, 4, "lda $$WORD"),
    LDA_ABSX(0xbd, 3, 4, 5, "lda $$WORD,x"),
    LDA_ABSY(0xb9, 3, 4, 5, "lda $$WORD,y"),
    STA_ZP(0x85, 2, 3, 3, "sta $$BYTE"),
    STA_ZPI(0x95, 2, 4, 4, "sta $$BYTE,x"),
    STA_ZPIX(0x81, 2, 6, 6, "sta ($$BYTE,x)"),
    STA_ZPIY(0x91, 2, 6, 6, "sta ($$BYTE),y"),
    STA_ABS(0x8d, 3, 4, 4, "sta $$WORD"),
    STA_ABSX(0x9d, 3, 5, 5, "sta $$WORD,x"),
    STA_ABSY(0x99, 3, 5, 5, "sta $$WORD,y"),
    LDX_IMM(0xa2, 2, 2, 2, "ldx #$BYTE"),
    LDX_ZP(0xa6, 2, 3, 3, "ldx $$BYTE"),
    LDX_ZPI(0xb6, 2, 4, 4, "ldx $$BYTE,y"),
    LDX_ABS(0xae, 3, 4, 4, "ldx $$WORD"),
    LDX_ABSY(0xbe, 3, 4, 5, "ldx $$WORD,y"),
    STX_ZP(0x86, 2, 3, 3, "stx $$BYTE"),
    STX_ZPI(0x96, 2, 4, 4, "stx $$BYTE,y"),
    STX_ABS(0x8e, 3, 4, 4, "stx $$WORD"),
    LDY_IMM(0xa0, 2, 2, 2, "ldy #$BYTE"),
    LDY_ZP(0xa4, 2, 3, 3, "ldy $$BYTE"),
    LDY_ZPI(0xb4, 2, 4, 4, "ldy $$BYTE,x"),
    LDY_ABS(0xac, 3, 4, 4, "ldy $$WORD"),
    LDY_ABSX(0xbc, 3, 4, 5, "ldy $$WORD,x"),
    STY_ZP(0x84, 2, 3, 3, "sty $$BYTE"),
    STY_ZPI(0x94, 2, 4, 4, "sty $$BYTE,x"),
    STY_ABS(0x8c, 3, 4, 4, "sty $$WORD"),
    BRK(0x00, 1, 7, 7, "brk"),
    RTI(0x40, 1, 6, 6, "rti"),
    JSR(0x20, 3, 6, 6, "jsr $$WORD"),
    RTS(0x60, 1, 6, 6, "rts"),
    JMP(0x4c, 3, 3, 3, "jmp $$WORD"),
    JMP_ABSI(0x6c, 3, 5, 5, "jmp ($$WORD)"), // absolute indirect
    NOP(0xea, 1, 2, 2, "nop");

    public byte opcode;
    public byte length;
    public byte cyclesMin;
    public byte cyclesMax;
    public String template;

    Opcode(int opcode, int length, int cyclesMin, int cyclesMax, String template) {
        this.opcode = (byte) opcode;
        this.length = (byte) length;
        this.cyclesMin = (byte) cyclesMin;
        this.cyclesMax = (byte) cyclesMax;
        this.template = template;
    }

    public byte getOpcode() {
        return opcode;
    }

    public byte getLength() {
        return length;
    }

    public byte getCyclesMin() {
        return cyclesMin;
    }

    public byte getCyclesMax() {
        return cyclesMax;
    }

    public String getTemplate() {
        return template;
    }

    public static Opcode getOpcode(byte opcode) {
        Opcode ret = null;
        for (Opcode o : values()) {
            if (o.getOpcode() == opcode) {
                ret = o;
                break;
            }
        }
        return ret;
    }

}

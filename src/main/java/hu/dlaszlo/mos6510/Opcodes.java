package hu.dlaszlo.mos6510;

import java.util.*;

import static hu.dlaszlo.mos6510.AddressingMode.*;
import static hu.dlaszlo.mos6510.Instructions.*;

public class Opcodes {

    public final static Map<Integer, Opcode> OPCODE_MAP;

    static {
        List<Opcode> opcodeList = new ArrayList<>();
        opcodeList.add(new Opcode(0x09, ORA, IMM, 2, 2, 2, "ora #$$BYTE"));
        opcodeList.add(new Opcode(0x05, ORA, ZP, 2, 3, 3, "ora $$BYTE"));
        opcodeList.add(new Opcode(0x15, ORA, ZPX, 2, 4, 4, "ora $$BYTE,x"));
        opcodeList.add(new Opcode(0x01, ORA, IZX, 2, 6, 6, "ora ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x11, ORA, IZY, 2, 5, 6, "ora ($$BYTE),y"));
        opcodeList.add(new Opcode(0x0d, ORA, ABS, 3, 4, 4, "ora $$WORD"));
        opcodeList.add(new Opcode(0x1d, ORA, ABX, 3, 4, 5, "ora $$WORD,x"));
        opcodeList.add(new Opcode(0x19, ORA, ABY, 3, 4, 5, "ora $$WORD,y"));
        opcodeList.add(new Opcode(0x29, AND, IMM, 2, 2, 2, "and #$$BYTE"));
        opcodeList.add(new Opcode(0x25, AND, ZP, 2, 3, 3, "and $$BYTE"));
        opcodeList.add(new Opcode(0x35, AND, ZPX, 2, 4, 4, "and $$BYTE,x"));
        opcodeList.add(new Opcode(0x21, AND, IZX, 2, 6, 6, "and ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x31, AND, IZY, 2, 5, 6, "and ($$BYTE),y"));
        opcodeList.add(new Opcode(0x2d, AND, ABS, 3, 4, 4, "and $$WORD"));
        opcodeList.add(new Opcode(0x3d, AND, ABX, 3, 4, 5, "and $$WORD,x"));
        opcodeList.add(new Opcode(0x39, AND, ABY, 3, 4, 5, "and $$WORD,y"));
        opcodeList.add(new Opcode(0x49, EOR, IMM, 2, 2, 2, "eor #$$BYTE"));
        opcodeList.add(new Opcode(0x45, EOR, ZP, 2, 3, 3, "eor $$BYTE"));
        opcodeList.add(new Opcode(0x55, EOR, ZPX, 2, 4, 4, "eor $$BYTE,x"));
        opcodeList.add(new Opcode(0x41, EOR, IZX, 2, 6, 6, "eor ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x51, EOR, IZY, 2, 5, 6, "eor ($$BYTE),y"));
        opcodeList.add(new Opcode(0x4d, EOR, ABS, 3, 4, 4, "eor $$WORD"));
        opcodeList.add(new Opcode(0x5d, EOR, ABX, 3, 4, 5, "eor $$WORD,x"));
        opcodeList.add(new Opcode(0x59, EOR, ABY, 3, 4, 5, "eor $$WORD,y"));
        opcodeList.add(new Opcode(0x69, ADC, IMM, 2, 2, 2, "adc #$$BYTE"));
        opcodeList.add(new Opcode(0x65, ADC, ZP, 2, 3, 3, "adc $$BYTE"));
        opcodeList.add(new Opcode(0x75, ADC, ZPX, 2, 4, 4, "adc $$BYTE,x"));
        opcodeList.add(new Opcode(0x61, ADC, IZX, 2, 6, 6, "adc ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x71, ADC, IZY, 2, 5, 6, "adc ($$BYTE),y"));
        opcodeList.add(new Opcode(0x6d, ADC, ABS, 3, 4, 4, "adc $$WORD"));
        opcodeList.add(new Opcode(0x7d, ADC, ABX, 3, 4, 5, "adc $$WORD,x"));
        opcodeList.add(new Opcode(0x79, ADC, ABY, 3, 4, 5, "adc $$WORD,y"));
        opcodeList.add(new Opcode(0xe9, SBC, IMM, 2, 2, 2, "sbc #$$BYTE"));
        opcodeList.add(new Opcode(0xe5, SBC, ZP, 2, 3, 3, "sbc $$BYTE"));
        opcodeList.add(new Opcode(0xf5, SBC, ZPX, 2, 4, 4, "sbc $$BYTE,x"));
        opcodeList.add(new Opcode(0xe1, SBC, IZX, 2, 6, 6, "sbc ($$BYTE,x)"));
        opcodeList.add(new Opcode(0xf1, SBC, IZY, 2, 5, 6, "sbc ($$BYTE),y"));
        opcodeList.add(new Opcode(0xed, SBC, ABS, 3, 4, 4, "sbc $$WORD"));
        opcodeList.add(new Opcode(0xfd, SBC, ABX, 3, 4, 5, "sbc $$WORD,x"));
        opcodeList.add(new Opcode(0xf9, SBC, ABY, 3, 4, 5, "sbc $$WORD,y"));
        opcodeList.add(new Opcode(0xc9, CMP, IMM, 2, 2, 2, "cmp #$$BYTE"));
        opcodeList.add(new Opcode(0xc5, CMP, ZP, 2, 3, 3, "cmp $$BYTE"));
        opcodeList.add(new Opcode(0xd5, CMP, ZPX, 2, 4, 4, "cmp $$BYTE,x"));
        opcodeList.add(new Opcode(0xc1, CMP, IZX, 2, 6, 6, "cmp ($$BYTE,x)"));
        opcodeList.add(new Opcode(0xd1, CMP, IZY, 2, 5, 6, "cmp ($$BYTE),y"));
        opcodeList.add(new Opcode(0xcd, CMP, ABS, 3, 4, 4, "cmp $$WORD"));
        opcodeList.add(new Opcode(0xdd, CMP, ABX, 3, 4, 5, "cmp $$WORD,x"));
        opcodeList.add(new Opcode(0xd9, CMP, ABY, 3, 4, 5, "cmp $$WORD,y"));
        opcodeList.add(new Opcode(0xe0, CPX, IMM, 2, 2, 2, "cpx #$$BYTE"));
        opcodeList.add(new Opcode(0xe4, CPX, ZP, 2, 3, 3, "cpx $$BYTE"));
        opcodeList.add(new Opcode(0xec, CPX, ABS, 3, 4, 4, "cpx $$WORD"));
        opcodeList.add(new Opcode(0xc0, CPY, IMM, 2, 2, 2, "cpy #$$BYTE"));
        opcodeList.add(new Opcode(0xc4, CPY, ZP, 2, 3, 3, "cpy $$BYTE"));
        opcodeList.add(new Opcode(0xcc, CPY, ABS, 3, 4, 4, "cpy $$WORD"));
        opcodeList.add(new Opcode(0xc6, DEC, ZP, 2, 5, 5, "dec $$BYTE"));
        opcodeList.add(new Opcode(0xd6, DEC, ZPX, 2, 6, 6, "dec $$BYTE,x"));
        opcodeList.add(new Opcode(0xce, DEC, ABS, 3, 6, 6, "dec $$WORD"));
        opcodeList.add(new Opcode(0xde, DEC, ABX, 3, 7, 7, "dec $$WORD,x"));
        opcodeList.add(new Opcode(0xca, DEX, IMP, 1, 2, 2, "dex"));
        opcodeList.add(new Opcode(0x88, DEY, IMP, 1, 2, 2, "dey"));
        opcodeList.add(new Opcode(0xe6, INC, ZP, 2, 5, 5, "inc $$BYTE"));
        opcodeList.add(new Opcode(0xf6, INC, ZPX, 2, 6, 6, "inc $$BYTE,x"));
        opcodeList.add(new Opcode(0xee, INC, ABS, 3, 6, 6, "inc $$WORD"));
        opcodeList.add(new Opcode(0xfe, INC, ABX, 3, 7, 7, "inc $$WORD,x"));
        opcodeList.add(new Opcode(0xe8, INX, IMP, 1, 2, 2, "inx"));
        opcodeList.add(new Opcode(0xc8, INY, IMP, 1, 2, 2, "iny"));
        opcodeList.add(new Opcode(0x0a, ASL_A, IMP, 1, 2, 2, "asl"));
        opcodeList.add(new Opcode(0x06, ASL, ZP, 2, 5, 5, "asl $$BYTE"));
        opcodeList.add(new Opcode(0x16, ASL, ZPX, 2, 6, 6, "asl $$BYTE,x"));
        opcodeList.add(new Opcode(0x0e, ASL, ABS, 3, 6, 6, "asl $$WORD"));
        opcodeList.add(new Opcode(0x1e, ASL, ABX, 3, 7, 7, "asl $$WORD,x"));
        opcodeList.add(new Opcode(0x2a, ROL_A, IMP, 1, 2, 2, "rol"));
        opcodeList.add(new Opcode(0x26, ROL, ZP, 2, 5, 5, "rol $$BYTE"));
        opcodeList.add(new Opcode(0x36, ROL, ZPX, 2, 6, 6, "rol $$BYTE,x"));
        opcodeList.add(new Opcode(0x2e, ROL, ABS, 3, 6, 6, "rol $$WORD"));
        opcodeList.add(new Opcode(0x3e, ROL, ABX, 3, 7, 7, "rol $$WORD,x"));
        opcodeList.add(new Opcode(0x4a, LSR_A, IMP, 1, 2, 2, "lsr"));
        opcodeList.add(new Opcode(0x46, LSR, ZP, 2, 5, 5, "lsr $$BYTE"));
        opcodeList.add(new Opcode(0x56, LSR, ZPX, 2, 6, 6, "lsr $$BYTE,x"));
        opcodeList.add(new Opcode(0x4e, LSR, ABS, 3, 6, 6, "lsr $$WORD"));
        opcodeList.add(new Opcode(0x5e, LSR, ABX, 3, 7, 7, "lsr $$WORD,x"));
        opcodeList.add(new Opcode(0x6a, ROR_A, IMP, 1, 2, 2, "ror"));
        opcodeList.add(new Opcode(0x66, ROR, ZP, 2, 5, 5, "ror $$BYTE"));
        opcodeList.add(new Opcode(0x76, ROR, ZPX, 2, 6, 6, "ror $$BYTE,x"));
        opcodeList.add(new Opcode(0x6e, ROR, ABS, 3, 6, 6, "ror $$WORD"));
        opcodeList.add(new Opcode(0x7e, ROR, ABX, 3, 7, 7, "ror $$WORD,x"));
        opcodeList.add(new Opcode(0xaa, TAX, IMP, 1, 2, 2, "tax"));
        opcodeList.add(new Opcode(0x8a, TXA, IMP, 1, 2, 2, "txa"));
        opcodeList.add(new Opcode(0xa8, TAY, IMP, 1, 2, 2, "tay"));
        opcodeList.add(new Opcode(0x98, TYA, IMP, 1, 2, 2, "tya"));
        opcodeList.add(new Opcode(0xba, TSX, IMP, 1, 2, 2, "tsx"));
        opcodeList.add(new Opcode(0x9a, TXS, IMP, 1, 2, 2, "txs"));
        opcodeList.add(new Opcode(0x68, PLA, IMP, 1, 4, 4, "pla"));
        opcodeList.add(new Opcode(0x48, PHA, IMP, 1, 3, 3, "pha"));
        opcodeList.add(new Opcode(0x28, PLP, IMP, 1, 4, 4, "plp"));
        opcodeList.add(new Opcode(0x08, PHP, IMP, 1, 3, 3, "php"));
        opcodeList.add(new Opcode(0x10, BPL, REL, 2, 2, 4, "bpl $$REL"));
        opcodeList.add(new Opcode(0x30, BMI, REL, 2, 2, 4, "bmi $$REL"));
        opcodeList.add(new Opcode(0x50, BVC, REL, 2, 2, 4, "bvc $$REL"));
        opcodeList.add(new Opcode(0x70, BVS, REL, 2, 2, 4, "bvs $$REL"));
        opcodeList.add(new Opcode(0x90, BCC, REL, 2, 2, 4, "bcc $$REL"));
        opcodeList.add(new Opcode(0xb0, BCS, REL, 2, 2, 4, "bcs $$REL"));
        opcodeList.add(new Opcode(0xd0, BNE, REL, 2, 2, 4, "bne $$REL"));
        opcodeList.add(new Opcode(0xf0, BEQ, REL, 2, 2, 4, "beq $$REL"));
        opcodeList.add(new Opcode(0x24, BIT, ZP, 2, 3, 3, "bit $$BYTE"));
        opcodeList.add(new Opcode(0x2c, BIT, ABS, 3, 4, 4, "bit $$WORD"));
        opcodeList.add(new Opcode(0x18, CLC, IMP, 1, 2, 2, "clc"));
        opcodeList.add(new Opcode(0x38, SEC, IMP, 1, 2, 2, "sec"));
        opcodeList.add(new Opcode(0xd8, CLD, IMP, 1, 2, 2, "cld"));
        opcodeList.add(new Opcode(0xf8, SED, IMP, 1, 2, 2, "sed"));
        opcodeList.add(new Opcode(0x58, CLI, IMP, 1, 2, 2, "cli"));
        opcodeList.add(new Opcode(0x78, SEI, IMP, 1, 2, 2, "sei"));
        opcodeList.add(new Opcode(0xb8, CLV, IMP, 1, 2, 2, "clv"));
        opcodeList.add(new Opcode(0xa9, LDA, IMM, 2, 2, 2, "lda #$$BYTE"));
        opcodeList.add(new Opcode(0xa5, LDA, ZP, 2, 3, 3, "lda $$BYTE"));
        opcodeList.add(new Opcode(0xb5, LDA, ZPX, 2, 4, 4, "lda $$BYTE,x"));
        opcodeList.add(new Opcode(0xa1, LDA, IZX, 2, 6, 6, "lda ($$BYTE,x)"));
        opcodeList.add(new Opcode(0xb1, LDA, IZY, 2, 5, 6, "lda ($$BYTE),y"));
        opcodeList.add(new Opcode(0xad, LDA, ABS, 3, 4, 4, "lda $$WORD"));
        opcodeList.add(new Opcode(0xbd, LDA, ABX, 3, 4, 5, "lda $$WORD,x"));
        opcodeList.add(new Opcode(0xb9, LDA, ABY, 3, 4, 5, "lda $$WORD,y"));
        opcodeList.add(new Opcode(0x85, STA, ZP, 2, 3, 3, "sta $$BYTE"));
        opcodeList.add(new Opcode(0x95, STA, ZPX, 2, 4, 4, "sta $$BYTE,x"));
        opcodeList.add(new Opcode(0x81, STA, IZX, 2, 6, 6, "sta ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x91, STA, IZY, 2, 6, 6, "sta ($$BYTE),y"));
        opcodeList.add(new Opcode(0x8d, STA, ABS, 3, 4, 4, "sta $$WORD"));
        opcodeList.add(new Opcode(0x9d, STA, ABX, 3, 5, 5, "sta $$WORD,x"));
        opcodeList.add(new Opcode(0x99, STA, ABY, 3, 5, 5, "sta $$WORD,y"));
        opcodeList.add(new Opcode(0xa2, LDX, IMM, 2, 2, 2, "ldx #$$BYTE"));
        opcodeList.add(new Opcode(0xa6, LDX, ZP, 2, 3, 3, "ldx $$BYTE"));
        opcodeList.add(new Opcode(0xb6, LDX, ZPY, 2, 4, 4, "ldx $$BYTE,y"));
        opcodeList.add(new Opcode(0xae, LDX, ABS, 3, 4, 4, "ldx $$WORD"));
        opcodeList.add(new Opcode(0xbe, LDX, ABY, 3, 4, 5, "ldx $$WORD,y"));
        opcodeList.add(new Opcode(0x86, STX, ZP, 2, 3, 3, "stx $$BYTE"));
        opcodeList.add(new Opcode(0x96, STX, ZPY, 2, 4, 4, "stx $$BYTE,y"));
        opcodeList.add(new Opcode(0x8e, STX, ABS, 3, 4, 4, "stx $$WORD"));
        opcodeList.add(new Opcode(0xa0, LDY, IMM, 2, 2, 2, "ldy #$$BYTE"));
        opcodeList.add(new Opcode(0xa4, LDY, ZP, 2, 3, 3, "ldy $$BYTE"));
        opcodeList.add(new Opcode(0xb4, LDY, ZPX, 2, 4, 4, "ldy $$BYTE,x"));
        opcodeList.add(new Opcode(0xac, LDY, ABS, 3, 4, 4, "ldy $$WORD"));
        opcodeList.add(new Opcode(0xbc, LDY, ABX, 3, 4, 5, "ldy $$WORD,x"));
        opcodeList.add(new Opcode(0x84, STY, ZP, 2, 3, 3, "sty $$BYTE"));
        opcodeList.add(new Opcode(0x94, STY, ZPX, 2, 4, 4, "sty $$BYTE,x"));
        opcodeList.add(new Opcode(0x8c, STY, ABS, 3, 4, 4, "sty $$WORD"));
        opcodeList.add(new Opcode(0x00, BRK, IMP, 1, 7, 7, "brk"));
        opcodeList.add(new Opcode(0x40, RTI, IMP, 1, 6, 6, "rti"));
        opcodeList.add(new Opcode(0x20, JSR, ABS, 3, 6, 6, "jsr $$WORD"));
        opcodeList.add(new Opcode(0x60, RTS, IMP, 1, 6, 6, "rts"));
        opcodeList.add(new Opcode(0x4c, JMP, ABS, 3, 3, 3, "jmp $$WORD"));
        opcodeList.add(new Opcode(0x6c, JMP, IND, 3, 5, 5, "jmp ($$WORD)"));
        opcodeList.add(new Opcode(0xea, NOP, IMP, 1, 2, 2, "nop"));

        // undocumented opcodes
        opcodeList.add(new Opcode(0x07, SLO, ZP, 2, 5, 5, "slo $$BYTE"));
        opcodeList.add(new Opcode(0x17, SLO, ZPX, 2, 6, 6, "slo $$BYTE,x"));
        opcodeList.add(new Opcode(0x03, SLO, IZX, 2, 8, 8, "slo ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x13, SLO, IZY, 2, 8, 8, "slo ($$BYTE),y"));
        opcodeList.add(new Opcode(0x0f, SLO, ABS, 3, 6, 6, "slo $$WORD"));
        opcodeList.add(new Opcode(0x1f, SLO, ABX, 3, 7, 7, "slo $$WORD,x"));
        opcodeList.add(new Opcode(0x1b, SLO, ABY, 3, 7, 7, "slo $$WORD,y"));
        opcodeList.add(new Opcode(0x27, RLA, ZP, 2, 5, 5, "rla $$BYTE"));
        opcodeList.add(new Opcode(0x37, RLA, ZPX, 2, 6, 6, "rla $$BYTE,x"));
        opcodeList.add(new Opcode(0x23, RLA, IZX, 2, 8, 8, "rla ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x33, RLA, IZY, 2, 8, 8, "rla ($$BYTE),y"));
        opcodeList.add(new Opcode(0x2f, RLA, ABS, 3, 6, 6, "rla $$WORD"));
        opcodeList.add(new Opcode(0x3f, RLA, ABX, 3, 7, 7, "rla $$WORD,x"));
        opcodeList.add(new Opcode(0x3b, RLA, ABY, 3, 7, 7, "rla $$WORD,y"));
        opcodeList.add(new Opcode(0x47, SRE, ZP, 2, 5, 5, "sre $$BYTE"));
        opcodeList.add(new Opcode(0x57, SRE, ZPX, 2, 6, 6, "sre $$BYTE,x"));
        opcodeList.add(new Opcode(0x43, SRE, IZX, 2, 8, 8, "sre ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x53, SRE, IZY, 2, 8, 8, "sre ($$BYTE),y"));
        opcodeList.add(new Opcode(0x4f, SRE, ABS, 3, 6, 6, "sre $$WORD"));
        opcodeList.add(new Opcode(0x5f, SRE, ABX, 3, 7, 7, "sre $$WORD,x"));
        opcodeList.add(new Opcode(0x5b, SRE, ABY, 3, 7, 7, "sre $$WORD,y"));
        opcodeList.add(new Opcode(0x67, RRA, ZP, 2, 5, 5, "rra $$BYTE"));
        opcodeList.add(new Opcode(0x77, RRA, ZPX, 2, 6, 6, "rra $$BYTE,x"));
        opcodeList.add(new Opcode(0x63, RRA, IZX, 2, 8, 8, "rra ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x73, RRA, IZY, 2, 8, 8, "rra ($$BYTE),y"));
        opcodeList.add(new Opcode(0x6f, RRA, ABS, 3, 6, 6, "rra $$WORD"));
        opcodeList.add(new Opcode(0x7f, RRA, ABX, 3, 7, 7, "rra $$WORD,x"));
        opcodeList.add(new Opcode(0x7b, RRA, ABY, 3, 7, 7, "rra $$WORD,y"));
        opcodeList.add(new Opcode(0xc7, DCP, ZP, 2, 5, 5, "dcp $$BYTE"));
        opcodeList.add(new Opcode(0xd7, DCP, ZPX, 2, 6, 6, "dcp $$BYTE,x"));
        opcodeList.add(new Opcode(0xc3, DCP, IZX, 2, 8, 8, "dcp ($$BYTE,x)"));
        opcodeList.add(new Opcode(0xd3, DCP, IZY, 2, 8, 8, "dcp ($$BYTE),y"));
        opcodeList.add(new Opcode(0xcf, DCP, ABS, 3, 6, 6, "dcp $$WORD"));
        opcodeList.add(new Opcode(0xdf, DCP, ABX, 3, 7, 7, "dcp $$WORD,x"));
        opcodeList.add(new Opcode(0xdb, DCP, ABY, 3, 7, 7, "dcp $$WORD,y"));
        opcodeList.add(new Opcode(0xe7, ISC, ZP, 2, 5, 5, "isc $$BYTE"));
        opcodeList.add(new Opcode(0xf7, ISC, ZPX, 2, 6, 6, "isc $$BYTE,x"));
        opcodeList.add(new Opcode(0xe3, ISC, IZX, 2, 8, 8, "isc ($$BYTE,x)"));
        opcodeList.add(new Opcode(0xf3, ISC, IZY, 2, 8, 8, "isc ($$BYTE),y"));
        opcodeList.add(new Opcode(0xef, ISC, ABS, 3, 6, 6, "isc $$WORD"));
        opcodeList.add(new Opcode(0xff, ISC, ABX, 3, 7, 7, "isc $$WORD,x"));
        opcodeList.add(new Opcode(0xfb, ISC, ABY, 3, 7, 7, "isc $$WORD,y"));
        opcodeList.add(new Opcode(0x87, SAX, ZP, 2, 3, 3, "sax $$BYTE"));
        opcodeList.add(new Opcode(0x97, SAX, ZPY, 2, 4, 4, "sax $$BYTE,y"));
        opcodeList.add(new Opcode(0x83, SAX, IZX, 2, 6, 6, "sax ($$BYTE,x)"));
        opcodeList.add(new Opcode(0x8f, SAX, ABS, 3, 4, 4, "sax $$WORD"));
        opcodeList.add(new Opcode(0xa7, LAX, ZP, 2, 3, 3, "lax $$BYTE"));
        opcodeList.add(new Opcode(0xb7, LAX, ZPY, 2, 4, 4, "lax $$BYTE,y"));
        opcodeList.add(new Opcode(0xa3, LAX, IZX, 2, 6, 6, "lax ($$BYTE,x)"));
        opcodeList.add(new Opcode(0xb3, LAX, IZY, 2, 5, 6, "lax ($$BYTE),y"));
        opcodeList.add(new Opcode(0xaf, LAX, ABS, 3, 4, 4, "lax $$WORD"));
        opcodeList.add(new Opcode(0xbf, LAX, ABY, 3, 4, 5, "lax $$WORD,y"));
        opcodeList.add(new Opcode(0xbb, LAS, ABY, 3, 4, 5, "las $$WORD,y"));
        opcodeList.add(new Opcode(0x0b, ANC, IMM, 2, 2, 2, "anc #$$BYTE"));
        opcodeList.add(new Opcode(0x2b, ANC, IMM, 2, 2, 2, "anc #$$BYTE"));
        opcodeList.add(new Opcode(0x4b, ALR, IMM, 2, 2, 2, "alr #$$BYTE"));
        opcodeList.add(new Opcode(0x6b, ARR, IMM, 2, 2, 2, "arr #$$BYTE"));
        opcodeList.add(new Opcode(0xcb, SBX, IMM, 2, 2, 2, "sbx #$$BYTE"));
        opcodeList.add(new Opcode(0xeb, SBC, IMM, 2, 2, 2, "usbc #$$BYTE"));
        opcodeList.add(new Opcode(0x8b, ANE, IMM, 2, 2, 2, "ane #$$BYTE"));
        opcodeList.add(new Opcode(0xab, LXA, IMM, 2, 2, 2, "lxa #$$BYTE"));
        opcodeList.add(new Opcode(0x93, SHA, IZY, 2, 6, 6, "sha ($$BYTE),y"));
        opcodeList.add(new Opcode(0x9f, SHA, ABY, 3, 5, 5, "sha $$WORD,y"));
        opcodeList.add(new Opcode(0x9e, SHX, ABY, 3, 5, 5, "shx $$WORD,y"));
        opcodeList.add(new Opcode(0x9c, SHY, ABX, 3, 5, 5, "shy $$WORD,x"));
        opcodeList.add(new Opcode(0x9b, TAS, ABY, 3, 5, 5, "tas $$WORD,y"));
        for (int op : new int[]{0x1a, 0x3a, 0x5a, 0x7a, 0xda, 0xfa}) {
            opcodeList.add(new Opcode(op, NOP, IMP, 1, 2, 2, "nop"));
        }
        for (int op : new int[]{0x80, 0x82, 0x89, 0xc2, 0xe2}) {
            opcodeList.add(new Opcode(op, NOP, IMM, 2, 2, 2, "nop #$$BYTE"));
        }
        for (int op : new int[]{0x04, 0x44, 0x64}) {
            opcodeList.add(new Opcode(op, NOP, ZP, 2, 3, 3, "nop $$BYTE"));
        }
        for (int op : new int[]{0x14, 0x34, 0x54, 0x74, 0xd4, 0xf4}) {
            opcodeList.add(new Opcode(op, NOP, ZPX, 2, 4, 4, "nop $$BYTE,x"));
        }
        opcodeList.add(new Opcode(0x0c, NOP, ABS, 3, 4, 4, "nop $$WORD"));
        for (int op : new int[]{0x1c, 0x3c, 0x5c, 0x7c, 0xdc, 0xfc}) {
            opcodeList.add(new Opcode(op, NOP, ABX, 3, 4, 5, "nop $$WORD,x"));
        }
        for (int op : new int[]{0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72, 0x92, 0xb2, 0xd2, 0xf2}) {
            opcodeList.add(new Opcode(op, JAM, IMP, 1, 0, 0, "jam"));
        }

        Map<Integer, Opcode> opcodeMap = new HashMap<>();

        for (Opcode opcode : opcodeList) {
            if (opcodeMap.put(opcode.getOpcode(), opcode) != null) {
                throw new IllegalArgumentException("Duplicated opcode");
            }
        }

        OPCODE_MAP = Collections.unmodifiableMap(opcodeMap);
    }

}

                .enc "screen"

*               = $02
.dsection       zeropage
.cerror         * > $30, "To many zero page variables!"

*               = $1000
.dsection       code
.cerror         * > $1fff, "Program section is too long!"

*               = $2000
.dsection       data
.cerror         * > $2fff, "Data section is too long!"


.section        zeropage
num1            .byte   ?
num2            .byte   ?
rl              .byte   ?
rh              .byte   ?
xtmp            .byte   ?
.send

.section        code
                lda     #80
                sta     num1
                lda     #80
                sta     num2
                jsr     mul
                jmp     *

; (signed multiplication) - by Oswald/Resource
; https://codebase64.org/doku.php?id=base:fast_8bit_multiplication_16bit_product
mul             .proc
                ldx     num1
                ldy		num2
		 
                stx     xtmp     ;storing X for later use
                tya
                eor     xtmp     ;getting the sign of the final product
                bmi     neg      ;take another routine if the final product will be negative



                lda     abs,x    ;this is the (a+b) part, we strip a&b from their signs using the abs table.
                clc              ;it is safe to force both numbers to be positive knowing the final sign of the product which we will set later
                adc     abs,y    ;this is done to avoid overflows, and the extra code/tables needed to handle them.
                sta     xtmp
                        
                lda     abs,x    ;(abs(a)-abs(b))
                sec
                sbc     abs,y
                tay
                        
                ldx     abs,y   ;((a-b)/2)^2 will be always positive so its safe to do abs(a-b)
                ldy     xtmp    ;we do this since the sqr table can only handle positive numbers


                ;now we have a+b in Y and a-b in X


                                ;low 8 bits of the product calculated here
                lda     sqrl,y  ;((a+b)/2)^2
                sec
                sbc     sqrl,x  ;-((a-b)/2)^2
                sta     rl
                                ;same as above for high 8 bits
                lda     sqrh,y
                sbc     sqrh,x
                tay
                sta     rh
                ldx     rl
                rts

                ;case for negative final product, all the same except inverting the result at the end.

neg             lda     abs,x
                clc
                adc     abs,y
                sta     xtmp

                lda     abs,x
                sec
                sbc     abs,y
                tay

                ldx     abs,y
                ldy     xtmp

                lda     sqrl,y
                sec
                sbc     sqrl,x
                sta     rl

                lda     sqrh,y
                sbc     sqrh,x
                sta     rh

                ;inverting the result's sign
                lda     rl
                eor     #$ff
                clc
                adc     #$01
                sta     rl
                lda     rh
                eor     #$ff
                adc     #$00
                sta     rh
                ldy     rh
                ldx     rl
                rts
		 
                .pend

.send

.section        data
.include		"tables.asm"
.send
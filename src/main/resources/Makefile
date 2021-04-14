PROGRAM = mul
START_ADDRESS = 1000

all: $(PROGRAM).prg

$(PROGRAM).prg: $(PROGRAM).tmp
#	b2 -c $(START_ADDRESS) $<
#	mv $<.b2 $@
	mv $< $@


$(PROGRAM).tmp: $(PROGRAM).asm
	64tass -C -a $< -o $@ -L $(PROGRAM).lst --verbose-list -l $(PROGRAM).sym -D LINKING=$(LINKING)

.INTERMEDIATE: $(PROGRAM).tmp

.PHONY: all clean

clean:
	rm -f $(PROGRAM).prg $(PROGRAM).tmp $(PROGRAM).lst $(PROGRAM).sym

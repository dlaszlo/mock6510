PROGRAM = mul

all: $(PROGRAM).prg

$(PROGRAM).prg: $(PROGRAM).tmp
	mv $< $@


$(PROGRAM).tmp: $(PROGRAM).asm
	64tass -C -a $< -o $@ -L $(PROGRAM).lst --verbose-list -l $(PROGRAM).sym -D LINKING=$(LINKING)

.INTERMEDIATE: $(PROGRAM).tmp

.PHONY: all clean

clean:
	rm -f $(PROGRAM).prg $(PROGRAM).tmp $(PROGRAM).lst $(PROGRAM).sym

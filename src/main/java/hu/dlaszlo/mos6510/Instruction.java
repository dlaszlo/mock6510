package hu.dlaszlo.mos6510;

@FunctionalInterface
public interface Instruction {

    int run(Opcode opcode, Registers registers, Memory memory);

}

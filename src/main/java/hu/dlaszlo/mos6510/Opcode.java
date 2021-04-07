package hu.dlaszlo.mos6510;

public class Opcode {

    private int opcode;
    private Instruction instruction;
    private AddressResolver addressResolver;
    private int length;
    private int cyclesMin;
    private int cyclesMax;
    private String template;

    public int getOpcode() {
        return opcode;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public AddressResolver getAddressResolver() {
        return addressResolver;
    }

    public int getLength() {
        return length;
    }

    public int getCyclesMin() {
        return cyclesMin;
    }

    public int getCyclesMax() {
        return cyclesMax;
    }

    public String getTemplate() {
        return template;
    }

    public Opcode(int opcode, Instruction instruction, AddressResolver addressResolver, int length, int cyclesMin, int cyclesMax, String template) {
        this.opcode = opcode;
        this.instruction = instruction;
        this.addressResolver = addressResolver;
        this.length = length;
        this.cyclesMin = cyclesMin;
        this.cyclesMax = cyclesMax;
        this.template = template;
    }
}

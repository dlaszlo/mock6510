package hu.dlaszlo.mos6510;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static hu.dlaszlo.mos6510.AddressingMode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks every opcode against the documented lengths and cycle counts of the NMOS 6510
 */
class OpcodeTableTest extends CpuTestBase {

    private static final Set<String> READ_MODIFY_WRITE = Set.of("asl", "lsr", "rol", "ror", "inc", "dec");
    private static final Set<String> STORE = Set.of("sta", "stx", "sty");
    private static final Set<String> BRANCH = Set.of("bpl", "bmi", "bvc", "bvs", "bcc", "bcs", "bne", "beq");
    private static final Set<String> CONTROL_FLOW = Set.of("jmp", "jsr", "rts", "rti", "brk");

    static Stream<Arguments> opcodes() {
        return Opcodes.OPCODE_MAP.values().stream()
                .sorted(Comparator.comparing(Opcode::getOpcode))
                .map(o -> Arguments.of(String.format("%02X %s", o.getOpcode(), o.getTemplate()), o));
    }

    private static String mnemonic(Opcode opcode) {
        return opcode.getTemplate().split(" ")[0];
    }

    private static int expectedLength(AddressResolver mode) {
        if (mode == IMP) {
            return 1;
        }
        if (mode == ABS || mode == ABX || mode == ABY || mode == IND) {
            return 3;
        }
        return 2;
    }

    private static int expectedCyclesMin(String mnemonic, AddressResolver mode) {
        switch (mnemonic) {
            case "pha":
            case "php":
                return 3;
            case "pla":
            case "plp":
                return 4;
            case "jsr":
            case "rts":
            case "rti":
                return 6;
            case "brk":
                return 7;
            case "jmp":
                return mode == ABS ? 3 : 5;
        }
        if (BRANCH.contains(mnemonic) || mode == IMP) {
            return 2;
        }
        if (READ_MODIFY_WRITE.contains(mnemonic)) {
            if (mode == ZP) return 5;
            if (mode == ZPX || mode == ABS) return 6;
            if (mode == ABX) return 7;
        }
        if (mode == IMM) return 2;
        if (mode == ZP) return 3;
        if (mode == ZPX || mode == ZPY || mode == ABS) return 4;
        if (mode == ABX || mode == ABY) return STORE.contains(mnemonic) ? 5 : 4;
        if (mode == IZX) return 6;
        if (mode == IZY) return STORE.contains(mnemonic) ? 6 : 5;
        throw new IllegalArgumentException("Unexpected opcode: " + mnemonic);
    }

    private static int expectedCyclesMax(String mnemonic, AddressResolver mode, int min) {
        if (BRANCH.contains(mnemonic)) {
            return 4;
        }
        boolean read = !STORE.contains(mnemonic) && !READ_MODIFY_WRITE.contains(mnemonic);
        if (read && (mode == ABX || mode == ABY || mode == IZY)) {
            return min + 1;
        }
        return min;
    }

    @Test
    void allDocumentedOpcodesArePresent() {
        assertEquals(151, Opcodes.OPCODE_MAP.size());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("opcodes")
    void lengthAndCycles(String name, Opcode opcode) {
        String mnemonic = mnemonic(opcode);
        AddressResolver mode = opcode.getAddressResolver();
        int min = expectedCyclesMin(mnemonic, mode);
        assertEquals(expectedLength(mode), opcode.getLength(), "length");
        assertEquals(min, opcode.getCyclesMin(), "min cycles");
        assertEquals(expectedCyclesMax(mnemonic, mode, min), opcode.getCyclesMax(), "max cycles");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("opcodes")
    void executesWithBaseCyclesAndAdvancesPc(String name, Opcode opcode) {
        String mnemonic = mnemonic(opcode);
        if (CONTROL_FLOW.contains(mnemonic) || BRANCH.contains(mnemonic)) {
            return;                             // see ControlFlowTest
        }
        // operands and index registers chosen so that no page is crossed
        code(opcode.getOpcode(), 0x10, 0x20);
        reg.setSp(0xf0);
        assertEquals(opcode.getCyclesMin(), step(), "cycles");
        assertPc(CODE + opcode.getLength());
    }

}

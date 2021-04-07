package hu.dlaszlo.mos6510;

@FunctionalInterface
public interface AddressResolver {

    Address getAddress(Registers registers, Memory memory);

}

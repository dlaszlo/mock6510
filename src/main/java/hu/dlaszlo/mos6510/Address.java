package hu.dlaszlo.mos6510;

import java.util.Objects;

public class Address {

    private int address;

    private boolean pageCrossed;

    private boolean pageBug;

    public Address(int address, boolean pageCrossed, boolean pageBug) {
        this.address = address;
        this.pageCrossed = pageCrossed;
        this.pageBug = pageBug;
    }

    public int getAddress() {
        return address;
    }

    public boolean isPageCrossed() {
        return pageCrossed;
    }

    public boolean isPageBug() {
        return pageBug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address1 = (Address) o;
        return address == address1.address && pageCrossed == address1.pageCrossed && pageBug == address1.pageBug;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, pageCrossed, pageBug);
    }

    @Override
    public String toString() {
        return "Address{" +
                "address=" + address +
                ", pageCrossed=" + pageCrossed +
                ", pageBug=" + pageBug +
                '}';
    }

}

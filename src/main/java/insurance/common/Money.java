package insurance.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money implements Comparable<Money> {
    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public static final Money ZERO = new Money(BigDecimal.ZERO); // khởi tạo thật, không để null

    public static Money of(String amount) { // scale 0, HALF_UP
        throw new UnsupportedOperationException("TODO");
    }

    public Money plus(Money o) {
        throw new UnsupportedOperationException("TODO");
    }

    public Money minus(Money o) { // được phép ra số âm
        throw new UnsupportedOperationException("TODO");
    }

    public Money times(String factor) { // "0.85" -> làm tròn về scale 0
        throw new UnsupportedOperationException("TODO");
    }

    public Money min(Money o) {
        throw new UnsupportedOperationException("TODO");
    }

    public Money atLeastZero() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isZero() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isPositive() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int compareTo(Money o) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String toString() { // định dạng 45.000.000
        throw new UnsupportedOperationException("TODO");
    }
}

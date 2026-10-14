package insurance.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private final BigDecimal value; // luôn scale 0

    private Money(BigDecimal v) {
        Objects.requireNonNull(v, "Value cannot be null");
        this.value = v.setScale(0, RoundingMode.HALF_UP);
    }

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public static Money of(String s) {
        Objects.requireNonNull(s, "Amount string cannot be null");
        return new Money(new BigDecimal(s));
    }

    public Money plus(Money o) {
        Objects.requireNonNull(o, "Money operand cannot be null");
        return new Money(value.add(o.value));
    }

    public Money minus(Money o) { // được phép ra số âm
        Objects.requireNonNull(o, "Money operand cannot be null");
        return new Money(value.subtract(o.value));
    }

    public Money times(String f) { // "0.85" -> làm tròn về scale 0
        Objects.requireNonNull(f, "Factor cannot be null");
        return new Money(value.multiply(new BigDecimal(f)));
    }

    public Money min(Money o) {
        Objects.requireNonNull(o, "Money operand cannot be null");
        return compareTo(o) <= 0 ? this : o;
    }

    public Money atLeastZero() {
        return value.signum() < 0 ? ZERO : this;
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    @Override
    public int compareTo(Money o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return this.compareTo(money) == 0;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() { // định dạng 45.000.000
        return String.format(Locale.GERMANY, "%,d", value.longValueExact());
    }
}

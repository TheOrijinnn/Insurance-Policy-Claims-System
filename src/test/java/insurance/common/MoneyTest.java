package insurance.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("1. of('50000000').minus(of('5000000')) = 45.000.000")
    void test1_minusNormal() {
        Money result = Money.of("50000000").minus(Money.of("5000000"));
        assertEquals(Money.of("45000000"), result);
        assertEquals("45.000.000", result.toString());
    }

    @Test
    @DisplayName("2. of('3000000').minus(of('5000000')) = -2.000.000 (không ném exception)")
    void test2_minusNegativeAllowed() {
        Money result = Money.of("3000000").minus(Money.of("5000000"));
        assertEquals(Money.of("-2000000"), result);
        assertEquals("-2.000.000", result.toString());
    }

    @Test
    @DisplayName("3. Kết quả #2 .atLeastZero() = Money.ZERO")
    void test3_atLeastZero() {
        Money negative = Money.of("3000000").minus(Money.of("5000000"));
        assertEquals(Money.ZERO, negative.atLeastZero());
    }

    @Test
    @DisplayName("4. of('50000000').times('0.85') = 42.500.000")
    void test4_timesEightyFivePercent() {
        Money result = Money.of("50000000").times("0.85");
        assertEquals(Money.of("42500000"), result);
    }

    @Test
    @DisplayName("5. of('48000000').times('0.80') = 38.400.000")
    void test5_timesEightyPercent() {
        Money result = Money.of("48000000").times("0.80");
        assertEquals(Money.of("38400000"), result);
    }

    @Test
    @DisplayName("6. of('1001').times('0.85') = 851 (850,85 làm tròn HALF_UP)")
    void test6_roundingHalfUp() {
        Money result = Money.of("1001").times("0.85");
        assertEquals(Money.of("851"), result);
    }

    @Test
    @DisplayName("7. of('1').times('0.5') = 1 và of('1').times('0.49') = 0")
    void test7_roundingBoundary() {
        assertEquals(Money.of("1"), Money.of("1").times("0.5"));
        assertEquals(Money.of("0"), Money.of("1").times("0.49"));
    }

    @Test
    @DisplayName("8. of('100000000').times('0.025') = 2.500.000")
    void test8_premiumAuto() {
        assertEquals(Money.of("2500000"), Money.of("100000000").times("0.025"));
    }

    @Test
    @DisplayName("9. min(45tr, 55tr), thử cả hai thứ tự = 45.000.000")
    void test9_minBothOrders() {
        Money m45 = Money.of("45000000");
        Money m55 = Money.of("55000000");
        assertEquals(m45, m45.min(m55));
        assertEquals(m45, m55.min(m45));
    }

    @Test
    @DisplayName("10. ZERO.isZero(), ZERO.isPositive(), of('1').isPositive(), of('-1').isPositive()")
    void test10_zeroAndPositiveChecks() {
        assertTrue(Money.ZERO.isZero());
        assertFalse(Money.ZERO.isPositive());
        assertTrue(Money.of("1").isPositive());
        assertFalse(Money.of("-1").isPositive());
    }

    @Test
    @DisplayName("11. of('100').equals(of('100.0')) = true (scale được chuẩn hóa)")
    void test11_normalizedScaleEquals() {
        assertTrue(Money.of("100").equals(Money.of("100.0")));
        assertEquals(Money.of("100"), Money.of("100.0"));
    }

    @Test
    @DisplayName("12. a.plus(b) rồi kiểm tra lại a -> a không đổi")
    void test12_immutability() {
        Money a = Money.of("1000000");
        Money b = Money.of("500000");
        Money c = a.plus(b);
        assertEquals(Money.of("1000000"), a);
        assertEquals(Money.of("1500000"), c);
    }

    @Test
    @DisplayName("13. of('45000000').toString() = '45.000.000'")
    void test13_toStringFormat() {
        assertEquals("45.000.000", Money.of("45000000").toString());
        assertEquals("9.223.372.036.854.775.808", Money.of("9223372036854775808").toString());
    }
}

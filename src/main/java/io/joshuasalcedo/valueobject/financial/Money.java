package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable value object representing a monetary amount with currency.
 *
 * <p>All arithmetic uses {@link BigDecimal} with proper rounding — never {@code double} or {@code float}.
 * Mixed-currency operations are rejected at runtime.</p>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * Money price  = Money.of("149.99", FiatCurrency.USD);
 * Money tax    = price.percentage(Percentage.of(12));       // $18.00
 * Money total  = price.add(tax);                            // $167.99
 * Money half   = total.divide(2);                           // $84.00
 * Money crypto = Money.of("0.00542100", CryptoCoin.Known.BTC);   // ₿0.00542100
 * }</pre>
 *
 * @author JoshuaSalcedo
 * @since 1/20/2026 9:26 AM
 */
public record Money(BigDecimal amount, Currency currency) implements ValueObject, Comparable<Money> {

    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    private static final MathContext MATH_CONTEXT = new MathContext(34, DEFAULT_ROUNDING);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public static final Money ZERO = Money.zero(FiatCurrency.USD);
    public static final Money MINUS_ONE = Money.of(-1, FiatCurrency.USD);
    // ==================== Compact Constructor ====================

    public Money {
        Objects.requireNonNull(amount, "Money amount cannot be null");
        Objects.requireNonNull(currency, "Money currency cannot be null");
        amount = amount.setScale(currency.decimalPlaces(), DEFAULT_ROUNDING);
    }


    // ==================== Factory Methods ====================

    /**
     * Creates Money from a BigDecimal amount.
     * This is the preferred provider method — no precision loss.
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Creates Money from a String amount.
     * Safe for precise values like "149.99" or "0.00542100".
     *
     * @throws ValueObjectException if the string is not a valid number
     */
    public static Money of(String amount, Currency currency) {
        Objects.requireNonNull(amount, "Amount string cannot be null");
        try {
            return new Money(new BigDecimal(amount.trim()), currency);
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Invalid money amount: '" + amount + "'");
        }
    }

    /**
     * Parses an amount written with its currency code on either side, e.g. {@code "USD 149.99"},
     * {@code "149.99 usd"}, {@code "PHP 1,250.00"} or {@code "0.5 BTC"}. Grouping commas are ignored.
     *
     * @param text the amount and its currency code
     * @return the money
     * @throws ValueObjectException if it is not an amount with a known currency code
     */
    public static Money parse(String text) {
        Objects.requireNonNull(text, "Money text cannot be null");
        java.util.regex.Matcher m = TEXT.matcher(text.trim());
        if (!m.matches()) throw new ValueObjectException("Not an amount with a currency code: '" + text + "'");
        String code = m.group(1) != null ? m.group(1) : m.group(4);
        String amount = (m.group(1) != null ? m.group(2) : m.group(3)).replace(",", "");
        Currency currency = Currency.parse(code)
                .orElseThrow(() -> new ValueObjectException("Unknown currency code: '" + code + "'"));
        return of(amount, currency);
    }

    private static final java.util.regex.Pattern TEXT = java.util.regex.Pattern.compile(
            "^(?:([A-Za-z]{3,5})\\s*(-?[\\d,]*\\.?\\d+)|(-?[\\d,]*\\.?\\d+)\\s*([A-Za-z]{3,5}))$");

    /**
     * Creates Money from a long (whole units, no fractions).
     * Safe — no precision loss.
     */
    public static Money of(long amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates Money from an int.
     * Safe — no precision loss.
     */
    public static Money of(int amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates Money from any {@link Number} (int, long, double, float, BigDecimal, BigInteger, etc.)
     * with the specified currency.
     *
     * <p>Converts via {@link BigDecimal} to avoid floating-point surprises:
     * integers and longs use {@code BigDecimal.valueOf(long)}, while
     * fractional types use {@code BigDecimal.valueOf(double)}.</p>
     *
     * @param amount any non-null Number
     * @param currency the currency
     * @return the Money value
     */
    public static Money of(Number amount, Currency currency) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        BigDecimal bd;
        if (amount instanceof BigDecimal bigDecimal) {
            bd = bigDecimal;
        } else if (amount instanceof Integer || amount instanceof Long
                || amount instanceof Short || amount instanceof Byte) {
            bd = BigDecimal.valueOf(amount.longValue());
        } else {
            bd = BigDecimal.valueOf(amount.doubleValue());
        }
        return new Money(bd, currency);
    }

    /**
     * Creates Money in USD from any {@link Number}.
     * Convenience shorthand for {@code Money.of(amount, FiatCurrency.USD)}.
     *
     * <p>Usage examples:</p>
     * <pre>{@code
     * Money.of(10);        // $10.00
     * Money.of(19.99);     // $19.99
     * Money.of(0.10);      // $0.10
     * Money.of(100L);      // $100.00
     * }</pre>
     *
     * @param amount any non-null Number
     * @return Money in USD
     */
    public static Money of(Number amount) {
        return of(amount, FiatCurrency.USD);
    }

    /**
     * Creates Money from cents/minor units.
     * Example: {@code Money.ofMinorUnits(1499, FiatCurrency.USD)} → $14.99
     */
    public static Money ofMinorUnits(long minorUnits, Currency currency) {
        BigDecimal amount = BigDecimal.valueOf(minorUnits)
                .movePointLeft(currency.decimalPlaces());
        return new Money(amount, currency);
    }

    /**
     * Creates zero Money in the given currency.
     */
    public static Money zero(Currency currency) {
        Objects.requireNonNull(currency, "Currency cannot be null");
        return new Money(BigDecimal.ZERO, currency);
    }

    // --- Convenience shortcuts for common currencies ---

    public static Money usd(String amount) { return of(amount, FiatCurrency.USD); }
    public static Money usd(long amount)   { return of(amount, FiatCurrency.USD); }
    public static Money php(String amount) { return of(amount, FiatCurrency.PHP); }
    public static Money php(long amount)   { return of(amount, FiatCurrency.PHP); }
    public static Money eur(String amount) { return of(amount, FiatCurrency.EUR); }
    public static Money eur(long amount)   { return of(amount, FiatCurrency.EUR); }

    // ==================== Arithmetic ====================

    /**
     * Adds two Money amounts. Must be the same currency.
     */
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another Money amount. Must be the same currency.
     */
    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies by a BigDecimal factor.
     * Use this for quantity × unit price, exchange rates, etc.
     */
    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Multiplier cannot be null");
        return new Money(this.amount.multiply(multiplier, MATH_CONTEXT), this.currency);
    }

    /**
     * Multiplies by an integer factor.
     */
    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Multiplies by a long factor.
     */
    public Money multiply(long multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Divides by a BigDecimal divisor.
     *
     * @throws ValueObjectException if divisor is zero
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide money by zero");
        }
        BigDecimal result = this.amount.divide(divisor, currency.decimalPlaces(), DEFAULT_ROUNDING);
        return new Money(result, this.currency);
    }

    /**
     * Divides by an integer divisor.
     */
    public Money divide(int divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Divides by a long divisor.
     */
    public Money divide(long divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Returns the negation of this amount.
     * $100 → -$100, -$50 → $50
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Returns the absolute value.
     * -$100 → $100, $50 → $50
     */
    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    // ==================== Percentage Operations ====================

    /**
     * Applies a percentage rate to this money.
     * Example: $200.percentage(10) → $20.00 (10% of $200)
     *
     * @param rate the percentage rate (e.g., 10 for 10%, 12.5 for 12.5%)
     * @return the computed amount
     */
    public Money percentage(BigDecimal rate) {
        Objects.requireNonNull(rate, "Percentage rate cannot be null");
        BigDecimal result = this.amount
                .multiply(rate, MATH_CONTEXT)
                .divide(HUNDRED, currency.decimalPlaces(), DEFAULT_ROUNDING);
        return new Money(result, this.currency);
    }

    /**
     * Applies a Percentage value object to this money.
     * Example: $200.percentage(Percentage.of(10)) → $20.00
     */
    public Money percentage(Percentage rate) {
        Objects.requireNonNull(rate, "Percentage cannot be null");
        return percentage(rate.value());
    }

    /**
     * Adds a percentage on top of this amount.
     * Example: $100.addPercentage(10) → $110.00 (original + 10%)
     */
    public Money addPercentage(BigDecimal rate) {
        return this.add(this.percentage(rate));
    }

    /**
     * Adds a Percentage on top of this amount.
     */
    public Money addPercentage(Percentage rate) {
        return this.add(this.percentage(rate));
    }

    /**
     * Subtracts a percentage from this amount.
     * Example: $100.subtractPercentage(10) → $90.00 (original - 10%)
     */
    public Money subtractPercentage(BigDecimal rate) {
        return this.subtract(this.percentage(rate));
    }

    /**
     * Subtracts a Percentage from this amount.
     */
    public Money subtractPercentage(Percentage rate) {
        return this.subtract(this.percentage(rate));
    }

    // ==================== Allocation / Splitting ====================

    /**
     * Splits this amount into N equal parts.
     * Handles rounding remainder by adding it to the first part.
     * Example: $100 split 3 ways → [$33.34, $33.33, $33.33]
     *
     * @param parts number of parts to split into
     * @return array of Money where sum equals the original amount
     * @throws ValueObjectException if parts ≤ 0
     */
    public Money[] split(int parts) {
        if (parts <= 0) {
            throw new ValueObjectException("Cannot split money into " + parts + " parts");
        }

        Money equalPart = this.divide(parts);
        Money[] result = new Money[parts];
        Arrays.fill(result, equalPart);

        // Distribute rounding remainder to the first part
        Money distributed = equalPart.multiply(parts);
        Money remainder = this.subtract(distributed);
        if (!remainder.isZero()) {
            result[0] = result[0].add(remainder);
        }

        return result;
    }

    /**
     * Allocates this amount by the given ratios.
     * Example: $100 allocated by [1, 2, 3] → [$16.67, $33.33, $50.00]
     *
     * @param ratios the ratios to allocate by
     * @return array of Money where sum equals the original amount
     */
    public Money[] allocate(int... ratios) {
        if (ratios == null || ratios.length == 0) {
            throw new ValueObjectException("Ratios cannot be empty");
        }

        long total = 0;
        for (int ratio : ratios) {
            if (ratio < 0) throw new ValueObjectException("Ratios cannot be negative");
            total += ratio;
        }
        if (total == 0) throw new ValueObjectException("Sum of ratios cannot be zero");

        Money[] result = new Money[ratios.length];
        Money allocated = Money.zero(currency);

        for (int i = 0; i < ratios.length; i++) {
            if (i == ratios.length - 1) {
                // Last allocation gets the remainder to avoid rounding errors
                result[i] = this.subtract(allocated);
            } else {
                BigDecimal share = this.amount
                        .multiply(BigDecimal.valueOf(ratios[i]))
                        .divide(BigDecimal.valueOf(total), currency.decimalPlaces(), DEFAULT_ROUNDING);
                result[i] = new Money(share, currency);
                allocated = allocated.add(result[i]);
            }
        }

        return result;
    }

    // ==================== Comparison ====================

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) <= 0;
    }

    /**
     * Returns the smaller of two Money amounts.
     */
    public Money min(Money other) {
        ensureSameCurrency(other);
        return isLessThanOrEqual(other) ? this : other;
    }

    /**
     * Returns the larger of two Money amounts.
     */
    public Money max(Money other) {
        ensureSameCurrency(other);
        return isGreaterThanOrEqual(other) ? this : other;
    }

    /**
     * Checks if this amount is between min and max (inclusive).
     */
    public boolean isBetween(Money min, Money max) {
        ensureSameCurrency(min);
        ensureSameCurrency(max);
        return isGreaterThanOrEqual(min) && isLessThanOrEqual(max);
    }

    @Override
    public int compareTo(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    // ==================== Conversion ====================

    /**
     * Returns the amount in minor units (cents).
     * Example: $14.99 → 1499
     */
    public long toMinorUnits() {
        return amount.movePointRight(currency.decimalPlaces()).longValueExact();
    }

    /**
     * Returns the amount as a BigDecimal (already scaled to currency precision).
     */
    public BigDecimal toBigDecimal() {
        return amount;
    }

    /**
     * Checks if this Money has the same currency as another.
     */
    public boolean isSameCurrency(Money other) {
        return this.currency.equals(other.currency);
    }

    /**
     * Returns a new Money with a different currency but the same numeric amount.
     * This is NOT currency conversion — it simply redenominates.
     * Use only when you know the amount is already in the target currency.
     */
    public Money inCurrency(Currency newCurrency) {
        return new Money(this.amount, newCurrency);
    }

    // ==================== Aggregation Helpers ====================

    /**
     * Sums multiple Money amounts. All must be the same currency.
     *
     * @throws ValueObjectException if currencies don't match or the list is empty
     */
    public static Money sum(Money first, Money... rest) {
        Objects.requireNonNull(first, "First money cannot be null");
        Money total = first;
        for (Money m : rest) {
            total = total.add(m);
        }
        return total;
    }

    /**
     * Sums an iterable of Money amounts.
     *
     * @param amounts  the amounts to sum
     * @param currency the expected currency (used for the zero base)
     * @return the sum, or zero in the given currency if empty
     */
    public static Money sum(Iterable<Money> amounts, Currency currency) {
        Money total = Money.zero(currency);
        for (Money m : amounts) {
            total = total.add(m);
        }
        return total;
    }

    // ==================== Formatting ====================

    /**
     * Formats as "$ 149.99" (symbol + amount).
     */
    @Override
    public String toString() {
        return String.format("%s %s", currency.symbol(), amount.toPlainString());
    }

    /**
     * Formats with locale-aware number formatting.
     * Example: Money.usd("1234567.89").format(Locale.US) → "$1,234,567.89"
     */
    public String format(Locale locale) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        nf.setCurrency(java.util.Currency.getInstance(currency.code()));
        nf.setMinimumFractionDigits(currency.decimalPlaces());
        nf.setMaximumFractionDigits(currency.decimalPlaces());
        return nf.format(amount);
    }

    /**
     * Formats as "149.99 USD" (amount + currency code).
     */
    public String toCodeString() {
        return String.format("%s %s", amount.toPlainString(), currency.code());
    }

    // ==================== Equality ====================

    /**
     * Two Money values are equal if they have the same currency
     * and the same numeric amount (ignoring trailing zeros).
     * This means USD 10.0 equals USD 10.00.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money that)) return false;
        return amount.compareTo(that.amount) == 0
                && currency.equals(that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    // ==================== Internal ====================

    private void ensureSameCurrency(Money other) {
        Objects.requireNonNull(other, "Cannot operate on null Money");
        if (!this.currency.equals(other.currency)) {
            throw new ValueObjectException(
                    String.format("Currency mismatch: cannot operate on %s and %s",
                            this.currency.code(), other.currency.code()));
        }
    }
}
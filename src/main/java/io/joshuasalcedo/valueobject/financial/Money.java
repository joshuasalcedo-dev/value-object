package io.joshuasalcedo.valueobject.financial;

import org.jspecify.annotations.Nullable;
import java.util.Optional;
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
 * <h2>Usage examples</h2>
 * <pre>{@code
 * Money price  = Money.of("149.99", FiatCurrency.USD);
 * Money tax    = price.percentage(Percentage.of(12));       // $18.00
 * Money total  = price.add(tax);                            // $167.99
 * Money half   = total.divide(2);                           // $84.00
 * Money crypto = Money.of("0.00542100", CryptoCoin.Known.BTC);   // ₿0.00542100
 * }</pre>
 *
 * @param amount   the amount, always held at the currency's decimal places
 * @param currency the currency
 * @author JoshuaSalcedo
 * @since 1/20/2026 9:26 AM
 */
public record Money(BigDecimal amount, Currency currency) implements ValueObject, Comparable<Money> {

    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    private static final MathContext MATH_CONTEXT = new MathContext(34, DEFAULT_ROUNDING);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Zero: {@code Money.zero(FiatCurrency.USD)}. */
    public static final Money ZERO = Money.zero(FiatCurrency.USD);
    /** Minus one: {@code Money.of(-1, FiatCurrency.USD)}. */
    public static final Money MINUS_ONE = Money.of(-1, FiatCurrency.USD);
    // ==================== Compact Constructor ====================

    /**
     * Creates money, rounding the amount half-even to the currency's decimal places.
     *
     * @param amount   the amount
     * @param currency the currency
     * @throws ValueObjectException if either is {@code null}
     */
    public Money {
        if (amount == null) throw new ValueObjectException("Money amount cannot be null");
        if (currency == null) throw new ValueObjectException("Money currency cannot be null");
        amount = amount.setScale(currency.decimalPlaces(), DEFAULT_ROUNDING);
    }


    // ==================== Factory Methods ====================

    /**
     * Creates money from a {@link BigDecimal} amount, rounded half-even to the currency's decimal places.
     * This is the preferred factory: no floating-point conversion.
     *
     * @param amount   the amount
     * @param currency the currency
     * @return the money
     * @throws ValueObjectException if either is {@code null}
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Creates money from a decimal string such as {@code "149.99"} or {@code "0.00542100"}, trimmed and rounded
     * half-even to the currency's decimal places. Grouping commas are not accepted; use {@link #parse(String)}.
     *
     * @param amount   the amount as a decimal string
     * @param currency the currency
     * @return the money
     * @throws ValueObjectException if the string is {@code null} or not a valid number, or the currency is
     *                              {@code null}
     */
    public static Money of(String amount, Currency currency) {
        if (amount == null) throw new ValueObjectException("Amount string cannot be null");
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
     * @throws ValueObjectException if it is {@code null} or not an amount with a known currency code
     */
    public static Money parse(String text) {
        if (text == null) throw new ValueObjectException("Money text cannot be null");
        java.util.regex.Matcher m = TEXT.matcher(text.trim());
        if (!m.matches()) throw new ValueObjectException("Not an amount with a currency code: '" + text + "'");
        String code = m.group(1) != null ? m.group(1) : m.group(4);
        String amount = (m.group(1) != null ? m.group(2) : m.group(3)).replace(",", "");
        Currency currency = Currency.tryParse(code)
                .orElseThrow(() -> new ValueObjectException("Unknown currency code: '" + code + "'"));
        return of(amount, currency);
    }

    private static final java.util.regex.Pattern TEXT = java.util.regex.Pattern.compile(
            "^(?:([A-Za-z]{3,5})\\s*(-?[\\d,]*\\.?\\d+)|(-?[\\d,]*\\.?\\d+)\\s*([A-Za-z]{3,5}))$");

    /**
     * Creates money from a whole number of major units, e.g. {@code 10} for $10.00.
     *
     * @param amount   the amount in whole units
     * @param currency the currency
     * @return the money
     * @throws ValueObjectException if the currency is {@code null}
     */
    public static Money of(long amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates money from a whole number of major units, e.g. {@code 10} for $10.00.
     *
     * @param amount   the amount in whole units
     * @param currency the currency
     * @return the money
     * @throws ValueObjectException if the currency is {@code null}
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
     * @throws ValueObjectException if either is {@code null}, or the number is NaN or infinite
     */
    public static Money of(Number amount, Currency currency) {
        if (amount == null) throw new ValueObjectException("Amount cannot be null");
        if (currency == null) throw new ValueObjectException("Currency cannot be null");
        BigDecimal bd;
        if (amount instanceof BigDecimal bigDecimal) {
            bd = bigDecimal;
        } else if (amount instanceof java.math.BigInteger bigInteger) {
            bd = new BigDecimal(bigInteger);
        } else if (amount instanceof Integer || amount instanceof Long
                || amount instanceof Short || amount instanceof Byte) {
            bd = BigDecimal.valueOf(amount.longValue());
        } else {
            double d = amount.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) throw new ValueObjectException("Not a money amount: " + d);
            bd = BigDecimal.valueOf(d);
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
     * @throws ValueObjectException if it is {@code null}, NaN or infinite
     */
    public static Money of(Number amount) {
        return of(amount, FiatCurrency.USD);
    }

    /**
     * Creates money from minor units (cents), moving the decimal point by the currency's decimal places.
     * Example: {@code Money.ofMinorUnits(1499, FiatCurrency.USD)} is $14.99.
     *
     * @param minorUnits the amount in minor units
     * @param currency   the currency
     * @return the money
     * @throws ValueObjectException if the currency is {@code null}
     */
    public static Money ofMinorUnits(long minorUnits, Currency currency) {
        if (currency == null) throw new ValueObjectException("Currency cannot be null");
        BigDecimal amount = BigDecimal.valueOf(minorUnits)
                .movePointLeft(currency.decimalPlaces());
        return new Money(amount, currency);
    }

    /**
     * Creates zero in the given currency.
     *
     * @param currency the currency
     * @return zero in that currency
     * @throws ValueObjectException if the currency is {@code null}
     */
    public static Money zero(Currency currency) {
        if (currency == null) throw new ValueObjectException("Currency cannot be null");
        return new Money(BigDecimal.ZERO, currency);
    }

    // --- Convenience shortcuts for common currencies ---

    /**
     * Creates US dollars from a decimal string, as {@link #of(String, Currency)}.
     *
     * @param amount the amount as a decimal string, e.g. {@code "149.99"}
     * @return the money in {@code USD}
     * @throws ValueObjectException if the string is not a valid number
     */
    public static Money usd(String amount) { return of(amount, FiatCurrency.USD); }
    /**
     * Creates US dollars from whole dollars.
     *
     * @param amount the amount in whole dollars
     * @return the money in {@code USD}
     */
    public static Money usd(long amount)   { return of(amount, FiatCurrency.USD); }
    /**
     * Creates Philippine pesos from a decimal string, as {@link #of(String, Currency)}.
     *
     * @param amount the amount as a decimal string, e.g. {@code "1250.00"}
     * @return the money in {@code PHP}
     * @throws ValueObjectException if the string is not a valid number
     */
    public static Money php(String amount) { return of(amount, FiatCurrency.PHP); }
    /**
     * Creates Philippine pesos from whole pesos.
     *
     * @param amount the amount in whole pesos
     * @return the money in {@code PHP}
     */
    public static Money php(long amount)   { return of(amount, FiatCurrency.PHP); }
    /**
     * Creates euros from a decimal string, as {@link #of(String, Currency)}.
     *
     * @param amount the amount as a decimal string, e.g. {@code "99.50"}
     * @return the money in {@code EUR}
     * @throws ValueObjectException if the string is not a valid number
     */
    public static Money eur(String amount) { return of(amount, FiatCurrency.EUR); }
    /**
     * Creates euros from whole euros.
     *
     * @param amount the amount in whole euros
     * @return the money in {@code EUR}
     */
    public static Money eur(long amount)   { return of(amount, FiatCurrency.EUR); }

    // ==================== Arithmetic ====================

    /**
     * Adds another amount in the same currency.
     *
     * @param other the amount to add
     * @return the sum
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another amount in the same currency.
     *
     * @param other the amount to subtract
     * @return the difference
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies by a factor, rounding the product half-even to the currency's decimal places.
     * Use this for quantity × unit price, exchange rates, etc.
     *
     * @param multiplier the factor
     * @return the product
     * @throws ValueObjectException if the factor is {@code null}
     */
    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) throw new ValueObjectException("Multiplier cannot be null");
        return new Money(this.amount.multiply(multiplier, MATH_CONTEXT), this.currency);
    }

    /**
     * Multiplies by a whole-number factor.
     *
     * @param multiplier the factor
     * @return the product
     */
    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Multiplies by a whole-number factor.
     *
     * @param multiplier the factor
     * @return the product
     */
    public Money multiply(long multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Divides by a divisor, rounding the quotient half-even to the currency's decimal places.
     *
     * @param divisor the divisor
     * @return the quotient
     * @throws ValueObjectException if the divisor is {@code null} or zero
     */
    public Money divide(BigDecimal divisor) {
        if (divisor == null) throw new ValueObjectException("Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide money by zero");
        }
        BigDecimal result = this.amount.divide(divisor, currency.decimalPlaces(), DEFAULT_ROUNDING);
        return new Money(result, this.currency);
    }

    /**
     * Divides by a whole-number divisor, rounding half-even to the currency's decimal places.
     *
     * @param divisor the divisor
     * @return the quotient
     * @throws ValueObjectException if the divisor is zero
     */
    public Money divide(int divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Divides by a whole-number divisor, rounding half-even to the currency's decimal places.
     *
     * @param divisor the divisor
     * @return the quotient
     * @throws ValueObjectException if the divisor is zero
     */
    public Money divide(long divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Flips the sign: $100 becomes -$100, -$50 becomes $50.
     *
     * @return the negated amount
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Drops the sign: -$100 becomes $100, $50 stays $50.
     *
     * @return the absolute amount
     */
    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    // ==================== Percentage Operations ====================

    /**
     * Works out a percentage of this amount, rounded half-even to the currency's decimal places.
     * Example: 10 percent of $200 is $20.00.
     *
     * @param rate the percentage rate (e.g., 10 for 10%, 12.5 for 12.5%)
     * @return the computed amount
     * @throws ValueObjectException if the rate is {@code null}
     */
    public Money percentage(BigDecimal rate) {
        if (rate == null) throw new ValueObjectException("Percentage rate cannot be null");
        BigDecimal result = this.amount
                .multiply(rate, MATH_CONTEXT)
                .divide(HUNDRED, currency.decimalPlaces(), DEFAULT_ROUNDING);
        return new Money(result, this.currency);
    }

    /**
     * Works out a percentage of this amount, rounded half-even to the currency's decimal places.
     * Example: {@code Money.usd(200).percentage(Percentage.of(10))} is $20.00.
     *
     * @param rate the percentage
     * @return the computed amount
     * @throws ValueObjectException if the rate is {@code null}
     */
    public Money percentage(Percentage rate) {
        if (rate == null) throw new ValueObjectException("Percentage cannot be null");
        return percentage(rate.value());
    }

    /**
     * Adds a percentage of this amount on top of it.
     * Example: $100 plus 10 percent is $110.00.
     *
     * @param rate the percentage rate (e.g., 10 for 10%)
     * @return this amount plus the percentage
     * @throws ValueObjectException if the rate is {@code null}
     */
    public Money addPercentage(BigDecimal rate) {
        return this.add(this.percentage(rate));
    }

    /**
     * Adds a percentage of this amount on top of it.
     *
     * @param rate the percentage
     * @return this amount plus the percentage
     * @throws ValueObjectException if the rate is {@code null}
     */
    public Money addPercentage(Percentage rate) {
        return this.add(this.percentage(rate));
    }

    /**
     * Takes a percentage of this amount off it.
     * Example: $100 less 10 percent is $90.00.
     *
     * @param rate the percentage rate (e.g., 10 for 10%)
     * @return this amount less the percentage
     * @throws ValueObjectException if the rate is {@code null}
     */
    public Money subtractPercentage(BigDecimal rate) {
        return this.subtract(this.percentage(rate));
    }

    /**
     * Takes a percentage of this amount off it.
     *
     * @param rate the percentage
     * @return this amount less the percentage
     * @throws ValueObjectException if the rate is {@code null}
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
     * Allocates this amount by the given ratios. Each share is rounded half-even to the currency's decimal
     * places and the last share takes whatever is left, so the shares always add up to this amount.
     * Example: $100 allocated by [1, 2, 3] → [$16.67, $33.33, $50.00]
     *
     * @param ratios the ratios to allocate by
     * @return array of Money where sum equals the original amount
     * @throws ValueObjectException if there are no ratios, a ratio is negative, or they add up to zero
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

    /**
     * Tells whether the amount is above zero.
     *
     * @return whether the amount is greater than zero
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Tells whether the amount is below zero.
     *
     * @return whether the amount is less than zero
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Tells whether the amount is zero.
     *
     * @return whether the amount is zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Tells whether this amount is more than another in the same currency.
     *
     * @param other the amount to compare to
     * @return whether this is greater than {@code other}
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    /**
     * Tells whether this amount is at least another in the same currency.
     *
     * @param other the amount to compare to
     * @return whether this is greater than or equal to {@code other}
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public boolean isGreaterThanOrEqual(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    /**
     * Tells whether this amount is less than another in the same currency.
     *
     * @param other the amount to compare to
     * @return whether this is less than {@code other}
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public boolean isLessThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * Tells whether this amount is at most another in the same currency.
     *
     * @param other the amount to compare to
     * @return whether this is less than or equal to {@code other}
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public boolean isLessThanOrEqual(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) <= 0;
    }

    /**
     * Returns the smaller of this and another amount in the same currency; this one when they are equal.
     *
     * @param other the amount to compare to
     * @return the smaller amount
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public Money min(Money other) {
        ensureSameCurrency(other);
        return isLessThanOrEqual(other) ? this : other;
    }

    /**
     * Returns the larger of this and another amount in the same currency; this one when they are equal.
     *
     * @param other the amount to compare to
     * @return the larger amount
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
     */
    public Money max(Money other) {
        ensureSameCurrency(other);
        return isGreaterThanOrEqual(other) ? this : other;
    }

    /**
     * Tells whether this amount lies between two others, both inclusive.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return whether {@code min <= this <= max}
     * @throws ValueObjectException if the other amount is {@code null} or the currencies differ
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
     * Returns the amount in minor units (cents), e.g. 1499 for $14.99.
     *
     * @return the amount in minor units
     * @throws ValueObjectException if it does not fit in a {@code long}
     */
    public long toMinorUnits() {
        try {
            return amount.movePointRight(currency.decimalPlaces()).longValueExact();
        } catch (ArithmeticException overflow) {
            throw new ValueObjectException("Too many minor units for a long: " + toCodeString());
        }
    }

    /**
     * Returns the amount, the same as {@link #amount()}.
     *
     * @return the amount, scaled to the currency's decimal places
     */
    public BigDecimal toBigDecimal() {
        return amount;
    }

    /**
     * Tells whether another amount is in the same currency as this one.
     *
     * @param other the amount to compare to
     * @return whether the currencies are equal
     * @throws ValueObjectException if the other amount is {@code null}
     */
    public boolean isSameCurrency(Money other) {
        if (other == null) throw new ValueObjectException("Cannot compare with null Money");
        return this.currency.equals(other.currency);
    }

    /**
     * Returns a new Money with a different currency but the same numeric amount.
     * This is NOT currency conversion — it simply redenominates.
     * Use only when you know the amount is already in the target currency. The amount is rounded half-even to
     * the new currency's decimal places.
     *
     * @param newCurrency the currency to label the amount with
     * @return the same amount in {@code newCurrency}
     * @throws ValueObjectException if the currency is {@code null}
     */
    public Money inCurrency(Currency newCurrency) {
        return new Money(this.amount, newCurrency);
    }

    // ==================== Aggregation Helpers ====================

    /**
     * Sums one or more amounts, all in the same currency.
     *
     * @param first the first amount
     * @param rest  the other amounts
     * @return the sum
     * @throws ValueObjectException if an amount is {@code null} or the currencies differ
     */
    public static Money sum(Money first, Money... rest) {
        if (first == null) throw new ValueObjectException("First money cannot be null");
        Money total = first;
        if (rest == null) return total;
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
     * @throws ValueObjectException if the amounts or currency are {@code null}, or an amount is {@code null} or
     *                              not in {@code currency}
     */
    public static Money sum(Iterable<Money> amounts, Currency currency) {
        if (amounts == null) throw new ValueObjectException("Amounts cannot be null");
        Money total = Money.zero(currency);
        for (Money m : amounts) {
            total = total.add(m);
        }
        return total;
    }

    // ==================== Formatting ====================

    /**
     * Formats as the currency symbol, a space and the plain amount, e.g. {@code "$ 149.99"}.
     *
     * @return the symbol and amount
     */
    @Override
    public String toString() {
        return currency.symbol() + " " + amount.toPlainString();
    }

    /**
     * Formats as a currency amount in a locale's style, with the currency's decimal places.
     * Example: {@code Money.usd("1234567.89").format(Locale.US)} is {@code "$1,234,567.89"}.
     *
     * <p>A currency the JDK does not know as ISO 4217 (every crypto coin, and any fiat code newer than the JDK's
     * data) is written as the locale's grouped number, at the currency's decimal places, after the currency's
     * symbol, e.g. {@code "₿0.00542100"} or {@code "-₿1.00000000"}; when the symbol is just the code,
     * the code follows the number instead, e.g. {@code "1.500000000 SOL"}.
     *
     * @param locale the locale whose number style to use
     * @return the formatted amount
     * @throws ValueObjectException if the locale is {@code null}
     */
    public String format(Locale locale) {
        if (locale == null) throw new ValueObjectException("Locale cannot be null");
        java.util.Currency jdk = null;
        if (currency instanceof FiatCurrency) {
            try {
                jdk = java.util.Currency.getInstance(currency.code());
            } catch (IllegalArgumentException notInJdk) {
                jdk = null;
            }
        }
        if (jdk != null) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
            nf.setCurrency(jdk);
            nf.setMinimumFractionDigits(currency.decimalPlaces());
            nf.setMaximumFractionDigits(currency.decimalPlaces());
            return nf.format(amount);
        }
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setGroupingUsed(true);
        nf.setMinimumFractionDigits(currency.decimalPlaces());
        nf.setMaximumFractionDigits(currency.decimalPlaces());
        String number = nf.format(amount.abs());
        String sign = amount.signum() < 0 ? "-" : "";
        String symbol = currency.symbol();
        if (symbol == null || symbol.isBlank() || symbol.equalsIgnoreCase(currency.code())) {
            return sign + number + " " + currency.code();
        }
        return sign + symbol + number;
    }

    /**
     * Formats as the plain amount, a space and the currency code, e.g. {@code "149.99 USD"}.
     *
     * @return the amount and code
     */
    public String toCodeString() {
        return amount.toPlainString() + " " + currency.code();
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
        if (other == null) throw new ValueObjectException("Cannot operate on null Money");
        if (!this.currency.equals(other.currency)) {
            throw new ValueObjectException(
                    String.format("Currency mismatch: cannot operate on %s and %s",
                            this.currency.code(), other.currency.code()));
        }
    }

    /**
     * Parses an amount with its currency code like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link Money}, or empty when it is not one; never throws
     */
    public static Optional<Money> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text is an amount with a known currency code, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Money}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}

package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * When an invoice must be paid, in the standard trade-credit notation, and the due date that follows from its
 * date. (EN 16931 carries payment terms as text, BT-20, beside the due date, BT-9; this reads the text and works out
 * the date.)
 *
 * <table>
 *   <caption>Terms and when they fall due</caption>
 *   <tr><th>Terms</th><th>{@link Basis}</th><th>Due</th></tr>
 *   <tr><td>{@code Net 30}</td><td>{@code NET}</td><td>30 days after the invoice date</td></tr>
 *   <tr><td>{@code 2/10 Net 30}</td><td>{@code NET}</td><td>30 days after; 2% off if paid within 10 days</td></tr>
 *   <tr><td>{@code EOM}</td><td>{@code END_OF_MONTH}</td><td>the last day of the invoice's month</td></tr>
 *   <tr><td>{@code Net 30 EOM}</td><td>{@code END_OF_MONTH}</td><td>30 days after the end of the invoice's month</td></tr>
 *   <tr><td>{@code 10 MFI}</td><td>{@code MONTH_FOLLOWING}</td><td>the 10th of the month following the invoice</td></tr>
 *   <tr><td>{@code Due on receipt}</td><td>{@code ON_RECEIPT}</td><td>the invoice date</td></tr>
 *   <tr><td>{@code COD}</td><td>{@code CASH_ON_DELIVERY}</td><td>on delivery (no date from the invoice)</td></tr>
 *   <tr><td>{@code CIA}, {@code CWO}, {@code PIA}</td><td>{@code IN_ADVANCE}</td><td>before delivery: the invoice date</td></tr>
 * </table>
 *
 * <pre>{@code
 * PaymentTerms t = PaymentTerms.parse("Net 30 EOM");
 * t.dueDate(LocalDate.of(2026, 1, 5));          // Optional[2026-03-02]: Jan 31 + 30 days
 * PaymentTerms.parse("2/10 Net 30").discountedAmount(invoiceTotal, paidOn, invoiceDate);
 * }</pre>
 *
 * @param basis           what the due date counts from
 * @param days            days after the basis (Net 30 -> 30); for {@code MONTH_FOLLOWING}, the day of the month
 * @param discount        an early-payment discount (2/10 -> 2%), if any
 * @param discountDays    the days from the invoice date the discount holds (2/10 -> 10); 0 when none
 * @author JoshuaSalcedo
 */
public record PaymentTerms(Basis basis, int days, Optional<Percentage> discount, int discountDays) implements ValueObject {

    /** What a due date counts from. */
    public enum Basis {
        /** days after the invoice date */
        NET,
        /** days after the last day of the invoice's month (End Of Month) */
        END_OF_MONTH,
        /** a day of the month after the invoice's (Month Following Invoice) */
        MONTH_FOLLOWING,
        /** due when the invoice arrives */
        ON_RECEIPT,
        /** paid when the goods are delivered */
        CASH_ON_DELIVERY,
        /** paid before the goods ship: cash in advance, cash with order, payment in advance */
        IN_ADVANCE
    }

    private static final Pattern DISCOUNT = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*%?\\s*/\\s*(\\d+)\\s*(?:days?)?,?\\s*(.*)$");
    private static final Pattern NET = Pattern.compile("^(?:n(?:et)?\\s*)?(\\d+)\\s*(?:days?)?(?:\\s*(?:net))?\\s*(eom|end of month)?$");
    private static final Pattern EOM_FIRST = Pattern.compile("^(?:eom|end of month)\\s*(?:\\+|plus)?\\s*(\\d+)?\\s*(?:days?)?$");
    private static final Pattern MFI = Pattern.compile("^(\\d{1,2})(?:st|nd|rd|th)?\\s*(?:mfi|prox(?:imo)?|of the month following(?: invoice)?)$");

    /**
     * @throws ValueObjectException if the days are negative, or a month-following day is not 1-31
     */
    public PaymentTerms {
        if (basis == null) throw new ValueObjectException("Payment terms need a basis");
        if (days < 0 || discountDays < 0) throw new ValueObjectException("Payment term days cannot be negative");
        if (basis == Basis.MONTH_FOLLOWING && (days < 1 || days > 31))
            throw new ValueObjectException("A month-following day is 1-31: " + days);
        discount = discount == null ? Optional.empty() : discount;
        if (discount.isPresent() && discountDays == 0) throw new ValueObjectException("A discount needs its days (2/10)");
    }

    /**
     * @param days days after the invoice date
     * @return Net {@code days}
     */
    public static PaymentTerms net(int days) {
        return new PaymentTerms(Basis.NET, days, Optional.empty(), 0);
    }

    /**
     * @param days days after the end of the invoice's month (0: the end of the month itself)
     * @return {@code EOM} or Net {@code days} EOM
     */
    public static PaymentTerms endOfMonth(int days) {
        return new PaymentTerms(Basis.END_OF_MONTH, days, Optional.empty(), 0);
    }

    /**
     * @param dayOfMonth the day of the following month, 1-31
     * @return {@code dayOfMonth} MFI
     */
    public static PaymentTerms monthFollowing(int dayOfMonth) {
        return new PaymentTerms(Basis.MONTH_FOLLOWING, dayOfMonth, Optional.empty(), 0);
    }

    /** @return due on receipt */
    public static PaymentTerms onReceipt() {
        return new PaymentTerms(Basis.ON_RECEIPT, 0, Optional.empty(), 0);
    }

    /**
     * @param text terms as printed: {@code "Net 30"}, {@code "N30"}, {@code "30 days"}, {@code "2/10 Net 30"},
     *             {@code "2% 10 Net 30"}, {@code "EOM"}, {@code "Net 30 EOM"}, {@code "EOM + 15"}, {@code "10th MFI"},
     *             {@code "Due on receipt"}, {@code "COD"}, {@code "CIA"}, {@code "CWO"}, {@code "PIA"}, {@code "Prepaid"}
     * @return the terms
     * @throws ValueObjectException if they are not payment terms
     */
    public static PaymentTerms parse(String text) {
        if (text == null) throw new ValueObjectException("Payment terms are required");
        String t = text.strip().toLowerCase(Locale.ROOT).replaceAll("[.]", "").replaceAll("\\s+", " ")
                .replaceFirst("^(payment )?terms?:?\\s*", "");
        switch (t) {
            case "due on receipt", "on receipt", "upon receipt", "due upon receipt", "payable on receipt", "immediate",
                 "immediately", "due immediately", "net 0", "cash" -> {
                return onReceipt();
            }
            case "cod", "cash on delivery", "collect on delivery", "payment on delivery" -> {
                return new PaymentTerms(Basis.CASH_ON_DELIVERY, 0, Optional.empty(), 0);
            }
            case "cia", "cash in advance", "cwo", "cash with order", "pia", "payment in advance", "prepaid", "prepayment",
                 "advance payment" -> {
                return new PaymentTerms(Basis.IN_ADVANCE, 0, Optional.empty(), 0);
            }
            default -> { }
        }
        Optional<Percentage> discount = Optional.empty();
        int discountDays = 0;
        Matcher d = DISCOUNT.matcher(t);
        if (d.matches()) {
            discount = Optional.of(Percentage.of(new BigDecimal(d.group(1))));
            discountDays = Integer.parseInt(d.group(2));
            t = d.group(3).strip();
        } else {
            Matcher spaced = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*%\\s+(\\d+)\\s*(?:days?)?,?\\s+(.*)$").matcher(t);
            if (spaced.matches()) {
                discount = Optional.of(Percentage.of(new BigDecimal(spaced.group(1))));
                discountDays = Integer.parseInt(spaced.group(2));
                t = spaced.group(3).strip();
            }
        }
        Matcher eomFirst = EOM_FIRST.matcher(t);
        if (eomFirst.matches()) {
            int days = eomFirst.group(1) == null ? 0 : Integer.parseInt(eomFirst.group(1));
            return new PaymentTerms(Basis.END_OF_MONTH, days, discount, discountDays);
        }
        Matcher mfi = MFI.matcher(t);
        if (mfi.matches()) return new PaymentTerms(Basis.MONTH_FOLLOWING, Integer.parseInt(mfi.group(1)), discount, discountDays);
        Matcher net = NET.matcher(t);
        if (net.matches() && (t.startsWith("n") || t.contains("day") || net.group(2) != null || discount.isPresent())) {
            int days = Integer.parseInt(net.group(1));
            Basis basis = net.group(2) != null ? Basis.END_OF_MONTH : Basis.NET;
            return new PaymentTerms(basis, days, discount, discountDays);
        }
        throw new ValueObjectException("Not payment terms: '" + text + "'");
    }

    /**
     * @param text any text
     * @return the payment terms it is, or empty
     */
    public static Optional<PaymentTerms> tryParse(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (ValueObjectException e) {
            return Optional.empty();
        }
    }

    /**
     * @param invoiceDate the invoice's date
     * @return when it falls due; empty for {@code COD}, which falls due on a delivery the invoice does not date
     */
    public Optional<LocalDate> dueDate(LocalDate invoiceDate) {
        return Optional.ofNullable(switch (basis) {
            case NET -> invoiceDate.plusDays(days);
            case END_OF_MONTH -> invoiceDate.with(TemporalAdjusters.lastDayOfMonth()).plusDays(days);
            case MONTH_FOLLOWING -> {
                LocalDate next = invoiceDate.plusMonths(1).withDayOfMonth(1);
                yield next.withDayOfMonth(Math.min(days, next.lengthOfMonth()));
            }
            case ON_RECEIPT, IN_ADVANCE -> invoiceDate;
            case CASH_ON_DELIVERY -> null;
        });
    }

    /**
     * @param invoiceDate the invoice's date
     * @return the last day the early-payment discount holds, if there is one
     */
    public Optional<LocalDate> discountDate(LocalDate invoiceDate) {
        return discount.map(d -> invoiceDate.plusDays(discountDays));
    }

    /**
     * @param total       the invoice total
     * @param paidOn      the day it is paid
     * @param invoiceDate the invoice's date
     * @return what is owed if paid that day: the total less the discount when paid within its days
     */
    public Money amountDue(Money total, LocalDate paidOn, LocalDate invoiceDate) {
        if (discount.isPresent() && !paidOn.isAfter(invoiceDate.plusDays(discountDays))) {
            return total.subtractPercentage(discount.get());
        }
        return total;
    }

    /** @return whether there is an early-payment discount */
    public boolean hasDiscount() {
        return discount.isPresent();
    }

    /** @return the standard notation: {@code "Net 30"}, {@code "2/10 Net 30"}, {@code "Net 30 EOM"}, {@code "EOM"}, {@code "10 MFI"} */
    @Override
    public String toString() {
        String prefix = discount.map(p -> p.value().stripTrailingZeros().toPlainString() + "/" + discountDays + " ").orElse("");
        return prefix + switch (basis) {
            case NET -> "Net " + days;
            case END_OF_MONTH -> days == 0 ? "EOM" : "Net " + days + " EOM";
            case MONTH_FOLLOWING -> days + " MFI";
            case ON_RECEIPT -> "Due on receipt";
            case CASH_ON_DELIVERY -> "COD";
            case IN_ADVANCE -> "CIA";
        };
    }
}

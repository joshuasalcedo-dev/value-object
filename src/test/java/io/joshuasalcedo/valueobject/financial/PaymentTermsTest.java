package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.PaymentTerms.Basis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTermsTest {

    private static final LocalDate JAN_5 = LocalDate.of(2026, 1, 5);

    @ParameterizedTest(name = "\"{0}\" -> {1} {2}, due {3}")
    @CsvSource({
            "Net 30, NET, 30, 2026-02-04, Net 30",
            "N30, NET, 30, 2026-02-04, Net 30",
            "net30, NET, 30, 2026-02-04, Net 30",
            "30 days, NET, 30, 2026-02-04, Net 30",
            "Terms: Net 45, NET, 45, 2026-02-19, Net 45",
            "'Payment terms: Net 15.', NET, 15, 2026-01-20, Net 15",
            "EOM, END_OF_MONTH, 0, 2026-01-31, EOM",
            "Net 30 EOM, END_OF_MONTH, 30, 2026-03-02, Net 30 EOM",
            "EOM + 15, END_OF_MONTH, 15, 2026-02-15, Net 15 EOM",
            "10th MFI, MONTH_FOLLOWING, 10, 2026-02-10, 10 MFI",
            "31 MFI, MONTH_FOLLOWING, 31, 2026-02-28, 31 MFI",
            "25th prox, MONTH_FOLLOWING, 25, 2026-02-25, 25 MFI",
            "Due on receipt, ON_RECEIPT, 0, 2026-01-05, Due on receipt",
            "Immediate, ON_RECEIPT, 0, 2026-01-05, Due on receipt",
            "Net 0, ON_RECEIPT, 0, 2026-01-05, Due on receipt",
            "CIA, IN_ADVANCE, 0, 2026-01-05, CIA",
            "Prepaid, IN_ADVANCE, 0, 2026-01-05, CIA"
    })
    void readsTermsAndWorksOutTheDueDate(String text, Basis basis, int days, LocalDate due, String printed) {
        PaymentTerms t = PaymentTerms.parse(text);
        assertEquals(basis, t.basis());
        assertEquals(days, t.days());
        assertEquals(Optional.of(due), t.dueDate(JAN_5));
        assertEquals(printed, t.toString());
        assertEquals(t, PaymentTerms.parse(printed), "the printed form reads back");
        assertEquals(t, PaymentTerms.tryParse(text).orElseThrow());
        assertTrue(PaymentTerms.isValid(text));
        assertFalse(t.hasDiscount());
    }

    @Test
    void cashOnDeliveryHasNoDueDate() {
        PaymentTerms cod = PaymentTerms.parse("COD");
        assertEquals(Basis.CASH_ON_DELIVERY, cod.basis());
        assertTrue(cod.dueDate(JAN_5).isEmpty());
        assertEquals("COD", cod.toString());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @CsvSource({"2/10 Net 30, 2, 10, 30", "2% 10 Net 30, 2, 10, 30", "1.5/15 net 45, 1.5, 15, 45", "'2/10, Net 30', 2, 10, 30"})
    void earlyPaymentDiscounts(String text, String percent, int discountDays, int days) {
        PaymentTerms t = PaymentTerms.parse(text);
        assertTrue(t.hasDiscount());
        assertEquals(Percentage.parse(percent), t.discount().orElseThrow());
        assertEquals(discountDays, t.discountDays());
        assertEquals(days, t.days());
        assertEquals(Optional.of(JAN_5.plusDays(discountDays)), t.discountDate(JAN_5));
    }

    @Test
    void amountDueTakesTheDiscountOnlyWithinItsDays() {
        PaymentTerms t = PaymentTerms.parse("2/10 Net 30");
        Money total = Money.usd("100");
        assertEquals(Money.usd("98.00"), t.amountDue(total, JAN_5, JAN_5));
        assertEquals(Money.usd("98.00"), t.amountDue(total, JAN_5.plusDays(10), JAN_5));
        assertEquals(Money.usd("100"), t.amountDue(total, JAN_5.plusDays(11), JAN_5));
        assertEquals(Money.usd("100"), PaymentTerms.net(30).amountDue(total, JAN_5, JAN_5));
        assertEquals("2/10 Net 30", t.toString());
        assertTrue(PaymentTerms.net(30).discountDate(JAN_5).isEmpty());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "whenever", "Net", "Net thirty", "45", "32 MFI", "0 MFI", "Net 99999999999",
            "99999999999/10 Net 30", "2/99999999999 Net 30", "150/10 Net 30", "0/10 Net 30"})
    void rejectsAnythingElse(String text) {
        assertThrows(ValueObjectException.class, () -> PaymentTerms.parse(text));
        assertTrue(PaymentTerms.tryParse(text).isEmpty(), "tryParse never throws");
        assertFalse(PaymentTerms.isValid(text));
    }

    @Test
    void factories() {
        assertEquals(PaymentTerms.parse("Net 30"), PaymentTerms.net(30));
        assertEquals(PaymentTerms.parse("Net 30 EOM"), PaymentTerms.endOfMonth(30));
        assertEquals(PaymentTerms.parse("10 MFI"), PaymentTerms.monthFollowing(10));
        assertEquals(PaymentTerms.parse("Due on receipt"), PaymentTerms.onReceipt());
    }

    @Test
    void constructorChecks() {
        assertThrows(ValueObjectException.class, () -> new PaymentTerms(null, 30, Optional.empty(), 0));
        assertThrows(ValueObjectException.class, () -> PaymentTerms.net(-1));
        assertThrows(ValueObjectException.class, () -> PaymentTerms.endOfMonth(-1));
        assertThrows(ValueObjectException.class, () -> PaymentTerms.monthFollowing(0));
        assertThrows(ValueObjectException.class, () -> PaymentTerms.monthFollowing(32));
        assertThrows(ValueObjectException.class, () -> new PaymentTerms(Basis.NET, 30, Optional.of(Percentage.of(2)), 0));
        assertThrows(ValueObjectException.class, () -> new PaymentTerms(Basis.NET, 30, Optional.of(Percentage.of(2)), -1));
        assertThrows(ValueObjectException.class, () -> new PaymentTerms(Basis.NET, 30, Optional.of(Percentage.of(101)), 10));
        assertEquals(Optional.empty(), new PaymentTerms(Basis.NET, 30, null, 0).discount());
    }

    @Test
    void badDatesAreValueObjectExceptions() {
        PaymentTerms t = PaymentTerms.parse("2/10 Net 30");
        assertThrows(ValueObjectException.class, () -> t.dueDate(null));
        assertThrows(ValueObjectException.class, () -> t.discountDate(null));
        assertThrows(ValueObjectException.class, () -> t.amountDue(null, JAN_5, JAN_5));
        assertThrows(ValueObjectException.class, () -> t.amountDue(Money.usd(1), null, JAN_5));
        assertThrows(ValueObjectException.class, () -> t.amountDue(Money.usd(1), JAN_5, null));
        assertThrows(ValueObjectException.class, () -> PaymentTerms.net(30).dueDate(LocalDate.MAX));
    }
}

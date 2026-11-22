package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Proof of a payment: a {@link BillingDocument} with how, when and how much was paid.
 *
 * <pre>{@code
 * Receipt r = Receipt.parse(text);
 * r.paymentMethod();   // Optional["Visa - 4242"]
 * r.paidOn();          // Optional[2026-01-15]
 * r.amountPaid();      // Optional[USD 42.50]
 * r.reference();       // Optional["TXN-0001"]
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public interface Receipt extends BillingDocument {

    /**
     * {@return it read as a receipt}
     *
     * @param text the receipt's text
     */
    static Receipt parse(String text) {
        return BillingDocumentReader.receipt(text);
    }

    /** {@return how it was paid: card, cash, e-wallet channel} */
    Optional<String> paymentMethod();

    /** {@return when it was paid} */
    Optional<LocalDate> paidOn();

    /** {@return the card it was paid with, when the payment method names one ({@code "Visa - 4242"})} */
    default Optional<MaskedCard> card() {
        return paymentMethod().flatMap(MaskedCard::tryParse);
    }

    /** {@return how much was paid} */
    Optional<Money> amountPaid();

    /** {@return the change given back} */
    Optional<Money> change();

    /** {@return the payment's reference or transaction number} */
    Optional<String> reference();

    /**
     * A receipt's data.
     *
     * @param fields        what it shares with bills and invoices
     * @param paymentMethod how it was paid
     * @param paidOn        when it was paid
     * @param amountPaid    how much was paid
     * @param change        the change given back
     * @param reference     the payment's reference number
     */
    record Data(Fields fields, Optional<String> paymentMethod, Optional<LocalDate> paidOn, Optional<Money> amountPaid,
                Optional<Money> change, Optional<String> reference) implements Receipt {
        /** Makes a receipts data; a null part is empty. */
        public Data {
            paymentMethod = paymentMethod == null ? Optional.empty() : paymentMethod;
            paidOn = paidOn == null ? Optional.empty() : paidOn;
            amountPaid = amountPaid == null ? Optional.empty() : amountPaid;
            change = change == null ? Optional.empty() : change;
            reference = reference == null ? Optional.empty() : reference;
        }
    }

    /**
     * {@return it as a {@link Receipt}, or empty when it is not one; never throws}
     *
     * @param text any text
     */
    public static Optional<Receipt> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * {@return whether it is a {@link Receipt}}
     *
     * @param text any text
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}

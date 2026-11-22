package io.joshuasalcedo.valueobject.financial;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A request for payment for goods or services: a {@link BillingDocument} with a due date and, often, the purchase
 * order it answers.
 *
 * <pre>{@code
 * Invoice inv = Invoice.parse(text);
 * inv.number();    // Optional["INV-0042"]
 * inv.dueDate();   // Optional[2026-02-15]
 * inv.total();     // Optional[USD 1,250.00]
 * inv.paymentTerms();   // Optional[Net 30 EOM]: the due date is worked out from them when not printed
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public interface Invoice extends BillingDocument {

    /**
     * @param text the invoice's text
     * @return it read as an invoice
     */
    static Invoice parse(String text) {
        return BillingDocumentReader.invoice(text);
    }

    /** @return when it must be paid */
    Optional<LocalDate> dueDate();

    /** @return the purchase order it answers */
    Optional<String> purchaseOrder();

    /** @return its payment terms ({@code Net 30}, {@code EOM}, {@code 2/10 Net 30}, ...) */
    Optional<PaymentTerms> paymentTerms();

    /**
     * An invoice's data.
     *
     * @param fields        what it shares with bills and receipts
     * @param dueDate       when it must be paid
     * @param purchaseOrder the purchase order it answers
     * @param paymentTerms  its payment terms
     */
    record Data(Fields fields, Optional<LocalDate> dueDate, Optional<String> purchaseOrder,
                Optional<PaymentTerms> paymentTerms) implements Invoice {
        public Data {
            dueDate = dueDate == null ? Optional.empty() : dueDate;
            purchaseOrder = purchaseOrder == null ? Optional.empty() : purchaseOrder;
            paymentTerms = paymentTerms == null ? Optional.empty() : paymentTerms;
        }
    }
}

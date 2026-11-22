package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.temporal.DateRange;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A statement of what an account owes for a period, as a utility, telco or card issuer sends it: a
 * {@link BillingDocument} with an account number, a billing period and a due date.
 *
 * <pre>{@code
 * Bill bill = Bill.parse(text);
 * bill.accountNumber();   // Optional["1234-5678-90"]
 * bill.billingPeriod();   // Optional[2024-02-01..2024-02-29]
 * bill.dueDate();         // Optional[2024-03-15]
 * bill.total();           // Optional[PHP 1,499.00]
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public interface Bill extends BillingDocument {

    /**
     * @param text the bill's text
     * @return it read as a bill
     */
    static Bill parse(String text) {
        return BillingDocumentReader.bill(text);
    }

    /** @return the account it is for */
    Optional<String> accountNumber();

    /** @return the period it covers */
    Optional<DateRange> billingPeriod();

    /** @return when it must be paid */
    Optional<LocalDate> dueDate();

    /**
     * A bill's data.
     *
     * @param fields        what it shares with invoices and receipts
     * @param accountNumber the account it is for
     * @param billingPeriod the period it covers
     * @param dueDate       when it must be paid
     */
    record Data(Fields fields, Optional<String> accountNumber, Optional<DateRange> billingPeriod,
                Optional<LocalDate> dueDate) implements Bill {
        public Data {
            accountNumber = accountNumber == null ? Optional.empty() : accountNumber;
            billingPeriod = billingPeriod == null ? Optional.empty() : billingPeriod;
            dueDate = dueDate == null ? Optional.empty() : dueDate;
        }
    }
}

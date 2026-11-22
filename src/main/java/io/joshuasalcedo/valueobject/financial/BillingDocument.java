package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.Found;
import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.contact.Party;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.table.Table;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What a {@link Bill}, an {@link Invoice} and a {@link Receipt} share: who issued it to whom, its number and date,
 * its line items and tables, its totals, and a metadata map of everything else found on it.
 *
 * <pre>{@code
 * BillingDocument doc = BillingDocument.parse(ocrText);   // a Receipt, an Invoice or a Bill
 * doc.number();        // Optional["RCPT-1001"]
 * doc.issuer();        // Optional[Northwind Cloud, 100 Example Ave, Springfield ... <billing@northwind.example>]
 * doc.lineItems();     // [Compute hours ..., Storage ..., ...]
 * doc.total();         // Optional[USD 42.50]
 * doc.metadata();      // {Invoice number=INV-0042, addresses=[...], ...}
 * if (doc instanceof Receipt r) r.paymentMethod();         // Optional["Visa - 4242"]
 * }</pre>
 *
 * <p>Read from text by {@link #parse(String)} (which works out the kind) or {@code Bill.parse}, {@code Invoice.parse},
 * {@code Receipt.parse}. Every labelled value it understands fills a field; every other {@code label: value} goes into
 * {@link #metadata()}, as a value object when it is one ({@code Email}, {@code Money}, a date, ...).
 *
 * @author JoshuaSalcedo
 */
public interface BillingDocument extends ValueObject {

    /**
     * Reads a bill, invoice or receipt, working out which from its words ("Receipt", "Statement of Account",
     * "Invoice", "Amount paid", "Billing period", ...).
     *
     * @param text the document's text: OCR output or extracted text
     * @return it as a {@link Receipt}, {@link Invoice} or {@link Bill}
     * @throws ValueObjectException if it does not look like any of them
     */
    static BillingDocument parse(String text) {
        return detect(text).orElseThrow(() -> new ValueObjectException("Not a bill, invoice or receipt"));
    }

    /**
     * {@return it read as a {@link Receipt}, {@link Invoice} or {@link Bill}, or empty when it is none of them}
     *
     * @param text any text
     */
    static Optional<BillingDocument> detect(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        return BillingDocumentReader.kind(text).map(kind -> switch (kind) {
            case RECEIPT -> Receipt.parse(text);
            case INVOICE -> Invoice.parse(text);
            default -> Bill.parse(text);
        });
    }

    /** {@return what the document shares with the other kinds} */
    Fields fields();

    /** {@return its number: the invoice, receipt or bill number} */
    default Optional<String> number() {
        return Optional.ofNullable(fields().number());
    }

    /** {@return the date it was issued (for a receipt, the date paid)} */
    default Optional<LocalDate> date() {
        return Optional.ofNullable(fields().date());
    }

    /** {@return who issued it: the seller, merchant or biller} */
    default Optional<Party> issuer() {
        return Optional.ofNullable(fields().issuer());
    }

    /** {@return who it is for: the customer, client or account holder} */
    default Optional<Party> customer() {
        return Optional.ofNullable(fields().customer());
    }

    /** {@return the lines charged, from its line-item table} */
    default List<LineItem> lineItems() {
        return fields().lineItems();
    }

    /** {@return every table found on it, the line items' among them} */
    default List<Table> tables() {
        return fields().tables();
    }

    /** {@return the total before tax} */
    default Optional<Money> subtotal() {
        return Optional.ofNullable(fields().subtotal());
    }

    /** {@return the tax (VAT, GST, sales tax)} */
    default Optional<Money> tax() {
        return Optional.ofNullable(fields().tax());
    }

    /** {@return the total: the amount due, or the amount of the payment} */
    default Optional<Money> total() {
        return Optional.ofNullable(fields().total());
    }

    /** {@return the currency most of its amounts are in} */
    default Optional<Currency> currency() {
        return Optional.ofNullable(fields().currency());
    }

    /**
     * {@return everything else found on it, by label as printed ("Merchant", "Status", "Channel", ...), plus {@code "addresses"}, {@code "emails"}, {@code "phones"} and {@code "links"} when any were found. A value is a {@link ValueObject} or a {@link LocalDate} when it parses as one, else the text.}
     */
    default Map<String, Object> metadata() {
        return fields().metadata();
    }

    /** {@return every value object found anywhere in its text, by type} */
    default Map<ValueObject.Type, List<ValueObject>> detected() {
        Map<ValueObject.Type, List<ValueObject>> out = new java.util.EnumMap<>(ValueObject.Type.class);
        for (Found f : fields().found()) out.computeIfAbsent(f.type(), k -> new java.util.ArrayList<>()).add(f.value());
        return out;
    }

    /** {@return every value object found in its text, in order, each with the shape that matched it and where} */
    default List<Found> found() {
        return fields().found();
    }

    /**
     * {@return how each field was read, by field name ({@code "number"}, {@code "total"}, {@code "issuer"}, {@code "dueDate"}, ...): the label, the value as printed, the line, and the form it was in}
     */
    default Map<String, Source> sources() {
        return fields().sources();
    }

    /**
     * How a field was read.
     *
     * @param field the field, e.g. {@code "total"}
     * @param label the label as printed, e.g. {@code "TOTAL DUE"}; empty when there was none
     * @param value the value as printed
     * @param line  the line it was on (0-based), -1 when inferred
     * @param form  the shape it had on the page
     */
    record Source(String field, String label, String value, int line, Form form) {}

    /** The shape a field had on the page. */
    enum Form {
        /** {@code "Label: value"} */
        LABEL_COLON,
        /** {@code "Label      value"}: a label cell, then its value cell */
        LABEL_BESIDE,
        /** a label with its value on a line below it */
        LABEL_ABOVE,
        /** a label and its value in one run of words: {@code "Invoice number INV-1"} */
        LABEL_PREFIX,
        /** a short phrase, then an amount or a date: {@code "Previous balance   PHP 1,499.00"} */
        PHRASE_AMOUNT,
        /** a block of lines (a party's name, address and contacts) */
        BLOCK,
        /** a row of a table (a line item, a total) */
        TABLE_ROW,
        /** not printed: worked out (a currency from the country, a total from the lines) */
        INFERRED
    }

    /** {@return it as JSON: its kind, fields, line items, metadata, sources and found values} */
    default String toJson() {
        return io.joshuasalcedo.valueobject.format.Json.pretty(this);
    }

    /** {@return its line items as CSV: description, quantity, unit price, amount, other columns} */
    default String lineItemsCsv() {
        return io.joshuasalcedo.valueobject.format.Csv.of(lineItems());
    }

    /** {@return its text, as read} */
    default String text() {
        return fields().text();
    }

    /**
     * What every bill, invoice and receipt has.
     *
     * @param number    its number
     * @param date      its date
     * @param issuer    who issued it
     * @param customer  who it is for
     * @param lineItems its lines
     * @param tables    its tables
     * @param subtotal  the total before tax
     * @param tax       the tax
     * @param total     the total
     * @param currency  its currency
     * @param metadata  everything else, by label
     * @param found     every value object in its text, with the shape that matched it
     * @param sources   how each field was read, by field name
     * @param text      its text
     */
    record Fields(@Nullable String number, @Nullable LocalDate date, @Nullable Party issuer, @Nullable Party customer,
                  List<LineItem> lineItems, List<Table> tables, @Nullable Money subtotal, @Nullable Money tax,
                  @Nullable Money total, @Nullable Currency currency, Map<String, Object> metadata,
                  List<Found> found, Map<String, Source> sources, String text) {

        /** Makes the fields; null lists and maps become empty, a null text empty. */
        public Fields {
            lineItems = lineItems == null ? List.of() : List.copyOf(lineItems);
            tables = tables == null ? List.of() : List.copyOf(tables);
            metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
            found = found == null ? List.of() : List.copyOf(found);
            sources = sources == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(sources));
            text = text == null ? "" : text;
        }
    }

    /**
     * {@return it as a {@link BillingDocument}, or empty when it is not one; never throws}
     *
     * @param text any text
     */
    public static Optional<BillingDocument> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * {@return whether it is a {@link BillingDocument}}
     *
     * @param text any text
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}

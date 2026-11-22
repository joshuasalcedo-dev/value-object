# value-object

Small, immutable, self-validating value objects for Java 25+, and the tools to find them in text: from a single
email address, to the tables in a page of OCR, to a whole bill, invoice or receipt.

```xml
<dependency>
    <groupId>io.joshuasalcedo</groupId>
    <artifactId>value-object</artifactId>
    <version>0.1.0</version>
</dependency>
```

![Value Object Studio, the demo app, reading a folder of receipts, invoices and bills](image/demo-documents.png)

## What is in it

| Package | Value objects |
|---|---|
| `contact` | `Email`, `PhoneNumber`, `Link`, `Address`, `FullName` (parses printed names: titles, suffixes, *dela Cruz*), `Party` |
| `financial` | `Money`, `Currency` (`FiatCurrency` ISO 4217, `CryptoCoin`), `Percentage`, `PaymentTerms` (Net 30, EOM, 2/10 Net 30, MFI, COD), `CardNumber` (Luhn, masked), `MaskedCard`, `Iban` (mod-97), `Bic`, `Bill`, `Invoice`, `Receipt`, `LineItem` |
| `measurement` | `UnitOfMeasure` (UN/ECE Rec 20 codes), `Quantity`, `Weight`, `Distance`, `FileSize` |
| `temporal` | `CalendarDate` (every printed form), `DateRange`, `Timestamp`, `DateTimeWithZone`, `Time`, `TimeZone`, `DurationNano`, `RecurrenceInterval` |
| `geo` | `Country` (all 249 ISO 3166-1), `Continent`, `Language` (ISO 639-1), `Demonym`, `PostalCode`, `Coordinates` |
| `identifier` | `UniqueId`, `Slug`, `Username`, `DisplayName`, `JwtToken`, `TaxId` (PH TIN, EU VAT, US EIN), `Gtin` (EAN/UPC) |
| `network` | `IpAddress` (v4, v6; literals only, never a DNS lookup), `MacAddress`, `Port`, `DomainName`, `OperatingSystem` |
| `file`, `table`, `visual`, … | `FileType`, `Table`, `Color`, `SemanticVersion`, `Gender`, … |

Every one implements `ValueObject`, validates itself when made (`ValueObjectException` when it is wrong), and is equal
by value. Optional parts are `Optional`, never `null`; lists are empty, never `null`.

Every type reads text the same way:

```java
Email.parse("jane@example.com");     // the value, or ValueObjectException
Email.tryParse(input);               // Optional<Email>; never throws, null is empty
Email.isValid(input);                // boolean; never throws
Country.parse("PH");  Country.parse("Philippines");   // enums: by code or by name
Country.UNKNOWN.isUnknown();         // enums keep UNKNOWN for "printed but not recognised"
```

## One way in: `ValueObject`

```java
// what is in a text
ValueObject.detect("Mail jane@example.com, call +63 917 555 0142");   // {EMAIL=1, PHONE_NUMBER=1}
ValueObject.find(text);                       // the values themselves, by type
ValueObject.scan(text);                       // each with the regex that matched it and where (Found)
ValueObject.identify("192.168.0.1");          // Optional[IPV4_ADDRESS]
ValueObject.parse("149.99 USD");              // Optional[Money]

// make one from anything its factories take
ValueObject.Parse.Money("149.99 USD");
ValueObject.Parse.Port(8080);
ValueObject.Parse.Quantity("2.5 kg");

// check anything; input that is not one fails, it never throws
ValueObject.Validate.MONEY.is.USD.test("149.99 USD");     // true
ValueObject.Validate.GENDER.is.MALE.test("male");         // true
ValueObject.Validate.EMAIL.valid.test("nope");            // false

ValueObject.Type.EMAIL.type();                // Email.class: every value object, listed
```

`ValueObject`, its `Type` list, `Parse` and `Validate` are **generated** from the value objects themselves and
`codegen/detection.txt` (the regex that recognises each one in text). After adding or changing a value object:

```sh
java codegen/GenerateValueObject.java           # rewrites ValueObject.java
java codegen/GenerateValueObject.java --check   # CI: fails if it is out of date
```

## Tables, bills, invoices and receipts

```java
List<Table> tables = ValueObject.tables(text);   // Tesseract TSV, positioned words, or plain text
Table t = tables.getFirst();
t.columns();                // [Item, Qty (numeric), Unit Price (MONEY), Contact (EMAIL)]
t.value(1, "Unit Price");   // Optional[Money]

BillingDocument doc = BillingDocument.parse(text);   // a Receipt, an Invoice or a Bill
doc.issuer();               // Party: name, Address, email, phone, tax id; person() reads it as a FullName
doc.lineItems();            // description, Quantity (with its UOM), unit price, amount
doc.total();                // Money; with no currency printed, the currency of the country it is from
doc.metadata();             // every other labelled value, as value objects where they are
doc.sources();              // how each field was read: its label, its line, its form on the page
if (doc instanceof Invoice inv) inv.dueDate();   // printed, or worked out from its terms (Net 30 EOM)
```

Columns are found where cells line up; the value objects in them name their types, join split columns
(`149.99` + `USD`) and tell the header row from the data.

Reading documents is guesswork, so nothing read throws its input away. When a part comes out wrong, the raw data is
there to read again, by hand, with your own rules, or with an LLM:

| Result | Keeps |
|---|---|
| `BillingDocument` | `text()`: the whole input; `sources()`: each field's label, line and printed value; `found()`, `metadata()` |
| `LineItem` | `printed()`: every cell of its row as printed, by column name |
| `Table` | `text()`: the lines it was read from; `firstLine()`, `lastLine()`, `page()` |
| `Party` | the lines its name, address and contacts were read from |
| `Found` | `text()`, `start()`, `end()`: the match and where it is; `shape()`: the regex |
| `Ocr`, `Pdf` | `rawText`, `tsv` (words with their boxes), `Pdf.words`: the input before any layout |

Labels are read in English and Filipino (*Resibo Blg.*, *Petsa*, *Kabuuan*, *Ibinayad*, *Sukli*, *Mamimili*, …). A
bare `$` is the dollar of the country the document is from (AUD in Australia, CAD in Canada) and USD anywhere else.

## Images and PDFs

```java
try (Ocr ocr = Ocr.english()) {                     // or Ocr.of("eng", "fil")
    Receipt r = (Receipt) BillingDocument.parse(ocr.text(Path.of("receipt.png")));
    String invoice = Pdf.text(Path.of("invoice.pdf"), ocr);   // the text layer; scanned pages through OCR
}
Pdf.text(Path.of("invoice.pdf"));                   // a PDF with a text layer needs no OCR
```

`Ocr` runs Tesseract inside the JVM: no install, no Docker. `Pdf` lays a PDF's text out as it is on the page, the
same way `Ocr.text` lays out an image, so tables and labels read the same from both. Both are **optional**: add the
dependency for what you use.

| To use | Add |
|---|---|
| `Ocr` | `org.bytedeco:tesseract-platform:5.5.0-1.5.11`, and the language data (`eng.traineddata`, `fil.traineddata`, …) found through `TESSDATA_PREFIX` or the usual install folders |
| `Pdf` | `org.apache.pdfbox:pdfbox:3.0.8` |

## CSV and JSON

```java
Json.pretty(doc);              // a document: its fields, lines, metadata, sources and found values
Csv.of(doc.lineItems());       // description,quantity,unitPrice,amount,other
Csv.of(ValueObject.scan(text));
table.toCsv();
```

Both read back what they write, with no library:

```java
Json.read(json, Invoice.class);        Json.readList(json, Found.class);
Csv.read(csv, LineItem.class);         Csv.readTable(csv);   // a Table, its columns typed again
```

Card numbers are always written masked.

## Connectors

All optional: add the library you use.

```java
// Jackson 2 (com.fasterxml.jackson) or Jackson 3 (tools.jackson)
new ObjectMapper().registerModule(new io.joshuasalcedo.valueobject.jackson.ValueObjectModule());
JsonMapper.builder().addModule(new io.joshuasalcedo.valueobject.jackson3.ValueObjectModule()).build();

// Jakarta Persistence: Converters.* are @Converter(autoApply = true); a value object is one readable column
@Entity class Customer { Email email; Money balance; Country country; }   // "jane@example.com", "149.99 USD", "PH"

// Jakarta Validation, for strings at the edge of an application
record SignUp(@ValidEmail String email, @ValidIban String iban, @ValidValue(ValueObject.Type.COUNTRY) String country) {}
```

The jar's module name is `io.joshuasalcedo.valueobject`.

## The demo: Value Object Studio

A desktop app (JavaFX, [AtlantaFX](https://github.com/mkpaz/atlantafx) themes,
[ControlsFX](https://controlsfx.github.io/) spreadsheets, [Ikonli](https://kordamp.org/ikonli/) icons) that reads a
folder (PDFs, images through OCR, `.txt`/`.tsv` as they are) and shows what the library found:

| | |
|---|---|
| ![Line items](image/demo-line-items.png) | ![Analysis](image/demo-analysis.png) |
| **Line items**, with quantities in their Rec 20 units | **Analysis**: by kind, by issuer and by month, never mixing currencies |
| ![Values found](image/demo-values-found.png) | ![Tables](image/demo-tables.png) |
| **Values found**, each with the regex that matched it | **Tables** found in each file |

```sh
mvn install                        # the library, into ~/.m2
mvn -f demo/pom.xml javafx:run     # open the app, then open a folder (or drop one on it)
mvn -f demo/pom.xml javafx:run "-Djavafx.args=--folder samples --theme Dracula"   # open with a folder read
demo/screenshots.sh                # run it on the samples and save the screenshots above into image/
demo\screenshots.ps1               # the same, in PowerShell
```

Every sample document is made up (fictional brands, people, addresses and numbers): the library's test documents in
`src/test/resources/samples` (HTML in `codegen/samples`, rendered and read back with OCR) and `demo/samples`.

## Build

```sh
mvn verify      # compile, test, sources and Javadoc jars; a missing Javadoc comment fails the build
mvn install     # into ~/.m2
```

## Release

Push a tag `vX.Y.Z` (or run the **Release** workflow by hand with a version). The workflow sets the version, signs
every artifact and publishes to Maven Central.

Repository secrets it needs:

| Secret | What |
|---|---|
| `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` | a Central Portal user token |
| `SIGNING_KEY` | an ASCII-armoured private PGP key |
| `SIGNING_KEY_PASSWORD` | its passphrase |

Changes are in [CHANGELOG.md](CHANGELOG.md).

## License

Apache License 2.0.

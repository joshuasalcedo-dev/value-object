# Changelog

All notable changes to this library. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and versions follow [Semantic Versioning](https://semver.org/). Until 1.0.0, a minor version may change the API.

## [Unreleased] — 0.1.0

The first release.

### Value objects
- **Contact:** `Email`, `PhoneNumber` (country code by the ITU prefixes), `Link`, `Address`, `FullName` (parses printed
  names: titles, suffixes, *dela Cruz*), `Party`.
- **Financial:** `Money`, `Currency` (`FiatCurrency` ISO 4217, `CryptoCoin` with `Known` and `Custom` coins),
  `Percentage`, `PaymentTerms` (Net 30, EOM, 2/10 Net 30, MFI, COD, CIA), `CardNumber` (Luhn, always masked),
  `MaskedCard`, `CardBrand`, `Iban` (mod-97), `Bic`, `LineItem`, `Bill`, `Invoice`, `Receipt`.
- **Measurement:** `UnitOfMeasure` (UN/ECE Recommendation 20 codes; every symbol reads back as its unit), `Quantity`,
  `Weight`, `Distance`, `FileSize` (read from text such as `2.5 kg`, `1.5 GB`).
- **Temporal:** `CalendarDate` (every printed form), `DateRange`, `Timestamp`, `DateTimeWithZone`, `Time`,
  `TimeZone`, `DurationNano`, `RecurrenceInterval`.
- **Geo:** `Country` (all 249 ISO 3166-1 countries), `Continent`, `Language` (ISO 639-1), `Demonym`, `PostalCode`,
  `Coordinates`.
- **Identifiers:** `UniqueId`, `Slug`, `Username`, `DisplayName`, `JwtToken`, `TaxId` (PH TIN, EU VAT, US EIN), `Gtin`.
- **Network and more:** `IpAddress` (v4, v6, no DNS lookups), `MacAddress`, `Port`, `DomainName`, `FileType`, `Table`,
  `Color`, `SemanticVersion`, `Gender`, and others.
- One standard on every type: `parse(String)` throws `ValueObjectException`, `tryParse(String)` returns an `Optional`,
  `isValid(String)` a boolean. No null placeholders; enums keep `UNKNOWN`, tested with `isUnknown()`.

### Finding value objects in text
- `ValueObject` is generated (`codegen/GenerateValueObject.java`): `Type` lists every value object, `Parse` makes one
  from anything its factories take, `Validate` checks input (`Validate.MONEY.is.USD`).
- `ValueObject.detect`, `find`, `scan` (each value with the regex that matched it), `identify`, `parse`.
- Tables in text (`ValueObject.tables`): plain text, Tesseract TSV or positioned words; columns typed by their values;
  rows anchored on their numbers, wrapped and centred descriptions joined to their row.
- Bills, invoices and receipts (`BillingDocument.parse`): parties, line items with units, totals, payment terms and due
  dates, metadata, and the source of every field. English and Filipino labels. With no currency printed, the currency of
  the country the document is from.

### Reading files (optional dependencies)
- OCR in the JVM (`Ocr`, Tesseract through JavaCPP): no install, no Docker; English and other languages.
- PDFs (`Pdf`, PDFBox): the text layer laid out as on the page, scanned pages through OCR.

### Formats and connectors
- CSV and JSON, written and read back (`Csv`, `Json`), with no library; card numbers always masked.
- Jackson 2 and Jackson 3 modules, Jakarta Persistence converters, Jakarta Validation constraints (`@ValidValue`,
  `@ValidEmail`, `@ValidPhoneNumber`, `@ValidIban`, `@ValidTaxId`, `@ValidMoney`). All optional.

### Build
- Javadoc with every doclint check on; a missing comment fails the build.
- CI and release check that `ValueObject.java` is generated from the current value objects.
- `Automatic-Module-Name: io.joshuasalcedo.valueobject`.

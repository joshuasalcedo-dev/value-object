package io.joshuasalcedo.valueobject.table;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.ValueObject.Type;
import io.joshuasalcedo.valueobject.contact.Email;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.Money;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TableDetectorTest {

    @Test
    void findsASpaceAlignedTableInsideProse() {
        String text = """
                Dear team,  please find the order below.

                Item              Qty    Unit Price    Contact
                Bolt M8            10    1.50 USD      jane@example.com
                Hex Nut M8        200    0.10 USD      bob@example.org
                Washer, flat       50    0.05 USD      jane@example.com

                Regards,  Procurement
                """;
        List<Table> tables = ValueObject.tables(text);
        assertEquals(1, tables.size(), tables::toString);
        Table t = tables.getFirst();

        assertTrue(t.hasHeader());
        assertEquals(List.of("Item", "Qty", "Unit Price", "Contact"), t.columns().stream().map(Table.Column::name).toList());
        assertEquals(3, t.height());
        assertTrue(t.column("Qty").orElseThrow().numeric());
        assertEquals(Optional.of(Type.MONEY), t.column("Unit Price").orElseThrow().type());
        assertEquals(Optional.of(Type.EMAIL), t.column("Contact").orElseThrow().type());

        assertEquals("Hex Nut M8", t.cell(1, 0));
        assertEquals(Money.of("0.10", FiatCurrency.USD), t.value(1, "Unit Price").orElseThrow());
        assertEquals(Email.of("bob@example.org"), t.value(1, "Contact").orElseThrow());
        assertEquals("200", t.records().get(1).get("Qty"));
    }

    @Test
    void mergesColumnsThatAreOneValueObjectTogether() {
        String text = """
                Item        Price
                Bolt        1.50    USD
                Nut         0.10    USD
                Washer      0.05    USD
                """;
        Table t = Table.parse(text);
        assertEquals(2, t.width(), t::toString);
        assertEquals(Optional.of(Type.MONEY), t.columns().get(1).type());
        assertEquals("1.50 USD", t.cell(0, 1));
    }

    @Test
    void linesAboveTheHeaderDoNotJoinItsColumns() {
        // "Cashier: Rio" sits over Qty, and the right-aligned "1,000.00" reaches under it: they must not join Qty and Amount
        String text = """
                Date: 2026-04-04        Cashier: Rio

                Item                     Qty        Amount
                Rice, 5 kg                 2        640.00
                Canned tuna                6        252.00

                Subtotal                            892.00
                Cash                              1,000.00
                Change                              108.00
                """;
        Table t = ValueObject.tables(text).getFirst();
        assertEquals(List.of("Item", "Qty", "Amount"), t.columns().stream().map(Table.Column::name).toList(), t::toMarkdown);
        assertEquals("2", t.cell(0, 1));
        assertEquals("640.00", t.cell(0, 2));
    }

    @Test
    void readsMarkdownAndTabSeparatedTables() {
        String markdown = """
                | Name  | Email            |
                |-------|------------------|
                | Jane  | jane@example.com |
                | Bob   | bob@example.org  |
                """;
        Table md = Table.parse(markdown);
        assertEquals(List.of("Name", "Email"), md.columns().stream().map(Table.Column::name).toList());
        assertEquals(2, md.height());
        assertEquals(Optional.of(Type.EMAIL), md.columns().get(1).type());

        Table tsv = Table.parse("Code\tCountry\nPH\tPhilippines\nUS\tUnited States\n");
        assertEquals("United States", tsv.cell(1, 1));
        assertEquals(Optional.of(Type.COUNTRY), tsv.columns().get(0).type());
    }

    @Test
    void joinsAWrappedCellToItsRow() {
        String text = """
                No.   Description                 Qty    Amount
                1     Lift envelope glass           1    4,495.50
                      1590mmW x 2290mmH
                2     Vitrine glass                 3    2,469.75
                """;
        Table t = Table.parse(text);
        assertEquals(2, t.height(), t::toMarkdown);
        assertEquals("Lift envelope glass 1590mmW x 2290mmH", t.cell(0, 1));
        assertTrue(t.column("Amount").orElseThrow().numeric());
    }

    @Test
    void findsSeveralTablesAndNoneInProse() {
        String text = """
                Team      Email
                Sales     sales@example.com
                Support   help@example.com

                Some prose follows here.  It has a double space but is not a table.

                Port   Service
                22     ssh
                443    https
                """;
        assertEquals(2, ValueObject.tables(text).size());
        assertTrue(ValueObject.tables("Just a sentence.  And another one.").isEmpty());
        assertTrue(ValueObject.tables(null).isEmpty());
    }

    @Test
    void readsTesseractTsvWithWordBoxes() {
        // a page as Tesseract TSV: header row and two rows, columns at x = 100, 400, 700 (pixels)
        String[][] page = {
                {"Item", "Qty", "Amount"},
                {"Bolt", "10", "15.00"},
                {"Nut", "200", "20.00"},
        };
        StringBuilder tsv = new StringBuilder("level\tpage_num\tblock_num\tpar_num\tline_num\tword_num\tleft\ttop\twidth\theight\tconf\ttext\n");
        for (int r = 0; r < page.length; r++) {
            for (int c = 0; c < page[r].length; c++) {
                String w = page[r][c];
                tsv.append("5\t1\t1\t1\t").append(r + 1).append('\t').append(c + 1).append('\t')
                        .append(100 + c * 300).append('\t').append(200 + r * 60).append('\t')
                        .append(w.length() * 20).append("\t30\t95\t").append(w).append('\n');
            }
        }
        List<Table> tables = ValueObject.tables(tsv.toString());
        assertEquals(1, tables.size(), tables::toString);
        Table t = tables.getFirst();
        assertEquals(List.of("Item", "Qty", "Amount"), t.columns().stream().map(Table.Column::name).toList());
        assertEquals("200", t.cell(1, 1));
        assertTrue(t.column("Amount").orElseThrow().numeric());
    }

    @Test
    void readsPositionedWords() {
        List<TableDetector.Word> words = new ArrayList<>();
        String[][] page = {{"Name", "Email"}, {"Jane", "jane@example.com"}, {"Bob", "bob@example.org"}};
        for (int r = 0; r < page.length; r++) {
            for (int c = 0; c < page[r].length; c++) {
                words.add(new TableDetector.Word(page[r][c], 50 + c * 200, 100 + r * 14, page[r][c].length() * 5, 10));
            }
        }
        Table t = TableDetector.detect(1, words).getFirst();
        assertEquals(Optional.of(Type.EMAIL), t.column("Email").orElseThrow().type());
    }

    @Test
    void isAValueObject() {
        String text = "A    B\n1    2\n3    4\n";
        Table t = ValueObject.Parse.Table(text);
        assertEquals(t, Table.parse(text));
        assertTrue(ValueObject.Validate.TABLE.isValid(text));
        assertFalse(ValueObject.Validate.TABLE.isValid("no table here"));
        assertEquals(Type.TABLE, Type.of(Table.class).orElseThrow());
    }
}

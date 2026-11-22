"""
Writes the demo's extra sample documents: made-up receipts, invoices and bills (every brand, person, address and
number is fictional; domains are .example), as plain text, the way OCR lays them out. Run from this folder:

    python make_samples.py
"""
import pathlib

HERE = pathlib.Path(__file__).resolve().parent


def store(no, date, store, street, city, items, cash):
    lines = [f"        {store}", f"      {street}, {city}", "          Philippines", "     VAT REG TIN 555-201-334-000",
             f"     OFFICIAL RECEIPT No. {no}", "", f"Date: {date}        Cashier: Rio", "",
             "Item                     Qty        Amount"]
    subtotal = 0.0
    for name, qty, price in items:
        amount = qty * price
        subtotal += amount
        lines.append(f"{name:<24} {qty:>3}   {amount:>11,.2f}")
    vat = subtotal - subtotal / 1.12
    lines += ["", f"Subtotal                       {subtotal:>11,.2f}", f"VAT  (12%)                     {vat:>11,.2f}",
              f"TOTAL                          {subtotal:>11,.2f}", f"Cash                           {cash:>11,.2f}",
              f"Change                         {cash - subtotal:>11,.2f}", "", "        Thank you, come again!"]
    return "\n".join(lines) + "\n"


def invoice(no, date, terms, seller, buyer, items):
    lines = ["                                                   INVOICE", "",
             f"Invoice No.:     {no}", f"Invoice Date:    {date}", f"Terms:           {terms}", "",
             f"Bill from:  {seller[0]}", *[f"            {l}" for l in seller[1:]], "",
             f"Bill to:    {buyer[0]}", *[f"            {l}" for l in buyer[1:]], "",
             "Description                    Qty     UOM     Unit Price          Amount"]
    total = 0.0
    for name, qty, uom, price in items:
        amount = qty * price
        total += amount
        lines.append(f"{name:<30} {qty:>4}     {uom:<5}   {'USD ' + format(price, ',.2f'):>13}   {'USD ' + format(amount, ',.2f'):>14}")
    lines += ["", f"                                                  TOTAL     {'USD ' + format(total, ',.2f'):>14}"]
    return "\n".join(lines) + "\n"


def bill(no, date, period, due, account, holder, address, charges):
    total = sum(a for _, a in charges)
    lines = [f"BLUEWAVE WATER CO.                                Statement of Account", "",
             f"Statement No.     {no}", f"Account Number    {account}", f"Account Name      {holder}",
             *[f"                  {l}" for l in address],
             f"Bill Date         {date}", f"Billing Period    {period}", f"Due Date          {due}", "",
             f"Total Amount Due                               PHP {total:,.2f}", "",
             "Summary of charges                              Amount"]
    for name, amount in charges:
        lines.append(f"{name:<44}PHP {amount:>9,.2f}")
    return "\n".join(lines) + "\n"


DOCS = {
    "2026-01-store-sunrise": store("004501", "2026-01-12", "SUNRISE MART", "18 Rizal Street", "Davao City",
                                   [("Rice, 5 kg", 1, 320.0), ("Cooking Oil 1L", 2, 115.0), ("Eggs (dozen)", 1, 105.0)], 1000.0),
    "2026-02-store-sunrise": store("004877", "2026-02-09", "SUNRISE MART", "18 Rizal Street", "Davao City",
                                   [("Rice, 5 kg", 1, 320.0), ("Coffee 200g", 1, 185.0), ("Sugar 1 kg", 2, 78.0)], 1000.0),
    "2026-03-store-hardware": store("000932", "2026-03-21", "PALMA HARDWARE", "77 Osmena Blvd", "Cebu City",
                                    [("Paint, white 4L", 2, 890.0), ("Brush set", 1, 245.0), ("Masking tape", 3, 55.0)], 2500.0),
    "2026-04-store-sunrise": store("005310", "2026-04-04", "SUNRISE MART", "18 Rizal Street", "Davao City",
                                   [("Rice, 5 kg", 2, 320.0), ("Canned tuna", 6, 42.0)], 1000.0),
    "2026-05-store-hardware": store("001204", "2026-05-15", "PALMA HARDWARE", "77 Osmena Blvd", "Cebu City",
                                    [("PVC pipe 1/2in", 4, 165.0), ("Pipe elbow", 6, 18.0), ("Sealant", 1, 210.0)], 1200.0),
    "2026-01-invoice-studio": invoice("INV-2026-011", "January 20, 2026", "Net 30 EOM",
                                      ["Lumen Design Studio", "21 Harbor View Rd", "Portland, OR 97209", "United States"],
                                      ["Kalayaan Digital", "5F Seaside Tower", "Cebu City, 6000", "Philippines"],
                                      [("Brand guidelines", 1, "EA", 1800.0), ("Icon set", 24, "PCS", 35.0)]),
    "2026-03-invoice-studio": invoice("INV-2026-034", "March 18, 2026", "Net 30 EOM",
                                      ["Lumen Design Studio", "21 Harbor View Rd", "Portland, OR 97209", "United States"],
                                      ["Kalayaan Digital", "5F Seaside Tower", "Cebu City, 6000", "Philippines"],
                                      [("Landing page design", 1, "EA", 2400.0), ("Revisions", 6, "HUR", 85.0)]),
    "2026-04-invoice-cloud": invoice("NWC-2026-0412", "April 1, 2026", "Net 15",
                                     ["Northwind Cloud", "100 Example Ave", "Springfield, Oregon 97477", "United States"],
                                     ["Kalayaan Digital", "5F Seaside Tower", "Cebu City, 6000", "Philippines"],
                                     [("Compute", 720, "HUR", 0.25), ("Object storage", 500, "GB", 0.02), ("Support", 1, "MON", 49.0)]),
    "2026-06-invoice-cloud": invoice("NWC-2026-0601", "June 1, 2026", "Net 15",
                                     ["Northwind Cloud", "100 Example Ave", "Springfield, Oregon 97477", "United States"],
                                     ["Kalayaan Digital", "5F Seaside Tower", "Cebu City, 6000", "Philippines"],
                                     [("Compute", 744, "HUR", 0.25), ("Object storage", 650, "GB", 0.02), ("Support", 1, "MON", 49.0)]),
    "2026-02-bill-water": bill("BW-2026-0207", "February 3, 2026", "Jan 1 - Jan 31, 2026", "February 18, 2026", "4410-2231",
                               "Ana Reyes", ["12 Acacia Lane, Barangay 5", "Davao City, 8000"],
                               [("Water consumption (18 m3)", 486.0), ("Maintenance fee", 60.0)]),
    "2026-03-bill-water": bill("BW-2026-0311", "March 3, 2026", "Feb 1 - Feb 28, 2026", "March 18, 2026", "4410-2231",
                               "Ana Reyes", ["12 Acacia Lane, Barangay 5", "Davao City, 8000"],
                               [("Water consumption (21 m3)", 567.0), ("Maintenance fee", 60.0)]),
    "2026-04-bill-water": bill("BW-2026-0408", "April 3, 2026", "Mar 1 - Mar 31, 2026", "April 18, 2026", "4410-2231",
                               "Ana Reyes", ["12 Acacia Lane, Barangay 5", "Davao City, 8000"],
                               [("Water consumption (16 m3)", 432.0), ("Maintenance fee", 60.0)]),
}

if __name__ == "__main__":
    for name, text in DOCS.items():
        (HERE / f"{name}.txt").write_text(text, encoding="utf-8")
        print(f"{name}.txt")

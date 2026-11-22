"""
Renders the sample bills, invoices and receipts the tests read.

The documents are HTML in codegen/samples/: made-up look-alikes of real ones (their layout and style, never their
data: every brand, person, address, number and amount is fictional; domains are .example). Each is rendered to a PNG
with headless Edge or Chrome, then read back with OCR (Tesseract inside the JVM, see OcrSamples.java), so the tests get
genuine OCR text:

    python codegen/samples.py                         HTML -> src/test/resources/samples/<name>.png
    mvn -q test-compile && java -cp ... codegen/OcrSamples.java      PNG -> <name>.txt   (see OcrSamples.java)
"""
import pathlib
import shutil
import subprocess

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCES = ROOT / "codegen/samples"
OUT = ROOT / "src/test/resources/samples"

# viewport per document (CSS px); the screenshot is taken at 2x for sharp text
SIZES = {
    "receipt-cloud": (860, 1000),
    "invoice-contractor": (860, 1060),
    "bill-telco": (850, 640),
    "receipt-billspay": (840, 1000),
    "receipt-transfer": (640, 760),
    "receipt-store": (400, 660),
}

BROWSERS = [
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    "google-chrome", "chromium", "msedge",
]


def browser() -> str:
    for b in BROWSERS:
        if pathlib.Path(b).exists() or shutil.which(b):
            return b
    raise SystemExit("No Edge or Chrome found to render the HTML")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    exe = browser()
    for html in sorted(SOURCES.glob("*.html")):
        name = html.stem
        w, h = SIZES.get(name, (860, 1100))
        png = OUT / f"{name}.png"
        subprocess.run([exe, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--force-device-scale-factor=2",
                        f"--window-size={w},{h}", f"--screenshot={png}", html.as_uri()],
                       check=True, capture_output=True)
        print(f"{html.name} -> {png.relative_to(ROOT)}")

    # PDFs: each document printed (a text layer), and one page that is only an image (a scan, read with OCR)
    for name in ["invoice-contractor", "bill-telco"]:
        pdf = OUT / f"{name}.pdf"
        subprocess.run([exe, "--headless=new", "--disable-gpu", "--no-pdf-header-footer", f"--print-to-pdf={pdf}",
                        (SOURCES / f"{name}.html").as_uri()], check=True, capture_output=True)
        print(f"{name}.html -> {pdf.relative_to(ROOT)}")
    scan_html = OUT.parent / "scan-page.html"
    scan_html.write_text('<!doctype html><html><body style="margin:0"><img src="samples/receipt-store.png" '
                         'style="width:100%"></body></html>', encoding="utf-8")
    try:
        scan = OUT / "receipt-store-scan.pdf"
        subprocess.run([exe, "--headless=new", "--disable-gpu", "--no-pdf-header-footer", f"--print-to-pdf={scan}",
                        scan_html.as_uri()], check=True, capture_output=True)
        print(f"receipt-store.png -> {scan.relative_to(ROOT)} (image only)")
    finally:
        scan_html.unlink()


if __name__ == "__main__":
    main()

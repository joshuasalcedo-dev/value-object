/**
 * Getting text out of files, for the value objects to read: laid out as it is on the page, so tables and labels
 * read the same from every source.
 *
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.extract.Pdf}: a PDF's text layer, and its scanned pages through OCR.
 *     Needs {@code org.apache.pdfbox:pdfbox} 3.x, an optional dependency.</li>
 *     <li>{@link io.joshuasalcedo.valueobject.extract.Ocr}: images through Tesseract inside the JVM (JavaCPP), with
 *     no install and no Docker. Needs {@code org.bytedeco:tesseract-platform}, an optional dependency.</li>
 * </ul>
 */
@NullMarked
package io.joshuasalcedo.valueobject.extract;

import org.jspecify.annotations.NullMarked;

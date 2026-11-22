import io.joshuasalcedo.valueobject.ocr.Ocr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Reads every sample image in src/test/resources/samples with OCR inside the JVM (Tesseract through JavaCPP: no
 * tesseract command, no Docker) and writes its text next to it as {@code <name>.txt}, the input the tests use.
 *
 * <pre>
 *   mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt
 *   java -cp "target/classes;$(cat target/cp.txt)" codegen/OcrSamples.java
 * </pre>
 */
public class OcrSamples {

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("src/test/resources/samples");
        try (Ocr ocr = Ocr.english(); Stream<Path> images = Files.list(dir)) {
            for (Path png : images.filter(p -> p.toString().endsWith(".png")).sorted().toList()) {
                String name = png.getFileName().toString().replaceAll("\\.png$", "");
                Files.writeString(dir.resolve(name + ".txt"), ocr.text(png));
                System.out.println(png.getFileName() + " -> " + name + ".txt");
            }
        }
    }
}

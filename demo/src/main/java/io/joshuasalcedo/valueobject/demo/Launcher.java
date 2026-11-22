package io.joshuasalcedo.valueobject.demo;

/**
 * Starts the demo from a plain classpath ({@code java -cp ... Launcher}): JavaFX wants its {@code Application} class
 * not to be the main class when it is not on the module path.
 *
 * <pre>
 *   Launcher                                   opens the window
 *   Launcher --folder DIR [--folder DIR2]      opens with those folders read
 *   Launcher --folder DIR --screenshots OUT    reads them, saves a screenshot of each tab to OUT, and quits
 *   Launcher ... --theme "Primer Dark"         starts in that theme
 * </pre>
 */
public final class Launcher {

    private Launcher() {}

    public static void main(String[] args) {
        DemoApp.main(args);
    }
}

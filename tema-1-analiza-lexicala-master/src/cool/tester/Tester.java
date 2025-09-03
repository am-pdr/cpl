package cool.tester;

import java.io.*;
import java.util.*;

import cool.compiler.Compiler;

public class Tester {
    public static void main(String[] args) throws IOException {
        // Rulare: java -cp "bin;antlr-4.8-complete.jar;%CLASSPATH%" cool.tester.Tester1
        final String TEST_DIR_NAME = "checker/tests/";
        File testDir = new File(TEST_DIR_NAME);

        if (!testDir.exists() || !testDir.isDirectory()) {
            System.err.println("Directorul de teste nu există: " + TEST_DIR_NAME);
            return;
        }

        FilenameFilter clFilter = (dir, name) -> name.endsWith(".cl");

        File[] files = testDir.listFiles(clFilter);
        if (files == null || files.length == 0) {
            System.err.println("Nu s-au găsit fișiere .cl în directorul: " + TEST_DIR_NAME);
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        int total = 0;
        int maxPointsPerTest = 5;

        for (File file : files) {
            String inPath = file.getPath();
            String outPath = inPath.replace(".cl", ".out");
            String refPath = inPath.replace(".cl", ".ref");

            try (PrintStream testOut = new PrintStream(outPath, "UTF-8")) {
                System.setOut(testOut);
                System.setErr(testOut);

                Compiler.main(new String[]{inPath});
            } catch (Exception e) {
                originalOut.printf("%-30s -> Exceptie: %s%n", file.getName(), e.getMessage());
                continue;
            }

            System.setOut(originalOut);
            System.setErr(originalErr);

            originalOut.printf("%-30s -> ", file.getName());
            int result = compare(outPath, refPath);

            if (result == 0) {
                originalOut.println("OK");
                total += maxPointsPerTest;
            } else {
                originalOut.println("Eșuat la linia " + result);
            }
        }

        originalOut.println("Scor total: " + total);
    }

    public static int compare(String file1, String file2) throws IOException {
        try (
                LineNumberReader reader1 = new LineNumberReader(new FileReader(file1));
                LineNumberReader reader2 = new LineNumberReader(new FileReader(file2))
        ) {
            String line1, line2;

            while ((line1 = reader1.readLine()) != null &
                    (line2 = reader2.readLine()) != null) {
                if (!line1.equals(line2)) {
                    return reader1.getLineNumber();
                }
            }

            if (line1 != null || reader2.readLine() != null)
                return reader1.getLineNumber() + 1;

            return 0;
        }
    }
}

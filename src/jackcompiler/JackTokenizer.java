package jackcompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class JackTokenizer {

    private Scanner inputFile;
    private String cleanInput;

    public JackTokenizer(String fileName) {

        try {
            inputFile = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            // TODO Learn standard practice
            e.printStackTrace();
            System.err.println("File not found");
            System.exit(0);
        }

        String line;
        StringBuilder cleanInputBuilder = new StringBuilder();

        while (inputFile.hasNextLine()) {
            line = inputFile.nextLine().trim().replaceAll(" ", "");
            cleanInputBuilder.append(line.split("//")[0]);
        }

        cleanInput = cleanInputBuilder.toString();
    }

    public static void main(String[] args) {
        JackTokenizer test = new JackTokenizer(args[0]);
        System.out.println(test.cleanInput);
    }
}

package jackcompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Manages the tokenization of a Jack source code file and outputs an XML parse
 * tree consisting of all of the tokens.
 *
 * @author Mark Pichler
 */
public class JackTokenizer {

    // SYMBOLS includes empty space for when used as a delimiter
    private final String SYMBOLS = " {}()[].,;+-*/&|<>+~=";
    private Scanner inputScanner;
    private String cleanInput;
    private TokenType tokenType;
    private char symbol;
    private int tracker;
    private int intVal;
    private StringBuilder tokenBuilder;
    private String token;
    private String identifier;
    private String stringVal;
    private Keyword keyword;
    private String outputFile;
    private String symbolStr;

    /**
     * Instantiates a new JackTokenizer and cleans the original source code of
     * all comments, multiline comments, and line breaks.
     *
     * @param fileName path of Jack source code to be tokenized
     */
    public JackTokenizer(String fileName) {
        File inputFile = new File(fileName);
        try {
            inputScanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            // TODO Learn standard practice
            e.printStackTrace();
            System.err.println("File not found");
            System.exit(0);
        }

        outputFile = fileName.substring(0, fileName.lastIndexOf(".")) + "T.xml";

        tracker = 0;
        tokenBuilder = new StringBuilder();

        String line;
        StringBuilder cleanInputBuilder = new StringBuilder();

        while (inputScanner.hasNextLine()) {
            line = inputScanner.nextLine().trim();

            // Cleans multiline comments
            if (line.startsWith("/**")) {
                while (!line.endsWith("*/")) {
                    line = inputScanner.nextLine().trim();
                }
            } else {
                cleanInputBuilder.append(line.split("//")[0]);
            }
        }

        cleanInput = cleanInputBuilder.toString();
    }

    /**
     * Advances the tracker past the next token and updates all object fields
     * to refer to the current token.
     */
    public void advance() {

        // Skip spaces and advance the tracker accordingly
        while (cleanInput.charAt(tracker) == ' ') {
            tracker++;
        }

        // Check if tracker is pointing to a symbol
        // i.e. next token is a symbol
        if (SYMBOLS.indexOf(cleanInput.charAt(tracker)) != -1) {
            readSymbol();
        } else if (cleanInput.charAt(tracker) == '"') {
            readStringConst();
        } else if (Character.isDigit(cleanInput.charAt(tracker))) {
            readIntConst();
        } else {

            // Advances tracker to next space or symbol and builds a token.
            // ASSUMPTION: The Jack source code ends in a symbol, otherwise
            //             will iterate out of bounds.
            while (SYMBOLS.indexOf(cleanInput.charAt(tracker)) == -1) {
                tokenBuilder.append(cleanInput.charAt(tracker));
                tracker++;
            }

            token = tokenBuilder.toString();
            // TODO Separation of concerns is a bit strange here
            // Assigns the token as a keyword or identifier
            if (!isKeyword(token)) {
                readIdentifier();
            }

            // TODO Might be a better way to do the reset
            // Reset tokenBuilder
            tokenBuilder.setLength(0);
        }

    }

    /**
     * Checks if there are more tokens to be parsed.
     *
     * @return true if more tokens exist, false otherwise
     */
    public boolean hasMoreTokens() {
        if (tracker < cleanInput.length()) {
            return true;
        }
        return false;
    }


    private void printElement(PrintWriter outputFile) {
        switch (tokenType) {
            case KEYWORD:
                outputFile.println(
                        "<keyword> " + keyword.toString() + " </keyword>");
                break;
            case SYMBOL:
                switch (symbol) {
                    case '<':
                        symbolStr = "&lt";
                        break;
                    case '>':
                        symbolStr = "&gt";
                        break;
                    case '"':
                        symbolStr = "&quot";
                        break;
                    case '&':
                        symbolStr = "&amp";
                        break;
                    default:
                        symbolStr = "" + symbol;
                }
                outputFile.println(
                        "<symbol> " + symbolStr + " </symbol>");
                break;
            case INT_CONST:
                outputFile.println(
                        "<integerConstant> " + intVal + " </integerConstant>");
                break;
            case STRING_CONST:
                outputFile.println(
                        "<stringConstant> " + stringVal + " </stringConstant>");
                break;
            case IDENTIFIER:
                outputFile.println(
                        "<identifier> " + identifier + " </identifier>");
                break;
        }
    }

    /**
     * Determines if a token is a keyword.  If token is a keyword, sets the
     * keyword field to the current token and updates tokenType.
     *
     * @param token the token to be checked
     * @return true if keyword, false otherwise
     */
    private boolean isKeyword(String token) {
        for (Keyword keyword : Keyword.values()) {
            if (token.equals(keyword.toString())) {
                this.keyword = keyword;
                tokenType = TokenType.KEYWORD;
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the identifier to the current token and updates tokenType.
     */
    private void readIdentifier() {
        identifier = token;
        tokenType = TokenType.IDENTIFIER;
    }

    /**
     * Reads the next symbol from cleanInput, adjusts the tracker accordingly,
     * and updates symbol and tokenType.
     */
    private void readSymbol() {
        symbol = cleanInput.charAt(tracker);
        tokenType = TokenType.SYMBOL;
        tracker++;
    }

    /**
     * Reads the next integer constant from cleanInput, adjusts the tracker
     * accordingly, and updates intVal and tokenType.
     */
    private void readIntConst() {
        while (Character.isDigit(cleanInput.charAt(tracker))) {
            tokenBuilder.append(cleanInput.charAt(tracker));
            tracker++;
        }
        intVal = Integer.parseInt(tokenBuilder.toString());
        tokenType = TokenType.INT_CONST;

        // TODO Create a subroutine for this process
        // Reset tokenBuilder
        tokenBuilder.setLength(0);
    }

    /**
     * Reads the next string constant from cleanInput, adjusts the tracker
     * accordingly, and updates stringVal and tokenType.
     */
    private void readStringConst() {

        tracker++; // Move tracker past first quotation mark

        while (cleanInput.charAt(tracker) != '"') {
            tokenBuilder.append(cleanInput.charAt(tracker));
            tracker++;
        }

        tracker++; // Move tracker past last quotation mark

        stringVal = tokenBuilder.toString();
        tokenType = TokenType.STRING_CONST;

        // TODO Create a subroutine for this process
        // Reset tokenBuilder
        tokenBuilder.setLength(0);
    }


    public static void main(String[] args) {
        JackTokenizer test = new JackTokenizer(args[0]);

        PrintWriter outputWriter = null;
        try {
            outputWriter = new PrintWriter(test.outputFile);
        } catch (IOException e) {
            // TODO Learn standard practice
            e.printStackTrace();
            System.err.println("File error");
            System.exit(0);
        }

        outputWriter.println("<tokens>");
        while (test.hasMoreTokens()) {
            test.advance();
            test.printElement(outputWriter);
        }
        outputWriter.println("</tokens>");
        outputWriter.close();

    }
}

/*
    1DV610 HT21
    Lab 1 - Tokenizer
    Filename: Tokenizer.java
    Author: Oskari Löytynoja
 */

package tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tokenizer class. Tokenizes a string according to a Grammar.
 * Has methods for retrieving the tokens.
 */
public class Tokenizer {

    private final List<Token> tokens = new ArrayList<>();
    private Grammar grammar;
    private String stringToTokenize;
    private List<TokenType> tokenTypes = new ArrayList<>();
    private String originalStringToTokenize;
    private int activeTokenIndex;

    /**
     * Default constructor.
     */
    public Tokenizer() {
    }

    /**
     * Constructor, accepts a grammar
     * and a string to be tokenized.
     */
    public Tokenizer(Grammar grammar, String stringToTokenize) {
        setGrammar(grammar);
        setStringToTokenize(stringToTokenize);
        originalStringToTokenize = stringToTokenize;
    }

    /**
     * Sets the grammar.
     */
    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
        tokenTypes = grammar.getTokenTypes();
    }

    /**
     * Returns the grammar.
     */
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Sets the string to be tokenized.
     */
    public void setStringToTokenize(String stringToTokenize) {
        this.stringToTokenize = stringToTokenize;
        originalStringToTokenize = stringToTokenize;
    }

    /**
     * Returns the original string to be tokenized.
     */
    public String getStringToTokenize() {
        return originalStringToTokenize;
    }

    /**
     * Returns the token pointed to by activeTokenIndex.
     *
     * @throws TokenizerException if no match for any TokenType
     */
    public Token getActiveToken() throws TokenizerException {

        // If the token at activeTokenIndex haven't
        // been retrieved yet, get the longest
        // matching token and add it to tokensList.
        if (tokens.size() <= activeTokenIndex) {
            tokens.add(getLongestMatchingToken());
        }
        return tokens.get(activeTokenIndex);
    }

    /**
     * Returns the previous token if there is one.
     *
     * @throws TokenizerException if no tokens or at first token.
     */
    public Token getPrevToken() throws TokenizerException {
        prev();
        return getActiveToken();
    }

    /**
     * Returns the next token if there is one.
     *
     * @throws TokenizerException if active token is END.
     */
    public Token getNextToken() throws TokenizerException {
        next();
        return getActiveToken();
    }

    /**
     * Returns true if there is a token prior to the active one.
     */
    public boolean hasPrev() {
        return activeTokenIndex > 0;
    }

    /**
     * Returns false if active or next token is END.
     *
     * @throws TokenizerException if no match for any TokenType.
     */
    public boolean hasNext() throws TokenizerException {

        if (getActiveToken().equals(Token.END)) {
            return false;
        }

        Token nextToken = getNextToken();
        // getNextToken() adds 1 to activeTokenIndex so decrease it
        activeTokenIndex--;
        return !nextToken.equals(Token.END);
    }

    /**
     * Decreases the token index by one.
     *
     * @throws TokenizerException if no tokens or at first token.
     */
    public Tokenizer prev() throws TokenizerException {

        if (activeTokenIndex == 0) {
            throw new TokenizerException("No tokens before the first one.");
        }
        activeTokenIndex--;
        getActiveToken();
        return this;
    }

    /**
     * Increases the token index by one.
     *
     * @throws TokenizerException if active token is END.
     */
    public Tokenizer next() throws TokenizerException {

        if (getActiveToken().equals(Token.END)) {
            throw new TokenizerException("No tokens after the END token.");
        }
        activeTokenIndex++;
        getActiveToken();
        return this;
    }

    /**
     * Returns token at index.
     *
     * @throws TokenizerException if index out of bounds.
     */
    public Token getTokenAt(int index) throws TokenizerException {

        getAllTokens();

        int maxIndex = tokens.size() - 1;

        if (index < 0) {
            throw new TokenizerException("Negative token index.");
        }

        if (index > maxIndex) {
            throw new TokenizerException("Index too large. Max index = " + maxIndex);
        }

        return tokens.get(index);
    }

    /**
     * Returns the first token and sets the activeTokenIndex to zero
     *
     * @throws TokenizerException if no match for any TokenType.
     */
    public Token getFirstToken() throws TokenizerException {
        activeTokenIndex = 0;
        return getActiveToken();
    }

    /**
     * Returns the last token. Returns
     * END if there is none.
     *
     * @throws TokenizerException if stringToTokenize contains
     *                            characters that don't match any TokenType.
     */
    public Token getLastToken() throws TokenizerException {
        getAllTokens();
        if (tokens.size() > 0) {
            activeTokenIndex = tokens.size() - 1;
            return getActiveToken();
        }
        return Token.END;
    }

    /**
     * Returns a List with all tokens,
     * activeTokenIndex is retained.
     *
     * @throws TokenizerException if stringToTokenize contains
     *                            characters that don't match any TokenType.
     */
    public List<Token> getAllTokens() throws TokenizerException {
        // Save activeTokenIndex
        int savedIndex = activeTokenIndex;

        activeTokenIndex = 0;
        while (!getActiveToken().equals(Token.END)) {
            next();
        }

        // Restore activeTokenIndex
        activeTokenIndex = savedIndex;

        // Remove END token before returning list
        tokens.remove(tokens.size() - 1);
        return tokens;
    }

    /**
     * Returns the zero based index of the active token.
     */
    public int getActiveTokenIndex() {
        return activeTokenIndex;
    }

    /**
     * Returns the longest matching token. END token is
     * returned if no more tokens in stringToBeTokenized.
     * TokenType with the lowest index has the highest
     * priority if two tokens has the same length.
     *
     * @throws TokenizerException if no match for any TokenType.
     */
    private Token getLongestMatchingToken() throws TokenizerException {

        if (grammar.isTrimming()) {
            stringToTokenize = stringToTokenize.trim();
        }

        if (stringToTokenize.length() == 0) {
            return Token.END;
        }

        int longestMatch = -1;
        int tokenTypeIndex = 0;
        String text = "";

        // Tests all TokenTypes and finds the
        // longest match (=maximal munch).
        for (int i = 0; i < tokenTypes.size(); i++) {
            String regex = tokenTypes.get(i).getRegex();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(stringToTokenize);
            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (start == 0) {
                    int length = end - start;
                    if (length > longestMatch) {
                        tokenTypeIndex = i;
                        longestMatch = length;
                        text = matcher.group();
                    }
                }
            }
        }

        // No match, throw an TokenizerException
        if (longestMatch == -1) {
            throw new TokenizerException("No lexical element matches \"" + stringToTokenize + "\"");
        }

        // Remove the matched text from stringToTokenize.
        stringToTokenize = stringToTokenize.substring(text.length());

        return new Token(tokenTypes.get(tokenTypeIndex), text);
    }
}

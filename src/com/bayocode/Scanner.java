package src.com.bayocode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static src.com.bayocode.TokenType.*; 

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", AND);
    keywords.put("program", PROGRAM);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("for", FOR);
    keywords.put("func", FUNC);
    keywords.put("if", IF);
    keywords.put("null", NULL);
    keywords.put("or", OR);
    keywords.put("output", OUTPUT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("this", THIS);
    keywords.put("true", TRUE);
    keywords.put("assign", ASSIGN);
    keywords.put("while", WHILE);
    keywords.put("input", INPUT);

  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      //The beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(SUBTRACT); break;
      case '+': addToken(ADD); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 
      case '!': addToken(match('=') ? NOT_EQUAL : NEGATE); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
      case '/': if (match('/')) { while (peek() != '\n' && !isAtEnd()) advance(); } else { addToken(SLASH); } break;
      case ' ':
      case '\r':
      case '\t': break;
      case '\n' : line++; break; 
      case '"': string(); break;
      default: if (isDigit(c)) { number(); } else if (isAlpha(c)) { identifier(); } else { DRS.error(line, "Unexpected character."); } break;
    } 
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  private void number() {
    boolean isFloat = false;
    
    //Consume all the digits for the integer part
    while (isDigit(peek())) advance();

    //Look for a fractional part
    if (peek() == '.' && isDigit(peekNext())) {
        isFloat = true;
        //Consume the "."
        advance();

        //Consume all digits after decimal point
        while (isDigit(peek())) advance();
    }

    //Convert the substring to a number
    String numberStr = source.substring(start, current);
    
    if (isFloat) {
        //Handle as a floating point number
        double value = Double.parseDouble(numberStr);
        addToken(FLOAT, value);
    } else {
        //Handle as an integer
        int value = Integer.parseInt(numberStr);
        addToken(INTEGER, value);
    }
}

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      DRS.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(TEXT, value);
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  } 

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  } 

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}

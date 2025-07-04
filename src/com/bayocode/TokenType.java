package src.com.bayocode;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, SUBTRACT, ADD, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    NEGATE, NOT_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, TEXT, INTEGER, FLOAT,

    // Keywords.
    AND, PROGRAM, ELSE, FALSE, FUNC, FOR, IF, NULL, OR,
    OUTPUT, RETURN, SUPER, THIS, TRUE, ASSIGN, WHILE, INPUT,

    EOF
}

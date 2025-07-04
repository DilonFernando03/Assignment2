package src.com.bayocode;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import static src.com.bayocode.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;
  
    Parser(List<Token> tokens) {
      this.tokens = tokens;
    }

    List<Stmt> parse() {
      List<Stmt> statements = new ArrayList<>();
      while (!isAtEnd()) {
        statements.add(declaration());
      }

      return statements; 
    }

    private Expr expression() {
      return assignment();
    }

    private Stmt declaration() {
      try {
        if (match(FUNC)) return function("function");
        if (match(ASSIGN)) return setDeclaration();
        return statement();
      } catch (ParseError error) {
        synchronize();
        return null;
      }
    }

    private Stmt statement() {
      if (match(FOR)) return forStatement();
      if (match(IF)) return ifStatement();
      if (match(OUTPUT)) return outputStatement();
      if (match(RETURN)) return returnStatement();
      if (match(INPUT)) return inputStatement();
      if (match(WHILE)) return whileStatement();
      if (match(LEFT_BRACE)) return new Stmt.Block(block());
      return expressionStatement();
    }

    private Stmt forStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'for'.");
      Stmt initializer;
        if (match(SEMICOLON)) {
          initializer = null;
        } else if (match(ASSIGN)) {
          initializer = setDeclaration();
        } else {
          initializer = expressionStatement();
        }
        Expr condition = null;
        if (!check(SEMICOLON)) {
          condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");
        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
          increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();
        if (increment != null) {
          body = new Stmt.Block(
              Arrays.asList(
                  body,
                  new Stmt.Expression(increment)));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) {
          body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt outputStatement() {
      Expr value = expression();
      consume(SEMICOLON, "Expect ';' after value.");
      return new Stmt.Output(value);
    }
    
    private Stmt returnStatement() {
      Token keyword = previous();
      Expr value = null;
      if (!check(SEMICOLON)) {
        value = expression();
      }
  
      consume(SEMICOLON, "Expect ';' after return value.");
      return new Stmt.Return(keyword, value);
    }

    private Stmt setDeclaration() {
      Token name = consume(IDENTIFIER, "Expect variable name.");
  
      Expr initializer = null;
      if (match(EQUAL)) {
        initializer = expression();
      }
  
      consume(SEMICOLON, "Expect ';' after variable declaration.");
      return new Stmt.Assign(name, initializer);
    }

    private Stmt inputStatement() {
      Token prompt = null;
      if (match(TEXT)) {
        prompt = previous();
      }
      
      Token name = consume(IDENTIFIER, "Expect variable name after input.");
      consume(SEMICOLON, "Expect ';' after input statement.");
      return new Stmt.Input(prompt, name);
    }

    private Stmt whileStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'while'.");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition.");
      Stmt body = statement();
    
      return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
      Expr expr = expression();
      consume(SEMICOLON, "Expect ';' after expression.");
      return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {
      Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
      consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }

        parameters.add(
            consume(IDENTIFIER, "Expect parameter name."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");
    consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
    }

    private Stmt ifStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'if'.");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after if condition.");
      Stmt thenBranch = statement();
      
      Stmt elseBranch = null;
      if (match(ELSE)) {
        elseBranch = statement();
      }
    
      return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
      List<Stmt> statements = new ArrayList<>();
  
      while (!check(RIGHT_BRACE) && !isAtEnd()) {
        statements.add(declaration());
      }
  
      consume(RIGHT_BRACE, "Expect '}' after block.");
      return statements;
    }

    private Expr assignment() {
      Expr expr = or();
  
      if (match(EQUAL)) {
        Token equals = previous();
        Expr value = assignment();
  
        if (expr instanceof Expr.AssignVariable) {
          Token name = ((Expr.AssignVariable)expr).name;
          return new Expr.Assign(name, value);
        }
  
        error(equals, "Invalid assignment target."); 
      }
  
      return expr;
    }

    private Expr or() {
      Expr expr = and();
  
      while (match(OR)) {
        Token operator = previous();
        Expr right = and();
        expr = new Expr.Logical(expr, operator, right);
      }
  
      return expr;
    }

    private Expr and() {
      Expr expr = equality();
  
      while (match(AND)) {
        Token operator = previous();
        Expr right = equality();
        expr = new Expr.Logical(expr, operator, right);
      }
  
      return expr;
    }

    private Expr equality() {
        Expr expr = comparison();    
        while (match(NEGATE, EQUAL_EQUAL, NOT_EQUAL)) {
          Token operator = previous();
          Expr right = comparison();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
    
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
          Token operator = previous();
          Expr right = term();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
    
        while (match(SUBTRACT, ADD)) {
          Token operator = previous();
          Expr right = factor();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
    
        while (match(SLASH, STAR)) {
          Token operator = previous();
          Expr right = unary();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr unary() {
        if (match(NEGATE, SUBTRACT)) {
          Token operator = previous();
          Expr right = unary();
          return new Expr.Unary(operator, right);
        }
    
        return call();
    } 

    private Expr call() {
      Expr expr = primary();
  
      while (true) { 
        if (match(LEFT_PAREN)) {
          expr = finishCall(expr);
        } else {
          break;
        }
      }
  
      return expr;
    }

    private Expr finishCall(Expr callee) {
      List<Expr> arguments = new ArrayList<>();
      if (!check(RIGHT_PAREN)) {
        do {
          if (arguments.size() >= 255) {
            error(peek(), "Can't have more than 255 arguments.");
          }
          arguments.add(expression());
        } while (match(COMMA));
      }
  
      Token paren = consume(RIGHT_PAREN,
                            "Expect ')' after arguments.");
  
      return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);
    
        if (match(INTEGER, FLOAT, TEXT)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
          return new Expr.AssignVariable(previous());
        }
    
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
          if (check(type)) {
            advance();
            return true;
          }
        }
    
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
    
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
      }
    
    private Token peek() {
      return tokens.get(current);
    }

    private Token previous() {
      return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
      DRS.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
    
        while (!isAtEnd()) {
          if (previous().type == SEMICOLON) return;
    
          switch (peek().type) {
            case PROGRAM:
            case FUNC:
            case ASSIGN:
            case FOR:
            case IF:
            case WHILE:
            case OUTPUT:
            case RETURN:
              return;
          }
    
          advance();
        }
    }
}
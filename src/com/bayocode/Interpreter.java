package src.com.bayocode;

import static src.com.bayocode.TokenType.ADD;
import static src.com.bayocode.TokenType.NOT_EQUAL;
import static src.com.bayocode.TokenType.SUBTRACT;

class Interpreter implements Expr.Visitor<Object> {
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  void interpret(Expr expression) { 
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right); 

    switch (expr.operator.type) {
        case NOT_EQUAL: return !isEqual(left, right);
        case EQUAL_EQUAL: return isEqual(left, right);
        case GREATER:
            checkNumberOperands(expr.operator, left, right);
            return convertToDouble(left) > convertToDouble(right);
        case GREATER_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return convertToDouble(left) >= convertToDouble(right);
        case LESS:
            checkNumberOperands(expr.operator, left, right);
            return convertToDouble(left) < convertToDouble(right);
        case LESS_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return convertToDouble(left) <= convertToDouble(right);
        case SUBTRACT:
            checkNumberOperands(expr.operator, left, right);
            return convertToDouble(left) - convertToDouble(right);
        case ADD:
            // If both are numbers (either INTEGER or FLOAT)
            if ((left instanceof Integer || left instanceof Double) && 
                (right instanceof Integer || right instanceof Double)) {
                return convertToDouble(left) + convertToDouble(right);
            } 

            // If both are strings
            if (left instanceof String && right instanceof String) {
                return (String)left + (String)right;
            }
            throw new RuntimeError(expr.operator,
                "Operands must be two numbers or two strings.");
        case SLASH:
            checkNumberOperands(expr.operator, left, right);
            if (convertToDouble(right) == 0) {
                throw new RuntimeError(expr.operator, "Division by zero.");
            }
            return convertToDouble(left) / convertToDouble(right);
        case STAR:
            checkNumberOperands(expr.operator, left, right);
            return convertToDouble(left) * convertToDouble(right);
    }

    // Unreachable.
    return null;
    }

    private double convertToDouble(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        }
        return (double) obj;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if ((left instanceof Integer || left instanceof Double) && 
            (right instanceof Integer || right instanceof Double)) return;
        
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Integer || operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
      Object right = evaluate(expr.right);
    
      switch (expr.operator.type) {
        case NEGATE:
          return !isTruthy(right);
        case SUBTRACT:
          checkNumberOperand(expr.operator, right);
          if (right instanceof Integer) {
            return -((Integer) right);
          } else {
            return -(double)right;
          }
      }
    
      // Unreachable.
      return null;
    }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }
}

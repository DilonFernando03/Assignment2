package src.com.bayocode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {
  final Environment globals = new Environment();
  private final Map<Expr, Integer> locals = new HashMap<>();
  private Environment environment = globals;
  private final java.util.Scanner inputScanner = new java.util.Scanner(System.in);

  Interpreter() {
    globals.define("clock", new DrsCallable() {
      @Override
      public int arity() { return 0; }

      @Override
      public Object call(Interpreter interpreter,
                         List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() { return "<native fn>"; }
    });
  }
  
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  void executeBlock(List<Stmt> statements,
                    Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;

      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    DrsFunction function = new DrsFunction(stmt, environment);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visitOutputStmt(Stmt.Output stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) value = evaluate(stmt.value);

    throw new Return(value);
  }

  @Override
public Void visitInputStmt(Stmt.Input stmt) {
    if (stmt.prompt != null) {
        System.out.print(stmt.prompt.literal);
    } else {
        System.out.print("> ");
    }
    
    String input;
    try {
        input = inputScanner.nextLine();
    } catch (java.util.NoSuchElementException e) {
        System.out.println("\nEnd of input detected.");
        // Set empty string to trigger end of loop
        input = "";
    }
    
    try {
        // Check if it's an integer
        if (!input.contains(".")) {
            try {
                Integer value = Integer.parseInt(input);
                environment.define(stmt.name.lexeme, value);
                return null;
            } catch (NumberFormatException e) {
                // Not an integer, continue to other checks
            }
        }
        // Check if it's a float
        try {
            Double value = Double.parseDouble(input);
            environment.define(stmt.name.lexeme, value);
            return null;
        } catch (NumberFormatException e) {
            // Not a number, store as string
            environment.define(stmt.name.lexeme, input);
        }
    } catch (Exception e) {
        // Any other exception, store empty string
        environment.define(stmt.name.lexeme, "");
    }
    
    return null;
}
  

  @Override
  public Void visitAssignStmt(Stmt.Assign stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
  while (isTruthy(evaluate(stmt.condition))) {
    execute(stmt.body);
  }
  return null;
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }
    return value;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      DRS.runtimeError(error);
    }
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right); 

    switch (expr.operator.type) {
        case NOT_EQUAL:
              if (left instanceof String || right instanceof String) {
                if (left instanceof Boolean || right instanceof Boolean) {
                    Object converted = attemptTypeConversion(left, right);
                    if (converted != null) {
                        if (left instanceof String) {
                            return !isEqual(converted, right);
                        } else {
                            return !isEqual(left, converted);
                        }
                    }
                }
                else if ((left instanceof Integer || left instanceof Double) || 
                        (right instanceof Integer || right instanceof Double)) {
                    Object converted = attemptTypeConversion(left, right);
                    if (converted != null) {
                        if (left instanceof String) {
                            return !isEqual(converted, right);
                        } else {
                            return !isEqual(left, converted);
                        }
                    }
                }
            }
            
            return !isEqual(left, right);
        case EQUAL_EQUAL: 
            // Try type conversion first
            if (left instanceof String || right instanceof String) {
              if (left instanceof Boolean || right instanceof Boolean) {
                  // Try converting string to boolean
                  Object converted = attemptTypeConversion(left, right);
                  if (converted != null) {
                      if (left instanceof String) {
                          return isEqual(converted, right);
                      } else {
                          return isEqual(left, converted);
                      }
                  }
              }
              else if ((left instanceof Integer || left instanceof Double) || 
                      (right instanceof Integer || right instanceof Double)) {
                  // Try converting string to number
                  Object converted = attemptTypeConversion(left, right);
                  if (converted != null) {
                      if (left instanceof String) {
                          return isEqual(converted, right);
                      } else {
                          return isEqual(left, converted);
                      }
                  }
              }
          }
          
          return isEqual(left, right);
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
            // Handle null values by converting them to string "null"
            if (left == null && right instanceof String) {
                return "null" + (String)right;
            }
            else if (right == null && left instanceof String) {
                return (String)left + "null";
            }
            else if (left == null && right == null) {
                return "nullnull";
            }
            
            // If both are numbers (either INTEGER or FLOAT)
            if ((left instanceof Integer || left instanceof Double) && 
                (right instanceof Integer || right instanceof Double)) {
                return convertToDouble(left) + convertToDouble(right);
            } 
        
            // Convert everything to strings for string concatenation
            if (left instanceof String || right instanceof String) {
              // Convert left to string if it's not already
              String leftStr = (left instanceof String) ? (String)left : stringify(left);
              // Convert right to string if it's not already
              String rightStr = (right instanceof String) ? (String)right : stringify(right);
              
              return leftStr + rightStr;
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

    @Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) { 
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof DrsCallable)) {
      throw new RuntimeError(expr.paren,
          "Can only call functions and classes.");
    }

    DrsCallable function = (DrsCallable)callee;
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }
    return function.call(this, arguments);
  }

    // Attempt to convert a string to a number
    private Object tryStringToNumber(String str) {
      try {
          // Check if it's an integer
          if (!str.contains(".")) {
              return Integer.parseInt(str);
          }
          // Check if it's a float
          return Double.parseDouble(str);
      } catch (NumberFormatException e) {
          return null; // Conversion failed
      }
    }

    // Attempt to convert a string to a boolean
    private Boolean tryStringToBoolean(String str) {
      if (str.equalsIgnoreCase("true")) return true;
      if (str.equalsIgnoreCase("false")) return false;
      return null; // Conversion failed
    }

    // Try to convert strings to match the type of the other operand
    private Object attemptTypeConversion(Object left, Object right) {
      // If one is a string and one is a number
      if (left instanceof String && (right instanceof Integer || right instanceof Double)) {
          Object converted = tryStringToNumber((String)left);
          if (converted != null) return converted;
      }
      else if (right instanceof String && (left instanceof Integer || left instanceof Double)) {
          Object converted = tryStringToNumber((String)right);
          if (converted != null) return converted;
      }
      // If one is a string and one is a boolean
      else if (left instanceof String && right instanceof Boolean) {
          Boolean converted = tryStringToBoolean((String)left);
          if (converted != null) return converted;
      }
      else if (right instanceof String && left instanceof Boolean) {
          Boolean converted = tryStringToBoolean((String)right);
          if (converted != null) return converted;
      }
      
      return null; // No conversion possible
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

    @Override
    public Object visitAssignVariableExpr(Expr.AssignVariable expr) {
      return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
      Integer distance = locals.get(expr);
      if (distance != null) {
        return environment.getAt(distance, name.lexeme);
      } else {
        return globals.get(name);
      }
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
    if (object == null) return "null";

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
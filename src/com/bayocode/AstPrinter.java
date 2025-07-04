package src.com.bayocode;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
      return expr.accept(this);
    }
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                            expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder builder = new StringBuilder();
        
        // Format the call as (call callee arg1 arg2 ...)
        builder.append("(call ");
        builder.append(expr.callee.accept(this));
        
        for (Expr argument : expr.arguments) {
          builder.append(" ");
          builder.append(argument.accept(this));
        }
        
        builder.append(")");
        
        return builder.toString();
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
    
        builder.append("(").append(name);
        for (Expr expr : exprs) {
          builder.append(" ");
          builder.append(expr.accept(this));
        }
        builder.append(")");
    
        return builder.toString();
      }
      @Override
      public String visitAssignVariableExpr(Expr.AssignVariable expr) {
          return expr.name.lexeme;
      }
      
      @Override
      public String visitAssignExpr(Expr.Assign expr) {
          return parenthesize("= " + expr.name.lexeme, expr.value);
      }

      @Override
      public String visitLogicalExpr(Expr.Logical expr) {
          return parenthesize(expr.operator.lexeme, expr.left, expr.right);
      }
}
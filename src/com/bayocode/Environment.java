package src.com.bayocode;

import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Boolean> initialized = new HashMap<>();
  private final Map<String, Object> values = new HashMap<>();
  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }
  
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      if (!initialized.getOrDefault(name.lexeme, false)) {
        throw new RuntimeError(name, "Variable '" + name.lexeme + "' has not been assigned a value.");
      }
      return values.get(name.lexeme);
    }
    if (enclosing != null) return enclosing.get(name);
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
  
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      initialized.put(name.lexeme, true);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) {
    values.put(name, value);
    initialized.put(name, value != null);
  }
}

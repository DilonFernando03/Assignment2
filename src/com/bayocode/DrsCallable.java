package src.com.bayocode;

import java.util.List;

interface DrsCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
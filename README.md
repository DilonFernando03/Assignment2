# DRS LANGUAGE USER GUIDE

## Introduction
DRS (Dilon Runtime Script) is a programming language implemented as an interpreter in Java. This language supports arithmetic operations, boolean logic, text manipulation, variables, control flow, and functions.

## File Extension
DRS scripts use the `.drs` file extension.

### Comments
Single-line comments begin with `//`:
```
// This is a comment
```

### Data Types
DRS supports the following data types:
- Integers: `5`, `42`, `-10`
- Floating-point numbers: `3.14`, `-0.5`
- Boolean values: `true`, `false`
- Text/Strings: `"Hello, world!"`
- Null: `null`

### Arithmetic Operations
```
5 + 3    // Addition
10 - 4   // Subtraction
7 * 2    // Multiplication
9 / 3    // Division
```

### Boolean Operations
```
true and false  // Logical AND
true or false   // Logical OR
!true           // Logical NOT
5 < 10          // Less than
5 <= 5          // Less than or equal to
10 > 5          // Greater than
10 >= 10        // Greater than or equal to
5 == 5          // Equality
5 != 10         // Inequality
```

### Variable Declaration and Assignment
Variables are declared using the `assign` keyword:
```
assign x = 10;
assign name = "John";
assign isActive = true;
```

Variables can be reassigned after declaration:
```
x = x + 5;
name = name + " Doe";
```

### Output
Use the `output` keyword to print to console:
```
output "Hello, world!";
output 2 + 2;
output x;
```

### Input
Use the `input` keyword to get user input:
```
input "Enter your name: " userName;
output "Hello, " + userName + "!";
```

### Control Flow

#### If Statements
```
if (condition) {
    // Statements if condition is true
} else {
    // Statements if condition is false
}
```

#### While Loops
```
assign counter = 1;
while (counter <= 5) {
    output counter;
    counter = counter + 1;
}
```

### Functions
Define functions using the `func` keyword:
```
func add(a, b) {
    return a + b;
}

output add(5, 3); 
```

### Type Conversions
DRS automatically converts between types when appropriate:
- Numbers and strings are concatenated when using the + operator with a string
- Strings that represent numbers can be converted to numbers in arithmetic operations
- Strings that represent booleans can be converted to booleans in logical operations

## Error Handling
DRS provides error messages for common issues such as:
- Syntax errors
- Undefined variables
- Type errors
- Division by zero
- Runtime errors

Error messages include the line number to help with debugging.
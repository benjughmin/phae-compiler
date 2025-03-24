import java.util.*;

// Token class
enum TokenType { 
    WOW, IDENTIFIER, NUMBER, STRING, IF, ELSE, WHILE, PRINT, FOR, IN, RANGE,
    OPERATOR, ASSIGN, SEMICOLON, LEFT_BRACE, RIGHT_BRACE, 
    LEFT_PAREN, RIGHT_PAREN, COMMA, EOF, FUNCTION, RETURN, INPUT
}

class Token {
    TokenType type;
    String value;

    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}

// Lexer
class Lexer {
    private String input;
    private int pos = 0;
    private static final Set<String> keywords = Set.of(
        "WOW", "if", "else", "while", "print", "for", "in", "range", 
        "function", "return", "input"
    );
    Lexer(String input) {
        this.input = input;
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    private void advance() {
        pos++;
    }

    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char current = peek();
            if (Character.isWhitespace(current)) {
                advance();
            } else if (Character.isLetter(current)) {
                StringBuilder ident = new StringBuilder();
                while (Character.isLetterOrDigit(peek())) {
                    ident.append(peek());
                    advance();
                }
                String word = ident.toString();
                TokenType type;
                
                if (keywords.contains(word)) {
                    if (word.equals("input")) {
                        type = TokenType.INPUT;
                    } else {
                        type = TokenType.valueOf(word.toUpperCase());
                    }
                } else {
                    type = TokenType.IDENTIFIER;
                }

                tokens.add(new Token(type, word));
                continue;
            } else if (Character.isDigit(current)) {
                StringBuilder number = new StringBuilder();
                while (Character.isDigit(peek())) {
                    number.append(peek());
                    advance();
                }
                tokens.add(new Token(TokenType.NUMBER, number.toString()));
            } else if (current == '"') {
                advance(); // Skip opening quote
                StringBuilder string = new StringBuilder();
                while (peek() != '"' && peek() != '\0') {
                    string.append(peek());
                    advance();
                }
                if (peek() == '"') {
                    advance(); // Skip closing quote
                    tokens.add(new Token(TokenType.STRING, string.toString()));
                } else {
                    throw new RuntimeException("Unterminated string");
                }
            } else {
                switch (current) {
                    case '=':
                        advance();
                        if (peek() == '=') {
                            tokens.add(new Token(TokenType.OPERATOR, "=="));
                            advance();
                        } else {
                            tokens.add(new Token(TokenType.ASSIGN, "="));
                        }
                        break;
                    case '>':
                        advance();
                        if (peek() == '=') {
                            tokens.add(new Token(TokenType.OPERATOR, ">="));
                            advance();
                        } else {
                            tokens.add(new Token(TokenType.OPERATOR, ">"));
                        }
                        break;
                    case '<':
                        advance();
                        if (peek() == '=') {
                            tokens.add(new Token(TokenType.OPERATOR, "<="));
                            advance();
                        } else {
                            tokens.add(new Token(TokenType.OPERATOR, "<"));
                        }
                        break;
                    case '!':
                        advance();
                        if (peek() == '=') {
                            tokens.add(new Token(TokenType.OPERATOR, "!="));
                            advance();
                        } else {
                            throw new RuntimeException("Unexpected character: !");
                        }
                        break;
                    case '+': case '-': case '*': case '/': case '%':
                        tokens.add(new Token(TokenType.OPERATOR, String.valueOf(current)));
                        advance();
                        break;
                    case ';': tokens.add(new Token(TokenType.SEMICOLON, ";")); advance(); break;
                    case '(': tokens.add(new Token(TokenType.LEFT_PAREN, "(")); advance(); break;
                    case ')': tokens.add(new Token(TokenType.RIGHT_PAREN, ")")); advance(); break;
                    case '{': tokens.add(new Token(TokenType.LEFT_BRACE, "{")); advance(); break;
                    case '}': tokens.add(new Token(TokenType.RIGHT_BRACE, "}")); advance(); break;
                    case ',': tokens.add(new Token(TokenType.COMMA, ",")); advance(); break;
                    default: throw new RuntimeException("Unexpected character: " + current);
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}

// AST Nodes
abstract class ASTNode {}

// Function declaration node
class FunctionDecl extends ASTNode {
    String name;
    List<String> parameters;
    List<ASTNode> body;

    FunctionDecl(String name, List<String> parameters, List<ASTNode> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
}

// Function call node
class FunctionCall extends ASTNode {
    String name;
    List<ASTNode> arguments;

    FunctionCall(String name, List<ASTNode> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
}

// Return statement node
class ReturnStmt extends ASTNode {
    ASTNode value;

    ReturnStmt(ASTNode value) {
        this.value = value;
    }
}

// for loop node
class ForLoop extends ASTNode {
    String variable;     // Loop variable name
    ASTNode iterable;    // What we're iterating over
    List<ASTNode> body;  // Loop body

    ForLoop(String variable, ASTNode iterable, List<ASTNode> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }
}

// while loop node
class WhileLoop extends ASTNode {
    ASTNode condition;
    List<ASTNode> body;

    WhileLoop(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }
}

// range() node
class RangeExpr extends ASTNode {
    ASTNode start;
    ASTNode end;
    ASTNode step;  

    RangeExpr(ASTNode start, ASTNode end, ASTNode step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }
}

// if statement node
class IfStmt extends ASTNode {
    ASTNode condition;
    List<ASTNode> thenBranch;
    List<ASTNode> elseBranch;

    IfStmt(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}

// print statement node
class PrintStmt extends ASTNode {
    ASTNode expression;

    PrintStmt(ASTNode expression) {
        this.expression = expression;
    }
}

// expression node
class Expression extends ASTNode {
    String value;
    TokenType type;

    Expression(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }
}

// binary expression node
class BinaryExpr extends ASTNode {
    ASTNode left;
    Token operator;
    ASTNode right;

    BinaryExpr(ASTNode left, Token operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}

// variable assignment node
class VarAssign extends ASTNode {
    String identifier;
    ASTNode expression;

    VarAssign(String identifier, ASTNode expression) {
        this.identifier = identifier;
        this.expression = expression;
    }
}

// INPUT node
class InputStmt extends ASTNode {
    String identifier;

    InputStmt(String identifier) {
        this.identifier = identifier;
    }
}

// Parser
class Parser {
    private List<Token> tokens;
    private int pos = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<ASTNode> parse() {
        List<ASTNode> nodes = new ArrayList<>();
        while (pos < tokens.size() - 1) {
            if (peek().type == TokenType.IF) {
                nodes.add(parseIfStmt());
            } else if (peek().type == TokenType.PRINT) {
                nodes.add(parsePrintStmt());
            } else if (peek().type == TokenType.INPUT) {
                nodes.add(parseInputStmt());
            } else if (peek().type == TokenType.FOR) {  
                nodes.add(parseForLoop());
            } else if (peek().type == TokenType.WHILE) {  
                nodes.add(parseWhileLoop());
            } else if (peek().type == TokenType.FUNCTION) {
                nodes.add(parseFunctionDecl());
            } else if (peek().type == TokenType.RETURN) {
                nodes.add(parseReturnStmt());
            } else if (peek().type == TokenType.IDENTIFIER) {
                if (peekNext().type == TokenType.ASSIGN) {
                    nodes.add(parseVarAssign());
                } else if (peekNext().type == TokenType.LEFT_PAREN) {
                    nodes.add(parseFunctionCall());
                    consume(TokenType.SEMICOLON);
                } else {
                    throw new RuntimeException("Unexpected token after identifier: " + peekNext().type);
                }
            } else {
                throw new RuntimeException("Unexpected token: " + peek().type);
            }
        }
        return nodes;
    }
    
    // Function declaration parsing
    private ASTNode parseFunctionDecl() {
        consume(TokenType.FUNCTION);
        Token name = consume(TokenType.IDENTIFIER);
        
        consume(TokenType.LEFT_PAREN);
        List<String> parameters = new ArrayList<>();
        
        if (peek().type != TokenType.RIGHT_PAREN) {
            do {
                Token param = consume(TokenType.IDENTIFIER);
                parameters.add(param.value);
                if (peek().type == TokenType.COMMA) {
                    consume(TokenType.COMMA);
                } else {
                    break;
                }
            } while (true);
        }
        
        consume(TokenType.RIGHT_PAREN);
        List<ASTNode> body = parseBlock();
        
        return new FunctionDecl(name.value, parameters, body);
    }
    
    // Function call parsing
    private ASTNode parseFunctionCall() {
        Token name = consume(TokenType.IDENTIFIER);
        consume(TokenType.LEFT_PAREN);
        
        List<ASTNode> arguments = new ArrayList<>();
        if (peek().type != TokenType.RIGHT_PAREN) {
            do {
                arguments.add(parseExpression());
                if (peek().type == TokenType.COMMA) {
                    consume(TokenType.COMMA);
                } else {
                    break;
                }
            } while (true);
        }
        
        consume(TokenType.RIGHT_PAREN);
        return new FunctionCall(name.value, arguments);
    }
    
    // Return statement parsing
    private ASTNode parseReturnStmt() {
        consume(TokenType.RETURN);
        ASTNode value = parseExpression();
        consume(TokenType.SEMICOLON);
        return new ReturnStmt(value);
    }

    // for loop parsing
    private ASTNode parseForLoop() {
        consume(TokenType.FOR);
        
        // Parse the loop variable
        Token identifier = consume(TokenType.IDENTIFIER);
        String variable = identifier.value;
        
        consume(TokenType.IN);
        
        // Parse the iterable (range())
        ASTNode iterable;
        if (peek().type == TokenType.RANGE) {
            iterable = parseRange();
        } else {
            throw new RuntimeException("Only 'range' iterables are supported");
        }
        
        // Parse body
        List<ASTNode> body = parseBlock();
        
        return new ForLoop(variable, iterable, body);
    }
    
    // while loop parsing
    private ASTNode parseWhileLoop() {
        consume(TokenType.WHILE);
        consume(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        consume(TokenType.RIGHT_PAREN);
        List<ASTNode> body = parseBlock();
        return new WhileLoop(condition, body);
    }

    // range() parsing
    private ASTNode parseRange() {
        consume(TokenType.RANGE);
        consume(TokenType.LEFT_PAREN);
        
        ASTNode start = parseExpression();
        
        ASTNode end = null;
        if (peek().type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            end = parseExpression();
        }
        
        ASTNode step = null;
        if (peek().type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            step = parseExpression();
        }
        
        consume(TokenType.RIGHT_PAREN);
        
        // If only one arg, it's the end (with implicit start at 0)
        if (end == null) {
            end = start;
            start = new Expression("0", TokenType.NUMBER);
        }
        
        // Default step is 1
        if (step == null) {
            step = new Expression("1", TokenType.NUMBER);
        }
        
        return new RangeExpr(start, end, step);
    }

    // variable assignment parsing
    private ASTNode parseVarAssign() {
        Token identifier = consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        ASTNode expression = parseExpression();
        consume(TokenType.SEMICOLON);
        return new VarAssign(identifier.value, expression);
    }

    // print statement parsing
    private ASTNode parsePrintStmt() {
        consume(TokenType.PRINT);
        consume(TokenType.LEFT_PAREN);
        ASTNode expression = parseExpression();
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.SEMICOLON);
        return new PrintStmt(expression);
    }

    // if statement parsing
    private ASTNode parseIfStmt() {
        consume(TokenType.IF);
        consume(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        consume(TokenType.RIGHT_PAREN);
        List<ASTNode> thenBranch = parseBlock();
        List<ASTNode> elseBranch = new ArrayList<>();

        if (peek().type == TokenType.ELSE) {
            consume(TokenType.ELSE);
            elseBranch = parseBlock();
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    private ASTNode parseInputStmt() {
        consume(TokenType.INPUT);
        consume(TokenType.LEFT_PAREN);
        Token identifier = consume(TokenType.IDENTIFIER);
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.SEMICOLON);
        return new InputStmt(identifier.value);
    }    

    // block parsing
    private List<ASTNode> parseBlock() {
        consume(TokenType.LEFT_BRACE);
        List<ASTNode> statements = new ArrayList<>();
        
        while (peek().type != TokenType.RIGHT_BRACE) {
            if (peek().type == TokenType.IF) {
                statements.add(parseIfStmt());
            } else if (peek().type == TokenType.PRINT) {
                statements.add(parsePrintStmt());
            } else if (peek().type == TokenType.FOR) {
                statements.add(parseForLoop());
            } else if (peek().type == TokenType.RETURN) {
                statements.add(parseReturnStmt());
            } else if (peek().type == TokenType.INPUT) {
                statements.add(parseInputStmt());
            } else if (peek().type == TokenType.IDENTIFIER) {
                if (peekNext().type == TokenType.ASSIGN) {
                    statements.add(parseVarAssign());
                } else if (peekNext().type == TokenType.LEFT_PAREN) {
                    statements.add(parseFunctionCall());
                    consume(TokenType.SEMICOLON);
                } else {
                    throw new RuntimeException("Unexpected token after identifier in block: " + peekNext().type);
                }
            } else {
                throw new RuntimeException("Unexpected token in block: " + peek().type);
            }
        }
        
        consume(TokenType.RIGHT_BRACE);
        return statements;
    }

    private ASTNode parseExpression() {
        ASTNode left = parsePrimary();
        
        while (peek().type == TokenType.OPERATOR) {
            Token operator = consume(TokenType.OPERATOR);
            ASTNode right = parsePrimary();
            left = new BinaryExpr(left, operator, right);
        }
        
        return left;
    }

    private ASTNode parsePrimary() {
        Token token = peek();
        
        if (token.type == TokenType.NUMBER) {
            advance();
            return new Expression(token.value, TokenType.NUMBER);
        } else if (token.type == TokenType.STRING) {
            advance();
            return new Expression(token.value, TokenType.STRING);
        } else if (token.type == TokenType.IDENTIFIER) {
            advance();
            if (peek().type == TokenType.LEFT_PAREN) {
                // This is a function call in an expression
                pos--; // Go back to the identifier
                return parseFunctionCall();
            }
            return new Expression(token.value, TokenType.IDENTIFIER);
        } else if (token.type == TokenType.LEFT_PAREN) {
            advance();
            ASTNode expr = parseExpression();
            consume(TokenType.RIGHT_PAREN);
            return expr;
        } else {
            throw new RuntimeException("Unexpected token: " + token.type);
        }
    }

    private Token consume(TokenType expected) {
        Token token = tokens.get(pos);
        if (token.type != expected) {
            throw new RuntimeException("Expected " + expected + " but found " + token.type);
        }
        pos++;
        return token;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token peekNext() {
        return pos + 1 < tokens.size() ? tokens.get(pos + 1) : tokens.get(pos);
    }

    private void advance() {
        pos++;
    }
}

// Function data
class Function {
    String name;
    List<String> parameters;
    List<ASTNode> body;
    
    Function(String name, List<String> parameters, List<ASTNode> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
}

// Return value signal
class ReturnValue {
    Object value;
    
    ReturnValue(Object value) {
        this.value = value;
    }
}

// Interpreter
class Interpreter {
    private Map<String, Object> globalVariables = new HashMap<>();
    private Map<String, Function> functions = new HashMap<>();
    private boolean returnSignal = false;
    private Object returnValue = null;
    
    void interpret(List<ASTNode> nodes) {
        for (ASTNode node : nodes) {
            execute(node, globalVariables);
            if (returnSignal) {
                break;
            }
        }
    }
    
    private Object execute(ASTNode node, Map<String, Object> variables) {
        if (returnSignal) {
            return null;
        }
        
        if (node instanceof PrintStmt printStmt) {
            Object value = evaluate(printStmt.expression, variables);
            System.out.println(value);
        } else if (node instanceof IfStmt ifStmt) {
            boolean condition = isTrue(evaluate(ifStmt.condition, variables));
            if (condition) {
                for (ASTNode stmt : ifStmt.thenBranch) {
                    execute(stmt, variables);
                    if (returnSignal) break;
                }
            } else if (!ifStmt.elseBranch.isEmpty()) {
                for (ASTNode stmt : ifStmt.elseBranch) {
                    execute(stmt, variables);
                    if (returnSignal) break;
                }
            }
        } else if (node instanceof ForLoop forLoop) {
            if (forLoop.iterable instanceof RangeExpr rangeExpr) {
                int start = (int) evaluate(rangeExpr.start, variables);
                int end = (int) evaluate(rangeExpr.end, variables);
                int step = (int) evaluate(rangeExpr.step, variables);
                
                for (int i = start; (step > 0) ? i < end : i > end; i += step) {
                    variables.put(forLoop.variable, i);
                    
                    for (ASTNode stmt : forLoop.body) {
                        execute(stmt, variables);
                        if (returnSignal) return null;
                    }
                }
            }
        } else if (node instanceof WhileLoop whileLoop) {
            while (isTrue(evaluate(whileLoop.condition, variables))) {
                for (ASTNode stmt : whileLoop.body) {
                    execute(stmt, variables);
                    if (returnSignal) return null; // Allow early exits
                }
            }
        } else if (node instanceof VarAssign varAssign) {
            Object value = evaluate(varAssign.expression, variables);
            variables.put(varAssign.identifier, value);
        } else if (node instanceof FunctionDecl funcDecl) {
            // Store function for later use
            functions.put(funcDecl.name, new Function(funcDecl.name, funcDecl.parameters, funcDecl.body));
        } else if (node instanceof FunctionCall funcCall) {
            return callFunction(funcCall.name, funcCall.arguments, variables);
        } else if (node instanceof ReturnStmt returnStmt) {
            returnValue = evaluate(returnStmt.value, variables);
            returnSignal = true;
            return returnValue;
        } else if (node instanceof InputStmt inputStmt) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter value for " + inputStmt.identifier + ": ");
            String input = scanner.nextLine();
        
            // Try to parse input as a number; otherwise, store as a string.
            try {
                variables.put(inputStmt.identifier, Integer.parseInt(input));
            } catch (NumberFormatException e) {
                variables.put(inputStmt.identifier, input);
            }
        }
        
        
        return null;
    }
    
    private Object callFunction(String name, List<ASTNode> arguments, Map<String, Object> currentScope) {
        if (!functions.containsKey(name)) {
            throw new RuntimeException("Undefined function: " + name);
        }
        
        Function function = functions.get(name);
        
        // Create a new scope for the function
        Map<String, Object> functionScope = new HashMap<>();
        
        // Evaluate and bind arguments to parameters
        if (arguments.size() != function.parameters.size()) {
            throw new RuntimeException("Expected " + function.parameters.size() + 
                                      " arguments but got " + arguments.size());
        }
        
        for (int i = 0; i < arguments.size(); i++) {
            Object argValue = evaluate(arguments.get(i), currentScope);
            functionScope.put(function.parameters.get(i), argValue);
        }
        
        // Reset return signal
        returnSignal = false;
        returnValue = null;
        
        // Execute function body
        for (ASTNode stmt : function.body) {
            execute(stmt, functionScope);
            if (returnSignal) {
                // Reset return signal for next function call
                returnSignal = false;
                return returnValue;
            }
        }
        
        // Default return value if no return statement
        return null;
    }
    
    private Object evaluate(ASTNode node, Map<String, Object> variables) {
        if (node instanceof Expression expr) {
            if (expr.type == TokenType.NUMBER) {
                return Integer.parseInt(expr.value);
            } else if (expr.type == TokenType.STRING) {
                return expr.value;
            } else if (expr.type == TokenType.IDENTIFIER) {
                if (!variables.containsKey(expr.value)) {
                    throw new RuntimeException("Undefined variable: " + expr.value);
                }
                return variables.get(expr.value);
            }
        } else if (node instanceof BinaryExpr binExpr) {
            Object left = evaluate(binExpr.left, variables);
            Object right = evaluate(binExpr.right, variables);
            
            switch (binExpr.operator.value) {
                case "+":
                    if (left instanceof Integer && right instanceof Integer) {
                        return (Integer)left + (Integer)right;
                    } else {
                        return left.toString() + right.toString();
                    }
                case "-":
                    checkNumbers(left, right);
                    return (Integer)left - (Integer)right;
                case "*":
                    checkNumbers(left, right);
                    return (Integer)left * (Integer)right;
                case "/":
                    checkNumbers(left, right);
                    if ((Integer)right == 0) {
                        throw new RuntimeException("Division by zero");
                    }
                    return (Integer)left / (Integer)right;
                case "%":
                    checkNumbers(left, right);
                    return (Integer)left % (Integer)right;
                case "==":
                    return isEqual(left, right);
                case "!=":
                    return !isEqual(left, right);
                case ">":
                    checkNumbers(left, right);
                    return (Integer)left > (Integer)right;
                case ">=":
                    checkNumbers(left, right);
                    return (Integer)left >= (Integer)right;
                case "<":
                    checkNumbers(left, right);
                    return (Integer)left < (Integer)right;
                case "<=":
                    checkNumbers(left, right);
                    return (Integer)left <= (Integer)right;
                default:
                    throw new RuntimeException("Unknown operator: " + binExpr.operator.value);
            }
        } else if (node instanceof FunctionCall funcCall) {
            return callFunction(funcCall.name, funcCall.arguments, variables);
        }
        
        throw new RuntimeException("Could not evaluate node: " + node.getClass().getSimpleName());
    }
    
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    private boolean isTrue(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean)obj;
        } else if (obj instanceof Integer) {
            return (Integer)obj != 0;
        } else if (obj instanceof String) {
            return !((String)obj).isEmpty();
        }
        return false;
    }
    
    private void checkNumbers(Object left, Object right) {
        if (!(left instanceof Integer && right instanceof Integer)) {
            throw new RuntimeException("Numeric operators only work on numbers");
        }
    }
}

// Compiler Runner
public class PhaeCompiler {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your code (Type 'END' to finish):");

        StringBuilder codeBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("END")) break;
            codeBuilder.append(line).append("\n");
        }

        try {
            String sourceCode = codeBuilder.toString();
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.tokenize();
            
            Parser parser = new Parser(tokens);
            List<ASTNode> ast = parser.parse();
            
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(ast);
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
import java.util.*;

// Token class
enum TokenType { VAR, IDENTIFIER, NUMBER, IF, ELSE, WHILE, PRINT, OPERATOR, ASSIGN, SEMICOLON, LEFT_BRACE, RIGHT_BRACE, LEFT_PAREN, RIGHT_PAREN, COMMA, EOF }

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

// Lexer: Converts input code into tokens
class Lexer {
    private String input;
    private int pos = 0;
    private static final Set<String> keywords = Set.of("var", "if", "else", "while", "print");

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
                TokenType type = keywords.contains(word) ? TokenType.valueOf(word.toUpperCase()) : TokenType.IDENTIFIER;
                tokens.add(new Token(type, word));
            } else if (Character.isDigit(current)) {
                StringBuilder number = new StringBuilder();
                while (Character.isDigit(peek())) {
                    number.append(peek());
                    advance();
                }
                tokens.add(new Token(TokenType.NUMBER, number.toString()));
            } else {
                switch (current) {
                    case '=': tokens.add(new Token(TokenType.ASSIGN, "=")); break;
                    case '+': case '-': case '*': case '/': case '%': 
                        tokens.add(new Token(TokenType.OPERATOR, String.valueOf(current))); break;
                    case ';': tokens.add(new Token(TokenType.SEMICOLON, ";")); break;
                    case '(': tokens.add(new Token(TokenType.LEFT_PAREN, "(")); break;
                    case ')': tokens.add(new Token(TokenType.RIGHT_PAREN, ")")); break;
                    case '{': tokens.add(new Token(TokenType.LEFT_BRACE, "{")); break;
                    case '}': tokens.add(new Token(TokenType.RIGHT_BRACE, "}")); break;
                    case ',': tokens.add(new Token(TokenType.COMMA, ",")); break;
                    default: throw new RuntimeException("Unexpected character: " + current);
                }
                advance();
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}

// AST Nodes
abstract class ASTNode {}
class VarDecl extends ASTNode {
    String name;
    ASTNode expr;
    VarDecl(String name, ASTNode expr) { this.name = name; this.expr = expr; }
}
class PrintStmt extends ASTNode {
    List<ASTNode> args;
    PrintStmt(List<ASTNode> args) { this.args = args; }
}
class Expression extends ASTNode {
    String value;
    Expression(String value) { this.value = value; }
}
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

// Parser
class Parser {
    private List<Token> tokens;
    private int pos = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token consume(TokenType expected) {
        Token token = tokens.get(pos);
        if (token.type != expected) {
            throw new RuntimeException("Expected " + expected + " but found " + token.type);
        }
        pos++;
        return token;
    }

    List<ASTNode> parse() {
        List<ASTNode> nodes = new ArrayList<>();
        while (pos < tokens.size() - 1) {
            if (peek().type == TokenType.VAR) {
                nodes.add(parseVarDecl());
            } else if (peek().type == TokenType.PRINT) {
                nodes.add(parsePrintStmt());
            } else {
                throw new RuntimeException("Unexpected token: " + peek().type);
            }
        }
        return nodes;
    }

    private ASTNode parseVarDecl() {
        consume(TokenType.VAR);
        Token ident = consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        ASTNode expr = parseExpression();
        consume(TokenType.SEMICOLON);
        return new VarDecl(ident.value, expr);
    }

    private ASTNode parsePrintStmt() {
        consume(TokenType.PRINT);
        consume(TokenType.LEFT_PAREN);
        List<ASTNode> args = new ArrayList<>();
        while (peek().type != TokenType.RIGHT_PAREN) {
            args.add(parseExpression());
            if (peek().type == TokenType.COMMA) {
                consume(TokenType.COMMA);
            }
        }
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.SEMICOLON);
        return new PrintStmt(args);
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
            consume(TokenType.NUMBER);
            return new Expression(token.value);
        } else if (token.type == TokenType.IDENTIFIER) {
            consume(TokenType.IDENTIFIER);
            return new Expression(token.value);
        } else {
            throw new RuntimeException("Unexpected token: " + token.type);
        }
    }

    private Token peek() {
        return tokens.get(pos);
    }
}

// Code Generator (Generates Python code)
class CodeGenerator {
    String generate(List<ASTNode> nodes) {
        StringBuilder output = new StringBuilder();
        for (ASTNode node : nodes) {
            if (node instanceof VarDecl varDecl) {
                output.append(varDecl.name).append(" = ").append(((Expression) varDecl.expr).value).append("\n");
            } else if (node instanceof PrintStmt printStmt) {
                output.append("print(");
                for (int i = 0; i < printStmt.args.size(); i++) {
                    output.append(((Expression) printStmt.args.get(i)).value);
                    if (i < printStmt.args.size() - 1) {
                        output.append(", ");
                    }
                }
                output.append(")\n");
            }
        }
        return output.toString();
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
        scanner.close();

        String code = codeBuilder.toString();
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        System.out.println("Tokens: " + tokens);

        Parser parser = new Parser(tokens);
        List<ASTNode> ast = parser.parse();

        CodeGenerator generator = new CodeGenerator();
        String pythonCode = generator.generate(ast);

        System.out.println("Generated Python Code:\n" + pythonCode);
    }
}
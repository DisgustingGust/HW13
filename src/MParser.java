import org.codehaus.jparsec.*;

public class MParser {

    static final Parser<String> IDENTIFIER = Terminals.Identifier.PARSER.label("IDENTIFER");


    private static final Terminals TERMS = Terminals
            .operators("+", "-", "*", "/", "%", "==", "!=", ">", ">=", "<", "<=", "&&", "||", "**", "(", ")", ":=", ";")
            .words(Scanners.IDENTIFIER)
            .keywords("fi", "od", "skip", "write", "read", "while", "do", "if", "then", "else")
            .build();


    public final static Parser<String> NUMBER = Terminals.IntegerLiteral.PARSER.label("NUMBER");

    public final static Parser<?> IGNORED = Parsers.or(Scanners.JAVA_BLOCK_COMMENT, Scanners.JAVA_LINE_COMMENT, Scanners.WHITESPACES);

    public static Parser<?> TOKENIZER = Parsers.or(TERMS.tokenizer(), Terminals.IntegerLiteral.TOKENIZER, Terminals.Identifier.TOKENIZER);

    public static Parser<?> term(String name) {
        return TERMS.token(name);
    }

    static Parser<String> SKIP = term("skip").label("SKIP").cast();

    static Parser<String> EXPRESSION = Parsers.or(NUMBER, IDENTIFIER).label("EXPRESSION");

    static Parser<String> READ = Parsers.sequence(term("read").cast(), IDENTIFIER).label("READ");

    static Parser<String> WRITE = Parsers.sequence(term("write").cast(), EXPRESSION).label("WRITE");

    static Parser<String> ASSIGNMENT = Parsers.sequence(IDENTIFIER, term(":=").cast(), EXPRESSION).label("ASSIGNMENT");

    static Parser.Reference<String> ref = Parser.newReference();

    static Parser<String> STATEMENT = Parsers
            .or(    SKIP,
                    READ,
                    WRITE,
                    ASSIGNMENT)
            .label("STATEMENT")
            .from(TOKENIZER, IGNORED.skipMany());

    static Parser<String> COLON = Parsers.sequence(STATEMENT, term(";").cast(), STATEMENT).label(";");

    static Parser<String> parse(Parser<String> atom) {
        Parser.Reference<String> ref = Parser.newReference();
        Parser<String> unit = ref.lazy().between(term("("), term(")")).or(atom);
        Parser<String> parser = new OperatorTable<String>()
                .build(unit);
        ref.set(parser);
        return parser;
    }
}
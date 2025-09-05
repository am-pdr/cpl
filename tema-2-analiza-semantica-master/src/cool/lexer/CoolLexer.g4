lexer grammar CoolLexer;

@header{
    package cool.lexer;
}

tokens { ERROR }

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }

    private void processMyString() {
        String str = getText();

        str = str.substring(1, str.length() - 1);

        str = str.replace("\\n", "\n");
        str = str.replace("\\r", "\r");
        str = str.replace("\\t", "\t");
        str = str.replace("\\f", "\f");
        str = str.replace("\\\"", "\"");
        str = str.replace("\\\\", "\\");

        if (str.length() > 1024) {
            raiseError("String constant too long");
        } else if (str.contains("\0")) {
            raiseError("String contains null character");
        } else {
            setText(str);
        }
    }
}
// keywords
CLASS: 'class'; // clAsS CLASS CLAss c|C l|L a|A s|S
INHERITS: 'inherits';
LET: 'let';
IN: 'in';
NEW: 'new';
ISVOID: 'isvoid';
NOT: 'not';
NEG: '~';

IF: 'if';
THEN: 'then';
ELSE: 'else';
FI: 'fi';
WHILE: 'while';
LOOP: 'loop';
POOL: 'pool';
CASE: 'case';
OF: 'of';
ESAC: 'esac';

BOOL : 'true' | 'false';

// symbols
DOT: '.';
COLON: ':';
SEMI : ';';
COMMA : ',';
ASSIGN : '<-';
RESULTS: '=>';
AT: '@';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
EQUAL : '=';
LT : '<';
LE : '<=';

fragment NEW_LINE : '\r'? '\n';
fragment SELF: 'self';
fragment SELF_TYPE: 'self_type';
fragment DIGIT: [0-9];
fragment DIGITS : DIGIT+;
fragment FRACTION : ('.' DIGITS?)?;
fragment EXPONENT : 'e' ('+' | '-')? DIGITS;
fragment LETTER : [a-zA-Z];
fragment ESCAPED_CHAR: '\\' [btnrf"\\];

TYPE: ([A-Z] (LETTER | '_' | DIGIT)*);
ID: ((LETTER | '_')(LETTER | '_' | DIGIT)* | SELF | SELF_TYPE);
INT: DIGIT+;
FLOAT: (DIGITS ('.' DIGITS?)? | '.' DIGITS) EXPONENT?;
STRING
    : '"' ( ESCAPED_CHAR
           | '\\' NEW_LINE
           | ~('\r' | '\n' | '"')
          )* '"' {
              processMyString();
          }
    | '"' ( ESCAPED_CHAR
           | '\\' NEW_LINE
           | ~('\r' | '\n' | '"')
          )* NEW_LINE {
              raiseError("Unterminated string constant");
          }
    | '"' ( ESCAPED_CHAR
           | '\\' NEW_LINE
           | ~('\r' | '\n' | '"')
          )* EOF {
              raiseError("EOF in string constant");
          }
    ;

LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

UNMATCHED_BLOCK_COMMENT: '*)' { raiseError("Unmatched *)"); };

BLOCK_COMMENT
    : '(*'
      (BLOCK_COMMENT | .)*?
      ('*)'{skip();} | EOF { raiseError("EOF in comment"); })
    ;

WS
    :   [ \n\f\r\t]+ -> skip
    ; 

ERROR_CHAR
    : . {
        raiseError("Invalid character: " + getText());
    }
    ;
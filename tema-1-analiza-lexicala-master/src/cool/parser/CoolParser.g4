parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program
    :   class*
    ;

class
    :
    CLASS type=TYPE (INHERITS inherited=TYPE)? LBRACE (feature SEMI)* RBRACE SEMI
    ;

formal
    :
    name=ID COLON type=TYPE
    ;

local
    :
    name=ID COLON type=TYPE (ASSIGN init=expr)?
    ;

feature
    :
    name=ID COLON type=TYPE (ASSIGN init=expr)?
    // local                                                                               // var
    | name=ID LPAREN (arg=formal (COMMA formal)*)? RPAREN COLON returnType=TYPE body=block   // func
    ;

block
    :
    LBRACE ((local | expr) SEMI)* expr? RBRACE
    ;

expr
    :
    name=ID ASSIGN init=expr                                                                   //# assignVal
    | target=expr (AT type=TYPE)? DOT id=ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN           //# staticDispatch
    | name=ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN                                         //# dispatch//
    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                                       //# if
    | WHILE cond=expr LOOP whileBranch=expr POOL                                                      //# while
    | block                                                                                             //# block
    | LET localVars+=local (COMMA localVars+=local)* IN exp=expr                                      //# let
    | CASE cond=expr OF (types+=formal RESULTS exprs+=expr SEMI)+ ESAC                                //# case
    | NEW type=TYPE                                                                                   //# new
    | ISVOID e=expr                                                                                   //# isVoid
    | left=expr (DIV | MULT) right=expr
    | left=expr (MINUS | PLUS) right=expr
    | NEG e=expr                                                                                      //# neg
    | left=expr (LT | LE | EQUAL) right=expr
    | NOT e=expr                                                                                      //# not
    | LPAREN e=expr RPAREN                                                                            //# parenthesis
    | ID                                                                                              //# id
    | INT                                                                                             //# int
    | STRING                                                                                          //# str
    | BOOL                                                                                              //# boolVal
    ;




package cool.structures;

import java.io.File;

import org.antlr.v4.runtime.*;
import cool.parser.*;

import cool.compiler.Compiler;

public class SymbolTable {
    public static Scope globals;
    
    private static boolean semanticErrors;
    
    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;
        
        // TODO Populate global scope.
        // Classes
        ClassSymbol objectClass = new ClassSymbol("Object", null);
        ClassSymbol ioClass = new ClassSymbol("IO", "Object");
        ClassSymbol intClass = new ClassSymbol("Int", "Object");
        ClassSymbol stringClass = new ClassSymbol("String", "Object");
        ClassSymbol boolClass = new ClassSymbol("Bool", "Object");

        globals.add(objectClass);
        globals.add(ioClass);
        globals.add(intClass);
        globals.add(stringClass);
        globals.add(boolClass);

        // Methods
        // Object methods
        var abortMethod = new MethodSymbol("abort", objectClass, "Object");
        var typeNameMethod = new MethodSymbol("type_name", objectClass, "String");
        var copyMethod = new MethodSymbol("copy", objectClass, "Object"); // TODO to chamge to SELF_TYPE

        objectClass.add(abortMethod);
        objectClass.add(typeNameMethod);
        objectClass.add(copyMethod);

        // IO methods
        var outStringMethod = new MethodSymbol("out_string", ioClass, "IO");
        var outIntMethod = new MethodSymbol("out_int", ioClass, "IO");
        var inStringMethod = new MethodSymbol("in_string", ioClass, "String");
        var inIntMethod = new MethodSymbol("in_int", ioClass, "Int");

        // parameters in methods
        outStringMethod.add(new IdSymbol("x", stringClass));
        outIntMethod.add(new IdSymbol("x", stringClass));

        ioClass.add(outStringMethod);
        ioClass.add(outIntMethod);
        ioClass.add(inStringMethod);
        ioClass.add(inIntMethod);

        // String methods
        var lengthMethod = new MethodSymbol("length", stringClass, "Int");
        var concatMethod = new MethodSymbol("concat", stringClass, "String");
        var substrMethod = new MethodSymbol("substr", stringClass, "String");

        // parameters in methods
        concatMethod.add(new IdSymbol("s", stringClass));
        substrMethod.add(new IdSymbol("i", intClass));
        substrMethod.add(new IdSymbol("l", intClass));

        stringClass.add(lengthMethod);
        stringClass.add(concatMethod);
        stringClass.add(substrMethod);

    }
    
    /**
     * Displays a semantic error message.
     * 
     * @param ctx Used to determine the enclosing class context of this error,
     *            which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (! (ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        
        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}

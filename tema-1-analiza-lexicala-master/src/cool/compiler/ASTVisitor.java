package cool.compiler;

public interface ASTVisitor<T> {
    // Literali & identificatori
    T visit(Id id);
    T visit(Int integer);
    T visit(Str str);
    T visit(Bool bool);

    T visit(Formal formal);
    T visit(Feature feature);
    T visit(Class classs);
    T visit(Program program);
    T visit(Local local);
    T visit(Method method);
    T visit(Attr attr);

    // Expresii
    T visit(Block block);
    T visit(If ifExpr);
    T visit(While whileExpr);
    T visit(Let letExpr);
    T visit(Case caseExpr);
    T visit(CaseBranch branch);

    T visit(Assign assign);
    T visit(Dispatch dispatch);            // implicit dispatch
    T visit(StaticDispatch staticDispatch); // static dispatch

    // Operații
    T visit(BinaryOp op);     // +, -, *, /, <, <=, =
    T visit(UnaryMinus op);   // -e
    T visit(Not notExpr);
    T visit(IsVoid isVoidExpr);

    T visit(New newExpr);
    T visit(Paren paren);
    T visit(Neg negExpr);
}

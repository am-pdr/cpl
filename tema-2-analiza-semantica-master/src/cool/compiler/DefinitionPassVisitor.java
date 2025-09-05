package cool.compiler;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    private Scope currentScope = SymbolTable.globals;
    private ClassSymbol currentClass;
    private MethodSymbol currentMethod;

    @Override
    public Void visit(Program program) {
        // collect the names of classes
        for (var classs : program.classes) {
            var name = classs.type.getText();
            if ("SELF_TYPE".equals(name)) {
                SymbolTable.error(classs.ctx, classs.type, "Class has illegal name SELF_TYPE");
                continue;
            }
            if (SymbolTable.globals.lookup(name) != null) {
                SymbolTable.error(classs.ctx, classs.type,
                        String.format("Class %s is redefined", name));
                continue;
            }
            var classSymbol = new ClassSymbol(name, null); // parent is null for each class
            SymbolTable.globals.add(classSymbol);   // adding the class in globals
            classs.sym = classSymbol;
        }

        // collect features from created classes
        for (var cls : p.classes)
            classs.accept(this);
        return null;
    }

    @Override
    public Void visit(Id id) {

        return null;
    }

    @Override
    public Void visit(Int integer) {

        return null;
    }

    @Override
    public Void visit(Str str) {

        return null;
    }

    @Override
    public Void visit(Bool bool) {

        return null;
    }

    @Override
    public Void visit(Formal formal) {

        return null;
    }

    @Override
    public Void visit(Feature feature) {
        return null;
    }

    @Override
    public Void visit(Class classs) {
        var classSymbol = (ClassSymbol) SymbolTable.globals.lookup(classs.type.getText());
        var old = currentScope;
        currentScope = classSymbol;

        for (var feature : classs.features) feature.accept(this);

        currentScope = old;
        return null;
    }


    @Override
    public Void visit(Local local) {

        return null;
    }

    @Override
    public Void visit(Method method) {
        var classs = (ClassSymbol) currentScope;
        if (classs.getMethod(method.name.getText()) != null) {
            SymbolTable.error(method.ctx, method.name,
                    String.format("Class %s redefines method %s",
                            classs.getName(), method.name.getText()));
            return null;
        }

        var methodSymbol = new MethodSymbol(method.name.getText(), method.returnType.getText(), classs);
        classs.addMethod(methodSymbol);

        var old = currentScope;
        currentScope = methodSymbol;

        for (var formal : method.formals) {
            if ("self".equals(formal.name.getText())) {
                SymbolTable.error(formal.ctx, formal.name,
                        String.format("Method %s of class %s has formal parameter with illegal name self",
                                methodSymbol.getName(), classs.getName()));
                continue;
            }
            if ("SELF_TYPE".equals(f.type.getText())) {
                SymbolTable.error(f.ctx, f.type,
                        String.format("Method %s of class %s has formal parameter %s with illegal type SELF_TYPE",
                                ms.getName(), cls.getName(), f.name.getText()));
                continue;
            }
            if (!ms.add(new IdSymbol(f.name.getText(), f.type.getText()))) {
                SymbolTable.error(f.ctx, f.name,
                        String.format("Method %s of class %s redefines formal parameter %s",
                                ms.getName(), cls.getName(), f.name.getText()));
            }
        }
        currentScope = old;
        return null;
    }

    @Override
    public Void visit(Attr attr) {

        return null;
    }

    @Override
    public Void visit(Block block) {

        return null;
    }

    @Override
    public Void visit(If ifExpr) {

        return null;
    }

    @Override
    public Void visit(While whileExpr) {

        return null;
    }

    @Override
    public Void visit(Let letExpr) {

        return null;
    }

    @Override
    public Void visit(Case caseExpr) {

        return null;
    }

    @Override
    public Void visit(CaseBranch branch) {

        return null;
    }

    @Override
    public Void visit(Assign assign) {

        return null;
    }

    @Override
    public Void visit(Dispatch dispatch) {

        return null;
    }

    @Override
    public Void visit(StaticDispatch staticDispatch) {

        return null;
    }

    @Override
    public Void visit(BinaryOp op) {

        return null;
    }

    @Override
    public Void visit(UnaryMinus op) { //~

        return null;
    }

    @Override
    public Void visit(Not notExpr) {

        return null;
    }

    @Override
    public Void visit(IsVoid isVoidExpr) {

        return null;
    }

    @Override
    public Void visit(New newExpr) {

        return null;
    }

    @Override
    public Void visit(Paren paren) {

        return null;
    }

    @Override
    public Void visit(Neg negExpr) {

        return null;
    }
}

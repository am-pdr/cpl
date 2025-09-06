package cool.compiler;

import cool.structures.*;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    private Scope currentScope = SymbolTable.globals;
    RulesChecker validateChecks = new RulesChecker();
    private ClassSymbol currentClass;
    private MethodSymbol currentMethod;

    @Override
    public Void visit(Program program) {
        program.classes.forEach(cls -> cls.accept(this));
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
        IdSymbol sym = new IdSymbol(formal.id.token.getText());
        if(!validateChecks.checkFormalDefinition(formal, currentScope))
            return null;

        sym.setType(new ClassSymbol(formal.type.getText(), ""));
        sym.setScope(currentScope);
        currentScope.add(sym);
        formal.id.accept(this);

        return null;
    }

    @Override
    public Void visit(Feature feature) {
        return null;
    }

    @Override
    public Void visit(Class classs) {
        String name = classs.type.getText();
        String parent = "Object";
        ClassSymbol classSymbol = new ClassSymbol(name, parent);

        // illegal name SELF_TYPE & redefinition
        if (!validateChecks.checkClassName(classs, currentScope))
            return null;

        // illegal parent
        if (classs.inherit != null) {
            validateChecks.checkParentName(classs);
            classSymbol.setParentName(classs.inherit.getText());
        }

        currentScope.add(classSymbol);
        currentScope = (Scope) classSymbol;

        for (var f : classs.features)
            f.accept(this);   // atât! fără verificări specifice atributelor aici

        currentScope = SymbolTable.globals;
        return null;
    }


    @Override
    public Void visit(Local local) {

        return null;
    }

    @Override
    public Void visit(Method method) {
        MethodSymbol sym = new MethodSymbol(method.id.token.getText(), currentScope);
        var cls = (ClassSymbol) currentScope;
        var mName = method.id.token.getText();
        // redefinition in the same class
        if (!validateChecks.checkMethodDefinition(method, currentScope)) {
            return null;
        }

        var mSym = new MethodSymbol(mName, cls, method.returnType.getText());
        cls.add(mSym);
        method.id.setSymbol(mSym);

        // 3) intrăm în scope-ul metodei și definim formalii (numai numele)
        var old = currentScope;
        currentScope = mSym;

        for (var f : method.formals) {
            f.accept(this);  // definim parametrii (doar numele și interdicțiile de nume)
        }

        // 4) corpul metodei rămâne pentru type-check, nu ai erori în testul 3 aici
        currentScope = old;

        return null;
    }

    @Override
    public Void visit(Attr attr) {
        var name = attr.id.getToken().getText();

        // illegal name self
        if ("self".equals(name)) {
            SymbolTable.error(attr.ctx, attr.id.getToken(),
                    "Class " + ((ClassSymbol) currentScope).getName() + " has attribute with illegal name self");
            return null;
        }

        // redefinition
        if (currentScope.lookup(name) instanceof IdSymbol) {
            SymbolTable.error(attr.ctx, attr.id.getToken(),
                    "Class " + ((ClassSymbol) currentScope).getName() + " redefines attribute " + name);
            return null;
        }

        // redefinition of inherited attr


        // define symbol and add in scope
        var sym = new IdSymbol(name);           // type will be set in resolution
        sym.setScope(currentScope);
        sym.setScope(currentScope);
        attr.id.setSymbol(sym);
        currentScope.add(sym);

        // initialization
        if (attr.init != null) attr.init.accept(this);
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

package cool.compiler;

import cool.structures.*;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    private Scope currentScope = SymbolTable.globals;
    RulesChecker validateChecks = new RulesChecker();

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

        sym.setScope(currentScope);
        currentScope.add(sym);
        formal.id.setSymbol(sym);

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

        // check illegal parent
        if (classs.inherit != null) {
            validateChecks.checkParentName(classs);
            classSymbol.setParentName(classs.inherit.getText());
        }

        currentScope.add(classSymbol);
        currentScope = (Scope) classSymbol;

        for (var f : classs.features)
            f.accept(this);

        currentScope = SymbolTable.globals;
        return null;
    }


    @Override
    public Void visit(Local local) {
        // illegal name
        if ("self".equals(local.id.getToken().getText())) {
            SymbolTable.error(local.ctx, local.id.getToken(),
                    "Let variable has illegal name self");
            return null;
        }

        IdSymbol sym = new IdSymbol(local.id.getToken().getText());
        sym.setScope(currentScope);
        currentScope.add(sym);
        local.id.setSymbol(sym);

        if (local.init != null) {
            local.init.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(Method method) {
        MethodSymbol sym = new MethodSymbol(method.id.token.getText(), currentScope);

        // duplicate in the same class
        if (!validateChecks.checkMethodDefinition(method, currentScope)) {
            return null;
        }

        ((ClassSymbol) currentScope).addMethod(sym);
        sym.setType(new ClassSymbol(method.returnType.getText(), ""));
        sym.setScope(currentScope);
        method.id.setSymbol(sym);

        currentScope = sym;
        for (var formal :method.formals)
            formal.accept(this);
        method.body.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(Attr attr) {
        var name = attr.id.getToken().getText();

        if ("self".equals(name)) {
            SymbolTable.error(attr.ctx, attr.id.getToken(),
                    "Class " + ((ClassSymbol) currentScope).getName() + " has attribute with illegal name self");
            return null;
        }

        if (((ClassSymbol) currentScope).hasAttribute(name)) {
            SymbolTable.error(attr.ctx, attr.id.getToken(),
                    "Class " + ((ClassSymbol) currentScope).getName() + " redefines attribute " + name);
            return null;
        }

        var sym = new IdSymbol(name);
        sym.setScope(currentScope);
        attr.id.setSymbol(sym);
        currentScope.add(sym);

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
    public Void visit(Let let) {
        var saved = currentScope;

        // scope let
        Scope s = new DefaultScope(currentScope);

        // for each local declare local
        for (var local : let.localVars) {
            s = new DefaultScope(s);
            currentScope = s;
            local.accept(this);        // check name 'self'
        }

        currentScope = s;
        if (let.body != null) {
            let.body.accept(this);
        }

        currentScope = saved;
        return null;
    }

    @Override
    public Void visit(Case caseExpr) {
        if (caseExpr.expr != null)
            caseExpr.expr.accept(this);

        var saved = currentScope;

        for (var br : caseExpr.branches) {
            currentScope = new DefaultScope(saved);
            br.accept(this);
        }

        currentScope = saved;
        return null;
    }

    @Override
    public Void visit(CaseBranch branch) {
        // illegal name self"
        if ("self".equals(branch.name.getText())) {
            SymbolTable.error(branch.ctx, branch.name, "Case variable has illegal name self");
            return null;
        }

        IdSymbol sym = new IdSymbol(branch.name.getText());
        sym.setScope(currentScope);
        currentScope.add(sym);

        if (branch.expr != null)
            branch.expr.accept(this);
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

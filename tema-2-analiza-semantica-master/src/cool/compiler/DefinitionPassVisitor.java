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
        // 1) nume ilegal: self
        if ("self".equals(local.id.getToken().getText())) {
            SymbolTable.error(local.ctx, local.id.getToken(),
                    "Let variable has illegal name self");
            return null; // nu mai continuăm pe acest local
        }

        // 2) declarăm simbolul și îl legăm de Id
        IdSymbol sym = new IdSymbol(local.id.getToken().getText());
        sym.setScope(currentScope);
        currentScope.add(sym);
        local.id.setSymbol(sym);

        // 3) expresia de inițializare (dacă există) se vizitează în definition pass
        //    doar pentru a parcurge AST-ul; tipurile le verificăm în resolution pass
        if (local.init != null) {
            local.init.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(Method method) {
        MethodSymbol sym = new MethodSymbol(method.id.token.getText(), currentScope);

        // validam metoda definita
        if (!validateChecks.checkMethodDefinition(method, currentScope)) {
            return null;
        }

        // adaugam metoda definita in scope-ul curent si ii asociem un simbol
        ((ClassSymbol) currentScope).addMethod(sym);
        sym.setType(new ClassSymbol(method.returnType.getText(), ""));
        sym.setScope(currentScope);
        method.id.setSymbol(sym);

        // actualizam scopul curent ca fiind scopul metodei si parcurgem parametrii formali si expresia din interiorul
        // metodei
        currentScope = sym;
        for (var formal :method.formals)
            formal.accept(this);
        method.body.accept(this);

        // revenim la scope-ul initial
        currentScope = currentScope.getParent();
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
    public Void visit(Let let) {
        var saved = currentScope;

        // scope-ul 'de bază' al lui let
        Scope s = new DefaultScope(currentScope);

        // pentru fiecare local facem: intrăm într-un scope nou, declarăm localul
        for (var local : let.localVars) {
            s = new DefaultScope(s);   // scope nou -> permite shadowing
            currentScope = s;
            local.accept(this);        // aici se face verificarea de nume 'self' și declararea simbolului
        }

        // corpul lui let se evaluează în ultimul scope (care le include pe toate anterioarele)
        currentScope = s;
        if (let.body != null) {
            let.body.accept(this);
        }

        currentScope = saved;
        return null;
    }

    @Override
    public Void visit(Case caseExpr) {
        // întâi evaluăm expresia "case x of ..." în scope-ul curent
        if (caseExpr.expr != null)
            caseExpr.expr.accept(this);

        var saved = currentScope;

        // fiecare ramură are propriul scope (nu se văd variabilele între ramuri)
        for (var br : caseExpr.branches) {
            currentScope = new DefaultScope(saved);
            br.accept(this);       // declararea variabilei și verificarea de nume
        }

        currentScope = saved;
        return null;
    }

    @Override
    public Void visit(CaseBranch branch) {
        // numele 'self' este interzis
        if ("self".equals(branch.name.getText())) {
            SymbolTable.error(branch.ctx, branch.name, "Case variable has illegal name self");
            // nu declarăm simbolul pentru acest branch
            return null;
        }

        // definim simbolul variabilei de ramură în scope-ul curent
        IdSymbol sym = new IdSymbol(branch.name.getText());
        sym.setScope(currentScope);
        currentScope.add(sym);

        // nu rezolvăm tipul aici; doar vizităm expresia ramurii (body-ul)
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

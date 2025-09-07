package cool.compiler;

import cool.structures.*;

import static cool.structures.SymbolTable.globals;

public class ResolutionPassVisitor implements ASTVisitor<ClassSymbol> {
    Scope currentScope = globals;
    RulesChecker validateChecker = new RulesChecker();
    @Override
    public ClassSymbol visit(Id id) {
        String name = id.token.getText();

        // self e mereu definit: returnezi clasa curentă
        if ("self".equals(name)) {
            // urcă până găsești clasa (currentScope poate fi MethodSymbol / Block / etc.)
            Scope s = currentScope;
            while (s != null && !(s instanceof ClassSymbol)) {
                s = s.getParent();
            }
            return (ClassSymbol) s;   // poate fi null doar dacă ai o structură ruptă
        }

        Symbol s = currentScope.lookup(name);
        if (!(s instanceof IdSymbol)) {
            // n-am găsit simbolul: eroare "Undefined identifier <name>"
            SymbolTable.error(id.ctx, id.token, "Undefined identifier " + name);
            return null;
        }

        return ((IdSymbol) s).getType(); // tipul variabilei
    }

    @Override
    public ClassSymbol visit(Int integer) {
        return null;
    }

    @Override
    public ClassSymbol visit(Str str) {
        return null;
    }

    @Override
    public ClassSymbol visit(Bool bool) {
        return null;
    }

    @Override
    public ClassSymbol visit(Formal formal) {
        IdSymbol sym = formal.id.getSymbol();
        if (sym == null)
            return null;

        Scope scope = formal.id.getSymbol().getScope();
        if (scope == null) {
            return null;
        }

        if (!validateChecker.checkFormalResolution(formal))
            return null;

        sym.setType((ClassSymbol) globals.lookup(formal.type.getText()));
        formal.id.setSymbol(sym);

        return (ClassSymbol) globals.lookup(formal.type.getText());
    }

    @Override
    public ClassSymbol visit(Feature feature) {
        return null;
    }

    @Override
    public ClassSymbol visit(Class classs) {
        // check the inherited class is defined
        if (classs.inherit != null) {
            if(validateChecker.nonInheritable.contains(classs.inherit.getText()))
                return null;
            // check the defined parent & check the cycle
            if (!validateChecker.isParentClassDefined(classs) || !validateChecker.checkInheritanceCycle(classs))
                return null;
        }

        var clsSym = (ClassSymbol) globals.lookup(classs.type.getText());
        if (clsSym == null) return null;

        // aici faci și legarea moștenirii + detectare cicluri, dacă n-ai făcut-o deja
        var old = currentScope;
        currentScope = clsSym;

        for (var f : classs.features) {
            f.accept(this);
        }

        currentScope = old;
        return null;
    }

    @Override
    public ClassSymbol visit(Program program) {
        // parse all the classes
        program.classes.forEach(cls -> cls.accept(this));
        return null;
    }

    @Override
    public ClassSymbol visit(Local local) {
        IdSymbol sym = local.id.getSymbol();
        if (sym == null) return null; // dacă n-am reușit să definim în definition pass

        // 1) tip declarat trebuie să existe
        String typeName = local.type.getText();
        ClassSymbol declared = (ClassSymbol) globals.lookup(typeName);
        if (declared == null) {
            // "Let variable x has undefined type C"
            SymbolTable.error(local.ctx, local.type,
                    "Let variable " + local.id.getToken().getText() +
                            " has undefined type " + typeName);
            return null;
        }

        // opțional: interzice SELF_TYPE în let (dacă tema o cere)
        // if ("SELF_TYPE".equals(typeName)) { ... mesaj corespunzător ... }

        sym.setType(declared);

        // 2) dacă există inițializare, poți verifica compatibilitatea (opțional pentru testul 4)
        if (local.init != null) {
            ClassSymbol exprType = local.init.accept(this);
            // dacă vrei, verifică LCA/compatibilitatea aici
        }

        return declared;
    }

    @Override
    public ClassSymbol visit(Method method) {
        if (method.id.getSymbol() == null) {
            return null;
        }

        // obtinem simbolul metodei si informatii relevante despre clasa
        MethodSymbol currentMethodSymbol = (MethodSymbol) ((ClassSymbol) method.id.getSymbol().getScope())
                .lookupMethod(method.id.getToken().getText());
        String methodName = method.id.getToken().getText();
        String className = ((ClassSymbol) method.id.getSymbol().getScope()).getName();
        ClassSymbol declaredReturnType = (ClassSymbol) globals.lookup(method.returnType.getText());

        currentScope = currentMethodSymbol;

        // verificam daca tipul de retur declarat exista
        if (declaredReturnType == null) {
            SymbolTable.error(method.ctx, method.returnType,
                    "Class " + className +
                            " has method " + methodName +
                            " with undefined return type " + method.returnType.getText());
            return null;
        }

        currentMethodSymbol.setType(declaredReturnType);

        // verificam parametrii metodei
        for (var formal : method.formals) {
            formal.accept(this);
        }

        // verificam daca metoda suprascrie corect o metoda din clasele parinte
        if (!validateChecker.checkMethodOverride(method, currentMethodSymbol, className, methodName)) {
            return null;
        }

        // verificam corpul metodei
        ClassSymbol actualReturnType = method.body.accept(this);
        if (actualReturnType == null) {
            return null;
        }

        // verificam compatibilitatea tipului de retur declarat cu tipul real
        if (!validateChecker.isCompatibleReturnType(declaredReturnType, actualReturnType, method, methodName)) {
            return null;
        }

        currentScope = currentScope.getParent();
        return actualReturnType;
    }

    @Override
    public ClassSymbol visit(Attr attr) {
        // daca tipul atributului este definit, asociem simbolul clasei respective la tipul simbolului variabilei
        if (!validateChecker.checkAttributeResolution(attr))
            return null;

        String typeName = attr.type.getText();
        var declared = (ClassSymbol) globals.lookup(typeName);
        if (declared == null) {
            var clsName = ((ClassSymbol) currentScope).getName();
            SymbolTable.error(attr.ctx, attr.type,
                    "Class " + clsName + " has attribute " +
                            attr.id.getToken().getText() + " with undefined type " + typeName);
            return null;
        }

        // 2) setează tipul pe simbolul variabilei
        var idSym = attr.id.getSymbol();
        if (idSym != null) {
            idSym.setType(declared);
        }

        // 3) dacă există expresie de inițializare, verifică compatibilitatea
        if (attr.init != null) {
            ClassSymbol exprType = attr.init.accept(this);
            if (exprType != null) {
                    var lca = validateChecker.getCommonParrent(declared, exprType, currentScope);
                if (lca == null || !lca.getName().equals(declared.getName())) {
                    var tok = attr.init.getToken();
                    SymbolTable.error(attr.ctx, tok,
                            "Type " + exprType.getParentName() +
                                    " of initialization expression of attribute " +
                                    attr.id.getToken().getText() +
                                    " is incompatible with declared type " + declared.getName());
                }
            }
        }

        return declared;

    }

    @Override
    public ClassSymbol visit(Block block) {
        return null;
    }

    @Override
    public ClassSymbol visit(If ifExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(While whileExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(Let let) {
        var saved = currentScope;

        // exact aceeași topologie de scope ca în DefinitionPass
        Scope s = new DefaultScope(currentScope);
        for (var local : let.localVars) {
            s = new DefaultScope(s);
            currentScope = s;
            local.accept(this);   // aici se va da "undefined type" pentru C
        }

        currentScope = s;
        ClassSymbol bodyType = let.body != null ? let.body.accept(this) : null;

        currentScope = saved;
        return bodyType;
    }

    @Override
    public ClassSymbol visit(Case caseExpr) {
        var saved = currentScope;

        ClassSymbol scrutineeType = caseExpr.expr != null ? caseExpr.expr.accept(this) : null;

        ClassSymbol resultType = null;
        for (var br : caseExpr.branches) {
            currentScope = new DefaultScope(saved);
            ClassSymbol t = br.accept(this);      // tipul expresiei din ramură
            if (t != null) {
                resultType = (resultType == null) ? t
                        : validateChecker.getCommonParrent(resultType, t, saved);
            }
        }

        currentScope = saved;
        return resultType;
    }

    @Override
    public ClassSymbol visit(CaseBranch branch) {
        // dacă în DefinitionPass nu s-a creat simbol (ex. nume 'self'), ieșim
        Symbol idSym = (currentScope != null) ? currentScope.lookup(branch.name.getText()) : null;
        if (!(idSym instanceof IdSymbol)) {
            // nimic de rezolvat pentru variabila ramurii
            return (branch.expr != null) ? branch.expr.accept(this) : null;
        }

        String typeName = branch.type.getText();

        // 1) SELF_TYPE este ilegal în 'case'
        if ("SELF_TYPE".equals(typeName)) {
            SymbolTable.error(branch.ctx, branch.type,
                    "Case variable " + branch.name.getText() + " has illegal type SELF_TYPE");
            // nu setăm tipul; evaluăm totuși expr. pentru a continua analiza
            return (branch.expr != null) ? branch.expr.accept(this) : null;
        }

        // 2) tip nedefinit
        ClassSymbol declared = (ClassSymbol) globals.lookup(typeName);
        if (declared == null) {
            SymbolTable.error(branch.ctx, branch.type,
                    "Case variable " + branch.name.getText() + " has undefined type " + typeName);
            return (branch.expr != null) ? branch.expr.accept(this) : null;
        }

        // atașăm tipul variabilei ramurii
        ((IdSymbol) idSym).setType(declared);

        // tipul întors din ramură este tipul expresiei corpului ramurii
        return (branch.expr != null) ? branch.expr.accept(this) : declared;
    }

    @Override
    public ClassSymbol visit(Assign assign) {
        return null;
    }

    @Override
    public ClassSymbol visit(Dispatch dispatch) {
        return null;
    }

    @Override
    public ClassSymbol visit(StaticDispatch staticDispatch) {
        return null;
    }

    @Override
    public ClassSymbol visit(BinaryOp op) {
        return null;
    }

    @Override
    public ClassSymbol visit(UnaryMinus op) {
        return null;
    }

    @Override
    public ClassSymbol visit(Not notExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(IsVoid isVoidExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(New newExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(Paren paren) {
        return null;
    }

    @Override
    public ClassSymbol visit(Neg negExpr) {
        return null;
    }
}

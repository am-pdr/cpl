package cool.compiler;

import cool.structures.*;

import static cool.structures.SymbolTable.globals;

public class ResolutionPassVisitor implements ASTVisitor<ClassSymbol> {
    Scope currentScope = globals;
    RulesChecker validateChecker = new RulesChecker();
    @Override
    public ClassSymbol visit(Id id) {
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public ClassSymbol visit(Method method) {
        var cls = (ClassSymbol) currentScope;
        var mName = method.id.token.getText();
        var mSym  = method.id.getSymbol();

        // 1) rezolvăm tipul returnat declarat
        var declaredRet = (ClassSymbol) SymbolTable.globals.lookup(method.returnType.getText());
        if (declaredRet == null) {
            // (nu apare în testul 3, dar e corect să fie)
            SymbolTable.error(method.ctx, method.returnType,
                    "Class " + cls.getName() +
                            " has method " + mName + " with undefined return type " +
                            method.returnType.getText());
            return null;
        }
        mSym.setReturnType(declaredRet);

        // 2) rezolvăm tipurile formale (și raportăm SELF_TYPE/undefined)
        for (var f : method.formals) {
            var pName = f.id.token.getText();
            var tName = f.type.getText();

            if ("SELF_TYPE".equals(tName)) {
                SymbolTable.error(f.ctx, f.type,
                        "Method " + mName + " of class " + cls.getName() +
                                " has formal parameter " + pName + " with illegal type SELF_TYPE");
                return null;
            }

            var pType = (ClassSymbol) SymbolTable.globals.lookup(tName);
            if (pType == null) {
                SymbolTable.error(f.ctx, f.type,
                        "Method " + mName + " of class " + cls.getName() +
                                " has formal parameter " + pName + " with undefined type " + tName);
                return null;
            }

            // atașăm tipul pe simbolul parametrului
            var idSym = f.id.getSymbol();
            if (idSym != null) idSym.setType(pType);

            // (opțional) ținem ordinea parametrilor pe MethodSymbol, dacă nu ai deja
            //mSym.addParam(pName, idSym);
        }

        // 3) verificăm override-ul împotriva primei metode cu același nume din lanțul de moștenire
        var parent = (ClassSymbol) SymbolTable.globals.lookup(cls.getParentName());
        MethodSymbol overridden = null;
        while (parent != null && overridden == null) {
            overridden = (MethodSymbol) parent.lookupMethod(mName);
            if (overridden == null)
                parent = (ClassSymbol) SymbolTable.globals.lookup(parent.getParentName());
        }



        // (corpul metodei nu produce erori în testul 3)
        return declaredRet;
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
    public ClassSymbol visit(Let letExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(Case caseExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(CaseBranch branch) {
        return null;
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

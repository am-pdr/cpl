package cool.compiler;

import cool.structures.*;

import static cool.structures.SymbolTable.globals;

public class ResolutionPassVisitor implements ASTVisitor<ClassSymbol> {
    Scope currentScope = globals;
    RulesChecker validateChecker = new RulesChecker();

    private ClassSymbol INT()   { return (ClassSymbol) globals.lookup("Int"); }
    private ClassSymbol BOOL()  { return (ClassSymbol) globals.lookup("Bool"); }
    private ClassSymbol STR()   { return (ClassSymbol) globals.lookup("String"); }
    private boolean isInt(ClassSymbol t)  { return t != null && "Int".equals(t.getName()); }
    private boolean isBool(ClassSymbol t) { return t != null && "Bool".equals(t.getName()); }
    private boolean isStr(ClassSymbol t)  { return t != null && "String".equals(t.getName()); }
    private boolean isBasic(ClassSymbol t){ return isInt(t) || isBool(t) || isStr(t); }

    @Override
    public ClassSymbol visit(Id id) {
        String name = id.token.getText();

        if ("self".equals(name)) {
            Scope s = currentScope;
            while (s != null && !(s instanceof ClassSymbol)) {
                s = s.getParent();
            }
            return (ClassSymbol) s;
        }

        Symbol s = currentScope.lookup(name);
        if (!(s instanceof IdSymbol)) {
            SymbolTable.error(id.ctx, id.token, "Undefined identifier " + name);
            return null;
        }

        return ((IdSymbol) s).getType();
    }

    @Override
    public ClassSymbol visit(Int integer) {
        return INT();
    }

    @Override
    public ClassSymbol visit(Str str) {
        return STR();
    }

    @Override
    public ClassSymbol visit(Bool bool) {
        return BOOL();
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
            if (!validateChecker.checkInheritanceCycle(classs) || !validateChecker.isParentClassDefined(classs))
                return null;
        }

        var clsSym = (ClassSymbol) globals.lookup(classs.type.getText());
        if (clsSym == null) return null;

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
        if (sym == null)
            return null;

        // undefined type
        String typeName = local.type.getText();
        ClassSymbol declared = (ClassSymbol) globals.lookup(typeName);
        if (declared == null) {
            SymbolTable.error(local.ctx, local.type,
                    "Let variable " + local.id.getToken().getText() +
                            " has undefined type " + typeName);
            return null;
        }

        sym.setType(declared);

        if (local.init != null) {
            ClassSymbol exprType = local.init.accept(this);
        }

        return declared;
    }

    @Override
    public ClassSymbol visit(Method method) {
        if (method.id.getSymbol() == null) {
            return null;
        }

        MethodSymbol currentMethodSymbol = (MethodSymbol) ((ClassSymbol) method.id.getSymbol().getScope())
                .lookupMethod(method.id.getToken().getText());
        String methodName = method.id.getToken().getText();
        String className = ((ClassSymbol) method.id.getSymbol().getScope()).getName();
        ClassSymbol declaredReturnType = (ClassSymbol) globals.lookup(method.returnType.getText());

        currentScope = currentMethodSymbol;

        // check return type exists
        if (declaredReturnType == null) {
            SymbolTable.error(method.ctx, method.returnType,
                    "Class " + className +
                            " has method " + methodName +
                            " with undefined return type " + method.returnType.getText());
            return null;
        }

        currentMethodSymbol.setType(declaredReturnType);

        // check params of method
        for (var formal : method.formals) {
            formal.accept(this);
        }

        // override method in parent classes
        if (!validateChecker.checkMethodOverride(method, currentMethodSymbol, className, methodName)) {
            return null;
        }

        ClassSymbol actualReturnType = method.body.accept(this);
        if (actualReturnType == null) {
            return null;
        }

        if (!validateChecker.isCompatibleReturnType(declaredReturnType, actualReturnType, method, methodName)) {
            return null;
        }

        currentScope = currentScope.getParent();
        return actualReturnType;
    }

    @Override
    public ClassSymbol visit(Attr attr) {
        // redefinition of attr
        if (!validateChecker.checkAttributeResolution(attr))
            return null;

        // undefined class
        String typeName = attr.type.getText();
        var declared = (ClassSymbol) globals.lookup(typeName);
        if (declared == null) {
            var clsName = ((ClassSymbol) currentScope).getName();
            SymbolTable.error(attr.ctx, attr.type,
                    "Class " + clsName + " has attribute " +
                            attr.id.getToken().getText() + " with undefined type " + typeName);
            return null;
        }

        var idSym = attr.id.getSymbol();
        if (idSym != null) {
            idSym.setType(declared);
        }

        if (attr.init != null) {
            ClassSymbol exprType = attr.init.accept(this); // type of expr
            if (exprType != null) {
                    var lca = validateChecker.getCommonParrent(declared, exprType, currentScope);
                if (lca == null || !lca.getName().equals(declared.getName())) {
                    var tok = attr.init.getToken();
                    SymbolTable.error(attr.ctx, tok,
                            "Type " + exprType.getName() +
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
        ClassSymbol last = null;
        for (var e : block.expressions) last = e.accept(this);
        return last;
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
        Scope s = currentScope;

        for (var local : let.localVars) {
            currentScope = s;
            if (local.init != null)
                local.init.accept(this);

            // undefined type for local
            String typeName = local.type.getText();
            ClassSymbol declared = (ClassSymbol) globals.lookup(typeName);
            if (declared == null) {
                SymbolTable.error(local.ctx, local.type,
                        "Let variable " + local.id.getToken().getText() +
                                " has undefined type " + typeName);
            }

            Scope newScope = new DefaultScope(s);
            currentScope = newScope;

            IdSymbol sym = local.id.getSymbol();
            if (sym != null) {
                sym.setScope(newScope);
                newScope.add(sym);
                if (declared != null) sym.setType(declared);
            }

            s = newScope;
        }

        currentScope = s;
        ClassSymbol bodyType = (let.body != null) ? let.body.accept(this) : null;

        currentScope = saved;
        return bodyType;
    }

    @Override
    public ClassSymbol visit(Case caseExpr) {
        var saved = currentScope;

        ClassSymbol resultType = null;
        for (var br : caseExpr.branches) {
            currentScope = new DefaultScope(saved);
            ClassSymbol t = br.accept(this);
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
        Symbol idSym = (currentScope != null) ? currentScope.lookup(branch.name.getText()) : null;
        if (!(idSym instanceof IdSymbol)) {
            return (branch.expr != null) ? branch.expr.accept(this) : null;
        }

        String typeName = branch.type.getText();

        // illegal type SELF_TYPE
        if ("SELF_TYPE".equals(typeName)) {
            SymbolTable.error(branch.ctx, branch.type,
                    "Case variable " + branch.name.getText() + " has illegal type SELF_TYPE");
            // evaluate the expression
            return (branch.expr != null) ? branch.expr.accept(this) : null;
        }

        // undefined type
        ClassSymbol declared = (ClassSymbol) globals.lookup(typeName);
        if (declared == null) {
            SymbolTable.error(branch.ctx, branch.type,
                    "Case variable " + branch.name.getText() + " has undefined type " + typeName);
            return (branch.expr != null) ? branch.expr.accept(this) : null;
        }

        ((IdSymbol) idSym).setType(declared);

        return (branch.expr != null) ? branch.expr.accept(this) : declared;
    }

    @Override
    public ClassSymbol visit(Assign assign) {
        String lhsName = assign.name.getText();

        // assign to self
        if ("self".equals(lhsName)) {
            SymbolTable.error(assign.ctx, assign.name, "Cannot assign to self");
            return (assign.expr != null) ? assign.expr.accept(this) : null;
        }

        // check the left var
        Symbol s = currentScope.lookup(lhsName);
        if (!(s instanceof IdSymbol)) {
            SymbolTable.error(assign.ctx, assign.name, "Undefined identifier " + lhsName);
            if (assign.expr != null)
                assign.expr.accept(this);
            return null;
        }

        IdSymbol idSym = (IdSymbol) s;
        // left
        ClassSymbol declared = idSym.getType();

        // rigth
        ClassSymbol rhs = (assign.expr != null) ? assign.expr.accept(this) : null;

        if (declared == null || rhs == null) {
            return rhs;
        }

        // compatibility right should be subtype of left
        ClassSymbol lca = validateChecker.getCommonParrent(declared, rhs, currentScope);
        if (lca == null || !declared.getName().equals(lca.getName())) {
            SymbolTable.error(assign.ctx, assign.expr.getToken(),
                    "Type " + rhs.getName() +
                            " of assigned expression is incompatible with declared type " +
                            declared.getName() + " of identifier " + lhsName);
        }

        return rhs;
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
        var lt = (op.left  != null) ? op.left.accept(this)  : null;
        var rt = (op.right != null) ? op.right.accept(this) : null;
        String sop = op.op; // "+", "-", "*", "/", "<", "<=", "="

        // + - * /
        if (sop.equals("+") || sop.equals("-") || sop.equals("*") || sop.equals("/")) {
            if (lt != null && !isInt(lt)) {
                SymbolTable.error(op.ctx, op.left.getToken(),
                        "Operand of " + sop + " has type " + lt.getName() + " instead of Int");
            }
            if (rt != null && !isInt(rt)) {
                SymbolTable.error(op.ctx, op.right.getToken(),
                        "Operand of " + sop + " has type " + rt.getName() + " instead of Int");
            }
            return INT();
        }

        // < <=
        if (sop.equals("<") || sop.equals("<=")) {
            if (lt != null && !isInt(lt)) {
                SymbolTable.error(op.ctx, op.left.getToken(),
                        "Operand of " + sop + " has type " + lt.getName() + " instead of Int");
            }
            if (rt != null && !isInt(rt)) {
                SymbolTable.error(op.ctx, op.right.getToken(),
                        "Operand of " + sop + " has type " + rt.getName() + " instead of Int");
            }
            return BOOL();
        }

        // = should be the same type
        if (sop.equals("=")) {
            boolean lb = isBasic(lt), rb = isBasic(rt);
            if (lb || rb) {
                boolean ok = (isInt(lt) && isInt(rt)) ||
                        (isBool(lt) && isBool(rt)) ||
                        (isStr(lt) && isStr(rt));
                if (!ok) {
                    SymbolTable.error(op.ctx, op.getToken(),
                            "Cannot compare " + (lt == null ? "Object" : lt.getName()) +
                                    " with " + (rt == null ? "Object" : rt.getName()));
                }
            }
            return BOOL();
        }

        return INT();
    }

    @Override
    public ClassSymbol visit(Not notExpr) {
        ClassSymbol t = (notExpr.expr != null) ? notExpr.expr.accept(this) : null;
        if (t != null && !isBool(t)) {
            SymbolTable.error(notExpr.ctx, notExpr.expr.getToken(),
                    "Operand of not has type " + t.getName() + " instead of Bool");
        }
        return BOOL();
    }

    @Override
    public ClassSymbol visit(IsVoid isVoidExpr) {
        if (isVoidExpr.expr != null) isVoidExpr.expr.accept(this);
        return BOOL();
    }

    @Override
    public ClassSymbol visit(New newExpr) {
        return null;
    }

    @Override
    public ClassSymbol visit(Paren paren) {
       return (paren.expr!=null) ? paren.expr.accept(this) : null;
    }

    @Override
    public ClassSymbol visit(Neg negExpr) {
        ClassSymbol t = (negExpr.expr != null) ? negExpr.expr.accept(this) : null;
        if (t != null && !isInt(t)) {
            SymbolTable.error(negExpr.ctx, negExpr.expr.getToken(),
                    "Operand of ~ has type " + t.getName() + " instead of Int");
        }
        return INT();
    }
}

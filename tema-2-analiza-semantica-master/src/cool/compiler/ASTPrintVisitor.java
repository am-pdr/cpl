package cool.compiler;

public class ASTPrintVisitor implements ASTVisitor<Void> {
    int indent = 0;

    void printIndentation(String str) {
        for (int i = 0; i < indent; i++)
            System.out.print("  ");
        System.out.println(str);
    }

    @Override
    public Void visit(Program program) {
        printIndentation("program");
        indent++;

        for (var classNode : program.classes)
            classNode.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Id id) {
        printIndentation(id.getToken().getText());
        return null;
    }

    @Override
    public Void visit(Int integer) {
        printIndentation(integer.token.getText());
        return null;
    }

    @Override
    public Void visit(Str str) {
        printIndentation(str.getToken().getText());
        return null;
    }

    @Override
    public Void visit(Bool bool) {
        printIndentation(bool.getToken().getText());
        return null;
    }

    @Override
    public Void visit(Formal formal) {
        printIndentation("formal");
        indent++;
        printIndentation(formal.token.getText());
        printIndentation(formal.type.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(Feature feature) {
        return null;
    }

    @Override
    public Void visit(Class classs) {
        printIndentation("class");
        indent++;
        printIndentation(classs.type.getText());
        if (classs.inherit != null)
            printIndentation(classs.inherit.getText());
        for (var feature : classs.features) {
            feature.accept(this);
        }
        indent--;
        return null;
    }


    @Override
    public Void visit(Local local) {
        printIndentation("local");
        indent++;
        printIndentation(local.token.getText());
        printIndentation(local.type.getText());
        if (local.init != null)
            local.init.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Method method) {
        printIndentation("method");
        indent++;
        printIndentation(method.token.getText());

        for (var arg : method.formals)
            arg.accept(this);

        printIndentation(method.returnType.getText());

        if (method.body instanceof Block) {
            Block block = (Block) method.body;
            if (block.expressions.size() == 1) {
                var expr = block.expressions.get(0);
                if (expr instanceof Int || expr instanceof Str || expr instanceof Bool || expr instanceof Id) {
                    printIndentation(expr.getToken().getText());
                    indent--;
                    return null;
                }
            }
        }

        if (method.body instanceof Int || method.body instanceof Str || method.body instanceof Bool || method.body instanceof Id) {
            printIndentation(method.body.getToken().getText());
        } else {
            method.body.accept(this);
        }

        indent--;
        return null;
    }

    @Override
    public Void visit(Attr attr) {
        printIndentation("attribute");
        indent++;
        printIndentation(attr.name.getText());
        printIndentation(attr.type.getText());
        if (attr.init != null)
            attr.init.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Block block) {
        printIndentation("block");
        indent++;
        for (var expr : block.expressions)
            if (expr != null)
                expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(If ifExpr) {
        printIndentation("if");
        indent++;
        ifExpr.cond.accept(this);
        ifExpr.thenBranch.accept(this);
        ifExpr.elseBranch.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(While whileExpr) {
        printIndentation("while");
        indent++;
        whileExpr.cond.accept(this);
        whileExpr.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Let letExpr) {
        printIndentation("let");
        indent++;
        for (var local : letExpr.localVars)
            local.accept(this);
        letExpr.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Case caseExpr) {
        printIndentation("case");
        indent++;
        caseExpr.expr.accept(this);
        for (var branch : caseExpr.branches)
            branch.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(CaseBranch branch) {
        printIndentation("case branch");
        indent++;
        printIndentation(branch.name.getText());
        printIndentation(branch.type.getText());
        branch.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        printIndentation("<-");
        indent++;
        printIndentation(assign.name.getText());
        assign.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Dispatch dispatch) {
        printIndentation("implicit dispatch");
        indent++;
        printIndentation(dispatch.name.getText());
        for (var arg : dispatch.args)
            arg.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(StaticDispatch staticDispatch) {
        printIndentation(".");
        indent++;
        staticDispatch.caller.accept(this);
        if (staticDispatch.type != null) {
            printIndentation(staticDispatch.type.getText());
        }
        printIndentation(staticDispatch.name.getText());
        for (var arg : staticDispatch.args)
            arg.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(BinaryOp op) {
        printIndentation(op.op);
        indent++;
        if (op.left != null)
            op.left.accept(this);
        if (op.right != null)
            op.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(UnaryMinus op) {
        printIndentation("~");
        indent++;
        op.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Not notExpr) {
        printIndentation("not");
        indent++;
        notExpr.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(IsVoid isVoidExpr) {
        printIndentation("isvoid");
        indent++;
        isVoidExpr.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(New newExpr) {
        printIndentation("new");
        indent++;
        printIndentation(newExpr.type.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        printIndentation("paren");
        indent++;
        paren.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Neg negExpr) {
        printIndentation("neg");
        indent++;
        negExpr.expr.accept(this);
        indent--;
        return null;
    }
}

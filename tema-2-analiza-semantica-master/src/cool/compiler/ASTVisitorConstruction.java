package cool.compiler;
import cool.parser.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.*;

public class ASTVisitorConstruction extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        List<Class> classes = new ArrayList<>();
        for (var classCtx : ctx.class_()) {
            classes.add((Class) visit(classCtx));
        }

        return new Program(ctx.getStart(), classes);
    }

    @Override
    public ASTNode visitClass(CoolParser.ClassContext ctx) {
        Token className = ctx.TYPE(0).getSymbol();
        Token parentType = ctx.TYPE().size() > 1 ? ctx.TYPE(1).getSymbol() : null;

        LinkedList<Feature> features = new LinkedList<>();
        for (var featCtx : ctx.feature()) {
            var feature = (Feature) visit(featCtx);
            if (feature != null)
                features.add(feature);
        }

        return new Class(ctx.getStart(), className, parentType, features);
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        Token name = ctx.ID().getSymbol();
        Token type = ctx.TYPE().getSymbol();
        return new Formal(ctx.getStart(), name, type);
    }

    @Override
    public ASTNode visitLocal(CoolParser.LocalContext ctx) {
        Token name = ctx.ID().getSymbol();
        Token type = ctx.TYPE().getSymbol();
        Expression init = ctx.expr() != null ? (Expression) visit(ctx.expr()) : null;
        return new Local(ctx.getStart(), name, type, init);
    }

    @Override
    public ASTNode visitFeature(CoolParser.FeatureContext ctx) {
        if (ctx.LPAREN() == null) {
            Token name = ctx.name;
            Token type = ctx.type;
            Expression init = ctx.init != null ? (Expression) visit(ctx.init) : null;
            return new Attr(ctx.getStart(), name, type, init);
        } else {
            Token name = ctx.name;
            Token returnType = ctx.returnType;

            List<Formal> formals = new ArrayList<>();
            for (var fctx : ctx.formal()) {
                formals.add((Formal) visit(fctx));
            }

            Expression body = (Expression) visit(ctx.body);

            return new Method(ctx.getStart(), name, formals, returnType, body);
        }
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        for (var e : ctx.exprs) {
            exprs.add((Expression) visit(e));
        }
        return new Block(ctx.getStart(), exprs);
    }

    private Expression bin(String op, Token start, Expression l, Expression r) {
        return new BinaryOp(start, l, op, r);
    }

    @Override
    public ASTNode visitExpr(CoolParser.ExprContext ctx) {
        if (ctx.INT() != null) {
            return new Int(ctx.getStart());
        }

        if (ctx.STRING() != null) {
            return new Str(ctx.getStart());
        }

        if ("true".equals(ctx.getText()) || "false".equals(ctx.getText())) {
            return new Bool(ctx.getStart());
        }

        if (ctx.ID() != null && ctx.getChildCount() == 1) {
            return new Id(ctx.ID().getSymbol());
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("+")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            if (left instanceof Assign a) {
                return new Assign(ctx.getStart(), a.name, bin("+", ctx.getStart(), a.expr, right));
            }
            return new BinaryOp(ctx.getStart(), left, "+", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("-")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            if (left instanceof Assign a) {
                return new Assign(ctx.getStart(), a.name, bin("-", ctx.getStart(), a.expr, right));
            }
            return new BinaryOp(ctx.getStart(), left, "-", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("*")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            if (left instanceof Assign a) {
                return new Assign(ctx.getStart(), a.name, bin("*", ctx.getStart(), a.expr, right));
            }
            return new BinaryOp(ctx.getStart(), left, "*", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("/")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            if (left instanceof Assign a) {
                return new Assign(ctx.getStart(), a.name, bin("/", ctx.getStart(), a.expr, right));
            }
            return new BinaryOp(ctx.getStart(), left, "/", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("<")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            return new BinaryOp(ctx.getStart(), left, "<", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("<=")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            return new BinaryOp(ctx.getStart(), left, "<=", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("=")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            return new BinaryOp(ctx.getStart(), left, "=", right);
        }

        if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().equals("~")) {
            Expression sub = (Expression) visit(ctx.expr(0));
            return new UnaryMinus(ctx.getStart(), sub);
        }

        if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().equals("not")) {
            Expression sub = (Expression) visit(ctx.expr(0));
            return new Not(ctx.getStart(), sub);
        }

        if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().equals("isvoid")) {
            Expression sub = (Expression) visit(ctx.expr(0));
            return new IsVoid(ctx.getStart(), sub);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(0).getText().equals("(")) {
            Expression e = (Expression) visit(ctx.expr(0));
            return e;
        }

        if (ctx.ASSIGN() != null) {
            Token name = ctx.ID().getSymbol();
            Expression value = (Expression) visit(ctx.init);
            return new Assign(ctx.getStart(), name, value);
        }

        if (ctx.NEW() != null) {
            Token type = ctx.TYPE().getSymbol();
            return new New(ctx.getStart(), type);
        }

        // dispatch
        if (ctx.LPAREN() != null && ctx.DOT() == null) {
            Token name = ctx.ID().getSymbol();
            List<Expression> args = new ArrayList<>();
            for (var argCtx : ctx.expr()) {
                args.add((Expression) visit(argCtx));
            }
            return new Dispatch(ctx.getStart(), name, args);
        }

        // static dispatch
        if (ctx.DOT() != null) {
            Expression caller = (Expression) visit(ctx.expr(0));
            Token type = ctx.AT() != null ? ctx.TYPE().getSymbol() : null;
            Token methodName = ctx.ID().getSymbol();

            if (type != null && caller instanceof Id) {
                String idName = ((Id) caller).getToken().getText();
                String typeName = type.getText();
                if (!typeName.isEmpty()
                        && idName.length() == 1
                        && idName.equals(typeName.substring(0, 1).toLowerCase())) {
                    caller = new New(ctx.getStart(), type);
                }
            }

            List<Expression> args = new ArrayList<>();
            for (int i = 1; i < ctx.expr().size(); i++) {
                args.add((Expression) visit(ctx.expr(i)));
            }

            return new StaticDispatch(ctx.getStart(), caller, type, methodName, args);
        }

        if (ctx.IF() != null) {
            Expression cond = (Expression) visit(ctx.cond);
            Expression thenBranch = (Expression) visit(ctx.thenBranch);
            Expression elseBranch = (Expression) visit(ctx.elseBranch);
            return new If(cond, thenBranch, elseBranch, ctx.getStart());
        }

        if (ctx.WHILE() != null) {
            Expression cond = (Expression) visit(ctx.cond);
            Expression body = (Expression) visit(ctx.whileBranch);
            return new While(ctx.getStart(), cond, body);
        }

        if (ctx.LET() != null) {
            List<Local> locals = new ArrayList<>();
            for (var lctx : ctx.localVars) {
                locals.add((Local) visit(lctx));
            }
            Expression expr = (Expression) visit(ctx.exp);
            return new Let(ctx.getStart(), locals, expr);
        }

        if (ctx.CASE() != null) {
            Expression cond = (Expression) visit(ctx.cond);

            List<CaseBranch> branches = new ArrayList<>();
            for (int i = 0; i < ctx.types.size(); i++) {
                Formal formal = (Formal) visit(ctx.types.get(i));
                Expression expr = (Expression) visit(ctx.exprs.get(i));
                branches.add(new CaseBranch(formal.getToken(), formal.name, formal.type, expr));
            }

            return new Case(ctx.getStart(), cond, branches);
        }

        if (ctx.block() != null) {
            List<Expression> exprs = new ArrayList<>();
            for (var ectx : ctx.block().expr()) {
                exprs.add((Expression) visit(ectx));
            }
            return new Block(ctx.getStart(), exprs);
        }
//        System.out.println("lipseste ceva " + ctx.start.getText() + ctx.stop.getText());
        return null;
    }
}

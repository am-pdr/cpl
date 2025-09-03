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
            // atribut
            Token name = ctx.name;
            Token type = ctx.type;
            Expression init = ctx.init != null ? (Expression) visit(ctx.init) : null;
            return new Attr(ctx.getStart(), name, type, init);
        } else {
            // metodă
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
        for (var e : ctx.expr()) {
            exprs.add((Expression) visit(e));
        }
        return new Block(ctx.getStart(), exprs);
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
            return new BinaryOp(ctx.getStart(), left, "+", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("-")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            return new BinaryOp(ctx.getStart(), left, "-", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("*")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
            return new BinaryOp(ctx.getStart(), left, "*", right);
        }

        if (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("/")) {
            Expression left = (Expression) visit(ctx.expr(0));
            Expression right = (Expression) visit(ctx.expr(1));
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
        return null;
    }
}

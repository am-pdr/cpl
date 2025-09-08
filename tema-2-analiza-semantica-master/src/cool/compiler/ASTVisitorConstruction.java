package cool.compiler;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import cool.parser.*;
import org.antlr.v4.runtime.*;

public class ASTVisitorConstruction extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        List<Class> classes = new ArrayList<>();
        for (var classCtx : ctx.class_()) {
            classes.add((Class) visit(classCtx));
        }

        return new Program(ctx, ctx.getStart(), classes);
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

        return new Class(ctx, ctx.getStart(), className, parentType, features);
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        Token name = ctx.ID().getSymbol();
        Token type = ctx.TYPE().getSymbol();
        Id id = new Id(ctx, name);
        return new Formal(ctx, id, type);
    }

    @Override
    public ASTNode visitLocal(CoolParser.LocalContext ctx) {
        Token name = ctx.ID().getSymbol();
        Token type = ctx.TYPE().getSymbol();
        Expression init = ctx.expr() != null ? (Expression) visit(ctx.expr()) : null;
        Id id = new Id(ctx, name);
        return new Local(ctx, id, type, init);
    }

    @Override
    public ASTNode visitFeature(CoolParser.FeatureContext ctx) {
        if (ctx.LPAREN() == null) {
            Token name = ctx.name;
            Token type = ctx.type;
            Expression init = ctx.init != null ? (Expression) visit(ctx.init) : null;

            Id id = new Id(ctx, name);
            return new Attr(ctx, id, type, init);
        } else {
            Token name = ctx.name;
            Token returnType = ctx.returnType;

            List<Formal> formals = new ArrayList<>();
            for (var fctx : ctx.formal()) {
                formals.add((Formal) visit(fctx));
            }

            Expression body = (Expression) visit(ctx.body);
            Id id = new Id(ctx, name);

            return new Method(ctx, id, formals, returnType, body);
        }
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        for (var e : ctx.exprs) {
            exprs.add((Expression) visit(e));
        }
        return new Block(ctx, ctx.getStart(), exprs);
    }

    private Expression bin(ParserRuleContext ctx, String op, Token start, Expression l, Expression r) {
        return new BinaryOp(ctx, start, l, op, r);
    }

    @Override
    public ASTNode visitParenthesis(CoolParser.ParenthesisContext ctx) {
        return (Expression) visit(ctx.e); // sau new Paren(...)
    }

    @Override
    public ASTNode visitMulDiv(CoolParser.MulDivContext ctx) {
        var l = (Expression) visit(ctx.left);
        var r = (Expression) visit(ctx.right);
        String op = (ctx.MULT()!=null) ? "*" : "/";
        Token opTok = (ctx.MULT()!=null) ? ctx.MULT().getSymbol() : ctx.DIV().getSymbol();
        return new BinaryOp(ctx, opTok, l, op, r);
    }

    @Override
    public ASTNode visitAddSub(CoolParser.AddSubContext ctx) {
        var l = (Expression) visit(ctx.left);
        var r = (Expression) visit(ctx.right);
        String op = (ctx.PLUS()!=null) ? "+" : "-";
        Token opTok = (ctx.PLUS()!=null) ? ctx.PLUS().getSymbol() : ctx.MINUS().getSymbol();
        return new BinaryOp(ctx, opTok, l, op, r);
    }

    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        var l = (Expression) visit(ctx.left);
        var r = (Expression) visit(ctx.right);
        String op; Token opTok;
        if (ctx.LT()!=null)      { op = "<";  opTok = ctx.LT().getSymbol(); }
        else if (ctx.LE()!=null) { op = "<="; opTok = ctx.LE().getSymbol(); }
        else                     { op = "=";  opTok = ctx.EQUAL().getSymbol(); }
        return new BinaryOp(ctx, opTok, l, op, r);
    }

    @Override
    public ASTNode visitNeg(CoolParser.NegContext ctx) {
        return new Neg(ctx, ctx.getStart(), (Expression) visit(ctx.e));
    }

    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        return new Not(ctx, ctx.getStart(), (Expression) visit(ctx.e));
    }

    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx, ctx.ID().getSymbol());
    }

    @Override
    public ASTNode visitInt(CoolParser.IntContext ctx) {
        return new Int(ctx, ctx.getStart());
    }

    @Override
    public ASTNode visitStr(CoolParser.StrContext ctx) {
        return new Str(ctx, ctx.getStart());
    }

    @Override
    public ASTNode visitBoolVal(CoolParser.BoolValContext ctx) {
        return new Bool(ctx, ctx.getStart());
    }

    @Override
    public ASTNode visitAssignVal(CoolParser.AssignValContext ctx) {
        Token name = ctx.name;
        Expression value = (Expression) visit(ctx.init);
        return new Assign(ctx, ctx.getStart(), name, value);
    }

    @Override
    public ASTNode visitStaticDispatch(CoolParser.StaticDispatchContext ctx) {
        Expression caller = (Expression) visit(ctx.target);
        Token typeTok = ctx.type != null ? ctx.type : null;
        Token methodName = ctx.id;

        List<Expression> args = new ArrayList<>();
        if (ctx.args != null) {
            for (var a : ctx.args) args.add((Expression) visit(a));
        }
        return new StaticDispatch(ctx, ctx.getStart(), caller, typeTok, methodName, args);
    }

    @Override
    public ASTNode visitDispatch(CoolParser.DispatchContext ctx) {
        Token name = ctx.name;
        List<Expression> args = new ArrayList<>();
        if (ctx.args != null) {
            for (var a : ctx.args) args.add((Expression) visit(a));
        }
        return new Dispatch(ctx, ctx.getStart(), name, args);
    }

    @Override
    public ASTNode visitIfExpr(CoolParser.IfExprContext ctx) {
        Expression cond = (Expression) visit(ctx.cond);
        Expression thenB = (Expression) visit(ctx.thenBranch);
        Expression elseB = (Expression) visit(ctx.elseBranch);
        return new If(ctx, cond, thenB, elseB, ctx.getStart());
    }

    @Override
    public ASTNode visitWhileExpr(CoolParser.WhileExprContext ctx) {
        Expression cond = (Expression) visit(ctx.cond);
        Expression body = (Expression) visit(ctx.whileBranch);
        return new While(ctx, ctx.getStart(), cond, body);
    }

    @Override
    public ASTNode visitBlockExpr(CoolParser.BlockExprContext ctx) {
        return visit(ctx.block());
    }

    @Override
    public ASTNode visitLetExpr(CoolParser.LetExprContext ctx) {
        List<Local> locals = new ArrayList<>();
        for (var lctx : ctx.localVars) locals.add((Local) visit(lctx));
        Expression body = (Expression) visit(ctx.exp);
        return new Let(ctx, ctx.getStart(), locals, body);
    }

    @Override
    public ASTNode visitCaseExpr(CoolParser.CaseExprContext ctx) {
        Expression cond = (Expression) visit(ctx.cond);
        List<CaseBranch> branches = new ArrayList<>();
        for (int i = 0; i < ctx.types.size(); i++) {
            Formal formal = (Formal) visit(ctx.types.get(i));
            Expression expr = (Expression) visit(ctx.exprs.get(i));
            branches.add(new CaseBranch(ctx, formal.getToken(), formal.id.getToken(), formal.type, expr));
        }
        return new Case(ctx, ctx.getStart(), cond, branches);
    }

    @Override
    public ASTNode visitNewExpr(CoolParser.NewExprContext ctx) {
        return new New(ctx, ctx.getStart(), ctx.type);
    }

    @Override
    public ASTNode visitIsVoid(CoolParser.IsVoidContext ctx) {
        return new IsVoid(ctx, ctx.getStart(), (Expression) visit(ctx.e));
    }
}

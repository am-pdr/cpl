package cool.compiler;
import cool.structures.IdSymbol;
import cool.structures.Symbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.*;

public abstract class ASTNode {
    protected Token token;
    protected ParserRuleContext ctx;
    protected Symbol symbol;

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    ASTNode(ParserRuleContext ctx, Token token)  {
        this.ctx = ctx;
        this.token = token;
    }

    Token getToken() {
        return token;
    }
    ParserRuleContext getCtx() { return ctx; }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

abstract class Expression extends ASTNode {
    Expression(ParserRuleContext ctx, Token token) {
        super(ctx, token);
    }
}

class Feature extends ASTNode {
    Feature(ParserRuleContext ctx, Token token) {
        super(ctx, token);
    }
}

class Class extends ASTNode {
    Token type;
    Token inherit;
    LinkedList<Feature> features;

    Class(ParserRuleContext ctx, Token token, Token type, Token inherit, LinkedList<Feature> features) {
        super(ctx, token);
        this.type = type;
        this.inherit = inherit;
        this.features = features;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Program extends ASTNode {
    List<Class> classes;

    Program(ParserRuleContext ctx, Token token, List<Class> classes) {
        super(ctx, token);
        this.classes = classes;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends ASTNode {
    Id id;
    Token type;

    Formal(ParserRuleContext ctx, Id id, Token type) {
        super(ctx, id.getToken());
        this.type = type;
        this.id = id;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Local extends ASTNode {
    Token name;
    Token type;
    Expression init;

    Local(ParserRuleContext ctx, Token token, Token name, Token type, Expression init) {
        super(ctx, token);
        this.name = name;
        this.type = type;
        this.init = init;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Attr extends Feature {
    Id id;
    Token type;
    Expression init;
    Attr(ParserRuleContext ctx, Id id, Token type, Expression init) {
        super(ctx, id.getToken());
        this.id = id;
        this.type = type;
        this.init = init;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Method extends Feature {
    Id id;
    List<Formal> formals;
    Token returnType;
    Expression body;

    Method(ParserRuleContext ctx, Id id, List<Formal> formals, Token returnType, Expression body) {
        super(ctx, id.getToken());
        this.id = id;
        this.formals = formals;
        this.returnType = returnType;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assign extends Expression {
    Token name;
    Expression expr;

    Assign(ParserRuleContext ctx, Token token, Token name, Expression expr) {
        super(ctx, token);
        this.name = name;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Id extends Expression {
    private IdSymbol symbol;
    Id(ParserRuleContext ctx, Token token) {
        super(ctx, token);
    }

    @Override
    public IdSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Int extends Expression {
    Int(ParserRuleContext ctx, Token token) {
        super(ctx, token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Str extends Expression {
    Str(ParserRuleContext ctx, Token token) {
        super(ctx, token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Bool extends Expression {
    Bool(ParserRuleContext ctx, Token token) { super(ctx, token); }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    List<Expression> expressions;

    public Block(ParserRuleContext ctx, Token token, List<Expression> expressions) {
        super(ctx, token);
        this.expressions = expressions;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    List<Local> localVars;
    Expression body;

    Let(ParserRuleContext ctx, Token token, List<Local> localVars, Expression body) {
        super(ctx, token);
        this.localVars = localVars;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class If extends Expression {
    Expression cond;
    Expression thenBranch;
    Expression elseBranch;

    If(ParserRuleContext ctx,
       Expression cond,
       Expression thenBranch,
       Expression elseBranch,
       Token start) {
        super(ctx, start);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class While extends Expression {
    Expression cond;
    Expression body;

    While(ParserRuleContext ctx, Token token, Expression cond, Expression body) {
        super(ctx, token);
        this.cond = cond;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class New extends Expression {
    Token type;

    New(ParserRuleContext ctx, Token token, Token type) {
        super(ctx, token);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IsVoid extends Expression {
    Expression expr;

    IsVoid(ParserRuleContext ctx, Token token, Expression expr) {
        super(ctx, token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Paren extends Expression {
    Expression expr;

    public Paren(ParserRuleContext ctx, Token token, Expression expr) {
        super(ctx, token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Expression expr;

    Not(ParserRuleContext ctx, Token token, Expression expr) {
        super(ctx, token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Neg extends Expression {
    Expression expr;

    Neg(ParserRuleContext ctx, Token token, Expression expr) {
        super(ctx, token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class UnaryMinus extends Expression {
    Expression expr;

    UnaryMinus(ParserRuleContext ctx, Token token, Expression expr) {
        super(ctx, token);
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BinaryOp extends Expression {
    String op;
    Expression left;
    Expression right;

    BinaryOp(ParserRuleContext ctx, Token token, Expression left, String op, Expression right) {
        super(ctx, token);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {
    Expression expr;
    List<CaseBranch> branches;

    Case(ParserRuleContext ctx, Token token, Expression expr, List<CaseBranch> branches) {
        super(ctx, token);
        this.expr = expr;
        this.branches = branches;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseBranch extends ASTNode {
    Token name;
    Token type;
    Expression expr;

    CaseBranch(ParserRuleContext ctx, Token token, Token name, Token type, Expression expr) {
        super(ctx, token);
        this.name = name;
        this.type = type;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// ex: f(x, y)
class Dispatch extends Expression {
    Token name;
    List<Expression> args;

    Dispatch(ParserRuleContext ctx, Token token, Token name, List<Expression> args) {
        super(ctx, token);
        this.name = name;
        this.args = args;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Static dispatch: expr@Type.method(args)
class StaticDispatch extends Expression {
    Expression caller;
    Token type;
    Token name;        // method name
    List<Expression> args;

    StaticDispatch(ParserRuleContext ctx, Token token, Expression caller, Token type, Token name, List<Expression> args) {
        super(ctx, token);
        this.caller = caller;
        this.type = type;
        this.name = name;
        this.args = args;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

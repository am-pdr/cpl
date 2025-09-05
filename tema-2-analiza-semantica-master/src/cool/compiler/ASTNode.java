package cool.compiler;
import org.antlr.v4.runtime.Token;
import java.util.*;

public abstract class ASTNode {
    protected Token token;

    ASTNode(Token token) {
        this.token = token;
    }

    Token getToken() {
        return token;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

abstract class Expression extends ASTNode {
    Expression(Token token) {
        super(token);
    }
}

class Feature extends ASTNode {
    Feature(Token token) {
        super(token);
    }
}

class Class extends ASTNode {
    Token type;
    Token inherit;
    LinkedList<Feature> features;

    Class(Token token, Token type, Token inherit, LinkedList<Feature> features) {
        super(token);
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

    Program(Token token, List<Class> classes) {
        super(token);
        this.classes = classes;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends ASTNode {
    Token name;
    Token type;

    Formal(Token token, Token name, Token type) {
        super(token);
        this.type = type;
        this.name = name;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Local extends ASTNode {
    Token name;
    Token type;
    Expression init;

    Local(Token token, Token name, Token type, Expression init) {
        super(token);
        this.name = name;
        this.type = type;
        this.init = init;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Attr extends Feature {
    Token name;
    Token type;
    Expression init;
    Attr(Token token, Token name, Token type, Expression init) {
        super(token);
        this.name = name;
        this.type = type;
        this.init = init;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Method extends Feature {
    Token name;
    List<Formal> formals;
    Token returnType;
    Expression body;

    Method(Token token, Token name, List<Formal> formals, Token returnType, Expression body) {
        super(token);
        this.name = name;
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

    Assign(Token token, Token name, Expression expr) {
        super(token);
        this.name = name;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Id extends Expression {
    Id(Token token) {
        super(token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Int extends Expression {
    Int(Token token) {
        super(token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Str extends Expression {
    Str(Token token) {
        super(token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Bool extends Expression {
    Bool(Token token) { super(token); }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    List<Expression> expressions;

    public Block(Token token, List<Expression> expressions) {
        super(token);
        this.expressions = expressions;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    List<Local> localVars;
    Expression body;

    Let(Token token, List<Local> localVars, Expression body) {
        super(token);
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

    If(Expression cond,
       Expression thenBranch,
       Expression elseBranch,
       Token start) {
        super(start);
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

    While(Token token, Expression cond, Expression body) {
        super(token);
        this.cond = cond;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class New extends Expression {
    Token type;

    New(Token token, Token type) {
        super(token);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IsVoid extends Expression {
    Expression expr;

    IsVoid(Token token, Expression expr) {
        super(token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Paren extends Expression {
    Expression expr;

    public Paren(Token token, Expression expr) {
        super(token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Expression expr;

    Not(Token token, Expression expr) {
        super(token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Neg extends Expression {
    Expression expr;

    Neg(Token token, Expression expr) {
        super(token);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class UnaryMinus extends Expression {
    Expression expr;

    UnaryMinus(Token token, Expression expr) {
        super(token);
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

    BinaryOp(Token token, Expression left, String op, Expression right) {
        super(token);
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

    Case(Token token, Expression expr, List<CaseBranch> branches) {
        super(token);
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

    CaseBranch(Token token, Token name, Token type, Expression expr) {
        super(token);
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

    Dispatch(Token token, Token name, List<Expression> args) {
        super(token);
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

    StaticDispatch(Token token, Expression caller, Token type, Token name, List<Expression> args) {
        super(token);
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

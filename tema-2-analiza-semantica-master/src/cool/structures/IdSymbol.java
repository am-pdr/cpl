package cool.structures;

public class IdSymbol extends Symbol {
    private ClassSymbol type;
    private Scope scope;


    public IdSymbol(String name) {
        super(name);
    }

    public IdSymbol(String name, ClassSymbol type) {
        super(name);
        this.type = type;
    }

    public void setType(ClassSymbol t) { this.type = t; }
    public ClassSymbol getType() { return type; }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}

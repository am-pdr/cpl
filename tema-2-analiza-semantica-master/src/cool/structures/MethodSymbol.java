package cool.structures;
import java.util.*;

public class MethodSymbol extends Symbol implements Scope {
    private final Scope parent;
    private final Map<String, Symbol> parameters = new LinkedHashMap<>();

    public String getReturnType() {
        return returnType;
    }

    public Map<String, Symbol> getParameters() {
        return parameters;
    }

    private final String returnType;

    public MethodSymbol(String name, ClassSymbol parent, returnType) {
        super(name);
        this.parent = parent;
        this.returnType = returnType;
    }

    @Override
    public boolean add(Symbol sym) {
        if (parameters.containsKey(sym.getName()))
            return false;

        parameters.put(sym.getName(), (IdSymbol) sym);
        return true;
    }

    @Override
    public Symbol lookup(String name) {
        var sym = parameters.get(name);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup(name);

        return null;
    }

    @Override
    public cool.structures.Scope getParent() {
        return parent;
    }
}

package cool.structures;
import java.util.*;

public class ClassSymbol extends Symbol implements Scope {
    private final Scope parent;
    private final Map<String, Symbol> members = new LinkedHashMap<>();

    public ClassSymbol(String name, ClassSymbol parent) {
        super(name);
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        if (members.containsKey(sym.getName()))
            return false;

        members.put(sym.getName(), sym);
        return true;
    }

    @Override
    public Symbol lookup(String name) {
        var sym = members.get(name);

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

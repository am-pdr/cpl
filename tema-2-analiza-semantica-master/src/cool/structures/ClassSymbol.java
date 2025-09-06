package cool.structures;
import java.util.*;

public class ClassSymbol extends Symbol implements Scope {
    private Scope parent;
    private String parentName;
    private final Map<String, Symbol> members = new LinkedHashMap<>();

    public ClassSymbol(String name, String parentName) {
        super(name);
        this.parentName = parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentName() {
        return parentName;
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
    public Scope getParent() {
        return parent;
    }

    public boolean hasLocal(String name) {
        return members.containsKey(name);
    }

    public Symbol lookupMethod(String str) {
        if (members.containsKey(str))
            return members.get(str);

        return null;
    }
}

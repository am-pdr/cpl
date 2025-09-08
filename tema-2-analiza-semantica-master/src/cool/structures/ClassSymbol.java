package cool.structures;
import java.util.*;

public class ClassSymbol extends Symbol implements Scope {
    private Scope parent;
    private String parentName;

    private final Map<String, Symbol> attributes = new LinkedHashMap<>();
    private final Map<String, Symbol> methods = new LinkedHashMap<>();

    public ClassSymbol(String name, String parentName) {
        super(name);
        this.parentName = parentName;
    }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public boolean hasAttribute(String name) { return attributes.containsKey(name); }

    public boolean addAttribute(Symbol sym) {
        if (attributes.containsKey(sym.getName())) return false;
        attributes.put(sym.getName(), sym);
        return true;
    }

    @Override
    public boolean add(Symbol sym) {
        return addAttribute(sym);
    }

    @Override
    public Symbol lookup(String name) {
        var sym = attributes.get(name);
        if (sym != null) return sym;
        if (parent != null) return parent.lookup(name);
        return null;
    }

    @Override
    public Scope getParent() { return parent; }

    public Symbol lookupMethod(String name) {
        var m = methods.get(name);
        if (m != null) return m;
        return (parent instanceof ClassSymbol)
                ? ((ClassSymbol) parent).lookupMethod(name)
                : null;
    }

    public boolean addMethod(Symbol sym) {
        if (methods.containsKey(sym.getName())) return false;
        methods.put(sym.getName(), sym);
        return true;
    }
}

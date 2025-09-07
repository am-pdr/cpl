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

    // --- ATRIBUTE ---
    public boolean hasAttribute(String name) { return attributes.containsKey(name); }

    public boolean addAttribute(Symbol sym) {
        if (attributes.containsKey(sym.getName())) return false;
        attributes.put(sym.getName(), sym);
        return true;
    }

    // Scope.add() pentru atribute (variabile de clasa)
    @Override
    public boolean add(Symbol sym) {
        return addAttribute(sym);
    }

    // lookup pentru IDENTIFICATORI (variabile/atribute), nu pentru metode
    @Override
    public Symbol lookup(String name) {
        var sym = attributes.get(name);
        if (sym != null) return sym;
        if (parent != null) return parent.lookup(name);
        return null;
    }

    @Override
    public Scope getParent() { return parent; }

    @Override
    public Scope findScope(String str) { return null; }

    public boolean hasLocal(String name) { return attributes.containsKey(name); }

    // --- METODE ---
    public Symbol lookupMethodLocal(String name) { return methods.get(name); }

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

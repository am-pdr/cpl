package cool.structures;
import java.util.*;

public class MethodSymbol extends Symbol implements Scope {
    private final Scope parent;
    private final Map<String, Symbol> parameters = new LinkedHashMap<>();
    private ClassSymbol returnType;

    public MethodSymbol(String name, Scope parent) {
        super(name);
        this.parent = parent;
    }

    public MethodSymbol(String name, Scope parent, String returnType) {
        super(name);
        this.parent = parent;
        this.returnType = returnType;
    }

    public String getReturnType() {
        return returnType;
    }

    public Map<String, Symbol> getParameters() {
        return parameters;
    }

    public void setReturnType(String returnType) {
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

    public Symbol hasSymbol(String str) {

        if (parameters.containsKey(str))
            return parameters.get(str);

        return null;
    }

    public String compare(MethodSymbol other) {

        if (this.parameters.size() != other.parameters.size()) {
            return "with different number of formal parameters";
        }

        // Parcurgem in paralel parametrii pentru comparare
        List<String> thisParamNames = new ArrayList<>(this.parameters.keySet());
        List<String> otherParamNames = new ArrayList<>(other.parameters.keySet());

        for (int i = 0; i < thisParamNames.size(); i++) {
            String paramName1 = thisParamNames.get(i);
            String paramName2 = otherParamNames.get(i);

            // Obținem simbolurile parametrilor
            IdSymbol thisParam = (IdSymbol) this.parameters.get(paramName1);
            IdSymbol otherParam = (IdSymbol) other.parameters.get(paramName2);

            // Comparam tipurile parametrilor
            if (thisParam.getType() != null && otherParam.getType() != null) {
                if (!(thisParam.getType().getName().equals(otherParam.getType().getName()))) {
                    return paramName2 + " " + thisParam.getType().getName() + " " + otherParam.getType().getName();
                }
            }
        }

        return "";
    }
}

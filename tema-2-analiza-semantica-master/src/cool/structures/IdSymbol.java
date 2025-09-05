package cool.structures;

public class IdSymbol extends Symbol {
    private String type;

    public IdSymbol(String name, String type) {
        super(name);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

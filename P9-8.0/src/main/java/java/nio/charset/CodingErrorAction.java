package java.nio.charset;

public class CodingErrorAction {
    public static final CodingErrorAction IGNORE = new CodingErrorAction("IGNORE");
    public static final CodingErrorAction REPLACE = new CodingErrorAction("REPLACE");
    public static final CodingErrorAction REPORT = new CodingErrorAction("REPORT");
    private String name;

    private CodingErrorAction(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}

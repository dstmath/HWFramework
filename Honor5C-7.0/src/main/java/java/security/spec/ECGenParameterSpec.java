package java.security.spec;

public class ECGenParameterSpec implements AlgorithmParameterSpec {
    private String name;

    public ECGenParameterSpec(String stdName) {
        if (stdName == null) {
            throw new NullPointerException("stdName is null");
        }
        this.name = stdName;
    }

    public String getName() {
        return this.name;
    }
}

package java.security.spec;

public class ECGenParameterSpec implements AlgorithmParameterSpec {
    private String name;

    public ECGenParameterSpec(String stdName) {
        if (stdName != null) {
            this.name = stdName;
            return;
        }
        throw new NullPointerException("stdName is null");
    }

    public String getName() {
        return this.name;
    }
}

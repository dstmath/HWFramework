package ohos.javax.xml.validation;

public final class SchemaFactoryConfigurationError extends Error {
    static final long serialVersionUID = 3531438703147750126L;

    public SchemaFactoryConfigurationError() {
    }

    public SchemaFactoryConfigurationError(String str) {
        super(str);
    }

    public SchemaFactoryConfigurationError(Throwable th) {
        super(th);
    }

    public SchemaFactoryConfigurationError(String str, Throwable th) {
        super(str, th);
    }
}

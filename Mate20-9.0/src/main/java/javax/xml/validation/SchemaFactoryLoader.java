package javax.xml.validation;

public abstract class SchemaFactoryLoader {
    public abstract SchemaFactory newFactory(String str);

    protected SchemaFactoryLoader() {
    }
}

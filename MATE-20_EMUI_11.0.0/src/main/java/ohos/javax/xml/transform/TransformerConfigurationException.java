package ohos.javax.xml.transform;

public class TransformerConfigurationException extends TransformerException {
    private static final long serialVersionUID = 1285547467942875745L;

    public TransformerConfigurationException() {
        super("Configuration Error");
    }

    public TransformerConfigurationException(String str) {
        super(str);
    }

    public TransformerConfigurationException(Throwable th) {
        super(th);
    }

    public TransformerConfigurationException(String str, Throwable th) {
        super(str, th);
    }

    public TransformerConfigurationException(String str, SourceLocator sourceLocator) {
        super(str, sourceLocator);
    }

    public TransformerConfigurationException(String str, SourceLocator sourceLocator, Throwable th) {
        super(str, sourceLocator, th);
    }
}

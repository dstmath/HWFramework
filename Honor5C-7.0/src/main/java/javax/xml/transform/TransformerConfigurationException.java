package javax.xml.transform;

public class TransformerConfigurationException extends TransformerException {
    public TransformerConfigurationException() {
        super("Configuration Error");
    }

    public TransformerConfigurationException(String msg) {
        super(msg);
    }

    public TransformerConfigurationException(Throwable e) {
        super(e);
    }

    public TransformerConfigurationException(String msg, Throwable e) {
        super(msg, e);
    }

    public TransformerConfigurationException(String message, SourceLocator locator) {
        super(message, locator);
    }

    public TransformerConfigurationException(String message, SourceLocator locator, Throwable e) {
        super(message, locator, e);
    }
}

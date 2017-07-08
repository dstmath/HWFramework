package javax.xml.transform;

public interface SourceLocator {
    int getColumnNumber();

    int getLineNumber();

    String getPublicId();

    String getSystemId();
}

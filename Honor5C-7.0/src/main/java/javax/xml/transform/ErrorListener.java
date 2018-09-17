package javax.xml.transform;

public interface ErrorListener {
    void error(TransformerException transformerException) throws TransformerException;

    void fatalError(TransformerException transformerException) throws TransformerException;

    void warning(TransformerException transformerException) throws TransformerException;
}

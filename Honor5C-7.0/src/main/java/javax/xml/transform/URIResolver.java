package javax.xml.transform;

public interface URIResolver {
    Source resolve(String str, String str2) throws TransformerException;
}

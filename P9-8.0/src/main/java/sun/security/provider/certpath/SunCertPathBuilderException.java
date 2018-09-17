package sun.security.provider.certpath;

import java.security.cert.CertPathBuilderException;

public class SunCertPathBuilderException extends CertPathBuilderException {
    private static final long serialVersionUID = -7814288414129264709L;
    private transient AdjacencyList adjList;

    public SunCertPathBuilderException(String msg) {
        super(msg);
    }

    public SunCertPathBuilderException(Throwable cause) {
        super(cause);
    }

    public SunCertPathBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }

    SunCertPathBuilderException(String msg, AdjacencyList adjList) {
        this(msg);
        this.adjList = adjList;
    }

    SunCertPathBuilderException(String msg, Throwable cause, AdjacencyList adjList) {
        this(msg, cause);
        this.adjList = adjList;
    }

    public AdjacencyList getAdjacencyList() {
        return this.adjList;
    }
}

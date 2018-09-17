package sun.security.provider.certpath;

import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import sun.security.util.Debug;

public class SunCertPathBuilderResult extends PKIXCertPathBuilderResult {
    private static final Debug debug = Debug.getInstance("certpath");
    private AdjacencyList adjList;

    SunCertPathBuilderResult(CertPath certPath, TrustAnchor trustAnchor, PolicyNode policyTree, PublicKey subjectPublicKey, AdjacencyList adjList) {
        super(certPath, trustAnchor, policyTree, subjectPublicKey);
        this.adjList = adjList;
    }

    public AdjacencyList getAdjacencyList() {
        return this.adjList;
    }
}

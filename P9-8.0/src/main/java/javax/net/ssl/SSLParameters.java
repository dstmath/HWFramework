package javax.net.ssl;

import java.security.AlgorithmConstraints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SSLParameters {
    private AlgorithmConstraints algorithmConstraints;
    private String[] cipherSuites;
    private String identificationAlgorithm;
    private boolean needClientAuth;
    private boolean preferLocalCipherSuites;
    private String[] protocols;
    private Map<Integer, SNIMatcher> sniMatchers = null;
    private Map<Integer, SNIServerName> sniNames = null;
    private boolean wantClientAuth;

    public SSLParameters(String[] cipherSuites) {
        setCipherSuites(cipherSuites);
    }

    public SSLParameters(String[] cipherSuites, String[] protocols) {
        setCipherSuites(cipherSuites);
        setProtocols(protocols);
    }

    private static String[] clone(String[] s) {
        return s == null ? null : (String[]) s.clone();
    }

    public String[] getCipherSuites() {
        return clone(this.cipherSuites);
    }

    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = clone(cipherSuites);
    }

    public String[] getProtocols() {
        return clone(this.protocols);
    }

    public void setProtocols(String[] protocols) {
        this.protocols = clone(protocols);
    }

    public boolean getWantClientAuth() {
        return this.wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
        this.needClientAuth = false;
    }

    public boolean getNeedClientAuth() {
        return this.needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.wantClientAuth = false;
        this.needClientAuth = needClientAuth;
    }

    public AlgorithmConstraints getAlgorithmConstraints() {
        return this.algorithmConstraints;
    }

    public void setAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.algorithmConstraints = constraints;
    }

    public String getEndpointIdentificationAlgorithm() {
        return this.identificationAlgorithm;
    }

    public void setEndpointIdentificationAlgorithm(String algorithm) {
        this.identificationAlgorithm = algorithm;
    }

    public final void setServerNames(List<SNIServerName> serverNames) {
        if (serverNames == null) {
            this.sniNames = null;
        } else if (serverNames.isEmpty()) {
            this.sniNames = Collections.emptyMap();
        } else {
            this.sniNames = new LinkedHashMap(serverNames.size());
            for (SNIServerName serverName : serverNames) {
                if (this.sniNames.put(Integer.valueOf(serverName.getType()), serverName) != null) {
                    throw new IllegalArgumentException("Duplicated server name of type " + serverName.getType());
                }
            }
        }
    }

    public final List<SNIServerName> getServerNames() {
        if (this.sniNames == null) {
            return null;
        }
        if (this.sniNames.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList(this.sniNames.values()));
    }

    public final void setSNIMatchers(Collection<SNIMatcher> matchers) {
        if (matchers == null) {
            this.sniMatchers = null;
        } else if (matchers.isEmpty()) {
            this.sniMatchers = Collections.emptyMap();
        } else {
            this.sniMatchers = new HashMap(matchers.size());
            for (SNIMatcher matcher : matchers) {
                if (this.sniMatchers.put(Integer.valueOf(matcher.getType()), matcher) != null) {
                    throw new IllegalArgumentException("Duplicated server name of type " + matcher.getType());
                }
            }
        }
    }

    public final Collection<SNIMatcher> getSNIMatchers() {
        if (this.sniMatchers == null) {
            return null;
        }
        if (this.sniMatchers.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList(this.sniMatchers.values()));
    }

    public final void setUseCipherSuitesOrder(boolean honorOrder) {
        this.preferLocalCipherSuites = honorOrder;
    }

    public final boolean getUseCipherSuitesOrder() {
        return this.preferLocalCipherSuites;
    }
}

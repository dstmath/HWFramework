package java.security.cert;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class PKIXRevocationChecker extends PKIXCertPathChecker {
    private List<Extension> ocspExtensions = Collections.emptyList();
    private URI ocspResponder;
    private X509Certificate ocspResponderCert;
    private Map<X509Certificate, byte[]> ocspResponses = Collections.emptyMap();
    private Set<Option> options = Collections.emptySet();

    public enum Option {
        ONLY_END_ENTITY,
        PREFER_CRLS,
        NO_FALLBACK,
        SOFT_FAIL
    }

    public abstract List<CertPathValidatorException> getSoftFailExceptions();

    protected PKIXRevocationChecker() {
    }

    public void setOcspResponder(URI uri) {
        this.ocspResponder = uri;
    }

    public URI getOcspResponder() {
        return this.ocspResponder;
    }

    public void setOcspResponderCert(X509Certificate cert) {
        this.ocspResponderCert = cert;
    }

    public X509Certificate getOcspResponderCert() {
        return this.ocspResponderCert;
    }

    public void setOcspExtensions(List<Extension> extensions) {
        List emptyList;
        if (extensions == null) {
            emptyList = Collections.emptyList();
        } else {
            emptyList = new ArrayList((Collection) extensions);
        }
        this.ocspExtensions = emptyList;
    }

    public List<Extension> getOcspExtensions() {
        return Collections.unmodifiableList(this.ocspExtensions);
    }

    public void setOcspResponses(Map<X509Certificate, byte[]> responses) {
        if (responses == null) {
            this.ocspResponses = Collections.emptyMap();
            return;
        }
        Map<X509Certificate, byte[]> copy = new HashMap(responses.size());
        for (Entry<X509Certificate, byte[]> e : responses.entrySet()) {
            copy.put((X509Certificate) e.getKey(), (byte[]) ((byte[]) e.getValue()).clone());
        }
        this.ocspResponses = copy;
    }

    public Map<X509Certificate, byte[]> getOcspResponses() {
        Map<X509Certificate, byte[]> copy = new HashMap(this.ocspResponses.size());
        for (Entry<X509Certificate, byte[]> e : this.ocspResponses.entrySet()) {
            copy.put((X509Certificate) e.getKey(), (byte[]) ((byte[]) e.getValue()).clone());
        }
        return copy;
    }

    public void setOptions(Set<Option> options) {
        Set emptySet;
        if (options == null) {
            emptySet = Collections.emptySet();
        } else {
            emptySet = new HashSet((Collection) options);
        }
        this.options = emptySet;
    }

    public Set<Option> getOptions() {
        return Collections.unmodifiableSet(this.options);
    }

    public PKIXRevocationChecker clone() {
        PKIXRevocationChecker copy = (PKIXRevocationChecker) super.clone();
        copy.ocspExtensions = new ArrayList(this.ocspExtensions);
        copy.ocspResponses = new HashMap(this.ocspResponses);
        for (Entry<X509Certificate, byte[]> entry : copy.ocspResponses.entrySet()) {
            entry.setValue((byte[]) ((byte[]) entry.getValue()).clone());
        }
        copy.options = new HashSet(this.options);
        return copy;
    }
}

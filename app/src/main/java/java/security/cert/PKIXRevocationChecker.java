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
    private List<Extension> ocspExtensions;
    private URI ocspResponder;
    private X509Certificate ocspResponderCert;
    private Map<X509Certificate, byte[]> ocspResponses;
    private Set<Option> options;

    public enum Option {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.cert.PKIXRevocationChecker.Option.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.cert.PKIXRevocationChecker.Option.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.security.cert.PKIXRevocationChecker.Option.<clinit>():void");
        }
    }

    public abstract List<CertPathValidatorException> getSoftFailExceptions();

    protected PKIXRevocationChecker() {
        this.ocspExtensions = Collections.emptyList();
        this.ocspResponses = Collections.emptyMap();
        this.options = Collections.emptySet();
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

    public /* bridge */ /* synthetic */ Object clone() {
        return clone();
    }

    public PKIXRevocationChecker m7clone() {
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

package sun.security.x509;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class RDN {
    final AVA[] assertion;
    private volatile List<AVA> avaList;
    private volatile String canonicalString;

    public RDN(String name) throws IOException {
        this(name, (Map<String, String>) Collections.emptyMap());
    }

    public RDN(String name, Map<String, String> keywordMap) throws IOException {
        int quoteCount = 0;
        int searchOffset = 0;
        int avaOffset = 0;
        List<AVA> avaVec = new ArrayList<>(3);
        int nextPlus = name.indexOf(43);
        while (nextPlus >= 0) {
            quoteCount += X500Name.countQuotes(name, searchOffset, nextPlus);
            if (!(nextPlus <= 0 || name.charAt(nextPlus - 1) == '\\' || quoteCount == 1)) {
                String avaString = name.substring(avaOffset, nextPlus);
                if (avaString.length() != 0) {
                    avaVec.add(new AVA((Reader) new StringReader(avaString), keywordMap));
                    avaOffset = nextPlus + 1;
                    quoteCount = 0;
                } else {
                    throw new IOException("empty AVA in RDN \"" + name + "\"");
                }
            }
            searchOffset = nextPlus + 1;
            nextPlus = name.indexOf(43, searchOffset);
        }
        String avaString2 = name.substring(avaOffset);
        if (avaString2.length() != 0) {
            avaVec.add(new AVA((Reader) new StringReader(avaString2), keywordMap));
            this.assertion = (AVA[]) avaVec.toArray(new AVA[avaVec.size()]);
            return;
        }
        throw new IOException("empty AVA in RDN \"" + name + "\"");
    }

    RDN(String name, String format) throws IOException {
        this(name, format, Collections.emptyMap());
    }

    RDN(String name, String format, Map<String, String> keywordMap) throws IOException {
        if (format.equalsIgnoreCase(X500Principal.RFC2253)) {
            int avaOffset = 0;
            List<AVA> avaVec = new ArrayList<>(3);
            int nextPlus = name.indexOf(43);
            while (nextPlus >= 0) {
                if (nextPlus > 0 && name.charAt(nextPlus - 1) != '\\') {
                    String avaString = name.substring(avaOffset, nextPlus);
                    if (avaString.length() != 0) {
                        avaVec.add(new AVA(new StringReader(avaString), 3, keywordMap));
                        avaOffset = nextPlus + 1;
                    } else {
                        throw new IOException("empty AVA in RDN \"" + name + "\"");
                    }
                }
                nextPlus = name.indexOf(43, nextPlus + 1);
            }
            String avaString2 = name.substring(avaOffset);
            if (avaString2.length() != 0) {
                avaVec.add(new AVA(new StringReader(avaString2), 3, keywordMap));
                this.assertion = (AVA[]) avaVec.toArray(new AVA[avaVec.size()]);
                return;
            }
            throw new IOException("empty AVA in RDN \"" + name + "\"");
        }
        throw new IOException("Unsupported format " + format);
    }

    RDN(DerValue rdn) throws IOException {
        if (rdn.tag == 49) {
            DerValue[] avaset = new DerInputStream(rdn.toByteArray()).getSet(5);
            this.assertion = new AVA[avaset.length];
            for (int i = 0; i < avaset.length; i++) {
                this.assertion[i] = new AVA(avaset[i]);
            }
            return;
        }
        throw new IOException("X500 RDN");
    }

    RDN(int i) {
        this.assertion = new AVA[i];
    }

    public RDN(AVA ava) {
        if (ava != null) {
            this.assertion = new AVA[]{ava};
            return;
        }
        throw new NullPointerException();
    }

    public RDN(AVA[] avas) {
        this.assertion = (AVA[]) avas.clone();
        int i = 0;
        while (i < this.assertion.length) {
            if (this.assertion[i] != null) {
                i++;
            } else {
                throw new NullPointerException();
            }
        }
    }

    public List<AVA> avas() {
        List<AVA> list = this.avaList;
        if (list != null) {
            return list;
        }
        List<AVA> list2 = Collections.unmodifiableList(Arrays.asList(this.assertion));
        this.avaList = list2;
        return list2;
    }

    public int size() {
        return this.assertion.length;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RDN)) {
            return false;
        }
        RDN other = (RDN) obj;
        if (this.assertion.length != other.assertion.length) {
            return false;
        }
        return toRFC2253String(true).equals(other.toRFC2253String(true));
    }

    public int hashCode() {
        return toRFC2253String(true).hashCode();
    }

    /* access modifiers changed from: package-private */
    public DerValue findAttribute(ObjectIdentifier oid) {
        for (int i = 0; i < this.assertion.length; i++) {
            if (this.assertion[i].oid.equals((Object) oid)) {
                return this.assertion[i].value;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void encode(DerOutputStream out) throws IOException {
        out.putOrderedSetOf((byte) 49, this.assertion);
    }

    public String toString() {
        if (this.assertion.length == 1) {
            return this.assertion[0].toString();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.assertion.length; i++) {
            if (i != 0) {
                sb.append(" + ");
            }
            sb.append(this.assertion[i].toString());
        }
        return sb.toString();
    }

    public String toRFC1779String() {
        return toRFC1779String(Collections.emptyMap());
    }

    public String toRFC1779String(Map<String, String> oidMap) {
        if (this.assertion.length == 1) {
            return this.assertion[0].toRFC1779String(oidMap);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.assertion.length; i++) {
            if (i != 0) {
                sb.append(" + ");
            }
            sb.append(this.assertion[i].toRFC1779String(oidMap));
        }
        return sb.toString();
    }

    public String toRFC2253String() {
        return toRFC2253StringInternal(false, Collections.emptyMap());
    }

    public String toRFC2253String(Map<String, String> oidMap) {
        return toRFC2253StringInternal(false, oidMap);
    }

    public String toRFC2253String(boolean canonical) {
        if (!canonical) {
            return toRFC2253StringInternal(false, Collections.emptyMap());
        }
        String c = this.canonicalString;
        if (c == null) {
            c = toRFC2253StringInternal(true, Collections.emptyMap());
            this.canonicalString = c;
        }
        return c;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: sun.security.x509.AVA[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    private String toRFC2253StringInternal(boolean canonical, Map<String, String> oidMap) {
        String str;
        String str2;
        if (this.assertion.length == 1) {
            if (canonical) {
                str2 = this.assertion[0].toRFC2253CanonicalString();
            } else {
                str2 = this.assertion[0].toRFC2253String(oidMap);
            }
            return str2;
        }
        AVA[] toOutput = this.assertion;
        if (canonical) {
            toOutput = this.assertion.clone();
            Arrays.sort(toOutput, AVAComparator.getInstance());
        }
        StringJoiner sj = new StringJoiner("+");
        for (AVA ava : toOutput) {
            if (canonical) {
                str = ava.toRFC2253CanonicalString();
            } else {
                str = ava.toRFC2253String(oidMap);
            }
            sj.add(str);
        }
        return sj.toString();
    }
}

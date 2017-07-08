package java.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import sun.security.util.Debug;
import sun.security.util.ManifestDigester;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;

class JarVerifier {
    static final Debug debug = null;
    private boolean anyToVerify;
    private ByteArrayOutputStream baos;
    private Object csdomain;
    boolean eagerValidation;
    private Enumeration emptyEnumeration;
    private CodeSigner[] emptySigner;
    private List jarCodeSigners;
    private URL lastURL;
    private Map lastURLMap;
    private volatile ManifestDigester manDig;
    private List manifestDigests;
    byte[] manifestRawBytes;
    private boolean parsingBlockOrSF;
    private boolean parsingMeta;
    private ArrayList pendingBlocks;
    private Hashtable sigFileData;
    private Hashtable sigFileSigners;
    private ArrayList signerCache;
    private Map signerMap;
    private Map signerToCodeSource;
    private Map urlToCodeSourceMap;
    private Hashtable verifiedSigners;

    /* renamed from: java.util.jar.JarVerifier.2 */
    class AnonymousClass2 implements Enumeration<String> {
        String name;
        final /* synthetic */ Enumeration val$enum2;
        final /* synthetic */ Iterator val$itor;
        final /* synthetic */ List val$signersReq;

        AnonymousClass2(Iterator val$itor, List val$signersReq, Enumeration val$enum2) {
            this.val$itor = val$itor;
            this.val$signersReq = val$signersReq;
            this.val$enum2 = val$enum2;
        }

        public boolean hasMoreElements() {
            if (this.name != null) {
                return true;
            }
            while (this.val$itor.hasNext()) {
                Entry e = (Entry) this.val$itor.next();
                if (this.val$signersReq.contains((CodeSigner[]) e.getValue())) {
                    this.name = (String) e.getKey();
                    return true;
                }
            }
            if (!this.val$enum2.hasMoreElements()) {
                return false;
            }
            this.name = (String) this.val$enum2.nextElement();
            return true;
        }

        public String nextElement() {
            if (hasMoreElements()) {
                String value = this.name;
                this.name = null;
                return value;
            }
            throw new NoSuchElementException();
        }
    }

    /* renamed from: java.util.jar.JarVerifier.3 */
    class AnonymousClass3 implements Enumeration<JarEntry> {
        JarEntry entry;
        Enumeration signers;
        final /* synthetic */ Enumeration val$enum_;
        final /* synthetic */ JarFile val$jar;
        final /* synthetic */ Map val$map;

        AnonymousClass3(Enumeration val$enum_, JarFile val$jar, Map val$map) {
            this.val$enum_ = val$enum_;
            this.val$jar = val$jar;
            this.val$map = val$map;
            this.signers = null;
        }

        public boolean hasMoreElements() {
            if (this.entry != null) {
                return true;
            }
            while (this.val$enum_.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) this.val$enum_.nextElement();
                if (!JarVerifier.isSigningRelated(ze.getName())) {
                    this.entry = this.val$jar.newEntry(ze);
                    return true;
                }
            }
            if (this.signers == null) {
                this.signers = Collections.enumeration(this.val$map.keySet());
            }
            if (!this.signers.hasMoreElements()) {
                return false;
            }
            this.entry = this.val$jar.newEntry(new ZipEntry((String) this.signers.nextElement()));
            return true;
        }

        public JarEntry nextElement() {
            if (hasMoreElements()) {
                JarEntry je = this.entry;
                this.val$map.remove(je.getName());
                this.entry = null;
                return je;
            }
            throw new NoSuchElementException();
        }
    }

    /* renamed from: java.util.jar.JarVerifier.4 */
    class AnonymousClass4 implements Enumeration<String> {
        String name;
        final /* synthetic */ Enumeration val$entries;
        final /* synthetic */ Map val$map;

        AnonymousClass4(Enumeration val$entries, Map val$map) {
            this.val$entries = val$entries;
            this.val$map = val$map;
        }

        public boolean hasMoreElements() {
            if (this.name != null) {
                return true;
            }
            while (this.val$entries.hasMoreElements()) {
                ZipEntry e = (ZipEntry) this.val$entries.nextElement();
                String value = e.getName();
                if (!e.isDirectory() && !JarVerifier.isSigningRelated(value) && this.val$map.get(value) == null) {
                    this.name = value;
                    return true;
                }
            }
            return false;
        }

        public String nextElement() {
            if (hasMoreElements()) {
                String value = this.name;
                this.name = null;
                return value;
            }
            throw new NoSuchElementException();
        }
    }

    private static class VerifierCodeSource extends CodeSource {
        Object csdomain;
        Certificate[] vcerts;
        URL vlocation;
        CodeSigner[] vsigners;

        VerifierCodeSource(Object csdomain, URL location, CodeSigner[] signers) {
            super(location, signers);
            this.csdomain = csdomain;
            this.vlocation = location;
            this.vsigners = signers;
        }

        VerifierCodeSource(Object csdomain, URL location, Certificate[] certs) {
            super(location, certs);
            this.csdomain = csdomain;
            this.vlocation = location;
            this.vcerts = certs;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof VerifierCodeSource) {
                VerifierCodeSource that = (VerifierCodeSource) obj;
                if (isSameDomain(that.csdomain)) {
                    if (that.vsigners != this.vsigners || that.vcerts != this.vcerts) {
                        return false;
                    }
                    if (that.vlocation != null) {
                        return that.vlocation.equals(this.vlocation);
                    }
                    if (this.vlocation != null) {
                        return this.vlocation.equals(that.vlocation);
                    }
                    return true;
                }
            }
            return super.equals(obj);
        }

        boolean isSameDomain(Object csdomain) {
            return this.csdomain == csdomain;
        }

        private CodeSigner[] getPrivateSigners() {
            return this.vsigners;
        }

        private Certificate[] getPrivateCertificates() {
            return this.vcerts;
        }
    }

    static class VerifierStream extends InputStream {
        private InputStream is;
        private JarVerifier jv;
        private ManifestEntryVerifier mev;
        private long numLeft;

        VerifierStream(Manifest man, JarEntry je, InputStream is, JarVerifier jv) throws IOException {
            if (is == null) {
                throw new NullPointerException("is == null");
            }
            this.is = is;
            this.jv = jv;
            this.mev = new ManifestEntryVerifier(man);
            this.jv.beginEntry(je, this.mev);
            this.numLeft = je.getSize();
            if (this.numLeft == 0) {
                this.jv.update(-1, this.mev);
            }
        }

        public int read() throws IOException {
            if (this.is == null) {
                throw new IOException("stream closed");
            } else if (this.numLeft <= 0) {
                return -1;
            } else {
                int b = this.is.read();
                this.jv.update(b, this.mev);
                this.numLeft--;
                if (this.numLeft == 0) {
                    this.jv.update(-1, this.mev);
                }
                return b;
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.is == null) {
                throw new IOException("stream closed");
            }
            if (this.numLeft > 0 && this.numLeft < ((long) len)) {
                len = (int) this.numLeft;
            }
            if (this.numLeft <= 0) {
                return -1;
            }
            int n = this.is.read(b, off, len);
            this.jv.update(n, b, off, len, this.mev);
            this.numLeft -= (long) n;
            if (this.numLeft == 0) {
                this.jv.update(-1, b, off, len, this.mev);
            }
            return n;
        }

        public void close() throws IOException {
            if (this.is != null) {
                this.is.close();
            }
            this.is = null;
            this.mev = null;
            this.jv = null;
        }

        public int available() throws IOException {
            if (this.is != null) {
                return this.is.available();
            }
            throw new IOException("stream closed");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.jar.JarVerifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.jar.JarVerifier.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.<clinit>():void");
    }

    public JarVerifier(byte[] rawBytes) {
        this.parsingBlockOrSF = false;
        this.parsingMeta = true;
        this.anyToVerify = true;
        this.manifestRawBytes = null;
        this.csdomain = new Object();
        this.urlToCodeSourceMap = new HashMap();
        this.signerToCodeSource = new HashMap();
        this.emptySigner = new CodeSigner[0];
        this.emptyEnumeration = new Enumeration<String>() {
            public boolean hasMoreElements() {
                return false;
            }

            public String nextElement() {
                throw new NoSuchElementException();
            }
        };
        this.manifestRawBytes = rawBytes;
        this.sigFileSigners = new Hashtable();
        this.verifiedSigners = new Hashtable();
        this.sigFileData = new Hashtable(11);
        this.pendingBlocks = new ArrayList();
        this.baos = new ByteArrayOutputStream();
        this.manifestDigests = new ArrayList();
    }

    public void beginEntry(JarEntry je, ManifestEntryVerifier mev) throws IOException {
        if (je != null) {
            if (debug != null) {
                debug.println("beginEntry " + je.getName());
            }
            String name = je.getName();
            if (this.parsingMeta) {
                String uname = name.toUpperCase(Locale.ENGLISH);
                if (uname.startsWith("META-INF/") || uname.startsWith("/META-INF/")) {
                    if (je.isDirectory()) {
                        mev.setEntry(null, je);
                        return;
                    }
                    if (SignatureFileVerifier.isBlockOrSF(uname)) {
                        this.parsingBlockOrSF = true;
                        this.baos.reset();
                        mev.setEntry(null, je);
                    }
                    return;
                }
            }
            if (this.parsingMeta) {
                doneWithMeta();
            }
            if (je.isDirectory()) {
                mev.setEntry(null, je);
                return;
            }
            if (name.startsWith("./")) {
                name = name.substring(2);
            }
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (this.sigFileSigners.get(name) != null) {
                mev.setEntry(name, je);
            } else {
                mev.setEntry(null, je);
            }
        }
    }

    public void update(int b, ManifestEntryVerifier mev) throws IOException {
        if (b == -1) {
            processEntry(mev);
        } else if (this.parsingBlockOrSF) {
            this.baos.write(b);
        } else {
            mev.update((byte) b);
        }
    }

    public void update(int n, byte[] b, int off, int len, ManifestEntryVerifier mev) throws IOException {
        if (n == -1) {
            processEntry(mev);
        } else if (this.parsingBlockOrSF) {
            this.baos.write(b, off, n);
        } else {
            mev.update(b, off, n);
        }
    }

    private void processEntry(ManifestEntryVerifier mev) throws IOException {
        if (this.parsingBlockOrSF) {
            try {
                this.parsingBlockOrSF = false;
                if (debug != null) {
                    debug.println("processEntry: processing block");
                }
                String uname = mev.getEntry().getName().toUpperCase(Locale.ENGLISH);
                String key;
                byte[] bytes;
                SignatureFileVerifier sfv;
                if (uname.endsWith(".SF")) {
                    key = uname.substring(0, uname.length() - 3);
                    bytes = this.baos.toByteArray();
                    this.sigFileData.put(key, bytes);
                    Iterator it = this.pendingBlocks.iterator();
                    while (it.hasNext()) {
                        sfv = (SignatureFileVerifier) it.next();
                        if (sfv.needSignatureFile(key)) {
                            if (debug != null) {
                                debug.println("processEntry: processing pending block");
                            }
                            sfv.setSignatureFile(bytes);
                            sfv.process(this.sigFileSigners, this.manifestDigests);
                        }
                    }
                    return;
                }
                key = uname.substring(0, uname.lastIndexOf("."));
                if (this.signerCache == null) {
                    this.signerCache = new ArrayList();
                }
                if (this.manDig == null) {
                    synchronized (this.manifestRawBytes) {
                        if (this.manDig == null) {
                            this.manDig = new ManifestDigester(this.manifestRawBytes);
                            this.manifestRawBytes = null;
                        }
                    }
                }
                sfv = new SignatureFileVerifier(this.signerCache, this.manDig, uname, this.baos.toByteArray());
                if (sfv.needSignatureFileBytes()) {
                    bytes = (byte[]) this.sigFileData.get(key);
                    if (bytes == null) {
                        if (debug != null) {
                            debug.println("adding pending block");
                        }
                        this.pendingBlocks.add(sfv);
                        return;
                    }
                    sfv.setSignatureFile(bytes);
                }
                sfv.process(this.sigFileSigners, this.manifestDigests);
            } catch (Object ioe) {
                if (debug != null) {
                    debug.println("processEntry caught: " + ioe);
                }
            } catch (Object se) {
                if (debug != null) {
                    debug.println("processEntry caught: " + se);
                }
            } catch (Object nsae) {
                if (debug != null) {
                    debug.println("processEntry caught: " + nsae);
                }
            } catch (Object ce) {
                if (debug != null) {
                    debug.println("processEntry caught: " + ce);
                }
            }
        } else {
            JarEntry je = mev.getEntry();
            if (je != null && je.signers == null) {
                je.signers = mev.verify(this.verifiedSigners, this.sigFileSigners);
                je.certs = mapSignersToCertArray(je.signers);
            }
        }
    }

    @Deprecated
    public Certificate[] getCerts(String name) {
        return mapSignersToCertArray(getCodeSigners(name));
    }

    public Certificate[] getCerts(JarFile jar, JarEntry entry) {
        return mapSignersToCertArray(getCodeSigners(jar, entry));
    }

    public CodeSigner[] getCodeSigners(String name) {
        return (CodeSigner[]) this.verifiedSigners.get(name);
    }

    public CodeSigner[] getCodeSigners(JarFile jar, JarEntry entry) {
        String name = entry.getName();
        if (this.eagerValidation && this.sigFileSigners.get(name) != null) {
            try {
                InputStream s = jar.getInputStream(entry);
                byte[] buffer = new byte[Record.maxExpansion];
                for (int n = buffer.length; n != -1; n = s.read(buffer, 0, buffer.length)) {
                }
                s.close();
            } catch (IOException e) {
            }
        }
        return getCodeSigners(name);
    }

    private static Certificate[] mapSignersToCertArray(CodeSigner[] signers) {
        if (signers == null) {
            return null;
        }
        ArrayList certChains = new ArrayList();
        for (CodeSigner signerCertPath : signers) {
            certChains.addAll(signerCertPath.getSignerCertPath().getCertificates());
        }
        return (Certificate[]) certChains.toArray(new Certificate[certChains.size()]);
    }

    boolean nothingToVerify() {
        return !this.anyToVerify;
    }

    void doneWithMeta() {
        boolean z = false;
        this.parsingMeta = false;
        if (!this.sigFileSigners.isEmpty()) {
            z = true;
        }
        this.anyToVerify = z;
        this.baos = null;
        this.sigFileData = null;
        this.pendingBlocks = null;
        this.signerCache = null;
        this.manDig = null;
        if (this.sigFileSigners.containsKey(JarFile.MANIFEST_NAME)) {
            this.verifiedSigners.put(JarFile.MANIFEST_NAME, this.sigFileSigners.remove(JarFile.MANIFEST_NAME));
        }
    }

    private synchronized CodeSource mapSignersToCodeSource(URL url, CodeSigner[] signers) {
        CodeSource cs;
        Map map;
        if (url == this.lastURL) {
            map = this.lastURLMap;
        } else {
            map = (Map) this.urlToCodeSourceMap.get(url);
            if (map == null) {
                map = new HashMap();
                this.urlToCodeSourceMap.put(url, map);
            }
            this.lastURLMap = map;
            this.lastURL = url;
        }
        cs = (CodeSource) map.get(signers);
        if (cs == null) {
            cs = new VerifierCodeSource(this.csdomain, url, signers);
            this.signerToCodeSource.put(signers, cs);
        }
        return cs;
    }

    private CodeSource[] mapSignersToCodeSources(URL url, List signers, boolean unsigned) {
        List sources = new ArrayList();
        for (int i = 0; i < signers.size(); i++) {
            sources.add(mapSignersToCodeSource(url, (CodeSigner[]) signers.get(i)));
        }
        if (unsigned) {
            sources.add(mapSignersToCodeSource(url, null));
        }
        return (CodeSource[]) sources.toArray(new CodeSource[sources.size()]);
    }

    private CodeSigner[] findMatchingSigners(CodeSource cs) {
        if ((cs instanceof VerifierCodeSource) && ((VerifierCodeSource) cs).isSameDomain(this.csdomain)) {
            return ((VerifierCodeSource) cs).getPrivateSigners();
        }
        CodeSource[] sources = mapSignersToCodeSources(cs.getLocation(), getJarCodeSigners(), true);
        List sourceList = new ArrayList();
        for (Object add : sources) {
            sourceList.add(add);
        }
        int j = sourceList.indexOf(cs);
        if (j == -1) {
            return null;
        }
        CodeSigner[] match = ((VerifierCodeSource) sourceList.get(j)).getPrivateSigners();
        if (match == null) {
            match = this.emptySigner;
        }
        return match;
    }

    private synchronized Map signerMap() {
        if (this.signerMap == null) {
            this.signerMap = new HashMap(this.verifiedSigners.size() + this.sigFileSigners.size());
            this.signerMap.putAll(this.verifiedSigners);
            this.signerMap.putAll(this.sigFileSigners);
        }
        return this.signerMap;
    }

    public synchronized Enumeration<String> entryNames(JarFile jar, CodeSource[] cs) {
        Iterator itor;
        boolean matchUnsigned;
        List req;
        itor = signerMap().entrySet().iterator();
        matchUnsigned = false;
        req = new ArrayList(cs.length);
        for (CodeSource findMatchingSigners : cs) {
            CodeSigner[] match = findMatchingSigners(findMatchingSigners);
            if (match != null) {
                if (match.length > 0) {
                    req.add(match);
                } else {
                    matchUnsigned = true;
                }
            }
        }
        List signersReq = req;
        return new AnonymousClass2(itor, req, matchUnsigned ? unsignedEntryNames(jar) : this.emptyEnumeration);
    }

    public Enumeration<JarEntry> entries2(JarFile jar, Enumeration e) {
        Map map = new HashMap();
        map.putAll(signerMap());
        Enumeration enum_ = e;
        return new AnonymousClass3(e, jar, map);
    }

    static boolean isSigningRelated(String name) {
        name = name.toUpperCase(Locale.ENGLISH);
        if (!name.startsWith("META-INF/")) {
            return false;
        }
        name = name.substring(9);
        if (name.indexOf(47) != -1) {
            return false;
        }
        if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".SF") || name.endsWith(".EC") || name.startsWith("SIG-") || name.equals("MANIFEST.MF")) {
            return true;
        }
        return false;
    }

    private Enumeration<String> unsignedEntryNames(JarFile jar) {
        return new AnonymousClass4(jar.entries(), signerMap());
    }

    private synchronized List getJarCodeSigners() {
        if (this.jarCodeSigners == null) {
            HashSet set = new HashSet();
            set.addAll(signerMap().values());
            this.jarCodeSigners = new ArrayList();
            this.jarCodeSigners.addAll(set);
        }
        return this.jarCodeSigners;
    }

    public synchronized CodeSource[] getCodeSources(JarFile jar, URL url) {
        return mapSignersToCodeSources(url, getJarCodeSigners(), unsignedEntryNames(jar).hasMoreElements());
    }

    public CodeSource getCodeSource(URL url, String name) {
        return mapSignersToCodeSource(url, (CodeSigner[]) signerMap().get(name));
    }

    public CodeSource getCodeSource(URL url, JarFile jar, JarEntry je) {
        return mapSignersToCodeSource(url, getCodeSigners(jar, je));
    }

    public void setEagerValidation(boolean eager) {
        this.eagerValidation = eager;
    }

    public synchronized List getManifestDigests() {
        return Collections.unmodifiableList(this.manifestDigests);
    }

    static CodeSource getUnsignedCS(URL url) {
        return new VerifierCodeSource(null, url, (Certificate[]) null);
    }
}

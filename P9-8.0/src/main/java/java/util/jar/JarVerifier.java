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
import sun.misc.JarIndex;
import sun.security.util.Debug;
import sun.security.util.ManifestDigester;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;

class JarVerifier {
    static final Debug debug = Debug.getInstance("jar");
    private boolean anyToVerify = true;
    private ByteArrayOutputStream baos;
    private Object csdomain = new Object();
    boolean eagerValidation;
    private Enumeration<String> emptyEnumeration = new Enumeration<String>() {
        public boolean hasMoreElements() {
            return false;
        }

        public String nextElement() {
            throw new NoSuchElementException();
        }
    };
    private CodeSigner[] emptySigner = new CodeSigner[0];
    private List<CodeSigner[]> jarCodeSigners;
    private URL lastURL;
    private Map<CodeSigner[], CodeSource> lastURLMap;
    private volatile ManifestDigester manDig;
    private List<Object> manifestDigests;
    byte[] manifestRawBytes = null;
    private boolean parsingBlockOrSF = false;
    private boolean parsingMeta = true;
    private ArrayList<SignatureFileVerifier> pendingBlocks;
    private Hashtable<String, byte[]> sigFileData;
    private Hashtable<String, CodeSigner[]> sigFileSigners;
    private ArrayList<CodeSigner[]> signerCache;
    private Map<String, CodeSigner[]> signerMap;
    private Map<CodeSigner[], CodeSource> signerToCodeSource = new HashMap();
    private Map<URL, Map<CodeSigner[], CodeSource>> urlToCodeSourceMap = new HashMap();
    private Hashtable<String, CodeSigner[]> verifiedSigners;

    private static class VerifierCodeSource extends CodeSource {
        private static final long serialVersionUID = -9047366145967768825L;
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
            return super.lambda$-java_util_function_Predicate_4628(obj);
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

    public JarVerifier(byte[] rawBytes) {
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
                    } else if (!uname.equals(JarFile.MANIFEST_NAME) && !uname.equals(JarIndex.INDEX_NAME)) {
                        if (SignatureFileVerifier.isBlockOrSF(uname)) {
                            this.parsingBlockOrSF = true;
                            this.baos.reset();
                            mev.setEntry(null, je);
                            return;
                        }
                    } else {
                        return;
                    }
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
            if (this.sigFileSigners.get(name) == null && this.verifiedSigners.get(name) == null) {
                mev.setEntry(null, je);
            } else {
                mev.setEntry(name, je);
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
                    Iterator<SignatureFileVerifier> it = this.pendingBlocks.iterator();
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
                byte[] buffer = new byte[1024];
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
        ArrayList<Certificate> certChains = new ArrayList();
        for (CodeSigner signerCertPath : signers) {
            certChains.addAll(signerCertPath.getSignerCertPath().getCertificates());
        }
        return (Certificate[]) certChains.toArray(new Certificate[certChains.size()]);
    }

    boolean nothingToVerify() {
        return !this.anyToVerify;
    }

    void doneWithMeta() {
        this.parsingMeta = false;
        this.anyToVerify = this.sigFileSigners.isEmpty() ^ 1;
        this.baos = null;
        this.sigFileData = null;
        this.pendingBlocks = null;
        this.signerCache = null;
        this.manDig = null;
        if (this.sigFileSigners.containsKey(JarFile.MANIFEST_NAME)) {
            this.verifiedSigners.put(JarFile.MANIFEST_NAME, (CodeSigner[]) this.sigFileSigners.remove(JarFile.MANIFEST_NAME));
        }
    }

    private synchronized CodeSource mapSignersToCodeSource(URL url, CodeSigner[] signers) {
        CodeSource cs;
        Map<CodeSigner[], CodeSource> map;
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

    private CodeSource[] mapSignersToCodeSources(URL url, List<CodeSigner[]> signers, boolean unsigned) {
        List<CodeSource> sources = new ArrayList();
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
        List<CodeSource> sourceList = new ArrayList();
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

    private synchronized Map<String, CodeSigner[]> signerMap() {
        if (this.signerMap == null) {
            this.signerMap = new HashMap(this.verifiedSigners.size() + this.sigFileSigners.size());
            this.signerMap.putAll(this.verifiedSigners);
            this.signerMap.putAll(this.sigFileSigners);
        }
        return this.signerMap;
    }

    public synchronized Enumeration<String> entryNames(JarFile jar, CodeSource[] cs) {
        final Iterator<Entry<String, CodeSigner[]>> itor;
        final List<CodeSigner[]> req;
        final Enumeration<String> enum2;
        itor = signerMap().entrySet().iterator();
        boolean matchUnsigned = false;
        req = new ArrayList(cs.length);
        for (CodeSource findMatchingSigners : cs) {
            CodeSigner[] match = findMatchingSigners(findMatchingSigners);
            if (match == null) {
                matchUnsigned = true;
            } else if (match.length > 0) {
                req.add(match);
            } else {
                matchUnsigned = true;
            }
        }
        List<CodeSigner[]> signersReq = req;
        enum2 = matchUnsigned ? unsignedEntryNames(jar) : this.emptyEnumeration;
        return new Enumeration<String>() {
            String name;

            public boolean hasMoreElements() {
                if (this.name != null) {
                    return true;
                }
                while (itor.hasNext()) {
                    Entry<String, CodeSigner[]> e = (Entry) itor.next();
                    if (req.contains(e.getValue())) {
                        this.name = (String) e.getKey();
                        return true;
                    }
                }
                if (!enum2.hasMoreElements()) {
                    return false;
                }
                this.name = (String) enum2.nextElement();
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
        };
    }

    public Enumeration<JarEntry> entries2(final JarFile jar, final Enumeration<? extends ZipEntry> e) {
        final Map<String, CodeSigner[]> map = new HashMap();
        map.putAll(signerMap());
        Enumeration<? extends ZipEntry> enum_ = e;
        return new Enumeration<JarEntry>() {
            JarEntry entry;
            Enumeration<String> signers = null;

            public boolean hasMoreElements() {
                if (this.entry != null) {
                    return true;
                }
                while (e.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) e.nextElement();
                    if (!JarVerifier.isSigningRelated(ze.getName())) {
                        this.entry = jar.newEntry(ze);
                        return true;
                    }
                }
                if (this.signers == null) {
                    this.signers = Collections.enumeration(map.keySet());
                }
                if (!this.signers.hasMoreElements()) {
                    return false;
                }
                this.entry = jar.newEntry(new ZipEntry((String) this.signers.nextElement()));
                return true;
            }

            public JarEntry nextElement() {
                if (hasMoreElements()) {
                    JarEntry je = this.entry;
                    map.remove(je.getName());
                    this.entry = null;
                    return je;
                }
                throw new NoSuchElementException();
            }
        };
    }

    static boolean isSigningRelated(String name) {
        return SignatureFileVerifier.isSigningRelated(name);
    }

    private Enumeration<String> unsignedEntryNames(JarFile jar) {
        final Map<String, CodeSigner[]> map = signerMap();
        final Enumeration<JarEntry> entries = jar.entries();
        return new Enumeration<String>() {
            String name;

            public boolean hasMoreElements() {
                if (this.name != null) {
                    return true;
                }
                while (entries.hasMoreElements()) {
                    ZipEntry e = (ZipEntry) entries.nextElement();
                    String value = e.getName();
                    if (!e.isDirectory() && !JarVerifier.isSigningRelated(value) && map.get(value) == null) {
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
        };
    }

    private synchronized List<CodeSigner[]> getJarCodeSigners() {
        if (this.jarCodeSigners == null) {
            HashSet<CodeSigner[]> set = new HashSet();
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

    public synchronized List<Object> getManifestDigests() {
        return Collections.unmodifiableList(this.manifestDigests);
    }

    static CodeSource getUnsignedCS(URL url) {
        return new VerifierCodeSource(null, url, (Certificate[]) null);
    }
}

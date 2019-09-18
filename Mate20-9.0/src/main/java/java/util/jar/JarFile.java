package java.util.jar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Spliterators;
import java.util.jar.JarVerifier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sun.misc.IOUtils;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;

public class JarFile extends ZipFile {
    private static final char[] CLASSPATH_CHARS = {'c', 'l', 'a', 's', 's', '-', 'p', 'a', 't', 'h'};
    private static final int[] CLASSPATH_LASTOCC = new int[128];
    private static final int[] CLASSPATH_OPTOSFT = new int[10];
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    static final String META_DIR = "META-INF/";
    private volatile boolean hasCheckedSpecialAttributes;
    private boolean hasClassPathAttribute;
    /* access modifiers changed from: private */
    public JarVerifier jv;
    private boolean jvInitialized;
    private JarEntry manEntry;
    private Manifest manifest;
    private boolean verify;

    private class JarEntryIterator implements Enumeration<JarEntry>, Iterator<JarEntry> {
        final Enumeration<? extends ZipEntry> e;

        private JarEntryIterator() {
            this.e = JarFile.super.entries();
        }

        public boolean hasNext() {
            return this.e.hasMoreElements();
        }

        public JarEntry next() {
            return new JarFileEntry((ZipEntry) this.e.nextElement());
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        public JarEntry nextElement() {
            return next();
        }
    }

    private class JarFileEntry extends JarEntry {
        JarFileEntry(ZipEntry ze) {
            super(ze);
        }

        public Attributes getAttributes() throws IOException {
            Manifest man = JarFile.this.getManifest();
            if (man != null) {
                return man.getAttributes(getName());
            }
            return null;
        }

        public Certificate[] getCertificates() {
            try {
                JarFile.this.maybeInstantiateVerifier();
                if (this.certs == null && JarFile.this.jv != null) {
                    this.certs = JarFile.this.jv.getCerts(JarFile.this, this);
                }
                if (this.certs == null) {
                    return null;
                }
                return (Certificate[]) this.certs.clone();
            } catch (IOException e) {
                throw new RuntimeException((Throwable) e);
            }
        }

        public CodeSigner[] getCodeSigners() {
            try {
                JarFile.this.maybeInstantiateVerifier();
                if (this.signers == null && JarFile.this.jv != null) {
                    this.signers = JarFile.this.jv.getCodeSigners(JarFile.this, this);
                }
                if (this.signers == null) {
                    return null;
                }
                return (CodeSigner[]) this.signers.clone();
            } catch (IOException e) {
                throw new RuntimeException((Throwable) e);
            }
        }
    }

    private native String[] getMetaInfEntryNames();

    public JarFile(String name) throws IOException {
        this(new File(name), true, 1);
    }

    public JarFile(String name, boolean verify2) throws IOException {
        this(new File(name), verify2, 1);
    }

    public JarFile(File file) throws IOException {
        this(file, true, 1);
    }

    public JarFile(File file, boolean verify2) throws IOException {
        this(file, verify2, 1);
    }

    public JarFile(File file, boolean verify2, int mode) throws IOException {
        super(file, mode);
        this.verify = verify2;
    }

    public Manifest getManifest() throws IOException {
        return getManifestFromReference();
    }

    private synchronized Manifest getManifestFromReference() throws IOException {
        if (this.manifest == null) {
            JarEntry manEntry2 = getManEntry();
            if (manEntry2 != null) {
                if (this.verify) {
                    byte[] b = getBytes(manEntry2);
                    this.manifest = new Manifest((InputStream) new ByteArrayInputStream(b));
                    if (!this.jvInitialized) {
                        this.jv = new JarVerifier(b);
                    }
                } else {
                    this.manifest = new Manifest(super.getInputStream(manEntry2));
                }
            }
        }
        return this.manifest;
    }

    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze != null) {
            return new JarFileEntry(ze);
        }
        return null;
    }

    public Enumeration<JarEntry> entries() {
        return new JarEntryIterator();
    }

    public Stream<JarEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(new JarEntryIterator(), (long) size(), 1297), false);
    }

    /* access modifiers changed from: private */
    public void maybeInstantiateVerifier() throws IOException {
        if (this.jv == null && this.verify) {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (String upperCase : names) {
                    String name = upperCase.toUpperCase(Locale.ENGLISH);
                    if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".EC") || name.endsWith(".SF")) {
                        getManifest();
                        return;
                    }
                }
            }
            this.verify = false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A[RETURN, SYNTHETIC] */
    private void initializeVerifier() {
        ManifestEntryVerifier mev;
        IOException ex;
        try {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                mev = null;
                int i = 0;
                while (i < names.length) {
                    try {
                        String uname = names[i].toUpperCase(Locale.ENGLISH);
                        if (MANIFEST_NAME.equals(uname) || SignatureFileVerifier.isBlockOrSF(uname)) {
                            JarEntry e = getJarEntry(names[i]);
                            if (e != null) {
                                if (mev == null) {
                                    mev = new ManifestEntryVerifier(getManifestFromReference());
                                }
                                byte[] b = getBytes(e);
                                if (b != null && b.length > 0) {
                                    this.jv.beginEntry(e, mev);
                                    this.jv.update(b.length, b, 0, b.length, mev);
                                    this.jv.update(-1, null, 0, 0, mev);
                                }
                            } else {
                                throw new JarException("corrupted jar file");
                            }
                        }
                        i++;
                    } catch (IOException e2) {
                        ex = e2;
                        this.jv = null;
                        this.verify = false;
                        if (JarVerifier.debug != null) {
                        }
                        ManifestEntryVerifier manifestEntryVerifier = mev;
                        if (this.jv != null) {
                        }
                    }
                }
                ManifestEntryVerifier manifestEntryVerifier2 = mev;
            }
        } catch (IOException e3) {
            mev = null;
            ex = e3;
            this.jv = null;
            this.verify = false;
            if (JarVerifier.debug != null) {
                JarVerifier.debug.println("jarfile parsing error!");
                ex.printStackTrace();
            }
            ManifestEntryVerifier manifestEntryVerifier3 = mev;
            if (this.jv != null) {
            }
        }
        if (this.jv != null) {
            this.jv.doneWithMeta();
            if (JarVerifier.debug != null) {
                JarVerifier.debug.println("done with meta!");
            }
            if (this.jv.nothingToVerify()) {
                if (JarVerifier.debug != null) {
                    JarVerifier.debug.println("nothing to verify!");
                }
                this.jv = null;
                this.verify = false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        if (r0 != null) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        if (r1 != null) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0015, code lost:
        r2 = move-exception;
     */
    private byte[] getBytes(ZipEntry ze) throws IOException {
        InputStream is = super.getInputStream(ze);
        byte[] readFully = IOUtils.readFully(is, (int) ze.getSize(), true);
        if (is != null) {
            is.close();
        }
        return readFully;
        throw th;
    }

    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        maybeInstantiateVerifier();
        if (this.jv == null) {
            return super.getInputStream(ze);
        }
        if (!this.jvInitialized) {
            initializeVerifier();
            this.jvInitialized = true;
            if (this.jv == null) {
                return super.getInputStream(ze);
            }
        }
        return new JarVerifier.VerifierStream(getManifestFromReference(), ze instanceof JarFileEntry ? (JarEntry) ze : getJarEntry(ze.getName()), super.getInputStream(ze), this.jv);
    }

    static {
        CLASSPATH_LASTOCC[99] = 1;
        CLASSPATH_LASTOCC[108] = 2;
        CLASSPATH_LASTOCC[115] = 5;
        CLASSPATH_LASTOCC[45] = 6;
        CLASSPATH_LASTOCC[112] = 7;
        CLASSPATH_LASTOCC[97] = 8;
        CLASSPATH_LASTOCC[116] = 9;
        CLASSPATH_LASTOCC[104] = 10;
        for (int i = 0; i < 9; i++) {
            CLASSPATH_OPTOSFT[i] = 10;
        }
        CLASSPATH_OPTOSFT[9] = 1;
    }

    private synchronized JarEntry getManEntry() {
        if (this.manEntry == null) {
            this.manEntry = getJarEntry(MANIFEST_NAME);
            if (this.manEntry == null) {
                String[] names = getMetaInfEntryNames();
                if (names != null) {
                    int i = 0;
                    while (true) {
                        if (i >= names.length) {
                            break;
                        } else if (MANIFEST_NAME.equals(names[i].toUpperCase(Locale.ENGLISH))) {
                            this.manEntry = getJarEntry(names[i]);
                            break;
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
        return this.manEntry;
    }

    public boolean hasClassPathAttribute() throws IOException {
        checkForSpecialAttributes();
        return this.hasClassPathAttribute;
    }

    private boolean match(char[] src, byte[] b, int[] lastOcc, int[] optoSft) {
        int len = src.length;
        int last = b.length - len;
        int i = 0;
        while (i <= last) {
            int j = len - 1;
            while (j >= 0) {
                char c = (char) b[i + j];
                char c2 = ((c + 65471) | ('Z' - c)) >= 0 ? (char) (c + ' ') : c;
                if (c2 != src[j]) {
                    i += Math.max((j + 1) - lastOcc[c2 & 127], optoSft[j]);
                } else {
                    j--;
                }
            }
            return true;
        }
        return false;
    }

    private void checkForSpecialAttributes() throws IOException {
        if (!this.hasCheckedSpecialAttributes) {
            JarEntry manEntry2 = getManEntry();
            if (manEntry2 != null) {
                if (match(CLASSPATH_CHARS, getBytes(manEntry2), CLASSPATH_LASTOCC, CLASSPATH_OPTOSFT)) {
                    this.hasClassPathAttribute = true;
                }
            }
            this.hasCheckedSpecialAttributes = true;
        }
    }

    /* access modifiers changed from: package-private */
    public JarEntry newEntry(ZipEntry ze) {
        return new JarFileEntry(ze);
    }
}

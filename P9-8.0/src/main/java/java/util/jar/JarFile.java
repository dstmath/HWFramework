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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sun.misc.IOUtils;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;

public class JarFile extends ZipFile {
    private static final char[] CLASSPATH_CHARS = new char[]{'c', 'l', 'a', 's', 's', '-', 'p', 'a', 't', 'h'};
    private static final int[] CLASSPATH_LASTOCC = new int[128];
    private static final int[] CLASSPATH_OPTOSFT = new int[10];
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    static final String META_DIR = "META-INF/";
    private volatile boolean hasCheckedSpecialAttributes;
    private boolean hasClassPathAttribute;
    private JarVerifier jv;
    private boolean jvInitialized;
    private JarEntry manEntry;
    private Manifest manifest;
    private boolean verify;

    private class JarEntryIterator implements Enumeration<JarEntry>, Iterator<JarEntry> {
        final Enumeration<? extends ZipEntry> e;

        /* synthetic */ JarEntryIterator(JarFile this$0, JarEntryIterator -this1) {
            this();
        }

        private JarEntryIterator() {
            this.e = super.-wrap1();
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
            } catch (Throwable e) {
                throw new RuntimeException(e);
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
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private native String[] getMetaInfEntryNames();

    public JarFile(String name) throws IOException {
        this(new File(name), true, 1);
    }

    public JarFile(String name, boolean verify) throws IOException {
        this(new File(name), verify, 1);
    }

    public JarFile(File file) throws IOException {
        this(file, true, 1);
    }

    public JarFile(File file, boolean verify) throws IOException {
        this(file, verify, 1);
    }

    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, mode);
        this.verify = verify;
    }

    public Manifest getManifest() throws IOException {
        return getManifestFromReference();
    }

    private synchronized Manifest getManifestFromReference() throws IOException {
        if (this.manifest == null) {
            JarEntry manEntry = getManEntry();
            if (manEntry != null) {
                if (this.verify) {
                    byte[] b = getBytes(manEntry);
                    this.manifest = new Manifest(new ByteArrayInputStream(b));
                    if (!this.jvInitialized) {
                        this.jv = new JarVerifier(b);
                    }
                } else {
                    this.manifest = new Manifest(super.getInputStream(manEntry));
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
        return new JarEntryIterator(this, null);
    }

    public Stream<JarEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(new JarEntryIterator(this, null), (long) size(), 1297), false);
    }

    private void maybeInstantiateVerifier() throws IOException {
        if (this.jv == null && this.verify) {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (String toUpperCase : names) {
                    String name = toUpperCase.toUpperCase(Locale.ENGLISH);
                    if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".EC") || name.endsWith(".SF")) {
                        getManifest();
                        return;
                    }
                }
            }
            this.verify = false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initializeVerifier() {
        IOException ex;
        ManifestEntryVerifier mev = null;
        try {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                int i = 0;
                while (true) {
                    ManifestEntryVerifier mev2;
                    try {
                        mev2 = mev;
                        if (i >= names.length) {
                            break;
                        }
                        String uname = names[i].toUpperCase(Locale.ENGLISH);
                        if (MANIFEST_NAME.equals(uname) || SignatureFileVerifier.isBlockOrSF(uname)) {
                            JarEntry e = getJarEntry(names[i]);
                            if (e == null) {
                                throw new JarException("corrupted jar file");
                            }
                            if (mev2 == null) {
                                mev = new ManifestEntryVerifier(getManifestFromReference());
                            } else {
                                mev = mev2;
                            }
                            byte[] b = getBytes(e);
                            if (b != null && b.length > 0) {
                                this.jv.beginEntry(e, mev);
                                this.jv.update(b.length, b, 0, b.length, mev);
                                this.jv.update(-1, null, 0, 0, mev);
                            }
                        } else {
                            mev = mev2;
                        }
                        i++;
                    } catch (IOException e2) {
                        ex = e2;
                        mev = mev2;
                    }
                }
            }
        } catch (IOException e3) {
            ex = e3;
        }
        if (this.jv == null) {
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
                return;
            }
            return;
        }
        return;
        this.jv = null;
        this.verify = false;
        if (JarVerifier.debug != null) {
            JarVerifier.debug.println("jarfile parsing error!");
            ex.printStackTrace();
        }
        if (this.jv == null) {
        }
    }

    private byte[] getBytes(ZipEntry ze) throws IOException {
        Throwable th;
        Throwable th2 = null;
        InputStream inputStream = null;
        try {
            inputStream = super.getInputStream(ze);
            byte[] readFully = IOUtils.readFully(inputStream, (int) ze.getSize(), true);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return readFully;
            }
            throw th2;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
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
        return new VerifierStream(getManifestFromReference(), ze instanceof JarFileEntry ? (JarEntry) ze : getJarEntry(ze.getName()), super.getInputStream(ze), this.jv);
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
                    for (int i = 0; i < names.length; i++) {
                        if (MANIFEST_NAME.equals(names[i].toUpperCase(Locale.ENGLISH))) {
                            this.manEntry = getJarEntry(names[i]);
                            break;
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
                if (((c - 65) | (90 - c)) >= 0) {
                    c = (char) (c + 32);
                }
                if (c != src[j]) {
                    i += Math.max((j + 1) - lastOcc[c & 127], optoSft[j]);
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
            JarEntry manEntry = getManEntry();
            if (manEntry != null) {
                if (match(CLASSPATH_CHARS, getBytes(manEntry), CLASSPATH_LASTOCC, CLASSPATH_OPTOSFT)) {
                    this.hasClassPathAttribute = true;
                }
            }
            this.hasCheckedSpecialAttributes = true;
        }
    }

    JarEntry newEntry(ZipEntry ze) {
        return new JarFileEntry(ze);
    }
}

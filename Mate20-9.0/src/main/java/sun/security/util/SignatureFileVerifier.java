package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.CodeSigner;
import java.security.CryptoPrimitive;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.security.jca.Providers;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.ManifestDigester;

public class SignatureFileVerifier {
    private static final String ATTR_DIGEST = "-DIGEST-Manifest-Main-Attributes".toUpperCase(Locale.ENGLISH);
    private static final Set<CryptoPrimitive> DIGEST_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.MESSAGE_DIGEST));
    private static final DisabledAlgorithmConstraints JAR_DISABLED_CHECK = new DisabledAlgorithmConstraints(DisabledAlgorithmConstraints.PROPERTY_JAR_DISABLED_ALGS);
    private static final Debug debug = Debug.getInstance("jar");
    private static final char[] hexc = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private PKCS7 block;
    private CertificateFactory certificateFactory;
    private HashMap<String, MessageDigest> createdDigests;
    private ManifestDigester md;
    private String name;
    private byte[] sfBytes;
    private ArrayList<CodeSigner[]> signerCache;
    private boolean workaround = false;

    /* JADX INFO: finally extract failed */
    public SignatureFileVerifier(ArrayList<CodeSigner[]> signerCache2, ManifestDigester md2, String name2, byte[] rawBytes) throws IOException, CertificateException {
        Object obj = null;
        this.certificateFactory = null;
        try {
            obj = Providers.startJarVerification();
            this.block = new PKCS7(rawBytes);
            this.sfBytes = this.block.getContentInfo().getData();
            this.certificateFactory = CertificateFactory.getInstance("X509");
            Providers.stopJarVerification(obj);
            this.name = name2.substring(0, name2.lastIndexOf(".")).toUpperCase(Locale.ENGLISH);
            this.md = md2;
            this.signerCache = signerCache2;
        } catch (Throwable th) {
            Providers.stopJarVerification(obj);
            throw th;
        }
    }

    public boolean needSignatureFileBytes() {
        return this.sfBytes == null;
    }

    public boolean needSignatureFile(String name2) {
        return this.name.equalsIgnoreCase(name2);
    }

    public void setSignatureFile(byte[] sfBytes2) {
        this.sfBytes = sfBytes2;
    }

    public static boolean isBlockOrSF(String s) {
        if (s.endsWith(".SF") || s.endsWith(".DSA") || s.endsWith(".RSA") || s.endsWith(".EC")) {
            return true;
        }
        return false;
    }

    public static boolean isSigningRelated(String name2) {
        String name3 = name2.toUpperCase(Locale.ENGLISH);
        if (!name3.startsWith("META-INF/")) {
            return false;
        }
        String name4 = name3.substring(9);
        if (name4.indexOf(47) != -1) {
            return false;
        }
        if (isBlockOrSF(name4) || name4.equals("MANIFEST.MF")) {
            return true;
        }
        if (!name4.startsWith("SIG-")) {
            return false;
        }
        int extIndex = name4.lastIndexOf(46);
        if (extIndex != -1) {
            String ext = name4.substring(extIndex + 1);
            if (ext.length() > 3 || ext.length() < 1) {
                return false;
            }
            for (int index = 0; index < ext.length(); index++) {
                char cc = ext.charAt(index);
                if ((cc < 'A' || cc > 'Z') && (cc < '0' || cc > '9')) {
                    return false;
                }
            }
        }
        return true;
    }

    private MessageDigest getDigest(String algorithm) throws SignatureException {
        if (JAR_DISABLED_CHECK.permits(DIGEST_PRIMITIVE_SET, algorithm, null)) {
            if (this.createdDigests == null) {
                this.createdDigests = new HashMap<>();
            }
            MessageDigest digest = this.createdDigests.get(algorithm);
            if (digest != null) {
                return digest;
            }
            try {
                digest = MessageDigest.getInstance(algorithm);
                this.createdDigests.put(algorithm, digest);
                return digest;
            } catch (NoSuchAlgorithmException e) {
                return digest;
            }
        } else {
            throw new SignatureException("SignatureFile check failed. Disabled algorithm used: " + algorithm);
        }
    }

    public void process(Hashtable<String, CodeSigner[]> signers, List<Object> manifestDigests) throws IOException, SignatureException, NoSuchAlgorithmException, JarException, CertificateException {
        Object obj = null;
        try {
            obj = Providers.startJarVerification();
            processImpl(signers, manifestDigests);
        } finally {
            Providers.stopJarVerification(obj);
        }
    }

    private void processImpl(Hashtable<String, CodeSigner[]> signers, List<Object> manifestDigests) throws IOException, SignatureException, NoSuchAlgorithmException, JarException, CertificateException {
        Manifest sf = new Manifest();
        sf.read(new ByteArrayInputStream(this.sfBytes));
        String version = sf.getMainAttributes().getValue(Attributes.Name.SIGNATURE_VERSION);
        if (version != null && version.equalsIgnoreCase("1.0")) {
            SignerInfo[] infos = this.block.verify(this.sfBytes);
            if (infos != null) {
                CodeSigner[] newSigners = getSigners(infos, this.block);
                if (newSigners != null) {
                    boolean manifestSigned = verifyManifestHash(sf, this.md, manifestDigests);
                    if (manifestSigned || verifyManifestMainAttrs(sf, this.md)) {
                        for (Map.Entry<String, Attributes> e : sf.getEntries().entrySet()) {
                            String name2 = e.getKey();
                            if (manifestSigned || verifySection(e.getValue(), name2, this.md)) {
                                if (name2.startsWith("./")) {
                                    name2 = name2.substring(2);
                                }
                                if (name2.startsWith("/")) {
                                    name2 = name2.substring(1);
                                }
                                updateSigners(newSigners, signers, name2);
                                if (debug != null) {
                                    Debug debug2 = debug;
                                    debug2.println("processSignature signed name = " + name2);
                                }
                            } else if (debug != null) {
                                Debug debug3 = debug;
                                debug3.println("processSignature unsigned name = " + name2);
                            }
                        }
                        updateSigners(newSigners, signers, JarFile.MANIFEST_NAME);
                        return;
                    }
                    throw new SecurityException("Invalid signature file digest for Manifest main attributes");
                }
                return;
            }
            throw new SecurityException("cannot verify signature block file " + this.name);
        }
    }

    private boolean verifyManifestHash(Manifest sf, ManifestDigester md2, List<Object> manifestDigests) throws IOException, SignatureException {
        boolean manifestSigned = false;
        for (Map.Entry<Object, Object> se : sf.getMainAttributes().entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST")) {
                String algorithm = key.substring(0, key.length() - 16);
                manifestDigests.add(key);
                manifestDigests.add(se.getValue());
                MessageDigest digest = getDigest(algorithm);
                if (digest != null) {
                    byte[] computedHash = md2.manifestDigest(digest);
                    byte[] expectedHash = Base64.getMimeDecoder().decode((String) se.getValue());
                    if (debug != null) {
                        Debug debug2 = debug;
                        debug2.println("Signature File: Manifest digest " + digest.getAlgorithm());
                        Debug debug3 = debug;
                        debug3.println("  sigfile  " + toHex(expectedHash));
                        Debug debug4 = debug;
                        debug4.println("  computed " + toHex(computedHash));
                        debug.println();
                    }
                    if (MessageDigest.isEqual(computedHash, expectedHash)) {
                        manifestSigned = true;
                    }
                }
            }
        }
        return manifestSigned;
    }

    private boolean verifyManifestMainAttrs(Manifest sf, ManifestDigester md2) throws IOException, SignatureException {
        boolean attrsVerified = true;
        Iterator<Map.Entry<Object, Object>> it = sf.getMainAttributes().entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<Object, Object> se = it.next();
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith(ATTR_DIGEST)) {
                MessageDigest digest = getDigest(key.substring(0, key.length() - ATTR_DIGEST.length()));
                if (digest != null) {
                    byte[] computedHash = md2.get(ManifestDigester.MF_MAIN_ATTRS, false).digest(digest);
                    byte[] expectedHash = Base64.getMimeDecoder().decode((String) se.getValue());
                    if (debug != null) {
                        Debug debug2 = debug;
                        debug2.println("Signature File: Manifest Main Attributes digest " + digest.getAlgorithm());
                        Debug debug3 = debug;
                        debug3.println("  sigfile  " + toHex(expectedHash));
                        Debug debug4 = debug;
                        debug4.println("  computed " + toHex(computedHash));
                        debug.println();
                    }
                    if (!MessageDigest.isEqual(computedHash, expectedHash)) {
                        attrsVerified = false;
                        if (debug != null) {
                            debug.println("Verification of Manifest main attributes failed");
                            debug.println();
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        return attrsVerified;
    }

    private boolean verifySection(Attributes sfAttr, String name2, ManifestDigester md2) throws IOException, SignatureException {
        byte[] computed;
        boolean oneDigestVerified;
        String str = name2;
        boolean oneDigestVerified2 = false;
        ManifestDigester.Entry mde = md2.get(str, this.block.isOldStyle());
        if (mde == null) {
            throw new SecurityException("no manifest section for signature file entry " + str);
        } else if (sfAttr == null) {
            return false;
        } else {
            for (Map.Entry<Object, Object> se : sfAttr.entrySet()) {
                String key = se.getKey().toString();
                if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                    MessageDigest digest = getDigest(key.substring(0, key.length() - 7));
                    if (digest != null) {
                        boolean ok = false;
                        byte[] expected = Base64.getMimeDecoder().decode((String) se.getValue());
                        if (this.workaround) {
                            computed = mde.digestWorkaround(digest);
                        } else {
                            computed = mde.digest(digest);
                        }
                        if (debug != null) {
                            Debug debug2 = debug;
                            StringBuilder sb = new StringBuilder();
                            oneDigestVerified = oneDigestVerified2;
                            sb.append("Signature Block File: ");
                            sb.append(str);
                            sb.append(" digest=");
                            sb.append(digest.getAlgorithm());
                            debug2.println(sb.toString());
                            Debug debug3 = debug;
                            debug3.println("  expected " + toHex(expected));
                            Debug debug4 = debug;
                            debug4.println("  computed " + toHex(computed));
                            debug.println();
                        } else {
                            oneDigestVerified = oneDigestVerified2;
                        }
                        if (MessageDigest.isEqual(computed, expected)) {
                            oneDigestVerified2 = true;
                            ok = true;
                        } else {
                            if (!this.workaround) {
                                byte[] computed2 = mde.digestWorkaround(digest);
                                if (MessageDigest.isEqual(computed2, expected)) {
                                    if (debug != null) {
                                        Debug debug5 = debug;
                                        debug5.println("  re-computed " + toHex(computed2));
                                        debug.println();
                                    }
                                    this.workaround = true;
                                    oneDigestVerified2 = true;
                                    ok = true;
                                }
                            }
                            oneDigestVerified2 = oneDigestVerified;
                        }
                        if (!ok) {
                            throw new SecurityException("invalid " + digest.getAlgorithm() + " signature file digest for " + str);
                        }
                    }
                }
                oneDigestVerified2 = oneDigestVerified2;
            }
            return oneDigestVerified2;
        }
    }

    private CodeSigner[] getSigners(SignerInfo[] infos, PKCS7 block2) throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException {
        ArrayList<CodeSigner> signers = null;
        for (SignerInfo info : infos) {
            CertPath certChain = this.certificateFactory.generateCertPath((List<? extends Certificate>) info.getCertificateChain(block2));
            if (signers == null) {
                signers = new ArrayList<>();
            }
            signers.add(new CodeSigner(certChain, info.getTimestamp()));
            if (debug != null) {
                debug.println("Signature Block Certificate: " + chain.get(0));
            }
        }
        if (signers != null) {
            return (CodeSigner[]) signers.toArray(new CodeSigner[signers.size()]);
        }
        return null;
    }

    static String toHex(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(hexc[(data[i] >> 4) & 15]);
            sb.append(hexc[data[i] & 15]);
        }
        return sb.toString();
    }

    static boolean contains(CodeSigner[] set, CodeSigner signer) {
        for (CodeSigner equals : set) {
            if (equals.equals(signer)) {
                return true;
            }
        }
        return false;
    }

    static boolean isSubSet(CodeSigner[] subset, CodeSigner[] set) {
        if (set == subset) {
            return true;
        }
        for (CodeSigner contains : subset) {
            if (!contains(set, contains)) {
                return false;
            }
        }
        return true;
    }

    static boolean matches(CodeSigner[] signers, CodeSigner[] oldSigners, CodeSigner[] newSigners) {
        if (oldSigners == null && signers == newSigners) {
            return true;
        }
        if ((oldSigners != null && !isSubSet(oldSigners, signers)) || !isSubSet(newSigners, signers)) {
            return false;
        }
        for (int i = 0; i < signers.length; i++) {
            if (!((oldSigners != null && contains(oldSigners, signers[i])) || contains(newSigners, signers[i]))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateSigners(CodeSigner[] newSigners, Hashtable<String, CodeSigner[]> signers, String name2) {
        CodeSigner[] cachedSigners;
        CodeSigner[] oldSigners = signers.get(name2);
        int i = this.signerCache.size();
        while (true) {
            i--;
            if (i != -1) {
                CodeSigner[] cachedSigners2 = this.signerCache.get(i);
                if (matches(cachedSigners2, oldSigners, newSigners)) {
                    signers.put(name2, cachedSigners2);
                    return;
                }
            } else {
                if (oldSigners == null) {
                    cachedSigners = newSigners;
                } else {
                    cachedSigners = new CodeSigner[(oldSigners.length + newSigners.length)];
                    System.arraycopy((Object) oldSigners, 0, (Object) cachedSigners, 0, oldSigners.length);
                    System.arraycopy((Object) newSigners, 0, (Object) cachedSigners, oldSigners.length, newSigners.length);
                }
                this.signerCache.add(cachedSigners);
                signers.put(name2, cachedSigners);
                return;
            }
        }
    }
}

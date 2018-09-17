package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.CodeSigner;
import java.security.CryptoPrimitive;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.security.jca.Providers;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

public class SignatureFileVerifier {
    private static final String ATTR_DIGEST = "-DIGEST-Manifest-Main-Attributes".toUpperCase(Locale.ENGLISH);
    private static final Set<CryptoPrimitive> DIGEST_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.MESSAGE_DIGEST));
    private static final DisabledAlgorithmConstraints JAR_DISABLED_CHECK = new DisabledAlgorithmConstraints(DisabledAlgorithmConstraints.PROPERTY_JAR_DISABLED_ALGS);
    private static final Debug debug = Debug.getInstance("jar");
    private static final char[] hexc = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private PKCS7 block;
    private CertificateFactory certificateFactory = null;
    private HashMap<String, MessageDigest> createdDigests;
    private ManifestDigester md;
    private String name;
    private byte[] sfBytes;
    private ArrayList<CodeSigner[]> signerCache;
    private boolean workaround = false;

    public SignatureFileVerifier(ArrayList<CodeSigner[]> signerCache, ManifestDigester md, String name, byte[] rawBytes) throws IOException, CertificateException {
        Object obj = null;
        try {
            obj = Providers.startJarVerification();
            this.block = new PKCS7(rawBytes);
            this.sfBytes = this.block.getContentInfo().getData();
            this.certificateFactory = CertificateFactory.getInstance("X509");
            this.name = name.substring(0, name.lastIndexOf(".")).toUpperCase(Locale.ENGLISH);
            this.md = md;
            this.signerCache = signerCache;
        } finally {
            Providers.stopJarVerification(obj);
        }
    }

    public boolean needSignatureFileBytes() {
        return this.sfBytes == null;
    }

    public boolean needSignatureFile(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    public void setSignatureFile(byte[] sfBytes) {
        this.sfBytes = sfBytes;
    }

    public static boolean isBlockOrSF(String s) {
        if (s.endsWith(".SF") || s.endsWith(".DSA") || s.endsWith(".RSA") || s.endsWith(".EC")) {
            return true;
        }
        return false;
    }

    public static boolean isSigningRelated(String name) {
        name = name.toUpperCase(Locale.ENGLISH);
        if (!name.startsWith("META-INF/")) {
            return false;
        }
        name = name.substring(9);
        if (name.indexOf(47) != -1) {
            return false;
        }
        if (isBlockOrSF(name) || name.equals("MANIFEST.MF")) {
            return true;
        }
        if (!name.startsWith("SIG-")) {
            return false;
        }
        int extIndex = name.lastIndexOf(46);
        if (extIndex != -1) {
            String ext = name.substring(extIndex + 1);
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
                this.createdDigests = new HashMap();
            }
            MessageDigest digest = (MessageDigest) this.createdDigests.get(algorithm);
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
        }
        throw new SignatureException("SignatureFile check failed. Disabled algorithm used: " + algorithm);
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
        String version = sf.getMainAttributes().getValue(Name.SIGNATURE_VERSION);
        if (version != null && (version.equalsIgnoreCase("1.0") ^ 1) == 0) {
            SignerInfo[] infos = this.block.verify(this.sfBytes);
            if (infos == null) {
                throw new SecurityException("cannot verify signature block file " + this.name);
            }
            CodeSigner[] newSigners = getSigners(infos, this.block);
            if (newSigners != null) {
                boolean manifestSigned = verifyManifestHash(sf, this.md, manifestDigests);
                if (manifestSigned || (verifyManifestMainAttrs(sf, this.md) ^ 1) == 0) {
                    for (Entry<String, Attributes> e : sf.getEntries().entrySet()) {
                        String name = (String) e.getKey();
                        if (manifestSigned || verifySection((Attributes) e.getValue(), name, this.md)) {
                            if (name.startsWith("./")) {
                                name = name.substring(2);
                            }
                            if (name.startsWith("/")) {
                                name = name.substring(1);
                            }
                            updateSigners(newSigners, signers, name);
                            if (debug != null) {
                                debug.println("processSignature signed name = " + name);
                            }
                        } else if (debug != null) {
                            debug.println("processSignature unsigned name = " + name);
                        }
                    }
                    updateSigners(newSigners, signers, JarFile.MANIFEST_NAME);
                    return;
                }
                throw new SecurityException("Invalid signature file digest for Manifest main attributes");
            }
        }
    }

    private boolean verifyManifestHash(Manifest sf, ManifestDigester md, List<Object> manifestDigests) throws IOException, SignatureException {
        boolean manifestSigned = false;
        for (Entry<Object, Object> se : sf.getMainAttributes().entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST")) {
                String algorithm = key.substring(0, key.length() - 16);
                manifestDigests.-java_util_stream_Collectors-mthref-2(key);
                manifestDigests.-java_util_stream_Collectors-mthref-2(se.getValue());
                MessageDigest digest = getDigest(algorithm);
                if (digest != null) {
                    byte[] computedHash = md.manifestDigest(digest);
                    byte[] expectedHash = Base64.getMimeDecoder().decode((String) se.getValue());
                    if (debug != null) {
                        debug.println("Signature File: Manifest digest " + digest.getAlgorithm());
                        debug.println("  sigfile  " + toHex(expectedHash));
                        debug.println("  computed " + toHex(computedHash));
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

    private boolean verifyManifestMainAttrs(Manifest sf, ManifestDigester md) throws IOException, SignatureException {
        boolean attrsVerified = true;
        for (Entry<Object, Object> se : sf.getMainAttributes().entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith(ATTR_DIGEST)) {
                MessageDigest digest = getDigest(key.substring(0, key.length() - ATTR_DIGEST.length()));
                if (digest != null) {
                    byte[] computedHash = md.get(ManifestDigester.MF_MAIN_ATTRS, false).digest(digest);
                    byte[] expectedHash = Base64.getMimeDecoder().decode((String) se.getValue());
                    if (debug != null) {
                        debug.println("Signature File: Manifest Main Attributes digest " + digest.getAlgorithm());
                        debug.println("  sigfile  " + toHex(expectedHash));
                        debug.println("  computed " + toHex(computedHash));
                        debug.println();
                    }
                    if (!MessageDigest.isEqual(computedHash, expectedHash)) {
                        attrsVerified = false;
                        if (debug != null) {
                            debug.println("Verification of Manifest main attributes failed");
                            debug.println();
                        }
                        return attrsVerified;
                    }
                } else {
                    continue;
                }
            }
        }
        return attrsVerified;
    }

    private boolean verifySection(Attributes sfAttr, String name, ManifestDigester md) throws IOException, SignatureException {
        boolean oneDigestVerified = false;
        ManifestDigester.Entry mde = md.get(name, this.block.isOldStyle());
        if (mde == null) {
            throw new SecurityException("no manifest section for signature file entry " + name);
        }
        if (sfAttr != null) {
            for (Entry<Object, Object> se : sfAttr.entrySet()) {
                String key = se.getKey().toString();
                if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                    MessageDigest digest = getDigest(key.substring(0, key.length() - 7));
                    if (digest != null) {
                        byte[] computed;
                        boolean ok = false;
                        byte[] expected = Base64.getMimeDecoder().decode((String) se.getValue());
                        if (this.workaround) {
                            computed = mde.digestWorkaround(digest);
                        } else {
                            computed = mde.digest(digest);
                        }
                        if (debug != null) {
                            debug.println("Signature Block File: " + name + " digest=" + digest.getAlgorithm());
                            debug.println("  expected " + toHex(expected));
                            debug.println("  computed " + toHex(computed));
                            debug.println();
                        }
                        if (MessageDigest.isEqual(computed, expected)) {
                            oneDigestVerified = true;
                            ok = true;
                        } else if (!this.workaround) {
                            computed = mde.digestWorkaround(digest);
                            if (MessageDigest.isEqual(computed, expected)) {
                                if (debug != null) {
                                    debug.println("  re-computed " + toHex(computed));
                                    debug.println();
                                }
                                this.workaround = true;
                                oneDigestVerified = true;
                                ok = true;
                            }
                        }
                        if (!ok) {
                            throw new SecurityException("invalid " + digest.getAlgorithm() + " signature file digest for " + name);
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return oneDigestVerified;
    }

    private CodeSigner[] getSigners(SignerInfo[] infos, PKCS7 block) throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException {
        ArrayList signers = null;
        for (SignerInfo info : infos) {
            List chain = info.getCertificateChain(block);
            CertPath certChain = this.certificateFactory.generateCertPath(chain);
            if (signers == null) {
                signers = new ArrayList();
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
        if ((oldSigners != null && (isSubSet(oldSigners, signers) ^ 1) != 0) || !isSubSet(newSigners, signers)) {
            return false;
        }
        int i = 0;
        while (i < signers.length) {
            boolean found;
            if (oldSigners == null || !contains(oldSigners, signers[i])) {
                found = contains(newSigners, signers[i]);
            } else {
                found = true;
            }
            if (!found) {
                return false;
            }
            i++;
        }
        return true;
    }

    void updateSigners(CodeSigner[] newSigners, Hashtable<String, CodeSigner[]> signers, String name) {
        CodeSigner[] cachedSigners;
        Object oldSigners = (CodeSigner[]) signers.get(name);
        for (int i = this.signerCache.size() - 1; i != -1; i--) {
            cachedSigners = (CodeSigner[]) this.signerCache.get(i);
            if (matches(cachedSigners, oldSigners, newSigners)) {
                signers.put(name, cachedSigners);
                return;
            }
        }
        if (oldSigners == null) {
            cachedSigners = newSigners;
        } else {
            Object cachedSigners2 = new CodeSigner[(oldSigners.length + newSigners.length)];
            System.arraycopy(oldSigners, 0, cachedSigners2, 0, oldSigners.length);
            System.arraycopy((Object) newSigners, 0, cachedSigners2, oldSigners.length, newSigners.length);
        }
        this.signerCache.add(cachedSigners);
        signers.put(name, cachedSigners);
    }
}

package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.misc.BASE64Decoder;
import sun.security.jca.Providers;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.SignerInfo;
import sun.security.timestamp.TimestampToken;

public class SignatureFileVerifier {
    private static final String ATTR_DIGEST = null;
    private static final Debug debug = null;
    private static final char[] hexc = null;
    private PKCS7 block;
    private CertificateFactory certificateFactory;
    private HashMap<String, MessageDigest> createdDigests;
    private ManifestDigester md;
    private String name;
    private byte[] sfBytes;
    private ArrayList<CodeSigner[]> signerCache;
    private boolean workaround;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.SignatureFileVerifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.SignatureFileVerifier.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.SignatureFileVerifier.<clinit>():void");
    }

    public SignatureFileVerifier(ArrayList<CodeSigner[]> signerCache, ManifestDigester md, String name, byte[] rawBytes) throws IOException, CertificateException {
        this.workaround = false;
        this.certificateFactory = null;
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

    private MessageDigest getDigest(String algorithm) {
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

    public void process(Hashtable<String, CodeSigner[]> signers, List manifestDigests) throws IOException, SignatureException, NoSuchAlgorithmException, JarException, CertificateException {
        Object obj = null;
        try {
            obj = Providers.startJarVerification();
            processImpl(signers, manifestDigests);
        } finally {
            Providers.stopJarVerification(obj);
        }
    }

    private void processImpl(Hashtable<String, CodeSigner[]> signers, List manifestDigests) throws IOException, SignatureException, NoSuchAlgorithmException, JarException, CertificateException {
        Manifest sf = new Manifest();
        sf.read(new ByteArrayInputStream(this.sfBytes));
        String version = sf.getMainAttributes().getValue(Name.SIGNATURE_VERSION);
        if (version != null && version.equalsIgnoreCase("1.0")) {
            SignerInfo[] infos = this.block.verify(this.sfBytes);
            if (infos == null) {
                throw new SecurityException("cannot verify signature block file " + this.name);
            }
            BASE64Decoder decoder = new BASE64Decoder();
            CodeSigner[] newSigners = getSigners(infos, this.block);
            if (newSigners != null) {
                boolean manifestSigned = verifyManifestHash(sf, this.md, decoder, manifestDigests);
                if (manifestSigned || verifyManifestMainAttrs(sf, this.md, decoder)) {
                    for (Entry<String, Attributes> e : sf.getEntries().entrySet()) {
                        String name = (String) e.getKey();
                        if (manifestSigned || verifySection((Attributes) e.getValue(), name, this.md, decoder)) {
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

    private boolean verifyManifestHash(Manifest sf, ManifestDigester md, BASE64Decoder decoder, List manifestDigests) throws IOException {
        boolean manifestSigned = false;
        for (Entry<Object, Object> se : sf.getMainAttributes().entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST")) {
                String algorithm = key.substring(0, key.length() - 16);
                manifestDigests.add(key);
                manifestDigests.add(se.getValue());
                MessageDigest digest = getDigest(algorithm);
                if (digest != null) {
                    byte[] computedHash = md.manifestDigest(digest);
                    byte[] expectedHash = decoder.decodeBuffer((String) se.getValue());
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

    private boolean verifyManifestMainAttrs(Manifest sf, ManifestDigester md, BASE64Decoder decoder) throws IOException {
        boolean attrsVerified = true;
        for (Entry<Object, Object> se : sf.getMainAttributes().entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith(ATTR_DIGEST)) {
                MessageDigest digest = getDigest(key.substring(0, key.length() - ATTR_DIGEST.length()));
                if (digest != null) {
                    byte[] computedHash = md.get(ManifestDigester.MF_MAIN_ATTRS, false).digest(digest);
                    byte[] expectedHash = decoder.decodeBuffer((String) se.getValue());
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

    private boolean verifySection(Attributes sfAttr, String name, ManifestDigester md, BASE64Decoder decoder) throws IOException {
        boolean oneDigestVerified = false;
        ManifestDigester.Entry mde = md.get(name, this.block.isOldStyle());
        if (mde == null) {
            throw new SecurityException("no manifiest section for signature file entry " + name);
        }
        if (sfAttr != null) {
            for (Entry<Object, Object> se : sfAttr.entrySet()) {
                String key = se.getKey().toString();
                if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                    MessageDigest digest = getDigest(key.substring(0, key.length() - 7));
                    if (digest != null) {
                        byte[] computed;
                        boolean ok = false;
                        byte[] expected = decoder.decodeBuffer((String) se.getValue());
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
            signers.add(new CodeSigner(certChain, getTimestamp(info)));
            if (debug != null) {
                debug.println("Signature Block Certificate: " + chain.get(0));
            }
        }
        if (signers != null) {
            return (CodeSigner[]) signers.toArray(new CodeSigner[signers.size()]);
        }
        return null;
    }

    private Timestamp getTimestamp(SignerInfo info) throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException {
        PKCS9Attributes unsignedAttrs = info.getUnauthenticatedAttributes();
        if (unsignedAttrs == null) {
            return null;
        }
        PKCS9Attribute timestampTokenAttr = unsignedAttrs.getAttribute("signatureTimestampToken");
        if (timestampTokenAttr == null) {
            return null;
        }
        PKCS7 timestampToken = new PKCS7((byte[]) timestampTokenAttr.getValue());
        byte[] encodedTimestampTokenInfo = timestampToken.getContentInfo().getData();
        CertPath tsaChain = this.certificateFactory.generateCertPath(timestampToken.verify(encodedTimestampTokenInfo)[0].getCertificateChain(timestampToken));
        TimestampToken timestampTokenInfo = new TimestampToken(encodedTimestampTokenInfo);
        verifyTimestamp(timestampTokenInfo, info.getEncryptedDigest());
        return new Timestamp(timestampTokenInfo.getDate(), tsaChain);
    }

    private void verifyTimestamp(TimestampToken token, byte[] signature) throws NoSuchAlgorithmException, SignatureException {
        if (!Arrays.equals(token.getHashedMessage(), MessageDigest.getInstance(token.getHashAlgorithm().getName()).digest(signature))) {
            throw new SignatureException("Signature timestamp (#" + token.getSerialNumber() + ") generated on " + token.getDate() + " is inapplicable");
        } else if (debug != null) {
            debug.println();
            debug.println("Detected signature timestamp (#" + token.getSerialNumber() + ") generated on " + token.getDate());
            debug.println();
        }
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

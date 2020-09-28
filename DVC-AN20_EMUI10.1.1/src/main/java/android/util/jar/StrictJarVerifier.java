package android.util.jar;

import android.security.keystore.KeyProperties;
import android.telephony.SmsManager;
import android.util.jar.StrictJarManifest;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import sun.security.jca.Providers;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

/* access modifiers changed from: package-private */
public class StrictJarVerifier {
    private static final String[] DIGEST_ALGORITHMS = {KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA256, "SHA1"};
    private static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME = "X-Android-APK-Signed";
    private final Hashtable<String, Certificate[]> certificates = new Hashtable<>(5);
    private final String jarName;
    private final int mainAttributesEnd;
    private final StrictJarManifest manifest;
    private final HashMap<String, byte[]> metaEntries;
    private final boolean signatureSchemeRollbackProtectionsEnforced;
    private final Hashtable<String, HashMap<String, Attributes>> signatures = new Hashtable<>(5);
    private final Hashtable<String, Certificate[][]> verifiedEntries = new Hashtable<>();

    /* access modifiers changed from: package-private */
    public static class VerifierEntry extends OutputStream {
        private final Certificate[][] certChains;
        private final MessageDigest digest;
        private final byte[] hash;
        private final String name;
        private final Hashtable<String, Certificate[][]> verifiedEntries;

        VerifierEntry(String name2, MessageDigest digest2, byte[] hash2, Certificate[][] certChains2, Hashtable<String, Certificate[][]> verifedEntries) {
            this.name = name2;
            this.digest = digest2;
            this.hash = hash2;
            this.certChains = certChains2;
            this.verifiedEntries = verifedEntries;
        }

        @Override // java.io.OutputStream
        public void write(int value) {
            this.digest.update((byte) value);
        }

        @Override // java.io.OutputStream
        public void write(byte[] buf, int off, int nbytes) {
            this.digest.update(buf, off, nbytes);
        }

        /* access modifiers changed from: package-private */
        public void verify() {
            if (StrictJarVerifier.verifyMessageDigest(this.digest.digest(), this.hash)) {
                this.verifiedEntries.put(this.name, this.certChains);
            } else {
                String str = this.name;
                throw StrictJarVerifier.invalidDigest("META-INF/MANIFEST.MF", str, str);
            }
        }
    }

    /* access modifiers changed from: private */
    public static SecurityException invalidDigest(String signatureFile, String name, String jarName2) {
        throw new SecurityException(signatureFile + " has invalid digest for " + name + " in " + jarName2);
    }

    private static SecurityException failedVerification(String jarName2, String signatureFile) {
        throw new SecurityException(jarName2 + " failed verification of " + signatureFile);
    }

    private static SecurityException failedVerification(String jarName2, String signatureFile, Throwable e) {
        throw new SecurityException(jarName2 + " failed verification of " + signatureFile, e);
    }

    StrictJarVerifier(String name, StrictJarManifest manifest2, HashMap<String, byte[]> metaEntries2, boolean signatureSchemeRollbackProtectionsEnforced2) {
        this.jarName = name;
        this.manifest = manifest2;
        this.metaEntries = metaEntries2;
        this.mainAttributesEnd = manifest2.getMainAttributesEnd();
        this.signatureSchemeRollbackProtectionsEnforced = signatureSchemeRollbackProtectionsEnforced2;
    }

    /* access modifiers changed from: package-private */
    public VerifierEntry initEntry(String name) {
        Attributes attributes;
        Certificate[] certChain;
        if (this.manifest == null || this.signatures.isEmpty() || (attributes = this.manifest.getAttributes(name)) == null) {
            return null;
        }
        ArrayList<Certificate[]> certChains = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, Attributes>> entry : this.signatures.entrySet()) {
            if (!(entry.getValue().get(name) == null || (certChain = this.certificates.get(entry.getKey())) == null)) {
                certChains.add(certChain);
            }
        }
        if (certChains.isEmpty()) {
            return null;
        }
        Certificate[][] certChainsArray = (Certificate[][]) certChains.toArray(new Certificate[certChains.size()][]);
        int i = 0;
        while (true) {
            String[] strArr = DIGEST_ALGORITHMS;
            if (i >= strArr.length) {
                return null;
            }
            String algorithm = strArr[i];
            String hash = attributes.getValue(algorithm + "-Digest");
            if (hash != null) {
                try {
                    try {
                        return new VerifierEntry(name, MessageDigest.getInstance(algorithm), hash.getBytes(StandardCharsets.ISO_8859_1), certChainsArray, this.verifiedEntries);
                    } catch (NoSuchAlgorithmException e) {
                    }
                } catch (NoSuchAlgorithmException e2) {
                }
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    public void addMetaEntry(String name, byte[] buf) {
        this.metaEntries.put(name.toUpperCase(Locale.US), buf);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean readCertificates() {
        if (this.metaEntries.isEmpty()) {
            return false;
        }
        Iterator<String> it = this.metaEntries.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.endsWith(".DSA") || key.endsWith(".RSA") || key.endsWith(".EC")) {
                verifyCertificate(key);
                it.remove();
            }
        }
        return true;
    }

    static Certificate[] verifyBytes(byte[] blockBytes, byte[] sfBytes) throws GeneralSecurityException {
        try {
            Object obj = Providers.startJarVerification();
            PKCS7 block = new PKCS7(blockBytes);
            SignerInfo[] verifiedSignerInfos = block.verify(sfBytes);
            if (verifiedSignerInfos == null || verifiedSignerInfos.length == 0) {
                throw new GeneralSecurityException("Failed to verify signature: no verified SignerInfos");
            }
            List<X509Certificate> verifiedSignerCertChain = verifiedSignerInfos[0].getCertificateChain(block);
            if (verifiedSignerCertChain == null) {
                throw new GeneralSecurityException("Failed to find verified SignerInfo certificate chain");
            } else if (!verifiedSignerCertChain.isEmpty()) {
                Certificate[] certificateArr = (Certificate[]) verifiedSignerCertChain.toArray(new X509Certificate[verifiedSignerCertChain.size()]);
                Providers.stopJarVerification(obj);
                return certificateArr;
            } else {
                throw new GeneralSecurityException("Verified SignerInfo certificate chain is emtpy");
            }
        } catch (IOException e) {
            throw new GeneralSecurityException("IO exception verifying jar cert", e);
        } catch (Throwable block2) {
            Providers.stopJarVerification((Object) null);
            throw block2;
        }
    }

    private void verifyCertificate(String certFile) {
        byte[] manifestBytes;
        HashMap<String, Attributes> entries;
        String apkSignatureSchemeIdList;
        String signatureFile = certFile.substring(0, certFile.lastIndexOf(46)) + ".SF";
        byte[] sfBytes = this.metaEntries.get(signatureFile);
        if (sfBytes != null && (manifestBytes = this.metaEntries.get("META-INF/MANIFEST.MF")) != null) {
            byte[] sBlockBytes = this.metaEntries.get(certFile);
            try {
                Certificate[] signerCertChain = verifyBytes(sBlockBytes, sfBytes);
                if (signerCertChain != null) {
                    try {
                        this.certificates.put(signatureFile, signerCertChain);
                    } catch (GeneralSecurityException e) {
                        e = e;
                    }
                }
                Attributes attributes = new Attributes();
                HashMap<String, Attributes> entries2 = new HashMap<>();
                try {
                    new StrictJarManifestReader(sfBytes, attributes).readEntries(entries2, null);
                    if (this.signatureSchemeRollbackProtectionsEnforced && (apkSignatureSchemeIdList = attributes.getValue(SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME)) != null) {
                        boolean v2SignatureGenerated = false;
                        boolean v3SignatureGenerated = false;
                        StringTokenizer tokenizer = new StringTokenizer(apkSignatureSchemeIdList, SmsManager.REGEX_PREFIX_DELIMITER);
                        while (true) {
                            if (!tokenizer.hasMoreTokens()) {
                                break;
                            }
                            String idText = tokenizer.nextToken().trim();
                            if (!idText.isEmpty()) {
                                try {
                                    int id = Integer.parseInt(idText);
                                    if (id == 2) {
                                        v2SignatureGenerated = true;
                                        break;
                                    } else if (id == 3) {
                                        v3SignatureGenerated = true;
                                        break;
                                    }
                                } catch (Exception e2) {
                                }
                            }
                        }
                        if (v2SignatureGenerated) {
                            throw new SecurityException(signatureFile + " indicates " + this.jarName + " is signed using APK Signature Scheme v2, but no such signature was found. Signature stripped?");
                        } else if (v3SignatureGenerated) {
                            throw new SecurityException(signatureFile + " indicates " + this.jarName + " is signed using APK Signature Scheme v3, but no such signature was found. Signature stripped?");
                        }
                    }
                    if (attributes.get(Attributes.Name.SIGNATURE_VERSION) != null) {
                        boolean createdBySigntool = false;
                        String createdBy = attributes.getValue("Created-By");
                        if (createdBy != null) {
                            createdBySigntool = createdBy.indexOf("signtool") != -1;
                        }
                        int i = this.mainAttributesEnd;
                        if (i <= 0 || createdBySigntool) {
                            entries = entries2;
                        } else {
                            entries = entries2;
                            if (!verify(attributes, "-Digest-Manifest-Main-Attributes", manifestBytes, 0, i, false, true)) {
                                throw failedVerification(this.jarName, signatureFile);
                            }
                        }
                        if (!verify(attributes, createdBySigntool ? "-Digest" : "-Digest-Manifest", manifestBytes, 0, manifestBytes.length, false, false)) {
                            for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
                                StrictJarManifest.Chunk chunk = this.manifest.getChunk(entry.getKey());
                                if (chunk == null) {
                                    return;
                                }
                                if (verify(entry.getValue(), "-Digest", manifestBytes, chunk.start, chunk.end, createdBySigntool, false)) {
                                    attributes = attributes;
                                    sBlockBytes = sBlockBytes;
                                    manifestBytes = manifestBytes;
                                } else {
                                    throw invalidDigest(signatureFile, entry.getKey(), this.jarName);
                                }
                            }
                        }
                        this.metaEntries.put(signatureFile, null);
                        this.signatures.put(signatureFile, entries);
                    }
                } catch (IOException e3) {
                }
            } catch (GeneralSecurityException e4) {
                e = e4;
                throw failedVerification(this.jarName, signatureFile, e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSignedJar() {
        return this.certificates.size() > 0;
    }

    private boolean verify(Attributes attributes, String entry, byte[] data, int start, int end, boolean ignoreSecondEndline, boolean ignorable) {
        int i = 0;
        while (true) {
            String[] strArr = DIGEST_ALGORITHMS;
            if (i >= strArr.length) {
                return ignorable;
            }
            String algorithm = strArr[i];
            String hash = attributes.getValue(algorithm + entry);
            if (hash != null) {
                try {
                    MessageDigest md = MessageDigest.getInstance(algorithm);
                    if (ignoreSecondEndline && data[end - 1] == 10 && data[end - 2] == 10) {
                        md.update(data, start, (end - 1) - start);
                    } else {
                        md.update(data, start, end - start);
                    }
                    return verifyMessageDigest(md.digest(), hash.getBytes(StandardCharsets.ISO_8859_1));
                } catch (NoSuchAlgorithmException e) {
                }
            }
            i++;
        }
    }

    /* access modifiers changed from: private */
    public static boolean verifyMessageDigest(byte[] expected, byte[] encodedActual) {
        try {
            return MessageDigest.isEqual(expected, Base64.getDecoder().decode(encodedActual));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public Certificate[][] getCertificateChains(String name) {
        return this.verifiedEntries.get(name);
    }

    /* access modifiers changed from: package-private */
    public void removeMetaEntries() {
        this.metaEntries.clear();
    }
}

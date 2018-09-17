package android.util.jar;

import android.security.keystore.KeyProperties;
import android.util.apk.ApkSignatureSchemeV2Verifier;
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
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import sun.security.jca.Providers;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

class StrictJarVerifier {
    private static final String[] DIGEST_ALGORITHMS = new String[]{KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA256, "SHA1"};
    private final Hashtable<String, Certificate[]> certificates = new Hashtable(5);
    private final String jarName;
    private final int mainAttributesEnd;
    private final StrictJarManifest manifest;
    private final HashMap<String, byte[]> metaEntries;
    private final boolean signatureSchemeRollbackProtectionsEnforced;
    private final Hashtable<String, HashMap<String, Attributes>> signatures = new Hashtable(5);
    private final Hashtable<String, Certificate[][]> verifiedEntries = new Hashtable();

    static class VerifierEntry extends OutputStream {
        private final Certificate[][] certChains;
        private final MessageDigest digest;
        private final byte[] hash;
        private final String name;
        private final Hashtable<String, Certificate[][]> verifiedEntries;

        VerifierEntry(String name, MessageDigest digest, byte[] hash, Certificate[][] certChains, Hashtable<String, Certificate[][]> verifedEntries) {
            this.name = name;
            this.digest = digest;
            this.hash = hash;
            this.certChains = certChains;
            this.verifiedEntries = verifedEntries;
        }

        public void write(int value) {
            this.digest.update((byte) value);
        }

        public void write(byte[] buf, int off, int nbytes) {
            this.digest.update(buf, off, nbytes);
        }

        void verify() {
            if (StrictJarVerifier.verifyMessageDigest(this.digest.digest(), this.hash)) {
                this.verifiedEntries.put(this.name, this.certChains);
                return;
            }
            throw StrictJarVerifier.invalidDigest("META-INF/MANIFEST.MF", this.name, this.name);
        }
    }

    private static SecurityException invalidDigest(String signatureFile, String name, String jarName) {
        throw new SecurityException(signatureFile + " has invalid digest for " + name + " in " + jarName);
    }

    private static SecurityException failedVerification(String jarName, String signatureFile) {
        throw new SecurityException(jarName + " failed verification of " + signatureFile);
    }

    private static SecurityException failedVerification(String jarName, String signatureFile, Throwable e) {
        throw new SecurityException(jarName + " failed verification of " + signatureFile, e);
    }

    StrictJarVerifier(String name, StrictJarManifest manifest, HashMap<String, byte[]> metaEntries, boolean signatureSchemeRollbackProtectionsEnforced) {
        this.jarName = name;
        this.manifest = manifest;
        this.metaEntries = metaEntries;
        this.mainAttributesEnd = manifest.getMainAttributesEnd();
        this.signatureSchemeRollbackProtectionsEnforced = signatureSchemeRollbackProtectionsEnforced;
    }

    VerifierEntry initEntry(String name) {
        if (this.manifest == null || this.signatures.isEmpty()) {
            return null;
        }
        Attributes attributes = this.manifest.getAttributes(name);
        if (attributes == null) {
            return null;
        }
        ArrayList<Certificate[]> certChains = new ArrayList();
        for (Entry<String, HashMap<String, Attributes>> entry : this.signatures.entrySet()) {
            if (((HashMap) entry.getValue()).get(name) != null) {
                Certificate[] certChain = (Certificate[]) this.certificates.get((String) entry.getKey());
                if (certChain != null) {
                    certChains.add(certChain);
                }
            }
        }
        if (certChains.isEmpty()) {
            return null;
        }
        Certificate[][] certChainsArray = (Certificate[][]) certChains.toArray(new Certificate[certChains.size()][]);
        for (String algorithm : DIGEST_ALGORITHMS) {
            String hash = attributes.getValue(algorithm + "-Digest");
            if (hash != null) {
                try {
                    return new VerifierEntry(name, MessageDigest.getInstance(algorithm), hash.getBytes(StandardCharsets.ISO_8859_1), certChainsArray, this.verifiedEntries);
                } catch (NoSuchAlgorithmException e) {
                }
            }
        }
        return null;
    }

    void addMetaEntry(String name, byte[] buf) {
        this.metaEntries.put(name.toUpperCase(Locale.US), buf);
    }

    synchronized boolean readCertificates() {
        if (this.metaEntries.isEmpty()) {
            return false;
        }
        Iterator<String> it = this.metaEntries.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.endsWith(".DSA") || key.endsWith(".RSA") || key.endsWith(".EC")) {
                verifyCertificate(key);
                it.remove();
            }
        }
        return true;
    }

    static Certificate[] verifyBytes(byte[] blockBytes, byte[] sfBytes) throws GeneralSecurityException {
        Object obj = null;
        try {
            obj = Providers.startJarVerification();
            PKCS7 block = new PKCS7(blockBytes);
            SignerInfo[] verifiedSignerInfos = block.verify(sfBytes);
            if (verifiedSignerInfos == null || verifiedSignerInfos.length == 0) {
                throw new GeneralSecurityException("Failed to verify signature: no verified SignerInfos");
            }
            List<X509Certificate> verifiedSignerCertChain = verifiedSignerInfos[0].getCertificateChain(block);
            if (verifiedSignerCertChain == null) {
                throw new GeneralSecurityException("Failed to find verified SignerInfo certificate chain");
            } else if (verifiedSignerCertChain.isEmpty()) {
                throw new GeneralSecurityException("Verified SignerInfo certificate chain is emtpy");
            } else {
                Certificate[] certificateArr = (Certificate[]) verifiedSignerCertChain.toArray(new X509Certificate[verifiedSignerCertChain.size()]);
                Providers.stopJarVerification(obj);
                return certificateArr;
            }
        } catch (IOException e) {
            throw new GeneralSecurityException("IO exception verifying jar cert", e);
        } catch (Throwable th) {
            Providers.stopJarVerification(obj);
        }
    }

    private void verifyCertificate(String certFile) {
        String signatureFile = certFile.substring(0, certFile.lastIndexOf(46)) + ".SF";
        byte[] sfBytes = (byte[]) this.metaEntries.get(signatureFile);
        if (sfBytes != null) {
            byte[] manifestBytes = (byte[]) this.metaEntries.get("META-INF/MANIFEST.MF");
            if (manifestBytes != null) {
                try {
                    Object signerCertChain = verifyBytes((byte[]) this.metaEntries.get(certFile), sfBytes);
                    if (signerCertChain != null) {
                        this.certificates.put(signatureFile, signerCertChain);
                    }
                    Attributes attributes = new Attributes();
                    HashMap<String, Attributes> entries = new HashMap();
                    try {
                        new StrictJarManifestReader(sfBytes, attributes).readEntries(entries, null);
                        if (this.signatureSchemeRollbackProtectionsEnforced) {
                            String apkSignatureSchemeIdList = attributes.getValue(ApkSignatureSchemeV2Verifier.SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME);
                            if (apkSignatureSchemeIdList != null) {
                                boolean v2SignatureGenerated = false;
                                StringTokenizer stringTokenizer = new StringTokenizer(apkSignatureSchemeIdList, ",");
                                while (stringTokenizer.hasMoreTokens()) {
                                    String idText = stringTokenizer.nextToken().trim();
                                    if (!idText.isEmpty()) {
                                        try {
                                            if (Integer.parseInt(idText) == 2) {
                                                v2SignatureGenerated = true;
                                                break;
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                if (v2SignatureGenerated) {
                                    throw new SecurityException(signatureFile + " indicates " + this.jarName + " is signed using APK Signature Scheme v2, but no such signature was" + " found. Signature stripped?");
                                }
                            }
                        }
                        if (attributes.get(Name.SIGNATURE_VERSION) != null) {
                            boolean createdBySigntool = false;
                            String createdBy = attributes.getValue("Created-By");
                            if (createdBy != null) {
                                createdBySigntool = createdBy.indexOf("signtool") != -1;
                            }
                            if (this.mainAttributesEnd > 0 && (createdBySigntool ^ 1) != 0) {
                                if (!verify(attributes, "-Digest-Manifest-Main-Attributes", manifestBytes, 0, this.mainAttributesEnd, false, true)) {
                                    throw failedVerification(this.jarName, signatureFile);
                                }
                            }
                            if (!verify(attributes, createdBySigntool ? "-Digest" : "-Digest-Manifest", manifestBytes, 0, manifestBytes.length, false, false)) {
                                for (Entry<String, Attributes> entry : entries.entrySet()) {
                                    Chunk chunk = this.manifest.getChunk((String) entry.getKey());
                                    if (chunk != null) {
                                        if (!verify((Attributes) entry.getValue(), "-Digest", manifestBytes, chunk.start, chunk.end, createdBySigntool, false)) {
                                            throw invalidDigest(signatureFile, (String) entry.getKey(), this.jarName);
                                        }
                                    }
                                    return;
                                }
                            }
                            this.metaEntries.put(signatureFile, null);
                            this.signatures.put(signatureFile, entries);
                        }
                    } catch (IOException e2) {
                    }
                } catch (Throwable e3) {
                    throw failedVerification(this.jarName, signatureFile, e3);
                }
            }
        }
    }

    boolean isSignedJar() {
        return this.certificates.size() > 0;
    }

    private boolean verify(Attributes attributes, String entry, byte[] data, int start, int end, boolean ignoreSecondEndline, boolean ignorable) {
        for (String algorithm : DIGEST_ALGORITHMS) {
            String hash = attributes.getValue(algorithm + entry);
            if (hash != null) {
                try {
                    MessageDigest md = MessageDigest.getInstance(algorithm);
                    if (ignoreSecondEndline && data[end - 1] == (byte) 10 && data[end - 2] == (byte) 10) {
                        md.update(data, start, (end - 1) - start);
                    } else {
                        md.update(data, start, end - start);
                    }
                    return verifyMessageDigest(md.digest(), hash.getBytes(StandardCharsets.ISO_8859_1));
                } catch (NoSuchAlgorithmException e) {
                }
            }
        }
        return ignorable;
    }

    private static boolean verifyMessageDigest(byte[] expected, byte[] encodedActual) {
        try {
            return MessageDigest.isEqual(expected, Base64.getDecoder().decode(encodedActual));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    Certificate[][] getCertificateChains(String name) {
        return (Certificate[][]) this.verifiedEntries.get(name);
    }

    void removeMetaEntries() {
        this.metaEntries.clear();
    }
}

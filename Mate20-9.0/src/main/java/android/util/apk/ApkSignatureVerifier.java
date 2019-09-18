package android.util.apk;

import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.os.Trace;
import android.security.keymaster.KeymasterDefs;
import android.util.apk.ApkSignatureSchemeV3Verifier;
import android.util.jar.StrictJarFile;
import com.android.internal.util.ArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;

public class ApkSignatureVerifier {
    private static final AtomicReference<byte[]> sBuffer = new AtomicReference<>();

    public static class Result {
        public final Certificate[][] certs;
        public final int signatureSchemeVersion;
        public final Signature[] sigs;

        public Result(Certificate[][] certs2, Signature[] sigs2, int signingVersion) {
            this.certs = certs2;
            this.sigs = sigs2;
            this.signatureSchemeVersion = signingVersion;
        }
    }

    public static PackageParser.SigningDetails verify(String apkPath, @PackageParser.SigningDetails.SignatureSchemeVersion int minSignatureSchemeVersion) throws PackageParser.PackageParserException {
        if (minSignatureSchemeVersion <= 3) {
            Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "verifyV3");
            try {
                ApkSignatureSchemeV3Verifier.VerifiedSigner vSigner = ApkSignatureSchemeV3Verifier.verify(apkPath);
                Signature[] signerSigs = convertToSignatures(new Certificate[][]{vSigner.certs});
                Signature[] pastSignerSigs = null;
                int[] pastSignerSigsFlags = null;
                if (vSigner.por != null) {
                    pastSignerSigs = new Signature[vSigner.por.certs.size()];
                    pastSignerSigsFlags = new int[vSigner.por.flagsList.size()];
                    for (int i = 0; i < pastSignerSigs.length; i++) {
                        pastSignerSigs[i] = new Signature(vSigner.por.certs.get(i).getEncoded());
                        pastSignerSigsFlags[i] = vSigner.por.flagsList.get(i).intValue();
                    }
                }
                PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(signerSigs, 3, pastSignerSigs, pastSignerSigsFlags);
                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                return signingDetails;
            } catch (SignatureNotFoundException e) {
                if (minSignatureSchemeVersion < 3) {
                    Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                    if (minSignatureSchemeVersion <= 2) {
                        Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "verifyV2");
                        try {
                            PackageParser.SigningDetails signingDetails2 = new PackageParser.SigningDetails(convertToSignatures(ApkSignatureSchemeV2Verifier.verify(apkPath)), 2);
                            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                            return signingDetails2;
                        } catch (SignatureNotFoundException e2) {
                            if (minSignatureSchemeVersion < 2) {
                                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                                if (minSignatureSchemeVersion <= 1) {
                                    return verifyV1Signature(apkPath, true);
                                }
                                throw new PackageParser.PackageParserException(-103, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                            }
                            throw new PackageParser.PackageParserException(-103, "No APK Signature Scheme v2 signature in package " + apkPath, e2);
                        } catch (Exception e3) {
                            throw new PackageParser.PackageParserException(-103, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e3);
                        } catch (Throwable th) {
                            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                            throw th;
                        }
                    } else {
                        throw new PackageParser.PackageParserException(-103, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                    }
                } else {
                    throw new PackageParser.PackageParserException(-103, "No APK Signature Scheme v3 signature in package " + apkPath, e);
                }
            } catch (Exception e4) {
                throw new PackageParser.PackageParserException(-103, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v3", e4);
            } catch (Throwable th2) {
                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                throw th2;
            }
        } else {
            throw new PackageParser.PackageParserException(-103, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
        }
    }

    private static PackageParser.SigningDetails verifyV1Signature(String apkPath, boolean verifyFull) throws PackageParser.PackageParserException {
        String str = apkPath;
        boolean z = verifyFull;
        StrictJarFile jarFile = null;
        try {
            Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "strictJarFileCtor");
            jarFile = new StrictJarFile(str, true, z);
            List<ZipEntry> toVerify = new ArrayList<>();
            ZipEntry manifestEntry = jarFile.findEntry("AndroidManifest.xml");
            if (manifestEntry != null) {
                Certificate[][] lastCerts = loadCertificates(jarFile, manifestEntry);
                if (!ArrayUtils.isEmpty(lastCerts)) {
                    Signature[] lastSigs = convertToSignatures(lastCerts);
                    if (z) {
                        Iterator<ZipEntry> i = jarFile.iterator();
                        while (i.hasNext()) {
                            ZipEntry entry = i.next();
                            if (!entry.isDirectory()) {
                                String entryName = entry.getName();
                                if (!entryName.startsWith("META-INF/")) {
                                    if (!entryName.equals("AndroidManifest.xml")) {
                                        toVerify.add(entry);
                                    }
                                }
                            }
                        }
                        for (ZipEntry entry2 : toVerify) {
                            Certificate[][] entryCerts = loadCertificates(jarFile, entry2);
                            if (ArrayUtils.isEmpty(entryCerts)) {
                                throw new PackageParser.PackageParserException(-103, "Package " + str + " has no certificates at entry " + entry2.getName());
                            } else if (!Signature.areExactMatch(lastSigs, convertToSignatures(entryCerts))) {
                                throw new PackageParser.PackageParserException(-104, "Package " + str + " has mismatched certificates at entry " + entry2.getName());
                            }
                        }
                    }
                    PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(lastSigs, 1);
                    Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                    closeQuietly(jarFile);
                    return signingDetails;
                }
                throw new PackageParser.PackageParserException(-103, "Package " + str + " has no certificates at entry " + "AndroidManifest.xml");
            }
            throw new PackageParser.PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Package " + str + " has no manifest");
        } catch (GeneralSecurityException e) {
            throw new PackageParser.PackageParserException(-105, "Failed to collect certificates from " + str, e);
        } catch (IOException | RuntimeException e2) {
            throw new PackageParser.PackageParserException(-103, "Failed to collect certificates from " + str, e2);
        } catch (Throwable th) {
            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
            closeQuietly(jarFile);
            throw th;
        }
    }

    private static Certificate[][] loadCertificates(StrictJarFile jarFile, ZipEntry entry) throws PackageParser.PackageParserException {
        try {
            InputStream is = jarFile.getInputStream(entry);
            readFullyIgnoringContents(is);
            Certificate[][] certificateChains = jarFile.getCertificateChains(entry);
            IoUtils.closeQuietly(is);
            return certificateChains;
        } catch (IOException | RuntimeException e) {
            throw new PackageParser.PackageParserException(-102, "Failed reading " + entry.getName() + " in " + jarFile, e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
    }

    private static void readFullyIgnoringContents(InputStream in) throws IOException {
        byte[] buffer = sBuffer.getAndSet(null);
        if (buffer == null) {
            buffer = new byte[4096];
        }
        int count = 0;
        while (true) {
            int read = in.read(buffer, 0, buffer.length);
            int n = read;
            if (read != -1) {
                count += n;
            } else {
                sBuffer.set(buffer);
                return;
            }
        }
    }

    public static Signature[] convertToSignatures(Certificate[][] certs) throws CertificateEncodingException {
        Signature[] res = new Signature[certs.length];
        for (int i = 0; i < certs.length; i++) {
            res[i] = new Signature(certs[i]);
        }
        return res;
    }

    private static void closeQuietly(StrictJarFile jarFile) {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (Exception e) {
            }
        }
    }

    public static PackageParser.SigningDetails plsCertsNoVerifyOnlyCerts(String apkPath, int minSignatureSchemeVersion) throws PackageParser.PackageParserException {
        if (minSignatureSchemeVersion <= 3) {
            Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "certsOnlyV3");
            try {
                ApkSignatureSchemeV3Verifier.VerifiedSigner vSigner = ApkSignatureSchemeV3Verifier.plsCertsNoVerifyOnlyCerts(apkPath);
                Signature[] signerSigs = convertToSignatures(new Certificate[][]{vSigner.certs});
                Signature[] pastSignerSigs = null;
                int[] pastSignerSigsFlags = null;
                if (vSigner.por != null) {
                    pastSignerSigs = new Signature[vSigner.por.certs.size()];
                    pastSignerSigsFlags = new int[vSigner.por.flagsList.size()];
                    for (int i = 0; i < pastSignerSigs.length; i++) {
                        pastSignerSigs[i] = new Signature(vSigner.por.certs.get(i).getEncoded());
                        pastSignerSigsFlags[i] = vSigner.por.flagsList.get(i).intValue();
                    }
                }
                PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(signerSigs, 3, pastSignerSigs, pastSignerSigsFlags);
                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                return signingDetails;
            } catch (SignatureNotFoundException e) {
                if (minSignatureSchemeVersion < 3) {
                    Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                    if (minSignatureSchemeVersion <= 2) {
                        Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "certsOnlyV2");
                        try {
                            PackageParser.SigningDetails signingDetails2 = new PackageParser.SigningDetails(convertToSignatures(ApkSignatureSchemeV2Verifier.plsCertsNoVerifyOnlyCerts(apkPath)), 2);
                            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                            return signingDetails2;
                        } catch (SignatureNotFoundException e2) {
                            if (minSignatureSchemeVersion < 2) {
                                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                                if (minSignatureSchemeVersion <= 1) {
                                    return verifyV1Signature(apkPath, false);
                                }
                                throw new PackageParser.PackageParserException(-103, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                            }
                            throw new PackageParser.PackageParserException(-103, "No APK Signature Scheme v2 signature in package " + apkPath, e2);
                        } catch (Exception e3) {
                            throw new PackageParser.PackageParserException(-103, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e3);
                        } catch (Throwable th) {
                            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                            throw th;
                        }
                    } else {
                        throw new PackageParser.PackageParserException(-103, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                    }
                } else {
                    throw new PackageParser.PackageParserException(-103, "No APK Signature Scheme v3 signature in package " + apkPath, e);
                }
            } catch (Exception e4) {
                throw new PackageParser.PackageParserException(-103, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v3", e4);
            } catch (Throwable th2) {
                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                throw th2;
            }
        } else {
            throw new PackageParser.PackageParserException(-103, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
        }
    }

    public static byte[] getVerityRootHash(String apkPath) throws IOException, SignatureNotFoundException, SecurityException {
        try {
            return ApkSignatureSchemeV3Verifier.getVerityRootHash(apkPath);
        } catch (SignatureNotFoundException e) {
            return ApkSignatureSchemeV2Verifier.getVerityRootHash(apkPath);
        }
    }

    public static byte[] generateApkVerity(String apkPath, ByteBufferFactory bufferFactory) throws IOException, SignatureNotFoundException, SecurityException, DigestException, NoSuchAlgorithmException {
        try {
            return ApkSignatureSchemeV3Verifier.generateApkVerity(apkPath, bufferFactory);
        } catch (SignatureNotFoundException e) {
            return ApkSignatureSchemeV2Verifier.generateApkVerity(apkPath, bufferFactory);
        }
    }

    public static byte[] generateFsverityRootHash(String apkPath) throws NoSuchAlgorithmException, DigestException, IOException {
        try {
            return ApkSignatureSchemeV3Verifier.generateFsverityRootHash(apkPath);
        } catch (SignatureNotFoundException e) {
            try {
                return ApkSignatureSchemeV2Verifier.generateFsverityRootHash(apkPath);
            } catch (SignatureNotFoundException e2) {
                return null;
            }
        }
    }
}

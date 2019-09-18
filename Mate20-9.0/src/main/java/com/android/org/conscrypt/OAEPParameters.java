package com.android.org.conscrypt;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class OAEPParameters extends AlgorithmParametersSpi {
    private static final String MGF1_OID = "1.2.840.113549.1.1.8";
    private static final Map<String, String> NAME_TO_OID = new HashMap();
    private static final Map<String, String> OID_TO_NAME = new HashMap();
    private static final String PSPECIFIED_OID = "1.2.840.113549.1.1.9";
    private OAEPParameterSpec spec = OAEPParameterSpec.DEFAULT;

    static {
        OID_TO_NAME.put("1.3.14.3.2.26", "SHA-1");
        OID_TO_NAME.put("2.16.840.1.101.3.4.2.4", "SHA-224");
        OID_TO_NAME.put("2.16.840.1.101.3.4.2.1", "SHA-256");
        OID_TO_NAME.put("2.16.840.1.101.3.4.2.2", "SHA-384");
        OID_TO_NAME.put("2.16.840.1.101.3.4.2.3", "SHA-512");
        for (Map.Entry<String, String> entry : OID_TO_NAME.entrySet()) {
            NAME_TO_OID.put(entry.getValue(), entry.getKey());
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        if (algorithmParameterSpec instanceof OAEPParameterSpec) {
            this.spec = (OAEPParameterSpec) algorithmParameterSpec;
            return;
        }
        throw new InvalidParameterSpecException("Only OAEPParameterSpec is supported");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes) throws IOException {
        long pSourceRef;
        long mgfRef;
        long mgfSeqRef;
        long readRef = 0;
        long pSourceSeqRef = 0;
        long seqRef = 0;
        try {
            readRef = NativeCrypto.asn1_read_init(bytes);
            seqRef = NativeCrypto.asn1_read_sequence(readRef);
            String hash = "SHA-1";
            String mgfHash = "SHA-1";
            PSource.PSpecified pSpecified = PSource.PSpecified.DEFAULT;
            if (NativeCrypto.asn1_read_next_tag_is(seqRef, 0)) {
                long hashRef = NativeCrypto.asn1_read_tagged(seqRef);
                hash = getHashName(hashRef);
                NativeCrypto.asn1_read_free(hashRef);
            }
            if (NativeCrypto.asn1_read_next_tag_is(seqRef, 1)) {
                mgfRef = 0;
                mgfSeqRef = 0;
                mgfRef = NativeCrypto.asn1_read_tagged(seqRef);
                mgfSeqRef = NativeCrypto.asn1_read_sequence(mgfRef);
                if (NativeCrypto.asn1_read_oid(mgfSeqRef).equals(MGF1_OID)) {
                    mgfHash = getHashName(mgfSeqRef);
                    if (NativeCrypto.asn1_read_is_empty(mgfSeqRef)) {
                        NativeCrypto.asn1_read_free(mgfSeqRef);
                        NativeCrypto.asn1_read_free(mgfRef);
                    } else {
                        throw new IOException("Error reading ASN.1 encoding");
                    }
                } else {
                    throw new IOException("Error reading ASN.1 encoding");
                }
            }
            if (NativeCrypto.asn1_read_next_tag_is(seqRef, 2)) {
                pSourceRef = 0;
                pSourceRef = NativeCrypto.asn1_read_tagged(seqRef);
                pSourceSeqRef = NativeCrypto.asn1_read_sequence(pSourceRef);
                if (NativeCrypto.asn1_read_oid(pSourceSeqRef).equals(PSPECIFIED_OID)) {
                    pSpecified = new PSource.PSpecified(NativeCrypto.asn1_read_octetstring(pSourceSeqRef));
                    if (NativeCrypto.asn1_read_is_empty(pSourceSeqRef)) {
                        NativeCrypto.asn1_read_free(pSourceSeqRef);
                        NativeCrypto.asn1_read_free(pSourceRef);
                    } else {
                        throw new IOException("Error reading ASN.1 encoding");
                    }
                } else {
                    throw new IOException("Error reading ASN.1 encoding");
                }
            }
            if (!NativeCrypto.asn1_read_is_empty(seqRef) || !NativeCrypto.asn1_read_is_empty(readRef)) {
                throw new IOException("Error reading ASN.1 encoding");
            }
            try {
                this.spec = new OAEPParameterSpec(hash, "MGF1", new MGF1ParameterSpec(mgfHash), pSpecified);
                NativeCrypto.asn1_read_free(seqRef);
                NativeCrypto.asn1_read_free(readRef);
            } catch (Throwable th) {
                th = th;
                NativeCrypto.asn1_read_free(seqRef);
                NativeCrypto.asn1_read_free(readRef);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes, String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            engineInit(bytes);
            return;
        }
        throw new IOException("Unsupported format: " + format);
    }

    private static String getHashName(long hashRef) throws IOException {
        long hashSeqRef = 0;
        try {
            hashSeqRef = NativeCrypto.asn1_read_sequence(hashRef);
            String hashOid = NativeCrypto.asn1_read_oid(hashSeqRef);
            if (!NativeCrypto.asn1_read_is_empty(hashSeqRef)) {
                NativeCrypto.asn1_read_null(hashSeqRef);
            }
            if (NativeCrypto.asn1_read_is_empty(hashSeqRef) && OID_TO_NAME.containsKey(hashOid)) {
                return OID_TO_NAME.get(hashOid);
            }
            throw new IOException("Error reading ASN.1 encoding");
        } finally {
            NativeCrypto.asn1_read_free(hashSeqRef);
        }
    }

    /* access modifiers changed from: protected */
    public <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> aClass) throws InvalidParameterSpecException {
        if (aClass != null && aClass == OAEPParameterSpec.class) {
            return this.spec;
        }
        throw new InvalidParameterSpecException("Unsupported class: " + aClass);
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() throws IOException {
        long pSourceRef;
        long mgfRef;
        long mgfParamsRef;
        long hashParamsRef;
        long hashRef;
        long hashParamsRef2;
        long cbbRef = 0;
        long pSourceParamsRef = 0;
        long seqRef = 0;
        try {
            cbbRef = NativeCrypto.asn1_write_init();
            seqRef = NativeCrypto.asn1_write_sequence(cbbRef);
            if (!this.spec.getDigestAlgorithm().equals("SHA-1")) {
                hashRef = 0;
                hashParamsRef2 = 0;
                hashRef = NativeCrypto.asn1_write_tag(seqRef, 0);
                hashParamsRef2 = writeAlgorithmIdentifier(hashRef, NAME_TO_OID.get(this.spec.getDigestAlgorithm()));
                NativeCrypto.asn1_write_null(hashParamsRef2);
                NativeCrypto.asn1_write_flush(seqRef);
                NativeCrypto.asn1_write_free(hashParamsRef2);
                NativeCrypto.asn1_write_free(hashRef);
            }
            MGF1ParameterSpec mgfSpec = (MGF1ParameterSpec) this.spec.getMGFParameters();
            if (!mgfSpec.getDigestAlgorithm().equals("SHA-1")) {
                mgfRef = 0;
                mgfParamsRef = 0;
                hashParamsRef = 0;
                mgfRef = NativeCrypto.asn1_write_tag(seqRef, 1);
                mgfParamsRef = writeAlgorithmIdentifier(mgfRef, MGF1_OID);
                hashParamsRef = writeAlgorithmIdentifier(mgfParamsRef, NAME_TO_OID.get(mgfSpec.getDigestAlgorithm()));
                NativeCrypto.asn1_write_null(hashParamsRef);
                NativeCrypto.asn1_write_flush(seqRef);
                NativeCrypto.asn1_write_free(hashParamsRef);
                NativeCrypto.asn1_write_free(mgfParamsRef);
                NativeCrypto.asn1_write_free(mgfRef);
            }
            PSource.PSpecified pSource = (PSource.PSpecified) this.spec.getPSource();
            if (pSource.getValue().length != 0) {
                pSourceRef = 0;
                pSourceRef = NativeCrypto.asn1_write_tag(seqRef, 2);
                pSourceParamsRef = writeAlgorithmIdentifier(pSourceRef, PSPECIFIED_OID);
                NativeCrypto.asn1_write_octetstring(pSourceParamsRef, pSource.getValue());
                NativeCrypto.asn1_write_flush(seqRef);
                NativeCrypto.asn1_write_free(pSourceParamsRef);
                NativeCrypto.asn1_write_free(pSourceRef);
            }
            byte[] asn1_write_finish = NativeCrypto.asn1_write_finish(cbbRef);
            NativeCrypto.asn1_write_free(seqRef);
            NativeCrypto.asn1_write_free(cbbRef);
            return asn1_write_finish;
        } catch (IOException e) {
            try {
                NativeCrypto.asn1_write_cleanup(cbbRef);
                throw e;
            } catch (Throwable th) {
                NativeCrypto.asn1_write_free(seqRef);
                NativeCrypto.asn1_write_free(cbbRef);
                throw th;
            }
        } catch (Throwable th2) {
            NativeCrypto.asn1_write_flush(seqRef);
            NativeCrypto.asn1_write_free(hashParamsRef2);
            NativeCrypto.asn1_write_free(hashRef);
            throw th2;
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            return engineGetEncoded();
        }
        throw new IOException("Unsupported format: " + format);
    }

    private static long writeAlgorithmIdentifier(long container, String oid) throws IOException {
        long seqRef = 0;
        try {
            seqRef = NativeCrypto.asn1_write_sequence(container);
            NativeCrypto.asn1_write_oid(seqRef, oid);
            return seqRef;
        } catch (IOException e) {
            NativeCrypto.asn1_write_free(seqRef);
            throw e;
        }
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "Conscrypt OAEP AlgorithmParameters";
    }
}

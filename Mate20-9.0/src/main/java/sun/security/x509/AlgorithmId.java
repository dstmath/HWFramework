package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.util.locale.LanguageTag;

public class AlgorithmId implements Serializable, DerEncoder {
    public static final ObjectIdentifier AES_oid = oid(2, 16, 840, 1, 101, 3, 4, 1);
    private static final int[] DH_PKIX_data = {1, 2, 840, 10046, 2, 1};
    public static final ObjectIdentifier DH_PKIX_oid = ObjectIdentifier.newInternal(DH_PKIX_data);
    private static final int[] DH_data = {1, 2, 840, 113549, 1, 3, 1};
    public static final ObjectIdentifier DH_oid = ObjectIdentifier.newInternal(DH_data);
    private static final int[] DSA_OIW_data = {1, 3, 14, 3, 2, 12};
    public static final ObjectIdentifier DSA_OIW_oid = ObjectIdentifier.newInternal(DSA_OIW_data);
    private static final int[] DSA_PKIX_data = {1, 2, 840, 10040, 4, 1};
    public static final ObjectIdentifier DSA_oid = ObjectIdentifier.newInternal(DSA_PKIX_data);
    public static final ObjectIdentifier ECDH_oid = oid(1, 3, 132, 1, 12);
    public static final ObjectIdentifier EC_oid = oid(1, 2, 840, 10045, 2, 1);
    public static final ObjectIdentifier MD2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 2});
    public static final ObjectIdentifier MD5_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 5});
    private static final int[] RSAEncryption_data = {1, 2, 840, 113549, 1, 1, 1};
    public static final ObjectIdentifier RSAEncryption_oid = ObjectIdentifier.newInternal(RSAEncryption_data);
    private static final int[] RSA_data = {2, 5, 8, 1, 1};
    public static final ObjectIdentifier RSA_oid = ObjectIdentifier.newInternal(RSA_data);
    public static final ObjectIdentifier SHA224_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 4});
    public static final ObjectIdentifier SHA256_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 1});
    public static final ObjectIdentifier SHA384_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 2});
    public static final ObjectIdentifier SHA512_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 3});
    public static final ObjectIdentifier SHA_oid = ObjectIdentifier.newInternal(new int[]{1, 3, 14, 3, 2, 26});
    private static final int[] dsaWithSHA1_PKIX_data = {1, 2, 840, 10040, 4, 3};
    private static int initOidTableVersion = -1;
    private static final int[] md2WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 2};
    public static final ObjectIdentifier md2WithRSAEncryption_oid = ObjectIdentifier.newInternal(md2WithRSAEncryption_data);
    private static final int[] md5WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 4};
    public static final ObjectIdentifier md5WithRSAEncryption_oid = ObjectIdentifier.newInternal(md5WithRSAEncryption_data);
    private static final Map<ObjectIdentifier, String> nameTable = new HashMap();
    private static final Map<String, ObjectIdentifier> oidTable = new HashMap(1);
    public static final ObjectIdentifier pbeWithMD5AndDES_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 3});
    public static final ObjectIdentifier pbeWithMD5AndRC2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 6});
    public static final ObjectIdentifier pbeWithSHA1AndDES_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 10});
    public static ObjectIdentifier pbeWithSHA1AndDESede_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 3});
    public static ObjectIdentifier pbeWithSHA1AndRC2_40_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 6});
    public static final ObjectIdentifier pbeWithSHA1AndRC2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 11});
    private static final long serialVersionUID = 7205873507486557157L;
    private static final int[] sha1WithDSA_OIW_data = {1, 3, 14, 3, 2, 27};
    public static final ObjectIdentifier sha1WithDSA_OIW_oid = ObjectIdentifier.newInternal(sha1WithDSA_OIW_data);
    public static final ObjectIdentifier sha1WithDSA_oid = ObjectIdentifier.newInternal(dsaWithSHA1_PKIX_data);
    public static final ObjectIdentifier sha1WithECDSA_oid = oid(1, 2, 840, 10045, 4, 1);
    private static final int[] sha1WithRSAEncryption_OIW_data = {1, 3, 14, 3, 2, 29};
    public static final ObjectIdentifier sha1WithRSAEncryption_OIW_oid = ObjectIdentifier.newInternal(sha1WithRSAEncryption_OIW_data);
    private static final int[] sha1WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 5};
    public static final ObjectIdentifier sha1WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha1WithRSAEncryption_data);
    public static final ObjectIdentifier sha224WithDSA_oid = oid(2, 16, 840, 1, 101, 3, 4, 3, 1);
    public static final ObjectIdentifier sha224WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 1);
    private static final int[] sha224WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 14};
    public static final ObjectIdentifier sha224WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha224WithRSAEncryption_data);
    public static final ObjectIdentifier sha256WithDSA_oid = oid(2, 16, 840, 1, 101, 3, 4, 3, 2);
    public static final ObjectIdentifier sha256WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 2);
    private static final int[] sha256WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 11};
    public static final ObjectIdentifier sha256WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha256WithRSAEncryption_data);
    public static final ObjectIdentifier sha384WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 3);
    private static final int[] sha384WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 12};
    public static final ObjectIdentifier sha384WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha384WithRSAEncryption_data);
    public static final ObjectIdentifier sha512WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 4);
    private static final int[] sha512WithRSAEncryption_data = {1, 2, 840, 113549, 1, 1, 13};
    public static final ObjectIdentifier sha512WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha512WithRSAEncryption_data);
    private static final int[] shaWithDSA_OIW_data = {1, 3, 14, 3, 2, 13};
    public static final ObjectIdentifier shaWithDSA_OIW_oid = ObjectIdentifier.newInternal(shaWithDSA_OIW_data);
    public static final ObjectIdentifier specifiedWithECDSA_oid = oid(1, 2, 840, 10045, 4, 3);
    private AlgorithmParameters algParams;
    private ObjectIdentifier algid;
    private boolean constructedFromDer = true;
    protected DerValue params;

    @Deprecated
    public AlgorithmId() {
    }

    public AlgorithmId(ObjectIdentifier oid) {
        this.algid = oid;
    }

    public AlgorithmId(ObjectIdentifier oid, AlgorithmParameters algparams) {
        this.algid = oid;
        this.algParams = algparams;
        this.constructedFromDer = false;
    }

    private AlgorithmId(ObjectIdentifier oid, DerValue params2) throws IOException {
        this.algid = oid;
        this.params = params2;
        if (this.params != null) {
            decodeParams();
        }
    }

    /* access modifiers changed from: protected */
    public void decodeParams() throws IOException {
        try {
            this.algParams = AlgorithmParameters.getInstance(this.algid.toString());
            this.algParams.init(this.params.toByteArray());
        } catch (NoSuchAlgorithmException e) {
            this.algParams = null;
        }
    }

    public final void encode(DerOutputStream out) throws IOException {
        derEncode(out);
    }

    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        bytes.putOID(this.algid);
        if (!this.constructedFromDer) {
            if (this.algParams != null) {
                this.params = new DerValue(this.algParams.getEncoded());
            } else {
                this.params = null;
            }
        }
        if (this.params == null) {
            bytes.putNull();
        } else {
            bytes.putDerValue(this.params);
        }
        tmp.write((byte) 48, bytes);
        out.write(tmp.toByteArray());
    }

    public final byte[] encode() throws IOException {
        DerOutputStream out = new DerOutputStream();
        derEncode(out);
        return out.toByteArray();
    }

    public final ObjectIdentifier getOID() {
        return this.algid;
    }

    public String getName() {
        String algName;
        String algName2 = nameTable.get(this.algid);
        if (algName2 != null) {
            return algName2;
        }
        if (this.params != null && this.algid.equals((Object) specifiedWithECDSA_oid)) {
            try {
                String algName3 = makeSigAlg(parse(new DerValue(getEncodedParams())).getName(), "EC");
            } catch (IOException e) {
            }
        }
        synchronized (oidTable) {
            reinitializeMappingTableLocked();
            algName = nameTable.get(this.algid);
        }
        return algName == null ? this.algid.toString() : algName;
    }

    public AlgorithmParameters getParameters() {
        return this.algParams;
    }

    public byte[] getEncodedParams() throws IOException {
        if (this.params == null) {
            return null;
        }
        return this.params.toByteArray();
    }

    public boolean equals(AlgorithmId other) {
        boolean paramsEqual = this.params == null ? other.params == null : this.params.equals(other.params);
        if (!this.algid.equals((Object) other.algid) || !paramsEqual) {
            return false;
        }
        return true;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof AlgorithmId) {
            return equals((AlgorithmId) other);
        }
        if (other instanceof ObjectIdentifier) {
            return equals((ObjectIdentifier) other);
        }
        return false;
    }

    public final boolean equals(ObjectIdentifier id) {
        return this.algid.equals((Object) id);
    }

    public int hashCode() {
        return (this.algid.toString() + paramsToString()).hashCode();
    }

    /* access modifiers changed from: protected */
    public String paramsToString() {
        if (this.params == null) {
            return "";
        }
        if (this.algParams != null) {
            return this.algParams.toString();
        }
        return ", params unparsed";
    }

    public String toString() {
        return getName() + paramsToString();
    }

    public static AlgorithmId parse(DerValue val) throws IOException {
        DerValue params2;
        if (val.tag == 48) {
            DerInputStream in = val.toDerInputStream();
            ObjectIdentifier algid2 = in.getOID();
            if (in.available() == 0) {
                params2 = null;
            } else {
                params2 = in.getDerValue();
                if (params2.tag == 5) {
                    if (params2.length() == 0) {
                        params2 = null;
                    } else {
                        throw new IOException("invalid NULL");
                    }
                }
                if (in.available() != 0) {
                    throw new IOException("Invalid AlgorithmIdentifier: extra data");
                }
            }
            return new AlgorithmId(algid2, params2);
        }
        throw new IOException("algid parse error, not a sequence");
    }

    @Deprecated
    public static AlgorithmId getAlgorithmId(String algname) throws NoSuchAlgorithmException {
        return get(algname);
    }

    public static AlgorithmId get(String algname) throws NoSuchAlgorithmException {
        try {
            ObjectIdentifier oid = algOID(algname);
            if (oid != null) {
                return new AlgorithmId(oid);
            }
            throw new NoSuchAlgorithmException("unrecognized algorithm name: " + algname);
        } catch (IOException e) {
            throw new NoSuchAlgorithmException("Invalid ObjectIdentifier " + algname);
        }
    }

    public static AlgorithmId get(AlgorithmParameters algparams) throws NoSuchAlgorithmException {
        String algname = algparams.getAlgorithm();
        try {
            ObjectIdentifier oid = algOID(algname);
            if (oid != null) {
                return new AlgorithmId(oid, algparams);
            }
            throw new NoSuchAlgorithmException("unrecognized algorithm name: " + algname);
        } catch (IOException e) {
            throw new NoSuchAlgorithmException("Invalid ObjectIdentifier " + algname);
        }
    }

    private static ObjectIdentifier algOID(String name) throws IOException {
        ObjectIdentifier objectIdentifier;
        if (name.indexOf(46) != -1) {
            if (name.startsWith("OID.")) {
                return new ObjectIdentifier(name.substring("OID.".length()));
            }
            return new ObjectIdentifier(name);
        } else if (name.equalsIgnoreCase("MD5")) {
            return MD5_oid;
        } else {
            if (name.equalsIgnoreCase("MD2")) {
                return MD2_oid;
            }
            if (name.equalsIgnoreCase("SHA") || name.equalsIgnoreCase("SHA1") || name.equalsIgnoreCase("SHA-1")) {
                return SHA_oid;
            }
            if (name.equalsIgnoreCase("SHA-256") || name.equalsIgnoreCase("SHA256")) {
                return SHA256_oid;
            }
            if (name.equalsIgnoreCase("SHA-384") || name.equalsIgnoreCase("SHA384")) {
                return SHA384_oid;
            }
            if (name.equalsIgnoreCase("SHA-512") || name.equalsIgnoreCase("SHA512")) {
                return SHA512_oid;
            }
            if (name.equalsIgnoreCase("SHA-224") || name.equalsIgnoreCase("SHA224")) {
                return SHA224_oid;
            }
            if (name.equalsIgnoreCase("RSA")) {
                return RSAEncryption_oid;
            }
            if (name.equalsIgnoreCase("Diffie-Hellman") || name.equalsIgnoreCase("DH")) {
                return DH_oid;
            }
            if (name.equalsIgnoreCase("DSA")) {
                return DSA_oid;
            }
            if (name.equalsIgnoreCase("EC")) {
                return EC_oid;
            }
            if (name.equalsIgnoreCase("ECDH")) {
                return ECDH_oid;
            }
            if (name.equalsIgnoreCase("AES")) {
                return AES_oid;
            }
            if (name.equalsIgnoreCase("MD5withRSA") || name.equalsIgnoreCase("MD5/RSA")) {
                return md5WithRSAEncryption_oid;
            }
            if (name.equalsIgnoreCase("MD2withRSA") || name.equalsIgnoreCase("MD2/RSA")) {
                return md2WithRSAEncryption_oid;
            }
            if (name.equalsIgnoreCase("SHAwithDSA") || name.equalsIgnoreCase("SHA1withDSA") || name.equalsIgnoreCase("SHA/DSA") || name.equalsIgnoreCase("SHA1/DSA") || name.equalsIgnoreCase("DSAWithSHA1") || name.equalsIgnoreCase("DSS") || name.equalsIgnoreCase("SHA-1/DSA")) {
                return sha1WithDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA224WithDSA")) {
                return sha224WithDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA256WithDSA")) {
                return sha256WithDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA1WithRSA") || name.equalsIgnoreCase("SHA1/RSA")) {
                return sha1WithRSAEncryption_oid;
            }
            if (name.equalsIgnoreCase("SHA1withECDSA") || name.equalsIgnoreCase("ECDSA")) {
                return sha1WithECDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA224withECDSA")) {
                return sha224WithECDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA256withECDSA")) {
                return sha256WithECDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA384withECDSA")) {
                return sha384WithECDSA_oid;
            }
            if (name.equalsIgnoreCase("SHA512withECDSA")) {
                return sha512WithECDSA_oid;
            }
            synchronized (oidTable) {
                reinitializeMappingTableLocked();
                objectIdentifier = oidTable.get(name.toUpperCase(Locale.ENGLISH));
            }
            return objectIdentifier;
        }
    }

    private static void reinitializeMappingTableLocked() {
        int currentVersion = Security.getVersion();
        if (initOidTableVersion != currentVersion) {
            Provider[] provs = Security.getProviders();
            for (int i = 0; i < provs.length; i++) {
                Enumeration<Object> enum_ = provs[i].keys();
                while (enum_.hasMoreElements()) {
                    String alias = (String) enum_.nextElement();
                    String upperCaseAlias = alias.toUpperCase(Locale.ENGLISH);
                    if (upperCaseAlias.startsWith("ALG.ALIAS")) {
                        int indexOf = upperCaseAlias.indexOf("OID.", 0);
                        int index = indexOf;
                        ObjectIdentifier oid = null;
                        if (indexOf != -1) {
                            int index2 = index + "OID.".length();
                            if (index2 == alias.length()) {
                                break;
                            }
                            String oidString = alias.substring(index2);
                            String stdAlgName = provs[i].getProperty(alias);
                            if (stdAlgName != null) {
                                String stdAlgName2 = stdAlgName.toUpperCase(Locale.ENGLISH);
                                try {
                                    oid = new ObjectIdentifier(oidString);
                                } catch (IOException e) {
                                }
                                if (oid != null) {
                                    if (!oidTable.containsKey(stdAlgName2)) {
                                        oidTable.put(stdAlgName2, oid);
                                    }
                                    if (!nameTable.containsKey(oid)) {
                                        nameTable.put(oid, stdAlgName2);
                                    }
                                }
                            }
                        } else {
                            try {
                                oid = new ObjectIdentifier(alias.substring(alias.indexOf(46, "ALG.ALIAS.".length()) + 1));
                            } catch (IOException e2) {
                            }
                            if (oid != null) {
                                String stdAlgName3 = provs[i].getProperty(alias);
                                if (stdAlgName3 != null) {
                                    String stdAlgName4 = stdAlgName3.toUpperCase(Locale.ENGLISH);
                                    if (!oidTable.containsKey(stdAlgName4)) {
                                        oidTable.put(stdAlgName4, oid);
                                    }
                                    if (!nameTable.containsKey(oid)) {
                                        nameTable.put(oid, stdAlgName4);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            initOidTableVersion = currentVersion;
        }
    }

    private static ObjectIdentifier oid(int... values) {
        return ObjectIdentifier.newInternal(values);
    }

    static {
        nameTable.put(MD5_oid, "MD5");
        nameTable.put(MD2_oid, "MD2");
        nameTable.put(SHA_oid, "SHA-1");
        nameTable.put(SHA224_oid, "SHA-224");
        nameTable.put(SHA256_oid, "SHA-256");
        nameTable.put(SHA384_oid, "SHA-384");
        nameTable.put(SHA512_oid, "SHA-512");
        nameTable.put(RSAEncryption_oid, "RSA");
        nameTable.put(RSA_oid, "RSA");
        nameTable.put(DH_oid, "Diffie-Hellman");
        nameTable.put(DH_PKIX_oid, "Diffie-Hellman");
        nameTable.put(DSA_oid, "DSA");
        nameTable.put(DSA_OIW_oid, "DSA");
        nameTable.put(EC_oid, "EC");
        nameTable.put(ECDH_oid, "ECDH");
        nameTable.put(AES_oid, "AES");
        nameTable.put(sha1WithECDSA_oid, "SHA1withECDSA");
        nameTable.put(sha224WithECDSA_oid, "SHA224withECDSA");
        nameTable.put(sha256WithECDSA_oid, "SHA256withECDSA");
        nameTable.put(sha384WithECDSA_oid, "SHA384withECDSA");
        nameTable.put(sha512WithECDSA_oid, "SHA512withECDSA");
        nameTable.put(md5WithRSAEncryption_oid, "MD5withRSA");
        nameTable.put(md2WithRSAEncryption_oid, "MD2withRSA");
        nameTable.put(sha1WithDSA_oid, "SHA1withDSA");
        nameTable.put(sha1WithDSA_OIW_oid, "SHA1withDSA");
        nameTable.put(shaWithDSA_OIW_oid, "SHA1withDSA");
        nameTable.put(sha224WithDSA_oid, "SHA224withDSA");
        nameTable.put(sha256WithDSA_oid, "SHA256withDSA");
        nameTable.put(sha1WithRSAEncryption_oid, "SHA1withRSA");
        nameTable.put(sha1WithRSAEncryption_OIW_oid, "SHA1withRSA");
        nameTable.put(sha224WithRSAEncryption_oid, "SHA224withRSA");
        nameTable.put(sha256WithRSAEncryption_oid, "SHA256withRSA");
        nameTable.put(sha384WithRSAEncryption_oid, "SHA384withRSA");
        nameTable.put(sha512WithRSAEncryption_oid, "SHA512withRSA");
        nameTable.put(pbeWithMD5AndDES_oid, "PBEWithMD5AndDES");
        nameTable.put(pbeWithMD5AndRC2_oid, "PBEWithMD5AndRC2");
        nameTable.put(pbeWithSHA1AndDES_oid, "PBEWithSHA1AndDES");
        nameTable.put(pbeWithSHA1AndRC2_oid, "PBEWithSHA1AndRC2");
        nameTable.put(pbeWithSHA1AndDESede_oid, "PBEWithSHA1AndDESede");
        nameTable.put(pbeWithSHA1AndRC2_40_oid, "PBEWithSHA1AndRC2_40");
    }

    public static String makeSigAlg(String digAlg, String encAlg) {
        String digAlg2 = digAlg.replace((CharSequence) LanguageTag.SEP, (CharSequence) "");
        if (encAlg.equalsIgnoreCase("EC")) {
            encAlg = "ECDSA";
        }
        return digAlg2 + "with" + encAlg;
    }

    public static String getEncAlgFromSigAlg(String signatureAlgorithm) {
        String keyAlgorithm;
        String signatureAlgorithm2 = signatureAlgorithm.toUpperCase(Locale.ENGLISH);
        int with = signatureAlgorithm2.indexOf("WITH");
        if (with <= 0) {
            return null;
        }
        int and = signatureAlgorithm2.indexOf("AND", with + 4);
        if (and > 0) {
            keyAlgorithm = signatureAlgorithm2.substring(with + 4, and);
        } else {
            keyAlgorithm = signatureAlgorithm2.substring(with + 4);
        }
        if (keyAlgorithm.equalsIgnoreCase("ECDSA")) {
            return "EC";
        }
        return keyAlgorithm;
    }

    public static String getDigAlgFromSigAlg(String signatureAlgorithm) {
        String signatureAlgorithm2 = signatureAlgorithm.toUpperCase(Locale.ENGLISH);
        int with = signatureAlgorithm2.indexOf("WITH");
        if (with > 0) {
            return signatureAlgorithm2.substring(0, with);
        }
        return null;
    }
}

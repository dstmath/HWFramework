package sun.security.provider.certpath;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.SerialNumber;

public class CertId {
    private static final AlgorithmId SHA1_ALGID = new AlgorithmId(AlgorithmId.SHA_oid);
    private static final boolean debug = false;
    private final SerialNumber certSerialNumber;
    private final AlgorithmId hashAlgId;
    private final byte[] issuerKeyHash;
    private final byte[] issuerNameHash;
    private int myhash;

    public CertId(X509Certificate issuerCert, SerialNumber serialNumber) throws IOException {
        this(issuerCert.getSubjectX500Principal(), issuerCert.getPublicKey(), serialNumber);
    }

    public CertId(X500Principal issuerName, PublicKey issuerKey, SerialNumber serialNumber) throws IOException {
        this.myhash = -1;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            this.hashAlgId = SHA1_ALGID;
            md.update(issuerName.getEncoded());
            this.issuerNameHash = md.digest();
            DerValue val = new DerValue(issuerKey.getEncoded());
            md.update(new DerValue[]{val.data.getDerValue(), val.data.getDerValue()}[1].getBitString());
            this.issuerKeyHash = md.digest();
            this.certSerialNumber = serialNumber;
        } catch (NoSuchAlgorithmException nsae) {
            throw new IOException("Unable to create CertId", nsae);
        }
    }

    public CertId(DerInputStream derIn) throws IOException {
        this.myhash = -1;
        this.hashAlgId = AlgorithmId.parse(derIn.getDerValue());
        this.issuerNameHash = derIn.getOctetString();
        this.issuerKeyHash = derIn.getOctetString();
        this.certSerialNumber = new SerialNumber(derIn);
    }

    public AlgorithmId getHashAlgorithm() {
        return this.hashAlgId;
    }

    public byte[] getIssuerNameHash() {
        return this.issuerNameHash;
    }

    public byte[] getIssuerKeyHash() {
        return this.issuerKeyHash;
    }

    public BigInteger getSerialNumber() {
        return this.certSerialNumber.getNumber();
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.hashAlgId.encode(tmp);
        tmp.putOctetString(this.issuerNameHash);
        tmp.putOctetString(this.issuerKeyHash);
        this.certSerialNumber.encode(tmp);
        out.write((byte) 48, tmp);
    }

    public int hashCode() {
        if (this.myhash == -1) {
            int i;
            this.myhash = this.hashAlgId.hashCode();
            for (i = 0; i < this.issuerNameHash.length; i++) {
                this.myhash += this.issuerNameHash[i] * i;
            }
            for (i = 0; i < this.issuerKeyHash.length; i++) {
                this.myhash += this.issuerKeyHash[i] * i;
            }
            this.myhash += this.certSerialNumber.getNumber().hashCode();
        }
        return this.myhash;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || ((other instanceof CertId) ^ 1) != 0) {
            return false;
        }
        CertId that = (CertId) other;
        return this.hashAlgId.equals(that.getHashAlgorithm()) && Arrays.equals(this.issuerNameHash, that.getIssuerNameHash()) && Arrays.equals(this.issuerKeyHash, that.getIssuerKeyHash()) && this.certSerialNumber.getNumber().equals(that.getSerialNumber());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CertId \n");
        sb.append("Algorithm: ").append(this.hashAlgId.toString()).append("\n");
        sb.append("issuerNameHash \n");
        HexDumpEncoder encoder = new HexDumpEncoder();
        sb.append(encoder.encode(this.issuerNameHash));
        sb.append("\nissuerKeyHash: \n");
        sb.append(encoder.encode(this.issuerKeyHash));
        sb.append("\n").append(this.certSerialNumber.toString());
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}

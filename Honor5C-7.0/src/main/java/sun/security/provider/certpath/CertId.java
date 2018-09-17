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
    private static final AlgorithmId SHA1_ALGID = null;
    private static final boolean debug = false;
    private final SerialNumber certSerialNumber;
    private final AlgorithmId hashAlgId;
    private final byte[] issuerKeyHash;
    private final byte[] issuerNameHash;
    private int myhash;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.CertId.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.CertId.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.CertId.<clinit>():void");
    }

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
        out.write((byte) DerValue.tag_SequenceOf, tmp);
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
        if (other == null || !(other instanceof CertId)) {
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
        return sb.toString();
    }
}

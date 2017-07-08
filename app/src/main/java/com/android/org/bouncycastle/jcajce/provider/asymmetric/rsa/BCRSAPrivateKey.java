package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import com.android.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Enumeration;

public class BCRSAPrivateKey implements RSAPrivateKey, PKCS12BagAttributeCarrier {
    private static BigInteger ZERO = null;
    static final long serialVersionUID = 5110188922551353628L;
    private transient PKCS12BagAttributeCarrierImpl attrCarrier;
    protected BigInteger modulus;
    protected BigInteger privateExponent;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey.<clinit>():void");
    }

    protected BCRSAPrivateKey() {
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
    }

    BCRSAPrivateKey(RSAKeyParameters key) {
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
        this.modulus = key.getModulus();
        this.privateExponent = key.getExponent();
    }

    BCRSAPrivateKey(RSAPrivateKeySpec spec) {
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
        this.modulus = spec.getModulus();
        this.privateExponent = spec.getPrivateExponent();
    }

    BCRSAPrivateKey(RSAPrivateKey key) {
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
        this.modulus = key.getModulus();
        this.privateExponent = key.getPrivateExponent();
    }

    BCRSAPrivateKey(com.android.org.bouncycastle.asn1.pkcs.RSAPrivateKey key) {
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
        this.modulus = key.getModulus();
        this.privateExponent = key.getPrivateExponent();
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPrivateExponent() {
        return this.privateExponent;
    }

    public String getAlgorithm() {
        return "RSA";
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        return KeyUtil.getEncodedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new com.android.org.bouncycastle.asn1.pkcs.RSAPrivateKey(getModulus(), ZERO, getPrivateExponent(), ZERO, ZERO, ZERO, ZERO, ZERO));
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof RSAPrivateKey)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        RSAPrivateKey key = (RSAPrivateKey) o;
        if (getModulus().equals(key.getModulus())) {
            z = getPrivateExponent().equals(key.getPrivateExponent());
        }
        return z;
    }

    public int hashCode() {
        return getModulus().hashCode() ^ getPrivateExponent().hashCode();
    }

    public void setBagAttribute(ASN1ObjectIdentifier oid, ASN1Encodable attribute) {
        this.attrCarrier.setBagAttribute(oid, attribute);
    }

    public ASN1Encodable getBagAttribute(ASN1ObjectIdentifier oid) {
        return this.attrCarrier.getBagAttribute(oid);
    }

    public Enumeration getBagAttributeKeys() {
        return this.attrCarrier.getBagAttributeKeys();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
}

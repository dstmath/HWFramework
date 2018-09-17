package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.util.Enumeration;

public class PBES2Parameters extends ASN1Object implements PKCSObjectIdentifiers {
    private KeyDerivationFunc func;
    private EncryptionScheme scheme;

    public static PBES2Parameters getInstance(Object obj) {
        if (obj instanceof PBES2Parameters) {
            return (PBES2Parameters) obj;
        }
        if (obj != null) {
            return new PBES2Parameters(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public PBES2Parameters(KeyDerivationFunc keyDevFunc, EncryptionScheme encScheme) {
        this.func = keyDevFunc;
        this.scheme = encScheme;
    }

    private PBES2Parameters(ASN1Sequence obj) {
        Enumeration e = obj.getObjects();
        ASN1Sequence funcSeq = ASN1Sequence.getInstance(((ASN1Encodable) e.nextElement()).toASN1Primitive());
        if (funcSeq.getObjectAt(0).equals(id_PBKDF2)) {
            this.func = new KeyDerivationFunc(id_PBKDF2, PBKDF2Params.getInstance(funcSeq.getObjectAt(1)));
        } else {
            this.func = KeyDerivationFunc.getInstance(funcSeq);
        }
        this.scheme = EncryptionScheme.getInstance(e.nextElement());
    }

    public KeyDerivationFunc getKeyDerivationFunc() {
        return this.func;
    }

    public EncryptionScheme getEncryptionScheme() {
        return this.scheme;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.func);
        v.add(this.scheme);
        return new DERSequence(v);
    }
}

package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;

public class BCPBEKey implements PBEKey {
    String algorithm;
    int digest;
    int ivSize;
    int keySize;
    ASN1ObjectIdentifier oid;
    CipherParameters param;
    PBEKeySpec pbeKeySpec;
    boolean tryWrong = false;
    int type;

    public BCPBEKey(String algorithm, ASN1ObjectIdentifier oid, int type, int digest, int keySize, int ivSize, PBEKeySpec pbeKeySpec, CipherParameters param) {
        this.algorithm = algorithm;
        this.oid = oid;
        this.type = type;
        this.digest = digest;
        this.keySize = keySize;
        this.ivSize = ivSize;
        this.pbeKeySpec = pbeKeySpec;
        this.param = param;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        if (this.param != null) {
            KeyParameter kParam;
            if (this.param instanceof ParametersWithIV) {
                kParam = (KeyParameter) ((ParametersWithIV) this.param).getParameters();
            } else {
                kParam = (KeyParameter) this.param;
            }
            return kParam.getKey();
        } else if (this.type == 2) {
            return PBEParametersGenerator.PKCS12PasswordToBytes(this.pbeKeySpec.getPassword());
        } else {
            if (this.type == 5) {
                return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(this.pbeKeySpec.getPassword());
            }
            return PBEParametersGenerator.PKCS5PasswordToBytes(this.pbeKeySpec.getPassword());
        }
    }

    int getType() {
        return this.type;
    }

    int getDigest() {
        return this.digest;
    }

    int getKeySize() {
        return this.keySize;
    }

    public int getIvSize() {
        return this.ivSize;
    }

    public CipherParameters getParam() {
        return this.param;
    }

    public char[] getPassword() {
        return this.pbeKeySpec.getPassword();
    }

    public byte[] getSalt() {
        return this.pbeKeySpec.getSalt();
    }

    public int getIterationCount() {
        return this.pbeKeySpec.getIterationCount();
    }

    public ASN1ObjectIdentifier getOID() {
        return this.oid;
    }

    public void setTryWrongPKCS12Zero(boolean tryWrong) {
        this.tryWrong = tryWrong;
    }

    boolean shouldTryWrongPKCS12() {
        return this.tryWrong;
    }

    public PBEKeySpec getPbeKeySpec() {
        return this.pbeKeySpec;
    }
}

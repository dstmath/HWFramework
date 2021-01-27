package org.bouncycastle.operator.jcajce;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.AsymmetricKeyUnwrapper;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OperatorException;

public class JceAsymmetricKeyUnwrapper extends AsymmetricKeyUnwrapper {
    private Map extraMappings = new HashMap();
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private PrivateKey privKey;
    private boolean unwrappedKeyMustBeEncodable;

    public JceAsymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, PrivateKey privateKey) {
        super(algorithmIdentifier);
        this.privKey = privateKey;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        if (r2.length != 0) goto L_0x0046;
     */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004a A[ExcHandler: IllegalStateException | UnsupportedOperationException | GeneralSecurityException | ProviderException (e java.lang.Throwable), Splitter:B:10:0x003c] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x004e  */
    @Override // org.bouncycastle.operator.KeyUnwrapper
    public GenericKey generateUnwrappedKey(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) throws OperatorException {
        try {
            Cipher createAsymmetricWrapper = this.helper.createAsymmetricWrapper(getAlgorithmIdentifier().getAlgorithm(), this.extraMappings);
            AlgorithmParameters createAlgorithmParameters = this.helper.createAlgorithmParameters(getAlgorithmIdentifier());
            Key key = null;
            if (createAlgorithmParameters != null) {
                try {
                    createAsymmetricWrapper.init(4, this.privKey, createAlgorithmParameters);
                } catch (IllegalStateException | UnsupportedOperationException | GeneralSecurityException | ProviderException e) {
                }
            } else {
                createAsymmetricWrapper.init(4, this.privKey);
            }
            Key unwrap = createAsymmetricWrapper.unwrap(bArr, this.helper.getKeyAlgorithmName(algorithmIdentifier.getAlgorithm()), 3);
            if (this.unwrappedKeyMustBeEncodable) {
                try {
                    byte[] encoded = unwrap.getEncoded();
                    if (encoded != null) {
                    }
                } catch (IllegalStateException | UnsupportedOperationException | GeneralSecurityException | ProviderException e2) {
                }
                if (key == null) {
                    createAsymmetricWrapper.init(2, this.privKey);
                    key = new SecretKeySpec(createAsymmetricWrapper.doFinal(bArr), algorithmIdentifier.getAlgorithm().getId());
                }
                return new JceGenericKey(algorithmIdentifier, key);
            }
            key = unwrap;
            if (key == null) {
            }
            return new JceGenericKey(algorithmIdentifier, key);
        } catch (InvalidKeyException e3) {
            throw new OperatorException("key invalid: " + e3.getMessage(), e3);
        } catch (IllegalBlockSizeException e4) {
            throw new OperatorException("illegal blocksize: " + e4.getMessage(), e4);
        } catch (BadPaddingException e5) {
            throw new OperatorException("bad padding: " + e5.getMessage(), e5);
        }
    }

    public JceAsymmetricKeyUnwrapper setAlgorithmMapping(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        this.extraMappings.put(aSN1ObjectIdentifier, str);
        return this;
    }

    public JceAsymmetricKeyUnwrapper setMustProduceEncodableUnwrappedKey(boolean z) {
        this.unwrappedKeyMustBeEncodable = z;
        return this;
    }

    public JceAsymmetricKeyUnwrapper setProvider(String str) {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JceAsymmetricKeyUnwrapper setProvider(Provider provider) {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));
        return this;
    }
}

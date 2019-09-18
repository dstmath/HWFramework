package org.bouncycastle.operator.jcajce;

import java.security.PrivateKey;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.bouncycastle.asn1.cms.GenericHybridParameters;
import org.bouncycastle.asn1.cms.RsaKemParameters;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.util.DEROtherInfo;
import org.bouncycastle.jcajce.spec.KTSParameterSpec;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.AsymmetricKeyUnwrapper;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.util.Arrays;

public class JceKTSKeyUnwrapper extends AsymmetricKeyUnwrapper {
    private Map extraMappings = new HashMap();
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private byte[] partyUInfo;
    private byte[] partyVInfo;
    private PrivateKey privKey;

    public JceKTSKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, PrivateKey privateKey, byte[] bArr, byte[] bArr2) {
        super(algorithmIdentifier);
        this.privKey = privateKey;
        this.partyUInfo = Arrays.clone(bArr);
        this.partyVInfo = Arrays.clone(bArr2);
    }

    public GenericKey generateUnwrappedKey(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) throws OperatorException {
        GenericHybridParameters instance = GenericHybridParameters.getInstance(getAlgorithmIdentifier().getParameters());
        Cipher createAsymmetricWrapper = this.helper.createAsymmetricWrapper(getAlgorithmIdentifier().getAlgorithm(), this.extraMappings);
        String wrappingAlgorithmName = this.helper.getWrappingAlgorithmName(instance.getDem().getAlgorithm());
        RsaKemParameters instance2 = RsaKemParameters.getInstance(instance.getKem().getParameters());
        try {
            createAsymmetricWrapper.init(4, this.privKey, new KTSParameterSpec.Builder(wrappingAlgorithmName, instance2.getKeyLength().intValue() * 8, new DEROtherInfo.Builder(instance.getDem(), this.partyUInfo, this.partyVInfo).build().getEncoded()).withKdfAlgorithm(instance2.getKeyDerivationFunction()).build());
            return new JceGenericKey(algorithmIdentifier, createAsymmetricWrapper.unwrap(bArr, this.helper.getKeyAlgorithmName(algorithmIdentifier.getAlgorithm()), 3));
        } catch (Exception e) {
            throw new OperatorException("Unable to unwrap contents key: " + e.getMessage(), e);
        }
    }

    public JceKTSKeyUnwrapper setProvider(String str) {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JceKTSKeyUnwrapper setProvider(Provider provider) {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));
        return this;
    }
}

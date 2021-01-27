package org.bouncycastle.jcajce.provider.config;

import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;

public interface ConfigurableProvider {
    public static final String ACCEPTABLE_EC_CURVES = "acceptableEcCurves";
    public static final String ADDITIONAL_EC_PARAMETERS = "additionalEcParameters";
    public static final String DH_DEFAULT_PARAMS = "DhDefaultParams";
    public static final String EC_IMPLICITLY_CA = "ecImplicitlyCa";
    public static final String THREAD_LOCAL_DH_DEFAULT_PARAMS = "threadLocalDhDefaultParams";
    public static final String THREAD_LOCAL_EC_IMPLICITLY_CA = "threadLocalEcImplicitlyCa";

    void addAlgorithm(String str, String str2);

    void addAlgorithm(String str, ASN1ObjectIdentifier aSN1ObjectIdentifier, String str2);

    void addAttributes(String str, Map<String, String> map);

    void addKeyInfoConverter(ASN1ObjectIdentifier aSN1ObjectIdentifier, AsymmetricKeyInfoConverter asymmetricKeyInfoConverter);

    boolean hasAlgorithm(String str, String str2);

    void setParameter(String str, Object obj);
}

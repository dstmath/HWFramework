package org.bouncycastle.asn1.cryptopro;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CryptoProObjectIdentifiers {
    public static final ASN1ObjectIdentifier GOST_id = new ASN1ObjectIdentifier("1.2.643.2.2");
    public static final ASN1ObjectIdentifier gostR28147_gcfb = GOST_id.branch("21");
    public static final ASN1ObjectIdentifier gostR3410_2001 = GOST_id.branch("19");
    public static final ASN1ObjectIdentifier gostR3410_2001DH = GOST_id.branch("98");
    public static final ASN1ObjectIdentifier gostR3410_2001_CryptoPro_A = GOST_id.branch("35.1");
    public static final ASN1ObjectIdentifier gostR3410_2001_CryptoPro_B = GOST_id.branch("35.2");
    public static final ASN1ObjectIdentifier gostR3410_2001_CryptoPro_C = GOST_id.branch("35.3");
    public static final ASN1ObjectIdentifier gostR3410_2001_CryptoPro_ESDH = GOST_id.branch("96");
    public static final ASN1ObjectIdentifier gostR3410_2001_CryptoPro_XchA = GOST_id.branch("36.0");
    public static final ASN1ObjectIdentifier gostR3410_2001_CryptoPro_XchB = GOST_id.branch("36.1");
    public static final ASN1ObjectIdentifier gostR3410_94 = GOST_id.branch("20");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_A = GOST_id.branch("32.2");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_B = GOST_id.branch("32.3");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_C = GOST_id.branch("32.4");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_D = GOST_id.branch("32.5");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_XchA = GOST_id.branch("33.1");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_XchB = GOST_id.branch("33.2");
    public static final ASN1ObjectIdentifier gostR3410_94_CryptoPro_XchC = GOST_id.branch("33.3");
    public static final ASN1ObjectIdentifier gostR3411 = GOST_id.branch("9");
    public static final ASN1ObjectIdentifier gostR3411Hmac = GOST_id.branch("10");
    public static final ASN1ObjectIdentifier gostR3411_94_CryptoProParamSet = GOST_id.branch("30.1");
    public static final ASN1ObjectIdentifier gostR3411_94_with_gostR3410_2001 = GOST_id.branch("3");
    public static final ASN1ObjectIdentifier gostR3411_94_with_gostR3410_94 = GOST_id.branch("4");
    public static final ASN1ObjectIdentifier gost_ElSgDH3410_1 = GOST_id.branch("36.1");
    public static final ASN1ObjectIdentifier gost_ElSgDH3410_default = GOST_id.branch("36.0");
    public static final ASN1ObjectIdentifier id_Gost28147_89_CryptoPro_A_ParamSet = GOST_id.branch("31.1");
    public static final ASN1ObjectIdentifier id_Gost28147_89_CryptoPro_B_ParamSet = GOST_id.branch("31.2");
    public static final ASN1ObjectIdentifier id_Gost28147_89_CryptoPro_C_ParamSet = GOST_id.branch("31.3");
    public static final ASN1ObjectIdentifier id_Gost28147_89_CryptoPro_D_ParamSet = GOST_id.branch("31.4");
    public static final ASN1ObjectIdentifier id_Gost28147_89_CryptoPro_KeyWrap = GOST_id.branch("13.1");
    public static final ASN1ObjectIdentifier id_Gost28147_89_CryptoPro_TestParamSet = GOST_id.branch("31.0");
    public static final ASN1ObjectIdentifier id_Gost28147_89_None_KeyWrap = GOST_id.branch("13.0");
}

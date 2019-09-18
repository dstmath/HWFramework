package com.huawei.nearbysdk;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class RSAUtils {
    public static byte[] encryptUsingPubKey(byte[] aesKey) {
        try {
            return encryptByPublicKey(aesKey, getPublicKey("F28F4F0737F0AC1EF95B14923ED01BBB7D908C3D2791FD973C9EBEF34C08C326C989254CFCB9FBDBA379EFCE10C1225DFDEDA4693D9B6C0B11BF19A8A5A37246B830B158DFAD87303585DD2091C289A29C67AA51EB197A472B56A9ECB6902692518FE8E25831FCD93035CAB4E1CE7053C127CA980C49303F42252A331E52E6405964BD40AB8A82FD2AE045271311D2F5967A69A59A0BBB53CA1C930D4148AC04FFB4442B4E2E7FE5094C45C65983871A45439BFC36A5E98682959BB4F4B4ACDB53441DF6B87DC9A4906FA87C999EF8B757DAD28707AD1C5612D5970DAFE0F04B85718830A1BD9130CE541333E9D0195E0D51EDF8B8CE1CFF291B98F4868B5A95", "010001"));
        } catch (Exception e) {
            HwLog.e("RSAUtils", "encryptByPublicKey fail: " + e);
            return null;
        }
    }

    public static RSAPublicKey getPublicKey(String modulus, String exponent) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exponent, 16)));
        } catch (Exception e) {
            HwLog.e("RSAUtils", "getPublicKey fail: " + e);
            return null;
        }
    }

    public static byte[] encryptByPublicKey(byte[] data, Key publicKey) throws Exception {
        AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP");
        algp.init(new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
        Cipher oaepFromAlgo = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING", "BC");
        oaepFromAlgo.init(1, publicKey, algp);
        return oaepFromAlgo.doFinal(data);
    }
}

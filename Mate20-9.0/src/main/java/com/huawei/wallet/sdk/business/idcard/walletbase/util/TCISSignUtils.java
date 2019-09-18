package com.huawei.wallet.sdk.business.idcard.walletbase.util;

import com.huawei.wallet.sdk.common.log.LogC;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

public class TCISSignUtils {
    public static final String CIPHER_AES_ALGORITHM = "AES/CBC/NoPadding";

    public static byte[] encryptUsingPubKey(byte[] aesKey) {
        RSAPublicKey publicKey = getPublicKey("F28F4F0737F0AC1EF95B14923ED01BBB7D908C3D2791FD973C9EBEF34C08C326C989254CFCB9FBDBA379EFCE10C1225DFDEDA4693D9B6C0B11BF19A8A5A37246B830B158DFAD87303585DD2091C289A29C67AA51EB197A472B56A9ECB6902692518FE8E25831FCD93035CAB4E1CE7053C127CA980C49303F42252A331E52E6405964BD40AB8A82FD2AE045271311D2F5967A69A59A0BBB53CA1C930D4148AC04FFB4442B4E2E7FE5094C45C65983871A45439BFC36A5E98682959BB4F4B4ACDB53441DF6B87DC9A4906FA87C999EF8B757DAD28707AD1C5612D5970DAFE0F04B85718830A1BD9130CE541333E9D0195E0D51EDF8B8CE1CFF291B98F4868B5A95", "010001");
        if (publicKey == null) {
            return new byte[0];
        }
        try {
            return encryptByPublicKey1(aesKey, publicKey);
        } catch (Exception e) {
            LogX.e("encryptUsingPubKey Exception", e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] encryptByPublicKey1(byte[] data, Key publicKey) throws Exception {
        AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP", "BC");
        algp.init(new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
        Cipher oaepFromAlgo = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
        oaepFromAlgo.init(1, publicKey, algp);
        return oaepFromAlgo.doFinal(data);
    }

    public static RSAPublicKey getPublicKey(String modulus, String exponent) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exponent, 16)));
        } catch (NoSuchAlgorithmException e) {
            LogC.e("NoSuchAlgorithmException", false);
            return null;
        } catch (InvalidKeySpecException e2) {
            LogC.e("InvalidKeySpecException", false);
            return null;
        } catch (Exception e3) {
            LogX.e("RSAPublicKey getPublicKey Exception", e3.getMessage());
            return null;
        }
    }

    public static byte[] decrypt(byte[] crypt, byte[] key, byte[] iv) throws Exception {
        if (key == null || key.length != 16 || iv == null || iv.length != 16) {
            throw new Exception("key or iv error");
        }
        SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(CIPHER_AES_ALGORITHM);
        cipher.init(2, sKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(crypt);
    }
}

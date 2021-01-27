package com.huawei.nearbysdk;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class RSAUtils {
    public static byte[] encryptUsingPubKey(byte[] aesKey) {
        if (aesKey == null) {
            HwLog.e("RSAUtils", "encryptByPublicKey fail: aesKey is null");
            return new byte[0];
        }
        RSAPublicKey publicKey = getPublicKey("F28F4F0737F0AC1EF95B14923ED01BBB7D908C3D2791FD973C9EBEF34C08C326C989254CFCB9FBDBA379EFCE10C1225DFDEDA4693D9B6C0B11BF19A8A5A37246B830B158DFAD87303585DD2091C289A29C67AA51EB197A472B56A9ECB6902692518FE8E25831FCD93035CAB4E1CE7053C127CA980C49303F42252A331E52E6405964BD40AB8A82FD2AE045271311D2F5967A69A59A0BBB53CA1C930D4148AC04FFB4442B4E2E7FE5094C45C65983871A45439BFC36A5E98682959BB4F4B4ACDB53441DF6B87DC9A4906FA87C999EF8B757DAD28707AD1C5612D5970DAFE0F04B85718830A1BD9130CE541333E9D0195E0D51EDF8B8CE1CFF291B98F4868B5A95", "010001").orElse(null);
        if (publicKey == null) {
            HwLog.e("RSAUtils", "encryptByPublicKey fail: publicKey is null");
            return new byte[0];
        }
        byte[] encryptByte = encryptByPublicKey(aesKey, publicKey);
        if (encryptByte != null && encryptByte.length != 0) {
            return encryptByte;
        }
        HwLog.e("RSAUtils", "encryptByPublicKey fail: encryptByte is null");
        return new byte[0];
    }

    public static Optional<RSAPublicKey> getPublicKey(String modulus, String exponent) {
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exponent, 16)));
            if (publicKey != null && (publicKey instanceof RSAPublicKey)) {
                return Optional.of((RSAPublicKey) publicKey);
            }
            HwLog.e("RSAUtils", "getPublicKey fail: publicKey is null or not rsapublickey");
            return Optional.empty();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            HwLog.e("RSAUtils", "getPublicKey fail: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static byte[] encryptByPublicKey(byte[] data, Key publicKey) {
        try {
            AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP");
            algp.init(new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
            Cipher oaepFromAlgo = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING", "BC");
            oaepFromAlgo.init(1, publicKey, algp);
            byte[] ct = oaepFromAlgo.doFinal(data);
            if (ct != null) {
                return ct;
            }
            HwLog.e("RSAUtils", "encryptByPublicKey fail: encrypt failed");
            return new byte[0];
        } catch (NullPointerException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            HwLog.e("RSAUtils", "encryptByPublicKey fail: " + e.getMessage());
            return new byte[0];
        }
    }
}

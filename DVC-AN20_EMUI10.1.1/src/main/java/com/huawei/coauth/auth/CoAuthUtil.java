package com.huawei.coauth.auth;

import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import java.security.SecureRandom;
import java.util.Optional;

public class CoAuthUtil {
    private static final int BYTE_NUM = 2;
    private static final int HEX_BASE = 255;
    public static final String HEX_CHAR = "0123456789ABCDEF";
    private static final int HEX_NUM = 16;
    private static final int HEX_RIGHT = 4;
    private static final int HEX_VALUE = 15;
    public static final int SDK_VERSION = 1;
    public static final String SELF_DID = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
    public static final String TAG = CoAuth.class.getName();

    private CoAuthUtil() {
    }

    public static String getSelfDeviceUdid() {
        return SELF_DID;
    }

    public static long getNewSessionId() {
        return new SecureRandom().nextLong();
    }

    public static CoAuthHeaderEntity getCoAuthHeader(String selfDid, int srcModule, CoAuthDevice peerCoAuthDevice, int dstModule) {
        return new CoAuthHeaderEntity.Builder().setVersion(1).setSrcDid(selfDid).setSrcModule(srcModule).setDstDid(peerCoAuthDevice.getDeviceId()).setDstDeviceIp(peerCoAuthDevice.getIp()).setDstDevicePort(peerCoAuthDevice.getPort()).setDstModule(dstModule).build();
    }

    public static Optional<String> bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return Optional.empty();
        }
        char[] hexChar = HEX_CHAR.toCharArray();
        char[] hexArray = new char[(bytes.length * 2)];
        for (int index = 0; index < bytes.length; index++) {
            int value = bytes[index] & 255;
            hexArray[index * 2] = hexChar[value >>> 4];
            hexArray[(index * 2) + 1] = hexChar[value & 15];
        }
        return Optional.ofNullable(new String(hexArray));
    }

    public static Optional<byte[]> hexStringToBytes(String hexString) {
        if (hexString == null) {
            return Optional.empty();
        }
        if (hexString.length() == 0) {
            return Optional.ofNullable(new byte[0]);
        }
        byte[] bytes = new byte[(hexString.length() / 2)];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(hexString.substring(index * 2, (index * 2) + 2), 16);
        }
        return Optional.ofNullable(bytes);
    }
}

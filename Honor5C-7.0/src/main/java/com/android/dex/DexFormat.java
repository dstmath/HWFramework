package com.android.dex;

import org.xmlpull.v1.XmlPullParser;

public final class DexFormat {
    public static final int API_CURRENT = 24;
    public static final int API_NO_EXTENDED_OPCODES = 13;
    public static final String DEX_IN_JAR_NAME = "classes.dex";
    public static final int ENDIAN_TAG = 305419896;
    public static final String MAGIC_PREFIX = "dex\n";
    public static final String MAGIC_SUFFIX = "\u0000";
    public static final int MAX_MEMBER_IDX = 65535;
    public static final int MAX_TYPE_IDX = 65535;
    public static final String VERSION_CURRENT = "037";
    public static final String VERSION_FOR_API_13 = "035";

    private DexFormat() {
    }

    public static int magicToApi(byte[] magic) {
        if (magic.length != 8 || magic[0] != 100 || magic[1] != 101 || magic[2] != 120 || magic[3] != 10 || magic[7] != null) {
            return -1;
        }
        String version = XmlPullParser.NO_NAMESPACE + ((char) magic[4]) + ((char) magic[5]) + ((char) magic[6]);
        if (version.equals(VERSION_CURRENT)) {
            return API_CURRENT;
        }
        if (version.equals(VERSION_FOR_API_13)) {
            return API_NO_EXTENDED_OPCODES;
        }
        return -1;
    }

    public static String apiToMagic(int targetApiLevel) {
        String version;
        if (targetApiLevel >= API_CURRENT) {
            version = VERSION_CURRENT;
        } else {
            version = VERSION_FOR_API_13;
        }
        return MAGIC_PREFIX + version + MAGIC_SUFFIX;
    }

    public static boolean isSupportedDexMagic(byte[] magic) {
        int api = magicToApi(magic);
        if (api == API_NO_EXTENDED_OPCODES || api == API_CURRENT) {
            return true;
        }
        return false;
    }
}

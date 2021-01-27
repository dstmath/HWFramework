package com.huawei.internal.telephony.dataconnection;

import android.database.Cursor;
import android.telephony.data.ApnSetting;
import java.util.stream.Stream;

public class ApnSettingHelper {
    private static final int INVALID_MTU = -1;
    public static final int TYPE_BIP0 = 32768;
    public static final int TYPE_BIP1 = 65536;
    public static final int TYPE_BIP2 = 131072;
    public static final int TYPE_BIP3 = 262144;
    public static final int TYPE_BIP4 = 524288;
    public static final int TYPE_BIP5 = 1048576;
    public static final int TYPE_BIP6 = 2097152;
    public static final int TYPE_CBS = 128;
    public static final int TYPE_DEFAULT = 17;
    public static final int TYPE_DUN = 8;
    public static final int TYPE_EMERGENCY = 512;
    public static final int TYPE_FOTA = 32;
    public static final int TYPE_IA = 256;
    public static final int TYPE_IMS = 64;
    public static final int TYPE_INTERNALDEFAULT = 8388608;
    public static final int TYPE_MCX = 1024;
    public static final int TYPE_MMS = 2;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_SNSSAI = 33554432;
    public static final int TYPE_SUPL = 4;
    public static final int TYPE_WIFI_MMS = 16777216;
    public static final int TYPE_XCAP = 4194304;

    private ApnSettingHelper() {
    }

    public static ApnSetting fromString(String apnString) {
        return ApnSetting.fromString(apnString);
    }

    public static boolean isPreset(ApnSetting apnSetting) {
        if (apnSetting == null) {
            return false;
        }
        return apnSetting.isPreset();
    }

    public static void setIsPreset(ApnSetting apn, boolean isPreset) {
        if (apn != null) {
            apn.setIsPreset(isPreset);
        }
    }

    public static boolean canHandleType(ApnSetting apn, int type) {
        if (apn == null) {
            return false;
        }
        return apn.canHandleType(type);
    }

    public static boolean canSupportNetworkType(ApnSetting apn, int type) {
        if (apn == null) {
            return false;
        }
        return apn.canSupportNetworkType(type);
    }

    public static ApnSetting makeApnSetting(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        return ApnSetting.makeApnSetting(cursor);
    }

    public static ApnSetting makeApnSettingWithAuth(ApnSetting apn, String username, String password, int authType) {
        if (apn == null) {
            return null;
        }
        return ApnSetting.makeApnSetting(apn.getId(), apn.getOperatorNumeric(), apn.getEntryName(), apn.getApnName(), apn.getProxyAddressAsString(), apn.getProxyPort(), apn.getMmsc(), apn.getMmsProxyAddressAsString(), apn.getMmsProxyPort(), username, password, authType, apn.getApnTypeBitmask(), apn.getProtocol(), apn.getRoamingProtocol(), apn.isEnabled(), apn.getNetworkTypeBitmask(), apn.getProfileId(), apn.isPersistent(), apn.getMaxConns(), apn.getWaitTime(), apn.getMaxConnsTime(), apn.getMtu(), apn.getMvnoType(), apn.getMvnoMatchData(), apn.getApnSetId(), apn.getCarrierId(), apn.getSkip464Xlat());
    }

    public static int getApnTypesBitmaskFromString(String types) {
        return ApnSetting.getApnTypesBitmaskFromString(types);
    }

    public static int getNetworkTypeBitmask(ApnSetting apnSetting) {
        if (apnSetting == null) {
            return 0;
        }
        return apnSetting.getNetworkTypeBitmask();
    }

    public static int getProtocolIntFromString(String protocol) {
        return ApnSetting.getProtocolIntFromString(protocol);
    }

    public static void setProtocol(ApnSetting apnSetting, int newProtocol, boolean isRoaming) {
        if (apnSetting != null) {
            apnSetting.setProtocol(newProtocol, isRoaming);
        }
    }

    public static ApnSetting makeApnSettingForSlice(ApnSetting apn) {
        if (apn == null) {
            return null;
        }
        return ApnSetting.makeApnSetting(apn.getId(), apn.getOperatorNumeric(), apn.getEntryName(), "snssai", apn.getProxyAddressAsString(), apn.getProxyPort(), apn.getMmsc(), apn.getMmsProxyAddressAsString(), apn.getMmsProxyPort(), apn.getUser(), apn.getPassword(), apn.getAuthType(), TYPE_SNSSAI, apn.getProtocol(), apn.getRoamingProtocol(), apn.isEnabled(), apn.getNetworkTypeBitmask(), apn.getProfileId(), apn.isPersistent(), apn.getMaxConns(), apn.getWaitTime(), apn.getMaxConnsTime(), apn.getMtu(), apn.getMvnoType(), apn.getMvnoMatchData(), apn.getApnSetId(), apn.getCarrierId(), apn.getSkip464Xlat());
    }

    public static boolean isSimilar(ApnSetting first, ApnSetting second) {
        if (Stream.of((Object[]) new ApnSetting[]{first, second}).anyMatch($$Lambda$xMijv4caoW8cwtYlczS2C9I4S08.INSTANCE)) {
            return first == second;
        }
        return first.similar(second);
    }

    public static String getProtocolStringFromInt(int protocol) {
        return ApnSetting.getProtocolStringFromInt(protocol);
    }

    public static int getMtu(ApnSetting apnSetting) {
        if (apnSetting == null) {
            return -1;
        }
        return apnSetting.getMtu();
    }
}

package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.EMUIBuildUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class PhoneFeatureAdaptUtil {
    public static final String ACTIVE_CONFIG_KEY = "ro.config.nfc_nxp_active";
    public static final int ACTIVE_MODE_BY_OMA = 1;
    public static final int ACTIVE_MODE_BY_TA = 2;
    public static final int CARD_GROUP_TYPE_ACCESS = 4;
    public static final int CARD_GROUP_TYPE_BANK = 1;
    public static final int CARD_GROUP_TYPE_BUS = 2;
    public static final int CARD_GROUP_TYPE_CTID = 3;
    private static final String DEFAULT_MANUFACTURER = "HUAWEI";
    public static final String ESE_CONFIG_KEY = "ro.config.se_esetype";
    private static final int FRD_KNT_OPEN_CARD_NUMBER_LIMIT = 5;
    public static final String HISI_ESE = "2";
    public static final String I2C_TYPE = "i";
    private static final String[] MODLE_WHITE = {"HMA-L09", "LYA-L09"};
    public static final String MULTI_ESE = "3";
    public static final String NXP_ESE = "1";
    public static final int OPEN_CARD_NUMBER_LIMIT = 8;
    public static final String SPI_TYPE = "s";

    public static boolean isDeviceNeedPowerOn() {
        String deviceType = PhoneDeviceUtil.getDeviceType();
        String SEManufacturer = ProductConfigUtil.geteSEManufacturer();
        boolean z = true;
        if (StringUtil.isEmpty(deviceType, true) || StringUtil.isEmpty(SEManufacturer, true)) {
            LogC.w("PhoneFeatureAdaptUtil isDeviceNeedPowerOn, invalid deviceType or SEManufacturer", false);
            return true;
        }
        if ((EMUIBuildUtil.VERSION.EMUI_SDK_INT >= 14 && "02".equals(SEManufacturer)) || deviceType.contains("KNT") || deviceType.contains("FRD")) {
            z = false;
        }
        return z;
    }

    public static int getLimitCardNum() {
        String deviceType = PhoneDeviceUtil.getDeviceType();
        if (deviceType.contains("KNT") || deviceType.contains("FRD")) {
            int build_b_version = getProductBuildNumber(Build.DISPLAY);
            if (build_b_version >= 316 && build_b_version < 335) {
                return 5;
            }
        }
        return 8;
    }

    public static int getProductBuildNumber(String versionInfo) {
        String buildNumStr = null;
        if (!StringUtil.isEmpty(versionInfo, true)) {
            int index = versionInfo.lastIndexOf("B");
            if (index >= 0) {
                if (versionInfo.length() < index + 4) {
                    return 0;
                }
                buildNumStr = versionInfo.substring(index + 1, index + 4);
            }
        }
        if (StringUtil.isEmpty(buildNumStr, true)) {
            return 0;
        }
        try {
            return Integer.parseInt(buildNumStr);
        } catch (NumberFormatException e) {
            LogC.e("getProductBuildNumber:NumberFormatException.", false);
            return 0;
        }
    }

    public static int getProductBuildSPNumber(String versionInfo) {
        String buildSPNumStr = null;
        if (!StringUtil.isEmpty(versionInfo, true)) {
            int index = versionInfo.indexOf("SP");
            if (index < 0) {
                return 0;
            }
            int index2 = index + "SP".length();
            if (versionInfo.length() < index2 + 2) {
                return 0;
            }
            buildSPNumStr = versionInfo.substring(index2, index2 + 2);
        }
        if (StringUtil.isEmpty(buildSPNumStr, true)) {
            return 0;
        }
        try {
            return Integer.parseInt(buildSPNumStr);
        } catch (NumberFormatException e) {
            LogC.e("getProductBuildSPNumber:, NumberFormatException.", false);
            return 0;
        }
    }

    public static int[] getGeekVersionNum(String version) {
        int i = 0;
        int[] error = new int[0];
        if (StringUtil.isEmpty(version, true)) {
            return error;
        }
        String[] infos = version.split("_");
        if (infos.length < 2) {
            return error;
        }
        if (infos.length == 3) {
            infos[1] = infos[2];
        }
        int[] versionNum = {0, 0};
        if (StringUtil.isEmpty(infos[0], true)) {
            return error;
        }
        String emuiVer = infos[0].replaceAll("EMUI|\\.", "");
        if (StringUtil.isEmpty(emuiVer, true)) {
            return error;
        }
        try {
            versionNum[0] = Integer.parseInt(emuiVer);
            if (StringUtil.isEmpty(infos[1], true)) {
                return error;
            }
            String romVer = infos[1].replaceAll("\\.", "");
            try {
                versionNum[1] = Integer.parseInt(romVer);
            } catch (NumberFormatException e) {
                LogC.e("getGeekVersionNum rom ver parseInt NumberFormatException. romVer : " + romVer, false);
                StringBuffer sb = new StringBuffer();
                while (i < romVer.length() && Character.isDigit(romVer.charAt(i))) {
                    sb.append(romVer.charAt(i));
                    i++;
                }
                if (!StringUtil.isNumeric(sb.toString())) {
                    return error;
                }
                versionNum[1] = Integer.parseInt(sb.toString());
            }
            return versionNum;
        } catch (NumberFormatException e2) {
            LogC.e("getGeekVersionNum emui ver parseFloat NumberFormatException. emuiVer : " + emuiVer, false);
            return error;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0106, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x010a, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        com.huawei.wallet.sdk.common.log.LogC.i("this dot split version has no sp version", false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0110, code lost:
        r1 = "00";
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0165, code lost:
        r3 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0168, code lost:
        r3 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x016d, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0170, code lost:
        com.huawei.wallet.sdk.common.log.LogC.e("split rom ArrayIndexOutOfBoundsException erro", false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0178, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0179, code lost:
        com.huawei.wallet.sdk.common.log.LogC.e("split rom PatternSyntaxException erro", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0180, code lost:
        r3 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0102 A[ExcHandler: PatternSyntaxException (e java.util.regex.PatternSyntaxException), PHI: r3 
      PHI: (r3v12 boolean) = (r3v10 boolean), (r3v10 boolean), (r3v13 boolean) binds: [B:36:0x010d, B:37:?, B:17:0x00b1] A[DONT_GENERATE, DONT_INLINE], Splitter:B:17:0x00b1] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0109 A[ExcHandler: StringIndexOutOfBoundsException (e java.lang.StringIndexOutOfBoundsException), Splitter:B:14:0x00ad] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x016f A[ExcHandler: ArrayIndexOutOfBoundsException (e java.lang.ArrayIndexOutOfBoundsException), Splitter:B:8:0x0025] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0177 A[ExcHandler: PatternSyntaxException (e java.util.regex.PatternSyntaxException), Splitter:B:8:0x0025] */
    public static long[] getDotSplitVersion(String version) {
        boolean z;
        boolean z2;
        String vsp;
        String str = version;
        long[] result = new long[2];
        if (StringUtil.isEmpty(str, true)) {
            return result;
        }
        if (str.contains("GK")) {
            result[0] = 1;
        } else {
            result[0] = 2;
        }
        result[1] = 0;
        try {
            String[] b = str.split(" ")[1].split("\\(");
            String[] c = b[0].split("\\.");
            if (4 != c.length) {
                return result;
            }
            String vm = c[0];
            String vsTmp = "0" + c[1];
            String vs = vsTmp.substring(vsTmp.length() - 2, vsTmp.length());
            String vfTmp = "00" + c[2];
            String vf = vfTmp.substring(vfTmp.length() - 3, vfTmp.length());
            String vbTmp = "00" + c[3];
            String vb = vbTmp.substring(vbTmp.length() - 3, vbTmp.length());
            z = false;
            if (!"SP".equalsIgnoreCase(b[1].substring(0, 2))) {
                vsp = "00";
            } else if (StringUtil.isNumeric(b[1].substring(2, 4))) {
                vsp = b[1].substring(2, 4);
            } else if (StringUtil.isNumeric(b[1].substring(2, 3))) {
                vsp = "0" + b[1].substring(2, 3);
            } else {
                vsp = "00";
            }
            String vsp2 = vsp;
            long versionLong = 0;
            try {
                versionLong = Long.parseLong(vm + vs + vf + vb + vsp2);
            } catch (NumberFormatException e) {
                NumberFormatException numberFormatException = e;
                z2 = false;
                LogC.i("formart versionLong erro", false);
            }
            result[1] = versionLong;
            z2 = false;
            LogC.e("versionString: " + versionString + ", versionLong: " + versionLong, false);
            return result;
        } catch (StringIndexOutOfBoundsException e2) {
        } catch (PatternSyntaxException e3) {
        } catch (ArrayIndexOutOfBoundsException e4) {
        }
        LogC.e("split rom StringIndexOutOfBoundsException erro", z);
        return result;
    }

    public static String getMANUFACTURER() {
        return DEFAULT_MANUFACTURER;
    }

    public static boolean isUseI2C() {
        String deviceType;
        String sesType = SettingUtil.getSystemProperties(ESE_CONFIG_KEY);
        if ("2".equalsIgnoreCase(sesType)) {
            LogC.i("isUseI2C return false,because type is 2", false);
            return false;
        } else if ("3".equalsIgnoreCase(sesType)) {
            LogC.i("isUseI2C return true,because type is 3", false);
            return true;
        } else {
            if ("1".equalsIgnoreCase(sesType)) {
                String activeConfig = SettingUtil.getSystemProperties(ACTIVE_CONFIG_KEY);
                if (I2C_TYPE.equalsIgnoreCase(activeConfig)) {
                    LogC.i("isUseI2C return true,because config is i", false);
                    return true;
                } else if (SPI_TYPE.equalsIgnoreCase(activeConfig)) {
                    LogC.i("isUseI2C return false,because config is s", false);
                    return false;
                } else {
                    for (String equals : MODLE_WHITE) {
                        LogC.d("isUseI2C deviceType = " + deviceType, false);
                        if (TextUtils.equals(equals, deviceType)) {
                            LogC.i("isUseI2C return true,because device is in white", false);
                            return true;
                        }
                    }
                }
            }
            LogC.i("isUseI2C return false", false);
            return false;
        }
    }

    public static boolean isMultiEseDevice() {
        String value = SettingUtil.getSystemProperties(ESE_CONFIG_KEY);
        LogC.i("isMultiEseDevice VALUE IS :" + value, false);
        return "3".equalsIgnoreCase(value);
    }

    public static ArrayList<Integer> getEseDeviceList() {
        ArrayList<Integer> temp = new ArrayList<>();
        if ("3".equalsIgnoreCase(SettingUtil.getSystemProperties(ESE_CONFIG_KEY))) {
            temp.add(0);
            temp.add(3);
        } else {
            temp.add(0);
        }
        return temp;
    }

    public static int[] getCardActiveModeAndSeReaderType(Context context, int cardGroup) {
        String defaultReader;
        String defaultType;
        int i = 2;
        int[] result = new int[2];
        int i2 = 0;
        if (isUseI2C()) {
            if (cardGroup == 3) {
                defaultType = "TA";
                defaultReader = "eSE2";
            } else {
                defaultType = "OMA";
                defaultReader = "eSE";
            }
            if (!defaultType.equalsIgnoreCase("TA")) {
                i = 1;
            }
            result[0] = i;
            if (!defaultReader.equalsIgnoreCase("eSE")) {
                i2 = 3;
            }
            result[1] = i2;
        } else {
            result[0] = 2;
            result[1] = 0;
        }
        return result;
    }
}

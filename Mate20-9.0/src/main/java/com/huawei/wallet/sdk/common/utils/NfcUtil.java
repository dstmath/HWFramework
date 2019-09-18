package com.huawei.wallet.sdk.common.utils;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.cardemulation.CardEmulation;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.EMUIBuildUtil;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.Date;

public final class NfcUtil {
    public static final int NFC_SHOW_CARRERA = 2;
    public static final int NFC_SHOW_NOT_SUPPORT = 0;
    public static final int SELECT_DEFAULT_CARD_EMULATION_SE = 1;
    public static final int SELECT_DEFAULT_CARD_EMULATION_UICC = -1;
    public static final int SELECT_DEFAULT_CARD_EMULATION_UNKNOWN = 0;
    public static final int SELECT_SE_TYPE_INSE = 12;
    public static final int SELECT_SE_TYPE_NORMAL = 2;
    public static final int SELECT_SE_TYPE_NXP = 11;

    public static boolean isEnabledNFC(Context context) {
        if (context == null) {
            return false;
        }
        NfcManager manager = (NfcManager) context.getSystemService("nfc");
        if (manager == null) {
            return false;
        }
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }
        return true;
    }

    public static int isSelectSE(Context context) {
        if (!isEmui60OrAbove()) {
            return selectOrCheckSE(context, true, 2);
        }
        throw new RuntimeException("NfcUtil.isSelectSE EMUI6.0 or above invoke denied.");
    }

    public static int isSelectSE(Context context, int targetSE) {
        if (!isEmui60OrAbove() || targetSE != 2) {
            return selectOrCheckSE(context, true, targetSE);
        }
        throw new RuntimeException("NfcUtil.isSelectSE EMUI6.0 or above invoke denied.");
    }

    public static int selectSE(Context context, int targetSE) {
        return selectOrCheckSE(context, false, targetSE);
    }

    public static void selectSE(Context context) {
        if (!isEmui60OrAbove()) {
            selectOrCheckSE(context, false, 2);
            return;
        }
        throw new RuntimeException("NfcUtil.selectSE EMUI6.0 or above invoke denied.");
    }

    private static int selectOrCheckSE(Context context, boolean isCheck, int targetSE) {
        int selectSETag = 0;
        try {
            Class<?> classType = Class.forName("com.huawei.android.nfc.NfcAdapterCustEx");
            if (isCheck) {
                if (targetSE == ((Integer) classType.getDeclaredMethod("getSelectedCardEmulation", new Class[]{Context.class}).invoke(classType, new Object[]{context})).intValue()) {
                    selectSETag = 1;
                } else {
                    selectSETag = -1;
                }
            } else {
                classType.getDeclaredMethod("selectCardEmulation", new Class[]{Context.class, Integer.TYPE}).invoke(classType, new Object[]{context, Integer.valueOf(targetSE)});
            }
        } catch (ClassNotFoundException ex) {
            LogC.e("isSelectSE ClassNotFoundException:" + ex, false);
        } catch (NoSuchMethodException ex2) {
            LogC.e("isSelectSE NoSuchMethodException:" + ex2, false);
        } catch (IllegalAccessException ex3) {
            LogC.e("isSelectSE IllegalAccessException:" + ex3, false);
        } catch (IllegalArgumentException ex4) {
            LogC.e("isSelectSE IllegalArgumentException:" + ex4, false);
        } catch (InvocationTargetException ex5) {
            LogC.e("isSelectSE InvocationTargetException:" + ex5, false);
        }
        return selectSETag;
    }

    public static int getNFCShowPlan(Context context) {
        int nfcShowFlag = 0;
        if (!isPhoneSupportNFC(context)) {
            LogC.d("getNFCShowPlan, The phone is not support nfc.", false);
            return 0;
        }
        String[] configs = ProductConfigUtil.getProductConfig();
        if (configs == null || configs.length == 0) {
            LogC.d("getNFCShowPlan, no product config exist.", false);
            return 0;
        }
        LogC.i("getNFCShowPlan, DealWith SupportESE.", false);
        boolean isSupportESE = false;
        if ("01".equals(configs[0])) {
            isSupportESE = true;
        }
        if (!isSupportESE) {
            LogC.d("getNFCShowPlan, do not support ese.", false);
            return 0;
        }
        if (configs.length > 4 && "01".equals(configs[3]) && "02".equals(configs[4])) {
            LogC.d("getNFCShowPlan, carrera show plan.", false);
            LogC.d("getNFCShowPlan, config fits carrera.", false);
            enableNFCOffHostService(context);
            nfcShowFlag = 2;
        }
        return nfcShowFlag;
    }

    public static boolean isPhoneSupportNFC(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.nfc");
    }

    public static void enableNFCOffHostService(Context context) {
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context.getPackageName(), "com.huawei.nfc.carrera.lifecycle.swipeservice.NFCOffHostApduService"), 1, 1);
    }

    public static boolean enableNFC(Context context) {
        try {
            NfcAdapter adpter = NfcAdapter.getDefaultAdapter(context);
            if (adpter == null) {
                return false;
            }
            return ((Boolean) adpter.getClass().getDeclaredMethod("enable", null).invoke(adpter, null)).booleanValue();
        } catch (NoSuchMethodException e) {
            LogC.e("enabledNFC NoSuchMethodException.", false);
            return false;
        } catch (IllegalAccessException e2) {
            LogC.e("enabledNFC IllegalAccessException.", false);
            return false;
        } catch (IllegalArgumentException e3) {
            LogC.e("enabledNFC IllegalArgumentException.", false);
            return false;
        } catch (InvocationTargetException e4) {
            LogC.e("enabledNFC InvocationTargetException.", false);
            return false;
        }
    }

    public static boolean disableNFC(Context context) {
        try {
            NfcAdapter adpter = NfcAdapter.getDefaultAdapter(context);
            if (adpter == null) {
                return false;
            }
            return ((Boolean) adpter.getClass().getDeclaredMethod("disable", null).invoke(adpter, null)).booleanValue();
        } catch (NoSuchMethodException e) {
            LogC.e("disableNFC NoSuchMethodException.", false);
            return false;
        } catch (IllegalAccessException e2) {
            LogC.e("disableNFC IllegalAccessException.", false);
            return false;
        } catch (IllegalArgumentException e3) {
            LogC.e("disableNFC IllegalArgumentException.", false);
            return false;
        } catch (InvocationTargetException e4) {
            LogC.e("disableNFC InvocationTargetException.", false);
            return false;
        }
    }

    public static int getCurrentSE() {
        try {
            Class<?> classType = Class.forName("com.nxp.nfc.NxpNfcAdapter");
            String value = (String) classType.getDeclaredMethod("getNfcInfo", new Class[]{String.class}).invoke(classType, new Object[]{"ese_type"});
            LogC.i("current SE is :" + value, false);
            if (!"11".equals(value) && "12".equals(value)) {
                return 12;
            }
        } catch (ClassNotFoundException ex) {
            LogC.e("getCurrentSE ClassNotFoundException:" + ex, false);
        } catch (NoSuchMethodException ex2) {
            LogC.e("getCurrentSE NoSuchMethodException:" + ex2, false);
        } catch (IllegalAccessException ex3) {
            LogC.e("getCurrentSE IllegalAccessException:" + ex3, false);
        } catch (IllegalArgumentException ex4) {
            LogC.e("getCurrentSE IllegalArgumentException:" + ex4, false);
        } catch (InvocationTargetException ex5) {
            LogC.e("getCurrentSE InvocationTargetException:" + ex5, false);
        }
        return 11;
    }

    public static boolean isCurrentDefaultPayService(Context context) {
        if (context == null) {
            return false;
        }
        return isCurDefaultPayService(context);
    }

    @TargetApi(19)
    public static boolean isCurDefaultPayService(Context context) {
        if (context == null) {
            return false;
        }
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        if (adapter == null) {
            return false;
        }
        return CardEmulation.getInstance(adapter).isDefaultServiceForCategory(new ComponentName(context.getPackageName(), "com.huawei.nfc.carrera.lifecycle.swipeservice.NFCOffHostApduService"), "payment");
    }

    public static boolean isHiseeDevice() {
        return "02".equals(ProductConfigUtil.geteSEManufacturer());
    }

    private static boolean isEmui60OrAbove() {
        return EMUIBuildUtil.VERSION.EMUI_SDK_INT >= 14;
    }

    public static boolean isTagRwEnabled(Context context) {
        try {
            NfcAdapter adpter = NfcAdapter.getDefaultAdapter(context);
            if (adpter == null) {
                return false;
            }
            return ((Boolean) adpter.getClass().getDeclaredMethod("isTagRwEnabled", null).invoke(adpter, null)).booleanValue();
        } catch (NoSuchMethodException e) {
            LogC.e("isTagRwEnabled NoSuchMethodException.", false);
            return false;
        } catch (IllegalAccessException e2) {
            LogC.e("isTagRwEnabled IllegalAccessException.", false);
            return false;
        } catch (IllegalArgumentException e3) {
            LogC.e("isTagRwEnabled IllegalArgumentException.", false);
            return false;
        } catch (InvocationTargetException e4) {
            LogC.e("isTagRwEnabled InvocationTargetException.", false);
            return false;
        }
    }

    public static void enableTagRw(Context context) {
        try {
            NfcAdapter adpter = NfcAdapter.getDefaultAdapter(context);
            if (adpter != null) {
                adpter.getClass().getDeclaredMethod("enableTagRw", null).invoke(adpter, null);
            }
        } catch (NoSuchMethodException e) {
            LogC.e("enableTagRw NoSuchMethodException.", false);
        } catch (IllegalAccessException e2) {
            LogC.e("enableTagRw IllegalAccessException.", false);
        } catch (IllegalArgumentException e3) {
            LogC.e("enableTagRw IllegalArgumentException.", false);
        } catch (InvocationTargetException e4) {
            LogC.e("enableTagRw InvocationTargetException.", false);
        }
    }

    public static String generateSrcId() {
        int randomNumber = (new SecureRandom().nextInt(10000000) + 10000000) % 10000000;
        if (randomNumber < 1000000) {
            randomNumber += 1000000;
        }
        return TimeUtil.formatDate2String(new Date(System.currentTimeMillis()), TimeUtil.YEAR_TO_MSEL_NO_LINE) + randomNumber;
    }
}

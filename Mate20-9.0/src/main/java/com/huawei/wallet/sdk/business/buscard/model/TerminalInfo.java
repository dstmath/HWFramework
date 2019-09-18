package com.huawei.wallet.sdk.business.buscard.model;

import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.IOUtils;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class TerminalInfo {
    private static final String CLSNAME_DEVICEATTESTATIONMANAGER = "com.huawei.attestation.HwAttestationManager";
    public static final String DEVICETYPE_ESN = "1";
    public static final String DEVICETYPE_IMEI = "0";
    public static final String DEVICETYPE_MEID = "2";
    public static final String DEVICETYPE_MHID = "5";
    public static final String DEVICETYPE_PCW = "3";
    public static final String DEVICETYPE_PCY = "4";
    public static final String DEVICETYPE_UNKNOWN = "-1";
    public static final String DEVICETYPE_UUID = "6";
    public static final long NO_TYPE = -1;
    public static final String TAG = "TerminalInfo";

    private static class CONSTS {
        static final String EMMACIDPATH_DEVICE = "/sys/block/mmcblk0/device/";
        static final int ERR_FAIL = -1;

        private CONSTS() {
        }
    }

    public static String getEMMCID() {
        String emmcid = getEMMCIDUseFrameWork();
        if (TextUtils.isEmpty(emmcid)) {
            LogC.i("call getEMMCIDUseFrameWork return empty!!, read it directory", false);
            emmcid = getEmmcIDDirect();
            if (TextUtils.isEmpty(emmcid)) {
                LogC.i("call getEmmcIDDirect also return empty!!", false);
            }
        }
        return emmcid;
    }

    public static String getEMMCIDUseFrameWork() {
        int typeEMMC = HwInvoke.getIntFiled(CLSNAME_DEVICEATTESTATIONMANAGER, "DEVICE_ID_TYPE_EMMC", -1);
        if (-1 == typeEMMC) {
            LogC.e("call get typeEMMC failed", false);
            return "";
        }
        try {
            Object retObj = HwInvoke.invokeFun(CLSNAME_DEVICEATTESTATIONMANAGER, "getDeviceID", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(typeEMMC)});
            if (retObj != null) {
                return new String((byte[]) retObj, AES.CHAR_ENCODING);
            }
        } catch (ParamsException e) {
            LogC.e("Params error", (Throwable) e, false);
        } catch (NoSuchMethodException e2) {
            LogC.e("NoSuchMethodException error", (Throwable) e2, false);
        } catch (UnsupportedEncodingException e3) {
            LogC.e("UnsupportedEncodingException error", (Throwable) e3, false);
        }
        return "";
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    private static java.lang.String getEmmcIDDirect() {
        /*
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            java.io.FileInputStream r6 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r8 = "/sys/block/mmcblk0/device/"
            r7.<init>(r8)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r8 = "type"
            r7.append(r8)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r7 = r7.toString()     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r6.<init>(r7)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r1 = r6
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r7 = "UTF-8"
            r6.<init>(r1, r7)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r2 = r6
            java.io.BufferedReader r6 = new java.io.BufferedReader     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r6.<init>(r2)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r3 = r6
            r6 = 500(0x1f4, float:7.0E-43)
            java.lang.String r7 = readLine(r3, r6)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r8 = 1
            if (r7 == 0) goto L_0x003c
            boolean r9 = android.text.TextUtils.isEmpty(r7)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            if (r9 == 0) goto L_0x003a
            goto L_0x003c
        L_0x003a:
            r9 = r5
            goto L_0x003d
        L_0x003c:
            r9 = 1
        L_0x003d:
            if (r9 == 0) goto L_0x0041
            r8 = 0
            goto L_0x004e
        L_0x0041:
            java.util.Locale r10 = java.util.Locale.US     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r10 = r7.toLowerCase(r10)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r11 = "mmc"
            boolean r10 = r10.contentEquals(r11)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r8 = r10
        L_0x004e:
            if (r8 == 0) goto L_0x0094
            java.lang.String r10 = "/sys/block/mmcblk0/device/"
            r0 = r10
            java.io.FileInputStream r10 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r11.<init>()     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r11.append(r0)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r12 = "cid"
            r11.append(r12)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r11 = r11.toString()     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r10.<init>(r11)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r1 = r10
            java.io.InputStreamReader r10 = new java.io.InputStreamReader     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            java.lang.String r11 = "UTF-8"
            r10.<init>(r1, r11)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r2 = r10
            java.io.BufferedReader r10 = new java.io.BufferedReader     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r10.<init>(r2)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r4 = r10
            java.lang.String r6 = readLine(r4, r6)     // Catch:{ FileNotFoundException -> 0x008e, UnsupportedEncodingException -> 0x0087, RuntimeException -> 0x0080 }
            r0 = r6
            goto L_0x0094
        L_0x007e:
            r5 = move-exception
            goto L_0x0099
        L_0x0080:
            r6 = move-exception
            java.lang.String r7 = "getEmmcIDDirect Exception."
            com.huawei.wallet.sdk.common.log.LogC.e(r7, r5)     // Catch:{ all -> 0x007e }
            goto L_0x0094
        L_0x0087:
            r6 = move-exception
            java.lang.String r7 = "getEmmcIDDirect Exception."
            com.huawei.wallet.sdk.common.log.LogC.e(r7, r5)     // Catch:{ all -> 0x007e }
            goto L_0x0094
        L_0x008e:
            r6 = move-exception
            java.lang.String r7 = "getEmmcIDDirect Exception."
            com.huawei.wallet.sdk.common.log.LogC.e(r7, r5)     // Catch:{ all -> 0x007e }
        L_0x0094:
            closeStream(r1, r2, r3, r4)
            return r0
        L_0x0099:
            closeStream(r1, r2, r3, r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.wallet.sdk.business.buscard.model.TerminalInfo.getEmmcIDDirect():java.lang.String");
    }

    private static String readLine(BufferedReader reader, int maxlen) {
        boolean start = true;
        StringBuffer strbuf = new StringBuffer();
        while (true) {
            try {
                int read = reader.read();
                int a = read;
                if (read == -1) {
                    break;
                }
                char ch = (char) a;
                if (ch != 10) {
                    if (ch != 13) {
                        start = false;
                        strbuf.append(ch);
                        if (strbuf.length() >= maxlen) {
                            break;
                        }
                    }
                }
                if (!start) {
                    break;
                }
            } catch (IOException e) {
                LogC.e("getEmmcIDDirect Exception.", false);
            }
        }
        if (strbuf.length() > 0) {
            return strbuf.toString();
        }
        return null;
    }

    private static void closeStream(FileInputStream inputStream, InputStreamReader reader, BufferedReader bufferedReaderFirst, BufferedReader bufferedReaderSecond) {
        IOUtils.closeQuietly((InputStream) inputStream);
        IOUtils.closeQuietly((Reader) reader);
        IOUtils.closeQuietly((Reader) bufferedReaderFirst);
        IOUtils.closeQuietly((Reader) bufferedReaderSecond);
    }

    public static byte[] getSign(String signatureType, String C) {
        int KEY_INDEX_HWCLOUD = HwInvoke.getIntFiled(CLSNAME_DEVICEATTESTATIONMANAGER, "KEY_INDEX_HWCLOUD", -1);
        if (-1 == KEY_INDEX_HWCLOUD) {
            LogC.e("get KEY_INDEX_HWCLOUD failed", false);
            return new byte[0];
        }
        int DEVICE_ID_TYPE_EMMC = HwInvoke.getIntFiled(CLSNAME_DEVICEATTESTATIONMANAGER, "DEVICE_ID_TYPE_EMMC", -1);
        if (-1 == DEVICE_ID_TYPE_EMMC) {
            LogC.e("get DEVICE_ID_TYPE_EMMC failed", false);
            return new byte[0];
        }
        byte[] ret = null;
        try {
            Object bytes = C.getBytes(AES.CHAR_ENCODING);
            Class<?> cls = Class.forName(CLSNAME_DEVICEATTESTATIONMANAGER);
            Object retObj = HwInvoke.invokeFun(cls, cls.newInstance(), "getAttestationSignature", new Class[]{Integer.TYPE, Integer.TYPE, String.class, byte[].class}, new Object[]{Integer.valueOf(KEY_INDEX_HWCLOUD), Integer.valueOf(DEVICE_ID_TYPE_EMMC), signatureType, bytes});
            if (retObj != null) {
                ret = (byte[]) retObj;
            }
        } catch (UnsupportedEncodingException e) {
            LogC.e("getSign", (Throwable) e, false);
        } catch (ClassNotFoundException e2) {
            LogC.e("getSign", (Throwable) e2, false);
        } catch (IllegalAccessException e3) {
            LogC.e("getSign", (Throwable) e3, false);
        } catch (InstantiationException e4) {
            LogC.e("getSign", (Throwable) e4, false);
        } catch (ParamsException e5) {
            LogC.e("getSign", (Throwable) e5, false);
        } catch (NoSuchMethodException e6) {
            LogC.e("getSign", (Throwable) e6, false);
        }
        if (ret == null || ret.length == 0) {
            LogC.e("call DeviceAttestationManager::getAttestionSignature cause err:" + getAttestationErr(), false);
        }
        return ret;
    }

    public static String getAttestationErr() {
        Object lastErr = null;
        try {
            lastErr = HwInvoke.invokeFun(CLSNAME_DEVICEATTESTATIONMANAGER, "getLastError", null, null);
        } catch (ParamsException e) {
            LogC.e("getAttestationErr ParamsException", false);
        } catch (NoSuchMethodException e2) {
            LogC.e("getAttestationErr NoSuchMethodException", false);
        }
        return String.valueOf(lastErr);
    }

    public static String getAttestationPublickKey() {
        try {
            return (String) HwInvoke.invokeFun(CLSNAME_DEVICEATTESTATIONMANAGER, "getPublickKey", new Class[]{Integer.TYPE}, new Object[]{1});
        } catch (ParamsException e) {
            LogC.e("getAttestationPublickKey ParamsException", false);
            return "";
        } catch (NoSuchMethodException e2) {
            LogC.e("getAttestationPublickKey NoSuchMethodException", false);
            return "";
        }
    }
}

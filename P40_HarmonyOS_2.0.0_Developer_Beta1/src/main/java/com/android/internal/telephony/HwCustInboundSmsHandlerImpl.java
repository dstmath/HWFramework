package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import com.android.internal.util.HexDump;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Locale;

public class HwCustInboundSmsHandlerImpl extends HwCustInboundSmsHandler {
    private static final boolean HWDBG = true;
    private static final boolean IS_IQI_Enable = SystemProperties.getBoolean("ro.config.iqi_att_support", (boolean) IS_IQI_Enable);
    private static final String TAG = "HwCustInboundSmsHandlerImpl";
    private static Class jarClass = null;
    private static Object jarObj = null;
    private Constructor<?> CONSTRUCTOR_IQClient;

    public void log(String message) {
        Rlog.d(TAG, message);
    }

    public boolean isIQIEnable() {
        return IS_IQI_Enable;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0367 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0369  */
    public boolean dispatchMessageByDestPort(int destPort, SmsMessageBase sms, Context mContext) {
        String str;
        String str2;
        String str3;
        String str4;
        String[] hexStringPduArray2;
        log("destination port before switch: " + destPort);
        if (!(destPort == 49175 || destPort == 49198)) {
            switch (destPort) {
                case 49162:
                case 49163:
                    Intent intent1 = new Intent();
                    intent1.putExtra("tid", destPort);
                    log("test_ dispatchNormalMessage 3 tid=" + destPort);
                    String[] hexStringPduArray1 = bytesToHexString(sms.getPdu()).toUpperCase(Locale.getDefault()).split("1D");
                    str4 = "1D";
                    if (hexStringPduArray1.length < 4) {
                        return IS_IQI_Enable;
                    }
                    intent1.putExtra("message", hexStringPduArray1[0]);
                    StringBuilder sb = new StringBuilder();
                    sb.append("test_ dispatchNormalMessage message = ");
                    str3 = "test_ dispatchNormalMessage message = ";
                    sb.append(hexStringPduArray1[0]);
                    log(sb.toString());
                    intent1.putExtra("appId", new String(hexStringToBytes(hexStringPduArray1[1]), Charset.defaultCharset()));
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("test_ dispatchNormalMessage appId = ");
                    str2 = "test_ dispatchNormalMessage appId = ";
                    sb2.append(new String(hexStringToBytes(hexStringPduArray1[1]), Charset.defaultCharset()));
                    log(sb2.toString());
                    intent1.putExtra("cmd", Integer.valueOf(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())));
                    log("test_ dispatchNormalMessage cmd = " + Integer.valueOf(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())));
                    intent1.putExtra("payload", new String(subBytes(hexStringToBytes(hexStringPduArray1[3]), 0, hexStringToBytes(hexStringPduArray1[3]).length - 1), Charset.defaultCharset()));
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("test_ dispatchNormalMessage payload = ");
                    str = "payload";
                    sb3.append(new String(subBytes(hexStringToBytes(hexStringPduArray1[3]), 0, hexStringToBytes(hexStringPduArray1[3]).length - 1), Charset.defaultCharset()));
                    log(sb3.toString());
                    if (Integer.parseInt(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())) == 0) {
                        intent1.setAction("android.lgt.action.CMD_EQUALS_ZERO");
                        intent1.putExtra("body", HexDump.toHexString(sms.getPdu()));
                        log("test_ dispatchNormalMessage body = " + HexDump.toHexString(sms.getPdu()));
                        mContext.sendBroadcast(intent1);
                        log("test_ dispatchNormalMessage before startService for cmd=0");
                        return IS_IQI_Enable;
                    } else if (Integer.parseInt(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())) == 1) {
                        intent1.setAction("android.lgt.action.APM_START_APP");
                        mContext.startService(intent1);
                        log("test_ dispatchNormalMessage after startService for cmd=1");
                        return HWDBG;
                    }
                    break;
                default:
                    switch (destPort) {
                        case 49200:
                        case 49201:
                        case 49202:
                        case 49204:
                            break;
                        case 49203:
                            Intent intent3 = new Intent("android.lgt.action.APM_SMS_RECEIVED");
                            intent3.putExtra("tid", destPort);
                            log("test_ dispatchNormalMessage 5 tid=" + destPort);
                            String[] hexStringPduArray3 = bytesToHexString(sms.getPdu()).toUpperCase(Locale.getDefault()).split("1D");
                            if (hexStringPduArray3.length < 4) {
                                return IS_IQI_Enable;
                            }
                            intent3.putExtra("message", hexStringPduArray3[0]);
                            log("test_ dispatchNormalMessage message = " + hexStringPduArray3[0]);
                            intent3.putExtra("appId", new String(hexStringToBytes(hexStringPduArray3[1]), Charset.defaultCharset()));
                            log("test_ dispatchNormalMessage appId = " + new String(hexStringToBytes(hexStringPduArray3[1]), Charset.defaultCharset()));
                            intent3.putExtra("cmd", Integer.valueOf(new String(hexStringToBytes(hexStringPduArray3[2]), Charset.defaultCharset())));
                            log("test_ dispatchNormalMessage cmd = " + Integer.valueOf(new String(hexStringToBytes(hexStringPduArray3[2]), Charset.defaultCharset())));
                            intent3.putExtra("payload", "0x" + Integer.toHexString(subBytes(hexStringToBytes(hexStringPduArray3[3]), 0, 1)[0] & 255) + new String(subBytes(hexStringToBytes(hexStringPduArray3[3]), 1, hexStringToBytes(hexStringPduArray3[3]).length - 1), Charset.defaultCharset()));
                            log("test_ dispatchNormalMessage payload = 0x" + Integer.toHexString(subBytes(hexStringToBytes(hexStringPduArray3[3]), 0, 1)[0] & 255) + new String(subBytes(hexStringToBytes(hexStringPduArray3[3]), 1, hexStringToBytes(hexStringPduArray3[3]).length - 1), Charset.defaultCharset()));
                            mContext.sendBroadcast(intent3);
                            log("test_ dispatchNormalMessage after sendBroadcast for 49203");
                            return HWDBG;
                        default:
                            return IS_IQI_Enable;
                    }
            }
            Intent intent2 = new Intent("android.lgt.action.APM_SMS_RECEIVED");
            intent2.putExtra("tid", destPort);
            log("test_ dispatchNormalMessage 4 tid=" + destPort);
            hexStringPduArray2 = bytesToHexString(sms.getPdu()).toUpperCase(Locale.getDefault()).split(str4);
            if (hexStringPduArray2.length >= 4) {
                return IS_IQI_Enable;
            }
            intent2.putExtra("message", hexStringPduArray2[0]);
            log(str3 + hexStringPduArray2[0]);
            intent2.putExtra("appId", new String(hexStringToBytes(hexStringPduArray2[1]), Charset.defaultCharset()));
            log(str2 + new String(hexStringToBytes(hexStringPduArray2[1]), Charset.defaultCharset()));
            intent2.putExtra("cmd", Integer.valueOf(new String(hexStringToBytes(hexStringPduArray2[2]), Charset.defaultCharset())));
            log("test_ dispatchNormalMessage cmd = " + Integer.valueOf(new String(hexStringToBytes(hexStringPduArray2[2]), Charset.defaultCharset())));
            intent2.putExtra(str, new String(subBytes(hexStringToBytes(hexStringPduArray2[3]), 0, hexStringToBytes(hexStringPduArray2[3]).length - 1), Charset.defaultCharset()));
            log("test_ dispatchNormalMessage payload = " + new String(subBytes(hexStringToBytes(hexStringPduArray2[3]), 0, hexStringToBytes(hexStringPduArray2[3]).length - 1), Charset.defaultCharset()));
            mContext.sendBroadcast(intent2);
            log("test_ dispatchNormalMessage after sendBroadcast");
            return HWDBG;
        }
        str = "payload";
        str2 = "test_ dispatchNormalMessage appId = ";
        str3 = "test_ dispatchNormalMessage message = ";
        str4 = "1D";
        Intent intent22 = new Intent("android.lgt.action.APM_SMS_RECEIVED");
        intent22.putExtra("tid", destPort);
        log("test_ dispatchNormalMessage 4 tid=" + destPort);
        hexStringPduArray2 = bytesToHexString(sms.getPdu()).toUpperCase(Locale.getDefault()).split(str4);
        if (hexStringPduArray2.length >= 4) {
        }
    }

    public byte[] subBytes(byte[] src, int begin, int end) {
        byte[] bs = new byte[(end - begin)];
        for (int i = begin; i < end; i++) {
            bs[i - begin] = src[i];
        }
        return bs;
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return stringBuilder.toString();
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return new byte[0];
        }
        String hexString2 = hexString.toUpperCase(Locale.getDefault());
        int length = hexString2.length() / 2;
        char[] hexChars = hexString2.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public void createIQClient(Context mContext) {
        try {
            setJarClass(Class.forName("com.carrieriq.iqagent.client.IQClientUtil"));
            this.CONSTRUCTOR_IQClient = jarClass.getConstructor(Context.class);
            setJarObj(this.CONSTRUCTOR_IQClient.newInstance(mContext));
        } catch (Exception e) {
            log("get client error in createIQClient");
        }
    }

    public boolean isIQISms(SmsMessage sms) {
        try {
            return ((Boolean) jarClass.getMethod("checkSMS", String.class).invoke(jarObj, sms.getMessageBody())).booleanValue();
        } catch (Exception e) {
            log("check SMS error in isIQISms");
            return IS_IQI_Enable;
        }
    }

    public boolean isIQIWapPush(ByteArrayOutputStream output) {
        try {
            return ((Boolean) jarClass.getMethod("checkWAPPush", byte[].class).invoke(jarObj, output.toByteArray())).booleanValue();
        } catch (Exception e) {
            log("check WapPush error in isIQIWapPush");
            return IS_IQI_Enable;
        }
    }

    public static void setJarClass(Class iqiClass) {
        jarClass = iqiClass;
    }

    public static void setJarObj(Object iqiObj) {
        jarObj = iqiObj;
    }

    public boolean isNotNotifyWappushEnabled(AsyncResult ar) {
        if (!SystemProperties.getBoolean("ro.config.hw_nonotify_wap", (boolean) IS_IQI_Enable) || !isWapPushMessage(ar)) {
            return IS_IQI_Enable;
        }
        return HWDBG;
    }

    public boolean isBlockMsgReceive(int slotId) {
        return HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId);
    }

    private boolean isWapPushMessage(AsyncResult ar) {
        try {
            SmsHeader smsHeader = ((SmsMessage) ar.result).mWrappedSmsMessage.getUserDataHeader();
            if (smsHeader == null || smsHeader.portAddrs == null || 2948 != smsHeader.portAddrs.destPort) {
                return IS_IQI_Enable;
            }
            return HWDBG;
        } catch (Exception e) {
            Rlog.e(TAG, "parse massage error in isWapPushMessage");
            return IS_IQI_Enable;
        }
    }
}

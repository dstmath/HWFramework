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
    private static final boolean IS_IQI_Enable = SystemProperties.getBoolean("ro.config.iqi_att_support", false);
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

    public boolean dispatchMessageByDestPort(int destPort, SmsMessageBase sms, Context mContext) {
        log("destination port before switch: " + destPort);
        if (!(destPort == 49175 || destPort == 49198)) {
            switch (destPort) {
                case 49162:
                case 49163:
                    Intent intent1 = new Intent();
                    intent1.putExtra("tid", destPort);
                    log("test_ dispatchNormalMessage 3 tid=" + destPort);
                    String[] hexStringPduArray1 = bytesToHexString(sms.getPdu()).toUpperCase(Locale.getDefault()).split("1D");
                    if (hexStringPduArray1.length < 4) {
                        return false;
                    }
                    intent1.putExtra("message", hexStringPduArray1[0]);
                    log("test_ dispatchNormalMessage message = " + hexStringPduArray1[0]);
                    intent1.putExtra("appId", new String(hexStringToBytes(hexStringPduArray1[1]), Charset.defaultCharset()));
                    log("test_ dispatchNormalMessage appId = " + new String(hexStringToBytes(hexStringPduArray1[1]), Charset.defaultCharset()));
                    intent1.putExtra("cmd", Integer.valueOf(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())));
                    log("test_ dispatchNormalMessage cmd = " + Integer.valueOf(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())));
                    intent1.putExtra("payload", new String(subBytes(hexStringToBytes(hexStringPduArray1[3]), 0, hexStringToBytes(hexStringPduArray1[3]).length - 1), Charset.defaultCharset()));
                    log("test_ dispatchNormalMessage payload = " + new String(subBytes(hexStringToBytes(hexStringPduArray1[3]), 0, hexStringToBytes(hexStringPduArray1[3]).length - 1), Charset.defaultCharset()));
                    if (Integer.parseInt(new String(hexStringToBytes(hexStringPduArray1[2]), Charset.defaultCharset())) == 0) {
                        intent1.setAction("android.lgt.action.CMD_EQUALS_ZERO");
                        intent1.putExtra("body", HexDump.toHexString(sms.getPdu()));
                        log("test_ dispatchNormalMessage body = " + HexDump.toHexString(sms.getPdu()));
                        mContext.sendBroadcast(intent1);
                        log("test_ dispatchNormalMessage before startService for cmd=0");
                        return false;
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
                                return false;
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
                            return false;
                    }
            }
        }
        Intent intent2 = new Intent("android.lgt.action.APM_SMS_RECEIVED");
        intent2.putExtra("tid", destPort);
        log("test_ dispatchNormalMessage 4 tid=" + destPort);
        String[] hexStringPduArray2 = bytesToHexString(sms.getPdu()).toUpperCase(Locale.getDefault()).split("1D");
        if (hexStringPduArray2.length < 4) {
            return false;
        }
        intent2.putExtra("message", hexStringPduArray2[0]);
        log("test_ dispatchNormalMessage message = " + hexStringPduArray2[0]);
        intent2.putExtra("appId", new String(hexStringToBytes(hexStringPduArray2[1]), Charset.defaultCharset()));
        log("test_ dispatchNormalMessage appId = " + new String(hexStringToBytes(hexStringPduArray2[1]), Charset.defaultCharset()));
        intent2.putExtra("cmd", Integer.valueOf(new String(hexStringToBytes(hexStringPduArray2[2]), Charset.defaultCharset())));
        log("test_ dispatchNormalMessage cmd = " + Integer.valueOf(new String(hexStringToBytes(hexStringPduArray2[2]), Charset.defaultCharset())));
        intent2.putExtra("payload", new String(subBytes(hexStringToBytes(hexStringPduArray2[3]), 0, hexStringToBytes(hexStringPduArray2[3]).length - 1), Charset.defaultCharset()));
        log("test_ dispatchNormalMessage payload = " + new String(subBytes(hexStringToBytes(hexStringPduArray2[3]), 0, hexStringToBytes(hexStringPduArray2[3]).length - 1), Charset.defaultCharset()));
        mContext.sendBroadcast(intent2);
        log("test_ dispatchNormalMessage after sendBroadcast");
        return HWDBG;
    }

    public byte[] subBytes(byte[] src, int begin, int end) {
        byte[] bs = new byte[(end - begin)];
        for (int i = begin; i < end; i++) {
            bs[i - begin] = src[i];
        }
        return bs;
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
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
            this.CONSTRUCTOR_IQClient = jarClass.getConstructor(new Class[]{Context.class});
            setJarObj(this.CONSTRUCTOR_IQClient.newInstance(new Object[]{mContext}));
        } catch (Exception e) {
            log("get client error" + e);
        }
    }

    public boolean isIQISms(SmsMessage sms) {
        try {
            return ((Boolean) jarClass.getMethod("checkSMS", new Class[]{String.class}).invoke(jarObj, new Object[]{sms.getMessageBody()})).booleanValue();
        } catch (Exception e) {
            log("check SMS error" + e);
            return false;
        }
    }

    public boolean isIQIWapPush(ByteArrayOutputStream output) {
        try {
            return ((Boolean) jarClass.getMethod("checkWAPPush", new Class[]{byte[].class}).invoke(jarObj, new Object[]{output.toByteArray()})).booleanValue();
        } catch (Exception e) {
            log("check WapPush error" + e);
            return false;
        }
    }

    public static void setJarClass(Class iqiClass) {
        jarClass = iqiClass;
    }

    public static void setJarObj(Object iqiObj) {
        jarObj = iqiObj;
    }

    public boolean isNotNotifyWappushEnabled(AsyncResult ar) {
        if (!SystemProperties.getBoolean("ro.config.hw_nonotify_wap", false) || !isWapPushMessage(ar)) {
            return false;
        }
        return HWDBG;
    }

    public boolean isBlockMsgReceive(int subId) {
        return isBlockMsgReceiveByNonAis(subId);
    }

    private boolean isBlockMsgReceiveByNonAis(int subId) {
        if (!HwTelephonyManagerInner.getDefault().isCustomAis() || HwTelephonyManagerInner.getDefault().isAISCard(subId) || HwTelephonyManagerInner.getDefault().isAisCustomDisable()) {
            return false;
        }
        log("ais custom version, but not ais card. block msg receive.");
        return HWDBG;
    }

    private boolean isWapPushMessage(AsyncResult ar) {
        try {
            SmsHeader smsHeader = ((SmsMessage) ar.result).mWrappedSmsMessage.getUserDataHeader();
            if (smsHeader == null || smsHeader.portAddrs == null || 2948 != smsHeader.portAddrs.destPort) {
                return false;
            }
            return HWDBG;
        } catch (Exception e) {
            Rlog.e(TAG, "parse massage error:" + e);
            return false;
        }
    }
}

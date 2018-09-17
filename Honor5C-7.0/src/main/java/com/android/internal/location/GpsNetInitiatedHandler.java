package com.android.internal.location;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.INetInitiatedListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.app.NetInitiatedActivity;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import java.io.UnsupportedEncodingException;

public class GpsNetInitiatedHandler {
    public static final String ACTION_NI_VERIFY = "android.intent.action.NETWORK_INITIATED_VERIFY";
    private static final boolean DEBUG = true;
    public static final int GPS_ENC_NONE = 0;
    public static final int GPS_ENC_SUPL_GSM_DEFAULT = 1;
    public static final int GPS_ENC_SUPL_UCS2 = 3;
    public static final int GPS_ENC_SUPL_UTF8 = 2;
    public static final int GPS_ENC_UNKNOWN = -1;
    public static final int GPS_NI_NEED_NOTIFY = 1;
    public static final int GPS_NI_NEED_VERIFY = 2;
    public static final int GPS_NI_PRIVACY_OVERRIDE = 4;
    public static final int GPS_NI_RESPONSE_ACCEPT = 1;
    public static final int GPS_NI_RESPONSE_DENY = 2;
    public static final int GPS_NI_RESPONSE_IGNORE = 4;
    public static final int GPS_NI_RESPONSE_NORESP = 3;
    public static final int GPS_NI_TYPE_EMERGENCY_SUPL = 4;
    public static final int GPS_NI_TYPE_UMTS_CTRL_PLANE = 3;
    public static final int GPS_NI_TYPE_UMTS_SUPL = 2;
    public static final int GPS_NI_TYPE_VOICE = 1;
    public static final String NI_EXTRA_CMD_NOTIF_ID = "notif_id";
    public static final String NI_EXTRA_CMD_RESPONSE = "response";
    public static final String NI_INTENT_KEY_DEFAULT_RESPONSE = "default_resp";
    public static final String NI_INTENT_KEY_MESSAGE = "message";
    public static final String NI_INTENT_KEY_NOTIF_ID = "notif_id";
    public static final String NI_INTENT_KEY_TIMEOUT = "timeout";
    public static final String NI_INTENT_KEY_TITLE = "title";
    public static final String NI_RESPONSE_EXTRA_CMD = "send_ni_response";
    private static final String TAG = "GpsNetInitiatedHandler";
    private static final boolean VERBOSE = false;
    private static boolean mIsHexInput;
    private final BroadcastReceiver mBroadcastReciever;
    private final Context mContext;
    private volatile boolean mIsInEmergency;
    private volatile boolean mIsLocationEnabled;
    private volatile boolean mIsSuplEsEnabled;
    private final LocationManager mLocationManager;
    private final INetInitiatedListener mNetInitiatedListener;
    private Builder mNiNotificationBuilder;
    private final PhoneStateListener mPhoneStateListener;
    private boolean mPlaySounds;
    private boolean mPopupImmediately;
    private final TelephonyManager mTelephonyManager;

    public static class GpsNiNotification {
        public int defaultResponse;
        public Bundle extras;
        public boolean needNotify;
        public boolean needVerify;
        public int niType;
        public int notificationId;
        public boolean privacyOverride;
        public String requestorId;
        public int requestorIdEncoding;
        public String text;
        public int textEncoding;
        public int timeout;
    }

    public static class GpsNiResponse {
        Bundle extras;
        int userResponse;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.location.GpsNetInitiatedHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.location.GpsNetInitiatedHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.location.GpsNetInitiatedHandler.<clinit>():void");
    }

    public GpsNetInitiatedHandler(Context context, INetInitiatedListener netInitiatedListener, boolean isSuplEsEnabled) {
        this.mPlaySounds = false;
        this.mPopupImmediately = DEBUG;
        this.mIsLocationEnabled = false;
        this.mBroadcastReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
                    GpsNetInitiatedHandler.this.setInEmergency(PhoneNumberUtils.isEmergencyNumber(intent.getStringExtra("android.intent.extra.PHONE_NUMBER")));
                    Log.v(GpsNetInitiatedHandler.TAG, "ACTION_NEW_OUTGOING_CALL - " + GpsNetInitiatedHandler.this.getInEmergency());
                } else if (action.equals("android.location.MODE_CHANGED")) {
                    GpsNetInitiatedHandler.this.updateLocationMode();
                    Log.d(GpsNetInitiatedHandler.TAG, "location enabled :" + GpsNetInitiatedHandler.this.getLocationEnabled());
                }
            }
        };
        this.mContext = context;
        if (netInitiatedListener == null) {
            throw new IllegalArgumentException("netInitiatedListener is null");
        }
        this.mNetInitiatedListener = netInitiatedListener;
        setSuplEsEnabled(isSuplEsEnabled);
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        updateLocationMode();
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                Log.d(GpsNetInitiatedHandler.TAG, "onCallStateChanged(): state is " + state);
                if (state == 0) {
                    GpsNetInitiatedHandler.this.setInEmergency(false);
                }
            }
        };
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.addAction("android.location.MODE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReciever, intentFilter);
    }

    public void setSuplEsEnabled(boolean isEnabled) {
        this.mIsSuplEsEnabled = isEnabled;
    }

    public boolean getSuplEsEnabled() {
        return this.mIsSuplEsEnabled;
    }

    public void updateLocationMode() {
        this.mIsLocationEnabled = this.mLocationManager.isProviderEnabled("gps");
    }

    public boolean getLocationEnabled() {
        return this.mIsLocationEnabled;
    }

    public void setInEmergency(boolean isInEmergency) {
        this.mIsInEmergency = isInEmergency;
    }

    public boolean getInEmergency() {
        return !this.mIsInEmergency ? Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE)) : DEBUG;
    }

    public void handleNiNotification(GpsNiNotification notif) {
        Log.d(TAG, "in handleNiNotification () : notificationId: " + notif.notificationId + " requestorId: " + notif.requestorId + " text: " + notif.text + " mIsSuplEsEnabled" + getSuplEsEnabled() + " mIsLocationEnabled" + getLocationEnabled());
        if (getSuplEsEnabled()) {
            handleNiInEs(notif);
        } else {
            handleNi(notif);
        }
    }

    private void handleNi(GpsNiNotification notif) {
        Log.d(TAG, "in handleNi () : needNotify: " + notif.needNotify + " needVerify: " + notif.needVerify + " privacyOverride: " + notif.privacyOverride + " mPopupImmediately: " + this.mPopupImmediately + " mInEmergency: " + getInEmergency());
        if (!(getLocationEnabled() || getInEmergency())) {
            try {
                this.mNetInitiatedListener.sendNiResponse(notif.notificationId, GPS_NI_TYPE_EMERGENCY_SUPL);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in sendNiResponse");
            }
        }
        if (notif.needNotify) {
            if (notif.needVerify && this.mPopupImmediately) {
                openNiDialog(notif);
            } else {
                setNiNotification(notif);
            }
        }
        if (!notif.needVerify || notif.privacyOverride) {
            try {
                this.mNetInitiatedListener.sendNiResponse(notif.notificationId, GPS_NI_TYPE_VOICE);
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in sendNiResponse");
            }
        }
    }

    private void handleNiInEs(GpsNiNotification notif) {
        Log.d(TAG, "in handleNiInEs () : niType: " + notif.niType + " notificationId: " + notif.notificationId);
        if ((notif.niType == GPS_NI_TYPE_EMERGENCY_SUPL ? DEBUG : false) != getInEmergency()) {
            try {
                this.mNetInitiatedListener.sendNiResponse(notif.notificationId, GPS_NI_TYPE_EMERGENCY_SUPL);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in sendNiResponse");
                return;
            }
        }
        handleNi(notif);
    }

    private synchronized void setNiNotification(GpsNiNotification notif) {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null) {
            String title = getNotifTitle(notif, this.mContext);
            String message = getNotifMessage(notif, this.mContext);
            Log.d(TAG, "setNiNotification, notifyId: " + notif.notificationId + ", title: " + title + ", message: " + message);
            if (this.mNiNotificationBuilder == null) {
                this.mNiNotificationBuilder = new Builder(this.mContext).setSmallIcon(R.drawable.stat_sys_gps_on).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), androidhwext.R.drawable.stat_sys_gps_on_large)).setWhen(0).setOngoing(DEBUG).setAutoCancel(DEBUG).setColor(this.mContext.getColor(R.color.system_notification_accent_color));
            }
            if (this.mPlaySounds) {
                this.mNiNotificationBuilder.setDefaults(GPS_NI_TYPE_VOICE);
            } else {
                this.mNiNotificationBuilder.setDefaults(GPS_ENC_NONE);
            }
            this.mNiNotificationBuilder.setTicker(getNotifTicker(notif, this.mContext)).setContentTitle(title).setContentText(message).setStyle(new BigTextStyle().bigText(message)).setContentIntent(PendingIntent.getBroadcast(this.mContext, GPS_ENC_NONE, !this.mPopupImmediately ? getDlgIntent(notif) : new Intent(), GPS_ENC_NONE));
            notificationManager.notifyAsUser(null, notif.notificationId, this.mNiNotificationBuilder.build(), UserHandle.ALL);
        }
    }

    private void openNiDialog(GpsNiNotification notif) {
        Intent intent = getDlgIntent(notif);
        Log.d(TAG, "openNiDialog, notifyId: " + notif.notificationId + ", requestorId: " + notif.requestorId + ", text: " + notif.text);
        this.mContext.startActivity(intent);
    }

    private Intent getDlgIntent(GpsNiNotification notif) {
        Intent intent = new Intent();
        String title = getDialogTitle(notif, this.mContext);
        String message = getDialogMessage(notif, this.mContext);
        intent.setFlags(268468224);
        intent.setClass(this.mContext, NetInitiatedActivity.class);
        intent.putExtra(NI_INTENT_KEY_NOTIF_ID, notif.notificationId);
        intent.putExtra(NI_INTENT_KEY_TITLE, title);
        intent.putExtra(NI_INTENT_KEY_MESSAGE, message);
        intent.putExtra(NI_INTENT_KEY_TIMEOUT, notif.timeout);
        intent.putExtra(NI_INTENT_KEY_DEFAULT_RESPONSE, notif.defaultResponse);
        Log.d(TAG, "generateIntent, title: " + title + ", message: " + message + ", timeout: " + notif.timeout);
        return intent;
    }

    static byte[] stringToByteArray(String original, boolean isHex) {
        int length = isHex ? original.length() / GPS_NI_TYPE_UMTS_SUPL : original.length();
        byte[] output = new byte[length];
        int i;
        if (isHex) {
            for (i = GPS_ENC_NONE; i < length; i += GPS_NI_TYPE_VOICE) {
                output[i] = (byte) Integer.parseInt(original.substring(i * GPS_NI_TYPE_UMTS_SUPL, (i * GPS_NI_TYPE_UMTS_SUPL) + GPS_NI_TYPE_UMTS_SUPL), 16);
            }
        } else {
            for (i = GPS_ENC_NONE; i < length; i += GPS_NI_TYPE_VOICE) {
                output[i] = (byte) original.charAt(i);
            }
        }
        return output;
    }

    static String decodeGSMPackedString(byte[] input) {
        int lengthBytes = input.length;
        int lengthSeptets = (lengthBytes * 8) / 7;
        if (lengthBytes % 7 == 0 && lengthBytes > 0 && (input[lengthBytes + GPS_ENC_UNKNOWN] >> GPS_NI_TYPE_VOICE) == 0) {
            lengthSeptets += GPS_ENC_UNKNOWN;
        }
        String decoded = GsmAlphabet.gsm7BitPackedToString(input, GPS_ENC_NONE, lengthSeptets);
        if (decoded != null) {
            return decoded;
        }
        Log.e(TAG, "Decoding of GSM packed string failed");
        return "";
    }

    static String decodeUTF8String(byte[] input) {
        String str = "";
        try {
            return new String(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    static String decodeUCS2String(byte[] input) {
        String str = "";
        try {
            return new String(input, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    private static String decodeString(String original, boolean isHex, int coding) {
        String decoded = original;
        byte[] input = stringToByteArray(original, isHex);
        switch (coding) {
            case GPS_ENC_UNKNOWN /*-1*/:
                return original;
            case GPS_ENC_NONE /*0*/:
                return decodeUTF8String(input);
            case GPS_NI_TYPE_VOICE /*1*/:
                return decodeGSMPackedString(input);
            case GPS_NI_TYPE_UMTS_SUPL /*2*/:
                return decodeUTF8String(input);
            case GPS_NI_TYPE_UMTS_CTRL_PLANE /*3*/:
                return decodeUCS2String(input);
            default:
                Log.e(TAG, "Unknown encoding " + coding + " for NI text " + original);
                return decoded;
        }
    }

    private static String getNotifTicker(GpsNiNotification notif, Context context) {
        String string = context.getString(R.string.gpsNotifTicker);
        Object[] objArr = new Object[GPS_NI_TYPE_UMTS_SUPL];
        objArr[GPS_ENC_NONE] = decodeString(notif.requestorId, mIsHexInput, notif.requestorIdEncoding);
        objArr[GPS_NI_TYPE_VOICE] = decodeString(notif.text, mIsHexInput, notif.textEncoding);
        return String.format(string, objArr);
    }

    private static String getNotifTitle(GpsNiNotification notif, Context context) {
        return String.format(context.getString(R.string.gpsNotifTitle), new Object[GPS_ENC_NONE]);
    }

    private static String getNotifMessage(GpsNiNotification notif, Context context) {
        String string = context.getString(R.string.gpsNotifMessage);
        Object[] objArr = new Object[GPS_NI_TYPE_UMTS_SUPL];
        objArr[GPS_ENC_NONE] = decodeString(notif.requestorId, mIsHexInput, notif.requestorIdEncoding);
        objArr[GPS_NI_TYPE_VOICE] = decodeString(notif.text, mIsHexInput, notif.textEncoding);
        return String.format(string, objArr);
    }

    public static String getDialogTitle(GpsNiNotification notif, Context context) {
        return getNotifTitle(notif, context);
    }

    private static String getDialogMessage(GpsNiNotification notif, Context context) {
        return getNotifMessage(notif, context);
    }
}

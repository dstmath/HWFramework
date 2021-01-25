package com.android.internal.location;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.INetInitiatedListener;
import android.location.LocationManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.app.NetInitiatedActivity;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

public class GpsNetInitiatedHandler {
    public static final String ACTION_NI_VERIFY = "android.intent.action.NETWORK_INITIATED_VERIFY";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
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
    private static boolean mIsHexInput = true;
    private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
        /* class com.android.internal.location.GpsNetInitiatedHandler.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                GpsNetInitiatedHandler.this.mIsInEmergencyCall = PhoneNumberUtils.isEmergencyNumber(phoneNumber);
                if (GpsNetInitiatedHandler.DEBUG) {
                    Log.v(GpsNetInitiatedHandler.TAG, "ACTION_NEW_OUTGOING_CALL - " + GpsNetInitiatedHandler.this.getInEmergency());
                }
            } else if (action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                GpsNetInitiatedHandler.this.updateLocationMode();
                if (GpsNetInitiatedHandler.DEBUG) {
                    Log.d(GpsNetInitiatedHandler.TAG, "location enabled :" + GpsNetInitiatedHandler.this.getLocationEnabled());
                }
            }
        }
    };
    private volatile long mCallEndElapsedRealtimeMillis = 0;
    private final Context mContext;
    private volatile long mEmergencyExtensionMillis = 0;
    private volatile boolean mIsInEmergencyCall;
    private volatile boolean mIsLocationEnabled = false;
    private volatile boolean mIsSuplEsEnabled;
    private final LocationManager mLocationManager;
    private final INetInitiatedListener mNetInitiatedListener;
    private Notification.Builder mNiNotificationBuilder;
    private final PhoneStateListener mPhoneStateListener;
    private boolean mPlaySounds = false;
    private boolean mPopupImmediately = true;
    private final TelephonyManager mTelephonyManager;

    public static class GpsNiNotification {
        public int defaultResponse;
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
        int userResponse;
    }

    public GpsNetInitiatedHandler(Context context, INetInitiatedListener netInitiatedListener, boolean isSuplEsEnabled) {
        this.mContext = context;
        if (netInitiatedListener != null) {
            this.mNetInitiatedListener = netInitiatedListener;
            setSuplEsEnabled(isSuplEsEnabled);
            this.mLocationManager = (LocationManager) context.getSystemService("location");
            updateLocationMode();
            this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            this.mPhoneStateListener = new PhoneStateListener() {
                /* class com.android.internal.location.GpsNetInitiatedHandler.AnonymousClass2 */

                @Override // android.telephony.PhoneStateListener
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (GpsNetInitiatedHandler.DEBUG) {
                        Log.d(GpsNetInitiatedHandler.TAG, "onCallStateChanged(): state is " + state);
                    }
                    if (state == 0 && GpsNetInitiatedHandler.this.mIsInEmergencyCall) {
                        GpsNetInitiatedHandler.this.mCallEndElapsedRealtimeMillis = SystemClock.elapsedRealtime();
                        GpsNetInitiatedHandler.this.mIsInEmergencyCall = false;
                    }
                }
            };
            this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
            intentFilter.addAction(LocationManager.MODE_CHANGED_ACTION);
            this.mContext.registerReceiver(this.mBroadcastReciever, intentFilter);
            return;
        }
        throw new IllegalArgumentException("netInitiatedListener is null");
    }

    public void setSuplEsEnabled(boolean isEnabled) {
        this.mIsSuplEsEnabled = isEnabled;
    }

    public boolean getSuplEsEnabled() {
        return this.mIsSuplEsEnabled;
    }

    public void updateLocationMode() {
        this.mIsLocationEnabled = this.mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean getLocationEnabled() {
        return this.mIsLocationEnabled;
    }

    public boolean getInEmergency() {
        return this.mIsInEmergencyCall || this.mTelephonyManager.getEmergencyCallbackMode() || ((this.mCallEndElapsedRealtimeMillis > 0 ? 1 : (this.mCallEndElapsedRealtimeMillis == 0 ? 0 : -1)) > 0 && ((SystemClock.elapsedRealtime() - this.mCallEndElapsedRealtimeMillis) > this.mEmergencyExtensionMillis ? 1 : ((SystemClock.elapsedRealtime() - this.mCallEndElapsedRealtimeMillis) == this.mEmergencyExtensionMillis ? 0 : -1)) < 0) || this.mTelephonyManager.isInEmergencySmsMode();
    }

    public void setEmergencyExtensionSeconds(int emergencyExtensionSeconds) {
        this.mEmergencyExtensionMillis = TimeUnit.SECONDS.toMillis((long) emergencyExtensionSeconds);
    }

    public void handleNiNotification(GpsNiNotification notif) {
        if (DEBUG) {
            Log.d(TAG, "in handleNiNotification () : notificationId: " + notif.notificationId + " requestorId: " + notif.requestorId + " text: " + notif.text + " mIsSuplEsEnabled" + getSuplEsEnabled() + " mIsLocationEnabled" + getLocationEnabled());
        }
        if (getSuplEsEnabled()) {
            handleNiInEs(notif);
        } else {
            handleNi(notif);
        }
    }

    private void handleNi(GpsNiNotification notif) {
        if (DEBUG) {
            Log.d(TAG, "in handleNi () : needNotify: " + notif.needNotify + " needVerify: " + notif.needVerify + " privacyOverride: " + notif.privacyOverride + " mPopupImmediately: " + this.mPopupImmediately + " mInEmergency: " + getInEmergency());
        }
        if (!getLocationEnabled() && !getInEmergency()) {
            try {
                this.mNetInitiatedListener.sendNiResponse(notif.notificationId, 4);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in sendNiResponse");
            }
        }
        if (notif.needNotify) {
            if (!notif.needVerify || !this.mPopupImmediately) {
                setNiNotification(notif);
            } else {
                openNiDialog(notif);
            }
        }
        if (!notif.needVerify || notif.privacyOverride) {
            try {
                this.mNetInitiatedListener.sendNiResponse(notif.notificationId, 1);
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in sendNiResponse");
            }
        }
    }

    private void handleNiInEs(GpsNiNotification notif) {
        if (DEBUG) {
            Log.d(TAG, "in handleNiInEs () : niType: " + notif.niType + " notificationId: " + notif.notificationId);
        }
        if ((notif.niType == 4) != getInEmergency()) {
            try {
                this.mNetInitiatedListener.sendNiResponse(notif.notificationId, 4);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in sendNiResponse");
            }
        } else {
            handleNi(notif);
        }
    }

    private synchronized void setNiNotification(GpsNiNotification notif) {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null) {
            String title = getNotifTitle(notif, this.mContext);
            String message = getNotifMessage(notif, this.mContext);
            if (DEBUG) {
                Log.d(TAG, "setNiNotification, notifyId: " + notif.notificationId + ", title: " + title + ", message: " + message);
            }
            if (this.mNiNotificationBuilder == null) {
                this.mNiNotificationBuilder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_ALERTS).setSmallIcon(R.drawable.stat_sys_gps_on).setWhen(0).setOngoing(true).setAutoCancel(true).setColor(this.mContext.getColor(17170460));
            }
            if (this.mPlaySounds) {
                this.mNiNotificationBuilder.setDefaults(1);
            } else {
                this.mNiNotificationBuilder.setDefaults(0);
            }
            this.mNiNotificationBuilder.setTicker(getNotifTicker(notif, this.mContext)).setContentTitle(title).setContentText(message).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, !this.mPopupImmediately ? getDlgIntent(notif) : new Intent(), 0));
            notificationManager.notifyAsUser(null, notif.notificationId, this.mNiNotificationBuilder.build(), UserHandle.ALL);
        }
    }

    private void openNiDialog(GpsNiNotification notif) {
        Intent intent = getDlgIntent(notif);
        if (DEBUG) {
            Log.d(TAG, "openNiDialog, notifyId: " + notif.notificationId + ", requestorId: " + notif.requestorId + ", text: " + notif.text);
        }
        this.mContext.startActivity(intent);
    }

    private Intent getDlgIntent(GpsNiNotification notif) {
        Intent intent = new Intent();
        String title = getDialogTitle(notif, this.mContext);
        String message = getDialogMessage(notif, this.mContext);
        intent.setFlags(268468224);
        intent.setClass(this.mContext, NetInitiatedActivity.class);
        intent.putExtra("notif_id", notif.notificationId);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra(NI_INTENT_KEY_TIMEOUT, notif.timeout);
        intent.putExtra(NI_INTENT_KEY_DEFAULT_RESPONSE, notif.defaultResponse);
        if (DEBUG) {
            Log.d(TAG, "generateIntent, title: " + title + ", message: " + message + ", timeout: " + notif.timeout);
        }
        return intent;
    }

    static byte[] stringToByteArray(String original, boolean isHex) {
        int length = original.length();
        if (isHex) {
            length /= 2;
        }
        byte[] output = new byte[length];
        if (isHex) {
            for (int i = 0; i < length; i++) {
                output[i] = (byte) Integer.parseInt(original.substring(i * 2, (i * 2) + 2), 16);
            }
        } else {
            for (int i2 = 0; i2 < length; i2++) {
                output[i2] = (byte) original.charAt(i2);
            }
        }
        return output;
    }

    static String decodeGSMPackedString(byte[] input) {
        int lengthBytes = input.length;
        int lengthSeptets = (lengthBytes * 8) / 7;
        if (lengthBytes % 7 == 0 && lengthBytes > 0 && (input[lengthBytes - 1] >> 1) == 0) {
            lengthSeptets--;
        }
        String decoded = GsmAlphabet.gsm7BitPackedToString(input, 0, lengthSeptets);
        if (decoded != null) {
            return decoded;
        }
        Log.e(TAG, "Decoding of GSM packed string failed");
        return "";
    }

    static String decodeUTF8String(byte[] input) {
        try {
            return new String(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    static String decodeUCS2String(byte[] input) {
        try {
            return new String(input, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    private static String decodeString(String original, boolean isHex, int coding) {
        if (coding == 0 || coding == -1) {
            return original;
        }
        byte[] input = stringToByteArray(original, isHex);
        if (coding == 1) {
            return decodeGSMPackedString(input);
        }
        if (coding == 2) {
            return decodeUTF8String(input);
        }
        if (coding == 3) {
            return decodeUCS2String(input);
        }
        Log.e(TAG, "Unknown encoding " + coding + " for NI text " + original);
        return original;
    }

    private static String getNotifTicker(GpsNiNotification notif, Context context) {
        return String.format(context.getString(R.string.gpsNotifTicker), decodeString(notif.requestorId, mIsHexInput, notif.requestorIdEncoding), decodeString(notif.text, mIsHexInput, notif.textEncoding));
    }

    private static String getNotifTitle(GpsNiNotification notif, Context context) {
        return String.format(context.getString(R.string.gpsNotifTitle), new Object[0]);
    }

    private static String getNotifMessage(GpsNiNotification notif, Context context) {
        return String.format(context.getString(R.string.gpsNotifMessage), decodeString(notif.requestorId, mIsHexInput, notif.requestorIdEncoding), decodeString(notif.text, mIsHexInput, notif.textEncoding));
    }

    public static String getDialogTitle(GpsNiNotification notif, Context context) {
        return getNotifTitle(notif, context);
    }

    private static String getDialogMessage(GpsNiNotification notif, Context context) {
        return getNotifMessage(notif, context);
    }
}

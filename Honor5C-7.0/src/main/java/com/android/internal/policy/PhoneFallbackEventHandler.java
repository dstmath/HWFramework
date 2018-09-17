package com.android.internal.policy;

import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.session.MediaSessionLegacyHelper;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.FallbackEventHandler;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;

public class PhoneFallbackEventHandler implements FallbackEventHandler {
    private static final boolean DEBUG = false;
    private static String TAG;
    AudioManager mAudioManager;
    Context mContext;
    KeyguardManager mKeyguardManager;
    SearchManager mSearchManager;
    TelephonyManager mTelephonyManager;
    View mView;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.PhoneFallbackEventHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.PhoneFallbackEventHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.PhoneFallbackEventHandler.<clinit>():void");
    }

    public PhoneFallbackEventHandler(Context context) {
        this.mContext = context;
    }

    public void setView(View v) {
        this.mView = v;
    }

    public void preDispatchKeyEvent(KeyEvent event) {
        getAudioManager().preDispatchKeyEvent(event, RtlSpacingHelper.UNDEFINED);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == 0) {
            return onKeyDown(keyCode, event);
        }
        return onKeyUp(keyCode, event);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean onKeyDown(int keyCode, KeyEvent event) {
        DispatcherState dispatcher = this.mView.getKeyDispatcherState();
        Intent intent;
        switch (keyCode) {
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                if (!(getKeyguardManager().inKeyguardRestrictedInputMode() || dispatcher == null)) {
                    if (event.getRepeatCount() == 0) {
                        dispatcher.startTracking(event, this);
                    } else if (event.isLongPress() && dispatcher.isTracking(event)) {
                        dispatcher.performedLongPress(event);
                        if (isUserSetupComplete()) {
                            this.mView.performHapticFeedback(0);
                            intent = new Intent("android.intent.action.VOICE_COMMAND");
                            intent.setFlags(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                            try {
                                sendCloseSystemWindows();
                                this.mContext.startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                startCallActivity();
                            }
                        } else {
                            Log.i(TAG, "Not starting call activity because user setup is in progress.");
                        }
                    }
                    return true;
                }
            case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
            case PerfHub.PERF_TAG_DEF_GPU_MIN /*25*/:
            case LogPower.AUDIO_SESSION_START /*164*/:
                MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, false);
                return true;
            case PerfHub.PERF_TAG_DEF_DDR_MIN /*27*/:
                if (!(getKeyguardManager().inKeyguardRestrictedInputMode() || dispatcher == null)) {
                    if (event.getRepeatCount() == 0) {
                        dispatcher.startTracking(event, this);
                    } else if (event.isLongPress() && dispatcher.isTracking(event)) {
                        dispatcher.performedLongPress(event);
                        if (isUserSetupComplete()) {
                            this.mView.performHapticFeedback(0);
                            sendCloseSystemWindows();
                            intent = new Intent("android.intent.action.CAMERA_BUTTON", null);
                            intent.putExtra("android.intent.extra.KEY_EVENT", event);
                            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF, null, null, null, 0, null, null);
                        } else {
                            Log.i(TAG, "Not dispatching CAMERA long press because user setup is in progress.");
                        }
                    }
                    return true;
                }
            case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
            case RILConstants.RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY /*86*/:
            case RILConstants.RIL_REQUEST_CDMA_SEND_SMS /*87*/:
            case RILConstants.RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE /*88*/:
            case RILConstants.RIL_REQUEST_GSM_GET_BROADCAST_CONFIG /*89*/:
            case StatisticalConstant.TYPE_TOUCH_FORCE_END /*90*/:
            case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
            case LogPower.END_CHG_ROTATION /*130*/:
            case MetricsEvent.DREAMING /*222*/:
                break;
            case RILConstants.RIL_REQUEST_CDMA_FLASH /*84*/:
                if (!(getKeyguardManager().inKeyguardRestrictedInputMode() || dispatcher == null)) {
                    if (event.getRepeatCount() == 0) {
                        dispatcher.startTracking(event, this);
                    } else if (event.isLongPress() && dispatcher.isTracking(event)) {
                        Configuration config = this.mContext.getResources().getConfiguration();
                        if (config.keyboard == 1 || config.hardKeyboardHidden == 2) {
                            if (isUserSetupComplete()) {
                                intent = new Intent("android.intent.action.SEARCH_LONG_PRESS");
                                intent.setFlags(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                                try {
                                    this.mView.performHapticFeedback(0);
                                    sendCloseSystemWindows();
                                    getSearchManager().stopSearch();
                                    this.mContext.startActivity(intent);
                                    dispatcher.performedLongPress(event);
                                    return true;
                                } catch (ActivityNotFoundException e2) {
                                    return false;
                                }
                            }
                            Log.i(TAG, "Not dispatching SEARCH long press because user setup is in progress.");
                        }
                    }
                }
            case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
            case LogPower.TOUCH_UP /*126*/:
            case LogPower.MIME_TYPE /*127*/:
                if (getTelephonyManager().getCallState() != 0) {
                    return true;
                }
                break;
        }
    }

    boolean onKeyUp(int keyCode, KeyEvent event) {
        DispatcherState dispatcher = this.mView.getKeyDispatcherState();
        if (dispatcher != null) {
            dispatcher.handleUpEvent(event);
        }
        switch (keyCode) {
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                if (!getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    if (event.isTracking() && !event.isCanceled()) {
                        if (isUserSetupComplete()) {
                            startCallActivity();
                        } else {
                            Log.i(TAG, "Not starting call activity because user setup is in progress.");
                        }
                    }
                    return true;
                }
                break;
            case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
            case PerfHub.PERF_TAG_DEF_GPU_MIN /*25*/:
            case LogPower.AUDIO_SESSION_START /*164*/:
                if (!event.isCanceled()) {
                    MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, false);
                }
                return true;
            case PerfHub.PERF_TAG_DEF_DDR_MIN /*27*/:
                if (!getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    return (!event.isTracking() || event.isCanceled()) ? true : true;
                }
                break;
            case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
            case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
            case RILConstants.RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY /*86*/:
            case RILConstants.RIL_REQUEST_CDMA_SEND_SMS /*87*/:
            case RILConstants.RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE /*88*/:
            case RILConstants.RIL_REQUEST_GSM_GET_BROADCAST_CONFIG /*89*/:
            case StatisticalConstant.TYPE_TOUCH_FORCE_END /*90*/:
            case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
            case LogPower.TOUCH_UP /*126*/:
            case LogPower.MIME_TYPE /*127*/:
            case LogPower.END_CHG_ROTATION /*130*/:
            case MetricsEvent.DREAMING /*222*/:
                handleMediaKeyEvent(event);
                return true;
        }
        return false;
    }

    void startCallActivity() {
        sendCloseSystemWindows();
        Intent intent = new Intent("android.intent.action.CALL_BUTTON");
        intent.setFlags(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No activity found for android.intent.action.CALL_BUTTON.");
        }
    }

    SearchManager getSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        }
        return this.mSearchManager;
    }

    TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        }
        return this.mTelephonyManager;
    }

    KeyguardManager getKeyguardManager() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        return this.mKeyguardManager;
    }

    AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        return this.mAudioManager;
    }

    void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(this.mContext, null);
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        MediaSessionLegacyHelper.getHelper(this.mContext).sendMediaButtonEvent(keyEvent, false);
    }

    private boolean isUserSetupComplete() {
        return Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) != 0;
    }
}

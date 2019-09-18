package com.android.internal.policy;

import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.session.MediaSessionManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.FallbackEventHandler;
import android.view.KeyEvent;
import android.view.View;
import com.android.internal.os.PowerProfile;
import com.android.internal.telephony.PhoneConstants;

public class PhoneFallbackEventHandler implements FallbackEventHandler {
    private static final boolean DEBUG = false;
    private static String TAG = "PhoneFallbackEventHandler";
    AudioManager mAudioManager;
    Context mContext;
    KeyguardManager mKeyguardManager;
    MediaSessionManager mMediaSessionManager;
    SearchManager mSearchManager;
    TelephonyManager mTelephonyManager;
    View mView;

    public PhoneFallbackEventHandler(Context context) {
        this.mContext = context;
    }

    public void setView(View v) {
        this.mView = v;
    }

    public void preDispatchKeyEvent(KeyEvent event) {
        getAudioManager().preDispatchKeyEvent(event, Integer.MIN_VALUE);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == 0) {
            return onKeyDown(keyCode, event);
        }
        return onKeyUp(keyCode, event);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003a, code lost:
        if (getTelephonyManager().getCallState() == 0) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003c, code lost:
        return true;
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int i = keyCode;
        KeyEvent keyEvent = event;
        KeyEvent.DispatcherState dispatcher = this.mView.getKeyDispatcherState();
        if (i != 5) {
            if (i != 27) {
                if (!(i == 79 || i == 130)) {
                    if (i != 164) {
                        if (i != 222) {
                            switch (i) {
                                case 24:
                                case 25:
                                    break;
                                default:
                                    switch (i) {
                                        case 84:
                                            if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
                                                if (event.getRepeatCount() != 0) {
                                                    if (event.isLongPress() && dispatcher.isTracking(keyEvent)) {
                                                        Configuration config = this.mContext.getResources().getConfiguration();
                                                        if (config.keyboard == 1 || config.hardKeyboardHidden == 2) {
                                                            if (!isUserSetupComplete()) {
                                                                Log.i(TAG, "Not dispatching SEARCH long press because user setup is in progress.");
                                                                break;
                                                            } else {
                                                                Intent intent = new Intent("android.intent.action.SEARCH_LONG_PRESS");
                                                                intent.setFlags(268435456);
                                                                try {
                                                                    this.mView.performHapticFeedback(0);
                                                                    sendCloseSystemWindows();
                                                                    getSearchManager().stopSearch();
                                                                    this.mContext.startActivity(intent);
                                                                    dispatcher.performedLongPress(keyEvent);
                                                                    return true;
                                                                } catch (ActivityNotFoundException e) {
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    dispatcher.startTracking(keyEvent, this);
                                                    break;
                                                }
                                            }
                                            break;
                                        case 85:
                                            break;
                                        case 86:
                                        case 87:
                                        case 88:
                                        case 89:
                                        case 90:
                                        case 91:
                                            break;
                                        default:
                                            switch (i) {
                                                case 126:
                                                case 127:
                                                    break;
                                            }
                                    }
                            }
                        }
                    }
                    handleVolumeKeyEvent(keyEvent);
                    return true;
                }
                handleMediaKeyEvent(keyEvent);
                return true;
            } else if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
                if (event.getRepeatCount() == 0) {
                    dispatcher.startTracking(keyEvent, this);
                } else if (event.isLongPress() && dispatcher.isTracking(keyEvent)) {
                    dispatcher.performedLongPress(keyEvent);
                    if (isUserSetupComplete()) {
                        this.mView.performHapticFeedback(0);
                        sendCloseSystemWindows();
                        Intent intent2 = new Intent("android.intent.action.CAMERA_BUTTON", null);
                        intent2.addFlags(268435456);
                        intent2.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                        this.mContext.sendOrderedBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF, null, null, null, 0, null, null);
                    } else {
                        Log.i(TAG, "Not dispatching CAMERA long press because user setup is in progress.");
                    }
                }
                return true;
            }
        } else if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
            if (event.getRepeatCount() == 0) {
                dispatcher.startTracking(keyEvent, this);
            } else if (event.isLongPress() && dispatcher.isTracking(keyEvent)) {
                dispatcher.performedLongPress(keyEvent);
                if (isUserSetupComplete()) {
                    this.mView.performHapticFeedback(0);
                    Intent intent3 = new Intent("android.intent.action.VOICE_COMMAND");
                    intent3.setFlags(268435456);
                    try {
                        sendCloseSystemWindows();
                        this.mContext.startActivity(intent3);
                    } catch (ActivityNotFoundException e2) {
                        startCallActivity();
                    }
                } else {
                    Log.i(TAG, "Not starting call activity because user setup is in progress.");
                }
            }
            return true;
        }
        return false;
    }

    private boolean isNotInstantAppAndKeyguardRestricted(KeyEvent.DispatcherState dispatcher) {
        return !this.mContext.getPackageManager().isInstantApp() && (getKeyguardManager().inKeyguardRestrictedInputMode() || dispatcher == null);
    }

    /* access modifiers changed from: package-private */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        KeyEvent.DispatcherState dispatcher = this.mView.getKeyDispatcherState();
        if (dispatcher != null) {
            dispatcher.handleUpEvent(event);
        }
        if (keyCode != 5) {
            if (keyCode != 27) {
                if (!(keyCode == 79 || keyCode == 130)) {
                    if (keyCode != 164) {
                        if (keyCode != 222) {
                            switch (keyCode) {
                                case 24:
                                case 25:
                                    break;
                                default:
                                    switch (keyCode) {
                                        case 85:
                                        case 86:
                                        case 87:
                                        case 88:
                                        case 89:
                                        case 90:
                                        case 91:
                                            break;
                                        default:
                                            switch (keyCode) {
                                                case 126:
                                                case 127:
                                                    break;
                                            }
                                    }
                            }
                        }
                    }
                    if (!event.isCanceled()) {
                        handleVolumeKeyEvent(event);
                    }
                    return true;
                }
                handleMediaKeyEvent(event);
                return true;
            } else if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
                if (event.isTracking()) {
                    event.isCanceled();
                }
                return true;
            }
        } else if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
            if (event.isTracking() && !event.isCanceled()) {
                if (isUserSetupComplete()) {
                    startCallActivity();
                } else {
                    Log.i(TAG, "Not starting call activity because user setup is in progress.");
                }
            }
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void startCallActivity() {
        sendCloseSystemWindows();
        Intent intent = new Intent("android.intent.action.CALL_BUTTON");
        intent.setFlags(268435456);
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No activity found for android.intent.action.CALL_BUTTON.");
        }
    }

    /* access modifiers changed from: package-private */
    public SearchManager getSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        }
        return this.mSearchManager;
    }

    /* access modifiers changed from: package-private */
    public TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        }
        return this.mTelephonyManager;
    }

    /* access modifiers changed from: package-private */
    public KeyguardManager getKeyguardManager() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        return this.mKeyguardManager;
    }

    /* access modifiers changed from: package-private */
    public AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService(PowerProfile.POWER_AUDIO);
        }
        return this.mAudioManager;
    }

    /* access modifiers changed from: package-private */
    public MediaSessionManager getMediaSessionManager() {
        if (this.mMediaSessionManager == null) {
            this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        }
        return this.mMediaSessionManager;
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(this.mContext, null);
    }

    private void handleVolumeKeyEvent(KeyEvent keyEvent) {
        getMediaSessionManager().dispatchVolumeKeyEventAsSystemService(keyEvent, Integer.MIN_VALUE);
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        getMediaSessionManager().dispatchMediaKeyEventAsSystemService(keyEvent);
    }

    private boolean isUserSetupComplete() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) != 0;
    }
}

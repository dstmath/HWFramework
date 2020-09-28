package com.android.internal.policy;

import android.annotation.UnsupportedAppUsage;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.FallbackEventHandler;
import android.view.KeyEvent;
import android.view.View;

public class PhoneFallbackEventHandler implements FallbackEventHandler {
    private static final String CONTACTS_CONF_KEY = "ro.packagename.contacts";
    private static final String CONTACTS_DEFAULT_PACKAGE_NAME = "com.android.contacts";
    private static final String CONTACTS_PACKAGENAME = SystemProperties.get(CONTACTS_CONF_KEY, "com.android.contacts");
    private static final boolean DEBUG = false;
    private static final String HICAR_CONTACTS_CLASS_NAME = "com.huawei.carcontact.dial.AutoDialerActivity";
    private static final String HICAR_SYSTEM_MODE_KEY = "ro.config.hw_hicar_mode";
    private static final boolean IS_HICAR_MODE = SystemProperties.getBoolean(HICAR_SYSTEM_MODE_KEY, false);
    private static String TAG = "PhoneFallbackEventHandler";
    AudioManager mAudioManager;
    @UnsupportedAppUsage
    Context mContext;
    KeyguardManager mKeyguardManager;
    MediaSessionManager mMediaSessionManager;
    SearchManager mSearchManager;
    TelephonyManager mTelephonyManager;
    @UnsupportedAppUsage
    View mView;

    @UnsupportedAppUsage
    public PhoneFallbackEventHandler(Context context) {
        this.mContext = context;
    }

    @Override // android.view.FallbackEventHandler
    public void setView(View v) {
        this.mView = v;
    }

    @Override // android.view.FallbackEventHandler
    public void preDispatchKeyEvent(KeyEvent event) {
        getAudioManager().preDispatchKeyEvent(event, Integer.MIN_VALUE);
    }

    @Override // android.view.FallbackEventHandler
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == 0) {
            return onKeyDown(keyCode, event);
        }
        return onKeyUp(keyCode, event);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        KeyEvent.DispatcherState dispatcher = this.mView.getKeyDispatcherState();
        if (keyCode != 5) {
            if (keyCode != 27) {
                if (!(keyCode == 79 || keyCode == 130)) {
                    if (keyCode != 164) {
                        if (keyCode != 222) {
                            if (!(keyCode == 24 || keyCode == 25)) {
                                if (!(keyCode == 126 || keyCode == 127)) {
                                    switch (keyCode) {
                                        case 84:
                                            if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
                                                if (event.getRepeatCount() != 0) {
                                                    if (event.isLongPress() && dispatcher.isTracking(event)) {
                                                        Configuration config = this.mContext.getResources().getConfiguration();
                                                        if (config.keyboard == 1 || config.hardKeyboardHidden == 2) {
                                                            if (!isUserSetupComplete()) {
                                                                Log.i(TAG, "Not dispatching SEARCH long press because user setup is in progress.");
                                                                break;
                                                            } else {
                                                                Intent intent = new Intent(Intent.ACTION_SEARCH_LONG_PRESS);
                                                                intent.setFlags(268435456);
                                                                try {
                                                                    this.mView.performHapticFeedback(0);
                                                                    sendCloseSystemWindows();
                                                                    getSearchManager().stopSearch();
                                                                    this.mContext.startActivity(intent);
                                                                    dispatcher.performedLongPress(event);
                                                                    return true;
                                                                } catch (ActivityNotFoundException e) {
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    dispatcher.startTracking(event, this);
                                                    break;
                                                }
                                            }
                                            break;
                                    }
                                }
                                if (getTelephonyManager().getCallState() != 0) {
                                    return true;
                                }
                            }
                        }
                    }
                    handleVolumeKeyEvent(event);
                    return true;
                }
                handleMediaKeyEvent(event);
                return true;
            } else if (!isNotInstantAppAndKeyguardRestricted(dispatcher)) {
                if (event.getRepeatCount() == 0) {
                    dispatcher.startTracking(event, this);
                } else if (event.isLongPress() && dispatcher.isTracking(event)) {
                    dispatcher.performedLongPress(event);
                    if (isUserSetupComplete()) {
                        this.mView.performHapticFeedback(0);
                        sendCloseSystemWindows();
                        Intent intent2 = new Intent(Intent.ACTION_CAMERA_BUTTON, (Uri) null);
                        intent2.addFlags(268435456);
                        intent2.putExtra(Intent.EXTRA_KEY_EVENT, event);
                        this.mContext.sendOrderedBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF, null, null, null, 0, null, null);
                    } else {
                        Log.i(TAG, "Not dispatching CAMERA long press because user setup is in progress.");
                    }
                }
                return true;
            }
        } else if (!isNotInstantAppAndKeyguardRestrictedAndIsNotHiCarMode(dispatcher)) {
            if (event.getRepeatCount() == 0) {
                dispatcher.startTracking(event, this);
            } else if (event.isLongPress() && dispatcher.isTracking(event)) {
                dispatcher.performedLongPress(event);
                if (isUserSetupComplete()) {
                    this.mView.performHapticFeedback(0);
                    Intent intent3 = new Intent(Intent.ACTION_VOICE_COMMAND);
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

    private boolean isHiCarKeyEvent() {
        if (!IS_HICAR_MODE || !HwPCUtils.isValidExtDisplayId(this.mContext)) {
            return false;
        }
        return HwPCUtils.isHiCarCastModeForClient();
    }

    private boolean isNotInstantAppAndKeyguardRestrictedAndIsNotHiCarMode(KeyEvent.DispatcherState dispatcher) {
        return !this.mContext.getPackageManager().isInstantApp() && ((getKeyguardManager().inKeyguardRestrictedInputMode() && !isHiCarKeyEvent()) || dispatcher == null);
    }

    private boolean isNotInstantAppAndKeyguardRestricted(KeyEvent.DispatcherState dispatcher) {
        return !this.mContext.getPackageManager().isInstantApp() && (getKeyguardManager().inKeyguardRestrictedInputMode() || dispatcher == null);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
                            if (!(keyCode == 24 || keyCode == 25)) {
                                if (!(keyCode == 126 || keyCode == 127)) {
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
                                            return false;
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
            } else if (isNotInstantAppAndKeyguardRestricted(dispatcher)) {
                return false;
            } else {
                if (event.isTracking()) {
                    event.isCanceled();
                }
                return true;
            }
        } else if (isNotInstantAppAndKeyguardRestrictedAndIsNotHiCarMode(dispatcher)) {
            return false;
        } else {
            if (event.isTracking() && !event.isCanceled()) {
                if (isUserSetupComplete()) {
                    startCallActivity();
                } else {
                    Log.i(TAG, "Not starting call activity because user setup is in progress.");
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void startCallActivity() {
        sendCloseSystemWindows();
        Intent intent = new Intent();
        if (isHiCarKeyEvent()) {
            Log.i(TAG, "now is hicar key event");
            intent.setFlags(270532608);
            intent.setComponent(new ComponentName(CONTACTS_PACKAGENAME, HICAR_CONTACTS_CLASS_NAME));
        } else {
            intent.setAction(Intent.ACTION_CALL_BUTTON);
            intent.setFlags(268435456);
        }
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
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        return this.mTelephonyManager;
    }

    /* access modifiers changed from: package-private */
    public KeyguardManager getKeyguardManager() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(Context.KEYGUARD_SERVICE);
        }
        return this.mKeyguardManager;
    }

    /* access modifiers changed from: package-private */
    public AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        return this.mAudioManager;
    }

    /* access modifiers changed from: package-private */
    public MediaSessionManager getMediaSessionManager() {
        if (this.mMediaSessionManager == null) {
            this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService(Context.MEDIA_SESSION_SERVICE);
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
        return Settings.Secure.getInt(this.mContext.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }
}

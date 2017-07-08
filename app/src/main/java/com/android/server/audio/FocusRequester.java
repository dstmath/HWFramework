package com.android.server.audio;

import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.vr.EnabledComponentsObserver;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

public class FocusRequester {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaFocusControl";
    private final AudioAttributes mAttributes;
    private final int mCallingUid;
    private final String mClientId;
    private AudioFocusDeathHandler mDeathHandler;
    private final MediaFocusControl mFocusController;
    private final IAudioFocusDispatcher mFocusDispatcher;
    private final int mFocusGainRequest;
    private int mFocusLossReceived;
    private final int mGrantFlags;
    private final String mPackageName;
    private final IBinder mSourceRef;

    FocusRequester(AudioAttributes aa, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr) {
        this.mAttributes = aa;
        this.mFocusDispatcher = afl;
        this.mSourceRef = source;
        this.mClientId = id;
        this.mDeathHandler = hdlr;
        this.mPackageName = pn;
        this.mCallingUid = uid;
        this.mFocusGainRequest = focusRequest;
        this.mGrantFlags = grantFlags;
        this.mFocusLossReceived = 0;
        this.mFocusController = ctlr;
    }

    boolean hasSameClient(String otherClient) {
        boolean z = DEBUG;
        try {
            if (this.mClientId.compareTo(otherClient) == 0) {
                z = true;
            }
            return z;
        } catch (NullPointerException e) {
            return DEBUG;
        }
    }

    boolean isLockedFocusOwner() {
        return (this.mGrantFlags & 4) != 0 ? true : DEBUG;
    }

    boolean hasSameBinder(IBinder ib) {
        return this.mSourceRef != null ? this.mSourceRef.equals(ib) : DEBUG;
    }

    boolean hasSamePackage(String pack) {
        boolean z = DEBUG;
        try {
            if (this.mPackageName.compareTo(pack) == 0) {
                z = true;
            }
            return z;
        } catch (NullPointerException e) {
            return DEBUG;
        }
    }

    boolean hasSameUid(int uid) {
        return this.mCallingUid == uid ? true : DEBUG;
    }

    String getClientId() {
        return this.mClientId;
    }

    int getGainRequest() {
        return this.mFocusGainRequest;
    }

    int getGrantFlags() {
        return this.mGrantFlags;
    }

    AudioAttributes getAudioAttributes() {
        return this.mAttributes;
    }

    private static String focusChangeToString(int focus) {
        switch (focus) {
            case WindowManagerService.COMPAT_MODE_MATCH_PARENT /*-3*/:
                return "LOSS_TRANSIENT_CAN_DUCK";
            case EnabledComponentsObserver.NOT_INSTALLED /*-2*/:
                return "LOSS_TRANSIENT";
            case AppTransition.TRANSIT_UNSET /*-1*/:
                return "LOSS";
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "none";
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "GAIN";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "GAIN_TRANSIENT";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "GAIN_TRANSIENT_MAY_DUCK";
            case H.DO_TRAVERSAL /*4*/:
                return "GAIN_TRANSIENT_EXCLUSIVE";
            default:
                return "[invalid focus change" + focus + "]";
        }
    }

    private String focusGainToString() {
        return focusChangeToString(this.mFocusGainRequest);
    }

    private String focusLossToString() {
        return focusChangeToString(this.mFocusLossReceived);
    }

    private static String flagsToString(int flags) {
        String msg = new String();
        if ((flags & 1) != 0) {
            msg = msg + "DELAY_OK";
        }
        if ((flags & 4) != 0) {
            if (!msg.isEmpty()) {
                msg = msg + "|";
            }
            msg = msg + "LOCK";
        }
        if ((flags & 2) == 0) {
            return msg;
        }
        if (!msg.isEmpty()) {
            msg = msg + "|";
        }
        return msg + "PAUSES_ON_DUCKABLE_LOSS";
    }

    void dump(PrintWriter pw) {
        pw.println("  source:" + this.mSourceRef + " -- pack: " + this.mPackageName + " -- client: " + this.mClientId + " -- gain: " + focusGainToString() + " -- flags: " + flagsToString(this.mGrantFlags) + " -- loss: " + focusLossToString() + " -- uid: " + this.mCallingUid + " -- attr: " + this.mAttributes);
    }

    void release() {
        try {
            if (this.mSourceRef != null && this.mDeathHandler != null) {
                this.mSourceRef.unlinkToDeath(this.mDeathHandler, 0);
                this.mDeathHandler = null;
            }
        } catch (NoSuchElementException e) {
            Log.e(TAG, "FocusRequester.release() hit ", e);
        }
    }

    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int focusLossForGainRequest(int gainRequest) {
        switch (gainRequest) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                switch (this.mFocusLossReceived) {
                    case WindowManagerService.COMPAT_MODE_MATCH_PARENT /*-3*/:
                    case EnabledComponentsObserver.NOT_INSTALLED /*-2*/:
                    case AppTransition.TRANSIT_UNSET /*-1*/:
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                        return -1;
                }
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
            case H.DO_TRAVERSAL /*4*/:
                break;
            case H.REPORT_LOSING_FOCUS /*3*/:
                break;
        }
    }

    void handleExternalFocusGain(int focusGain) {
        handleFocusLoss(focusLossForGainRequest(focusGain));
    }

    void handleFocusGain(int focusGain) {
        try {
            this.mFocusLossReceived = 0;
            this.mFocusController.notifyExtPolicyFocusGrant_syncAf(toAudioFocusInfo(), 1);
            if (this.mFocusDispatcher != null) {
                this.mFocusDispatcher.dispatchAudioFocusChange(focusGain, this.mClientId);
                LogPower.push(147, this.mPackageName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failure to signal gain of audio focus due to: ", e);
        }
    }

    void handleFocusLoss(int focusLoss) {
        try {
            if (focusLoss != this.mFocusLossReceived) {
                this.mFocusLossReceived = focusLoss;
                if (!this.mFocusController.mustNotifyFocusOwnerOnDuck() && this.mFocusLossReceived == -3 && (this.mGrantFlags & 2) == 0) {
                    this.mFocusController.notifyExtPolicyFocusLoss_syncAf(toAudioFocusInfo(), DEBUG);
                } else if (this.mFocusDispatcher != null) {
                    this.mFocusController.notifyExtPolicyFocusLoss_syncAf(toAudioFocusInfo(), true);
                    this.mFocusDispatcher.dispatchAudioFocusChange(this.mFocusLossReceived, this.mClientId);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failure to signal loss of audio focus due to:", e);
        }
    }

    AudioFocusInfo toAudioFocusInfo() {
        return new AudioFocusInfo(this.mAttributes, this.mClientId, this.mPackageName, this.mFocusGainRequest, this.mFocusLossReceived, this.mGrantFlags);
    }
}

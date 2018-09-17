package com.android.server.audio;

import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
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
    private IAudioFocusDispatcher mFocusDispatcher;
    private final int mFocusGainRequest;
    private int mFocusLossReceived = 0;
    private boolean mFocusLossWasNotified = true;
    private final int mGrantFlags;
    private final String mPackageName;
    private final int mSdkTarget;
    private final IBinder mSourceRef;

    FocusRequester(AudioAttributes aa, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk) {
        this.mAttributes = aa;
        this.mFocusDispatcher = afl;
        this.mSourceRef = source;
        this.mClientId = id;
        this.mDeathHandler = hdlr;
        this.mPackageName = pn;
        this.mCallingUid = uid;
        this.mFocusGainRequest = focusRequest;
        this.mGrantFlags = grantFlags;
        this.mFocusController = ctlr;
        this.mSdkTarget = sdk;
    }

    FocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, AudioFocusDeathHandler hdlr, MediaFocusControl ctlr) {
        this.mAttributes = afi.getAttributes();
        this.mClientId = afi.getClientId();
        this.mPackageName = afi.getPackageName();
        this.mCallingUid = afi.getClientUid();
        this.mFocusGainRequest = afi.getGainRequest();
        this.mGrantFlags = afi.getFlags();
        this.mSdkTarget = afi.getSdkTarget();
        this.mFocusDispatcher = afl;
        this.mSourceRef = source;
        this.mDeathHandler = hdlr;
        this.mFocusController = ctlr;
    }

    boolean hasSameClient(String otherClient) {
        boolean z = false;
        try {
            if (this.mClientId.compareTo(otherClient) == 0) {
                z = true;
            }
            return z;
        } catch (NullPointerException e) {
            return false;
        }
    }

    boolean isLockedFocusOwner() {
        return (this.mGrantFlags & 4) != 0;
    }

    boolean hasSameBinder(IBinder ib) {
        return this.mSourceRef != null ? this.mSourceRef.equals(ib) : false;
    }

    boolean hasSameDispatcher(IAudioFocusDispatcher fd) {
        return this.mFocusDispatcher != null ? this.mFocusDispatcher.equals(fd) : false;
    }

    boolean hasSamePackage(String pack) {
        boolean z = false;
        try {
            if (this.mPackageName.compareTo(pack) == 0) {
                z = true;
            }
            return z;
        } catch (NullPointerException e) {
            return false;
        }
    }

    boolean hasSameUid(int uid) {
        return this.mCallingUid == uid;
    }

    int getClientUid() {
        return this.mCallingUid;
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

    int getSdkTarget() {
        return this.mSdkTarget;
    }

    private static String focusChangeToString(int focus) {
        switch (focus) {
            case -3:
                return "LOSS_TRANSIENT_CAN_DUCK";
            case -2:
                return "LOSS_TRANSIENT";
            case -1:
                return "LOSS";
            case 0:
                return "none";
            case 1:
                return "GAIN";
            case 2:
                return "GAIN_TRANSIENT";
            case 3:
                return "GAIN_TRANSIENT_MAY_DUCK";
            case 4:
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
        pw.println("  source:" + this.mSourceRef + " -- pack: " + this.mPackageName + " -- client: " + this.mClientId + " -- gain: " + focusGainToString() + " -- flags: " + flagsToString(this.mGrantFlags) + " -- loss: " + focusLossToString() + " -- notified: " + this.mFocusLossWasNotified + " -- uid: " + this.mCallingUid + " -- attr: " + this.mAttributes + " -- sdk:" + this.mSdkTarget);
    }

    void release() {
        try {
            if (this.mSourceRef != null && this.mDeathHandler != null) {
                this.mSourceRef.unlinkToDeath(this.mDeathHandler, 0);
                this.mDeathHandler = null;
                this.mFocusDispatcher = null;
            }
        } catch (NoSuchElementException e) {
            Log.e(TAG, "FocusRequester.release() hit ", e);
        }
    }

    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /* JADX WARNING: Missing block: B:2:0x0005, code:
            android.util.Log.e(TAG, "focusLossForGainRequest() for invalid focus request " + r4);
     */
    /* JADX WARNING: Missing block: B:3:0x0020, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:7:0x0028, code:
            switch(r3.mFocusLossReceived) {
                case -3: goto L_0x0034;
                case -2: goto L_0x0034;
                case -1: goto L_0x0035;
                case 0: goto L_0x0034;
                default: goto L_0x002b;
            };
     */
    /* JADX WARNING: Missing block: B:9:0x002d, code:
            switch(r3.mFocusLossReceived) {
                case -3: goto L_0x0031;
                case -2: goto L_0x0036;
                case -1: goto L_0x0037;
                case 0: goto L_0x0031;
                default: goto L_0x0030;
            };
     */
    /* JADX WARNING: Missing block: B:11:0x0032, code:
            return -3;
     */
    /* JADX WARNING: Missing block: B:13:0x0034, code:
            return -2;
     */
    /* JADX WARNING: Missing block: B:14:0x0035, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:15:0x0036, code:
            return -2;
     */
    /* JADX WARNING: Missing block: B:16:0x0037, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int focusLossForGainRequest(int gainRequest) {
        switch (gainRequest) {
            case 1:
                switch (this.mFocusLossReceived) {
                    case -3:
                    case -2:
                    case -1:
                    case 0:
                        return -1;
                }
                break;
            case 2:
            case 4:
                break;
            case 3:
                break;
        }
    }

    void handleExternalFocusGain(int focusGain, FocusRequester fr) {
        handleFocusLoss(focusLossForGainRequest(focusGain), fr);
    }

    void handleFocusGain(int focusGain) {
        try {
            this.mFocusLossReceived = 0;
            this.mFocusController.notifyExtPolicyFocusGrant_syncAf(toAudioFocusInfo(), 1);
            IAudioFocusDispatcher fd = this.mFocusDispatcher;
            if (fd != null && this.mFocusLossWasNotified) {
                fd.dispatchAudioFocusChange(focusGain, this.mClientId);
                LogPower.push(147, this.mPackageName);
            }
            this.mFocusController.unduckPlayers(this);
        } catch (RemoteException e) {
            Log.e(TAG, "Failure to signal gain of audio focus due to: ", e);
        }
    }

    void handleFocusGainFromRequest(int focusRequestResult) {
        if (focusRequestResult == 1) {
            this.mFocusController.unduckPlayers(this);
        }
    }

    void handleFocusLoss(int focusLoss, FocusRequester fr) {
        try {
            if (focusLoss != this.mFocusLossReceived) {
                this.mFocusLossReceived = focusLoss;
                this.mFocusLossWasNotified = false;
                if (!this.mFocusController.mustNotifyFocusOwnerOnDuck() && this.mFocusLossReceived == -3 && (this.mGrantFlags & 2) == 0) {
                    this.mFocusController.notifyExtPolicyFocusLoss_syncAf(toAudioFocusInfo(), false);
                    return;
                }
                boolean handled = false;
                if (!(focusLoss != -3 || fr == null || fr.mCallingUid == this.mCallingUid)) {
                    if ((this.mGrantFlags & 2) != 0) {
                        handled = false;
                        Log.v(TAG, "not ducking uid " + this.mCallingUid + " - flags");
                    } else if (getSdkTarget() <= 25) {
                        handled = false;
                        Log.v(TAG, "not ducking uid " + this.mCallingUid + " - old SDK");
                    } else {
                        handled = this.mFocusController.duckPlayers(fr, this);
                    }
                }
                if (handled) {
                    this.mFocusController.notifyExtPolicyFocusLoss_syncAf(toAudioFocusInfo(), false);
                    return;
                }
                IAudioFocusDispatcher fd = this.mFocusDispatcher;
                if (fd != null) {
                    this.mFocusController.notifyExtPolicyFocusLoss_syncAf(toAudioFocusInfo(), true);
                    this.mFocusLossWasNotified = true;
                    fd.dispatchAudioFocusChange(this.mFocusLossReceived, this.mClientId);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failure to signal loss of audio focus due to:", e);
        }
    }

    int dispatchFocusChange(int focusChange) {
        if (this.mFocusDispatcher == null || focusChange == 0) {
            return 0;
        }
        if ((focusChange == 3 || focusChange == 4 || focusChange == 2 || focusChange == 1) && this.mFocusGainRequest != focusChange) {
            Log.w(TAG, "focus gain was requested with " + this.mFocusGainRequest + ", dispatching " + focusChange);
        } else if (focusChange == -3 || focusChange == -2 || focusChange == -1) {
            this.mFocusLossReceived = focusChange;
        }
        try {
            this.mFocusDispatcher.dispatchAudioFocusChange(focusChange, this.mClientId);
            return 1;
        } catch (RemoteException e) {
            Log.v(TAG, "dispatchFocusChange: error talking to focus listener", e);
            return 0;
        }
    }

    AudioFocusInfo toAudioFocusInfo() {
        return new AudioFocusInfo(this.mAttributes, this.mCallingUid, this.mClientId, this.mPackageName, this.mFocusGainRequest, this.mFocusLossReceived, this.mGrantFlags, this.mSdkTarget);
    }
}

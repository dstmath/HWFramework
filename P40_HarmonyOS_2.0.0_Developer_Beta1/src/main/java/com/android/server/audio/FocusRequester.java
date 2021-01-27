package com.android.server.audio;

import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.audio.MediaFocusControl;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

public class FocusRequester {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaFocusControl";
    private final AudioAttributes mAttributes;
    private final int mCallingUid;
    private final String mClientId;
    private MediaFocusControl.AudioFocusDeathHandler mDeathHandler;
    private final MediaFocusControl mFocusController;
    private IAudioFocusDispatcher mFocusDispatcher;
    private final int mFocusGainRequest;
    private int mFocusLossReceived;
    private boolean mFocusLossWasNotified;
    private final int mGrantFlags;
    protected boolean mIsInExternal = false;
    protected final String mPackageName;
    private final int mSdkTarget;
    private final IBinder mSourceRef;

    public boolean getIsInExternal() {
        return this.mIsInExternal;
    }

    public void setIsInExternal(boolean isInExternal) {
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public FocusRequester(AudioAttributes aa, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk) {
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
        this.mFocusLossWasNotified = true;
        this.mFocusController = ctlr;
        this.mSdkTarget = sdk;
    }

    public FocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, MediaFocusControl.AudioFocusDeathHandler hdlr, MediaFocusControl ctlr) {
        this.mAttributes = afi.getAttributes();
        this.mClientId = afi.getClientId();
        this.mPackageName = afi.getPackageName();
        this.mCallingUid = afi.getClientUid();
        this.mFocusGainRequest = afi.getGainRequest();
        this.mFocusLossReceived = 0;
        this.mFocusLossWasNotified = true;
        this.mGrantFlags = afi.getFlags();
        this.mSdkTarget = afi.getSdkTarget();
        this.mFocusDispatcher = afl;
        this.mSourceRef = source;
        this.mDeathHandler = hdlr;
        this.mFocusController = ctlr;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameClient(String otherClient) {
        return this.mClientId.compareTo(otherClient) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isLockedFocusOwner() {
        return (this.mGrantFlags & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameBinder(IBinder ib) {
        IBinder iBinder = this.mSourceRef;
        return iBinder != null && iBinder.equals(ib);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameDispatcher(IAudioFocusDispatcher fd) {
        IAudioFocusDispatcher iAudioFocusDispatcher = this.mFocusDispatcher;
        return iAudioFocusDispatcher != null && iAudioFocusDispatcher.equals(fd);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSamePackage(String pack) {
        return this.mPackageName.compareTo(pack) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameUid(int uid) {
        return this.mCallingUid == uid;
    }

    /* access modifiers changed from: package-private */
    public int getClientUid() {
        return this.mCallingUid;
    }

    /* access modifiers changed from: package-private */
    public String getClientId() {
        return this.mClientId;
    }

    /* access modifiers changed from: package-private */
    public int getGainRequest() {
        return this.mFocusGainRequest;
    }

    /* access modifiers changed from: package-private */
    public int getGrantFlags() {
        return this.mGrantFlags;
    }

    /* access modifiers changed from: package-private */
    public AudioAttributes getAudioAttributes() {
        return this.mAttributes;
    }

    /* access modifiers changed from: package-private */
    public int getSdkTarget() {
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

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw) {
        pw.println("  source:" + this.mSourceRef + " -- pack: " + this.mPackageName + " -- client: " + this.mClientId + " -- gain: " + focusGainToString() + " -- flags: " + flagsToString(this.mGrantFlags) + " -- loss: " + focusLossToString() + " -- notified: " + this.mFocusLossWasNotified + " -- uid: " + this.mCallingUid + " -- attr: " + this.mAttributes + " -- sdk:" + this.mSdkTarget);
    }

    /* access modifiers changed from: package-private */
    public void release() {
        IBinder srcRef = this.mSourceRef;
        MediaFocusControl.AudioFocusDeathHandler deathHdlr = this.mDeathHandler;
        if (!(srcRef == null || deathHdlr == null)) {
            try {
                srcRef.unlinkToDeath(deathHdlr, 0);
            } catch (NoSuchElementException e) {
            }
        }
        this.mDeathHandler = null;
        this.mFocusDispatcher = null;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        if (r0 != 0) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002c, code lost:
        if (r0 != 0) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002e, code lost:
        android.util.Log.e(com.android.server.audio.FocusRequester.TAG, "focusLossForGainRequest() for invalid focus request " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000d, code lost:
        if (r5 != 4) goto L_0x002e;
     */
    private int focusLossForGainRequest(int gainRequest) {
        if (gainRequest == 1) {
            int i = this.mFocusLossReceived;
            if (i == -3 || i == -2 || i == -1 || i == 0) {
                return -1;
            }
        } else if (gainRequest != 2) {
            if (gainRequest != 3) {
            }
            int i2 = this.mFocusLossReceived;
            if (i2 != -3) {
                if (i2 == -2) {
                    return -2;
                }
                if (i2 == -1) {
                    return -1;
                }
            }
            return -3;
        }
        int i3 = this.mFocusLossReceived;
        if (!(i3 == -3 || i3 == -2)) {
            if (i3 == -1) {
                return -1;
            }
        }
        return -2;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"MediaFocusControl.mAudioFocusLock"})
    public boolean handleFocusLossFromGain(int focusGain, FocusRequester frWinner, boolean forceDuck) {
        int focusLoss = focusLossForGainRequest(focusGain);
        handleFocusLoss(focusLoss, frWinner, forceDuck);
        return focusLoss == -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"MediaFocusControl.mAudioFocusLock"})
    public void handleFocusGain(int focusGain) {
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

    /* access modifiers changed from: package-private */
    @GuardedBy({"MediaFocusControl.mAudioFocusLock"})
    public void handleFocusGainFromRequest(int focusRequestResult) {
        if (focusRequestResult == 1) {
            this.mFocusController.unduckPlayers(this);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"MediaFocusControl.mAudioFocusLock"})
    public void handleFocusLoss(int focusLoss, FocusRequester frWinner, boolean forceDuck) {
        try {
            if (focusLoss != this.mFocusLossReceived || this.mFocusController.isInDesktopMode()) {
                this.mFocusLossReceived = focusLoss;
                this.mFocusLossWasNotified = false;
                if (!this.mFocusController.mustNotifyFocusOwnerOnDuck() && this.mFocusLossReceived == -3 && (this.mGrantFlags & 2) == 0) {
                    this.mFocusController.notifyExtPolicyFocusLoss_syncAf(toAudioFocusInfo(), false);
                    return;
                }
                boolean handled = false;
                if (!(focusLoss != -3 || frWinner == null || frWinner.mCallingUid == this.mCallingUid)) {
                    if (!forceDuck && (this.mGrantFlags & 2) != 0) {
                        handled = false;
                        Log.i(TAG, "not ducking uid " + this.mCallingUid + " - flags");
                    } else if (forceDuck || getSdkTarget() > 25) {
                        handled = this.mFocusController.duckPlayers(frWinner, this, forceDuck);
                    } else {
                        handled = false;
                        Log.i(TAG, "not ducking uid " + this.mCallingUid + " - old SDK");
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

    /* access modifiers changed from: package-private */
    public int dispatchFocusChange(int focusChange) {
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
            Log.e(TAG, "dispatchFocusChange: error talking to focus listener " + this.mClientId, e);
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchFocusResultFromExtPolicy(int requestResult) {
        try {
            this.mFocusDispatcher.dispatchFocusResultFromExtPolicy(requestResult, this.mClientId);
        } catch (RemoteException e) {
            Log.e(TAG, "dispatchFocusResultFromExtPolicy: error talking to focus listener" + this.mClientId, e);
        }
    }

    /* access modifiers changed from: package-private */
    public AudioFocusInfo toAudioFocusInfo() {
        return new AudioFocusInfo(this.mAttributes, this.mCallingUid, this.mClientId, this.mPackageName, this.mFocusGainRequest, this.mFocusLossReceived, this.mGrantFlags, this.mSdkTarget);
    }
}

package com.android.server.audio;

import android.app.AppOpsManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.util.Log;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

public class MediaFocusControl implements PlayerFocusEnforcer {
    static final boolean DEBUG = false;
    static final int DUCKING_IN_APP_SDK_LEVEL = 25;
    static final boolean ENFORCE_DUCKING = true;
    static final boolean ENFORCE_DUCKING_FOR_NEW = true;
    static final boolean ENFORCE_MUTING_FOR_RING_OR_CALL = true;
    private static final int RING_CALL_MUTING_ENFORCEMENT_DELAY_MS = 100;
    private static final String TAG = "MediaFocusControl";
    private static final int[] USAGES_TO_MUTE_IN_RING_OR_CALL = new int[]{1, 14};
    protected static final Object mAudioFocusLock = new Object();
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private PlayerFocusEnforcer mFocusEnforcer;
    private ArrayList<IAudioPolicyCallback> mFocusFollowers = new ArrayList();
    private HashMap<String, FocusRequester> mFocusOwnersForFocusPolicy = new HashMap();
    private IAudioPolicyCallback mFocusPolicy = null;
    private final Stack<FocusRequester> mFocusStack = new Stack();
    protected final Stack<FocusRequester> mFocusStackForExternal = new Stack();
    private boolean mNotifyFocusOwnerOnDuck = true;
    private boolean mRingOrCallActive = false;

    protected class AudioFocusDeathHandler implements DeathRecipient {
        private IBinder mCb;

        AudioFocusDeathHandler(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (MediaFocusControl.mAudioFocusLock) {
                if (MediaFocusControl.this.mFocusPolicy != null) {
                    MediaFocusControl.this.removeFocusEntryForExtPolicy(this.mCb);
                } else {
                    MediaFocusControl.this.removeFocusStackEntryOnDeath(this.mCb, MediaFocusControl.this.mFocusStack);
                    MediaFocusControl.this.removeFocusStackEntryOnDeath(this.mCb, MediaFocusControl.this.mFocusStackForExternal);
                }
            }
        }
    }

    protected MediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        this.mContext = cntxt;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mFocusEnforcer = pfe;
    }

    protected void dump(PrintWriter pw) {
        pw.println("\nMediaFocusControl dump time: " + DateFormat.getTimeInstance().format(new Date()));
        dumpFocusStack(pw);
    }

    public boolean duckPlayers(FocusRequester winner, FocusRequester loser) {
        return this.mFocusEnforcer.duckPlayers(winner, loser);
    }

    public void unduckPlayers(FocusRequester winner) {
        this.mFocusEnforcer.unduckPlayers(winner);
    }

    public void mutePlayersForCall(int[] usagesToMute) {
        this.mFocusEnforcer.mutePlayersForCall(usagesToMute);
    }

    public void unmutePlayersForCall() {
        this.mFocusEnforcer.unmutePlayersForCall();
    }

    protected void discardAudioFocusOwner() {
        synchronized (mAudioFocusLock) {
            FocusRequester exFocusOwner;
            if (!this.mFocusStack.empty()) {
                exFocusOwner = (FocusRequester) this.mFocusStack.pop();
                exFocusOwner.handleFocusLoss(-1, null);
                exFocusOwner.release();
            }
            if (!this.mFocusStackForExternal.empty()) {
                exFocusOwner = (FocusRequester) this.mFocusStackForExternal.pop();
                exFocusOwner.handleFocusLoss(-1, null);
                exFocusOwner.release();
            }
        }
    }

    private void notifyTopOfAudioFocusStack(Stack<FocusRequester> stack) {
        if (!stack.empty() && canReassignAudioFocus(stack)) {
            ((FocusRequester) stack.peek()).handleFocusGain(1);
        }
    }

    private void propagateFocusLossFromGain_syncAf(int focusGain, FocusRequester fr, Stack<FocusRequester> stack) {
        Iterator<FocusRequester> stackIterator = stack.iterator();
        while (stackIterator.hasNext()) {
            ((FocusRequester) stackIterator.next()).handleExternalFocusGain(focusGain, fr);
        }
    }

    private void dumpFocusStack(PrintWriter pw) {
        pw.println("\nAudio Focus stack entries (last is top of stack):");
        synchronized (mAudioFocusLock) {
            pw.println("mFocusStack:\n");
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                ((FocusRequester) stackIterator.next()).dump(pw);
            }
            pw.println("mFocusStackForExternal:\n");
            stackIterator = this.mFocusStackForExternal.iterator();
            while (stackIterator.hasNext()) {
                ((FocusRequester) stackIterator.next()).dump(pw);
            }
            if (this.mFocusPolicy == null) {
                pw.println("No external focus policy\n");
            } else {
                pw.println("External focus policy: " + this.mFocusPolicy + ", focus owners:\n");
                dumpExtFocusPolicyFocusOwners(pw);
            }
        }
        pw.println("\n");
        pw.println(" Notify on duck:  " + this.mNotifyFocusOwnerOnDuck + "\n");
        pw.println(" In ring or call: " + this.mRingOrCallActive + "\n");
    }

    private void removeFocusStackEntry(String clientToRemove, boolean signal, boolean notifyFocusFollowers, Stack<FocusRequester> stack) {
        FocusRequester fr;
        if (stack.empty() || !((FocusRequester) stack.peek()).hasSameClient(clientToRemove)) {
            Iterator<FocusRequester> stackIterator = stack.iterator();
            while (stackIterator.hasNext()) {
                fr = (FocusRequester) stackIterator.next();
                if (fr.hasSameClient(clientToRemove)) {
                    Log.i(TAG, "AudioFocus  removeFocusStackEntry(): removing entry for " + clientToRemove);
                    stackIterator.remove();
                    fr.release();
                }
            }
            return;
        }
        fr = (FocusRequester) stack.pop();
        fr.release();
        if (notifyFocusFollowers) {
            AudioFocusInfo afi = fr.toAudioFocusInfo();
            afi.clearLossReceived();
            notifyExtPolicyFocusLoss_syncAf(afi, false);
        }
        if (signal) {
            notifyTopOfAudioFocusStack(stack);
        }
    }

    private void removeFocusStackEntryOnDeath(IBinder cb, Stack<FocusRequester> stack) {
        boolean isTopOfStackForClientToRemove;
        if (stack.isEmpty()) {
            isTopOfStackForClientToRemove = false;
        } else {
            isTopOfStackForClientToRemove = ((FocusRequester) stack.peek()).hasSameBinder(cb);
        }
        Iterator<FocusRequester> stackIterator = stack.iterator();
        while (stackIterator.hasNext()) {
            FocusRequester fr = (FocusRequester) stackIterator.next();
            if (fr.hasSameBinder(cb)) {
                Log.i(TAG, "AudioFocus  removeFocusStackEntryOnDeath(): removing entry for " + cb);
                stackIterator.remove();
                fr.release();
            }
        }
        if (isTopOfStackForClientToRemove) {
            notifyTopOfAudioFocusStack(stack);
        }
    }

    private void removeFocusEntryForExtPolicy(IBinder cb) {
        if (!this.mFocusOwnersForFocusPolicy.isEmpty()) {
            Iterator<Entry<String, FocusRequester>> ownerIterator = this.mFocusOwnersForFocusPolicy.entrySet().iterator();
            while (ownerIterator.hasNext()) {
                FocusRequester fr = (FocusRequester) ((Entry) ownerIterator.next()).getValue();
                if (fr.hasSameBinder(cb)) {
                    ownerIterator.remove();
                    fr.release();
                    notifyExtFocusPolicyFocusAbandon_syncAf(fr.toAudioFocusInfo());
                    break;
                }
            }
        }
    }

    private boolean canReassignAudioFocus(Stack<FocusRequester> stack) {
        if (stack.isEmpty() || !isLockedFocusOwner((FocusRequester) stack.peek())) {
            return true;
        }
        return false;
    }

    private boolean isLockedFocusOwner(FocusRequester fr) {
        return !fr.hasSameClient("AudioFocus_For_Phone_Ring_And_Calls") ? fr.isLockedFocusOwner() : true;
    }

    private int pushBelowLockedFocusOwners(FocusRequester nfr, Stack<FocusRequester> stack) {
        int lastLockedFocusOwnerIndex = stack.size();
        for (int index = stack.size() - 1; index >= 0; index--) {
            if (isLockedFocusOwner((FocusRequester) stack.elementAt(index))) {
                lastLockedFocusOwnerIndex = index;
            }
        }
        if (lastLockedFocusOwnerIndex == stack.size()) {
            Log.e(TAG, "No exclusive focus owner found in propagateFocusLossFromGain_syncAf()", new Exception());
            propagateFocusLossFromGain_syncAf(nfr.getGainRequest(), nfr, stack);
            stack.push(nfr);
            return 1;
        }
        stack.insertElementAt(nfr, lastLockedFocusOwnerIndex);
        return 2;
    }

    protected void setDuckingInExtPolicyAvailable(boolean available) {
        this.mNotifyFocusOwnerOnDuck = available ^ 1;
    }

    boolean mustNotifyFocusOwnerOnDuck() {
        return this.mNotifyFocusOwnerOnDuck;
    }

    void addFocusFollower(IAudioPolicyCallback ff) {
        if (ff != null) {
            synchronized (mAudioFocusLock) {
                boolean found = false;
                for (IAudioPolicyCallback pcb : this.mFocusFollowers) {
                    if (pcb.asBinder().equals(ff.asBinder())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    return;
                }
                this.mFocusFollowers.add(ff);
                notifyExtPolicyCurrentFocusAsync(ff);
            }
        }
    }

    void removeFocusFollower(IAudioPolicyCallback ff) {
        if (ff != null) {
            synchronized (mAudioFocusLock) {
                for (IAudioPolicyCallback pcb : this.mFocusFollowers) {
                    if (pcb.asBinder().equals(ff.asBinder())) {
                        this.mFocusFollowers.remove(pcb);
                        break;
                    }
                }
            }
        }
    }

    void setFocusPolicy(IAudioPolicyCallback policy) {
        if (policy != null) {
            synchronized (mAudioFocusLock) {
                this.mFocusPolicy = policy;
            }
        }
    }

    void unsetFocusPolicy(IAudioPolicyCallback policy) {
        if (policy != null) {
            synchronized (mAudioFocusLock) {
                if (this.mFocusPolicy == policy) {
                    this.mFocusPolicy = null;
                }
            }
        }
    }

    void notifyExtPolicyCurrentFocusAsync(final IAudioPolicyCallback pcb) {
        IAudioPolicyCallback pcb2 = pcb;
        new Thread() {
            public void run() {
                synchronized (MediaFocusControl.mAudioFocusLock) {
                    if (MediaFocusControl.this.mFocusStack.isEmpty()) {
                        return;
                    }
                    try {
                        pcb.notifyAudioFocusGrant(((FocusRequester) MediaFocusControl.this.mFocusStack.peek()).toAudioFocusInfo(), 1);
                    } catch (RemoteException e) {
                        Log.e(MediaFocusControl.TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + pcb.asBinder(), e);
                    }
                }
                return;
            }
        }.start();
    }

    void notifyExtPolicyFocusGrant_syncAf(AudioFocusInfo afi, int requestResult) {
        for (IAudioPolicyCallback pcb : this.mFocusFollowers) {
            try {
                pcb.notifyAudioFocusGrant(afi, requestResult);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + pcb.asBinder(), e);
            }
        }
    }

    void notifyExtPolicyFocusLoss_syncAf(AudioFocusInfo afi, boolean wasDispatched) {
        for (IAudioPolicyCallback pcb : this.mFocusFollowers) {
            try {
                pcb.notifyAudioFocusLoss(afi, wasDispatched);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call notifyAudioFocusLoss() on IAudioPolicyCallback " + pcb.asBinder(), e);
            }
        }
    }

    boolean notifyExtFocusPolicyFocusRequest_syncAf(AudioFocusInfo afi, int requestResult, IAudioFocusDispatcher fd, IBinder cb) {
        if (this.mFocusPolicy == null) {
            return false;
        }
        FocusRequester existingFr = (FocusRequester) this.mFocusOwnersForFocusPolicy.get(afi.getClientId());
        if (existingFr != null) {
            if (!existingFr.hasSameDispatcher(fd)) {
                existingFr.release();
                this.mFocusOwnersForFocusPolicy.put(afi.getClientId(), new FocusRequester(afi, fd, cb, new AudioFocusDeathHandler(cb), this));
            }
        } else if (requestResult == 1 || requestResult == 2) {
            this.mFocusOwnersForFocusPolicy.put(afi.getClientId(), new FocusRequester(afi, fd, cb, new AudioFocusDeathHandler(cb), this));
        }
        try {
            this.mFocusPolicy.notifyAudioFocusRequest(afi, requestResult);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call notifyAudioFocusRequest() on IAudioPolicyCallback " + this.mFocusPolicy.asBinder(), e);
        }
        return true;
    }

    boolean notifyExtFocusPolicyFocusAbandon_syncAf(AudioFocusInfo afi) {
        if (this.mFocusPolicy == null) {
            return false;
        }
        FocusRequester fr = (FocusRequester) this.mFocusOwnersForFocusPolicy.remove(afi.getClientId());
        if (fr != null) {
            fr.release();
        }
        try {
            this.mFocusPolicy.notifyAudioFocusAbandon(afi);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call notifyAudioFocusAbandon() on IAudioPolicyCallback " + this.mFocusPolicy.asBinder(), e);
        }
        return true;
    }

    int dispatchFocusChange(AudioFocusInfo afi, int focusChange) {
        synchronized (mAudioFocusLock) {
            if (this.mFocusPolicy == null) {
                return 0;
            }
            FocusRequester fr = (FocusRequester) this.mFocusOwnersForFocusPolicy.get(afi.getClientId());
            if (fr == null) {
                return 0;
            }
            int dispatchFocusChange = fr.dispatchFocusChange(focusChange);
            return dispatchFocusChange;
        }
    }

    private void dumpExtFocusPolicyFocusOwners(PrintWriter pw) {
        for (Entry<String, FocusRequester> owner : this.mFocusOwnersForFocusPolicy.entrySet()) {
            ((FocusRequester) owner.getValue()).dump(pw);
        }
    }

    protected int getCurrentAudioFocus() {
        synchronized (mAudioFocusLock) {
            if (this.mFocusStack.empty()) {
                return 0;
            }
            int gainRequest = ((FocusRequester) this.mFocusStack.peek()).getGainRequest();
            return gainRequest;
        }
    }

    protected static int getFocusRampTimeMs(int focusGain, AudioAttributes attr) {
        switch (attr.getUsage()) {
            case 1:
            case 14:
                return 1000;
            case 2:
            case 3:
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
            case 13:
                return 500;
            case 4:
            case 6:
            case 11:
            case 12:
            case 16:
                return 700;
            default:
                return 0;
        }
    }

    /* JADX WARNING: Missing block: B:77:0x01f5, code:
            return r22;
     */
    /* JADX WARNING: Missing block: B:87:0x0222, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int requestAudioFocus(AudioAttributes aa, int focusChangeHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, int sdk) {
        Log.i(TAG, " AudioFocus  requestAudioFocus() from uid/pid " + Binder.getCallingUid() + "/" + Binder.getCallingPid() + " clientId=" + clientId + " req=" + focusChangeHint + " flags=0x" + Integer.toHexString(flags));
        if (3 == AudioAttributes.toLegacyStreamType(aa)) {
            LogPower.push(147, callingPackageName, Integer.toString(AudioAttributes.toLegacyStreamType(aa)));
        }
        if (!cb.pingBinder()) {
            Log.e(TAG, " AudioFocus DOA client for requestAudioFocus(), aborting.");
            return 0;
        } else if (this.mAppOps.noteOp(32, Binder.getCallingUid(), callingPackageName) != 0) {
            return 0;
        } else {
            boolean isInExternalDisplay = isAppInExternalDisplay(aa, clientId, callingPackageName, Binder.getCallingUid());
            HwPCUtils.log(TAG, " requestAudioFocus isInExternalDisplay = " + isInExternalDisplay);
            Stack<FocusRequester> stack = this.mFocusStack;
            if (isInExternalDisplay) {
                stack = this.mFocusStackForExternal;
            }
            synchronized (mAudioFocusLock) {
                AudioFocusInfo afiForExtPolicy;
                boolean enteringRingOrCall = (this.mRingOrCallActive ^ 1) & ("AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0 ? 1 : 0);
                if (enteringRingOrCall) {
                    this.mRingOrCallActive = true;
                }
                if (this.mFocusPolicy != null) {
                    afiForExtPolicy = new AudioFocusInfo(aa, Binder.getCallingUid(), clientId, callingPackageName, focusChangeHint, 0, flags, sdk);
                } else {
                    afiForExtPolicy = null;
                }
                boolean focusGrantDelayed = false;
                if (!canReassignAudioFocus(stack)) {
                    if ((flags & 1) == 0) {
                        notifyExtFocusPolicyFocusRequest_syncAf(afiForExtPolicy, 0, fd, cb);
                        return 0;
                    }
                    focusGrantDelayed = true;
                }
                if (notifyExtFocusPolicyFocusRequest_syncAf(afiForExtPolicy, 2, fd, cb)) {
                    return 2;
                }
                AudioFocusDeathHandler afdh = new AudioFocusDeathHandler(cb);
                try {
                    cb.linkToDeath(afdh, 0);
                    if (!stack.empty() && ((FocusRequester) stack.peek()).hasSameClient(clientId)) {
                        FocusRequester fr = (FocusRequester) stack.peek();
                        if (fr.getGainRequest() == focusChangeHint && fr.getGrantFlags() == flags) {
                            cb.unlinkToDeath(afdh, 0);
                            notifyExtPolicyFocusGrant_syncAf(fr.toAudioFocusInfo(), 1);
                            return 1;
                        } else if (!focusGrantDelayed) {
                            stack.pop();
                            fr.release();
                        }
                    }
                    removeFocusStackEntry(clientId, false, false, stack);
                    FocusRequester nfr = new FocusRequester(aa, focusChangeHint, flags, fd, cb, clientId, afdh, callingPackageName, Binder.getCallingUid(), this, sdk);
                    if (focusGrantDelayed) {
                        int requestResult = pushBelowLockedFocusOwners(nfr, stack);
                        if (requestResult != 0) {
                            notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), requestResult);
                        }
                    } else {
                        if (!stack.empty()) {
                            propagateFocusLossFromGain_syncAf(focusChangeHint, nfr, stack);
                        }
                        stack.push(nfr);
                        nfr.handleFocusGainFromRequest(1);
                        notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), 1);
                        if (enteringRingOrCall) {
                            runAudioCheckerForRingOrCallAsync(true);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(TAG, "AudioFocus  requestAudioFocus() could not link to " + cb + " binder death");
                    return 0;
                }
            }
        }
    }

    protected int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId, AudioAttributes aa, String callingPackageName) {
        Log.i(TAG, " AudioFocus  abandonAudioFocus() from uid/pid " + Binder.getCallingUid() + "/" + Binder.getCallingPid() + " clientId=" + clientId);
        try {
            boolean isInExternalDisplay = isAppInExternalDisplay(aa, clientId, callingPackageName, Binder.getCallingUid());
            HwPCUtils.log(TAG, " abandonAudioFocus isInExternalDisplay = " + isInExternalDisplay);
            Stack<FocusRequester> stack = this.mFocusStack;
            if (isInExternalDisplay) {
                stack = this.mFocusStackForExternal;
            }
            synchronized (mAudioFocusLock) {
                if (this.mFocusPolicy != null) {
                    if (notifyExtFocusPolicyFocusAbandon_syncAf(new AudioFocusInfo(aa, Binder.getCallingUid(), clientId, callingPackageName, 0, 0, 0, 0))) {
                        return 1;
                    }
                }
                boolean exitingRingOrCall = this.mRingOrCallActive & ("AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0 ? 1 : 0);
                if (exitingRingOrCall) {
                    this.mRingOrCallActive = false;
                }
                removeFocusStackEntry(clientId, true, true, stack);
                if (exitingRingOrCall) {
                    runAudioCheckerForRingOrCallAsync(false);
                }
            }
        } catch (ConcurrentModificationException cme) {
            Log.e(TAG, "FATAL EXCEPTION AudioFocus  abandonAudioFocus() caused " + cme);
            cme.printStackTrace();
        }
        return 1;
    }

    protected void unregisterAudioFocusClient(String clientId) {
        synchronized (mAudioFocusLock) {
            removeFocusStackEntry(clientId, false, true, this.mFocusStack);
            removeFocusStackEntry(clientId, false, true, this.mFocusStackForExternal);
        }
    }

    private void runAudioCheckerForRingOrCallAsync(final boolean enteringRingOrCall) {
        new Thread() {
            public void run() {
                if (enteringRingOrCall) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (MediaFocusControl.mAudioFocusLock) {
                    if (MediaFocusControl.this.mRingOrCallActive) {
                        MediaFocusControl.this.mFocusEnforcer.mutePlayersForCall(MediaFocusControl.USAGES_TO_MUTE_IN_RING_OR_CALL);
                    } else {
                        MediaFocusControl.this.mFocusEnforcer.unmutePlayersForCall();
                    }
                }
            }
        }.start();
    }

    public void desktopModeChanged(boolean desktopMode) {
    }

    protected boolean isAppInExternalDisplay(AudioAttributes aa, String clientId, String pkgName, int uid) {
        return false;
    }

    public boolean isPkgInExternalStack(String pkgName) {
        return false;
    }
}

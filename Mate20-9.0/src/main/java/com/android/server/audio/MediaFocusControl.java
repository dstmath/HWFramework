package com.android.server.audio;

import android.app.AppOpsManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.HwServiceFactory;
import com.android.server.audio.AudioEventLogger;
import com.android.server.slice.SliceClientPermissions;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MediaFocusControl implements PlayerFocusEnforcer {
    static final boolean DEBUG = false;
    static final int DUCKING_IN_APP_SDK_LEVEL = 25;
    static final boolean ENFORCE_DUCKING = true;
    static final boolean ENFORCE_DUCKING_FOR_NEW = true;
    static final boolean ENFORCE_MUTING_FOR_RING_OR_CALL = true;
    private static final int MAX_STACK_SIZE = 100;
    private static final int RING_CALL_MUTING_ENFORCEMENT_DELAY_MS = 100;
    private static final String TAG = "MediaFocusControl";
    /* access modifiers changed from: private */
    public static final int[] USAGES_TO_MUTE_IN_RING_OR_CALL = {1, 14};
    protected static final Object mAudioFocusLock = new Object();
    private static final AudioEventLogger mEventLogger = new AudioEventLogger(50, "focus commands as seen by MediaFocusControl");
    private final AppOpsManager mAppOps;
    protected final Context mContext;
    @GuardedBy("mExtFocusChangeLock")
    private long mExtFocusChangeCounter;
    private final Object mExtFocusChangeLock = new Object();
    /* access modifiers changed from: private */
    public PlayerFocusEnforcer mFocusEnforcer;
    private ArrayList<IAudioPolicyCallback> mFocusFollowers = new ArrayList<>();
    private HashMap<String, FocusRequester> mFocusOwnersForFocusPolicy = new HashMap<>();
    /* access modifiers changed from: private */
    public IAudioPolicyCallback mFocusPolicy = null;
    protected final Stack<FocusRequester> mFocusStack = new Stack<>();
    private boolean mNotifyFocusOwnerOnDuck = true;
    /* access modifiers changed from: private */
    public boolean mRingOrCallActive = false;

    public class AudioFocusDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;

        AudioFocusDeathHandler(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (MediaFocusControl.mAudioFocusLock) {
                if (MediaFocusControl.this.mFocusPolicy != null) {
                    MediaFocusControl.this.removeFocusEntryForExtPolicy(this.mCb);
                } else {
                    MediaFocusControl.this.removeFocusStackEntryOnDeath(this.mCb);
                }
            }
        }
    }

    protected MediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        this.mContext = cntxt;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mFocusEnforcer = pfe;
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("\nMediaFocusControl dump time: " + DateFormat.getTimeInstance().format(new Date()));
        dumpFocusStack(pw);
        pw.println("\n");
        mEventLogger.dump(pw);
    }

    public boolean duckPlayers(FocusRequester winner, FocusRequester loser, boolean forceDuck) {
        return this.mFocusEnforcer.duckPlayers(winner, loser, forceDuck);
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

    /* access modifiers changed from: protected */
    public void discardAudioFocusOwner() {
        synchronized (mAudioFocusLock) {
            if (!this.mFocusStack.empty()) {
                FocusRequester exFocusOwner = this.mFocusStack.pop();
                exFocusOwner.handleFocusLoss(-1, null, false);
                exFocusOwner.release();
            }
        }
    }

    private void notifyTopOfAudioFocusStack(boolean isInExternal, int topUsage) {
        if (!this.mFocusStack.empty() && canReassignAudioFocus()) {
            FocusRequester nextFr = this.mFocusStack.peek();
            int usage = nextFr.getAudioAttributes().getUsage();
            Log.v(TAG, "nextFr.getIsInExternal() = " + nextFr.getIsInExternal() + ", isInExternal = " + isInExternal + ", usage = " + usage + ", topUsage = " + topUsage);
            if (nextFr.getIsInExternal() == isInExternal || ((isUsageAffectDesktopMedia(topUsage) && nextFr.getIsInExternal()) || isInExternal)) {
                nextFr.handleFocusGain(1);
            }
        }
    }

    @GuardedBy("mAudioFocusLock")
    private void propagateFocusLossFromGain_syncAf(int focusGain, FocusRequester fr, boolean forceDuck) {
        List<String> clientsToRemove = new LinkedList<>();
        Iterator it = this.mFocusStack.iterator();
        while (it.hasNext()) {
            FocusRequester focusLoser = (FocusRequester) it.next();
            int usage = fr.getAudioAttributes().getUsage();
            if ((focusLoser.getIsInExternal() == fr.getIsInExternal() || ((isUsageAffectDesktopMedia(usage) && !fr.getIsInExternal()) || fr.getIsInExternal())) && focusLoser.handleFocusLossFromGain(focusGain, fr, forceDuck)) {
                clientsToRemove.add(focusLoser.getClientId());
            }
        }
        for (String clientToRemove : clientsToRemove) {
            removeFocusStackEntry(clientToRemove, false, true);
        }
    }

    private void dumpFocusStack(PrintWriter pw) {
        pw.println("\nAudio Focus stack entries (last is top of stack):");
        synchronized (mAudioFocusLock) {
            pw.println("mFocusStack:\n");
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                stackIterator.next().dump(pw);
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

    @GuardedBy("mAudioFocusLock")
    private void removeFocusStackEntry(String clientToRemove, boolean signal, boolean notifyFocusFollowers) {
        if (this.mFocusStack.empty() || !this.mFocusStack.peek().hasSameClient(clientToRemove)) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                FocusRequester fr = stackIterator.next();
                if (fr.hasSameClient(clientToRemove)) {
                    Log.i(TAG, "AudioFocus  removeFocusStackEntry(): removing entry for " + clientToRemove);
                    stackIterator.remove();
                    fr.release();
                }
            }
            return;
        }
        FocusRequester fr2 = this.mFocusStack.pop();
        boolean isInExternal = fr2.getIsInExternal();
        int usage = fr2.getAudioAttributes().getUsage();
        fr2.release();
        if (notifyFocusFollowers) {
            AudioFocusInfo afi = fr2.toAudioFocusInfo();
            afi.clearLossReceived();
            notifyExtPolicyFocusLoss_syncAf(afi, false);
        }
        if (signal) {
            notifyTopOfAudioFocusStack(isInExternal, usage);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mAudioFocusLock")
    public void removeFocusStackEntryOnDeath(IBinder cb) {
        boolean isTopOfStackForClientToRemove = !this.mFocusStack.isEmpty() && this.mFocusStack.peek().hasSameBinder(cb);
        boolean isInExternal = false;
        int usage = 0;
        if (isTopOfStackForClientToRemove) {
            FocusRequester topFr = this.mFocusStack.peek();
            isInExternal = topFr.getIsInExternal();
            usage = topFr.getAudioAttributes().getUsage();
        }
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            FocusRequester fr = stackIterator.next();
            if (fr.hasSameBinder(cb)) {
                Log.i(TAG, "AudioFocus  removeFocusStackEntryOnDeath(): removing entry for " + cb);
                stackIterator.remove();
                fr.release();
            }
        }
        if (isTopOfStackForClientToRemove) {
            notifyTopOfAudioFocusStack(isInExternal, usage);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mAudioFocusLock")
    public void removeFocusEntryForExtPolicy(IBinder cb) {
        if (!this.mFocusOwnersForFocusPolicy.isEmpty()) {
            Iterator<Map.Entry<String, FocusRequester>> ownerIterator = this.mFocusOwnersForFocusPolicy.entrySet().iterator();
            while (true) {
                if (!ownerIterator.hasNext()) {
                    break;
                }
                FocusRequester fr = ownerIterator.next().getValue();
                if (fr.hasSameBinder(cb)) {
                    ownerIterator.remove();
                    fr.release();
                    notifyExtFocusPolicyFocusAbandon_syncAf(fr.toAudioFocusInfo());
                    break;
                }
            }
        }
    }

    private boolean canReassignAudioFocus() {
        if (this.mFocusStack.isEmpty() || !isLockedFocusOwner(this.mFocusStack.peek())) {
            return true;
        }
        return false;
    }

    private boolean isLockedFocusOwner(FocusRequester fr) {
        return fr.hasSameClient("AudioFocus_For_Phone_Ring_And_Calls") || fr.isLockedFocusOwner();
    }

    @GuardedBy("mAudioFocusLock")
    private int pushBelowLockedFocusOwners(FocusRequester nfr) {
        int lastLockedFocusOwnerIndex = this.mFocusStack.size();
        for (int index = this.mFocusStack.size() - 1; index >= 0; index--) {
            if (isLockedFocusOwner((FocusRequester) this.mFocusStack.elementAt(index))) {
                lastLockedFocusOwnerIndex = index;
            }
        }
        if (lastLockedFocusOwnerIndex == this.mFocusStack.size()) {
            Log.e(TAG, "No exclusive focus owner found in propagateFocusLossFromGain_syncAf()", new Exception());
            propagateFocusLossFromGain_syncAf(nfr.getGainRequest(), nfr, false);
            this.mFocusStack.push(nfr);
            return 1;
        }
        this.mFocusStack.insertElementAt(nfr, lastLockedFocusOwnerIndex);
        return 2;
    }

    /* access modifiers changed from: protected */
    public void setDuckingInExtPolicyAvailable(boolean available) {
        this.mNotifyFocusOwnerOnDuck = !available;
    }

    /* access modifiers changed from: package-private */
    public boolean mustNotifyFocusOwnerOnDuck() {
        return this.mNotifyFocusOwnerOnDuck;
    }

    /* access modifiers changed from: package-private */
    public void addFocusFollower(IAudioPolicyCallback ff) {
        if (ff != null) {
            synchronized (mAudioFocusLock) {
                boolean found = false;
                Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().asBinder().equals(ff.asBinder())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    this.mFocusFollowers.add(ff);
                    notifyExtPolicyCurrentFocusAsync(ff);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeFocusFollower(IAudioPolicyCallback ff) {
        if (ff != null) {
            synchronized (mAudioFocusLock) {
                Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    IAudioPolicyCallback pcb = it.next();
                    if (pcb.asBinder().equals(ff.asBinder())) {
                        this.mFocusFollowers.remove(pcb);
                        break;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setFocusPolicy(IAudioPolicyCallback policy) {
        if (policy != null) {
            synchronized (mAudioFocusLock) {
                this.mFocusPolicy = policy;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unsetFocusPolicy(IAudioPolicyCallback policy) {
        if (policy != null) {
            synchronized (mAudioFocusLock) {
                if (this.mFocusPolicy == policy) {
                    this.mFocusPolicy = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyExtPolicyCurrentFocusAsync(IAudioPolicyCallback pcb) {
        final IAudioPolicyCallback pcb2 = pcb;
        new Thread() {
            public void run() {
                synchronized (MediaFocusControl.mAudioFocusLock) {
                    if (!MediaFocusControl.this.mFocusStack.isEmpty()) {
                        try {
                            pcb2.notifyAudioFocusGrant(MediaFocusControl.this.mFocusStack.peek().toAudioFocusInfo(), 1);
                        } catch (RemoteException e) {
                            Log.e(MediaFocusControl.TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + pcb2.asBinder(), e);
                        }
                    }
                }
            }
        }.start();
    }

    /* access modifiers changed from: package-private */
    public void notifyExtPolicyFocusGrant_syncAf(AudioFocusInfo afi, int requestResult) {
        Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
        while (it.hasNext()) {
            IAudioPolicyCallback pcb = it.next();
            try {
                pcb.notifyAudioFocusGrant(afi, requestResult);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + pcb.asBinder(), e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyExtPolicyFocusLoss_syncAf(AudioFocusInfo afi, boolean wasDispatched) {
        Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
        while (it.hasNext()) {
            IAudioPolicyCallback pcb = it.next();
            try {
                pcb.notifyAudioFocusLoss(afi, wasDispatched);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call notifyAudioFocusLoss() on IAudioPolicyCallback " + pcb.asBinder(), e);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008a, code lost:
        r0 = th;
     */
    public boolean notifyExtFocusPolicyFocusRequest_syncAf(AudioFocusInfo afi, IAudioFocusDispatcher fd, IBinder cb) {
        AudioFocusInfo audioFocusInfo = afi;
        IBinder iBinder = cb;
        if (this.mFocusPolicy == null) {
            return false;
        }
        synchronized (this.mExtFocusChangeLock) {
            try {
                long j = this.mExtFocusChangeCounter;
                this.mExtFocusChangeCounter = 1 + j;
                audioFocusInfo.setGen(j);
            } catch (Throwable th) {
                th = th;
                IAudioFocusDispatcher iAudioFocusDispatcher = fd;
                while (true) {
                    throw th;
                }
            }
        }
        FocusRequester existingFr = this.mFocusOwnersForFocusPolicy.get(audioFocusInfo.getClientId());
        if (existingFr != null) {
            IAudioFocusDispatcher iAudioFocusDispatcher2 = fd;
            if (!existingFr.hasSameDispatcher(iAudioFocusDispatcher2)) {
                existingFr.release();
                this.mFocusOwnersForFocusPolicy.put(audioFocusInfo.getClientId(), HwServiceFactory.getHwFocusRequester(audioFocusInfo, iAudioFocusDispatcher2, iBinder, new AudioFocusDeathHandler(iBinder), this, false));
            }
        } else {
            this.mFocusOwnersForFocusPolicy.put(audioFocusInfo.getClientId(), HwServiceFactory.getHwFocusRequester(audioFocusInfo, fd, iBinder, new AudioFocusDeathHandler(iBinder), this, false));
        }
        try {
            this.mFocusPolicy.notifyAudioFocusRequest(audioFocusInfo, 1);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call notifyAudioFocusRequest() on IAudioPolicyCallback " + this.mFocusPolicy.asBinder(), e);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        r0.dispatchFocusResultFromExtPolicy(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        r0 = r5.mFocusOwnersForFocusPolicy.get(r6.getClientId());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        if (r0 == null) goto L_0x0021;
     */
    public void setFocusRequestResultFromExtPolicy(AudioFocusInfo afi, int requestResult) {
        synchronized (this.mExtFocusChangeLock) {
            if (afi.getGen() > this.mExtFocusChangeCounter) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean notifyExtFocusPolicyFocusAbandon_syncAf(AudioFocusInfo afi) {
        if (this.mFocusPolicy == null) {
            return false;
        }
        FocusRequester fr = this.mFocusOwnersForFocusPolicy.remove(afi.getClientId());
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

    /* access modifiers changed from: package-private */
    public int dispatchFocusChange(AudioFocusInfo afi, int focusChange) {
        FocusRequester fr;
        synchronized (mAudioFocusLock) {
            if (this.mFocusPolicy == null) {
                return 0;
            }
            if (focusChange == -1) {
                fr = this.mFocusOwnersForFocusPolicy.remove(afi.getClientId());
            } else {
                fr = this.mFocusOwnersForFocusPolicy.get(afi.getClientId());
            }
            if (fr == null) {
                return 0;
            }
            int dispatchFocusChange = fr.dispatchFocusChange(focusChange);
            return dispatchFocusChange;
        }
    }

    private void dumpExtFocusPolicyFocusOwners(PrintWriter pw) {
        for (Map.Entry<String, FocusRequester> owner : this.mFocusOwnersForFocusPolicy.entrySet()) {
            owner.getValue().dump(pw);
        }
    }

    /* access modifiers changed from: protected */
    public int getCurrentAudioFocus() {
        synchronized (mAudioFocusLock) {
            if (this.mFocusStack.empty()) {
                return 0;
            }
            int gainRequest = this.mFocusStack.peek().getGainRequest();
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

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01ee, code lost:
        return r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01c6, code lost:
        return r1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01c7 A[Catch:{ all -> 0x01ef, all -> 0x022b }] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01b8 A[Catch:{ all -> 0x01ef, all -> 0x022b }] */
    public int requestAudioFocus(AudioAttributes aa, int focusChangeHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, int sdk, boolean forceDuck) {
        boolean isInExternalDisplay;
        int i;
        AudioFocusInfo afiForExtPolicy;
        boolean z;
        boolean z2;
        int i2 = focusChangeHint;
        IBinder iBinder = cb;
        String str = clientId;
        String str2 = callingPackageName;
        int i3 = flags;
        mEventLogger.log(new AudioEventLogger.StringEvent("requestAudioFocus() from uid/pid " + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + " clientId=" + str + " callingPack=" + str2 + " req=" + i2 + " flags=0x" + Integer.toHexString(flags) + " sdk=" + sdk).printLog(TAG));
        if (3 == AudioAttributes.toLegacyStreamType(aa)) {
            LogPower.push(147, str2, Integer.toString(AudioAttributes.toLegacyStreamType(aa)));
        }
        if (!cb.pingBinder()) {
            Log.e(TAG, " AudioFocus DOA client for requestAudioFocus(), aborting.");
            return 0;
        } else if (this.mAppOps.noteOp(32, Binder.getCallingUid(), str2) != 0) {
            return 0;
        } else {
            AudioAttributes audioAttributes = aa;
            boolean isInExternalDisplay2 = isMediaForDPExternalDisplay(audioAttributes, str, str2, Binder.getCallingUid());
            HwPCUtils.log(TAG, " requestAudioFocus isInExternalDisplay = " + isInExternalDisplay2);
            synchronized (mAudioFocusLock) {
                try {
                    if (this.mFocusStack.size() > 100) {
                        try {
                            Log.e(TAG, "Max AudioFocus stack size reached, failing requestAudioFocus()");
                            return 0;
                        } catch (Throwable th) {
                            afiForExtPolicy = th;
                            boolean z3 = forceDuck;
                            boolean z4 = isInExternalDisplay2;
                            IBinder iBinder2 = iBinder;
                            throw afiForExtPolicy;
                        }
                    } else {
                        travelsFocusedStack();
                        boolean enteringRingOrCall = (!this.mRingOrCallActive) & ("AudioFocus_For_Phone_Ring_And_Calls".compareTo(str) == 0);
                        if (enteringRingOrCall) {
                            this.mRingOrCallActive = true;
                        }
                        if (this.mFocusPolicy != null) {
                            try {
                                afiForExtPolicy = afiForExtPolicy;
                                i = 100;
                                isInExternalDisplay = isInExternalDisplay2;
                                try {
                                    afiForExtPolicy = new AudioFocusInfo(audioAttributes, Binder.getCallingUid(), str, str2, i2, 0, i3, sdk);
                                } catch (Throwable th2) {
                                    afiForExtPolicy = th2;
                                    boolean z5 = forceDuck;
                                    IBinder iBinder3 = iBinder;
                                    boolean z6 = isInExternalDisplay;
                                }
                            } catch (Throwable th3) {
                                afiForExtPolicy = th3;
                                boolean z7 = forceDuck;
                                boolean z8 = isInExternalDisplay2;
                                IBinder iBinder4 = iBinder;
                                throw afiForExtPolicy;
                            }
                        } else {
                            i = 100;
                            isInExternalDisplay = isInExternalDisplay2;
                            afiForExtPolicy = null;
                        }
                        AudioFocusInfo afiForExtPolicy2 = afiForExtPolicy;
                        boolean focusGrantDelayed = false;
                        try {
                            if (canReassignAudioFocus()) {
                                z = false;
                            } else if ((i3 & 1) == 0) {
                                return 0;
                            } else {
                                z = false;
                                focusGrantDelayed = true;
                            }
                            boolean focusGrantDelayed2 = focusGrantDelayed;
                            IAudioFocusDispatcher iAudioFocusDispatcher = fd;
                            if (notifyExtFocusPolicyFocusRequest_syncAf(afiForExtPolicy2, iAudioFocusDispatcher, iBinder)) {
                                return i;
                            }
                            AudioFocusDeathHandler afdh = new AudioFocusDeathHandler(iBinder);
                            try {
                                iBinder.linkToDeath(afdh, z);
                                try {
                                    if (!this.mFocusStack.empty()) {
                                        if (this.mFocusStack.peek().hasSameClient(str)) {
                                            FocusRequester fr = this.mFocusStack.peek();
                                            if (fr.getGainRequest() == i2 && fr.getGrantFlags() == i3) {
                                                iBinder.unlinkToDeath(afdh, z);
                                                notifyExtPolicyFocusGrant_syncAf(fr.toAudioFocusInfo(), 1);
                                                return 1;
                                            }
                                            z2 = true;
                                            if (!focusGrantDelayed2) {
                                                this.mFocusStack.pop();
                                                fr.release();
                                            }
                                            removeFocusStackEntry(str, z, z);
                                            IBinder iBinder5 = iBinder;
                                            boolean z9 = z2;
                                            AudioFocusDeathHandler audioFocusDeathHandler = afdh;
                                            AudioFocusInfo audioFocusInfo = afiForExtPolicy2;
                                            FocusRequester nfr = HwServiceFactory.getHwFocusRequester(aa, i2, i3, iAudioFocusDispatcher, iBinder5, str, afdh, str2, Binder.getCallingUid(), this, sdk, isInExternalDisplay);
                                            nfr.setIsInExternal(isInExternalDisplay);
                                            if (!focusGrantDelayed2) {
                                                int requestResult = pushBelowLockedFocusOwners(nfr);
                                                if (requestResult != 0) {
                                                    notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), requestResult);
                                                }
                                            } else {
                                                if (!this.mFocusStack.empty()) {
                                                    propagateFocusLossFromGain_syncAf(i2, nfr, forceDuck);
                                                } else {
                                                    boolean z10 = forceDuck;
                                                }
                                                this.mFocusStack.push(nfr);
                                                nfr.handleFocusGainFromRequest(z9 ? 1 : 0);
                                                notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), z9);
                                                if (z9 && enteringRingOrCall) {
                                                    runAudioCheckerForRingOrCallAsync(z9);
                                                }
                                            }
                                        }
                                    }
                                    z2 = true;
                                    removeFocusStackEntry(str, z, z);
                                    IBinder iBinder52 = iBinder;
                                    boolean z92 = z2;
                                    AudioFocusDeathHandler audioFocusDeathHandler2 = afdh;
                                    AudioFocusInfo audioFocusInfo2 = afiForExtPolicy2;
                                } catch (Throwable th4) {
                                    afiForExtPolicy = th4;
                                    boolean z11 = forceDuck;
                                    boolean z12 = isInExternalDisplay;
                                    IBinder iBinder6 = iBinder;
                                    throw afiForExtPolicy;
                                }
                                try {
                                    FocusRequester nfr2 = HwServiceFactory.getHwFocusRequester(aa, i2, i3, iAudioFocusDispatcher, iBinder52, str, afdh, str2, Binder.getCallingUid(), this, sdk, isInExternalDisplay);
                                } catch (Throwable th5) {
                                    afiForExtPolicy = th5;
                                    boolean z13 = forceDuck;
                                    boolean z14 = isInExternalDisplay;
                                    IBinder iBinder7 = cb;
                                    throw afiForExtPolicy;
                                }
                            } catch (RemoteException e) {
                                boolean z15 = forceDuck;
                                AudioFocusDeathHandler audioFocusDeathHandler3 = afdh;
                                AudioFocusInfo audioFocusInfo3 = afiForExtPolicy2;
                                boolean z16 = isInExternalDisplay;
                                RemoteException remoteException = e;
                                StringBuilder sb = new StringBuilder();
                                sb.append("AudioFocus  requestAudioFocus() could not link to ");
                                sb.append(cb);
                                sb.append(" binder death");
                                Log.w(TAG, sb.toString());
                                return z ? 1 : 0;
                            } catch (Throwable th6) {
                                afiForExtPolicy = th6;
                                throw afiForExtPolicy;
                            }
                            try {
                                nfr2.setIsInExternal(isInExternalDisplay);
                                if (!focusGrantDelayed2) {
                                }
                            } catch (Throwable th7) {
                                afiForExtPolicy = th7;
                                IBinder iBinder8 = cb;
                                throw afiForExtPolicy;
                            }
                        } catch (Throwable th8) {
                            afiForExtPolicy = th8;
                            boolean z17 = forceDuck;
                            IBinder iBinder9 = iBinder;
                            boolean z18 = isInExternalDisplay;
                            throw afiForExtPolicy;
                        }
                    }
                } catch (Throwable th9) {
                    afiForExtPolicy = th9;
                    boolean z19 = forceDuck;
                    boolean z20 = isInExternalDisplay2;
                    IBinder iBinder10 = iBinder;
                    throw afiForExtPolicy;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId, AudioAttributes aa, String callingPackageName) {
        String str = clientId;
        AudioEventLogger audioEventLogger = mEventLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("abandonAudioFocus() from uid/pid " + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + " clientId=" + str).printLog(TAG));
        try {
            AudioAttributes audioAttributes = aa;
            String str2 = callingPackageName;
            try {
                HwPCUtils.log(TAG, " abandonAudioFocus isInExternalDisplay = " + isMediaForDPExternalDisplay(audioAttributes, str, str2, Binder.getCallingUid()));
                synchronized (mAudioFocusLock) {
                    travelsFocusedStack();
                    if (this.mFocusPolicy != null) {
                        AudioFocusInfo audioFocusInfo = new AudioFocusInfo(audioAttributes, Binder.getCallingUid(), str, str2, 0, 0, 0, 0);
                        if (notifyExtFocusPolicyFocusAbandon_syncAf(audioFocusInfo)) {
                            return 1;
                        }
                    }
                    boolean exitingRingOrCall = this.mRingOrCallActive & ("AudioFocus_For_Phone_Ring_And_Calls".compareTo(str) == 0);
                    if (exitingRingOrCall) {
                        this.mRingOrCallActive = false;
                    }
                    removeFocusStackEntry(str, true, true);
                    if (true && exitingRingOrCall) {
                        runAudioCheckerForRingOrCallAsync(false);
                    }
                }
            } catch (ConcurrentModificationException e) {
                cme = e;
            }
        } catch (ConcurrentModificationException e2) {
            cme = e2;
            AudioAttributes audioAttributes2 = aa;
            String str3 = callingPackageName;
            Log.e(TAG, "FATAL EXCEPTION AudioFocus  abandonAudioFocus() caused " + cme);
            cme.printStackTrace();
            return 1;
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterAudioFocusClient(String clientId) {
        synchronized (mAudioFocusLock) {
            removeFocusStackEntry(clientId, false, true);
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

    /* access modifiers changed from: protected */
    public boolean isMediaForDPExternalDisplay(AudioAttributes aa, String clientId, String pkgName, int uid) {
        return false;
    }

    public boolean isPkgInExternalDisplay(String pkgName) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isInDesktopMode() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void travelsFocusedStack() {
    }

    /* access modifiers changed from: protected */
    public boolean isUsageAffectDesktopMedia(int usage) {
        return false;
    }
}

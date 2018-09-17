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
import android.util.Log;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.Stack;

public class MediaFocusControl {
    private static final String TAG = "MediaFocusControl";
    private static final Object mAudioFocusLock = null;
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private ArrayList<IAudioPolicyCallback> mFocusFollowers;
    private final Stack<FocusRequester> mFocusStack;
    private boolean mNotifyFocusOwnerOnDuck;

    /* renamed from: com.android.server.audio.MediaFocusControl.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ IAudioPolicyCallback val$pcb2;

        AnonymousClass1(IAudioPolicyCallback val$pcb2) {
            this.val$pcb2 = val$pcb2;
        }

        public void run() {
            synchronized (MediaFocusControl.mAudioFocusLock) {
                if (MediaFocusControl.this.mFocusStack.isEmpty()) {
                    return;
                }
                try {
                    this.val$pcb2.notifyAudioFocusGrant(((FocusRequester) MediaFocusControl.this.mFocusStack.peek()).toAudioFocusInfo(), 1);
                } catch (RemoteException e) {
                    Log.e(MediaFocusControl.TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + this.val$pcb2.asBinder(), e);
                }
            }
        }
    }

    protected class AudioFocusDeathHandler implements DeathRecipient {
        private IBinder mCb;

        AudioFocusDeathHandler(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (MediaFocusControl.mAudioFocusLock) {
                Log.w(MediaFocusControl.TAG, "  AudioFocus   audio focus client died");
                MediaFocusControl.this.removeFocusStackEntryForClient(this.mCb);
            }
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.MediaFocusControl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.audio.MediaFocusControl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.MediaFocusControl.<clinit>():void");
    }

    protected MediaFocusControl(Context cntxt) {
        this.mFocusStack = new Stack();
        this.mNotifyFocusOwnerOnDuck = true;
        this.mFocusFollowers = new ArrayList();
        this.mContext = cntxt;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
    }

    protected void dump(PrintWriter pw) {
        pw.println("\nMediaFocusControl dump time: " + DateFormat.getTimeInstance().format(new Date()));
        dumpFocusStack(pw);
    }

    protected void discardAudioFocusOwner() {
        synchronized (mAudioFocusLock) {
            if (!this.mFocusStack.empty()) {
                FocusRequester exFocusOwner = (FocusRequester) this.mFocusStack.pop();
                exFocusOwner.handleFocusLoss(-1);
                exFocusOwner.release();
            }
        }
    }

    private void notifyTopOfAudioFocusStack() {
        if (!this.mFocusStack.empty() && canReassignAudioFocus()) {
            ((FocusRequester) this.mFocusStack.peek()).handleFocusGain(1);
        }
    }

    private void propagateFocusLossFromGain_syncAf(int focusGain) {
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            ((FocusRequester) stackIterator.next()).handleExternalFocusGain(focusGain);
        }
    }

    private void dumpFocusStack(PrintWriter pw) {
        pw.println("\nAudio Focus stack entries (last is top of stack):");
        synchronized (mAudioFocusLock) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                ((FocusRequester) stackIterator.next()).dump(pw);
            }
        }
        pw.println("\n Notify on duck: " + this.mNotifyFocusOwnerOnDuck + "\n");
    }

    private void removeFocusStackEntry(String clientToRemove, boolean signal, boolean notifyFocusFollowers) {
        FocusRequester fr;
        if (this.mFocusStack.empty() || !((FocusRequester) this.mFocusStack.peek()).hasSameClient(clientToRemove)) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
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
        fr = (FocusRequester) this.mFocusStack.pop();
        fr.release();
        if (notifyFocusFollowers) {
            AudioFocusInfo afi = fr.toAudioFocusInfo();
            afi.clearLossReceived();
            notifyExtPolicyFocusLoss_syncAf(afi, false);
        }
        if (signal) {
            notifyTopOfAudioFocusStack();
        }
    }

    private void removeFocusStackEntryForClient(IBinder cb) {
        boolean isTopOfStackForClientToRemove;
        if (this.mFocusStack.isEmpty()) {
            isTopOfStackForClientToRemove = false;
        } else {
            isTopOfStackForClientToRemove = ((FocusRequester) this.mFocusStack.peek()).hasSameBinder(cb);
        }
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            if (((FocusRequester) stackIterator.next()).hasSameBinder(cb)) {
                Log.i(TAG, "AudioFocus  removeFocusStackEntry(): removing entry for " + cb);
                stackIterator.remove();
            }
        }
        if (isTopOfStackForClientToRemove) {
            notifyTopOfAudioFocusStack();
        }
    }

    private boolean canReassignAudioFocus() {
        if (this.mFocusStack.isEmpty() || !isLockedFocusOwner((FocusRequester) this.mFocusStack.peek())) {
            return true;
        }
        return false;
    }

    private boolean isLockedFocusOwner(FocusRequester fr) {
        return !fr.hasSameClient("AudioFocus_For_Phone_Ring_And_Calls") ? fr.isLockedFocusOwner() : true;
    }

    private int pushBelowLockedFocusOwners(FocusRequester nfr) {
        int lastLockedFocusOwnerIndex = this.mFocusStack.size();
        for (int index = this.mFocusStack.size() - 1; index >= 0; index--) {
            if (isLockedFocusOwner((FocusRequester) this.mFocusStack.elementAt(index))) {
                lastLockedFocusOwnerIndex = index;
            }
        }
        if (lastLockedFocusOwnerIndex == this.mFocusStack.size()) {
            Log.e(TAG, "No exclusive focus owner found in propagateFocusLossFromGain_syncAf()", new Exception());
            propagateFocusLossFromGain_syncAf(nfr.getGainRequest());
            this.mFocusStack.push(nfr);
            return 1;
        }
        this.mFocusStack.insertElementAt(nfr, lastLockedFocusOwnerIndex);
        return 2;
    }

    protected void setDuckingInExtPolicyAvailable(boolean available) {
        this.mNotifyFocusOwnerOnDuck = !available;
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

    void notifyExtPolicyCurrentFocusAsync(IAudioPolicyCallback pcb) {
        IAudioPolicyCallback pcb2 = pcb;
        new AnonymousClass1(pcb).start();
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

    protected int getCurrentAudioFocus() {
        synchronized (mAudioFocusLock) {
            if (this.mFocusStack.empty()) {
                return 0;
            }
            int gainRequest = ((FocusRequester) this.mFocusStack.peek()).getGainRequest();
            return gainRequest;
        }
    }

    protected int requestAudioFocus(AudioAttributes aa, int focusChangeHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags) {
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
            synchronized (mAudioFocusLock) {
                boolean focusGrantDelayed = false;
                if (!canReassignAudioFocus()) {
                    if ((flags & 1) == 0) {
                        return 0;
                    }
                    focusGrantDelayed = true;
                }
                AudioFocusDeathHandler afdh = new AudioFocusDeathHandler(cb);
                try {
                    cb.linkToDeath(afdh, 0);
                    if (!this.mFocusStack.empty() && ((FocusRequester) this.mFocusStack.peek()).hasSameClient(clientId)) {
                        FocusRequester fr = (FocusRequester) this.mFocusStack.peek();
                        if (fr.getGainRequest() == focusChangeHint && fr.getGrantFlags() == flags) {
                            cb.unlinkToDeath(afdh, 0);
                            notifyExtPolicyFocusGrant_syncAf(fr.toAudioFocusInfo(), 1);
                            return 1;
                        } else if (!focusGrantDelayed) {
                            this.mFocusStack.pop();
                            fr.release();
                        }
                    }
                    removeFocusStackEntry(clientId, false, false);
                    FocusRequester nfr = new FocusRequester(aa, focusChangeHint, flags, fd, cb, clientId, afdh, callingPackageName, Binder.getCallingUid(), this);
                    if (focusGrantDelayed) {
                        int requestResult = pushBelowLockedFocusOwners(nfr);
                        if (requestResult != 0) {
                            notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), requestResult);
                        }
                        return requestResult;
                    }
                    if (!this.mFocusStack.empty()) {
                        propagateFocusLossFromGain_syncAf(focusChangeHint);
                    }
                    this.mFocusStack.push(nfr);
                    notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), 1);
                    return 1;
                } catch (RemoteException e) {
                    Log.w(TAG, "AudioFocus  requestAudioFocus() could not link to " + cb + " binder death");
                    return 0;
                }
            }
        }
    }

    protected int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId, AudioAttributes aa) {
        Log.i(TAG, " AudioFocus  abandonAudioFocus() from uid/pid " + Binder.getCallingUid() + "/" + Binder.getCallingPid() + " clientId=" + clientId);
        try {
            synchronized (mAudioFocusLock) {
                removeFocusStackEntry(clientId, true, true);
            }
        } catch (ConcurrentModificationException cme) {
            Log.e(TAG, "FATAL EXCEPTION AudioFocus  abandonAudioFocus() caused " + cme);
            cme.printStackTrace();
        }
        return 1;
    }

    protected void unregisterAudioFocusClient(String clientId) {
        synchronized (mAudioFocusLock) {
            removeFocusStackEntry(clientId, false, true);
        }
    }
}

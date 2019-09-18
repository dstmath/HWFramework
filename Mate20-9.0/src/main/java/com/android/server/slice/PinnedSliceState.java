package com.android.server.slice;

import android.app.slice.SliceSpec;
import android.content.ContentProviderClient;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public class PinnedSliceState {
    private static final long SLICE_TIMEOUT = 5000;
    private static final String TAG = "PinnedSliceState";
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        public final void binderDied() {
            PinnedSliceState.this.handleRecheckListeners();
        }
    };
    @GuardedBy("mLock")
    private final ArrayMap<IBinder, ListenerInfo> mListeners = new ArrayMap<>();
    private final Object mLock;
    @GuardedBy("mLock")
    private final ArraySet<String> mPinnedPkgs = new ArraySet<>();
    private final String mPkg;
    private final SliceManagerService mService;
    private boolean mSlicePinned;
    @GuardedBy("mLock")
    private SliceSpec[] mSupportedSpecs = null;
    private final Uri mUri;

    private class ListenerInfo {
        private int callingPid;
        private int callingUid;
        private boolean hasPermission;
        private String pkg;
        /* access modifiers changed from: private */
        public IBinder token;

        public ListenerInfo(IBinder token2, String pkg2, boolean hasPermission2, int callingUid2, int callingPid2) {
            this.token = token2;
            this.pkg = pkg2;
            this.hasPermission = hasPermission2;
            this.callingUid = callingUid2;
            this.callingPid = callingPid2;
        }
    }

    public PinnedSliceState(SliceManagerService service, Uri uri, String pkg) {
        this.mService = service;
        this.mUri = uri;
        this.mPkg = pkg;
        this.mLock = this.mService.getLock();
    }

    public String getPkg() {
        return this.mPkg;
    }

    public SliceSpec[] getSpecs() {
        return this.mSupportedSpecs;
    }

    public void mergeSpecs(SliceSpec[] supportedSpecs) {
        synchronized (this.mLock) {
            if (this.mSupportedSpecs == null) {
                this.mSupportedSpecs = supportedSpecs;
            } else {
                this.mSupportedSpecs = (SliceSpec[]) Arrays.asList(this.mSupportedSpecs).stream().map(new Function(supportedSpecs) {
                    private final /* synthetic */ SliceSpec[] f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final Object apply(Object obj) {
                        return PinnedSliceState.lambda$mergeSpecs$0(PinnedSliceState.this, this.f$1, (SliceSpec) obj);
                    }
                }).filter($$Lambda$PinnedSliceState$2PaYhOaggf1E5xg82LTTEwxmLE4.INSTANCE).toArray($$Lambda$PinnedSliceState$vxnx7v9Z67Tj9aywVmtdX48br1M.INSTANCE);
            }
        }
    }

    public static /* synthetic */ SliceSpec lambda$mergeSpecs$0(PinnedSliceState pinnedSliceState, SliceSpec[] supportedSpecs, SliceSpec s) {
        SliceSpec other = pinnedSliceState.findSpec(supportedSpecs, s.getType());
        if (other == null) {
            return null;
        }
        if (other.getRevision() < s.getRevision()) {
            return other;
        }
        return s;
    }

    static /* synthetic */ boolean lambda$mergeSpecs$1(SliceSpec s) {
        return s != null;
    }

    static /* synthetic */ SliceSpec[] lambda$mergeSpecs$2(int x$0) {
        return new SliceSpec[x$0];
    }

    private SliceSpec findSpec(SliceSpec[] specs, String type) {
        for (SliceSpec spec : specs) {
            if (Objects.equals(spec.getType(), type)) {
                return spec;
            }
        }
        return null;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void destroy() {
        setSlicePinned(false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002b, code lost:
        return;
     */
    private void setSlicePinned(boolean pinned) {
        synchronized (this.mLock) {
            if (this.mSlicePinned != pinned) {
                this.mSlicePinned = pinned;
                if (pinned) {
                    this.mService.getHandler().post(new Runnable() {
                        public final void run() {
                            PinnedSliceState.this.handleSendPinned();
                        }
                    });
                } else {
                    this.mService.getHandler().post(new Runnable() {
                        public final void run() {
                            PinnedSliceState.this.handleSendUnpinned();
                        }
                    });
                }
            }
        }
    }

    public void pin(String pkg, SliceSpec[] specs, IBinder token) {
        synchronized (this.mLock) {
            ArrayMap<IBinder, ListenerInfo> arrayMap = this.mListeners;
            ListenerInfo listenerInfo = new ListenerInfo(token, pkg, true, Binder.getCallingUid(), Binder.getCallingPid());
            arrayMap.put(token, listenerInfo);
            try {
                token.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
            }
            mergeSpecs(specs);
            setSlicePinned(true);
        }
    }

    public boolean unpin(String pkg, IBinder token) {
        synchronized (this.mLock) {
            token.unlinkToDeath(this.mDeathRecipient, 0);
            this.mListeners.remove(token);
        }
        return !hasPinOrListener();
    }

    public boolean isListening() {
        boolean z;
        synchronized (this.mLock) {
            z = !this.mListeners.isEmpty();
        }
        return z;
    }

    @VisibleForTesting
    public boolean hasPinOrListener() {
        boolean z;
        synchronized (this.mLock) {
            if (this.mPinnedPkgs.isEmpty()) {
                if (this.mListeners.isEmpty()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public ContentProviderClient getClient() {
        ContentProviderClient client = this.mService.getContext().getContentResolver().acquireContentProviderClient(this.mUri);
        if (client == null) {
            return null;
        }
        client.setDetectNotResponding(SLICE_TIMEOUT);
        return client;
    }

    private void checkSelfRemove() {
        if (!hasPinOrListener()) {
            this.mService.removePinnedSlice(this.mUri);
        }
    }

    /* access modifiers changed from: private */
    public void handleRecheckListeners() {
        if (hasPinOrListener()) {
            synchronized (this.mLock) {
                for (int i = this.mListeners.size() - 1; i >= 0; i--) {
                    if (!this.mListeners.valueAt(i).token.isBinderAlive()) {
                        this.mListeners.removeAt(i);
                    }
                }
                checkSelfRemove();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSendPinned() {
        ContentProviderClient client = getClient();
        if (client == null) {
            if (client != null) {
                $closeResource(null, client);
            }
            return;
        }
        Bundle b = new Bundle();
        b.putParcelable("slice_uri", this.mUri);
        try {
            client.call("pin", null, b);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to contact " + this.mUri, e);
        }
        if (client != null) {
            $closeResource(null, client);
        }
        return;
        try {
        } catch (Throwable th) {
            if (client != null) {
                $closeResource(r1, client);
            }
            throw th;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* access modifiers changed from: private */
    public void handleSendUnpinned() {
        ContentProviderClient client = getClient();
        if (client == null) {
            if (client != null) {
                $closeResource(null, client);
            }
            return;
        }
        Bundle b = new Bundle();
        b.putParcelable("slice_uri", this.mUri);
        try {
            client.call("unpin", null, b);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to contact " + this.mUri, e);
        }
        if (client != null) {
            $closeResource(null, client);
        }
        return;
        try {
        } catch (Throwable th) {
            if (client != null) {
                $closeResource(r1, client);
            }
            throw th;
        }
    }
}

package com.android.server.fingerprint;

import android.content.Context;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Slog;
import java.util.NoSuchElementException;

public abstract class ClientMonitor implements DeathRecipient {
    protected static final boolean DEBUG = true;
    protected static final int ERROR_ESRCH = 3;
    private static final int HIDDEN_SPACE_ID = -100;
    protected static final String TAG = "FingerprintService";
    public static int mAcquiredInfo;
    private Context mContext;
    private int mGroupId;
    private long mHalDeviceId;
    private boolean mIsRestricted;
    private String mOwner;
    private IFingerprintServiceReceiver mReceiver;
    private int mTargetUserId;
    private IBinder mToken;
    private final UserManager mUserManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.fingerprint.ClientMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.fingerprint.ClientMonitor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.ClientMonitor.<clinit>():void");
    }

    public abstract IFingerprintDaemon getFingerprintDaemon();

    public abstract void notifyUserActivity();

    public abstract boolean onAuthenticated(int i, int i2);

    public abstract boolean onEnrollResult(int i, int i2, int i3);

    public abstract boolean onEnumerationResult(int i, int i2);

    public abstract boolean onRemoved(int i, int i2);

    public abstract int start();

    public abstract int stop(boolean z);

    public ClientMonitor(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int userId, int groupId, boolean restricted, String owner) {
        this.mContext = context;
        this.mHalDeviceId = halDeviceId;
        this.mToken = token;
        this.mReceiver = receiver;
        this.mTargetUserId = userId;
        this.mGroupId = groupId;
        this.mIsRestricted = restricted;
        this.mOwner = owner;
        this.mUserManager = UserManager.get(this.mContext);
        if (token != null) {
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "caught remote exception in linkToDeath: ", e);
            }
        }
    }

    public boolean onAcquired(int acquiredInfo) {
        boolean z;
        if (this.mReceiver == null) {
            return DEBUG;
        }
        try {
            this.mReceiver.onAcquired(getHalDeviceId(), acquiredInfo);
            z = false;
            return z;
        } catch (RemoteException e) {
            z = TAG;
            Slog.w(z, "Failed to invoke sendAcquired:", e);
            return DEBUG;
        } finally {
            mAcquiredInfo = acquiredInfo;
        }
    }

    public boolean onError(int error) {
        if (this.mReceiver != null) {
            try {
                this.mReceiver.onError(getHalDeviceId(), error);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to invoke sendError:", e);
            }
        }
        return DEBUG;
    }

    public void destroy() {
        Slog.v(TAG, "ClientMonitor destroy");
        if (this.mToken != null) {
            try {
                this.mToken.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                Slog.e(TAG, "destroy(): " + this + ":", new Exception("here"));
            }
            this.mToken = null;
        }
        this.mReceiver = null;
    }

    public void binderDied() {
        Slog.v(TAG, "fingerprint app died");
        stop(false);
        this.mToken = null;
        this.mReceiver = null;
        onError(1);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mToken != null) {
                Slog.w(TAG, "removing leaked reference: " + this.mToken);
                onError(1);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    public final String getOwnerString() {
        return this.mOwner;
    }

    public final IFingerprintServiceReceiver getReceiver() {
        return this.mReceiver;
    }

    public final boolean getIsRestricted() {
        return this.mIsRestricted;
    }

    public final int getTargetUserId() {
        return this.mTargetUserId;
    }

    public final int getGroupId() {
        return this.mGroupId;
    }

    public final IBinder getToken() {
        return this.mToken;
    }

    protected final int getRealUserIdForHal(int groupId) {
        UserInfo info = this.mUserManager.getUserInfo(groupId);
        if (info == null || !info.isHwHiddenSpace()) {
            return groupId;
        }
        return HIDDEN_SPACE_ID;
    }
}

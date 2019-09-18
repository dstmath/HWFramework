package android.os;

import android.os.IBinder;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

public class CommonClock {
    public static final int ERROR_ESTIMATE_UNKNOWN = Integer.MAX_VALUE;
    public static final long INVALID_TIMELINE_ID = 0;
    private static final int METHOD_CBK_ON_TIMELINE_CHANGED = 1;
    private static final int METHOD_COMMON_TIME_TO_LOCAL_TIME = 2;
    private static final int METHOD_GET_COMMON_FREQ = 5;
    private static final int METHOD_GET_COMMON_TIME = 4;
    private static final int METHOD_GET_ESTIMATED_ERROR = 8;
    private static final int METHOD_GET_LOCAL_FREQ = 7;
    private static final int METHOD_GET_LOCAL_TIME = 6;
    private static final int METHOD_GET_MASTER_ADDRESS = 11;
    private static final int METHOD_GET_STATE = 10;
    private static final int METHOD_GET_TIMELINE_ID = 9;
    private static final int METHOD_IS_COMMON_TIME_VALID = 1;
    private static final int METHOD_LOCAL_TIME_TO_COMMON_TIME = 3;
    private static final int METHOD_REGISTER_LISTENER = 12;
    private static final int METHOD_UNREGISTER_LISTENER = 13;
    public static final String SERVICE_NAME = "common_time.clock";
    public static final int STATE_CLIENT = 1;
    public static final int STATE_INITIAL = 0;
    public static final int STATE_INVALID = -1;
    public static final int STATE_MASTER = 2;
    public static final int STATE_RONIN = 3;
    public static final int STATE_WAIT_FOR_ELECTION = 4;
    public static final long TIME_NOT_SYNCED = -1;
    private TimelineChangedListener mCallbackTgt;
    private IBinder.DeathRecipient mDeathHandler;
    private String mInterfaceDesc;
    /* access modifiers changed from: private */
    public final Object mListenerLock;
    private IBinder mRemote;
    /* access modifiers changed from: private */
    public OnServerDiedListener mServerDiedListener;
    /* access modifiers changed from: private */
    public OnTimelineChangedListener mTimelineChangedListener;
    private CommonTimeUtils mUtils;

    public interface OnServerDiedListener {
        void onServerDied();
    }

    public interface OnTimelineChangedListener {
        void onTimelineChanged(long j);
    }

    private class TimelineChangedListener extends Binder {
        private static final String DESCRIPTOR = "android.os.ICommonClockListener";

        private TimelineChangedListener() {
        }

        /* access modifiers changed from: protected */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                return super.onTransact(code, data, reply, flags);
            }
            data.enforceInterface(DESCRIPTOR);
            long timelineId = data.readLong();
            synchronized (CommonClock.this.mListenerLock) {
                if (CommonClock.this.mTimelineChangedListener != null) {
                    CommonClock.this.mTimelineChangedListener.onTimelineChanged(timelineId);
                }
            }
            return true;
        }
    }

    public CommonClock() throws RemoteException {
        this.mListenerLock = new Object();
        this.mTimelineChangedListener = null;
        this.mServerDiedListener = null;
        this.mRemote = null;
        this.mInterfaceDesc = "";
        this.mDeathHandler = new IBinder.DeathRecipient() {
            public void binderDied() {
                synchronized (CommonClock.this.mListenerLock) {
                    if (CommonClock.this.mServerDiedListener != null) {
                        CommonClock.this.mServerDiedListener.onServerDied();
                    }
                }
            }
        };
        this.mCallbackTgt = null;
        this.mRemote = ServiceManager.getService(SERVICE_NAME);
        if (this.mRemote != null) {
            this.mInterfaceDesc = this.mRemote.getInterfaceDescriptor();
            this.mUtils = new CommonTimeUtils(this.mRemote, this.mInterfaceDesc);
            this.mRemote.linkToDeath(this.mDeathHandler, 0);
            registerTimelineChangeListener();
            return;
        }
        throw new RemoteException();
    }

    public static CommonClock create() {
        try {
            return new CommonClock();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void release() {
        unregisterTimelineChangeListener();
        if (this.mRemote != null) {
            try {
                this.mRemote.unlinkToDeath(this.mDeathHandler, 0);
            } catch (NoSuchElementException e) {
            }
            this.mRemote = null;
        }
        this.mUtils = null;
    }

    public long getTime() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetLong(4, -1);
    }

    public int getEstimatedError() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(8, Integer.MAX_VALUE);
    }

    public long getTimelineId() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetLong(9, 0);
    }

    public int getState() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(10, -1);
    }

    public InetSocketAddress getMasterAddr() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetSockaddr(11);
    }

    public void setTimelineChangedListener(OnTimelineChangedListener listener) {
        synchronized (this.mListenerLock) {
            this.mTimelineChangedListener = listener;
        }
    }

    public void setServerDiedListener(OnServerDiedListener listener) {
        synchronized (this.mListenerLock) {
            this.mServerDiedListener = listener;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
    }

    private void throwOnDeadServer() throws RemoteException {
        if (this.mRemote == null || this.mUtils == null) {
            throw new RemoteException();
        }
    }

    private void registerTimelineChangeListener() throws RemoteException {
        boolean success;
        if (this.mCallbackTgt == null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            this.mCallbackTgt = new TimelineChangedListener();
            try {
                data.writeInterfaceToken(this.mInterfaceDesc);
                data.writeStrongBinder(this.mCallbackTgt);
                boolean z = false;
                this.mRemote.transact(12, data, reply, 0);
                if (reply.readInt() == 0) {
                    z = true;
                }
                success = z;
            } catch (RemoteException e) {
                success = false;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            if (!success) {
                this.mCallbackTgt = null;
                this.mRemote = null;
                this.mUtils = null;
            }
        }
    }

    private void unregisterTimelineChangeListener() {
        if (this.mCallbackTgt != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(this.mInterfaceDesc);
                data.writeStrongBinder(this.mCallbackTgt);
                this.mRemote.transact(13, data, reply, 0);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                this.mCallbackTgt = null;
                throw th;
            }
            reply.recycle();
            data.recycle();
            this.mCallbackTgt = null;
        }
    }
}

package android.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITaskStackListener extends IInterface {
    public static final int FORCED_RESIZEABLE_REASON_SECONDARY_DISPLAY = 2;
    public static final int FORCED_RESIZEABLE_REASON_SPLIT_SCREEN = 1;

    public static abstract class Stub extends Binder implements ITaskStackListener {
        private static final String DESCRIPTOR = "android.app.ITaskStackListener";
        static final int TRANSACTION_onActivityDismissingDockedStack = 8;
        static final int TRANSACTION_onActivityForcedResizable = 7;
        static final int TRANSACTION_onActivityLaunchOnSecondaryDisplayFailed = 9;
        static final int TRANSACTION_onActivityPinned = 2;
        static final int TRANSACTION_onActivityRequestedOrientationChanged = 14;
        static final int TRANSACTION_onActivityUnpinned = 3;
        static final int TRANSACTION_onPinnedActivityRestartAttempt = 4;
        static final int TRANSACTION_onPinnedStackAnimationEnded = 6;
        static final int TRANSACTION_onPinnedStackAnimationStarted = 5;
        static final int TRANSACTION_onTaskCreated = 10;
        static final int TRANSACTION_onTaskDescriptionChanged = 13;
        static final int TRANSACTION_onTaskMovedToFront = 12;
        static final int TRANSACTION_onTaskProfileLocked = 16;
        static final int TRANSACTION_onTaskRemovalStarted = 15;
        static final int TRANSACTION_onTaskRemoved = 11;
        static final int TRANSACTION_onTaskSnapshotChanged = 17;
        static final int TRANSACTION_onTaskStackChanged = 1;

        private static class Proxy implements ITaskStackListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onTaskStackChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityPinned(String packageName, int userId, int taskId, int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityUnpinned() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clearedTask);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPinnedStackAnimationStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPinnedStackAnimationEnded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(taskId);
                    _data.writeInt(reason);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityDismissingDockedStack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskRemoved(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskMovedToFront(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (td != null) {
                        _data.writeInt(1);
                        td.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(requestedOrientation);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskRemovalStarted(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskProfileLocked(int taskId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(userId);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (snapshot != null) {
                        _data.writeInt(1);
                        snapshot.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITaskStackListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITaskStackListener)) {
                return new Proxy(obj);
            }
            return (ITaskStackListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.app.ActivityManager$TaskDescription} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: android.app.ActivityManager$TaskSnapshot} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v26 */
        /* JADX WARNING: type inference failed for: r1v27 */
        /* JADX WARNING: type inference failed for: r1v28 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg1 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskStackChanged();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onActivityPinned(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onActivityUnpinned();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onPinnedActivityRestartAttempt(data.readInt() != 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onPinnedStackAnimationStarted();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onPinnedStackAnimationEnded();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onActivityForcedResizable(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onActivityDismissingDockedStack();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onActivityLaunchOnSecondaryDisplayFailed();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(data);
                        }
                        onTaskCreated(_arg0, _arg1);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskRemoved(data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskMovedToFront(data.readInt());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ActivityManager.TaskDescription.CREATOR.createFromParcel(data);
                        }
                        onTaskDescriptionChanged(_arg02, _arg1);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onActivityRequestedOrientationChanged(data.readInt(), data.readInt());
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskRemovalStarted(data.readInt());
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskProfileLocked(data.readInt(), data.readInt());
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ActivityManager.TaskSnapshot.CREATOR.createFromParcel(data);
                        }
                        onTaskSnapshotChanged(_arg03, _arg1);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onActivityDismissingDockedStack() throws RemoteException;

    void onActivityForcedResizable(String str, int i, int i2) throws RemoteException;

    void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException;

    void onActivityPinned(String str, int i, int i2, int i3) throws RemoteException;

    void onActivityRequestedOrientationChanged(int i, int i2) throws RemoteException;

    void onActivityUnpinned() throws RemoteException;

    void onPinnedActivityRestartAttempt(boolean z) throws RemoteException;

    void onPinnedStackAnimationEnded() throws RemoteException;

    void onPinnedStackAnimationStarted() throws RemoteException;

    void onTaskCreated(int i, ComponentName componentName) throws RemoteException;

    void onTaskDescriptionChanged(int i, ActivityManager.TaskDescription taskDescription) throws RemoteException;

    void onTaskMovedToFront(int i) throws RemoteException;

    void onTaskProfileLocked(int i, int i2) throws RemoteException;

    void onTaskRemovalStarted(int i) throws RemoteException;

    void onTaskRemoved(int i) throws RemoteException;

    void onTaskSnapshotChanged(int i, ActivityManager.TaskSnapshot taskSnapshot) throws RemoteException;

    void onTaskStackChanged() throws RemoteException;
}

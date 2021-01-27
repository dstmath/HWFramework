package com.huawei.systemserver.swing;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwSwingEventNotifier extends IInterface {
    List<String> getKeyguardNotificationInfoList() throws RemoteException;

    boolean isMusicLockScreenStyle() throws RemoteException;

    void swingMotionGesture(String str) throws RemoteException;

    void swingNotificationState(int i) throws RemoteException;

    public static class Default implements IHwSwingEventNotifier {
        @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
        public void swingNotificationState(int state) throws RemoteException {
        }

        @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
        public List<String> getKeyguardNotificationInfoList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
        public boolean isMusicLockScreenStyle() throws RemoteException {
            return false;
        }

        @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
        public void swingMotionGesture(String motionGesture) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSwingEventNotifier {
        private static final String DESCRIPTOR = "com.huawei.systemserver.swing.IHwSwingEventNotifier";
        static final int TRANSACTION_getKeyguardNotificationInfoList = 2;
        static final int TRANSACTION_isMusicLockScreenStyle = 3;
        static final int TRANSACTION_swingMotionGesture = 4;
        static final int TRANSACTION_swingNotificationState = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSwingEventNotifier asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSwingEventNotifier)) {
                return new Proxy(obj);
            }
            return (IHwSwingEventNotifier) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                swingNotificationState(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result = getKeyguardNotificationInfoList();
                reply.writeNoException();
                reply.writeStringList(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean isMusicLockScreenStyle = isMusicLockScreenStyle();
                reply.writeNoException();
                reply.writeInt(isMusicLockScreenStyle ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                swingMotionGesture(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSwingEventNotifier {
            public static IHwSwingEventNotifier sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
            public void swingNotificationState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().swingNotificationState(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
            public List<String> getKeyguardNotificationInfoList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyguardNotificationInfoList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
            public boolean isMusicLockScreenStyle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMusicLockScreenStyle();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.systemserver.swing.IHwSwingEventNotifier
            public void swingMotionGesture(String motionGesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(motionGesture);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().swingMotionGesture(motionGesture);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSwingEventNotifier impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSwingEventNotifier getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

package android.net.wifi.p2p;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiP2pManager extends IInterface {

    public static abstract class Stub extends Binder implements IWifiP2pManager {
        private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
        static final int TRANSACTION_getMessenger = 1;
        static final int TRANSACTION_getP2pStateMachineMessenger = 2;
        static final int TRANSACTION_isWifiP2pEnabled = 5;
        static final int TRANSACTION_setMiracastMode = 3;
        static final int TRANSACTION_setRecoveryWifiFlag = 6;
        static final int TRANSACTION_setWifiP2pEnabled = 4;

        private static class Proxy implements IWifiP2pManager {
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

            public Messenger getMessenger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Messenger messenger;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMessenger, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        messenger = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return messenger;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Messenger getP2pStateMachineMessenger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Messenger messenger;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getP2pStateMachineMessenger, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        messenger = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return messenger;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMiracastMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_setMiracastMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setWifiP2pEnabled(int p2pFlag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(p2pFlag);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiP2pEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWifiP2pEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isWifiP2pEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRecoveryWifiFlag(boolean flag) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = Stub.TRANSACTION_getMessenger;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRecoveryWifiFlag, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiP2pManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiP2pManager)) {
                return new Proxy(obj);
            }
            return (IWifiP2pManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            Messenger _result;
            boolean _result2;
            switch (code) {
                case TRANSACTION_getMessenger /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMessenger();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getMessenger);
                        _result.writeToParcel(reply, TRANSACTION_getMessenger);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getP2pStateMachineMessenger /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getP2pStateMachineMessenger();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getMessenger);
                        _result.writeToParcel(reply, TRANSACTION_getMessenger);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setMiracastMode /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMiracastMode(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setWifiP2pEnabled /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setWifiP2pEnabled(data.readInt());
                    reply.writeNoException();
                    if (_result2) {
                        i = TRANSACTION_getMessenger;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_isWifiP2pEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isWifiP2pEnabled();
                    reply.writeNoException();
                    if (_result2) {
                        i = TRANSACTION_getMessenger;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_setRecoveryWifiFlag /*6*/:
                    boolean _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    } else {
                        _arg0 = false;
                    }
                    setRecoveryWifiFlag(_arg0);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Messenger getMessenger() throws RemoteException;

    Messenger getP2pStateMachineMessenger() throws RemoteException;

    boolean isWifiP2pEnabled() throws RemoteException;

    void setMiracastMode(int i) throws RemoteException;

    void setRecoveryWifiFlag(boolean z) throws RemoteException;

    boolean setWifiP2pEnabled(int i) throws RemoteException;
}

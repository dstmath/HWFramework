package android.app.mtm;

import android.app.mtm.iaware.RSceneData;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.RPolicyData;

public interface IMultiTaskManagerService extends IInterface {

    public static abstract class Stub extends Binder implements IMultiTaskManagerService {
        private static final String DESCRIPTOR = "android.app.mtm.IMultiTaskManagerService";
        static final int TRANSACTION_acquirePolicyData = 9;
        static final int TRANSACTION_forcestopApps = 7;
        static final int TRANSACTION_getMultiTaskPolicy = 3;
        static final int TRANSACTION_killProcess = 5;
        static final int TRANSACTION_notifyProcessGroupChange = 6;
        static final int TRANSACTION_notifyResourceStatusOverload = 4;
        static final int TRANSACTION_registerObserver = 1;
        static final int TRANSACTION_reportScene = 8;
        static final int TRANSACTION_unregisterObserver = 2;

        private static class Proxy implements IMultiTaskManagerService {
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

            public void registerObserver(IMultiTaskProcessObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerObserver, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterObserver(IMultiTaskProcessObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterObserver, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MultiTaskPolicy getMultiTaskPolicy(int resourcetype, String resourceextend, int resourcestatus, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MultiTaskPolicy multiTaskPolicy;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourcetype);
                    _data.writeString(resourceextend);
                    _data.writeInt(resourcestatus);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getMultiTaskPolicy, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        multiTaskPolicy = (MultiTaskPolicy) MultiTaskPolicy.CREATOR.createFromParcel(_reply);
                    } else {
                        multiTaskPolicy = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return multiTaskPolicy;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourcetype);
                    _data.writeString(resourceextend);
                    _data.writeInt(resourcestatus);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyResourceStatusOverload, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killProcess(int pid, boolean restartservice) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    if (restartservice) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_killProcess, _data, _reply, 0);
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

            public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_notifyProcessGroupChange, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean forcestopApps(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(Stub.TRANSACTION_forcestopApps, _data, _reply, 0);
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

            public boolean reportScene(int featureId, RSceneData scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    if (scene != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        scene.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_reportScene, _data, _reply, 0);
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

            public RPolicyData acquirePolicyData(int featureId, RSceneData scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    RPolicyData rPolicyData;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    if (scene != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        scene.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_acquirePolicyData, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        rPolicyData = (RPolicyData) RPolicyData.CREATOR.createFromParcel(_reply);
                    } else {
                        rPolicyData = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return rPolicyData;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiTaskManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiTaskManagerService)) {
                return new Proxy(obj);
            }
            return (IMultiTaskManagerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            String _arg1;
            int _arg2;
            Bundle bundle;
            boolean _result;
            RSceneData rSceneData;
            switch (code) {
                case TRANSACTION_registerObserver /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerObserver(android.app.mtm.IMultiTaskProcessObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterObserver /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterObserver(android.app.mtm.IMultiTaskProcessObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMultiTaskPolicy /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    MultiTaskPolicy _result2 = getMultiTaskPolicy(_arg0, _arg1, _arg2, bundle);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result2.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_notifyResourceStatusOverload /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    notifyResourceStatusOverload(_arg0, _arg1, _arg2, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_killProcess /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = killProcess(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_notifyProcessGroupChange /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyProcessGroupChange(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forcestopApps /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = forcestopApps(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_reportScene /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        rSceneData = (RSceneData) RSceneData.CREATOR.createFromParcel(data);
                    } else {
                        rSceneData = null;
                    }
                    _result = reportScene(_arg0, rSceneData);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_acquirePolicyData /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        rSceneData = (RSceneData) RSceneData.CREATOR.createFromParcel(data);
                    } else {
                        rSceneData = null;
                    }
                    RPolicyData _result3 = acquirePolicyData(_arg0, rSceneData);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result3.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    RPolicyData acquirePolicyData(int i, RSceneData rSceneData) throws RemoteException;

    boolean forcestopApps(int i) throws RemoteException;

    MultiTaskPolicy getMultiTaskPolicy(int i, String str, int i2, Bundle bundle) throws RemoteException;

    boolean killProcess(int i, boolean z) throws RemoteException;

    void notifyProcessGroupChange(int i, int i2) throws RemoteException;

    void notifyResourceStatusOverload(int i, String str, int i2, Bundle bundle) throws RemoteException;

    void registerObserver(IMultiTaskProcessObserver iMultiTaskProcessObserver) throws RemoteException;

    boolean reportScene(int i, RSceneData rSceneData) throws RemoteException;

    void unregisterObserver(IMultiTaskProcessObserver iMultiTaskProcessObserver) throws RemoteException;
}

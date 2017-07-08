package com.huawei.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IPGSdk extends IInterface {

    public static abstract class Stub extends Binder implements IPGSdk {
        private static final String DESCRIPTOR = "com.huawei.pgmng.plug.IPGSdk";
        static final int TRANSACTION_checkStateByPid = 1;
        static final int TRANSACTION_checkStateByPkg = 2;
        static final int TRANSACTION_disableStateEvent = 12;
        static final int TRANSACTION_enableStateEvent = 11;
        static final int TRANSACTION_getHibernateApps = 4;
        static final int TRANSACTION_getPkgType = 3;
        static final int TRANSACTION_getSensorInfoByUid = 13;
        static final int TRANSACTION_getSupportedStates = 7;
        static final int TRANSACTION_getThermalInfo = 14;
        static final int TRANSACTION_getTopFrontApp = 6;
        static final int TRANSACTION_hibernateApps = 5;
        static final int TRANSACTION_isStateSupported = 8;
        static final int TRANSACTION_registerSink = 9;
        static final int TRANSACTION_unregisterSink = 10;

        private static class Proxy implements IPGSdk {
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

            public boolean checkStateByPid(String callingPkg, int pid, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(pid);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_checkStateByPid, _data, _reply, 0);
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

            public boolean checkStateByPkg(String callingPkg, String pkg, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_checkStateByPkg, _data, _reply, 0);
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

            public int getPkgType(String callingPkg, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getPkgType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getHibernateApps(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getHibernateApps, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hibernateApps(String callingPkg, List<String> pkgNames, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeStringList(pkgNames);
                    _data.writeString(reason);
                    this.mRemote.transact(Stub.TRANSACTION_hibernateApps, _data, _reply, 0);
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

            public String getTopFrontApp(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getTopFrontApp, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getSupportedStates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSupportedStates, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStateSupported(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    this.mRemote.transact(Stub.TRANSACTION_isStateSupported, _data, _reply, 0);
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

            public boolean registerSink(IStateRecognitionSink sink) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sink != null) {
                        iBinder = sink.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerSink, _data, _reply, 0);
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

            public boolean unregisterSink(IStateRecognitionSink sink) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sink != null) {
                        iBinder = sink.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterSink, _data, _reply, 0);
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

            public boolean enableStateEvent(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    this.mRemote.transact(Stub.TRANSACTION_enableStateEvent, _data, _reply, 0);
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

            public boolean disableStateEvent(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    this.mRemote.transact(Stub.TRANSACTION_disableStateEvent, _data, _reply, 0);
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

            public Map getSensorInfoByUid(String callingPkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getSensorInfoByUid, _data, _reply, 0);
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getThermalInfo(String callingPkg, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_getThermalInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPGSdk asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPGSdk)) {
                return new Proxy(obj);
            }
            return (IPGSdk) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case TRANSACTION_checkStateByPid /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkStateByPid(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_checkStateByPkg /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkStateByPkg(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_getPkgType /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPkgType(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getHibernateApps /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _result3 = getHibernateApps(data.readString());
                    reply.writeNoException();
                    reply.writeStringList(_result3);
                    return true;
                case TRANSACTION_hibernateApps /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hibernateApps(data.readString(), data.createStringArrayList(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_getTopFrontApp /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result4 = getTopFrontApp(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getSupportedStates /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result5 = getSupportedStates();
                    reply.writeNoException();
                    reply.writeIntArray(_result5);
                    return true;
                case TRANSACTION_isStateSupported /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isStateSupported(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_registerSink /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerSink(com.huawei.pgmng.plug.IStateRecognitionSink.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_unregisterSink /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unregisterSink(com.huawei.pgmng.plug.IStateRecognitionSink.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_enableStateEvent /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableStateEvent(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_disableStateEvent /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableStateEvent(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkStateByPid : 0);
                    return true;
                case TRANSACTION_getSensorInfoByUid /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    Map _result6 = getSensorInfoByUid(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeMap(_result6);
                    return true;
                case TRANSACTION_getThermalInfo /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getThermalInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean checkStateByPid(String str, int i, int i2) throws RemoteException;

    boolean checkStateByPkg(String str, String str2, int i) throws RemoteException;

    boolean disableStateEvent(int i) throws RemoteException;

    boolean enableStateEvent(int i) throws RemoteException;

    List<String> getHibernateApps(String str) throws RemoteException;

    int getPkgType(String str, String str2) throws RemoteException;

    Map getSensorInfoByUid(String str, int i) throws RemoteException;

    int[] getSupportedStates() throws RemoteException;

    int getThermalInfo(String str, int i) throws RemoteException;

    String getTopFrontApp(String str) throws RemoteException;

    boolean hibernateApps(String str, List<String> list, String str2) throws RemoteException;

    boolean isStateSupported(int i) throws RemoteException;

    boolean registerSink(IStateRecognitionSink iStateRecognitionSink) throws RemoteException;

    boolean unregisterSink(IStateRecognitionSink iStateRecognitionSink) throws RemoteException;
}

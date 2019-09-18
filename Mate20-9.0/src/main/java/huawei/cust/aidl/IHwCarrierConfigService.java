package huawei.cust.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHwCarrierConfigService extends IInterface {

    public static abstract class Stub extends Binder implements IHwCarrierConfigService {
        private static final String DESCRIPTOR = "huawei.cust.aidl.IHwCarrierConfigService";
        static final int TRANSACTION_getConfigForSlotId = 4;
        static final int TRANSACTION_getOpKey = 3;
        static final int TRANSACTION_querySimMatchRule = 1;
        static final int TRANSACTION_updateSimFileInfo = 2;

        private static class Proxy implements IHwCarrierConfigService {
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

            public SimMatchRule querySimMatchRule(String mccmnc, String iccid, String imsi, int slotId) throws RemoteException {
                SimMatchRule _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mccmnc);
                    _data.writeString(iccid);
                    _data.writeString(imsi);
                    _data.writeInt(slotId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SimMatchRule.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateSimFileInfo(SimFileInfo simFileInfo, int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (simFileInfo != null) {
                        _data.writeInt(1);
                        simFileInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(slotId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getOpKey(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getConfigForSlotId(String key, int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(slotId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwCarrierConfigService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwCarrierConfigService)) {
                return new Proxy(obj);
            }
            return (IHwCarrierConfigService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SimFileInfo _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        SimMatchRule _result = querySimMatchRule(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SimFileInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        updateSimFileInfo(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getOpKey(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result3 = getConfigForSlotId(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result3);
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

    Map getConfigForSlotId(String str, int i) throws RemoteException;

    String getOpKey(int i) throws RemoteException;

    SimMatchRule querySimMatchRule(String str, String str2, String str3, int i) throws RemoteException;

    void updateSimFileInfo(SimFileInfo simFileInfo, int i) throws RemoteException;
}

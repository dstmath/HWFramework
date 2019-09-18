package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPwdProtectManager extends IInterface {

    public static abstract class Stub extends Binder implements IPwdProtectManager {
        private static final String DESCRIPTOR = "huawei.android.security.IPwdProtectManager";
        static final int TRANSACTION_decodeCurrentPwd = 5;
        static final int TRANSACTION_getPwdQuestion = 8;
        static final int TRANSACTION_hasKeyFileExisted = 1;
        static final int TRANSACTION_modifyMainPwd = 4;
        static final int TRANSACTION_modifyPrivPwd = 3;
        static final int TRANSACTION_pwdQAnswerVertify = 7;
        static final int TRANSACTION_removeKeyFile = 2;
        static final int TRANSACTION_startPwdProtect = 6;

        private static class Proxy implements IPwdProtectManager {
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

            public boolean hasKeyFileExisted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeKeyFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean modifyPrivPwd(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean modifyMainPwd(String origPassword, String newPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(origPassword);
                    _data.writeString(newPassword);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String decodeCurrentPwd(String mainSpacePin, String answer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mainSpacePin);
                    _data.writeString(answer);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startPwdProtect(String privSpacePin, String question, String answer, String mainSpacePin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(privSpacePin);
                    _data.writeString(question);
                    _data.writeString(answer);
                    _data.writeString(mainSpacePin);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean pwdQAnswerVertify(byte[] pwQuestion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(pwQuestion);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPwdQuestion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPwdProtectManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPwdProtectManager)) {
                return new Proxy(obj);
            }
            return (IPwdProtectManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = hasKeyFileExisted();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = removeKeyFile();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = modifyPrivPwd(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = modifyMainPwd(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = decodeCurrentPwd(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = startPwdProtect(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result7 = pwdQAnswerVertify(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getPwdQuestion();
                        reply.writeNoException();
                        reply.writeString(_result8);
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

    String decodeCurrentPwd(String str, String str2) throws RemoteException;

    String getPwdQuestion() throws RemoteException;

    boolean hasKeyFileExisted() throws RemoteException;

    boolean modifyMainPwd(String str, String str2) throws RemoteException;

    boolean modifyPrivPwd(String str) throws RemoteException;

    boolean pwdQAnswerVertify(byte[] bArr) throws RemoteException;

    boolean removeKeyFile() throws RemoteException;

    boolean startPwdProtect(String str, String str2, String str3, String str4) throws RemoteException;
}

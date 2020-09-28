package com.mediatek.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.ims.internal.IMtkImsUtListener;

public interface IMtkImsUt extends IInterface {
    String getUtIMPUFromNetwork() throws RemoteException;

    String getXcapConflictErrorMessage() throws RemoteException;

    boolean isSupportCFT() throws RemoteException;

    void processECT(Message message, Messenger messenger) throws RemoteException;

    int queryCFForServiceClass(int i, String str, int i2) throws RemoteException;

    int queryCallForwardInTimeSlot(int i) throws RemoteException;

    void setListener(IMtkImsUtListener iMtkImsUtListener) throws RemoteException;

    void setupXcapUserAgentString(String str) throws RemoteException;

    int updateCallBarringForServiceClass(String str, int i, int i2, String[] strArr, int i3) throws RemoteException;

    int updateCallForwardInTimeSlot(int i, int i2, String str, int i3, long[] jArr) throws RemoteException;

    public static class Default implements IMtkImsUt {
        @Override // com.mediatek.ims.internal.IMtkImsUt
        public void setListener(IMtkImsUtListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public String getUtIMPUFromNetwork() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public int queryCallForwardInTimeSlot(int condition) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public int updateCallForwardInTimeSlot(int action, int condition, String number, int timeSeconds, long[] timeSlot) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public int updateCallBarringForServiceClass(String password, int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public void processECT(Message result, Messenger target) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public boolean isSupportCFT() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public void setupXcapUserAgentString(String userAgent) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public String getXcapConflictErrorMessage() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsUt
        public int queryCFForServiceClass(int condition, String number, int serviceClass) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsUt {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsUt";
        static final int TRANSACTION_getUtIMPUFromNetwork = 2;
        static final int TRANSACTION_getXcapConflictErrorMessage = 9;
        static final int TRANSACTION_isSupportCFT = 7;
        static final int TRANSACTION_processECT = 6;
        static final int TRANSACTION_queryCFForServiceClass = 10;
        static final int TRANSACTION_queryCallForwardInTimeSlot = 3;
        static final int TRANSACTION_setListener = 1;
        static final int TRANSACTION_setupXcapUserAgentString = 8;
        static final int TRANSACTION_updateCallBarringForServiceClass = 5;
        static final int TRANSACTION_updateCallForwardInTimeSlot = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsUt asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsUt)) {
                return new Proxy(obj);
            }
            return (IMtkImsUt) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Message _arg0;
            Messenger _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setListener(IMtkImsUtListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getUtIMPUFromNetwork();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = queryCallForwardInTimeSlot(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = updateCallForwardInTimeSlot(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.createLongArray());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = updateCallBarringForServiceClass(data.readString(), data.readInt(), data.readInt(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        processECT(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportCFT = isSupportCFT();
                        reply.writeNoException();
                        reply.writeInt(isSupportCFT ? 1 : 0);
                        return true;
                    case TRANSACTION_setupXcapUserAgentString /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setupXcapUserAgentString(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getXcapConflictErrorMessage /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getXcapConflictErrorMessage();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case TRANSACTION_queryCFForServiceClass /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = queryCFForServiceClass(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMtkImsUt {
            public static IMtkImsUt sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public void setListener(IMtkImsUtListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public String getUtIMPUFromNetwork() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUtIMPUFromNetwork();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public int queryCallForwardInTimeSlot(int condition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(condition);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCallForwardInTimeSlot(condition);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public int updateCallForwardInTimeSlot(int action, int condition, String number, int timeSeconds, long[] timeSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    _data.writeInt(timeSeconds);
                    _data.writeLongArray(timeSlot);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCallForwardInTimeSlot(action, condition, number, timeSeconds, timeSlot);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public int updateCallBarringForServiceClass(String password, int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeInt(cbType);
                    _data.writeInt(action);
                    _data.writeStringArray(barrList);
                    _data.writeInt(serviceClass);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCallBarringForServiceClass(password, cbType, action, barrList, serviceClass);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public void processECT(Message result, Messenger target) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (target != null) {
                        _data.writeInt(1);
                        target.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().processECT(result, target);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public boolean isSupportCFT() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportCFT();
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

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public void setupXcapUserAgentString(String userAgent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(userAgent);
                    if (this.mRemote.transact(Stub.TRANSACTION_setupXcapUserAgentString, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setupXcapUserAgentString(userAgent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public String getXcapConflictErrorMessage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getXcapConflictErrorMessage, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getXcapConflictErrorMessage();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUt
            public int queryCFForServiceClass(int condition, String number, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    _data.writeInt(serviceClass);
                    if (!this.mRemote.transact(Stub.TRANSACTION_queryCFForServiceClass, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCFForServiceClass(condition, number, serviceClass);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsUt impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsUt getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

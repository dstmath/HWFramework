package com.huawei.android.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwDisplayManager extends IInterface {
    void checkVerificationResult(boolean z) throws RemoteException;

    void connectWifiDisplay(String str, HwWifiDisplayParameters hwWifiDisplayParameters) throws RemoteException;

    boolean createVrDisplay(String str, int[] iArr) throws RemoteException;

    boolean destroyAllVrDisplay() throws RemoteException;

    boolean destroyVrDisplay(String str) throws RemoteException;

    HwWifiDisplayParameters getHwWifiDisplayParameters() throws RemoteException;

    boolean sendWifiDisplayAction(String str) throws RemoteException;

    void startWifiDisplayScan(int i) throws RemoteException;

    public static class Default implements IHwDisplayManager {
        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public void startWifiDisplayScan(int channelId) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public void connectWifiDisplay(String address, HwWifiDisplayParameters parameters) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public void checkVerificationResult(boolean isRight) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public boolean sendWifiDisplayAction(String action) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public boolean createVrDisplay(String displayName, int[] displayParams) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public boolean destroyVrDisplay(String displayName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public boolean destroyAllVrDisplay() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.hardware.display.IHwDisplayManager
        public HwWifiDisplayParameters getHwWifiDisplayParameters() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwDisplayManager {
        private static final String DESCRIPTOR = "com.huawei.android.hardware.display.IHwDisplayManager";
        static final int TRANSACTION_checkVerificationResult = 3;
        static final int TRANSACTION_connectWifiDisplay = 2;
        static final int TRANSACTION_createVrDisplay = 5;
        static final int TRANSACTION_destroyAllVrDisplay = 7;
        static final int TRANSACTION_destroyVrDisplay = 6;
        static final int TRANSACTION_getHwWifiDisplayParameters = 8;
        static final int TRANSACTION_sendWifiDisplayAction = 4;
        static final int TRANSACTION_startWifiDisplayScan = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDisplayManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDisplayManager)) {
                return new Proxy(obj);
            }
            return (IHwDisplayManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startWifiDisplayScan";
                case 2:
                    return "connectWifiDisplay";
                case 3:
                    return "checkVerificationResult";
                case 4:
                    return "sendWifiDisplayAction";
                case 5:
                    return "createVrDisplay";
                case 6:
                    return "destroyVrDisplay";
                case 7:
                    return "destroyAllVrDisplay";
                case 8:
                    return "getHwWifiDisplayParameters";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwWifiDisplayParameters _arg1;
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        startWifiDisplayScan(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = HwWifiDisplayParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        connectWifiDisplay(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        checkVerificationResult(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean sendWifiDisplayAction = sendWifiDisplayAction(data.readString());
                        reply.writeNoException();
                        reply.writeInt(sendWifiDisplayAction ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean createVrDisplay = createVrDisplay(data.readString(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(createVrDisplay ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean destroyVrDisplay = destroyVrDisplay(data.readString());
                        reply.writeNoException();
                        reply.writeInt(destroyVrDisplay ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean destroyAllVrDisplay = destroyAllVrDisplay();
                        reply.writeNoException();
                        reply.writeInt(destroyAllVrDisplay ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        HwWifiDisplayParameters _result = getHwWifiDisplayParameters();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements IHwDisplayManager {
            public static IHwDisplayManager sDefaultImpl;
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

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public void startWifiDisplayScan(int channelId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channelId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWifiDisplayScan(channelId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public void connectWifiDisplay(String address, HwWifiDisplayParameters parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    if (parameters != null) {
                        _data.writeInt(1);
                        parameters.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().connectWifiDisplay(address, parameters);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public void checkVerificationResult(boolean isRight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isRight ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().checkVerificationResult(isRight);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public boolean sendWifiDisplayAction(String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendWifiDisplayAction(action);
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

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public boolean createVrDisplay(String displayName, int[] displayParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(displayName);
                    _data.writeIntArray(displayParams);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createVrDisplay(displayName, displayParams);
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

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public boolean destroyVrDisplay(String displayName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(displayName);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().destroyVrDisplay(displayName);
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

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public boolean destroyAllVrDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().destroyAllVrDisplay();
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

            @Override // com.huawei.android.hardware.display.IHwDisplayManager
            public HwWifiDisplayParameters getHwWifiDisplayParameters() throws RemoteException {
                HwWifiDisplayParameters _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwWifiDisplayParameters();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwWifiDisplayParameters.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwDisplayManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwDisplayManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

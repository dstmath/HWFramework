package android.net.wifi;

import android.net.wifi.IPnoScanEvent;
import android.net.wifi.IScanEvent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.wifi.wificond.NativeScanResult;
import com.android.server.wifi.wificond.PnoSettings;
import com.android.server.wifi.wificond.SingleScanSettings;

public interface IWifiScannerImpl extends IInterface {
    public static final int SCAN_TYPE_DEFAULT = -1;
    public static final int SCAN_TYPE_HIGH_ACCURACY = 2;
    public static final int SCAN_TYPE_LOW_POWER = 1;
    public static final int SCAN_TYPE_LOW_SPAN = 0;

    void abortScan() throws RemoteException;

    NativeScanResult[] getPnoScanResults() throws RemoteException;

    NativeScanResult[] getScanResults() throws RemoteException;

    boolean scan(SingleScanSettings singleScanSettings) throws RemoteException;

    boolean startPnoScan(PnoSettings pnoSettings) throws RemoteException;

    boolean stopPnoScan() throws RemoteException;

    void subscribePnoScanEvents(IPnoScanEvent iPnoScanEvent) throws RemoteException;

    void subscribeScanEvents(IScanEvent iScanEvent) throws RemoteException;

    void unsubscribePnoScanEvents() throws RemoteException;

    void unsubscribeScanEvents() throws RemoteException;

    public static class Default implements IWifiScannerImpl {
        @Override // android.net.wifi.IWifiScannerImpl
        public NativeScanResult[] getScanResults() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public NativeScanResult[] getPnoScanResults() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public boolean scan(SingleScanSettings scanSettings) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public void subscribeScanEvents(IScanEvent handler) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public void unsubscribeScanEvents() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public void subscribePnoScanEvents(IPnoScanEvent handler) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public void unsubscribePnoScanEvents() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public boolean startPnoScan(PnoSettings pnoSettings) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public boolean stopPnoScan() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiScannerImpl
        public void abortScan() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiScannerImpl {
        private static final String DESCRIPTOR = "android.net.wifi.IWifiScannerImpl";
        static final int TRANSACTION_abortScan = 10;
        static final int TRANSACTION_getPnoScanResults = 2;
        static final int TRANSACTION_getScanResults = 1;
        static final int TRANSACTION_scan = 3;
        static final int TRANSACTION_startPnoScan = 8;
        static final int TRANSACTION_stopPnoScan = 9;
        static final int TRANSACTION_subscribePnoScanEvents = 6;
        static final int TRANSACTION_subscribeScanEvents = 4;
        static final int TRANSACTION_unsubscribePnoScanEvents = 7;
        static final int TRANSACTION_unsubscribeScanEvents = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiScannerImpl asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiScannerImpl)) {
                return new Proxy(obj);
            }
            return (IWifiScannerImpl) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SingleScanSettings _arg0;
            PnoSettings _arg02;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        NativeScanResult[] _result = getScanResults();
                        reply.writeNoException();
                        reply.writeTypedArray(_result, 1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        NativeScanResult[] _result2 = getPnoScanResults();
                        reply.writeNoException();
                        reply.writeTypedArray(_result2, 1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SingleScanSettings.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean scan = scan(_arg0);
                        reply.writeNoException();
                        reply.writeInt(scan ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        subscribeScanEvents(IScanEvent.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unsubscribeScanEvents();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        subscribePnoScanEvents(IPnoScanEvent.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        unsubscribePnoScanEvents();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = PnoSettings.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean startPnoScan = startPnoScan(_arg02);
                        reply.writeNoException();
                        reply.writeInt(startPnoScan ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopPnoScan = stopPnoScan();
                        reply.writeNoException();
                        reply.writeInt(stopPnoScan ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        abortScan();
                        reply.writeNoException();
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
        public static class Proxy implements IWifiScannerImpl {
            public static IWifiScannerImpl sDefaultImpl;
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

            @Override // android.net.wifi.IWifiScannerImpl
            public NativeScanResult[] getScanResults() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScanResults();
                    }
                    _reply.readException();
                    NativeScanResult[] _result = (NativeScanResult[]) _reply.createTypedArray(NativeScanResult.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public NativeScanResult[] getPnoScanResults() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPnoScanResults();
                    }
                    _reply.readException();
                    NativeScanResult[] _result = (NativeScanResult[]) _reply.createTypedArray(NativeScanResult.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public boolean scan(SingleScanSettings scanSettings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (scanSettings != null) {
                        _data.writeInt(1);
                        scanSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().scan(scanSettings);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public void subscribeScanEvents(IScanEvent handler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(handler != null ? handler.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().subscribeScanEvents(handler);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public void unsubscribeScanEvents() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unsubscribeScanEvents();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public void subscribePnoScanEvents(IPnoScanEvent handler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(handler != null ? handler.asBinder() : null);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().subscribePnoScanEvents(handler);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public void unsubscribePnoScanEvents() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unsubscribePnoScanEvents();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public boolean startPnoScan(PnoSettings pnoSettings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (pnoSettings != null) {
                        _data.writeInt(1);
                        pnoSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startPnoScan(pnoSettings);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiScannerImpl
            public boolean stopPnoScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopPnoScan();
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

            @Override // android.net.wifi.IWifiScannerImpl
            public void abortScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().abortScan();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiScannerImpl impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiScannerImpl getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

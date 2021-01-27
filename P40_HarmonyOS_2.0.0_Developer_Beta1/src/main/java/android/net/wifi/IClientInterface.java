package android.net.wifi;

import android.net.wifi.IHwVendorEvent;
import android.net.wifi.ISendMgmtFrameEvent;
import android.net.wifi.IWifiScannerImpl;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IClientInterface extends IInterface {
    void SendMgmtFrame(byte[] bArr, ISendMgmtFrameEvent iSendMgmtFrameEvent, int i) throws RemoteException;

    void SubscribeVendorEvents(IHwVendorEvent iHwVendorEvent) throws RemoteException;

    void UnsubscribeVendorEvents() throws RemoteException;

    String getInterfaceName() throws RemoteException;

    byte[] getMacAddress() throws RemoteException;

    int[] getPacketCounters() throws RemoteException;

    IWifiScannerImpl getWifiScannerImpl() throws RemoteException;

    boolean setMacAddress(byte[] bArr) throws RemoteException;

    int[] signalPoll() throws RemoteException;

    public static class Default implements IClientInterface {
        @Override // android.net.wifi.IClientInterface
        public int[] getPacketCounters() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IClientInterface
        public int[] signalPoll() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IClientInterface
        public byte[] getMacAddress() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IClientInterface
        public String getInterfaceName() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IClientInterface
        public IWifiScannerImpl getWifiScannerImpl() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IClientInterface
        public boolean setMacAddress(byte[] mac) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IClientInterface
        public void SendMgmtFrame(byte[] frame, ISendMgmtFrameEvent callback, int mcs) throws RemoteException {
        }

        @Override // android.net.wifi.IClientInterface
        public void SubscribeVendorEvents(IHwVendorEvent handler) throws RemoteException {
        }

        @Override // android.net.wifi.IClientInterface
        public void UnsubscribeVendorEvents() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IClientInterface {
        private static final String DESCRIPTOR = "android.net.wifi.IClientInterface";
        static final int TRANSACTION_SendMgmtFrame = 7;
        static final int TRANSACTION_SubscribeVendorEvents = 8;
        static final int TRANSACTION_UnsubscribeVendorEvents = 9;
        static final int TRANSACTION_getInterfaceName = 4;
        static final int TRANSACTION_getMacAddress = 3;
        static final int TRANSACTION_getPacketCounters = 1;
        static final int TRANSACTION_getWifiScannerImpl = 5;
        static final int TRANSACTION_setMacAddress = 6;
        static final int TRANSACTION_signalPoll = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IClientInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IClientInterface)) {
                return new Proxy(obj);
            }
            return (IClientInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result = getPacketCounters();
                        reply.writeNoException();
                        reply.writeIntArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result2 = signalPoll();
                        reply.writeNoException();
                        reply.writeIntArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result3 = getMacAddress();
                        reply.writeNoException();
                        reply.writeByteArray(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getInterfaceName();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IWifiScannerImpl _result5 = getWifiScannerImpl();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean macAddress = setMacAddress(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(macAddress ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        SendMgmtFrame(data.createByteArray(), ISendMgmtFrameEvent.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        SubscribeVendorEvents(IHwVendorEvent.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        UnsubscribeVendorEvents();
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
        public static class Proxy implements IClientInterface {
            public static IClientInterface sDefaultImpl;
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

            @Override // android.net.wifi.IClientInterface
            public int[] getPacketCounters() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPacketCounters();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IClientInterface
            public int[] signalPoll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().signalPoll();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IClientInterface
            public byte[] getMacAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMacAddress();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IClientInterface
            public String getInterfaceName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInterfaceName();
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

            @Override // android.net.wifi.IClientInterface
            public IWifiScannerImpl getWifiScannerImpl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiScannerImpl();
                    }
                    _reply.readException();
                    IWifiScannerImpl _result = IWifiScannerImpl.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IClientInterface
            public boolean setMacAddress(byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(mac);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMacAddress(mac);
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

            @Override // android.net.wifi.IClientInterface
            public void SendMgmtFrame(byte[] frame, ISendMgmtFrameEvent callback, int mcs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(frame);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(mcs);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().SendMgmtFrame(frame, callback, mcs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IClientInterface
            public void SubscribeVendorEvents(IHwVendorEvent handler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(handler != null ? handler.asBinder() : null);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().SubscribeVendorEvents(handler);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IClientInterface
            public void UnsubscribeVendorEvents() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().UnsubscribeVendorEvents();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IClientInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IClientInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

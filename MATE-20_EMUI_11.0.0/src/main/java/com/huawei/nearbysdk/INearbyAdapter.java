package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nearbysdk.ICreateSocketListener;
import com.huawei.nearbysdk.IDevConnectListen;
import com.huawei.nearbysdk.IDevFindListener;
import com.huawei.nearbysdk.IInternalConnectionListener;
import com.huawei.nearbysdk.IInternalSocketListener;
import com.huawei.nearbysdk.INearbyBroadcaster;
import com.huawei.nearbysdk.IP2pDevConnectListener;
import com.huawei.nearbysdk.IP2pDevFindListener;
import com.huawei.nearbysdk.IPublishListener;
import com.huawei.nearbysdk.ISubscribeListener;
import com.huawei.nearbysdk.closeRange.CloseRangeBusinessType;
import com.huawei.nearbysdk.closeRange.CloseRangeDeviceFilter;
import com.huawei.nearbysdk.closeRange.CloseRangeEventFilter;
import com.huawei.nearbysdk.closeRange.ICloseRangeDeviceListener;
import com.huawei.nearbysdk.closeRange.ICloseRangeEventListener;

public interface INearbyAdapter extends IInterface {
    void cancelConnectP2pDev(IP2pDevConnectListener iP2pDevConnectListener) throws RemoteException;

    void close(int i, int i2, NearbyDevice nearbyDevice) throws RemoteException;

    void connectP2pDev(IP2pDevConnectListener iP2pDevConnectListener) throws RemoteException;

    boolean connectVendorDevice(NearbyDevice nearbyDevice, int i, IDevConnectListen iDevConnectListen) throws RemoteException;

    boolean disconnectVendorDevice(NearbyDevice nearbyDevice) throws RemoteException;

    boolean findVendorDevice(int i, int i2, IDevFindListener iDevFindListener) throws RemoteException;

    INearbyBroadcaster getBroadcaster() throws RemoteException;

    byte[] getDeviceHash() throws RemoteException;

    boolean hasInit() throws RemoteException;

    boolean open(int i, int i2, int i3, NearbyDevice nearbyDevice, int i4) throws RemoteException;

    boolean openNearbySocket(int i, int i2, int i3, String str, NearbyDevice nearbyDevice, int i4, ICreateSocketListener iCreateSocketListener) throws RemoteException;

    boolean publish(int i, int i2, int i3, IPublishListener iPublishListener) throws RemoteException;

    boolean registerConnectionListener(int i, int i2, NearbyConfiguration nearbyConfiguration, IInternalConnectionListener iInternalConnectionListener) throws RemoteException;

    boolean registerInternalSocketListener(int i, int i2, IInternalSocketListener iInternalSocketListener) throws RemoteException;

    boolean setFrequency(CloseRangeBusinessType closeRangeBusinessType, BleScanLevel bleScanLevel) throws RemoteException;

    void startP2pDiscovery(IP2pDevFindListener iP2pDevFindListener) throws RemoteException;

    boolean stopFindVendorDevice(int i, int i2) throws RemoteException;

    void stopP2pDiscovery(IP2pDevFindListener iP2pDevFindListener) throws RemoteException;

    boolean subscribe(boolean z, int i, ISubscribeListener iSubscribeListener) throws RemoteException;

    boolean subscribeDevice(CloseRangeDeviceFilter closeRangeDeviceFilter, ICloseRangeDeviceListener iCloseRangeDeviceListener) throws RemoteException;

    boolean subscribeEvent(CloseRangeEventFilter closeRangeEventFilter, ICloseRangeEventListener iCloseRangeEventListener) throws RemoteException;

    boolean unPublish(IPublishListener iPublishListener) throws RemoteException;

    boolean unRegisterConnectionListener(IInternalConnectionListener iInternalConnectionListener) throws RemoteException;

    boolean unRegisterInternalSocketListener(IInternalSocketListener iInternalSocketListener) throws RemoteException;

    boolean unSubscribe(ISubscribeListener iSubscribeListener) throws RemoteException;

    boolean unSubscribeDevice(CloseRangeDeviceFilter closeRangeDeviceFilter) throws RemoteException;

    boolean unSubscribeEvent(CloseRangeEventFilter closeRangeEventFilter) throws RemoteException;

    int write(int i, int i2, NearbyDevice nearbyDevice, byte[] bArr) throws RemoteException;

    public static abstract class Stub extends Binder implements INearbyAdapter {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.INearbyAdapter";
        static final int TRANSACTION_cancelConnectP2pDev = 26;
        static final int TRANSACTION_close = 12;
        static final int TRANSACTION_connectP2pDev = 27;
        static final int TRANSACTION_connectVendorDevice = 21;
        static final int TRANSACTION_disconnectVendorDevice = 22;
        static final int TRANSACTION_findVendorDevice = 19;
        static final int TRANSACTION_getBroadcaster = 23;
        static final int TRANSACTION_getDeviceHash = 28;
        static final int TRANSACTION_hasInit = 13;
        static final int TRANSACTION_open = 10;
        static final int TRANSACTION_openNearbySocket = 5;
        static final int TRANSACTION_publish = 1;
        static final int TRANSACTION_registerConnectionListener = 8;
        static final int TRANSACTION_registerInternalSocketListener = 6;
        static final int TRANSACTION_setFrequency = 18;
        static final int TRANSACTION_startP2pDiscovery = 24;
        static final int TRANSACTION_stopFindVendorDevice = 20;
        static final int TRANSACTION_stopP2pDiscovery = 25;
        static final int TRANSACTION_subscribe = 3;
        static final int TRANSACTION_subscribeDevice = 16;
        static final int TRANSACTION_subscribeEvent = 14;
        static final int TRANSACTION_unPublish = 2;
        static final int TRANSACTION_unRegisterConnectionListener = 9;
        static final int TRANSACTION_unRegisterInternalSocketListener = 7;
        static final int TRANSACTION_unSubscribe = 4;
        static final int TRANSACTION_unSubscribeDevice = 17;
        static final int TRANSACTION_unSubscribeEvent = 15;
        static final int TRANSACTION_write = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INearbyAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INearbyAdapter)) {
                return new Proxy(obj);
            }
            return (INearbyAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CloseRangeBusinessType _arg0;
            if (code != 1598968902) {
                NearbyDevice _arg4 = null;
                IBinder iBinder = null;
                NearbyDevice _arg02 = null;
                NearbyDevice _arg03 = null;
                BleScanLevel _arg1 = null;
                CloseRangeDeviceFilter _arg04 = null;
                CloseRangeDeviceFilter _arg05 = null;
                CloseRangeEventFilter _arg06 = null;
                CloseRangeEventFilter _arg07 = null;
                NearbyDevice _arg2 = null;
                NearbyDevice _arg22 = null;
                NearbyDevice _arg3 = null;
                NearbyConfiguration _arg23 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean publish = publish(data.readInt(), data.readInt(), data.readInt(), IPublishListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(publish ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unPublish = unPublish(IPublishListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unPublish ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean subscribe = subscribe(data.readInt() != 0, data.readInt(), ISubscribeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(subscribe ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unSubscribe = unSubscribe(ISubscribeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unSubscribe ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        int _arg12 = data.readInt();
                        int _arg24 = data.readInt();
                        String _arg32 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = NearbyDevice.CREATOR.createFromParcel(data);
                        }
                        boolean openNearbySocket = openNearbySocket(_arg08, _arg12, _arg24, _arg32, _arg4, data.readInt(), ICreateSocketListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(openNearbySocket ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerInternalSocketListener = registerInternalSocketListener(data.readInt(), data.readInt(), IInternalSocketListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerInternalSocketListener ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unRegisterInternalSocketListener = unRegisterInternalSocketListener(IInternalSocketListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unRegisterInternalSocketListener ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = NearbyConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean registerConnectionListener = registerConnectionListener(_arg09, _arg13, _arg23, IInternalConnectionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerConnectionListener ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unRegisterConnectionListener = unRegisterConnectionListener(IInternalConnectionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unRegisterConnectionListener ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        int _arg14 = data.readInt();
                        int _arg25 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = NearbyDevice.CREATOR.createFromParcel(data);
                        }
                        boolean open = open(_arg010, _arg14, _arg25, _arg3, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(open ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = NearbyDevice.CREATOR.createFromParcel(data);
                        }
                        int _result = write(_arg011, _arg15, _arg22, data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = NearbyDevice.CREATOR.createFromParcel(data);
                        }
                        close(_arg012, _arg16, _arg2);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasInit = hasInit();
                        reply.writeNoException();
                        reply.writeInt(hasInit ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = CloseRangeEventFilter.CREATOR.createFromParcel(data);
                        }
                        boolean subscribeEvent = subscribeEvent(_arg07, ICloseRangeEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(subscribeEvent ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = CloseRangeEventFilter.CREATOR.createFromParcel(data);
                        }
                        boolean unSubscribeEvent = unSubscribeEvent(_arg06);
                        reply.writeNoException();
                        reply.writeInt(unSubscribeEvent ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = CloseRangeDeviceFilter.CREATOR.createFromParcel(data);
                        }
                        boolean subscribeDevice = subscribeDevice(_arg05, ICloseRangeDeviceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(subscribeDevice ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = CloseRangeDeviceFilter.CREATOR.createFromParcel(data);
                        }
                        boolean unSubscribeDevice = unSubscribeDevice(_arg04);
                        reply.writeNoException();
                        reply.writeInt(unSubscribeDevice ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CloseRangeBusinessType.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = BleScanLevel.CREATOR.createFromParcel(data);
                        }
                        boolean frequency = setFrequency(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(frequency ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean findVendorDevice = findVendorDevice(data.readInt(), data.readInt(), IDevFindListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(findVendorDevice ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopFindVendorDevice = stopFindVendorDevice(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(stopFindVendorDevice ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = NearbyDevice.CREATOR.createFromParcel(data);
                        }
                        boolean connectVendorDevice = connectVendorDevice(_arg03, data.readInt(), IDevConnectListen.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(connectVendorDevice ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(data);
                        }
                        boolean disconnectVendorDevice = disconnectVendorDevice(_arg02);
                        reply.writeNoException();
                        reply.writeInt(disconnectVendorDevice ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        INearbyBroadcaster _result2 = getBroadcaster();
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        startP2pDiscovery(IP2pDevFindListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        stopP2pDiscovery(IP2pDevFindListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        cancelConnectP2pDev(IP2pDevConnectListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        connectP2pDev(IP2pDevConnectListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result3 = getDeviceHash();
                        reply.writeNoException();
                        reply.writeByteArray(_result3);
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
        public static class Proxy implements INearbyAdapter {
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean publish(int businessType, int businessId, int typeChannel, IPublishListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(businessId);
                    _data.writeInt(typeChannel);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean unPublish(IPublishListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean subscribe(boolean allowWakeupById, int businessId, ISubscribeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allowWakeupById ? 1 : 0);
                    _data.writeInt(businessId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean unSubscribe(ISubscribeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean openNearbySocket(int businessType, int channel, int businessId, String businessTag, NearbyDevice device, int timeout, ICreateSocketListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(channel);
                    _data.writeInt(businessId);
                    _data.writeString(businessTag);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeout);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean registerInternalSocketListener(int businessType, int businessId, IInternalSocketListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(businessId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean unRegisterInternalSocketListener(IInternalSocketListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean registerConnectionListener(int businessType, int businessId, NearbyConfiguration configuration, IInternalConnectionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(businessId);
                    boolean _result = true;
                    if (configuration != null) {
                        _data.writeInt(1);
                        configuration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean unRegisterConnectionListener(IInternalConnectionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean open(int businessType, int channelId, int businessId, NearbyDevice device, int timeoutMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(channelId);
                    _data.writeInt(businessId);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeoutMs);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public int write(int businessType, int businessId, NearbyDevice device, byte[] message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(businessId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(message);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public void close(int businessType, int businessId, NearbyDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeInt(businessId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean hasInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean subscribeEvent(CloseRangeEventFilter eventFilter, ICloseRangeEventListener eventListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (eventFilter != null) {
                        _data.writeInt(1);
                        eventFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(eventListener != null ? eventListener.asBinder() : null);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean unSubscribeEvent(CloseRangeEventFilter eventFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (eventFilter != null) {
                        _data.writeInt(1);
                        eventFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean subscribeDevice(CloseRangeDeviceFilter deviceFilter, ICloseRangeDeviceListener deviceListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (deviceFilter != null) {
                        _data.writeInt(1);
                        deviceFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(deviceListener != null ? deviceListener.asBinder() : null);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean unSubscribeDevice(CloseRangeDeviceFilter deviceFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (deviceFilter != null) {
                        _data.writeInt(1);
                        deviceFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean setFrequency(CloseRangeBusinessType type, BleScanLevel frequency) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (type != null) {
                        _data.writeInt(1);
                        type.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (frequency != null) {
                        _data.writeInt(1);
                        frequency.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean findVendorDevice(int manu, int devType, IDevFindListener listen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(manu);
                    _data.writeInt(devType);
                    _data.writeStrongBinder(listen != null ? listen.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean stopFindVendorDevice(int manu, int devType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(manu);
                    _data.writeInt(devType);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
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

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean connectVendorDevice(NearbyDevice dev, int timeout, IDevConnectListen listen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (dev != null) {
                        _data.writeInt(1);
                        dev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeout);
                    _data.writeStrongBinder(listen != null ? listen.asBinder() : null);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public boolean disconnectVendorDevice(NearbyDevice dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (dev != null) {
                        _data.writeInt(1);
                        dev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public INearbyBroadcaster getBroadcaster() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return INearbyBroadcaster.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public void startP2pDiscovery(IP2pDevFindListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public void stopP2pDiscovery(IP2pDevFindListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public void cancelConnectP2pDev(IP2pDevConnectListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public void connectP2pDev(IP2pDevConnectListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyAdapter
            public byte[] getDeviceHash() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}

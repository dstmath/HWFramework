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
import com.huawei.nearbysdk.IPublishListener;
import com.huawei.nearbysdk.ISubscribeListener;
import com.huawei.nearbysdk.closeRange.CloseRangeBusinessType;
import com.huawei.nearbysdk.closeRange.CloseRangeDeviceFilter;
import com.huawei.nearbysdk.closeRange.CloseRangeEventFilter;
import com.huawei.nearbysdk.closeRange.ICloseRangeDeviceListener;
import com.huawei.nearbysdk.closeRange.ICloseRangeEventListener;

public interface INearbyAdapter extends IInterface {

    public static abstract class Stub extends Binder implements INearbyAdapter {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.INearbyAdapter";
        static final int TRANSACTION_close = 12;
        static final int TRANSACTION_connectVendorDevice = 21;
        static final int TRANSACTION_disconnectVendorDevice = 22;
        static final int TRANSACTION_findVendorDevice = 19;
        static final int TRANSACTION_hasInit = 13;
        static final int TRANSACTION_open = 10;
        static final int TRANSACTION_openNearbySocket = 5;
        static final int TRANSACTION_publish = 1;
        static final int TRANSACTION_registerConnectionListener = 8;
        static final int TRANSACTION_registerInternalSocketListener = 6;
        static final int TRANSACTION_setFrequency = 18;
        static final int TRANSACTION_stopFindVendorDevice = 20;
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

        private static class Proxy implements INearbyAdapter {
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

            public boolean subscribe(boolean allowWakeupById, int businessId, ISubscribeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allowWakeupById);
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
        }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v21, resolved type: com.huawei.nearbysdk.NearbyConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v33, resolved type: com.huawei.nearbysdk.NearbyDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v37, resolved type: com.huawei.nearbysdk.NearbyDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v42, resolved type: com.huawei.nearbysdk.closeRange.CloseRangeEventFilter} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v46, resolved type: com.huawei.nearbysdk.closeRange.CloseRangeEventFilter} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v50, resolved type: com.huawei.nearbysdk.closeRange.CloseRangeDeviceFilter} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v54, resolved type: com.huawei.nearbysdk.closeRange.CloseRangeDeviceFilter} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v58, resolved type: com.huawei.nearbysdk.BleScanLevel} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v64, resolved type: com.huawei.nearbysdk.NearbyDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v68, resolved type: com.huawei.nearbysdk.NearbyDevice} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v11 */
        /* JADX WARNING: type inference failed for: r0v27 */
        /* JADX WARNING: type inference failed for: r0v73 */
        /* JADX WARNING: type inference failed for: r0v74 */
        /* JADX WARNING: type inference failed for: r0v75 */
        /* JADX WARNING: type inference failed for: r0v76 */
        /* JADX WARNING: type inference failed for: r0v77 */
        /* JADX WARNING: type inference failed for: r0v78 */
        /* JADX WARNING: type inference failed for: r0v79 */
        /* JADX WARNING: type inference failed for: r0v80 */
        /* JADX WARNING: type inference failed for: r0v81 */
        /* JADX WARNING: type inference failed for: r0v82 */
        /* JADX WARNING: type inference failed for: r0v83 */
        /* JADX WARNING: type inference failed for: r0v84 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CloseRangeBusinessType _arg0;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg02 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = publish(data.readInt(), data.readInt(), data.readInt(), IPublishListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = unPublish(IPublishListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = subscribe(data.readInt() != 0, data.readInt(), ISubscribeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = unSubscribe(ISubscribeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg1 = data.readInt();
                        int _arg2 = data.readInt();
                        String _arg3 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result5 = openNearbySocket(_arg03, _arg1, _arg2, _arg3, _arg02, data.readInt(), ICreateSocketListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = registerInternalSocketListener(data.readInt(), data.readInt(), IInternalSocketListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result7 = unRegisterInternalSocketListener(IInternalSocketListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = NearbyConfiguration.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result8 = registerConnectionListener(_arg04, _arg12, _arg02, IInternalConnectionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result9 = unRegisterConnectionListener(IInternalConnectionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg13 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result10 = open(_arg05, _arg13, _arg22, _arg02, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result11 = write(_arg06, _arg14, _arg02, data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(parcel);
                        }
                        close(_arg07, _arg15, _arg02);
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = hasInit();
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = CloseRangeEventFilter.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result13 = subscribeEvent(_arg02, ICloseRangeEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = CloseRangeEventFilter.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result14 = unSubscribeEvent(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = CloseRangeDeviceFilter.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result15 = subscribeDevice(_arg02, ICloseRangeDeviceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = CloseRangeDeviceFilter.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result16 = unSubscribeDevice(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CloseRangeBusinessType.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg02 = BleScanLevel.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result17 = setFrequency(_arg0, _arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result18 = findVendorDevice(data.readInt(), data.readInt(), IDevFindListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = stopFindVendorDevice(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result20 = connectVendorDevice(_arg02, data.readInt(), IDevConnectListen.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = NearbyDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result21 = disconnectVendorDevice(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void close(int i, int i2, NearbyDevice nearbyDevice) throws RemoteException;

    boolean connectVendorDevice(NearbyDevice nearbyDevice, int i, IDevConnectListen iDevConnectListen) throws RemoteException;

    boolean disconnectVendorDevice(NearbyDevice nearbyDevice) throws RemoteException;

    boolean findVendorDevice(int i, int i2, IDevFindListener iDevFindListener) throws RemoteException;

    boolean hasInit() throws RemoteException;

    boolean open(int i, int i2, int i3, NearbyDevice nearbyDevice, int i4) throws RemoteException;

    boolean openNearbySocket(int i, int i2, int i3, String str, NearbyDevice nearbyDevice, int i4, ICreateSocketListener iCreateSocketListener) throws RemoteException;

    boolean publish(int i, int i2, int i3, IPublishListener iPublishListener) throws RemoteException;

    boolean registerConnectionListener(int i, int i2, NearbyConfiguration nearbyConfiguration, IInternalConnectionListener iInternalConnectionListener) throws RemoteException;

    boolean registerInternalSocketListener(int i, int i2, IInternalSocketListener iInternalSocketListener) throws RemoteException;

    boolean setFrequency(CloseRangeBusinessType closeRangeBusinessType, BleScanLevel bleScanLevel) throws RemoteException;

    boolean stopFindVendorDevice(int i, int i2) throws RemoteException;

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
}

package com.huawei.airsharing.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.airsharing.api.Event;
import com.huawei.airsharing.api.ProjectionDevice;

public interface IAidlHwListener extends IInterface {
    int getId() throws RemoteException;

    void onDisplayUpdate(int i, String str, String str2, int i2) throws RemoteException;

    boolean onEvent(int i, String str) throws RemoteException;

    void onEventHandle(Event event) throws RemoteException;

    void onMirrorUpdate(int i, String str, String str2, int i2, boolean z) throws RemoteException;

    void onProjectionDeviceUpdate(int i, ProjectionDevice projectionDevice) throws RemoteException;

    public static abstract class Stub extends Binder implements IAidlHwListener {
        private static final String DESCRIPTOR = "com.huawei.airsharing.client.IAidlHwListener";
        static final int TRANSACTION_getId = 4;
        static final int TRANSACTION_onDisplayUpdate = 2;
        static final int TRANSACTION_onEvent = 1;
        static final int TRANSACTION_onEventHandle = 6;
        static final int TRANSACTION_onMirrorUpdate = 3;
        static final int TRANSACTION_onProjectionDeviceUpdate = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAidlHwListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAidlHwListener)) {
                return new Proxy(obj);
            }
            return (IAidlHwListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ProjectionDevice _arg1;
            Event _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean onEvent = onEvent(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(onEvent ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onDisplayUpdate(data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onMirrorUpdate(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getId();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ProjectionDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onProjectionDeviceUpdate(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Event.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onEventHandle(_arg0);
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

        private static class Proxy implements IAidlHwListener {
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

            @Override // com.huawei.airsharing.client.IAidlHwListener
            public boolean onEvent(int eventId, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeString(type);
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

            @Override // com.huawei.airsharing.client.IAidlHwListener
            public void onDisplayUpdate(int eventId, String devName, String devAdderss, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeString(devName);
                    _data.writeString(devAdderss);
                    _data.writeInt(priority);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwListener
            public void onMirrorUpdate(int eventId, String devName, String udn, int priority, boolean isSupportMirror) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeString(devName);
                    _data.writeString(udn);
                    _data.writeInt(priority);
                    _data.writeInt(isSupportMirror ? 1 : 0);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwListener
            public int getId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwListener
            public void onProjectionDeviceUpdate(int eventId, ProjectionDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwListener
            public void onEventHandle(Event event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}

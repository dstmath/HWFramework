package android.media.midi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMidiDeviceListener extends IInterface {

    public static abstract class Stub extends Binder implements IMidiDeviceListener {
        private static final String DESCRIPTOR = "android.media.midi.IMidiDeviceListener";
        static final int TRANSACTION_onDeviceAdded = 1;
        static final int TRANSACTION_onDeviceRemoved = 2;
        static final int TRANSACTION_onDeviceStatusChanged = 3;

        private static class Proxy implements IMidiDeviceListener {
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

            public void onDeviceAdded(MidiDeviceInfo device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_onDeviceAdded);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceAdded, _data, null, Stub.TRANSACTION_onDeviceAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceRemoved(MidiDeviceInfo device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_onDeviceAdded);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceRemoved, _data, null, Stub.TRANSACTION_onDeviceAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceStatusChanged(MidiDeviceStatus status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (status != null) {
                        _data.writeInt(Stub.TRANSACTION_onDeviceAdded);
                        status.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceStatusChanged, _data, null, Stub.TRANSACTION_onDeviceAdded);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMidiDeviceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMidiDeviceListener)) {
                return new Proxy(obj);
            }
            return (IMidiDeviceListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MidiDeviceInfo midiDeviceInfo;
            switch (code) {
                case TRANSACTION_onDeviceAdded /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        midiDeviceInfo = (MidiDeviceInfo) MidiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        midiDeviceInfo = null;
                    }
                    onDeviceAdded(midiDeviceInfo);
                    return true;
                case TRANSACTION_onDeviceRemoved /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        midiDeviceInfo = (MidiDeviceInfo) MidiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        midiDeviceInfo = null;
                    }
                    onDeviceRemoved(midiDeviceInfo);
                    return true;
                case TRANSACTION_onDeviceStatusChanged /*3*/:
                    MidiDeviceStatus midiDeviceStatus;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        midiDeviceStatus = (MidiDeviceStatus) MidiDeviceStatus.CREATOR.createFromParcel(data);
                    } else {
                        midiDeviceStatus = null;
                    }
                    onDeviceStatusChanged(midiDeviceStatus);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onDeviceAdded(MidiDeviceInfo midiDeviceInfo) throws RemoteException;

    void onDeviceRemoved(MidiDeviceInfo midiDeviceInfo) throws RemoteException;

    void onDeviceStatusChanged(MidiDeviceStatus midiDeviceStatus) throws RemoteException;
}

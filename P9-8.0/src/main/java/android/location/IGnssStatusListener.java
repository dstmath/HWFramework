package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGnssStatusListener extends IInterface {

    public static abstract class Stub extends Binder implements IGnssStatusListener {
        private static final String DESCRIPTOR = "android.location.IGnssStatusListener";
        static final int TRANSACTION_onFirstFix = 3;
        static final int TRANSACTION_onGnssStarted = 1;
        static final int TRANSACTION_onGnssStopped = 2;
        static final int TRANSACTION_onNmeaReceived = 5;
        static final int TRANSACTION_onSvStatusChanged = 4;

        private static class Proxy implements IGnssStatusListener {
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

            public void onGnssStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onGnssStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFirstFix(int ttff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ttff);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSvStatusChanged(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(svCount);
                    _data.writeIntArray(svidWithFlags);
                    _data.writeFloatArray(cn0s);
                    _data.writeFloatArray(elevations);
                    _data.writeFloatArray(azimuths);
                    _data.writeFloatArray(carrierFreqs);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNmeaReceived(long timestamp, String nmea) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestamp);
                    _data.writeString(nmea);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGnssStatusListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGnssStatusListener)) {
                return new Proxy(obj);
            }
            return (IGnssStatusListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onGnssStarted();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onGnssStopped();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onFirstFix(data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onSvStatusChanged(data.readInt(), data.createIntArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onNmeaReceived(data.readLong(), data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onFirstFix(int i) throws RemoteException;

    void onGnssStarted() throws RemoteException;

    void onGnssStopped() throws RemoteException;

    void onNmeaReceived(long j, String str) throws RemoteException;

    void onSvStatusChanged(int i, int[] iArr, float[] fArr, float[] fArr2, float[] fArr3, float[] fArr4) throws RemoteException;
}

package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGnssStatusListener extends IInterface {
    void onFirstFix(int i) throws RemoteException;

    void onGnssStarted() throws RemoteException;

    void onGnssStopped() throws RemoteException;

    void onNmeaReceived(long j, String str) throws RemoteException;

    void onSvStatusChanged(int i, int[] iArr, float[] fArr, float[] fArr2, float[] fArr3, float[] fArr4) throws RemoteException;

    public static class Default implements IGnssStatusListener {
        @Override // android.location.IGnssStatusListener
        public void onGnssStarted() throws RemoteException {
        }

        @Override // android.location.IGnssStatusListener
        public void onGnssStopped() throws RemoteException {
        }

        @Override // android.location.IGnssStatusListener
        public void onFirstFix(int ttff) throws RemoteException {
        }

        @Override // android.location.IGnssStatusListener
        public void onSvStatusChanged(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) throws RemoteException {
        }

        @Override // android.location.IGnssStatusListener
        public void onNmeaReceived(long timestamp, String nmea) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGnssStatusListener {
        private static final String DESCRIPTOR = "android.location.IGnssStatusListener";
        static final int TRANSACTION_onFirstFix = 3;
        static final int TRANSACTION_onGnssStarted = 1;
        static final int TRANSACTION_onGnssStopped = 2;
        static final int TRANSACTION_onNmeaReceived = 5;
        static final int TRANSACTION_onSvStatusChanged = 4;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onGnssStarted";
            }
            if (transactionCode == 2) {
                return "onGnssStopped";
            }
            if (transactionCode == 3) {
                return "onFirstFix";
            }
            if (transactionCode == 4) {
                return "onSvStatusChanged";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onNmeaReceived";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onGnssStarted();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onGnssStopped();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onFirstFix(data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onSvStatusChanged(data.readInt(), data.createIntArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onNmeaReceived(data.readLong(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGnssStatusListener {
            public static IGnssStatusListener sDefaultImpl;
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

            @Override // android.location.IGnssStatusListener
            public void onGnssStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGnssStarted();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.IGnssStatusListener
            public void onGnssStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGnssStopped();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.IGnssStatusListener
            public void onFirstFix(int ttff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ttff);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFirstFix(ttff);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.IGnssStatusListener
            public void onSvStatusChanged(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(svCount);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeIntArray(svidWithFlags);
                        try {
                            _data.writeFloatArray(cn0s);
                            try {
                                _data.writeFloatArray(elevations);
                                try {
                                    _data.writeFloatArray(azimuths);
                                } catch (Throwable th2) {
                                    th = th2;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeFloatArray(carrierFreqs);
                            try {
                                if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().onSvStatusChanged(svCount, svidWithFlags, cn0s, elevations, azimuths, carrierFreqs);
                                _data.recycle();
                            } catch (Throwable th5) {
                                th = th5;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.location.IGnssStatusListener
            public void onNmeaReceived(long timestamp, String nmea) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestamp);
                    _data.writeString(nmea);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNmeaReceived(timestamp, nmea);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGnssStatusListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGnssStatusListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGpsGeofenceHardware extends IInterface {

    public static abstract class Stub extends Binder implements IGpsGeofenceHardware {
        private static final String DESCRIPTOR = "android.location.IGpsGeofenceHardware";
        static final int TRANSACTION_addCircularHardwareGeofence = 2;
        static final int TRANSACTION_isHardwareGeofenceSupported = 1;
        static final int TRANSACTION_pauseHardwareGeofence = 4;
        static final int TRANSACTION_removeHardwareGeofence = 3;
        static final int TRANSACTION_resumeHardwareGeofence = 5;

        private static class Proxy implements IGpsGeofenceHardware {
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

            public boolean isHardwareGeofenceSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransition, int notificationResponsiveness, int unknownTimer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    try {
                        _data.writeDouble(latitude);
                        try {
                            _data.writeDouble(longitude);
                            try {
                                _data.writeDouble(radius);
                            } catch (Throwable th) {
                                th = th;
                                int i = lastTransition;
                                int i2 = monitorTransition;
                                int i3 = notificationResponsiveness;
                                int i4 = unknownTimer;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            double d = radius;
                            int i5 = lastTransition;
                            int i22 = monitorTransition;
                            int i32 = notificationResponsiveness;
                            int i42 = unknownTimer;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        double d2 = longitude;
                        double d3 = radius;
                        int i52 = lastTransition;
                        int i222 = monitorTransition;
                        int i322 = notificationResponsiveness;
                        int i422 = unknownTimer;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(lastTransition);
                        try {
                            _data.writeInt(monitorTransition);
                            try {
                                _data.writeInt(notificationResponsiveness);
                            } catch (Throwable th4) {
                                th = th4;
                                int i4222 = unknownTimer;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            int i3222 = notificationResponsiveness;
                            int i42222 = unknownTimer;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i2222 = monitorTransition;
                        int i32222 = notificationResponsiveness;
                        int i422222 = unknownTimer;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(unknownTimer);
                        try {
                            boolean z = false;
                            this.mRemote.transact(2, _data, _reply, 0);
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                z = true;
                            }
                            boolean _result = z;
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        } catch (Throwable th7) {
                            th = th7;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    double d4 = latitude;
                    double d22 = longitude;
                    double d32 = radius;
                    int i522 = lastTransition;
                    int i22222 = monitorTransition;
                    int i322222 = notificationResponsiveness;
                    int i4222222 = unknownTimer;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public boolean removeHardwareGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
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

            public boolean pauseHardwareGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
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

            public boolean resumeHardwareGeofence(int geofenceId, int monitorTransition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(monitorTransition);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGpsGeofenceHardware asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGpsGeofenceHardware)) {
                return new Proxy(obj);
            }
            return (IGpsGeofenceHardware) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        public boolean onTransact(int r30, android.os.Parcel r31, android.os.Parcel r32, int r33) throws android.os.RemoteException {
            /*
                r29 = this;
                r12 = r29
                r13 = r30
                r14 = r31
                r15 = r32
                java.lang.String r11 = "android.location.IGpsGeofenceHardware"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r16 = 1
                if (r13 == r0) goto L_0x00a2
                switch(r13) {
                    case 1: goto L_0x0093;
                    case 2: goto L_0x0053;
                    case 3: goto L_0x0041;
                    case 4: goto L_0x002f;
                    case 5: goto L_0x0019;
                    default: goto L_0x0014;
                }
            L_0x0014:
                boolean r0 = super.onTransact(r30, r31, r32, r33)
                return r0
            L_0x0019:
                r14.enforceInterface(r11)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                boolean r2 = r12.resumeHardwareGeofence(r0, r1)
                r32.writeNoException()
                r15.writeInt(r2)
                return r16
            L_0x002f:
                r14.enforceInterface(r11)
                int r0 = r31.readInt()
                boolean r1 = r12.pauseHardwareGeofence(r0)
                r32.writeNoException()
                r15.writeInt(r1)
                return r16
            L_0x0041:
                r14.enforceInterface(r11)
                int r0 = r31.readInt()
                boolean r1 = r12.removeHardwareGeofence(r0)
                r32.writeNoException()
                r15.writeInt(r1)
                return r16
            L_0x0053:
                r14.enforceInterface(r11)
                int r17 = r31.readInt()
                double r18 = r31.readDouble()
                double r20 = r31.readDouble()
                double r22 = r31.readDouble()
                int r24 = r31.readInt()
                int r25 = r31.readInt()
                int r26 = r31.readInt()
                int r27 = r31.readInt()
                r0 = r12
                r1 = r17
                r2 = r18
                r4 = r20
                r6 = r22
                r8 = r24
                r9 = r25
                r10 = r26
                r13 = r11
                r11 = r27
                boolean r0 = r0.addCircularHardwareGeofence(r1, r2, r4, r6, r8, r9, r10, r11)
                r32.writeNoException()
                r15.writeInt(r0)
                return r16
            L_0x0093:
                r13 = r11
                r14.enforceInterface(r13)
                boolean r0 = r29.isHardwareGeofenceSupported()
                r32.writeNoException()
                r15.writeInt(r0)
                return r16
            L_0x00a2:
                r13 = r11
                r15.writeString(r13)
                return r16
            */
            throw new UnsupportedOperationException("Method not decompiled: android.location.IGpsGeofenceHardware.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    boolean addCircularHardwareGeofence(int i, double d, double d2, double d3, int i2, int i3, int i4, int i5) throws RemoteException;

    boolean isHardwareGeofenceSupported() throws RemoteException;

    boolean pauseHardwareGeofence(int i) throws RemoteException;

    boolean removeHardwareGeofence(int i) throws RemoteException;

    boolean resumeHardwareGeofence(int i, int i2) throws RemoteException;
}

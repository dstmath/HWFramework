package android.app;

import android.app.AlarmManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;

public interface IAlarmManager extends IInterface {

    public static abstract class Stub extends Binder implements IAlarmManager {
        private static final String DESCRIPTOR = "android.app.IAlarmManager";
        static final int TRANSACTION_adjustHwRTCAlarm = 10;
        static final int TRANSACTION_checkHasHwRTCAlarm = 9;
        static final int TRANSACTION_currentNetworkTimeMillis = 13;
        static final int TRANSACTION_getNextAlarmClock = 6;
        static final int TRANSACTION_getNextWakeFromIdleTime = 5;
        static final int TRANSACTION_getWakeUpNum = 8;
        static final int TRANSACTION_remove = 4;
        static final int TRANSACTION_set = 1;
        static final int TRANSACTION_setHwAirPlaneStateProp = 11;
        static final int TRANSACTION_setHwRTCAlarm = 12;
        static final int TRANSACTION_setTime = 2;
        static final int TRANSACTION_setTimeZone = 3;
        static final int TRANSACTION_updateBlockedUids = 7;

        private static class Proxy implements IAlarmManager {
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

            public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener listener, String listenerTag, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock) throws RemoteException {
                int i;
                PendingIntent pendingIntent = operation;
                WorkSource workSource2 = workSource;
                AlarmManager.AlarmClockInfo alarmClockInfo = alarmClock;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPackage);
                        try {
                            _data.writeInt(type);
                        } catch (Throwable th) {
                            th = th;
                            long j = triggerAtTime;
                            long j2 = windowLength;
                            long j3 = interval;
                            int i2 = flags;
                            String str = listenerTag;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i3 = type;
                        long j4 = triggerAtTime;
                        long j22 = windowLength;
                        long j32 = interval;
                        int i22 = flags;
                        String str2 = listenerTag;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(triggerAtTime);
                        try {
                            _data.writeLong(windowLength);
                            try {
                                _data.writeLong(interval);
                                try {
                                    _data.writeInt(flags);
                                    if (pendingIntent != null) {
                                        _data.writeInt(1);
                                        pendingIntent.writeToParcel(_data, 0);
                                    } else {
                                        _data.writeInt(0);
                                    }
                                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                                } catch (Throwable th3) {
                                    th = th3;
                                    String str22 = listenerTag;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                int i222 = flags;
                                String str222 = listenerTag;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            long j322 = interval;
                            int i2222 = flags;
                            String str2222 = listenerTag;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        long j222 = windowLength;
                        long j3222 = interval;
                        int i22222 = flags;
                        String str22222 = listenerTag;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(listenerTag);
                        if (workSource2 != null) {
                            _data.writeInt(1);
                            i = 0;
                            workSource2.writeToParcel(_data, 0);
                        } else {
                            i = 0;
                            _data.writeInt(0);
                        }
                        if (alarmClockInfo != null) {
                            _data.writeInt(1);
                            alarmClockInfo.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(i);
                        }
                        this.mRemote.transact(1, _data, _reply, 0);
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    String str3 = callingPackage;
                    int i32 = type;
                    long j42 = triggerAtTime;
                    long j2222 = windowLength;
                    long j32222 = interval;
                    int i222222 = flags;
                    String str222222 = listenerTag;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public boolean setTime(long millis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(millis);
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

            public void setTimeZone(String zone) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(zone);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remove(PendingIntent operation, IAlarmListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getNextWakeFromIdleTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AlarmManager.AlarmClockInfo getNextAlarmClock(int userId) throws RemoteException {
                AlarmManager.AlarmClockInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AlarmManager.AlarmClockInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateBlockedUids(int uid, boolean isBlocked) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(isBlocked);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWakeUpNum(int uid, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(pkg);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long checkHasHwRTCAlarm(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deskClockTime);
                    _data.writeInt(bootOnTime);
                    _data.writeInt(typeState);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHwAirPlaneStateProp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHwRTCAlarm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long currentNetworkTimeMillis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAlarmManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAlarmManager)) {
                return new Proxy(obj);
            }
            return (IAlarmManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v0, resolved type: android.app.AlarmManager$AlarmClockInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v25, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v26, resolved type: android.app.PendingIntent} */
        /* JADX WARNING: type inference failed for: r0v7, types: [android.app.AlarmManager$AlarmClockInfo] */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r29, android.os.Parcel r30, android.os.Parcel r31, int r32) throws android.os.RemoteException {
            /*
                r28 = this;
                r15 = r28
                r12 = r29
                r11 = r30
                r9 = r31
                java.lang.String r7 = "android.app.IAlarmManager"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r8 = 1
                if (r12 == r0) goto L_0x0179
                r0 = 0
                r1 = 0
                switch(r12) {
                    case 1: goto L_0x0104;
                    case 2: goto L_0x00f2;
                    case 3: goto L_0x00e4;
                    case 4: goto L_0x00c2;
                    case 5: goto L_0x00b4;
                    case 6: goto L_0x0099;
                    case 7: goto L_0x0083;
                    case 8: goto L_0x006d;
                    case 9: goto L_0x005b;
                    case 10: goto L_0x003c;
                    case 11: goto L_0x0032;
                    case 12: goto L_0x0028;
                    case 13: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r29, r30, r31, r32)
                return r0
            L_0x001a:
                r11.enforceInterface(r7)
                long r0 = r28.currentNetworkTimeMillis()
                r31.writeNoException()
                r9.writeLong(r0)
                return r8
            L_0x0028:
                r11.enforceInterface(r7)
                r28.setHwRTCAlarm()
                r31.writeNoException()
                return r8
            L_0x0032:
                r11.enforceInterface(r7)
                r28.setHwAirPlaneStateProp()
                r31.writeNoException()
                return r8
            L_0x003c:
                r11.enforceInterface(r7)
                int r0 = r30.readInt()
                if (r0 == 0) goto L_0x0047
                r0 = r8
                goto L_0x0048
            L_0x0047:
                r0 = r1
            L_0x0048:
                int r2 = r30.readInt()
                if (r2 == 0) goto L_0x0050
                r1 = r8
            L_0x0050:
                int r2 = r30.readInt()
                r15.adjustHwRTCAlarm(r0, r1, r2)
                r31.writeNoException()
                return r8
            L_0x005b:
                r11.enforceInterface(r7)
                java.lang.String r0 = r30.readString()
                long r1 = r15.checkHasHwRTCAlarm(r0)
                r31.writeNoException()
                r9.writeLong(r1)
                return r8
            L_0x006d:
                r11.enforceInterface(r7)
                int r0 = r30.readInt()
                java.lang.String r1 = r30.readString()
                int r2 = r15.getWakeUpNum(r0, r1)
                r31.writeNoException()
                r9.writeInt(r2)
                return r8
            L_0x0083:
                r11.enforceInterface(r7)
                int r0 = r30.readInt()
                int r2 = r30.readInt()
                if (r2 == 0) goto L_0x0092
                r1 = r8
            L_0x0092:
                r15.updateBlockedUids(r0, r1)
                r31.writeNoException()
                return r8
            L_0x0099:
                r11.enforceInterface(r7)
                int r0 = r30.readInt()
                android.app.AlarmManager$AlarmClockInfo r2 = r15.getNextAlarmClock(r0)
                r31.writeNoException()
                if (r2 == 0) goto L_0x00b0
                r9.writeInt(r8)
                r2.writeToParcel(r9, r8)
                goto L_0x00b3
            L_0x00b0:
                r9.writeInt(r1)
            L_0x00b3:
                return r8
            L_0x00b4:
                r11.enforceInterface(r7)
                long r0 = r28.getNextWakeFromIdleTime()
                r31.writeNoException()
                r9.writeLong(r0)
                return r8
            L_0x00c2:
                r11.enforceInterface(r7)
                int r1 = r30.readInt()
                if (r1 == 0) goto L_0x00d4
                android.os.Parcelable$Creator<android.app.PendingIntent> r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r11)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                goto L_0x00d5
            L_0x00d4:
            L_0x00d5:
                android.os.IBinder r1 = r30.readStrongBinder()
                android.app.IAlarmListener r1 = android.app.IAlarmListener.Stub.asInterface(r1)
                r15.remove(r0, r1)
                r31.writeNoException()
                return r8
            L_0x00e4:
                r11.enforceInterface(r7)
                java.lang.String r0 = r30.readString()
                r15.setTimeZone(r0)
                r31.writeNoException()
                return r8
            L_0x00f2:
                r11.enforceInterface(r7)
                long r0 = r30.readLong()
                boolean r2 = r15.setTime(r0)
                r31.writeNoException()
                r9.writeInt(r2)
                return r8
            L_0x0104:
                r11.enforceInterface(r7)
                java.lang.String r16 = r30.readString()
                int r17 = r30.readInt()
                long r18 = r30.readLong()
                long r20 = r30.readLong()
                long r22 = r30.readLong()
                int r24 = r30.readInt()
                int r1 = r30.readInt()
                if (r1 == 0) goto L_0x012f
                android.os.Parcelable$Creator<android.app.PendingIntent> r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r11)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                r10 = r1
                goto L_0x0130
            L_0x012f:
                r10 = r0
            L_0x0130:
                android.os.IBinder r1 = r30.readStrongBinder()
                android.app.IAlarmListener r25 = android.app.IAlarmListener.Stub.asInterface(r1)
                java.lang.String r26 = r30.readString()
                int r1 = r30.readInt()
                if (r1 == 0) goto L_0x014c
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r11)
                android.os.WorkSource r1 = (android.os.WorkSource) r1
                r13 = r1
                goto L_0x014d
            L_0x014c:
                r13 = r0
            L_0x014d:
                int r1 = r30.readInt()
                if (r1 == 0) goto L_0x015d
                android.os.Parcelable$Creator<android.app.AlarmManager$AlarmClockInfo> r0 = android.app.AlarmManager.AlarmClockInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r11)
                android.app.AlarmManager$AlarmClockInfo r0 = (android.app.AlarmManager.AlarmClockInfo) r0
            L_0x015b:
                r14 = r0
                goto L_0x015e
            L_0x015d:
                goto L_0x015b
            L_0x015e:
                r0 = r15
                r1 = r16
                r2 = r17
                r3 = r18
                r5 = r20
                r15 = r7
                r27 = r8
                r7 = r22
                r9 = r24
                r11 = r25
                r12 = r26
                r0.set(r1, r2, r3, r5, r7, r9, r10, r11, r12, r13, r14)
                r31.writeNoException()
                return r27
            L_0x0179:
                r15 = r7
                r27 = r8
                r0 = r31
                r0.writeString(r15)
                return r27
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.IAlarmManager.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void adjustHwRTCAlarm(boolean z, boolean z2, int i) throws RemoteException;

    long checkHasHwRTCAlarm(String str) throws RemoteException;

    long currentNetworkTimeMillis() throws RemoteException;

    AlarmManager.AlarmClockInfo getNextAlarmClock(int i) throws RemoteException;

    long getNextWakeFromIdleTime() throws RemoteException;

    int getWakeUpNum(int i, String str) throws RemoteException;

    void remove(PendingIntent pendingIntent, IAlarmListener iAlarmListener) throws RemoteException;

    void set(String str, int i, long j, long j2, long j3, int i2, PendingIntent pendingIntent, IAlarmListener iAlarmListener, String str2, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClockInfo) throws RemoteException;

    void setHwAirPlaneStateProp() throws RemoteException;

    void setHwRTCAlarm() throws RemoteException;

    boolean setTime(long j) throws RemoteException;

    void setTimeZone(String str) throws RemoteException;

    void updateBlockedUids(int i, boolean z) throws RemoteException;
}

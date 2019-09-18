package android.app;

import android.app.backup.IBackupManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBackupAgent extends IInterface {

    public static abstract class Stub extends Binder implements IBackupAgent {
        private static final String DESCRIPTOR = "android.app.IBackupAgent";
        static final int TRANSACTION_doBackup = 1;
        static final int TRANSACTION_doFullBackup = 3;
        static final int TRANSACTION_doMeasureFullBackup = 4;
        static final int TRANSACTION_doQuotaExceeded = 5;
        static final int TRANSACTION_doRestore = 2;
        static final int TRANSACTION_doRestoreFile = 6;
        static final int TRANSACTION_doRestoreFinished = 7;
        static final int TRANSACTION_fail = 8;

        private static class Proxy implements IBackupAgent {
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

            public void doBackup(ParcelFileDescriptor oldState, ParcelFileDescriptor data, ParcelFileDescriptor newState, long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oldState != null) {
                        _data.writeInt(1);
                        oldState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newState != null) {
                        _data.writeInt(1);
                        newState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(quotaBytes);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    _data.writeInt(transportFlags);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void doRestore(ParcelFileDescriptor data, long appVersionCode, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(appVersionCode);
                    if (newState != null) {
                        _data.writeInt(1);
                        newState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void doFullBackup(ParcelFileDescriptor data, long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(quotaBytes);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    _data.writeInt(transportFlags);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void doMeasureFullBackup(long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(quotaBytes);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    _data.writeInt(transportFlags);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void doQuotaExceeded(long backupDataBytes, long quotaBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(backupDataBytes);
                    _data.writeLong(quotaBytes);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void doRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
                ParcelFileDescriptor parcelFileDescriptor = data;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (parcelFileDescriptor != null) {
                        _data.writeInt(1);
                        parcelFileDescriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(size);
                    try {
                        _data.writeInt(type);
                        try {
                            _data.writeString(domain);
                            try {
                                _data.writeString(path);
                            } catch (Throwable th) {
                                th = th;
                                long j = mode;
                                long j2 = mtime;
                                int i = token;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            String str = path;
                            long j3 = mode;
                            long j22 = mtime;
                            int i2 = token;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        String str2 = domain;
                        String str3 = path;
                        long j32 = mode;
                        long j222 = mtime;
                        int i22 = token;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(mode);
                        try {
                            _data.writeLong(mtime);
                            try {
                                _data.writeInt(token);
                                _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                            } catch (Throwable th4) {
                                th = th4;
                                _data.recycle();
                                throw th;
                            }
                            try {
                                this.mRemote.transact(6, _data, null, 1);
                                _data.recycle();
                            } catch (Throwable th5) {
                                th = th5;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            int i222 = token;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        long j2222 = mtime;
                        int i2222 = token;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    int i3 = type;
                    String str22 = domain;
                    String str32 = path;
                    long j322 = mode;
                    long j22222 = mtime;
                    int i22222 = token;
                    _data.recycle();
                    throw th;
                }
            }

            public void doRestoreFinished(int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void fail(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBackupAgent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBackupAgent)) {
                return new Proxy(obj);
            }
            return (IBackupAgent) iin;
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
                r13 = r29
                r14 = r30
                r15 = r31
                java.lang.String r12 = "android.app.IBackupAgent"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r16 = 1
                if (r14 == r0) goto L_0x0177
                r0 = 0
                switch(r14) {
                    case 1: goto L_0x0124;
                    case 2: goto L_0x00e7;
                    case 3: goto L_0x00b5;
                    case 4: goto L_0x0094;
                    case 5: goto L_0x0084;
                    case 6: goto L_0x0036;
                    case 7: goto L_0x0023;
                    case 8: goto L_0x0018;
                    default: goto L_0x0013;
                }
            L_0x0013:
                boolean r0 = super.onTransact(r30, r31, r32, r33)
                return r0
            L_0x0018:
                r15.enforceInterface(r12)
                java.lang.String r0 = r31.readString()
                r13.fail(r0)
                return r16
            L_0x0023:
                r15.enforceInterface(r12)
                int r0 = r31.readInt()
                android.os.IBinder r1 = r31.readStrongBinder()
                android.app.backup.IBackupManager r1 = android.app.backup.IBackupManager.Stub.asInterface(r1)
                r13.doRestoreFinished(r0, r1)
                return r16
            L_0x0036:
                r15.enforceInterface(r12)
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x0049
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.os.ParcelFileDescriptor r0 = (android.os.ParcelFileDescriptor) r0
            L_0x0047:
                r1 = r0
                goto L_0x004a
            L_0x0049:
                goto L_0x0047
            L_0x004a:
                long r17 = r31.readLong()
                int r19 = r31.readInt()
                java.lang.String r20 = r31.readString()
                java.lang.String r21 = r31.readString()
                long r22 = r31.readLong()
                long r24 = r31.readLong()
                int r26 = r31.readInt()
                android.os.IBinder r0 = r31.readStrongBinder()
                android.app.backup.IBackupManager r27 = android.app.backup.IBackupManager.Stub.asInterface(r0)
                r0 = r13
                r2 = r17
                r4 = r19
                r5 = r20
                r6 = r21
                r7 = r22
                r9 = r24
                r11 = r26
                r14 = r12
                r12 = r27
                r0.doRestoreFile(r1, r2, r4, r5, r6, r7, r9, r11, r12)
                return r16
            L_0x0084:
                r14 = r12
                r15.enforceInterface(r14)
                long r0 = r31.readLong()
                long r2 = r31.readLong()
                r13.doQuotaExceeded(r0, r2)
                return r16
            L_0x0094:
                r14 = r12
                r15.enforceInterface(r14)
                long r6 = r31.readLong()
                int r8 = r31.readInt()
                android.os.IBinder r0 = r31.readStrongBinder()
                android.app.backup.IBackupManager r9 = android.app.backup.IBackupManager.Stub.asInterface(r0)
                int r10 = r31.readInt()
                r0 = r13
                r1 = r6
                r3 = r8
                r4 = r9
                r5 = r10
                r0.doMeasureFullBackup(r1, r3, r4, r5)
                return r16
            L_0x00b5:
                r14 = r12
                r15.enforceInterface(r14)
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x00c9
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.os.ParcelFileDescriptor r0 = (android.os.ParcelFileDescriptor) r0
            L_0x00c7:
                r1 = r0
                goto L_0x00ca
            L_0x00c9:
                goto L_0x00c7
            L_0x00ca:
                long r7 = r31.readLong()
                int r9 = r31.readInt()
                android.os.IBinder r0 = r31.readStrongBinder()
                android.app.backup.IBackupManager r10 = android.app.backup.IBackupManager.Stub.asInterface(r0)
                int r11 = r31.readInt()
                r0 = r13
                r2 = r7
                r4 = r9
                r5 = r10
                r6 = r11
                r0.doFullBackup(r1, r2, r4, r5, r6)
                return r16
            L_0x00e7:
                r14 = r12
                r15.enforceInterface(r14)
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x00fa
                android.os.Parcelable$Creator r1 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.os.ParcelFileDescriptor r1 = (android.os.ParcelFileDescriptor) r1
                goto L_0x00fb
            L_0x00fa:
                r1 = r0
            L_0x00fb:
                long r7 = r31.readLong()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x010f
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.os.ParcelFileDescriptor r0 = (android.os.ParcelFileDescriptor) r0
            L_0x010d:
                r4 = r0
                goto L_0x0110
            L_0x010f:
                goto L_0x010d
            L_0x0110:
                int r9 = r31.readInt()
                android.os.IBinder r0 = r31.readStrongBinder()
                android.app.backup.IBackupManager r10 = android.app.backup.IBackupManager.Stub.asInterface(r0)
                r0 = r13
                r2 = r7
                r5 = r9
                r6 = r10
                r0.doRestore(r1, r2, r4, r5, r6)
                return r16
            L_0x0124:
                r14 = r12
                r15.enforceInterface(r14)
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x0137
                android.os.Parcelable$Creator r1 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.os.ParcelFileDescriptor r1 = (android.os.ParcelFileDescriptor) r1
                goto L_0x0138
            L_0x0137:
                r1 = r0
            L_0x0138:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0147
                android.os.Parcelable$Creator r2 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.os.ParcelFileDescriptor r2 = (android.os.ParcelFileDescriptor) r2
                goto L_0x0148
            L_0x0147:
                r2 = r0
            L_0x0148:
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x0158
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.os.ParcelFileDescriptor r0 = (android.os.ParcelFileDescriptor) r0
            L_0x0156:
                r3 = r0
                goto L_0x0159
            L_0x0158:
                goto L_0x0156
            L_0x0159:
                long r9 = r31.readLong()
                int r11 = r31.readInt()
                android.os.IBinder r0 = r31.readStrongBinder()
                android.app.backup.IBackupManager r12 = android.app.backup.IBackupManager.Stub.asInterface(r0)
                int r17 = r31.readInt()
                r0 = r13
                r4 = r9
                r6 = r11
                r7 = r12
                r8 = r17
                r0.doBackup(r1, r2, r3, r4, r6, r7, r8)
                return r16
            L_0x0177:
                r14 = r12
                r0 = r32
                r0.writeString(r14)
                return r16
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.IBackupAgent.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void doBackup(ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, ParcelFileDescriptor parcelFileDescriptor3, long j, int i, IBackupManager iBackupManager, int i2) throws RemoteException;

    void doFullBackup(ParcelFileDescriptor parcelFileDescriptor, long j, int i, IBackupManager iBackupManager, int i2) throws RemoteException;

    void doMeasureFullBackup(long j, int i, IBackupManager iBackupManager, int i2) throws RemoteException;

    void doQuotaExceeded(long j, long j2) throws RemoteException;

    void doRestore(ParcelFileDescriptor parcelFileDescriptor, long j, ParcelFileDescriptor parcelFileDescriptor2, int i, IBackupManager iBackupManager) throws RemoteException;

    void doRestoreFile(ParcelFileDescriptor parcelFileDescriptor, long j, int i, String str, String str2, long j2, long j3, int i2, IBackupManager iBackupManager) throws RemoteException;

    void doRestoreFinished(int i, IBackupManager iBackupManager) throws RemoteException;

    void fail(String str) throws RemoteException;
}

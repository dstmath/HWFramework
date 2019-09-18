package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITrustCircleManager extends IInterface {

    public static abstract class Stub extends Binder implements ITrustCircleManager {
        private static final String DESCRIPTOR = "huawei.android.security.ITrustCircleManager";
        static final int TRANSACTION_cancelAuthentication = 17;
        static final int TRANSACTION_cancelRegOrLogin = 8;
        static final int TRANSACTION_finalLogin = 7;
        static final int TRANSACTION_finalRegister = 6;
        static final int TRANSACTION_getCurrentState = 3;
        static final int TRANSACTION_getTcisInfo = 1;
        static final int TRANSACTION_initAuthenticate = 11;
        static final int TRANSACTION_initKeyAgreement = 2;
        static final int TRANSACTION_loginServerRequest = 4;
        static final int TRANSACTION_logout = 9;
        static final int TRANSACTION_receiveAck = 14;
        static final int TRANSACTION_receiveAuthSync = 12;
        static final int TRANSACTION_receiveAuthSyncAck = 13;
        static final int TRANSACTION_receivePK = 16;
        static final int TRANSACTION_requestPK = 15;
        static final int TRANSACTION_unregister = 10;
        static final int TRANSACTION_updateServerRequest = 5;

        private static class Proxy implements ITrustCircleManager {
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

            public Bundle getTcisInfo() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long initKeyAgreement(IKaCallback callBack, int kaVersion, long userId, byte[] aesTmpKey, String kaInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    _data.writeInt(kaVersion);
                    _data.writeLong(userId);
                    _data.writeByteArray(aesTmpKey);
                    _data.writeString(kaInfo);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCurrentState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    _data.writeInt(serverRegisterStatus);
                    _data.writeString(sessionID);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateServerRequest(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finalRegister(ILifeCycleCallback callback, String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(authPKData);
                    _data.writeString(authPKDataSign);
                    _data.writeString(updateIndexInfo);
                    _data.writeString(updateIndexSignature);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(updateResult);
                    _data.writeString(updateIndexInfo);
                    _data.writeString(updateIndexSignature);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void logout(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregister(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long initAuthenticate(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(authType);
                    _data.writeInt(authVersion);
                    _data.writeInt(policy);
                    _data.writeLong(userID);
                    _data.writeByteArray(AESTmpKey);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(authType);
                    try {
                        _data.writeInt(authVersion);
                        try {
                            _data.writeInt(taVersion);
                            try {
                                _data.writeInt(policy);
                                try {
                                    _data.writeLong(userID);
                                } catch (Throwable th) {
                                    th = th;
                                    byte[] bArr = AESTmpKey;
                                    byte[] bArr2 = tcisId;
                                    int i = pkVersion;
                                    long j = nonce;
                                    int i2 = authKeyAlgoType;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                long j2 = userID;
                                byte[] bArr3 = AESTmpKey;
                                byte[] bArr22 = tcisId;
                                int i3 = pkVersion;
                                long j3 = nonce;
                                int i22 = authKeyAlgoType;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i4 = policy;
                            long j22 = userID;
                            byte[] bArr32 = AESTmpKey;
                            byte[] bArr222 = tcisId;
                            int i32 = pkVersion;
                            long j32 = nonce;
                            int i222 = authKeyAlgoType;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeByteArray(AESTmpKey);
                            try {
                                _data.writeByteArray(tcisId);
                                try {
                                    _data.writeInt(pkVersion);
                                } catch (Throwable th4) {
                                    th = th4;
                                    long j322 = nonce;
                                    int i2222 = authKeyAlgoType;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                int i322 = pkVersion;
                                long j3222 = nonce;
                                int i22222 = authKeyAlgoType;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            byte[] bArr2222 = tcisId;
                            int i3222 = pkVersion;
                            long j32222 = nonce;
                            int i222222 = authKeyAlgoType;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(nonce);
                            try {
                                _data.writeInt(authKeyAlgoType);
                                _data.writeByteArray(authKeyInfo);
                                _data.writeByteArray(authKeyInfoSign);
                                this.mRemote.transact(12, _data, _reply, 0);
                                _reply.readException();
                                long _result = _reply.readLong();
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
                            int i2222222 = authKeyAlgoType;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        int i5 = taVersion;
                        int i42 = policy;
                        long j222 = userID;
                        byte[] bArr322 = AESTmpKey;
                        byte[] bArr22222 = tcisId;
                        int i32222 = pkVersion;
                        long j322222 = nonce;
                        int i22222222 = authKeyAlgoType;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th10) {
                    th = th10;
                    int i6 = authVersion;
                    int i52 = taVersion;
                    int i422 = policy;
                    long j2222 = userID;
                    byte[] bArr3222 = AESTmpKey;
                    byte[] bArr222222 = tcisId;
                    int i322222 = pkVersion;
                    long j3222222 = nonce;
                    int i222222222 = authKeyAlgoType;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(tcisIdSlave);
                    _data.writeInt(pkVersionSlave);
                    _data.writeLong(nonceSlave);
                    _data.writeByteArray(mac);
                    _data.writeInt(authKeyAlgoTypeSlave);
                    _data.writeByteArray(authKeyInfoSlave);
                    _data.writeByteArray(authKeyInfoSignSlave);
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

            public boolean receiveAck(long authID, byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(mac);
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean requestPK(long authID, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeLong(userID);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(authKeyAlgoType);
                    _data.writeByteArray(authKeyData);
                    _data.writeByteArray(authKeyDataSign);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
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

            public void cancelAuthentication(long authId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustCircleManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustCircleManager)) {
                return new Proxy(obj);
            }
            return (ITrustCircleManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        public boolean onTransact(int r34, android.os.Parcel r35, android.os.Parcel r36, int r37) throws android.os.RemoteException {
            /*
                r33 = this;
                r15 = r33
                r14 = r34
                r13 = r35
                r11 = r36
                java.lang.String r12 = "huawei.android.security.ITrustCircleManager"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r10 = 1
                if (r14 == r0) goto L_0x02a1
                switch(r14) {
                    case 1: goto L_0x0284;
                    case 2: goto L_0x0251;
                    case 3: goto L_0x023e;
                    case 4: goto L_0x0216;
                    case 5: goto L_0x01fb;
                    case 6: goto L_0x01ce;
                    case 7: goto L_0x01ab;
                    case 8: goto L_0x0190;
                    case 9: goto L_0x0175;
                    case 10: goto L_0x0159;
                    case 11: goto L_0x011d;
                    case 12: goto L_0x00b6;
                    case 13: goto L_0x0076;
                    case 14: goto L_0x0060;
                    case 15: goto L_0x004a;
                    case 16: goto L_0x0026;
                    case 17: goto L_0x0018;
                    default: goto L_0x0013;
                }
            L_0x0013:
                boolean r0 = super.onTransact(r34, r35, r36, r37)
                return r0
            L_0x0018:
                r13.enforceInterface(r12)
                long r0 = r35.readLong()
                r15.cancelAuthentication(r0)
                r36.writeNoException()
                return r10
            L_0x0026:
                r13.enforceInterface(r12)
                long r6 = r35.readLong()
                int r8 = r35.readInt()
                byte[] r9 = r35.createByteArray()
                byte[] r16 = r35.createByteArray()
                r0 = r15
                r1 = r6
                r3 = r8
                r4 = r9
                r5 = r16
                boolean r0 = r0.receivePK(r1, r3, r4, r5)
                r36.writeNoException()
                r11.writeInt(r0)
                return r10
            L_0x004a:
                r13.enforceInterface(r12)
                long r0 = r35.readLong()
                long r2 = r35.readLong()
                boolean r4 = r15.requestPK(r0, r2)
                r36.writeNoException()
                r11.writeInt(r4)
                return r10
            L_0x0060:
                r13.enforceInterface(r12)
                long r0 = r35.readLong()
                byte[] r2 = r35.createByteArray()
                boolean r3 = r15.receiveAck(r0, r2)
                r36.writeNoException()
                r11.writeInt(r3)
                return r10
            L_0x0076:
                r13.enforceInterface(r12)
                long r16 = r35.readLong()
                byte[] r18 = r35.createByteArray()
                int r19 = r35.readInt()
                long r20 = r35.readLong()
                byte[] r22 = r35.createByteArray()
                int r23 = r35.readInt()
                byte[] r24 = r35.createByteArray()
                byte[] r25 = r35.createByteArray()
                r0 = r15
                r1 = r16
                r3 = r18
                r4 = r19
                r5 = r20
                r7 = r22
                r8 = r23
                r9 = r24
                r14 = r10
                r10 = r25
                boolean r0 = r0.receiveAuthSyncAck(r1, r3, r4, r5, r7, r8, r9, r10)
                r36.writeNoException()
                r11.writeInt(r0)
                return r14
            L_0x00b6:
                r14 = r10
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.IAuthCallback r16 = huawei.android.security.IAuthCallback.Stub.asInterface(r0)
                int r17 = r35.readInt()
                int r18 = r35.readInt()
                int r19 = r35.readInt()
                int r20 = r35.readInt()
                long r21 = r35.readLong()
                byte[] r23 = r35.createByteArray()
                byte[] r24 = r35.createByteArray()
                int r25 = r35.readInt()
                long r26 = r35.readLong()
                int r28 = r35.readInt()
                byte[] r29 = r35.createByteArray()
                byte[] r30 = r35.createByteArray()
                r0 = r15
                r1 = r16
                r2 = r17
                r3 = r18
                r4 = r19
                r5 = r20
                r6 = r21
                r8 = r23
                r9 = r24
                r10 = r25
                r31 = r12
                r11 = r26
                r13 = r28
                r14 = r29
                r15 = r30
                long r0 = r0.receiveAuthSync(r1, r2, r3, r4, r5, r6, r8, r9, r10, r11, r13, r14, r15)
                r36.writeNoException()
                r8 = r36
                r8.writeLong(r0)
                r9 = 1
                return r9
            L_0x011d:
                r9 = r10
                r8 = r11
                r31 = r12
                r11 = r31
                r10 = r35
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.IAuthCallback r12 = huawei.android.security.IAuthCallback.Stub.asInterface(r0)
                int r13 = r35.readInt()
                int r14 = r35.readInt()
                int r15 = r35.readInt()
                long r16 = r35.readLong()
                byte[] r18 = r35.createByteArray()
                r0 = r33
                r1 = r12
                r2 = r13
                r3 = r14
                r4 = r15
                r5 = r16
                r7 = r18
                long r0 = r0.initAuthenticate(r1, r2, r3, r4, r5, r7)
                r36.writeNoException()
                r8.writeLong(r0)
                return r9
            L_0x0159:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r0 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                long r1 = r35.readLong()
                r7 = r33
                r7.unregister(r0, r1)
                r36.writeNoException()
                return r9
            L_0x0175:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r0 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                long r1 = r35.readLong()
                r7.logout(r0, r1)
                r36.writeNoException()
                return r9
            L_0x0190:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r0 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                long r1 = r35.readLong()
                r7.cancelRegOrLogin(r0, r1)
                r36.writeNoException()
                return r9
            L_0x01ab:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r0 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                int r1 = r35.readInt()
                java.lang.String r2 = r35.readString()
                java.lang.String r3 = r35.readString()
                r7.finalLogin(r0, r1, r2, r3)
                r36.writeNoException()
                return r9
            L_0x01ce:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r6 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                java.lang.String r12 = r35.readString()
                java.lang.String r13 = r35.readString()
                java.lang.String r14 = r35.readString()
                java.lang.String r15 = r35.readString()
                r0 = r7
                r1 = r6
                r2 = r12
                r3 = r13
                r4 = r14
                r5 = r15
                r0.finalRegister(r1, r2, r3, r4, r5)
                r36.writeNoException()
                return r9
            L_0x01fb:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r0 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                long r1 = r35.readLong()
                r7.updateServerRequest(r0, r1)
                r36.writeNoException()
                return r9
            L_0x0216:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.ILifeCycleCallback r6 = huawei.android.security.ILifeCycleCallback.Stub.asInterface(r0)
                long r12 = r35.readLong()
                int r14 = r35.readInt()
                java.lang.String r15 = r35.readString()
                r0 = r7
                r1 = r6
                r2 = r12
                r4 = r14
                r5 = r15
                r0.loginServerRequest(r1, r2, r4, r5)
                r36.writeNoException()
                return r9
            L_0x023e:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                int r0 = r33.getCurrentState()
                r36.writeNoException()
                r8.writeInt(r0)
                return r9
            L_0x0251:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r35.readStrongBinder()
                huawei.android.security.IKaCallback r12 = huawei.android.security.IKaCallback.Stub.asInterface(r0)
                int r13 = r35.readInt()
                long r14 = r35.readLong()
                byte[] r16 = r35.createByteArray()
                java.lang.String r17 = r35.readString()
                r0 = r7
                r1 = r12
                r2 = r13
                r3 = r14
                r5 = r16
                r6 = r17
                long r0 = r0.initKeyAgreement(r1, r2, r3, r5, r6)
                r36.writeNoException()
                r8.writeLong(r0)
                return r9
            L_0x0284:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r10.enforceInterface(r11)
                android.os.Bundle r0 = r33.getTcisInfo()
                r36.writeNoException()
                if (r0 == 0) goto L_0x029c
                r8.writeInt(r9)
                r0.writeToParcel(r8, r9)
                goto L_0x02a0
            L_0x029c:
                r1 = 0
                r8.writeInt(r1)
            L_0x02a0:
                return r9
            L_0x02a1:
                r9 = r10
                r8 = r11
                r11 = r12
                r10 = r13
                r7 = r15
                r8.writeString(r11)
                return r9
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.security.ITrustCircleManager.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void cancelAuthentication(long j) throws RemoteException;

    void cancelRegOrLogin(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    void finalLogin(ILifeCycleCallback iLifeCycleCallback, int i, String str, String str2) throws RemoteException;

    void finalRegister(ILifeCycleCallback iLifeCycleCallback, String str, String str2, String str3, String str4) throws RemoteException;

    int getCurrentState() throws RemoteException;

    Bundle getTcisInfo() throws RemoteException;

    long initAuthenticate(IAuthCallback iAuthCallback, int i, int i2, int i3, long j, byte[] bArr) throws RemoteException;

    long initKeyAgreement(IKaCallback iKaCallback, int i, long j, byte[] bArr, String str) throws RemoteException;

    void loginServerRequest(ILifeCycleCallback iLifeCycleCallback, long j, int i, String str) throws RemoteException;

    void logout(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    boolean receiveAck(long j, byte[] bArr) throws RemoteException;

    long receiveAuthSync(IAuthCallback iAuthCallback, int i, int i2, int i3, int i4, long j, byte[] bArr, byte[] bArr2, int i5, long j2, int i6, byte[] bArr3, byte[] bArr4) throws RemoteException;

    boolean receiveAuthSyncAck(long j, byte[] bArr, int i, long j2, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4) throws RemoteException;

    boolean receivePK(long j, int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean requestPK(long j, long j2) throws RemoteException;

    void unregister(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    void updateServerRequest(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;
}

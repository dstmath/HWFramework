package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwEidPlugin extends IInterface {

    public static abstract class Stub extends Binder implements IHwEidPlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwEidPlugin";
        static final int TRANSACTION_ctid_get_sec_image = 11;
        static final int TRANSACTION_ctid_get_service_verion_info = 12;
        static final int TRANSACTION_ctid_set_sec_mode = 10;
        static final int TRANSACTION_eidGetSecImageZip = 13;
        static final int TRANSACTION_eidGetUnsecImageZip = 14;
        static final int TRANSACTION_eid_finish = 2;
        static final int TRANSACTION_eid_get_certificate_request_message = 5;
        static final int TRANSACTION_eid_get_face_is_changed = 8;
        static final int TRANSACTION_eid_get_identity_information = 7;
        static final int TRANSACTION_eid_get_image = 3;
        static final int TRANSACTION_eid_get_unsec_image = 4;
        static final int TRANSACTION_eid_get_version = 9;
        static final int TRANSACTION_eid_init = 1;
        static final int TRANSACTION_eid_sign_info = 6;

        private static class Proxy implements IHwEidPlugin {
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

            public int eid_init(byte[] hw_aid, int hw_aid_len, byte[] eid_aid, int eid_aid_len, byte[] logo, int logo_size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(hw_aid);
                    _data.writeInt(hw_aid_len);
                    _data.writeByteArray(eid_aid);
                    _data.writeInt(eid_aid_len);
                    _data.writeByteArray(logo);
                    _data.writeInt(logo_size);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_finish() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_get_image(int transpotCounter, int encryption_method, byte[] certificate, int certificate_len, byte[] image, int[] image_len, byte[] de_skey, int[] de_skey_len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transpotCounter);
                    _data.writeInt(encryption_method);
                    _data.writeByteArray(certificate);
                    _data.writeInt(certificate_len);
                    if (image == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(image.length);
                    }
                    if (image_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(image_len.length);
                    }
                    if (de_skey == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(de_skey.length);
                    }
                    if (de_skey_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(de_skey_len.length);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(image);
                    _reply.readIntArray(image_len);
                    _reply.readByteArray(de_skey);
                    _reply.readIntArray(de_skey_len);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_get_unsec_image(byte[] src_image, int src_image_len, int transpotCounter, int encryption_method, byte[] certificate, int certificate_len, byte[] image, int[] image_len, byte[] de_skey, int[] de_skey_len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(src_image);
                    _data.writeInt(src_image_len);
                    _data.writeInt(transpotCounter);
                    _data.writeInt(encryption_method);
                    _data.writeByteArray(certificate);
                    _data.writeInt(certificate_len);
                    if (image == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(image.length);
                    }
                    if (image_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(image_len.length);
                    }
                    if (de_skey == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(de_skey.length);
                    }
                    if (de_skey_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(de_skey_len.length);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(image);
                    _reply.readIntArray(image_len);
                    _reply.readByteArray(de_skey);
                    _reply.readIntArray(de_skey_len);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_get_certificate_request_message(byte[] request_message, int[] message_len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request_message == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(request_message.length);
                    }
                    if (message_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(message_len.length);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(request_message);
                    _reply.readIntArray(message_len);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_sign_info(int transpotCounter, int encryption_method, byte[] info, int info_len, byte[] sign, int[] sign_len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transpotCounter);
                    _data.writeInt(encryption_method);
                    _data.writeByteArray(info);
                    _data.writeInt(info_len);
                    if (sign == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(sign.length);
                    }
                    if (sign_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(sign_len.length);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(sign);
                    _reply.readIntArray(sign_len);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_get_identity_information(byte[] identity_info, int[] identity_info_len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identity_info == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(identity_info.length);
                    }
                    if (identity_info_len == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(identity_info_len.length);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(identity_info);
                    _reply.readIntArray(identity_info_len);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eid_get_face_is_changed(int cmd_id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd_id);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String eid_get_version() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ctid_set_sec_mode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ctid_get_sec_image() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ctid_get_service_verion_info(byte[] uuid, int uuid_len, String ta_path, int[] cmd_list, int cmd_count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(uuid);
                    _data.writeInt(uuid_len);
                    _data.writeString(ta_path);
                    _data.writeIntArray(cmd_list);
                    _data.writeInt(cmd_count);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int eidGetSecImageZip(int hash_len, byte[] hash, int image_zip_len, byte[] image_zip, int up, int down, int left, int right, int encryption_method, int certificate_len, byte[] certificate, int[] sec_image_len, byte[] sec_image, int[] de_skey_len, byte[] de_skey) throws RemoteException {
                int[] iArr = sec_image_len;
                byte[] bArr = sec_image;
                int[] iArr2 = de_skey_len;
                byte[] bArr2 = de_skey;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hash_len);
                    _data.writeByteArray(hash);
                    try {
                        _data.writeInt(image_zip_len);
                        try {
                            _data.writeByteArray(image_zip);
                            try {
                                _data.writeInt(up);
                            } catch (Throwable th) {
                                th = th;
                                int i = down;
                                int i2 = left;
                                int i3 = right;
                                int i4 = encryption_method;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            int i5 = up;
                            int i6 = down;
                            int i22 = left;
                            int i32 = right;
                            int i42 = encryption_method;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        byte[] bArr3 = image_zip;
                        int i52 = up;
                        int i62 = down;
                        int i222 = left;
                        int i322 = right;
                        int i422 = encryption_method;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(down);
                        try {
                            _data.writeInt(left);
                            try {
                                _data.writeInt(right);
                            } catch (Throwable th4) {
                                th = th4;
                                int i4222 = encryption_method;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            int i3222 = right;
                            int i42222 = encryption_method;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i2222 = left;
                        int i32222 = right;
                        int i422222 = encryption_method;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(encryption_method);
                        _data.writeInt(certificate_len);
                        _data.writeByteArray(certificate);
                        if (iArr == null) {
                            _data.writeInt(-1);
                        } else {
                            _data.writeInt(iArr.length);
                        }
                        if (bArr == null) {
                            _data.writeInt(-1);
                        } else {
                            _data.writeInt(bArr.length);
                        }
                        if (iArr2 == null) {
                            _data.writeInt(-1);
                        } else {
                            _data.writeInt(iArr2.length);
                        }
                        if (bArr2 == null) {
                            _data.writeInt(-1);
                        } else {
                            _data.writeInt(bArr2.length);
                        }
                        this.mRemote.transact(13, _data, _reply, 0);
                        _reply.readException();
                        int _result = _reply.readInt();
                        _reply.readIntArray(iArr);
                        _reply.readByteArray(bArr);
                        _reply.readIntArray(iArr2);
                        _reply.readByteArray(bArr2);
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
                    int i7 = image_zip_len;
                    byte[] bArr32 = image_zip;
                    int i522 = up;
                    int i622 = down;
                    int i22222 = left;
                    int i322222 = right;
                    int i4222222 = encryption_method;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int eidGetUnsecImageZip(int hash_len, byte[] hash, int image_zip_len, byte[] image_zip, int encryption_method, int certificate_len, byte[] certificate, int[] sec_image_len, byte[] sec_image, int[] de_skey_len, byte[] de_skey) throws RemoteException {
                int[] iArr = sec_image_len;
                byte[] bArr = sec_image;
                int[] iArr2 = de_skey_len;
                byte[] bArr2 = de_skey;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hash_len);
                    try {
                        _data.writeByteArray(hash);
                    } catch (Throwable th) {
                        th = th;
                        int i = image_zip_len;
                        byte[] bArr3 = image_zip;
                        int i2 = encryption_method;
                        int i3 = certificate_len;
                        byte[] bArr4 = certificate;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(image_zip_len);
                        try {
                            _data.writeByteArray(image_zip);
                            try {
                                _data.writeInt(encryption_method);
                            } catch (Throwable th2) {
                                th = th2;
                                int i32 = certificate_len;
                                byte[] bArr42 = certificate;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i22 = encryption_method;
                            int i322 = certificate_len;
                            byte[] bArr422 = certificate;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        byte[] bArr32 = image_zip;
                        int i222 = encryption_method;
                        int i3222 = certificate_len;
                        byte[] bArr4222 = certificate;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(certificate_len);
                        try {
                            _data.writeByteArray(certificate);
                            if (iArr == null) {
                                _data.writeInt(-1);
                            } else {
                                _data.writeInt(iArr.length);
                            }
                            if (bArr == null) {
                                _data.writeInt(-1);
                            } else {
                                _data.writeInt(bArr.length);
                            }
                            if (iArr2 == null) {
                                _data.writeInt(-1);
                            } else {
                                _data.writeInt(iArr2.length);
                            }
                            if (bArr2 == null) {
                                _data.writeInt(-1);
                            } else {
                                _data.writeInt(bArr2.length);
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            this.mRemote.transact(14, _data, _reply, 0);
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.readIntArray(iArr);
                            _reply.readByteArray(bArr);
                            _reply.readIntArray(iArr2);
                            _reply.readByteArray(bArr2);
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        byte[] bArr42222 = certificate;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    byte[] bArr5 = hash;
                    int i4 = image_zip_len;
                    byte[] bArr322 = image_zip;
                    int i2222 = encryption_method;
                    int i32222 = certificate_len;
                    byte[] bArr422222 = certificate;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwEidPlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwEidPlugin)) {
                return new Proxy(obj);
            }
            return (IHwEidPlugin) iin;
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
        public boolean onTransact(int r49, android.os.Parcel r50, android.os.Parcel r51, int r52) throws android.os.RemoteException {
            /*
                r48 = this;
                r15 = r48
                r14 = r49
                r13 = r50
                r12 = r51
                java.lang.String r11 = "huawei.android.security.IHwEidPlugin"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r16 = 1
                if (r14 == r0) goto L_0x03e6
                switch(r14) {
                    case 1: goto L_0x03b1;
                    case 2: goto L_0x039c;
                    case 3: goto L_0x031b;
                    case 4: goto L_0x028c;
                    case 5: goto L_0x025d;
                    case 6: goto L_0x020b;
                    case 7: goto L_0x01dc;
                    case 8: goto L_0x01c2;
                    case 9: goto L_0x01ae;
                    case 10: goto L_0x019a;
                    case 11: goto L_0x0186;
                    case 12: goto L_0x0156;
                    case 13: goto L_0x00a7;
                    case 14: goto L_0x0019;
                    default: goto L_0x0014;
                }
            L_0x0014:
                boolean r0 = super.onTransact(r49, r50, r51, r52)
                return r0
            L_0x0019:
                r13.enforceInterface(r11)
                int r17 = r50.readInt()
                byte[] r18 = r50.createByteArray()
                int r19 = r50.readInt()
                byte[] r20 = r50.createByteArray()
                int r21 = r50.readInt()
                int r22 = r50.readInt()
                byte[] r23 = r50.createByteArray()
                int r10 = r50.readInt()
                if (r10 >= 0) goto L_0x0040
                r0 = 0
                goto L_0x0042
            L_0x0040:
                int[] r0 = new int[r10]
            L_0x0042:
                r9 = r0
                int r8 = r50.readInt()
                if (r8 >= 0) goto L_0x004b
                r0 = 0
                goto L_0x004d
            L_0x004b:
                byte[] r0 = new byte[r8]
            L_0x004d:
                r7 = r0
                int r6 = r50.readInt()
                if (r6 >= 0) goto L_0x0056
                r0 = 0
                goto L_0x0058
            L_0x0056:
                int[] r0 = new int[r6]
            L_0x0058:
                r5 = r0
                int r4 = r50.readInt()
                if (r4 >= 0) goto L_0x0061
                r0 = 0
                goto L_0x0063
            L_0x0061:
                byte[] r0 = new byte[r4]
            L_0x0063:
                r3 = r0
                r0 = r15
                r1 = r17
                r2 = r18
                r24 = r3
                r3 = r19
                r25 = r4
                r4 = r20
                r26 = r5
                r5 = r21
                r27 = r6
                r6 = r22
                r28 = r7
                r7 = r23
                r29 = r8
                r8 = r9
                r14 = r9
                r9 = r28
                r31 = r10
                r10 = r26
                r15 = r11
                r11 = r24
                int r0 = r0.eidGetUnsecImageZip(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r51.writeNoException()
                r12.writeInt(r0)
                r12.writeIntArray(r14)
                r1 = r28
                r12.writeByteArray(r1)
                r2 = r26
                r12.writeIntArray(r2)
                r3 = r24
                r12.writeByteArray(r3)
                return r16
            L_0x00a7:
                r15 = r11
                r13.enforceInterface(r15)
                int r17 = r50.readInt()
                byte[] r18 = r50.createByteArray()
                int r19 = r50.readInt()
                byte[] r20 = r50.createByteArray()
                int r21 = r50.readInt()
                int r22 = r50.readInt()
                int r23 = r50.readInt()
                int r24 = r50.readInt()
                int r25 = r50.readInt()
                int r26 = r50.readInt()
                byte[] r27 = r50.createByteArray()
                int r14 = r50.readInt()
                if (r14 >= 0) goto L_0x00df
                r0 = 0
                goto L_0x00e1
            L_0x00df:
                int[] r0 = new int[r14]
            L_0x00e1:
                r11 = r0
                int r10 = r50.readInt()
                if (r10 >= 0) goto L_0x00ea
                r0 = 0
                goto L_0x00ec
            L_0x00ea:
                byte[] r0 = new byte[r10]
            L_0x00ec:
                r9 = r0
                int r8 = r50.readInt()
                if (r8 >= 0) goto L_0x00f5
                r0 = 0
                goto L_0x00f7
            L_0x00f5:
                int[] r0 = new int[r8]
            L_0x00f7:
                r7 = r0
                int r6 = r50.readInt()
                if (r6 >= 0) goto L_0x0100
                r0 = 0
                goto L_0x0102
            L_0x0100:
                byte[] r0 = new byte[r6]
            L_0x0102:
                r5 = r0
                r0 = r48
                r1 = r17
                r2 = r18
                r3 = r19
                r4 = r20
                r32 = r5
                r5 = r21
                r28 = r6
                r6 = r22
                r33 = r7
                r7 = r23
                r29 = r8
                r8 = r24
                r34 = r9
                r9 = r25
                r31 = r10
                r10 = r26
                r35 = r11
                r11 = r27
                r12 = r35
                r13 = r34
                r30 = r14
                r14 = r33
                r36 = r15
                r15 = r32
                int r0 = r0.eidGetSecImageZip(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
                r51.writeNoException()
                r11 = r51
                r11.writeInt(r0)
                r1 = r35
                r11.writeIntArray(r1)
                r2 = r34
                r11.writeByteArray(r2)
                r3 = r33
                r11.writeIntArray(r3)
                r4 = r32
                r11.writeByteArray(r4)
                return r16
            L_0x0156:
                r36 = r11
                r11 = r12
                r13 = r36
                r12 = r50
                r12.enforceInterface(r13)
                byte[] r6 = r50.createByteArray()
                int r7 = r50.readInt()
                java.lang.String r8 = r50.readString()
                int[] r9 = r50.createIntArray()
                int r10 = r50.readInt()
                r0 = r48
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r10
                int r0 = r0.ctid_get_service_verion_info(r1, r2, r3, r4, r5)
                r51.writeNoException()
                r11.writeInt(r0)
                return r16
            L_0x0186:
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r0 = r48.ctid_get_sec_image()
                r51.writeNoException()
                r11.writeInt(r0)
                return r16
            L_0x019a:
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r0 = r48.ctid_set_sec_mode()
                r51.writeNoException()
                r11.writeInt(r0)
                return r16
            L_0x01ae:
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                java.lang.String r0 = r48.eid_get_version()
                r51.writeNoException()
                r11.writeString(r0)
                return r16
            L_0x01c2:
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r0 = r50.readInt()
                r14 = r48
                int r1 = r14.eid_get_face_is_changed(r0)
                r51.writeNoException()
                r11.writeInt(r1)
                return r16
            L_0x01dc:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r0 = r50.readInt()
                if (r0 >= 0) goto L_0x01ee
                r1 = 0
                goto L_0x01f0
            L_0x01ee:
                byte[] r1 = new byte[r0]
            L_0x01f0:
                int r2 = r50.readInt()
                if (r2 >= 0) goto L_0x01f8
                r3 = 0
                goto L_0x01fa
            L_0x01f8:
                int[] r3 = new int[r2]
            L_0x01fa:
                int r4 = r14.eid_get_identity_information(r1, r3)
                r51.writeNoException()
                r11.writeInt(r4)
                r11.writeByteArray(r1)
                r11.writeIntArray(r3)
                return r16
            L_0x020b:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r7 = r50.readInt()
                int r8 = r50.readInt()
                byte[] r9 = r50.createByteArray()
                int r10 = r50.readInt()
                int r15 = r50.readInt()
                if (r15 >= 0) goto L_0x022d
                r0 = 0
                goto L_0x022f
            L_0x022d:
                byte[] r0 = new byte[r15]
            L_0x022f:
                r6 = r0
                int r5 = r50.readInt()
                if (r5 >= 0) goto L_0x0238
                r0 = 0
                goto L_0x023a
            L_0x0238:
                int[] r0 = new int[r5]
            L_0x023a:
                r4 = r0
                r0 = r14
                r1 = r7
                r2 = r8
                r3 = r9
                r37 = r4
                r4 = r10
                r17 = r5
                r5 = r6
                r38 = r7
                r7 = r6
                r6 = r37
                int r0 = r0.eid_sign_info(r1, r2, r3, r4, r5, r6)
                r51.writeNoException()
                r11.writeInt(r0)
                r11.writeByteArray(r7)
                r1 = r37
                r11.writeIntArray(r1)
                return r16
            L_0x025d:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r0 = r50.readInt()
                if (r0 >= 0) goto L_0x026f
                r1 = 0
                goto L_0x0271
            L_0x026f:
                byte[] r1 = new byte[r0]
            L_0x0271:
                int r2 = r50.readInt()
                if (r2 >= 0) goto L_0x0279
                r3 = 0
                goto L_0x027b
            L_0x0279:
                int[] r3 = new int[r2]
            L_0x027b:
                int r4 = r14.eid_get_certificate_request_message(r1, r3)
                r51.writeNoException()
                r11.writeInt(r4)
                r11.writeByteArray(r1)
                r11.writeIntArray(r3)
                return r16
            L_0x028c:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                byte[] r15 = r50.createByteArray()
                int r17 = r50.readInt()
                int r18 = r50.readInt()
                int r19 = r50.readInt()
                byte[] r20 = r50.createByteArray()
                int r21 = r50.readInt()
                int r10 = r50.readInt()
                if (r10 >= 0) goto L_0x02b6
                r0 = 0
                goto L_0x02b8
            L_0x02b6:
                byte[] r0 = new byte[r10]
            L_0x02b8:
                r9 = r0
                int r8 = r50.readInt()
                if (r8 >= 0) goto L_0x02c1
                r0 = 0
                goto L_0x02c3
            L_0x02c1:
                int[] r0 = new int[r8]
            L_0x02c3:
                r7 = r0
                int r6 = r50.readInt()
                if (r6 >= 0) goto L_0x02cc
                r0 = 0
                goto L_0x02ce
            L_0x02cc:
                byte[] r0 = new byte[r6]
            L_0x02ce:
                r5 = r0
                int r4 = r50.readInt()
                if (r4 >= 0) goto L_0x02d7
                r0 = 0
                goto L_0x02d9
            L_0x02d7:
                int[] r0 = new int[r4]
            L_0x02d9:
                r3 = r0
                r0 = r14
                r1 = r15
                r2 = r17
                r39 = r3
                r3 = r18
                r22 = r4
                r4 = r19
                r40 = r5
                r5 = r20
                r23 = r6
                r6 = r21
                r41 = r7
                r7 = r9
                r24 = r8
                r8 = r41
                r42 = r15
                r15 = r9
                r9 = r40
                r25 = r10
                r10 = r39
                int r0 = r0.eid_get_unsec_image(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r51.writeNoException()
                r11.writeInt(r0)
                r11.writeByteArray(r15)
                r1 = r41
                r11.writeIntArray(r1)
                r2 = r40
                r11.writeByteArray(r2)
                r3 = r39
                r11.writeIntArray(r3)
                return r16
            L_0x031b:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r9 = r50.readInt()
                int r10 = r50.readInt()
                byte[] r15 = r50.createByteArray()
                int r17 = r50.readInt()
                int r8 = r50.readInt()
                if (r8 >= 0) goto L_0x033d
                r0 = 0
                goto L_0x033f
            L_0x033d:
                byte[] r0 = new byte[r8]
            L_0x033f:
                r7 = r0
                int r6 = r50.readInt()
                if (r6 >= 0) goto L_0x0348
                r0 = 0
                goto L_0x034a
            L_0x0348:
                int[] r0 = new int[r6]
            L_0x034a:
                r5 = r0
                int r4 = r50.readInt()
                if (r4 >= 0) goto L_0x0353
                r0 = 0
                goto L_0x0355
            L_0x0353:
                byte[] r0 = new byte[r4]
            L_0x0355:
                r3 = r0
                int r2 = r50.readInt()
                if (r2 >= 0) goto L_0x035e
                r0 = 0
                goto L_0x0360
            L_0x035e:
                int[] r0 = new int[r2]
            L_0x0360:
                r1 = r0
                r0 = r14
                r43 = r1
                r1 = r9
                r18 = r2
                r2 = r10
                r44 = r3
                r3 = r15
                r19 = r4
                r4 = r17
                r45 = r5
                r5 = r7
                r20 = r6
                r6 = r45
                r46 = r9
                r9 = r7
                r7 = r44
                r21 = r8
                r8 = r43
                int r0 = r0.eid_get_image(r1, r2, r3, r4, r5, r6, r7, r8)
                r51.writeNoException()
                r11.writeInt(r0)
                r11.writeByteArray(r9)
                r1 = r45
                r11.writeIntArray(r1)
                r2 = r44
                r11.writeByteArray(r2)
                r3 = r43
                r11.writeIntArray(r3)
                return r16
            L_0x039c:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                int r0 = r48.eid_finish()
                r51.writeNoException()
                r11.writeInt(r0)
                return r16
            L_0x03b1:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r12.enforceInterface(r13)
                byte[] r7 = r50.createByteArray()
                int r8 = r50.readInt()
                byte[] r9 = r50.createByteArray()
                int r10 = r50.readInt()
                byte[] r15 = r50.createByteArray()
                int r17 = r50.readInt()
                r0 = r14
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r15
                r6 = r17
                int r0 = r0.eid_init(r1, r2, r3, r4, r5, r6)
                r51.writeNoException()
                r11.writeInt(r0)
                return r16
            L_0x03e6:
                r14 = r15
                r47 = r13
                r13 = r11
                r11 = r12
                r12 = r47
                r11.writeString(r13)
                return r16
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.security.IHwEidPlugin.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    int ctid_get_sec_image() throws RemoteException;

    int ctid_get_service_verion_info(byte[] bArr, int i, String str, int[] iArr, int i2) throws RemoteException;

    int ctid_set_sec_mode() throws RemoteException;

    int eidGetSecImageZip(int i, byte[] bArr, int i2, byte[] bArr2, int i3, int i4, int i5, int i6, int i7, int i8, byte[] bArr3, int[] iArr, byte[] bArr4, int[] iArr2, byte[] bArr5) throws RemoteException;

    int eidGetUnsecImageZip(int i, byte[] bArr, int i2, byte[] bArr2, int i3, int i4, byte[] bArr3, int[] iArr, byte[] bArr4, int[] iArr2, byte[] bArr5) throws RemoteException;

    int eid_finish() throws RemoteException;

    int eid_get_certificate_request_message(byte[] bArr, int[] iArr) throws RemoteException;

    int eid_get_face_is_changed(int i) throws RemoteException;

    int eid_get_identity_information(byte[] bArr, int[] iArr) throws RemoteException;

    int eid_get_image(int i, int i2, byte[] bArr, int i3, byte[] bArr2, int[] iArr, byte[] bArr3, int[] iArr2) throws RemoteException;

    int eid_get_unsec_image(byte[] bArr, int i, int i2, int i3, byte[] bArr2, int i4, byte[] bArr3, int[] iArr, byte[] bArr4, int[] iArr2) throws RemoteException;

    String eid_get_version() throws RemoteException;

    int eid_init(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3) throws RemoteException;

    int eid_sign_info(int i, int i2, byte[] bArr, int i3, byte[] bArr2, int[] iArr) throws RemoteException;
}

package android.view.autofill;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.autofill.FillEventHistory;
import android.service.autofill.UserData;
import java.util.List;

public interface IAutoFillManager extends IInterface {

    public static abstract class Stub extends Binder implements IAutoFillManager {
        private static final String DESCRIPTOR = "android.view.autofill.IAutoFillManager";
        static final int TRANSACTION_addClient = 1;
        static final int TRANSACTION_cancelSession = 10;
        static final int TRANSACTION_disableOwnedAutofillServices = 13;
        static final int TRANSACTION_finishSession = 9;
        static final int TRANSACTION_getAutofillServiceComponentName = 21;
        static final int TRANSACTION_getAvailableFieldClassificationAlgorithms = 22;
        static final int TRANSACTION_getDefaultFieldClassificationAlgorithm = 23;
        static final int TRANSACTION_getFillEventHistory = 4;
        static final int TRANSACTION_getUserData = 17;
        static final int TRANSACTION_getUserDataId = 18;
        static final int TRANSACTION_isFieldClassificationEnabled = 20;
        static final int TRANSACTION_isServiceEnabled = 15;
        static final int TRANSACTION_isServiceSupported = 14;
        static final int TRANSACTION_onPendingSaveUi = 16;
        static final int TRANSACTION_removeClient = 2;
        static final int TRANSACTION_restoreSession = 5;
        static final int TRANSACTION_setAuthenticationResult = 11;
        static final int TRANSACTION_setAutofillFailure = 8;
        static final int TRANSACTION_setHasCallback = 12;
        static final int TRANSACTION_setUserData = 19;
        static final int TRANSACTION_startSession = 3;
        static final int TRANSACTION_updateOrRestartSession = 7;
        static final int TRANSACTION_updateSession = 6;

        private static class Proxy implements IAutoFillManager {
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

            public int addClient(IAutoFillManagerClient client, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeClient(IAutoFillManagerClient client, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(userId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startSession(IBinder activityToken, IBinder appCallback, AutofillId autoFillId, Rect bounds, AutofillValue value, int userId, boolean hasCallback, int flags, ComponentName componentName, boolean compatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeStrongBinder(appCallback);
                    if (autoFillId != null) {
                        _data.writeInt(1);
                        autoFillId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeInt(hasCallback);
                    _data.writeInt(flags);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(compatMode);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FillEventHistory getFillEventHistory() throws RemoteException {
                FillEventHistory _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = FillEventHistory.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean restoreSession(int sessionId, IBinder activityToken, IBinder appCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeStrongBinder(activityToken);
                    _data.writeStrongBinder(appCallback);
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

            public void updateSession(int sessionId, AutofillId id, Rect bounds, AutofillValue value, int action, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (id != null) {
                        _data.writeInt(1);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(action);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateOrRestartSession(IBinder activityToken, IBinder appCallback, AutofillId autoFillId, Rect bounds, AutofillValue value, int userId, boolean hasCallback, int flags, ComponentName componentName, int sessionId, int action, boolean compatMode) throws RemoteException {
                AutofillId autofillId = autoFillId;
                Rect rect = bounds;
                AutofillValue autofillValue = value;
                ComponentName componentName2 = componentName;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(activityToken);
                        try {
                            _data.writeStrongBinder(appCallback);
                            if (autofillId != null) {
                                _data.writeInt(1);
                                autofillId.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (rect != null) {
                                _data.writeInt(1);
                                rect.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (autofillValue != null) {
                                _data.writeInt(1);
                                autofillValue.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th) {
                            th = th;
                            int i = userId;
                            boolean z = hasCallback;
                            int i2 = flags;
                            int i3 = sessionId;
                            int i4 = action;
                            boolean z2 = compatMode;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(userId);
                            try {
                                _data.writeInt(hasCallback ? 1 : 0);
                            } catch (Throwable th2) {
                                th = th2;
                                int i22 = flags;
                                int i32 = sessionId;
                                int i42 = action;
                                boolean z22 = compatMode;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            boolean z3 = hasCallback;
                            int i222 = flags;
                            int i322 = sessionId;
                            int i422 = action;
                            boolean z222 = compatMode;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        IBinder iBinder = appCallback;
                        int i5 = userId;
                        boolean z32 = hasCallback;
                        int i2222 = flags;
                        int i3222 = sessionId;
                        int i4222 = action;
                        boolean z2222 = compatMode;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        if (componentName2 != null) {
                            _data.writeInt(1);
                            componentName2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(sessionId);
                            try {
                                _data.writeInt(action);
                                try {
                                    _data.writeInt(compatMode ? 1 : 0);
                                } catch (Throwable th5) {
                                    th = th5;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                boolean z22222 = compatMode;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            int i42222 = action;
                            boolean z222222 = compatMode;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            this.mRemote.transact(7, _data, _reply, 0);
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        } catch (Throwable th8) {
                            th = th8;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        int i32222 = sessionId;
                        int i422222 = action;
                        boolean z2222222 = compatMode;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th10) {
                    th = th10;
                    IBinder iBinder2 = activityToken;
                    IBinder iBinder3 = appCallback;
                    int i52 = userId;
                    boolean z322 = hasCallback;
                    int i22222 = flags;
                    int i322222 = sessionId;
                    int i4222222 = action;
                    boolean z22222222 = compatMode;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void setAutofillFailure(int sessionId, List<AutofillId> ids, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeTypedList(ids);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishSession(int sessionId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(userId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelSession(int sessionId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(userId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAuthenticationResult(Bundle data, int sessionId, int authenticationId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sessionId);
                    _data.writeInt(authenticationId);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHasCallback(int sessionId, int userId, boolean hasIt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(userId);
                    _data.writeInt(hasIt);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableOwnedAutofillServices(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isServiceSupported(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
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

            public boolean isServiceEnabled(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
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

            public void onPendingSaveUi(int operation, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UserData getUserData() throws RemoteException {
                UserData _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserData.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUserDataId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserData(UserData userData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (userData != null) {
                        _data.writeInt(1);
                        userData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFieldClassificationEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public ComponentName getAutofillServiceComponentName() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getAvailableFieldClassificationAlgorithms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDefaultFieldClassificationAlgorithm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAutoFillManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAutoFillManager)) {
                return new Proxy(obj);
            }
            return (IAutoFillManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v22, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v28, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v31, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v39, resolved type: android.service.autofill.UserData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v35, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v36, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v37, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v38, resolved type: android.content.ComponentName} */
        /* JADX WARNING: type inference failed for: r1v25, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r1v33, types: [android.service.autofill.UserData] */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r24, android.os.Parcel r25, android.os.Parcel r26, int r27) throws android.os.RemoteException {
            /*
                r23 = this;
                r13 = r23
                r14 = r24
                r15 = r25
                r11 = r26
                java.lang.String r10 = "android.view.autofill.IAutoFillManager"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r8 = 1
                if (r14 == r0) goto L_0x0330
                r0 = 0
                r1 = 0
                switch(r14) {
                    case 1: goto L_0x0314;
                    case 2: goto L_0x02fc;
                    case 3: goto L_0x027d;
                    case 4: goto L_0x0264;
                    case 5: goto L_0x0248;
                    case 6: goto L_0x01f0;
                    case 7: goto L_0x0162;
                    case 8: goto L_0x014a;
                    case 9: goto L_0x0138;
                    case 10: goto L_0x0126;
                    case 11: goto L_0x00fe;
                    case 12: goto L_0x00e4;
                    case 13: goto L_0x00d6;
                    case 14: goto L_0x00c4;
                    case 15: goto L_0x00ae;
                    case 16: goto L_0x009c;
                    case 17: goto L_0x0085;
                    case 18: goto L_0x0077;
                    case 19: goto L_0x005b;
                    case 20: goto L_0x004d;
                    case 21: goto L_0x0036;
                    case 22: goto L_0x0028;
                    case 23: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r24, r25, r26, r27)
                return r0
            L_0x001a:
                r15.enforceInterface(r10)
                java.lang.String r0 = r23.getDefaultFieldClassificationAlgorithm()
                r26.writeNoException()
                r11.writeString(r0)
                return r8
            L_0x0028:
                r15.enforceInterface(r10)
                java.lang.String[] r0 = r23.getAvailableFieldClassificationAlgorithms()
                r26.writeNoException()
                r11.writeStringArray(r0)
                return r8
            L_0x0036:
                r15.enforceInterface(r10)
                android.content.ComponentName r1 = r23.getAutofillServiceComponentName()
                r26.writeNoException()
                if (r1 == 0) goto L_0x0049
                r11.writeInt(r8)
                r1.writeToParcel(r11, r8)
                goto L_0x004c
            L_0x0049:
                r11.writeInt(r0)
            L_0x004c:
                return r8
            L_0x004d:
                r15.enforceInterface(r10)
                boolean r0 = r23.isFieldClassificationEnabled()
                r26.writeNoException()
                r11.writeInt(r0)
                return r8
            L_0x005b:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                if (r0 == 0) goto L_0x006e
                android.os.Parcelable$Creator<android.service.autofill.UserData> r0 = android.service.autofill.UserData.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                r1 = r0
                android.service.autofill.UserData r1 = (android.service.autofill.UserData) r1
                goto L_0x006f
            L_0x006e:
            L_0x006f:
                r0 = r1
                r13.setUserData(r0)
                r26.writeNoException()
                return r8
            L_0x0077:
                r15.enforceInterface(r10)
                java.lang.String r0 = r23.getUserDataId()
                r26.writeNoException()
                r11.writeString(r0)
                return r8
            L_0x0085:
                r15.enforceInterface(r10)
                android.service.autofill.UserData r1 = r23.getUserData()
                r26.writeNoException()
                if (r1 == 0) goto L_0x0098
                r11.writeInt(r8)
                r1.writeToParcel(r11, r8)
                goto L_0x009b
            L_0x0098:
                r11.writeInt(r0)
            L_0x009b:
                return r8
            L_0x009c:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                android.os.IBinder r1 = r25.readStrongBinder()
                r13.onPendingSaveUi(r0, r1)
                r26.writeNoException()
                return r8
            L_0x00ae:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                java.lang.String r1 = r25.readString()
                boolean r2 = r13.isServiceEnabled(r0, r1)
                r26.writeNoException()
                r11.writeInt(r2)
                return r8
            L_0x00c4:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                boolean r1 = r13.isServiceSupported(r0)
                r26.writeNoException()
                r11.writeInt(r1)
                return r8
            L_0x00d6:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                r13.disableOwnedAutofillServices(r0)
                r26.writeNoException()
                return r8
            L_0x00e4:
                r15.enforceInterface(r10)
                int r1 = r25.readInt()
                int r2 = r25.readInt()
                int r3 = r25.readInt()
                if (r3 == 0) goto L_0x00f7
                r0 = r8
            L_0x00f7:
                r13.setHasCallback(r1, r2, r0)
                r26.writeNoException()
                return r8
            L_0x00fe:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                if (r0 == 0) goto L_0x0111
                android.os.Parcelable$Creator<android.os.Bundle> r0 = android.os.Bundle.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                r1 = r0
                android.os.Bundle r1 = (android.os.Bundle) r1
                goto L_0x0112
            L_0x0111:
            L_0x0112:
                r0 = r1
                int r1 = r25.readInt()
                int r2 = r25.readInt()
                int r3 = r25.readInt()
                r13.setAuthenticationResult(r0, r1, r2, r3)
                r26.writeNoException()
                return r8
            L_0x0126:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                int r1 = r25.readInt()
                r13.cancelSession(r0, r1)
                r26.writeNoException()
                return r8
            L_0x0138:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                int r1 = r25.readInt()
                r13.finishSession(r0, r1)
                r26.writeNoException()
                return r8
            L_0x014a:
                r15.enforceInterface(r10)
                int r0 = r25.readInt()
                android.os.Parcelable$Creator<android.view.autofill.AutofillId> r1 = android.view.autofill.AutofillId.CREATOR
                java.util.ArrayList r1 = r15.createTypedArrayList(r1)
                int r2 = r25.readInt()
                r13.setAutofillFailure(r0, r1, r2)
                r26.writeNoException()
                return r8
            L_0x0162:
                r15.enforceInterface(r10)
                android.os.IBinder r16 = r25.readStrongBinder()
                android.os.IBinder r17 = r25.readStrongBinder()
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x017d
                android.os.Parcelable$Creator<android.view.autofill.AutofillId> r2 = android.view.autofill.AutofillId.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.view.autofill.AutofillId r2 = (android.view.autofill.AutofillId) r2
                r3 = r2
                goto L_0x017e
            L_0x017d:
                r3 = r1
            L_0x017e:
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x018e
                android.os.Parcelable$Creator r2 = android.graphics.Rect.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.graphics.Rect r2 = (android.graphics.Rect) r2
                r4 = r2
                goto L_0x018f
            L_0x018e:
                r4 = r1
            L_0x018f:
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x019f
                android.os.Parcelable$Creator<android.view.autofill.AutofillValue> r2 = android.view.autofill.AutofillValue.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.view.autofill.AutofillValue r2 = (android.view.autofill.AutofillValue) r2
                r5 = r2
                goto L_0x01a0
            L_0x019f:
                r5 = r1
            L_0x01a0:
                int r18 = r25.readInt()
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x01ac
                r7 = r8
                goto L_0x01ad
            L_0x01ac:
                r7 = r0
            L_0x01ad:
                int r19 = r25.readInt()
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x01c1
                android.os.Parcelable$Creator r1 = android.content.ComponentName.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.content.ComponentName r1 = (android.content.ComponentName) r1
            L_0x01bf:
                r9 = r1
                goto L_0x01c2
            L_0x01c1:
                goto L_0x01bf
            L_0x01c2:
                int r20 = r25.readInt()
                int r21 = r25.readInt()
                int r1 = r25.readInt()
                if (r1 == 0) goto L_0x01d2
                r12 = r8
                goto L_0x01d3
            L_0x01d2:
                r12 = r0
            L_0x01d3:
                r0 = r13
                r1 = r16
                r2 = r17
                r6 = r18
                r14 = r8
                r8 = r19
                r22 = r10
                r10 = r20
                r11 = r21
                int r0 = r0.updateOrRestartSession(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r26.writeNoException()
                r11 = r26
                r11.writeInt(r0)
                return r14
            L_0x01f0:
                r14 = r8
                r22 = r10
                r12 = r22
                r15.enforceInterface(r12)
                int r8 = r25.readInt()
                int r0 = r25.readInt()
                if (r0 == 0) goto L_0x020c
                android.os.Parcelable$Creator<android.view.autofill.AutofillId> r0 = android.view.autofill.AutofillId.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.view.autofill.AutofillId r0 = (android.view.autofill.AutofillId) r0
                r2 = r0
                goto L_0x020d
            L_0x020c:
                r2 = r1
            L_0x020d:
                int r0 = r25.readInt()
                if (r0 == 0) goto L_0x021d
                android.os.Parcelable$Creator r0 = android.graphics.Rect.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.graphics.Rect r0 = (android.graphics.Rect) r0
                r3 = r0
                goto L_0x021e
            L_0x021d:
                r3 = r1
            L_0x021e:
                int r0 = r25.readInt()
                if (r0 == 0) goto L_0x022e
                android.os.Parcelable$Creator<android.view.autofill.AutofillValue> r0 = android.view.autofill.AutofillValue.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.view.autofill.AutofillValue r0 = (android.view.autofill.AutofillValue) r0
                r4 = r0
                goto L_0x022f
            L_0x022e:
                r4 = r1
            L_0x022f:
                int r9 = r25.readInt()
                int r10 = r25.readInt()
                int r16 = r25.readInt()
                r0 = r13
                r1 = r8
                r5 = r9
                r6 = r10
                r7 = r16
                r0.updateSession(r1, r2, r3, r4, r5, r6, r7)
                r26.writeNoException()
                return r14
            L_0x0248:
                r14 = r8
                r12 = r10
                r15.enforceInterface(r12)
                int r0 = r25.readInt()
                android.os.IBinder r1 = r25.readStrongBinder()
                android.os.IBinder r2 = r25.readStrongBinder()
                boolean r3 = r13.restoreSession(r0, r1, r2)
                r26.writeNoException()
                r11.writeInt(r3)
                return r14
            L_0x0264:
                r14 = r8
                r12 = r10
                r15.enforceInterface(r12)
                android.service.autofill.FillEventHistory r1 = r23.getFillEventHistory()
                r26.writeNoException()
                if (r1 == 0) goto L_0x0279
                r11.writeInt(r14)
                r1.writeToParcel(r11, r14)
                goto L_0x027c
            L_0x0279:
                r11.writeInt(r0)
            L_0x027c:
                return r14
            L_0x027d:
                r14 = r8
                r12 = r10
                r15.enforceInterface(r12)
                android.os.IBinder r16 = r25.readStrongBinder()
                android.os.IBinder r17 = r25.readStrongBinder()
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x029a
                android.os.Parcelable$Creator<android.view.autofill.AutofillId> r2 = android.view.autofill.AutofillId.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.view.autofill.AutofillId r2 = (android.view.autofill.AutofillId) r2
                r3 = r2
                goto L_0x029b
            L_0x029a:
                r3 = r1
            L_0x029b:
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x02ab
                android.os.Parcelable$Creator r2 = android.graphics.Rect.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.graphics.Rect r2 = (android.graphics.Rect) r2
                r4 = r2
                goto L_0x02ac
            L_0x02ab:
                r4 = r1
            L_0x02ac:
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x02bc
                android.os.Parcelable$Creator<android.view.autofill.AutofillValue> r2 = android.view.autofill.AutofillValue.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.view.autofill.AutofillValue r2 = (android.view.autofill.AutofillValue) r2
                r5 = r2
                goto L_0x02bd
            L_0x02bc:
                r5 = r1
            L_0x02bd:
                int r18 = r25.readInt()
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x02c9
                r7 = r14
                goto L_0x02ca
            L_0x02c9:
                r7 = r0
            L_0x02ca:
                int r19 = r25.readInt()
                int r2 = r25.readInt()
                if (r2 == 0) goto L_0x02de
                android.os.Parcelable$Creator r1 = android.content.ComponentName.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.content.ComponentName r1 = (android.content.ComponentName) r1
            L_0x02dc:
                r9 = r1
                goto L_0x02df
            L_0x02de:
                goto L_0x02dc
            L_0x02df:
                int r1 = r25.readInt()
                if (r1 == 0) goto L_0x02e7
                r10 = r14
                goto L_0x02e8
            L_0x02e7:
                r10 = r0
            L_0x02e8:
                r0 = r13
                r1 = r16
                r2 = r17
                r6 = r18
                r8 = r19
                int r0 = r0.startSession(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r26.writeNoException()
                r11.writeInt(r0)
                return r14
            L_0x02fc:
                r14 = r8
                r12 = r10
                r15.enforceInterface(r12)
                android.os.IBinder r0 = r25.readStrongBinder()
                android.view.autofill.IAutoFillManagerClient r0 = android.view.autofill.IAutoFillManagerClient.Stub.asInterface(r0)
                int r1 = r25.readInt()
                r13.removeClient(r0, r1)
                r26.writeNoException()
                return r14
            L_0x0314:
                r14 = r8
                r12 = r10
                r15.enforceInterface(r12)
                android.os.IBinder r0 = r25.readStrongBinder()
                android.view.autofill.IAutoFillManagerClient r0 = android.view.autofill.IAutoFillManagerClient.Stub.asInterface(r0)
                int r1 = r25.readInt()
                int r2 = r13.addClient(r0, r1)
                r26.writeNoException()
                r11.writeInt(r2)
                return r14
            L_0x0330:
                r14 = r8
                r12 = r10
                r11.writeString(r12)
                return r14
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.autofill.IAutoFillManager.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    int addClient(IAutoFillManagerClient iAutoFillManagerClient, int i) throws RemoteException;

    void cancelSession(int i, int i2) throws RemoteException;

    void disableOwnedAutofillServices(int i) throws RemoteException;

    void finishSession(int i, int i2) throws RemoteException;

    ComponentName getAutofillServiceComponentName() throws RemoteException;

    String[] getAvailableFieldClassificationAlgorithms() throws RemoteException;

    String getDefaultFieldClassificationAlgorithm() throws RemoteException;

    FillEventHistory getFillEventHistory() throws RemoteException;

    UserData getUserData() throws RemoteException;

    String getUserDataId() throws RemoteException;

    boolean isFieldClassificationEnabled() throws RemoteException;

    boolean isServiceEnabled(int i, String str) throws RemoteException;

    boolean isServiceSupported(int i) throws RemoteException;

    void onPendingSaveUi(int i, IBinder iBinder) throws RemoteException;

    void removeClient(IAutoFillManagerClient iAutoFillManagerClient, int i) throws RemoteException;

    boolean restoreSession(int i, IBinder iBinder, IBinder iBinder2) throws RemoteException;

    void setAuthenticationResult(Bundle bundle, int i, int i2, int i3) throws RemoteException;

    void setAutofillFailure(int i, List<AutofillId> list, int i2) throws RemoteException;

    void setHasCallback(int i, int i2, boolean z) throws RemoteException;

    void setUserData(UserData userData) throws RemoteException;

    int startSession(IBinder iBinder, IBinder iBinder2, AutofillId autofillId, Rect rect, AutofillValue autofillValue, int i, boolean z, int i2, ComponentName componentName, boolean z2) throws RemoteException;

    int updateOrRestartSession(IBinder iBinder, IBinder iBinder2, AutofillId autofillId, Rect rect, AutofillValue autofillValue, int i, boolean z, int i2, ComponentName componentName, int i3, int i4, boolean z2) throws RemoteException;

    void updateSession(int i, AutofillId autofillId, Rect rect, AutofillValue autofillValue, int i2, int i3, int i4) throws RemoteException;
}

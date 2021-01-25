package com.android.internal.inputmethod;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.view.inputmethod.EditorInfo;
import com.android.internal.view.IInputContext;

public interface IMultiClientInputMethodSession extends IInterface {
    void hideSoftInput(int i, ResultReceiver resultReceiver) throws RemoteException;

    void showSoftInput(int i, ResultReceiver resultReceiver) throws RemoteException;

    void startInputOrWindowGainedFocus(IInputContext iInputContext, int i, EditorInfo editorInfo, int i2, int i3, int i4) throws RemoteException;

    public static class Default implements IMultiClientInputMethodSession {
        @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
        public void startInputOrWindowGainedFocus(IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int softInputMode, int targetWindowHandle) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
        public void showSoftInput(int flags, ResultReceiver resultReceiver) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
        public void hideSoftInput(int flags, ResultReceiver resultReceiver) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMultiClientInputMethodSession {
        private static final String DESCRIPTOR = "com.android.internal.inputmethod.IMultiClientInputMethodSession";
        static final int TRANSACTION_hideSoftInput = 3;
        static final int TRANSACTION_showSoftInput = 2;
        static final int TRANSACTION_startInputOrWindowGainedFocus = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiClientInputMethodSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiClientInputMethodSession)) {
                return new Proxy(obj);
            }
            return (IMultiClientInputMethodSession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "startInputOrWindowGainedFocus";
            }
            if (transactionCode == 2) {
                return "showSoftInput";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "hideSoftInput";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            EditorInfo _arg2;
            ResultReceiver _arg1;
            ResultReceiver _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IInputContext _arg0 = IInputContext.Stub.asInterface(data.readStrongBinder());
                int _arg13 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = EditorInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                startInputOrWindowGainedFocus(_arg0, _arg13, _arg2, data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = ResultReceiver.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                showSoftInput(_arg02, _arg1);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg03 = data.readInt();
                if (data.readInt() != 0) {
                    _arg12 = ResultReceiver.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                hideSoftInput(_arg03, _arg12);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMultiClientInputMethodSession {
            public static IMultiClientInputMethodSession sDefaultImpl;
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

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
            public void startInputOrWindowGainedFocus(IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int softInputMode, int targetWindowHandle) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(inputContext != null ? inputContext.asBinder() : null);
                    try {
                        _data.writeInt(missingMethods);
                        if (attribute != null) {
                            _data.writeInt(1);
                            attribute.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(controlFlags);
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
                    try {
                        _data.writeInt(softInputMode);
                        try {
                            _data.writeInt(targetWindowHandle);
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().startInputOrWindowGainedFocus(inputContext, missingMethods, attribute, controlFlags, softInputMode, targetWindowHandle);
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
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
            public void showSoftInput(int flags, ResultReceiver resultReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (resultReceiver != null) {
                        _data.writeInt(1);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showSoftInput(flags, resultReceiver);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
            public void hideSoftInput(int flags, ResultReceiver resultReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (resultReceiver != null) {
                        _data.writeInt(1);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hideSoftInput(flags, resultReceiver);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMultiClientInputMethodSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMultiClientInputMethodSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

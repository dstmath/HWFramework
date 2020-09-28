package com.android.internal.textservice;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesSessionListener;

public interface ITextServicesManager extends IInterface {
    void finishSpellCheckerService(int i, ISpellCheckerSessionListener iSpellCheckerSessionListener) throws RemoteException;

    SpellCheckerInfo getCurrentSpellChecker(int i, String str) throws RemoteException;

    SpellCheckerSubtype getCurrentSpellCheckerSubtype(int i, boolean z) throws RemoteException;

    SpellCheckerInfo[] getEnabledSpellCheckers(int i) throws RemoteException;

    void getSpellCheckerService(int i, String str, String str2, ITextServicesSessionListener iTextServicesSessionListener, ISpellCheckerSessionListener iSpellCheckerSessionListener, Bundle bundle) throws RemoteException;

    boolean isSpellCheckerEnabled(int i) throws RemoteException;

    public static class Default implements ITextServicesManager {
        @Override // com.android.internal.textservice.ITextServicesManager
        public SpellCheckerInfo getCurrentSpellChecker(int userId, String locale) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.textservice.ITextServicesManager
        public SpellCheckerSubtype getCurrentSpellCheckerSubtype(int userId, boolean allowImplicitlySelectedSubtype) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.textservice.ITextServicesManager
        public void getSpellCheckerService(int userId, String sciId, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, Bundle bundle) throws RemoteException {
        }

        @Override // com.android.internal.textservice.ITextServicesManager
        public void finishSpellCheckerService(int userId, ISpellCheckerSessionListener listener) throws RemoteException {
        }

        @Override // com.android.internal.textservice.ITextServicesManager
        public boolean isSpellCheckerEnabled(int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.textservice.ITextServicesManager
        public SpellCheckerInfo[] getEnabledSpellCheckers(int userId) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITextServicesManager {
        private static final String DESCRIPTOR = "com.android.internal.textservice.ITextServicesManager";
        static final int TRANSACTION_finishSpellCheckerService = 4;
        static final int TRANSACTION_getCurrentSpellChecker = 1;
        static final int TRANSACTION_getCurrentSpellCheckerSubtype = 2;
        static final int TRANSACTION_getEnabledSpellCheckers = 6;
        static final int TRANSACTION_getSpellCheckerService = 3;
        static final int TRANSACTION_isSpellCheckerEnabled = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITextServicesManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITextServicesManager)) {
                return new Proxy(obj);
            }
            return (ITextServicesManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getCurrentSpellChecker";
                case 2:
                    return "getCurrentSpellCheckerSubtype";
                case 3:
                    return "getSpellCheckerService";
                case 4:
                    return "finishSpellCheckerService";
                case 5:
                    return "isSpellCheckerEnabled";
                case 6:
                    return "getEnabledSpellCheckers";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg5;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        SpellCheckerInfo _result = getCurrentSpellChecker(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        SpellCheckerSubtype _result2 = getCurrentSpellCheckerSubtype(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg1 = data.readString();
                        String _arg2 = data.readString();
                        ITextServicesSessionListener _arg3 = ITextServicesSessionListener.Stub.asInterface(data.readStrongBinder());
                        ISpellCheckerSessionListener _arg4 = ISpellCheckerSessionListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg5 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        getSpellCheckerService(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        finishSpellCheckerService(data.readInt(), ISpellCheckerSessionListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSpellCheckerEnabled = isSpellCheckerEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSpellCheckerEnabled ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        SpellCheckerInfo[] _result3 = getEnabledSpellCheckers(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result3, 1);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITextServicesManager {
            public static ITextServicesManager sDefaultImpl;
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

            @Override // com.android.internal.textservice.ITextServicesManager
            public SpellCheckerInfo getCurrentSpellChecker(int userId, String locale) throws RemoteException {
                SpellCheckerInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(locale);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentSpellChecker(userId, locale);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SpellCheckerInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.textservice.ITextServicesManager
            public SpellCheckerSubtype getCurrentSpellCheckerSubtype(int userId, boolean allowImplicitlySelectedSubtype) throws RemoteException {
                SpellCheckerSubtype _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(allowImplicitlySelectedSubtype ? 1 : 0);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentSpellCheckerSubtype(userId, allowImplicitlySelectedSubtype);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SpellCheckerSubtype.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.textservice.ITextServicesManager
            public void getSpellCheckerService(int userId, String sciId, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(userId);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(sciId);
                        try {
                            _data.writeString(locale);
                            _data.writeStrongBinder(tsListener != null ? tsListener.asBinder() : null);
                            _data.writeStrongBinder(scListener != null ? scListener.asBinder() : null);
                            if (bundle != null) {
                                _data.writeInt(1);
                                bundle.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().getSpellCheckerService(userId, sciId, locale, tsListener, scListener, bundle);
                                _data.recycle();
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
                } catch (Throwable th5) {
                    th = th5;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.textservice.ITextServicesManager
            public void finishSpellCheckerService(int userId, ISpellCheckerSessionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().finishSpellCheckerService(userId, listener);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.textservice.ITextServicesManager
            public boolean isSpellCheckerEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSpellCheckerEnabled(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.textservice.ITextServicesManager
            public SpellCheckerInfo[] getEnabledSpellCheckers(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnabledSpellCheckers(userId);
                    }
                    _reply.readException();
                    SpellCheckerInfo[] _result = (SpellCheckerInfo[]) _reply.createTypedArray(SpellCheckerInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITextServicesManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITextServicesManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

package android.app.contentsuggestions;

import android.app.contentsuggestions.IClassificationsCallback;
import android.app.contentsuggestions.ISelectionsCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.os.IResultReceiver;

public interface IContentSuggestionsManager extends IInterface {
    void classifyContentSelections(int i, ClassificationsRequest classificationsRequest, IClassificationsCallback iClassificationsCallback) throws RemoteException;

    void isEnabled(int i, IResultReceiver iResultReceiver) throws RemoteException;

    void notifyInteraction(int i, String str, Bundle bundle) throws RemoteException;

    void provideContextImage(int i, int i2, Bundle bundle) throws RemoteException;

    void suggestContentSelections(int i, SelectionsRequest selectionsRequest, ISelectionsCallback iSelectionsCallback) throws RemoteException;

    public static class Default implements IContentSuggestionsManager {
        @Override // android.app.contentsuggestions.IContentSuggestionsManager
        public void provideContextImage(int userId, int taskId, Bundle imageContextRequestExtras) throws RemoteException {
        }

        @Override // android.app.contentsuggestions.IContentSuggestionsManager
        public void suggestContentSelections(int userId, SelectionsRequest request, ISelectionsCallback callback) throws RemoteException {
        }

        @Override // android.app.contentsuggestions.IContentSuggestionsManager
        public void classifyContentSelections(int userId, ClassificationsRequest request, IClassificationsCallback callback) throws RemoteException {
        }

        @Override // android.app.contentsuggestions.IContentSuggestionsManager
        public void notifyInteraction(int userId, String requestId, Bundle interaction) throws RemoteException {
        }

        @Override // android.app.contentsuggestions.IContentSuggestionsManager
        public void isEnabled(int userId, IResultReceiver receiver) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IContentSuggestionsManager {
        private static final String DESCRIPTOR = "android.app.contentsuggestions.IContentSuggestionsManager";
        static final int TRANSACTION_classifyContentSelections = 3;
        static final int TRANSACTION_isEnabled = 5;
        static final int TRANSACTION_notifyInteraction = 4;
        static final int TRANSACTION_provideContextImage = 1;
        static final int TRANSACTION_suggestContentSelections = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IContentSuggestionsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContentSuggestionsManager)) {
                return new Proxy(obj);
            }
            return (IContentSuggestionsManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "provideContextImage";
            }
            if (transactionCode == 2) {
                return "suggestContentSelections";
            }
            if (transactionCode == 3) {
                return "classifyContentSelections";
            }
            if (transactionCode == 4) {
                return "notifyInteraction";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "isEnabled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            SelectionsRequest _arg1;
            ClassificationsRequest _arg12;
            Bundle _arg22;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg13 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                provideContextImage(_arg0, _arg13, _arg2);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = SelectionsRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                suggestContentSelections(_arg02, _arg1, ISelectionsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg03 = data.readInt();
                if (data.readInt() != 0) {
                    _arg12 = ClassificationsRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                classifyContentSelections(_arg03, _arg12, IClassificationsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _arg04 = data.readInt();
                String _arg14 = data.readString();
                if (data.readInt() != 0) {
                    _arg22 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg22 = null;
                }
                notifyInteraction(_arg04, _arg14, _arg22);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                isEnabled(data.readInt(), IResultReceiver.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IContentSuggestionsManager {
            public static IContentSuggestionsManager sDefaultImpl;
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

            @Override // android.app.contentsuggestions.IContentSuggestionsManager
            public void provideContextImage(int userId, int taskId, Bundle imageContextRequestExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(taskId);
                    if (imageContextRequestExtras != null) {
                        _data.writeInt(1);
                        imageContextRequestExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().provideContextImage(userId, taskId, imageContextRequestExtras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.contentsuggestions.IContentSuggestionsManager
            public void suggestContentSelections(int userId, SelectionsRequest request, ISelectionsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().suggestContentSelections(userId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.contentsuggestions.IContentSuggestionsManager
            public void classifyContentSelections(int userId, ClassificationsRequest request, IClassificationsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().classifyContentSelections(userId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.contentsuggestions.IContentSuggestionsManager
            public void notifyInteraction(int userId, String requestId, Bundle interaction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(requestId);
                    if (interaction != null) {
                        _data.writeInt(1);
                        interaction.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyInteraction(userId, requestId, interaction);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.contentsuggestions.IContentSuggestionsManager
            public void isEnabled(int userId, IResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().isEnabled(userId, receiver);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IContentSuggestionsManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IContentSuggestionsManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

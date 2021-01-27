package android.service.contentsuggestions;

import android.app.contentsuggestions.ClassificationsRequest;
import android.app.contentsuggestions.IClassificationsCallback;
import android.app.contentsuggestions.ISelectionsCallback;
import android.app.contentsuggestions.SelectionsRequest;
import android.graphics.GraphicBuffer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IContentSuggestionsService extends IInterface {
    void classifyContentSelections(ClassificationsRequest classificationsRequest, IClassificationsCallback iClassificationsCallback) throws RemoteException;

    void notifyInteraction(String str, Bundle bundle) throws RemoteException;

    void provideContextImage(int i, GraphicBuffer graphicBuffer, int i2, Bundle bundle) throws RemoteException;

    void suggestContentSelections(SelectionsRequest selectionsRequest, ISelectionsCallback iSelectionsCallback) throws RemoteException;

    public static class Default implements IContentSuggestionsService {
        @Override // android.service.contentsuggestions.IContentSuggestionsService
        public void provideContextImage(int taskId, GraphicBuffer contextImage, int colorSpaceId, Bundle imageContextRequestExtras) throws RemoteException {
        }

        @Override // android.service.contentsuggestions.IContentSuggestionsService
        public void suggestContentSelections(SelectionsRequest request, ISelectionsCallback callback) throws RemoteException {
        }

        @Override // android.service.contentsuggestions.IContentSuggestionsService
        public void classifyContentSelections(ClassificationsRequest request, IClassificationsCallback callback) throws RemoteException {
        }

        @Override // android.service.contentsuggestions.IContentSuggestionsService
        public void notifyInteraction(String requestId, Bundle interaction) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IContentSuggestionsService {
        private static final String DESCRIPTOR = "android.service.contentsuggestions.IContentSuggestionsService";
        static final int TRANSACTION_classifyContentSelections = 3;
        static final int TRANSACTION_notifyInteraction = 4;
        static final int TRANSACTION_provideContextImage = 1;
        static final int TRANSACTION_suggestContentSelections = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IContentSuggestionsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContentSuggestionsService)) {
                return new Proxy(obj);
            }
            return (IContentSuggestionsService) iin;
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
            if (transactionCode != 4) {
                return null;
            }
            return "notifyInteraction";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            GraphicBuffer _arg1;
            Bundle _arg3;
            SelectionsRequest _arg0;
            ClassificationsRequest _arg02;
            Bundle _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg03 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = GraphicBuffer.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                provideContextImage(_arg03, _arg1, _arg2, _arg3);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = SelectionsRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                suggestContentSelections(_arg0, ISelectionsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ClassificationsRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                classifyContentSelections(_arg02, IClassificationsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _arg04 = data.readString();
                if (data.readInt() != 0) {
                    _arg12 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                notifyInteraction(_arg04, _arg12);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IContentSuggestionsService {
            public static IContentSuggestionsService sDefaultImpl;
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

            @Override // android.service.contentsuggestions.IContentSuggestionsService
            public void provideContextImage(int taskId, GraphicBuffer contextImage, int colorSpaceId, Bundle imageContextRequestExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (contextImage != null) {
                        _data.writeInt(1);
                        contextImage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(colorSpaceId);
                    if (imageContextRequestExtras != null) {
                        _data.writeInt(1);
                        imageContextRequestExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().provideContextImage(taskId, contextImage, colorSpaceId, imageContextRequestExtras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.contentsuggestions.IContentSuggestionsService
            public void suggestContentSelections(SelectionsRequest request, ISelectionsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                        Stub.getDefaultImpl().suggestContentSelections(request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.contentsuggestions.IContentSuggestionsService
            public void classifyContentSelections(ClassificationsRequest request, IClassificationsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                        Stub.getDefaultImpl().classifyContentSelections(request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.contentsuggestions.IContentSuggestionsService
            public void notifyInteraction(String requestId, Bundle interaction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                        Stub.getDefaultImpl().notifyInteraction(requestId, interaction);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IContentSuggestionsService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IContentSuggestionsService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

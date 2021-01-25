package android.service.textclassifier;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.textclassifier.ITextClassifierCallback;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.SelectionEvent;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationContext;
import android.view.textclassifier.TextClassificationSessionId;
import android.view.textclassifier.TextClassifierEvent;
import android.view.textclassifier.TextLanguage;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;

public interface ITextClassifierService extends IInterface {
    void onClassifyText(TextClassificationSessionId textClassificationSessionId, TextClassification.Request request, ITextClassifierCallback iTextClassifierCallback) throws RemoteException;

    void onCreateTextClassificationSession(TextClassificationContext textClassificationContext, TextClassificationSessionId textClassificationSessionId) throws RemoteException;

    void onDestroyTextClassificationSession(TextClassificationSessionId textClassificationSessionId) throws RemoteException;

    void onDetectLanguage(TextClassificationSessionId textClassificationSessionId, TextLanguage.Request request, ITextClassifierCallback iTextClassifierCallback) throws RemoteException;

    void onGenerateLinks(TextClassificationSessionId textClassificationSessionId, TextLinks.Request request, ITextClassifierCallback iTextClassifierCallback) throws RemoteException;

    void onSelectionEvent(TextClassificationSessionId textClassificationSessionId, SelectionEvent selectionEvent) throws RemoteException;

    void onSuggestConversationActions(TextClassificationSessionId textClassificationSessionId, ConversationActions.Request request, ITextClassifierCallback iTextClassifierCallback) throws RemoteException;

    void onSuggestSelection(TextClassificationSessionId textClassificationSessionId, TextSelection.Request request, ITextClassifierCallback iTextClassifierCallback) throws RemoteException;

    void onTextClassifierEvent(TextClassificationSessionId textClassificationSessionId, TextClassifierEvent textClassifierEvent) throws RemoteException;

    public static class Default implements ITextClassifierService {
        @Override // android.service.textclassifier.ITextClassifierService
        public void onSuggestSelection(TextClassificationSessionId sessionId, TextSelection.Request request, ITextClassifierCallback callback) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onClassifyText(TextClassificationSessionId sessionId, TextClassification.Request request, ITextClassifierCallback callback) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onGenerateLinks(TextClassificationSessionId sessionId, TextLinks.Request request, ITextClassifierCallback callback) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onSelectionEvent(TextClassificationSessionId sessionId, SelectionEvent event) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onTextClassifierEvent(TextClassificationSessionId sessionId, TextClassifierEvent event) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onCreateTextClassificationSession(TextClassificationContext context, TextClassificationSessionId sessionId) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onDestroyTextClassificationSession(TextClassificationSessionId sessionId) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onDetectLanguage(TextClassificationSessionId sessionId, TextLanguage.Request request, ITextClassifierCallback callback) throws RemoteException {
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onSuggestConversationActions(TextClassificationSessionId sessionId, ConversationActions.Request request, ITextClassifierCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITextClassifierService {
        private static final String DESCRIPTOR = "android.service.textclassifier.ITextClassifierService";
        static final int TRANSACTION_onClassifyText = 2;
        static final int TRANSACTION_onCreateTextClassificationSession = 6;
        static final int TRANSACTION_onDestroyTextClassificationSession = 7;
        static final int TRANSACTION_onDetectLanguage = 8;
        static final int TRANSACTION_onGenerateLinks = 3;
        static final int TRANSACTION_onSelectionEvent = 4;
        static final int TRANSACTION_onSuggestConversationActions = 9;
        static final int TRANSACTION_onSuggestSelection = 1;
        static final int TRANSACTION_onTextClassifierEvent = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITextClassifierService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITextClassifierService)) {
                return new Proxy(obj);
            }
            return (ITextClassifierService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onSuggestSelection";
                case 2:
                    return "onClassifyText";
                case 3:
                    return "onGenerateLinks";
                case 4:
                    return "onSelectionEvent";
                case 5:
                    return "onTextClassifierEvent";
                case 6:
                    return "onCreateTextClassificationSession";
                case 7:
                    return "onDestroyTextClassificationSession";
                case 8:
                    return "onDetectLanguage";
                case 9:
                    return "onSuggestConversationActions";
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
            TextClassificationSessionId _arg0;
            TextSelection.Request _arg1;
            TextClassificationSessionId _arg02;
            TextClassification.Request _arg12;
            TextClassificationSessionId _arg03;
            TextLinks.Request _arg13;
            TextClassificationSessionId _arg04;
            SelectionEvent _arg14;
            TextClassificationSessionId _arg05;
            TextClassifierEvent _arg15;
            TextClassificationContext _arg06;
            TextClassificationSessionId _arg16;
            TextClassificationSessionId _arg07;
            TextClassificationSessionId _arg08;
            TextLanguage.Request _arg17;
            TextClassificationSessionId _arg09;
            ConversationActions.Request _arg18;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = TextSelection.Request.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onSuggestSelection(_arg0, _arg1, ITextClassifierCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = TextClassification.Request.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        onClassifyText(_arg02, _arg12, ITextClassifierCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = TextLinks.Request.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        onGenerateLinks(_arg03, _arg13, ITextClassifierCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = SelectionEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        onSelectionEvent(_arg04, _arg14);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg15 = TextClassifierEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        onTextClassifierEvent(_arg05, _arg15);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = TextClassificationContext.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg16 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        onCreateTextClassificationSession(_arg06, _arg16);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        onDestroyTextClassificationSession(_arg07);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg17 = TextLanguage.Request.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        onDetectLanguage(_arg08, _arg17, ITextClassifierCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = TextClassificationSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg18 = ConversationActions.Request.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        onSuggestConversationActions(_arg09, _arg18, ITextClassifierCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements ITextClassifierService {
            public static ITextClassifierService sDefaultImpl;
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

            @Override // android.service.textclassifier.ITextClassifierService
            public void onSuggestSelection(TextClassificationSessionId sessionId, TextSelection.Request request, ITextClassifierCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSuggestSelection(sessionId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onClassifyText(TextClassificationSessionId sessionId, TextClassification.Request request, ITextClassifierCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
                        Stub.getDefaultImpl().onClassifyText(sessionId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onGenerateLinks(TextClassificationSessionId sessionId, TextLinks.Request request, ITextClassifierCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
                        Stub.getDefaultImpl().onGenerateLinks(sessionId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onSelectionEvent(TextClassificationSessionId sessionId, SelectionEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSelectionEvent(sessionId, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onTextClassifierEvent(TextClassificationSessionId sessionId, TextClassifierEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTextClassifierEvent(sessionId, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onCreateTextClassificationSession(TextClassificationContext context, TextClassificationSessionId sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (context != null) {
                        _data.writeInt(1);
                        context.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCreateTextClassificationSession(context, sessionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onDestroyTextClassificationSession(TextClassificationSessionId sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDestroyTextClassificationSession(sessionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onDetectLanguage(TextClassificationSessionId sessionId, TextLanguage.Request request, ITextClassifierCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDetectLanguage(sessionId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.textclassifier.ITextClassifierService
            public void onSuggestConversationActions(TextClassificationSessionId sessionId, ConversationActions.Request request, ITextClassifierCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSuggestConversationActions(sessionId, request, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITextClassifierService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITextClassifierService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

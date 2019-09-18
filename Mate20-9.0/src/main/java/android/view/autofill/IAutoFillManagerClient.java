package android.view.autofill;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.autofill.IAutofillWindowPresenter;
import java.util.List;

public interface IAutoFillManagerClient extends IInterface {

    public static abstract class Stub extends Binder implements IAutoFillManagerClient {
        private static final String DESCRIPTOR = "android.view.autofill.IAutoFillManagerClient";
        static final int TRANSACTION_authenticate = 3;
        static final int TRANSACTION_autofill = 2;
        static final int TRANSACTION_dispatchUnhandledKey = 8;
        static final int TRANSACTION_notifyNoFillUi = 7;
        static final int TRANSACTION_requestHideFillUi = 6;
        static final int TRANSACTION_requestShowFillUi = 5;
        static final int TRANSACTION_setSaveUiState = 10;
        static final int TRANSACTION_setSessionFinished = 11;
        static final int TRANSACTION_setState = 1;
        static final int TRANSACTION_setTrackedViews = 4;
        static final int TRANSACTION_startIntentSender = 9;

        private static class Proxy implements IAutoFillManagerClient {
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

            public void setState(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void autofill(int sessionId, List<AutofillId> ids, List<AutofillValue> values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeTypedList(ids);
                    _data.writeTypedList(values);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void authenticate(int sessionId, int authenticationId, IntentSender intent, Intent fillInIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(authenticationId);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (fillInIntent != null) {
                        _data.writeInt(1);
                        fillInIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setTrackedViews(int sessionId, AutofillId[] savableIds, boolean saveOnAllViewsInvisible, boolean saveOnFinish, AutofillId[] fillableIds, AutofillId saveTriggerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeTypedArray(savableIds, 0);
                    _data.writeInt(saveOnAllViewsInvisible);
                    _data.writeInt(saveOnFinish);
                    _data.writeTypedArray(fillableIds, 0);
                    if (saveTriggerId != null) {
                        _data.writeInt(1);
                        saveTriggerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestShowFillUi(int sessionId, AutofillId id, int width, int height, Rect anchorBounds, IAutofillWindowPresenter presenter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (id != null) {
                        _data.writeInt(1);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (anchorBounds != null) {
                        _data.writeInt(1);
                        anchorBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(presenter != null ? presenter.asBinder() : null);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestHideFillUi(int sessionId, AutofillId id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (id != null) {
                        _data.writeInt(1);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyNoFillUi(int sessionId, AutofillId id, int sessionFinishedState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (id != null) {
                        _data.writeInt(1);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sessionFinishedState);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchUnhandledKey(int sessionId, AutofillId id, KeyEvent keyEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (id != null) {
                        _data.writeInt(1);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (keyEvent != null) {
                        _data.writeInt(1);
                        keyEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startIntentSender(IntentSender intentSender, Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intentSender != null) {
                        _data.writeInt(1);
                        intentSender.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setSaveUiState(int sessionId, boolean shown) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(shown);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setSessionFinished(int newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newState);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAutoFillManagerClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAutoFillManagerClient)) {
                return new Proxy(obj);
            }
            return (IAutoFillManagerClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: android.view.autofill.AutofillId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: android.view.autofill.AutofillId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: android.view.KeyEvent} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v3, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r1v21, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r1v26 */
        /* JADX WARNING: type inference failed for: r1v27 */
        /* JADX WARNING: type inference failed for: r1v28 */
        /* JADX WARNING: type inference failed for: r1v29 */
        /* JADX WARNING: type inference failed for: r1v30 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IntentSender _arg2;
            AutofillId _arg5;
            AutofillId _arg1;
            Rect _arg4;
            AutofillId _arg12;
            IntentSender _arg0;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                boolean _arg13 = false;
                ? _arg22 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        setState(data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        autofill(data.readInt(), parcel.createTypedArrayList(AutofillId.CREATOR), parcel.createTypedArrayList(AutofillValue.CREATOR));
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (IntentSender) IntentSender.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        authenticate(_arg02, _arg14, _arg2, _arg22);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        AutofillId[] _arg15 = (AutofillId[]) parcel.createTypedArray(AutofillId.CREATOR);
                        boolean _arg23 = data.readInt() != 0;
                        boolean _arg3 = data.readInt() != 0;
                        AutofillId[] _arg42 = (AutofillId[]) parcel.createTypedArray(AutofillId.CREATOR);
                        if (data.readInt() != 0) {
                            _arg5 = AutofillId.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg5 = null;
                        }
                        setTrackedViews(_arg03, _arg15, _arg23, _arg3, _arg42, _arg5);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = AutofillId.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        int _arg24 = data.readInt();
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        requestShowFillUi(_arg04, _arg1, _arg24, _arg32, _arg4, IAutofillWindowPresenter.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = AutofillId.CREATOR.createFromParcel(parcel);
                        }
                        requestHideFillUi(_arg05, _arg22);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = AutofillId.CREATOR.createFromParcel(parcel);
                        }
                        notifyNoFillUi(_arg06, _arg22, data.readInt());
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = AutofillId.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = KeyEvent.CREATOR.createFromParcel(parcel);
                        }
                        dispatchUnhandledKey(_arg07, _arg12, _arg22);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (IntentSender) IntentSender.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        startIntentSender(_arg0, _arg22);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setSaveUiState(_arg08, _arg13);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        setSessionFinished(data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void authenticate(int i, int i2, IntentSender intentSender, Intent intent) throws RemoteException;

    void autofill(int i, List<AutofillId> list, List<AutofillValue> list2) throws RemoteException;

    void dispatchUnhandledKey(int i, AutofillId autofillId, KeyEvent keyEvent) throws RemoteException;

    void notifyNoFillUi(int i, AutofillId autofillId, int i2) throws RemoteException;

    void requestHideFillUi(int i, AutofillId autofillId) throws RemoteException;

    void requestShowFillUi(int i, AutofillId autofillId, int i2, int i3, Rect rect, IAutofillWindowPresenter iAutofillWindowPresenter) throws RemoteException;

    void setSaveUiState(int i, boolean z) throws RemoteException;

    void setSessionFinished(int i) throws RemoteException;

    void setState(int i) throws RemoteException;

    void setTrackedViews(int i, AutofillId[] autofillIdArr, boolean z, boolean z2, AutofillId[] autofillIdArr2, AutofillId autofillId) throws RemoteException;

    void startIntentSender(IntentSender intentSender, Intent intent) throws RemoteException;
}

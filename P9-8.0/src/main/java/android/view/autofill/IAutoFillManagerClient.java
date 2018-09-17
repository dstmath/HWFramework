package android.view.autofill;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAutoFillManagerClient extends IInterface {

    public static abstract class Stub extends Binder implements IAutoFillManagerClient {
        private static final String DESCRIPTOR = "android.view.autofill.IAutoFillManagerClient";
        static final int TRANSACTION_authenticate = 3;
        static final int TRANSACTION_autofill = 2;
        static final int TRANSACTION_notifyNoFillUi = 7;
        static final int TRANSACTION_requestHideFillUi = 6;
        static final int TRANSACTION_requestShowFillUi = 5;
        static final int TRANSACTION_setState = 1;
        static final int TRANSACTION_setTrackedViews = 4;
        static final int TRANSACTION_startIntentSender = 8;

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

            public void setState(boolean enabled, boolean resetSession, boolean resetClient) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (resetSession) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!resetClient) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public void setTrackedViews(int sessionId, AutofillId[] savableIds, boolean saveOnAllViewsInvisible, AutofillId[] fillableIds) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeTypedArray(savableIds, 0);
                    if (!saveOnAllViewsInvisible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeTypedArray(fillableIds, 0);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestShowFillUi(int sessionId, AutofillId id, int width, int height, Rect anchorBounds, IAutofillWindowPresenter presenter) throws RemoteException {
                IBinder iBinder = null;
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
                    if (presenter != null) {
                        iBinder = presenter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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

            public void notifyNoFillUi(int sessionId, AutofillId id) throws RemoteException {
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
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startIntentSender(IntentSender intentSender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intentSender != null) {
                        _data.writeInt(1);
                        intentSender.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            AutofillId _arg1;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setState(data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    autofill(data.readInt(), data.createTypedArrayList(AutofillId.CREATOR), data.createTypedArrayList(AutofillValue.CREATOR));
                    return true;
                case 3:
                    IntentSender _arg2;
                    Intent _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    authenticate(_arg0, _arg12, _arg2, _arg3);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setTrackedViews(data.readInt(), (AutofillId[]) data.createTypedArray(AutofillId.CREATOR), data.readInt() != 0, (AutofillId[]) data.createTypedArray(AutofillId.CREATOR));
                    return true;
                case 5:
                    Rect _arg4;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (AutofillId) AutofillId.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    int _arg22 = data.readInt();
                    int _arg32 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg4 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    requestShowFillUi(_arg0, _arg1, _arg22, _arg32, _arg4, android.view.autofill.IAutofillWindowPresenter.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (AutofillId) AutofillId.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    requestHideFillUi(_arg0, _arg1);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (AutofillId) AutofillId.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    notifyNoFillUi(_arg0, _arg1);
                    return true;
                case 8:
                    IntentSender _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    startIntentSender(_arg02);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void authenticate(int i, int i2, IntentSender intentSender, Intent intent) throws RemoteException;

    void autofill(int i, List<AutofillId> list, List<AutofillValue> list2) throws RemoteException;

    void notifyNoFillUi(int i, AutofillId autofillId) throws RemoteException;

    void requestHideFillUi(int i, AutofillId autofillId) throws RemoteException;

    void requestShowFillUi(int i, AutofillId autofillId, int i2, int i3, Rect rect, IAutofillWindowPresenter iAutofillWindowPresenter) throws RemoteException;

    void setState(boolean z, boolean z2, boolean z3) throws RemoteException;

    void setTrackedViews(int i, AutofillId[] autofillIdArr, boolean z, AutofillId[] autofillIdArr2) throws RemoteException;

    void startIntentSender(IntentSender intentSender) throws RemoteException;
}

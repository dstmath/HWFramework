package com.android.internal.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import java.util.List;

public interface IInputMethodManager extends IInterface {
    void addClient(IInputMethodClient iInputMethodClient, IInputContext iInputContext, int i) throws RemoteException;

    InputMethodSubtype getCurrentInputMethodSubtype() throws RemoteException;

    List<InputMethodInfo> getEnabledInputMethodList(int i) throws RemoteException;

    List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String str, boolean z) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    List<InputMethodInfo> getInputMethodList(int i) throws RemoteException;

    int getInputMethodWindowVisibleHeight() throws RemoteException;

    InputMethodSubtype getLastInputMethodSubtype() throws RemoteException;

    boolean hideSoftInput(IInputMethodClient iInputMethodClient, int i, ResultReceiver resultReceiver) throws RemoteException;

    boolean isInputMethodPickerShownForTest() throws RemoteException;

    void reportActivityView(IInputMethodClient iInputMethodClient, int i, float[] fArr) throws RemoteException;

    void setAdditionalInputMethodSubtypes(String str, InputMethodSubtype[] inputMethodSubtypeArr) throws RemoteException;

    void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient iInputMethodClient, String str) throws RemoteException;

    void showInputMethodPickerFromClient(IInputMethodClient iInputMethodClient, int i) throws RemoteException;

    void showInputMethodPickerFromSystem(IInputMethodClient iInputMethodClient, int i, int i2) throws RemoteException;

    boolean showSoftInput(IInputMethodClient iInputMethodClient, int i, ResultReceiver resultReceiver) throws RemoteException;

    InputBindResult startInputOrWindowGainedFocus(int i, IInputMethodClient iInputMethodClient, IBinder iBinder, int i2, int i3, int i4, EditorInfo editorInfo, IInputContext iInputContext, int i5, int i6) throws RemoteException;

    public static class Default implements IInputMethodManager {
        @Override // com.android.internal.view.IInputMethodManager
        public void addClient(IInputMethodClient client, IInputContext inputContext, int untrustedDisplayId) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodManager
        public List<InputMethodInfo> getInputMethodList(int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public List<InputMethodInfo> getEnabledInputMethodList(int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public InputMethodSubtype getLastInputMethodSubtype() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethodFlags, int unverifiedTargetSdkVersion) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodManager
        public void showInputMethodPickerFromSystem(IInputMethodClient client, int auxiliarySubtypeMode, int displayId) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodManager
        public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String topId) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodManager
        public boolean isInputMethodPickerShownForTest() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public InputMethodSubtype getCurrentInputMethodSubtype() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public void setAdditionalInputMethodSubtypes(String id, InputMethodSubtype[] subtypes) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodManager
        public int getInputMethodWindowVisibleHeight() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.view.IInputMethodManager
        public void reportActivityView(IInputMethodClient parentClient, int childDisplayId, float[] matrixValues) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInputMethodManager {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodManager";
        static final int TRANSACTION_addClient = 1;
        static final int TRANSACTION_getCurrentInputMethodSubtype = 13;
        static final int TRANSACTION_getEnabledInputMethodList = 3;
        static final int TRANSACTION_getEnabledInputMethodSubtypeList = 4;
        static final int TRANSACTION_getHwInnerService = 16;
        static final int TRANSACTION_getInputMethodList = 2;
        static final int TRANSACTION_getInputMethodWindowVisibleHeight = 15;
        static final int TRANSACTION_getLastInputMethodSubtype = 5;
        static final int TRANSACTION_hideSoftInput = 7;
        static final int TRANSACTION_isInputMethodPickerShownForTest = 12;
        static final int TRANSACTION_reportActivityView = 17;
        static final int TRANSACTION_setAdditionalInputMethodSubtypes = 14;
        static final int TRANSACTION_showInputMethodAndSubtypeEnablerFromClient = 11;
        static final int TRANSACTION_showInputMethodPickerFromClient = 9;
        static final int TRANSACTION_showInputMethodPickerFromSystem = 10;
        static final int TRANSACTION_showSoftInput = 6;
        static final int TRANSACTION_startInputOrWindowGainedFocus = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMethodManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMethodManager)) {
                return new Proxy(obj);
            }
            return (IInputMethodManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addClient";
                case 2:
                    return "getInputMethodList";
                case 3:
                    return "getEnabledInputMethodList";
                case 4:
                    return "getEnabledInputMethodSubtypeList";
                case 5:
                    return "getLastInputMethodSubtype";
                case 6:
                    return "showSoftInput";
                case 7:
                    return "hideSoftInput";
                case 8:
                    return "startInputOrWindowGainedFocus";
                case 9:
                    return "showInputMethodPickerFromClient";
                case 10:
                    return "showInputMethodPickerFromSystem";
                case 11:
                    return "showInputMethodAndSubtypeEnablerFromClient";
                case 12:
                    return "isInputMethodPickerShownForTest";
                case 13:
                    return "getCurrentInputMethodSubtype";
                case 14:
                    return "setAdditionalInputMethodSubtypes";
                case 15:
                    return "getInputMethodWindowVisibleHeight";
                case 16:
                    return "getHwInnerService";
                case 17:
                    return "reportActivityView";
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
            ResultReceiver _arg2;
            ResultReceiver _arg22;
            EditorInfo _arg6;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addClient(IInputMethodClient.Stub.asInterface(data.readStrongBinder()), IInputContext.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<InputMethodInfo> _result = getInputMethodList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<InputMethodInfo> _result2 = getEnabledInputMethodList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 4:
                        boolean _arg1 = false;
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        List<InputMethodSubtype> _result3 = getEnabledInputMethodSubtypeList(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        InputMethodSubtype _result4 = getLastInputMethodSubtype();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IInputMethodClient _arg02 = IInputMethodClient.Stub.asInterface(data.readStrongBinder());
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean showSoftInput = showSoftInput(_arg02, _arg12, _arg2);
                        reply.writeNoException();
                        reply.writeInt(showSoftInput ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IInputMethodClient _arg03 = IInputMethodClient.Stub.asInterface(data.readStrongBinder());
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        boolean hideSoftInput = hideSoftInput(_arg03, _arg13, _arg22);
                        reply.writeNoException();
                        reply.writeInt(hideSoftInput ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        IInputMethodClient _arg14 = IInputMethodClient.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg23 = data.readStrongBinder();
                        int _arg3 = data.readInt();
                        int _arg4 = data.readInt();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = EditorInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        InputBindResult _result5 = startInputOrWindowGainedFocus(_arg04, _arg14, _arg23, _arg3, _arg4, _arg5, _arg6, IInputContext.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        showInputMethodPickerFromClient(IInputMethodClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        showInputMethodPickerFromSystem(IInputMethodClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInputMethodPickerShownForTest = isInputMethodPickerShownForTest();
                        reply.writeNoException();
                        reply.writeInt(isInputMethodPickerShownForTest ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        InputMethodSubtype _result6 = getCurrentInputMethodSubtype();
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        setAdditionalInputMethodSubtypes(data.readString(), (InputMethodSubtype[]) data.createTypedArray(InputMethodSubtype.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getInputMethodWindowVisibleHeight();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result8 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result8);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        reportActivityView(IInputMethodClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.createFloatArray());
                        reply.writeNoException();
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
        public static class Proxy implements IInputMethodManager {
            public static IInputMethodManager sDefaultImpl;
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

            @Override // com.android.internal.view.IInputMethodManager
            public void addClient(IInputMethodClient client, IInputContext inputContext, int untrustedDisplayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    if (inputContext != null) {
                        iBinder = inputContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(untrustedDisplayId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addClient(client, inputContext, untrustedDisplayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public List<InputMethodInfo> getInputMethodList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputMethodList(userId);
                    }
                    _reply.readException();
                    List<InputMethodInfo> _result = _reply.createTypedArrayList(InputMethodInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public List<InputMethodInfo> getEnabledInputMethodList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnabledInputMethodList(userId);
                    }
                    _reply.readException();
                    List<InputMethodInfo> _result = _reply.createTypedArrayList(InputMethodInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imiId);
                    _data.writeInt(allowsImplicitlySelectedSubtypes ? 1 : 0);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnabledInputMethodSubtypeList(imiId, allowsImplicitlySelectedSubtypes);
                    }
                    _reply.readException();
                    List<InputMethodSubtype> _result = _reply.createTypedArrayList(InputMethodSubtype.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public InputMethodSubtype getLastInputMethodSubtype() throws RemoteException {
                InputMethodSubtype _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastInputMethodSubtype();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = InputMethodSubtype.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.view.IInputMethodManager
            public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(flags);
                    boolean _result = true;
                    if (resultReceiver != null) {
                        _data.writeInt(1);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().showSoftInput(client, flags, resultReceiver);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(flags);
                    boolean _result = true;
                    if (resultReceiver != null) {
                        _data.writeInt(1);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hideSoftInput(client, flags, resultReceiver);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethodFlags, int unverifiedTargetSdkVersion) throws RemoteException {
                Throwable th;
                InputBindResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(startInputReason);
                        IBinder iBinder = null;
                        _data.writeStrongBinder(client != null ? client.asBinder() : null);
                        _data.writeStrongBinder(windowToken);
                        _data.writeInt(startInputFlags);
                        _data.writeInt(softInputMode);
                        _data.writeInt(windowFlags);
                        if (attribute != null) {
                            _data.writeInt(1);
                            attribute.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (inputContext != null) {
                            iBinder = inputContext.asBinder();
                        }
                        _data.writeStrongBinder(iBinder);
                        _data.writeInt(missingMethodFlags);
                        _data.writeInt(unverifiedTargetSdkVersion);
                        if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = InputBindResult.CREATOR.createFromParcel(_reply);
                            } else {
                                _result = null;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        InputBindResult startInputOrWindowGainedFocus = Stub.getDefaultImpl().startInputOrWindowGainedFocus(startInputReason, client, windowToken, startInputFlags, softInputMode, windowFlags, attribute, inputContext, missingMethodFlags, unverifiedTargetSdkVersion);
                        _reply.recycle();
                        _data.recycle();
                        return startInputOrWindowGainedFocus;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(auxiliarySubtypeMode);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showInputMethodPickerFromClient(client, auxiliarySubtypeMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public void showInputMethodPickerFromSystem(IInputMethodClient client, int auxiliarySubtypeMode, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(auxiliarySubtypeMode);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showInputMethodPickerFromSystem(client, auxiliarySubtypeMode, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String topId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeString(topId);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showInputMethodAndSubtypeEnablerFromClient(client, topId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public boolean isInputMethodPickerShownForTest() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInputMethodPickerShownForTest();
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

            @Override // com.android.internal.view.IInputMethodManager
            public InputMethodSubtype getCurrentInputMethodSubtype() throws RemoteException {
                InputMethodSubtype _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentInputMethodSubtype();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = InputMethodSubtype.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.view.IInputMethodManager
            public void setAdditionalInputMethodSubtypes(String id, InputMethodSubtype[] subtypes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    _data.writeTypedArray(subtypes, 0);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAdditionalInputMethodSubtypes(id, subtypes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public int getInputMethodWindowVisibleHeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputMethodWindowVisibleHeight();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodManager
            public void reportActivityView(IInputMethodClient parentClient, int childDisplayId, float[] matrixValues) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(parentClient != null ? parentClient.asBinder() : null);
                    _data.writeInt(childDisplayId);
                    _data.writeFloatArray(matrixValues);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportActivityView(parentClient, childDisplayId, matrixValues);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInputMethodManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInputMethodManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

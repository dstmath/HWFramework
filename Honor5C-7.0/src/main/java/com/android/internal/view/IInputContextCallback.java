package com.android.internal.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.inputmethod.ExtractedText;

public interface IInputContextCallback extends IInterface {

    public static abstract class Stub extends Binder implements IInputContextCallback {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputContextCallback";
        static final int TRANSACTION_setCursorCapsMode = 3;
        static final int TRANSACTION_setExtractedText = 4;
        static final int TRANSACTION_setRequestUpdateCursorAnchorInfoResult = 6;
        static final int TRANSACTION_setSelectedText = 5;
        static final int TRANSACTION_setTextAfterCursor = 2;
        static final int TRANSACTION_setTextBeforeCursor = 1;

        private static class Proxy implements IInputContextCallback {
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

            public void setTextBeforeCursor(CharSequence textBeforeCursor, int seq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (textBeforeCursor != null) {
                        _data.writeInt(Stub.TRANSACTION_setTextBeforeCursor);
                        TextUtils.writeToParcel(textBeforeCursor, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(seq);
                    this.mRemote.transact(Stub.TRANSACTION_setTextBeforeCursor, _data, null, Stub.TRANSACTION_setTextBeforeCursor);
                } finally {
                    _data.recycle();
                }
            }

            public void setTextAfterCursor(CharSequence textAfterCursor, int seq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (textAfterCursor != null) {
                        _data.writeInt(Stub.TRANSACTION_setTextBeforeCursor);
                        TextUtils.writeToParcel(textAfterCursor, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(seq);
                    this.mRemote.transact(Stub.TRANSACTION_setTextAfterCursor, _data, null, Stub.TRANSACTION_setTextBeforeCursor);
                } finally {
                    _data.recycle();
                }
            }

            public void setCursorCapsMode(int capsMode, int seq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(capsMode);
                    _data.writeInt(seq);
                    this.mRemote.transact(Stub.TRANSACTION_setCursorCapsMode, _data, null, Stub.TRANSACTION_setTextBeforeCursor);
                } finally {
                    _data.recycle();
                }
            }

            public void setExtractedText(ExtractedText extractedText, int seq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (extractedText != null) {
                        _data.writeInt(Stub.TRANSACTION_setTextBeforeCursor);
                        extractedText.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(seq);
                    this.mRemote.transact(Stub.TRANSACTION_setExtractedText, _data, null, Stub.TRANSACTION_setTextBeforeCursor);
                } finally {
                    _data.recycle();
                }
            }

            public void setSelectedText(CharSequence selectedText, int seq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (selectedText != null) {
                        _data.writeInt(Stub.TRANSACTION_setTextBeforeCursor);
                        TextUtils.writeToParcel(selectedText, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(seq);
                    this.mRemote.transact(Stub.TRANSACTION_setSelectedText, _data, null, Stub.TRANSACTION_setTextBeforeCursor);
                } finally {
                    _data.recycle();
                }
            }

            public void setRequestUpdateCursorAnchorInfoResult(boolean result, int seq) throws RemoteException {
                int i = Stub.TRANSACTION_setTextBeforeCursor;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(seq);
                    this.mRemote.transact(Stub.TRANSACTION_setRequestUpdateCursorAnchorInfoResult, _data, null, Stub.TRANSACTION_setTextBeforeCursor);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputContextCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputContextCallback)) {
                return new Proxy(obj);
            }
            return (IInputContextCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CharSequence charSequence;
            switch (code) {
                case TRANSACTION_setTextBeforeCursor /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    setTextBeforeCursor(charSequence, data.readInt());
                    return true;
                case TRANSACTION_setTextAfterCursor /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    setTextAfterCursor(charSequence, data.readInt());
                    return true;
                case TRANSACTION_setCursorCapsMode /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCursorCapsMode(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_setExtractedText /*4*/:
                    ExtractedText extractedText;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        extractedText = (ExtractedText) ExtractedText.CREATOR.createFromParcel(data);
                    } else {
                        extractedText = null;
                    }
                    setExtractedText(extractedText, data.readInt());
                    return true;
                case TRANSACTION_setSelectedText /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    setSelectedText(charSequence, data.readInt());
                    return true;
                case TRANSACTION_setRequestUpdateCursorAnchorInfoResult /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRequestUpdateCursorAnchorInfoResult(data.readInt() != 0, data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void setCursorCapsMode(int i, int i2) throws RemoteException;

    void setExtractedText(ExtractedText extractedText, int i) throws RemoteException;

    void setRequestUpdateCursorAnchorInfoResult(boolean z, int i) throws RemoteException;

    void setSelectedText(CharSequence charSequence, int i) throws RemoteException;

    void setTextAfterCursor(CharSequence charSequence, int i) throws RemoteException;

    void setTextBeforeCursor(CharSequence charSequence, int i) throws RemoteException;
}

package com.android.internal.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;

public interface IInputMethodSession extends IInterface {

    public static abstract class Stub extends Binder implements IInputMethodSession {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodSession";
        static final int TRANSACTION_appPrivateCommand = 7;
        static final int TRANSACTION_displayCompletions = 6;
        static final int TRANSACTION_finishInput = 1;
        static final int TRANSACTION_finishSession = 9;
        static final int TRANSACTION_toggleSoftInput = 8;
        static final int TRANSACTION_updateCursor = 5;
        static final int TRANSACTION_updateCursorAnchorInfo = 10;
        static final int TRANSACTION_updateExtractedText = 2;
        static final int TRANSACTION_updateSelection = 3;
        static final int TRANSACTION_viewClicked = 4;

        private static class Proxy implements IInputMethodSession {
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

            public void finishInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_finishInput, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void updateExtractedText(int token, ExtractedText text) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    if (text != null) {
                        _data.writeInt(Stub.TRANSACTION_finishInput);
                        text.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateExtractedText, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(oldSelStart);
                    _data.writeInt(oldSelEnd);
                    _data.writeInt(newSelStart);
                    _data.writeInt(newSelEnd);
                    _data.writeInt(candidatesStart);
                    _data.writeInt(candidatesEnd);
                    this.mRemote.transact(Stub.TRANSACTION_updateSelection, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void viewClicked(boolean focusChanged) throws RemoteException {
                int i = Stub.TRANSACTION_finishInput;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!focusChanged) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_viewClicked, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void updateCursor(Rect newCursor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newCursor != null) {
                        _data.writeInt(Stub.TRANSACTION_finishInput);
                        newCursor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateCursor, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void displayCompletions(CompletionInfo[] completions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(completions, 0);
                    this.mRemote.transact(Stub.TRANSACTION_displayCompletions, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void appPrivateCommand(String action, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    if (data != null) {
                        _data.writeInt(Stub.TRANSACTION_finishInput);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_appPrivateCommand, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void toggleSoftInput(int showFlags, int hideFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showFlags);
                    _data.writeInt(hideFlags);
                    this.mRemote.transact(Stub.TRANSACTION_toggleSoftInput, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void finishSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_finishSession, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }

            public void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cursorAnchorInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_finishInput);
                        cursorAnchorInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateCursorAnchorInfo, _data, null, Stub.TRANSACTION_finishInput);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMethodSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMethodSession)) {
                return new Proxy(obj);
            }
            return (IInputMethodSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_finishInput /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishInput();
                    return true;
                case TRANSACTION_updateExtractedText /*2*/:
                    ExtractedText extractedText;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        extractedText = (ExtractedText) ExtractedText.CREATOR.createFromParcel(data);
                    } else {
                        extractedText = null;
                    }
                    updateExtractedText(_arg0, extractedText);
                    return true;
                case TRANSACTION_updateSelection /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateSelection(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_viewClicked /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    viewClicked(data.readInt() != 0);
                    return true;
                case TRANSACTION_updateCursor /*5*/:
                    Rect rect;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    updateCursor(rect);
                    return true;
                case TRANSACTION_displayCompletions /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    displayCompletions((CompletionInfo[]) data.createTypedArray(CompletionInfo.CREATOR));
                    return true;
                case TRANSACTION_appPrivateCommand /*7*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    appPrivateCommand(_arg02, bundle);
                    return true;
                case TRANSACTION_toggleSoftInput /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    toggleSoftInput(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_finishSession /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishSession();
                    return true;
                case TRANSACTION_updateCursorAnchorInfo /*10*/:
                    CursorAnchorInfo cursorAnchorInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        cursorAnchorInfo = (CursorAnchorInfo) CursorAnchorInfo.CREATOR.createFromParcel(data);
                    } else {
                        cursorAnchorInfo = null;
                    }
                    updateCursorAnchorInfo(cursorAnchorInfo);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void appPrivateCommand(String str, Bundle bundle) throws RemoteException;

    void displayCompletions(CompletionInfo[] completionInfoArr) throws RemoteException;

    void finishInput() throws RemoteException;

    void finishSession() throws RemoteException;

    void toggleSoftInput(int i, int i2) throws RemoteException;

    void updateCursor(Rect rect) throws RemoteException;

    void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException;

    void updateExtractedText(int i, ExtractedText extractedText) throws RemoteException;

    void updateSelection(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    void viewClicked(boolean z) throws RemoteException;
}

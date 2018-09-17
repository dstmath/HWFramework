package android.service.voice;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;

public interface IVoiceInteractionSession extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceInteractionSession {
        private static final String DESCRIPTOR = "android.service.voice.IVoiceInteractionSession";
        static final int TRANSACTION_closeSystemDialogs = 7;
        static final int TRANSACTION_destroy = 9;
        static final int TRANSACTION_handleAssist = 3;
        static final int TRANSACTION_handleScreenshot = 4;
        static final int TRANSACTION_hide = 2;
        static final int TRANSACTION_onLockscreenShown = 8;
        static final int TRANSACTION_show = 1;
        static final int TRANSACTION_taskFinished = 6;
        static final int TRANSACTION_taskStarted = 5;

        private static class Proxy implements IVoiceInteractionSession {
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

            public void show(Bundle sessionArgs, int flags, IVoiceInteractionSessionShowCallback showCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionArgs != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        sessionArgs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (showCallback != null) {
                        iBinder = showCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_show, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void hide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hide, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void handleAssist(Bundle assistData, AssistStructure structure, AssistContent content, int index, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (assistData != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        assistData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (structure != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        structure.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (content != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        content.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(index);
                    _data.writeInt(count);
                    this.mRemote.transact(Stub.TRANSACTION_handleAssist, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void handleScreenshot(Bitmap screenshot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (screenshot != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        screenshot.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleScreenshot, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void taskStarted(Intent intent, int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(taskId);
                    this.mRemote.transact(Stub.TRANSACTION_taskStarted, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void taskFinished(Intent intent, int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_show);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(taskId);
                    this.mRemote.transact(Stub.TRANSACTION_taskFinished, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void closeSystemDialogs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_closeSystemDialogs, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void onLockscreenShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onLockscreenShown, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void destroy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_destroy, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractionSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractionSession)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractionSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle bundle;
            Intent intent;
            switch (code) {
                case TRANSACTION_show /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    show(bundle, data.readInt(), com.android.internal.app.IVoiceInteractionSessionShowCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_hide /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    hide();
                    return true;
                case TRANSACTION_handleAssist /*3*/:
                    AssistStructure assistStructure;
                    AssistContent assistContent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    if (data.readInt() != 0) {
                        assistStructure = (AssistStructure) AssistStructure.CREATOR.createFromParcel(data);
                    } else {
                        assistStructure = null;
                    }
                    if (data.readInt() != 0) {
                        assistContent = (AssistContent) AssistContent.CREATOR.createFromParcel(data);
                    } else {
                        assistContent = null;
                    }
                    handleAssist(bundle, assistStructure, assistContent, data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_handleScreenshot /*4*/:
                    Bitmap bitmap;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    handleScreenshot(bitmap);
                    return true;
                case TRANSACTION_taskStarted /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    taskStarted(intent, data.readInt());
                    return true;
                case TRANSACTION_taskFinished /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    taskFinished(intent, data.readInt());
                    return true;
                case TRANSACTION_closeSystemDialogs /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    closeSystemDialogs();
                    return true;
                case TRANSACTION_onLockscreenShown /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onLockscreenShown();
                    return true;
                case TRANSACTION_destroy /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroy();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void closeSystemDialogs() throws RemoteException;

    void destroy() throws RemoteException;

    void handleAssist(Bundle bundle, AssistStructure assistStructure, AssistContent assistContent, int i, int i2) throws RemoteException;

    void handleScreenshot(Bitmap bitmap) throws RemoteException;

    void hide() throws RemoteException;

    void onLockscreenShown() throws RemoteException;

    void show(Bundle bundle, int i, IVoiceInteractionSessionShowCallback iVoiceInteractionSessionShowCallback) throws RemoteException;

    void taskFinished(Intent intent, int i) throws RemoteException;

    void taskStarted(Intent intent, int i) throws RemoteException;
}

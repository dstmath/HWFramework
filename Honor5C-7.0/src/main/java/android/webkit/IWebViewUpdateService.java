package android.webkit;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWebViewUpdateService extends IInterface {

    public static abstract class Stub extends Binder implements IWebViewUpdateService {
        private static final String DESCRIPTOR = "android.webkit.IWebViewUpdateService";
        static final int TRANSACTION_changeProviderAndSetting = 3;
        static final int TRANSACTION_enableFallbackLogic = 8;
        static final int TRANSACTION_getAllWebViewPackages = 5;
        static final int TRANSACTION_getCurrentWebViewPackageName = 6;
        static final int TRANSACTION_getValidWebViewPackages = 4;
        static final int TRANSACTION_isFallbackPackage = 7;
        static final int TRANSACTION_notifyRelroCreationCompleted = 1;
        static final int TRANSACTION_waitForAndGetProvider = 2;

        private static class Proxy implements IWebViewUpdateService {
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

            public void notifyRelroCreationCompleted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_notifyRelroCreationCompleted, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WebViewProviderResponse waitForAndGetProvider() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WebViewProviderResponse webViewProviderResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_waitForAndGetProvider, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        webViewProviderResponse = (WebViewProviderResponse) WebViewProviderResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        webViewProviderResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return webViewProviderResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String changeProviderAndSetting(String newProvider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(newProvider);
                    this.mRemote.transact(Stub.TRANSACTION_changeProviderAndSetting, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WebViewProviderInfo[] getValidWebViewPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getValidWebViewPackages, _data, _reply, 0);
                    _reply.readException();
                    WebViewProviderInfo[] _result = (WebViewProviderInfo[]) _reply.createTypedArray(WebViewProviderInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WebViewProviderInfo[] getAllWebViewPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllWebViewPackages, _data, _reply, 0);
                    _reply.readException();
                    WebViewProviderInfo[] _result = (WebViewProviderInfo[]) _reply.createTypedArray(WebViewProviderInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCurrentWebViewPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentWebViewPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFallbackPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_isFallbackPackage, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableFallbackLogic(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_notifyRelroCreationCompleted;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableFallbackLogic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWebViewUpdateService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWebViewUpdateService)) {
                return new Proxy(obj);
            }
            return (IWebViewUpdateService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            String _result;
            WebViewProviderInfo[] _result2;
            switch (code) {
                case TRANSACTION_notifyRelroCreationCompleted /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyRelroCreationCompleted();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_waitForAndGetProvider /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    WebViewProviderResponse _result3 = waitForAndGetProvider();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_notifyRelroCreationCompleted);
                        _result3.writeToParcel(reply, TRANSACTION_notifyRelroCreationCompleted);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_changeProviderAndSetting /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = changeProviderAndSetting(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getValidWebViewPackages /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getValidWebViewPackages();
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_notifyRelroCreationCompleted);
                    return true;
                case TRANSACTION_getAllWebViewPackages /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAllWebViewPackages();
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_notifyRelroCreationCompleted);
                    return true;
                case TRANSACTION_getCurrentWebViewPackageName /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCurrentWebViewPackageName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_isFallbackPackage /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result4 = isFallbackPackage(data.readString());
                    reply.writeNoException();
                    if (_result4) {
                        i = TRANSACTION_notifyRelroCreationCompleted;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_enableFallbackLogic /*8*/:
                    boolean _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    } else {
                        _arg0 = false;
                    }
                    enableFallbackLogic(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String changeProviderAndSetting(String str) throws RemoteException;

    void enableFallbackLogic(boolean z) throws RemoteException;

    WebViewProviderInfo[] getAllWebViewPackages() throws RemoteException;

    String getCurrentWebViewPackageName() throws RemoteException;

    WebViewProviderInfo[] getValidWebViewPackages() throws RemoteException;

    boolean isFallbackPackage(String str) throws RemoteException;

    void notifyRelroCreationCompleted() throws RemoteException;

    WebViewProviderResponse waitForAndGetProvider() throws RemoteException;
}

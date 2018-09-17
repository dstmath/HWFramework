package android.view.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.IWindow;
import java.util.List;

public interface IAccessibilityManager extends IInterface {

    public static abstract class Stub extends Binder implements IAccessibilityManager {
        private static final String DESCRIPTOR = "android.view.accessibility.IAccessibilityManager";
        static final int TRANSACTION_addAccessibilityInteractionConnection = 6;
        static final int TRANSACTION_addClient = 1;
        static final int TRANSACTION_disableAccessibilityService = 13;
        static final int TRANSACTION_enableAccessibilityService = 12;
        static final int TRANSACTION_getEnabledAccessibilityServiceList = 4;
        static final int TRANSACTION_getInstalledAccessibilityServiceList = 3;
        static final int TRANSACTION_getWindowToken = 11;
        static final int TRANSACTION_interrupt = 5;
        static final int TRANSACTION_registerUiTestAutomationService = 8;
        static final int TRANSACTION_removeAccessibilityInteractionConnection = 7;
        static final int TRANSACTION_sendAccessibilityEvent = 2;
        static final int TRANSACTION_temporaryEnableAccessibilityStateUntilKeyguardRemoved = 10;
        static final int TRANSACTION_unregisterUiTestAutomationService = 9;

        private static class Proxy implements IAccessibilityManager {
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

            public int addClient(IAccessibilityManagerClient client, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addClient, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendAccessibilityEvent(AccessibilityEvent uiEvent, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uiEvent != null) {
                        _data.writeInt(Stub.TRANSACTION_addClient);
                        uiEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_sendAccessibilityEvent, _data, _reply, 0);
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

            public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getInstalledAccessibilityServiceList, _data, _reply, 0);
                    _reply.readException();
                    List<AccessibilityServiceInfo> _result = _reply.createTypedArrayList(AccessibilityServiceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feedbackType);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getEnabledAccessibilityServiceList, _data, _reply, 0);
                    _reply.readException();
                    List<AccessibilityServiceInfo> _result = _reply.createTypedArrayList(AccessibilityServiceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interrupt(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_interrupt, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (windowToken != null) {
                        asBinder = windowToken.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (connection != null) {
                        iBinder = connection.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addAccessibilityInteractionConnection, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAccessibilityInteractionConnection(IWindow windowToken) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (windowToken != null) {
                        iBinder = windowToken.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeAccessibilityInteractionConnection, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerUiTestAutomationService(IBinder owner, IAccessibilityServiceClient client, AccessibilityServiceInfo info, int flags) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(owner);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_addClient);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_registerUiTestAutomationService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterUiTestAutomationService(IAccessibilityServiceClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterUiTestAutomationService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void temporaryEnableAccessibilityStateUntilKeyguardRemoved(ComponentName service, boolean touchExplorationEnabled) throws RemoteException {
                int i = Stub.TRANSACTION_addClient;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(Stub.TRANSACTION_addClient);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!touchExplorationEnabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_temporaryEnableAccessibilityStateUntilKeyguardRemoved, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getWindowToken(int windowId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(windowId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getWindowToken, _data, _reply, 0);
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableAccessibilityService(ComponentName service, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(Stub.TRANSACTION_addClient);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_enableAccessibilityService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableAccessibilityService(ComponentName service, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(Stub.TRANSACTION_addClient);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_disableAccessibilityService, _data, _reply, 0);
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

        public static IAccessibilityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccessibilityManager)) {
                return new Proxy(obj);
            }
            return (IAccessibilityManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            List<AccessibilityServiceInfo> _result2;
            ComponentName componentName;
            switch (code) {
                case TRANSACTION_addClient /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addClient(android.view.accessibility.IAccessibilityManagerClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendAccessibilityEvent /*2*/:
                    AccessibilityEvent accessibilityEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        accessibilityEvent = (AccessibilityEvent) AccessibilityEvent.CREATOR.createFromParcel(data);
                    } else {
                        accessibilityEvent = null;
                    }
                    boolean _result3 = sendAccessibilityEvent(accessibilityEvent, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_addClient : 0);
                    return true;
                case TRANSACTION_getInstalledAccessibilityServiceList /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInstalledAccessibilityServiceList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getEnabledAccessibilityServiceList /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getEnabledAccessibilityServiceList(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_interrupt /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    interrupt(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addAccessibilityInteractionConnection /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addAccessibilityInteractionConnection(android.view.IWindow.Stub.asInterface(data.readStrongBinder()), android.view.accessibility.IAccessibilityInteractionConnection.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_removeAccessibilityInteractionConnection /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeAccessibilityInteractionConnection(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerUiTestAutomationService /*8*/:
                    AccessibilityServiceInfo accessibilityServiceInfo;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg0 = data.readStrongBinder();
                    IAccessibilityServiceClient _arg1 = android.accessibilityservice.IAccessibilityServiceClient.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        accessibilityServiceInfo = (AccessibilityServiceInfo) AccessibilityServiceInfo.CREATOR.createFromParcel(data);
                    } else {
                        accessibilityServiceInfo = null;
                    }
                    registerUiTestAutomationService(_arg0, _arg1, accessibilityServiceInfo, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterUiTestAutomationService /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterUiTestAutomationService(android.accessibilityservice.IAccessibilityServiceClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_temporaryEnableAccessibilityStateUntilKeyguardRemoved /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    temporaryEnableAccessibilityStateUntilKeyguardRemoved(componentName, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWindowToken /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _result4 = getWindowToken(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4);
                    return true;
                case TRANSACTION_enableAccessibilityService /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    enableAccessibilityService(componentName, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableAccessibilityService /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    disableAccessibilityService(componentName, data.readInt());
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

    int addAccessibilityInteractionConnection(IWindow iWindow, IAccessibilityInteractionConnection iAccessibilityInteractionConnection, int i) throws RemoteException;

    int addClient(IAccessibilityManagerClient iAccessibilityManagerClient, int i) throws RemoteException;

    void disableAccessibilityService(ComponentName componentName, int i) throws RemoteException;

    void enableAccessibilityService(ComponentName componentName, int i) throws RemoteException;

    List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int i, int i2) throws RemoteException;

    List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(int i) throws RemoteException;

    IBinder getWindowToken(int i, int i2) throws RemoteException;

    void interrupt(int i) throws RemoteException;

    void registerUiTestAutomationService(IBinder iBinder, IAccessibilityServiceClient iAccessibilityServiceClient, AccessibilityServiceInfo accessibilityServiceInfo, int i) throws RemoteException;

    void removeAccessibilityInteractionConnection(IWindow iWindow) throws RemoteException;

    boolean sendAccessibilityEvent(AccessibilityEvent accessibilityEvent, int i) throws RemoteException;

    void temporaryEnableAccessibilityStateUntilKeyguardRemoved(ComponentName componentName, boolean z) throws RemoteException;

    void unregisterUiTestAutomationService(IAccessibilityServiceClient iAccessibilityServiceClient) throws RemoteException;
}

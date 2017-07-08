package com.android.internal.appwidget;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.RemoteViews;

public interface IAppWidgetService extends IInterface {

    public static abstract class Stub extends Binder implements IAppWidgetService {
        private static final String DESCRIPTOR = "com.android.internal.appwidget.IAppWidgetService";
        static final int TRANSACTION_allocateAppWidgetId = 3;
        static final int TRANSACTION_bindAppWidgetId = 20;
        static final int TRANSACTION_bindRemoteViewsService = 21;
        static final int TRANSACTION_createAppWidgetConfigIntentSender = 9;
        static final int TRANSACTION_deleteAllHosts = 6;
        static final int TRANSACTION_deleteAppWidgetId = 4;
        static final int TRANSACTION_deleteHost = 5;
        static final int TRANSACTION_getAppWidgetIds = 23;
        static final int TRANSACTION_getAppWidgetIdsForHost = 8;
        static final int TRANSACTION_getAppWidgetInfo = 17;
        static final int TRANSACTION_getAppWidgetOptions = 12;
        static final int TRANSACTION_getAppWidgetViews = 7;
        static final int TRANSACTION_getInstalledProvidersForProfile = 16;
        static final int TRANSACTION_hasBindAppWidgetPermission = 18;
        static final int TRANSACTION_isBoundWidgetPackage = 24;
        static final int TRANSACTION_notifyAppWidgetViewDataChanged = 15;
        static final int TRANSACTION_partiallyUpdateAppWidgetIds = 13;
        static final int TRANSACTION_setBindAppWidgetPermission = 19;
        static final int TRANSACTION_startListening = 1;
        static final int TRANSACTION_stopListening = 2;
        static final int TRANSACTION_unbindRemoteViewsService = 22;
        static final int TRANSACTION_updateAppWidgetIds = 10;
        static final int TRANSACTION_updateAppWidgetOptions = 11;
        static final int TRANSACTION_updateAppWidgetProvider = 14;

        private static class Proxy implements IAppWidgetService {
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

            public ParceledListSlice startListening(IAppWidgetHost host, String callingPackage, int hostId, int[] appWidgetIds, int[] updatedIds) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (host != null) {
                        iBinder = host.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(callingPackage);
                    _data.writeInt(hostId);
                    _data.writeIntArray(appWidgetIds);
                    if (updatedIds == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(updatedIds.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startListening, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.readIntArray(updatedIds);
                    return parceledListSlice;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopListening(String callingPackage, int hostId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(hostId);
                    this.mRemote.transact(Stub.TRANSACTION_stopListening, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int allocateAppWidgetId(String callingPackage, int hostId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(hostId);
                    this.mRemote.transact(Stub.TRANSACTION_allocateAppWidgetId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteAppWidgetId(String callingPackage, int appWidgetId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    this.mRemote.transact(Stub.TRANSACTION_deleteAppWidgetId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteHost(String packageName, int hostId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(hostId);
                    this.mRemote.transact(Stub.TRANSACTION_deleteHost, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteAllHosts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_deleteAllHosts, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RemoteViews getAppWidgetViews(String callingPackage, int appWidgetId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    RemoteViews remoteViews;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    this.mRemote.transact(Stub.TRANSACTION_getAppWidgetViews, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        remoteViews = (RemoteViews) RemoteViews.CREATOR.createFromParcel(_reply);
                    } else {
                        remoteViews = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return remoteViews;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAppWidgetIdsForHost(String callingPackage, int hostId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(hostId);
                    this.mRemote.transact(Stub.TRANSACTION_getAppWidgetIdsForHost, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IntentSender createAppWidgetConfigIntentSender(String callingPackage, int appWidgetId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IntentSender intentSender;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    this.mRemote.transact(Stub.TRANSACTION_createAppWidgetConfigIntentSender, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        intentSender = (IntentSender) IntentSender.CREATOR.createFromParcel(_reply);
                    } else {
                        intentSender = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return intentSender;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeIntArray(appWidgetIds);
                    if (views != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        views.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateAppWidgetIds, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppWidgetOptions(String callingPackage, int appWidgetId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateAppWidgetOptions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getAppWidgetOptions(String callingPackage, int appWidgetId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    this.mRemote.transact(Stub.TRANSACTION_getAppWidgetOptions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void partiallyUpdateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeIntArray(appWidgetIds);
                    if (views != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        views.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_partiallyUpdateAppWidgetIds, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppWidgetProvider(ComponentName provider, RemoteViews views) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (provider != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        provider.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (views != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        views.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateAppWidgetProvider, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppWidgetViewDataChanged(String packageName, int[] appWidgetIds, int viewId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeIntArray(appWidgetIds);
                    _data.writeInt(viewId);
                    this.mRemote.transact(Stub.TRANSACTION_notifyAppWidgetViewDataChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getInstalledProvidersForProfile(int categoryFilter, int profileId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(categoryFilter);
                    _data.writeInt(profileId);
                    this.mRemote.transact(Stub.TRANSACTION_getInstalledProvidersForProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AppWidgetProviderInfo getAppWidgetInfo(String callingPackage, int appWidgetId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AppWidgetProviderInfo appWidgetProviderInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    this.mRemote.transact(Stub.TRANSACTION_getAppWidgetInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        appWidgetProviderInfo = (AppWidgetProviderInfo) AppWidgetProviderInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        appWidgetProviderInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return appWidgetProviderInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasBindAppWidgetPermission(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_hasBindAppWidgetPermission, _data, _reply, 0);
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

            public void setBindAppWidgetPermission(String packageName, int userId, boolean permission) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (permission) {
                        i = Stub.TRANSACTION_startListening;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setBindAppWidgetPermission, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean bindAppWidgetId(String callingPackage, int appWidgetId, int providerProfileId, ComponentName providerComponent, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    _data.writeInt(providerProfileId);
                    if (providerComponent != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        providerComponent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_bindAppWidgetId, _data, _reply, 0);
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

            public void bindRemoteViewsService(String callingPackage, int appWidgetId, Intent intent, IBinder connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(connection);
                    this.mRemote.transact(Stub.TRANSACTION_bindRemoteViewsService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unbindRemoteViewsService(String callingPackage, int appWidgetId, Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(appWidgetId);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unbindRemoteViewsService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAppWidgetIds(ComponentName providerComponent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (providerComponent != null) {
                        _data.writeInt(Stub.TRANSACTION_startListening);
                        providerComponent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAppWidgetIds, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBoundWidgetPackage(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isBoundWidgetPackage, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppWidgetService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppWidgetService)) {
                return new Proxy(obj);
            }
            return (IAppWidgetService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg2;
            ParceledListSlice _result;
            int[] _result2;
            String _arg0;
            int[] _arg1;
            RemoteViews remoteViews;
            int _arg12;
            ComponentName componentName;
            boolean _result3;
            Intent intent;
            switch (code) {
                case TRANSACTION_startListening /*1*/:
                    int[] iArr;
                    data.enforceInterface(DESCRIPTOR);
                    IAppWidgetHost _arg02 = com.android.internal.appwidget.IAppWidgetHost.Stub.asInterface(data.readStrongBinder());
                    String _arg13 = data.readString();
                    _arg2 = data.readInt();
                    int[] _arg3 = data.createIntArray();
                    int _arg4_length = data.readInt();
                    if (_arg4_length < 0) {
                        iArr = null;
                    } else {
                        iArr = new int[_arg4_length];
                    }
                    _result = startListening(_arg02, _arg13, _arg2, _arg3, iArr);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_startListening);
                        _result.writeToParcel(reply, TRANSACTION_startListening);
                    } else {
                        reply.writeInt(0);
                    }
                    reply.writeIntArray(iArr);
                    return true;
                case TRANSACTION_stopListening /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopListening(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_allocateAppWidgetId /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = allocateAppWidgetId(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_deleteAppWidgetId /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteAppWidgetId(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteHost /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteHost(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteAllHosts /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteAllHosts();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAppWidgetViews /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    RemoteViews _result5 = getAppWidgetViews(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_startListening);
                        _result5.writeToParcel(reply, TRANSACTION_startListening);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAppWidgetIdsForHost /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppWidgetIdsForHost(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_createAppWidgetConfigIntentSender /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    IntentSender _result6 = createAppWidgetConfigIntentSender(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_startListening);
                        _result6.writeToParcel(reply, TRANSACTION_startListening);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateAppWidgetIds /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.createIntArray();
                    if (data.readInt() != 0) {
                        remoteViews = (RemoteViews) RemoteViews.CREATOR.createFromParcel(data);
                    } else {
                        remoteViews = null;
                    }
                    updateAppWidgetIds(_arg0, _arg1, remoteViews);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateAppWidgetOptions /*11*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    updateAppWidgetOptions(_arg0, _arg12, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAppWidgetOptions /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result7 = getAppWidgetOptions(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_startListening);
                        _result7.writeToParcel(reply, TRANSACTION_startListening);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_partiallyUpdateAppWidgetIds /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.createIntArray();
                    if (data.readInt() != 0) {
                        remoteViews = (RemoteViews) RemoteViews.CREATOR.createFromParcel(data);
                    } else {
                        remoteViews = null;
                    }
                    partiallyUpdateAppWidgetIds(_arg0, _arg1, remoteViews);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateAppWidgetProvider /*14*/:
                    RemoteViews remoteViews2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        remoteViews2 = (RemoteViews) RemoteViews.CREATOR.createFromParcel(data);
                    } else {
                        remoteViews2 = null;
                    }
                    updateAppWidgetProvider(componentName, remoteViews2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyAppWidgetViewDataChanged /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyAppWidgetViewDataChanged(data.readString(), data.createIntArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getInstalledProvidersForProfile /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getInstalledProvidersForProfile(data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_startListening);
                        _result.writeToParcel(reply, TRANSACTION_startListening);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAppWidgetInfo /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    AppWidgetProviderInfo _result8 = getAppWidgetInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(TRANSACTION_startListening);
                        _result8.writeToParcel(reply, TRANSACTION_startListening);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_hasBindAppWidgetPermission /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = hasBindAppWidgetPermission(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_startListening : 0);
                    return true;
                case TRANSACTION_setBindAppWidgetPermission /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBindAppWidgetPermission(data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_bindAppWidgetId /*20*/:
                    ComponentName componentName2;
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg12 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        componentName2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName2 = null;
                    }
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    _result3 = bindAppWidgetId(_arg0, _arg12, _arg2, componentName2, bundle2);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_startListening : 0);
                    return true;
                case TRANSACTION_bindRemoteViewsService /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    bindRemoteViewsService(_arg0, _arg12, intent, data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unbindRemoteViewsService /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    unbindRemoteViewsService(_arg0, _arg12, intent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAppWidgetIds /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result2 = getAppWidgetIds(componentName);
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_isBoundWidgetPackage /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isBoundWidgetPackage(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_startListening : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int allocateAppWidgetId(String str, int i) throws RemoteException;

    boolean bindAppWidgetId(String str, int i, int i2, ComponentName componentName, Bundle bundle) throws RemoteException;

    void bindRemoteViewsService(String str, int i, Intent intent, IBinder iBinder) throws RemoteException;

    IntentSender createAppWidgetConfigIntentSender(String str, int i) throws RemoteException;

    void deleteAllHosts() throws RemoteException;

    void deleteAppWidgetId(String str, int i) throws RemoteException;

    void deleteHost(String str, int i) throws RemoteException;

    int[] getAppWidgetIds(ComponentName componentName) throws RemoteException;

    int[] getAppWidgetIdsForHost(String str, int i) throws RemoteException;

    AppWidgetProviderInfo getAppWidgetInfo(String str, int i) throws RemoteException;

    Bundle getAppWidgetOptions(String str, int i) throws RemoteException;

    RemoteViews getAppWidgetViews(String str, int i) throws RemoteException;

    ParceledListSlice getInstalledProvidersForProfile(int i, int i2) throws RemoteException;

    boolean hasBindAppWidgetPermission(String str, int i) throws RemoteException;

    boolean isBoundWidgetPackage(String str, int i) throws RemoteException;

    void notifyAppWidgetViewDataChanged(String str, int[] iArr, int i) throws RemoteException;

    void partiallyUpdateAppWidgetIds(String str, int[] iArr, RemoteViews remoteViews) throws RemoteException;

    void setBindAppWidgetPermission(String str, int i, boolean z) throws RemoteException;

    ParceledListSlice startListening(IAppWidgetHost iAppWidgetHost, String str, int i, int[] iArr, int[] iArr2) throws RemoteException;

    void stopListening(String str, int i) throws RemoteException;

    void unbindRemoteViewsService(String str, int i, Intent intent) throws RemoteException;

    void updateAppWidgetIds(String str, int[] iArr, RemoteViews remoteViews) throws RemoteException;

    void updateAppWidgetOptions(String str, int i, Bundle bundle) throws RemoteException;

    void updateAppWidgetProvider(ComponentName componentName, RemoteViews remoteViews) throws RemoteException;
}

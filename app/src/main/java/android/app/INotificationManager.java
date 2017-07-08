package android.app;

import android.app.NotificationManager.Policy;
import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import java.util.List;

public interface INotificationManager extends IInterface {

    public static abstract class Stub extends Binder implements INotificationManager {
        private static final String DESCRIPTOR = "android.app.INotificationManager";
        static final int TRANSACTION_addAutomaticZenRule = 49;
        static final int TRANSACTION_applyAdjustmentFromRankerService = 32;
        static final int TRANSACTION_applyAdjustmentsFromRankerService = 33;
        static final int TRANSACTION_applyRestore = 55;
        static final int TRANSACTION_areNotificationsEnabled = 8;
        static final int TRANSACTION_areNotificationsEnabledForPackage = 7;
        static final int TRANSACTION_cancelAllNotifications = 1;
        static final int TRANSACTION_cancelNotificationFromListener = 20;
        static final int TRANSACTION_cancelNotificationWithTag = 5;
        static final int TRANSACTION_cancelNotificationsFromListener = 21;
        static final int TRANSACTION_cancelToast = 3;
        static final int TRANSACTION_enqueueNotificationWithTag = 4;
        static final int TRANSACTION_enqueueToast = 2;
        static final int TRANSACTION_enqueueToastEx = 57;
        static final int TRANSACTION_getActiveNotifications = 16;
        static final int TRANSACTION_getActiveNotificationsFromListener = 25;
        static final int TRANSACTION_getAppActiveNotifications = 56;
        static final int TRANSACTION_getAutomaticZenRule = 47;
        static final int TRANSACTION_getBackupPayload = 54;
        static final int TRANSACTION_getEffectsSuppressor = 34;
        static final int TRANSACTION_getHintsFromListener = 27;
        static final int TRANSACTION_getHistoricalNotifications = 17;
        static final int TRANSACTION_getImportance = 14;
        static final int TRANSACTION_getInterruptionFilterFromListener = 29;
        static final int TRANSACTION_getNotificationPolicy = 42;
        static final int TRANSACTION_getPackageImportance = 15;
        static final int TRANSACTION_getPackagesRequestingNotificationPolicyAccess = 44;
        static final int TRANSACTION_getPriority = 12;
        static final int TRANSACTION_getRuleInstanceCount = 53;
        static final int TRANSACTION_getVisibilityOverride = 10;
        static final int TRANSACTION_getZenMode = 37;
        static final int TRANSACTION_getZenModeConfig = 38;
        static final int TRANSACTION_getZenRules = 48;
        static final int TRANSACTION_isNotificationPolicyAccessGranted = 41;
        static final int TRANSACTION_isNotificationPolicyAccessGrantedForPackage = 45;
        static final int TRANSACTION_isSystemConditionProviderEnabled = 36;
        static final int TRANSACTION_matchesCallFilter = 35;
        static final int TRANSACTION_notifyConditions = 40;
        static final int TRANSACTION_registerListener = 18;
        static final int TRANSACTION_removeAutomaticZenRule = 51;
        static final int TRANSACTION_removeAutomaticZenRules = 52;
        static final int TRANSACTION_requestBindListener = 22;
        static final int TRANSACTION_requestHintsFromListener = 26;
        static final int TRANSACTION_requestInterruptionFilterFromListener = 28;
        static final int TRANSACTION_requestUnbindListener = 23;
        static final int TRANSACTION_setImportance = 13;
        static final int TRANSACTION_setInterruptionFilter = 31;
        static final int TRANSACTION_setNotificationPolicy = 43;
        static final int TRANSACTION_setNotificationPolicyAccessGranted = 46;
        static final int TRANSACTION_setNotificationsEnabledForPackage = 6;
        static final int TRANSACTION_setNotificationsShownFromListener = 24;
        static final int TRANSACTION_setOnNotificationPostedTrimFromListener = 30;
        static final int TRANSACTION_setPriority = 11;
        static final int TRANSACTION_setVisibilityOverride = 9;
        static final int TRANSACTION_setZenMode = 39;
        static final int TRANSACTION_unregisterListener = 19;
        static final int TRANSACTION_updateAutomaticZenRule = 50;

        private static class Proxy implements INotificationManager {
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

            public void cancelAllNotifications(String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelAllNotifications, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enqueueToast(String pkg, ITransientNotification callback, int duration) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(duration);
                    this.mRemote.transact(Stub.TRANSACTION_enqueueToast, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelToast(String pkg, ITransientNotification callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_cancelToast, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int[] idReceived, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(opPkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    if (notification != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        notification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeIntArray(idReceived);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_enqueueNotificationWithTag, _data, _reply, 0);
                    _reply.readException();
                    _reply.readIntArray(idReceived);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelNotificationWithTag, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (enabled) {
                        i = Stub.TRANSACTION_cancelAllNotifications;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setNotificationsEnabledForPackage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean areNotificationsEnabledForPackage(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_areNotificationsEnabledForPackage, _data, _reply, 0);
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

            public boolean areNotificationsEnabled(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_areNotificationsEnabled, _data, _reply, 0);
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

            public void setVisibilityOverride(String pkg, int uid, int visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(visibility);
                    this.mRemote.transact(Stub.TRANSACTION_setVisibilityOverride, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVisibilityOverride(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getVisibilityOverride, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPriority(String pkg, int uid, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(priority);
                    this.mRemote.transact(Stub.TRANSACTION_setPriority, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPriority(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getPriority, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setImportance(String pkg, int uid, int importance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(importance);
                    this.mRemote.transact(Stub.TRANSACTION_setImportance, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getImportance(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getImportance, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPackageImportance(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getPackageImportance, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusBarNotification[] getActiveNotifications(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNotifications, _data, _reply, 0);
                    _reply.readException();
                    StatusBarNotification[] _result = (StatusBarNotification[]) _reply.createTypedArray(StatusBarNotification.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(count);
                    this.mRemote.transact(Stub.TRANSACTION_getHistoricalNotifications, _data, _reply, 0);
                    _reply.readException();
                    StatusBarNotification[] _result = (StatusBarNotification[]) _reply.createTypedArray(StatusBarNotification.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerListener(INotificationListener listener, ComponentName component, int userid) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userid);
                    this.mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(INotificationListener listener, int userid) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userid);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    this.mRemote.transact(Stub.TRANSACTION_cancelNotificationFromListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelNotificationsFromListener(INotificationListener token, String[] keys) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeStringArray(keys);
                    this.mRemote.transact(Stub.TRANSACTION_cancelNotificationsFromListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestBindListener(ComponentName component) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestBindListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestUnbindListener(INotificationListener token) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_requestUnbindListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationsShownFromListener(INotificationListener token, String[] keys) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeStringArray(keys);
                    this.mRemote.transact(Stub.TRANSACTION_setNotificationsShownFromListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeStringArray(keys);
                    _data.writeInt(trim);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNotificationsFromListener, _data, _reply, 0);
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

            public void requestHintsFromListener(INotificationListener token, int hints) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(hints);
                    this.mRemote.transact(Stub.TRANSACTION_requestHintsFromListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHintsFromListener(INotificationListener token) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getHintsFromListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(interruptionFilter);
                    this.mRemote.transact(Stub.TRANSACTION_requestInterruptionFilterFromListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getInterruptionFilterFromListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(trim);
                    this.mRemote.transact(Stub.TRANSACTION_setOnNotificationPostedTrimFromListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterruptionFilter(String pkg, int interruptionFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(interruptionFilter);
                    this.mRemote.transact(Stub.TRANSACTION_setInterruptionFilter, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyAdjustmentFromRankerService(INotificationListener token, Adjustment adjustment) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (adjustment != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        adjustment.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_applyAdjustmentFromRankerService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyAdjustmentsFromRankerService(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeTypedList(adjustments);
                    this.mRemote.transact(Stub.TRANSACTION_applyAdjustmentsFromRankerService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getEffectsSuppressor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getEffectsSuppressor, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return componentName;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean matchesCallFilter(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_matchesCallFilter, _data, _reply, 0);
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

            public boolean isSystemConditionProviderEnabled(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(Stub.TRANSACTION_isSystemConditionProviderEnabled, _data, _reply, 0);
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

            public int getZenMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getZenMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ZenModeConfig getZenModeConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ZenModeConfig zenModeConfig;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getZenModeConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        zenModeConfig = (ZenModeConfig) ZenModeConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        zenModeConfig = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return zenModeConfig;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setZenMode(int mode, Uri conditionId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (conditionId != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        conditionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(reason);
                    this.mRemote.transact(Stub.TRANSACTION_setZenMode, _data, null, Stub.TRANSACTION_cancelAllNotifications);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyConditions(String pkg, IConditionProvider provider, Condition[] conditions) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (provider != null) {
                        iBinder = provider.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeTypedArray(conditions, 0);
                    this.mRemote.transact(Stub.TRANSACTION_notifyConditions, _data, null, Stub.TRANSACTION_cancelAllNotifications);
                } finally {
                    _data.recycle();
                }
            }

            public boolean isNotificationPolicyAccessGranted(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_isNotificationPolicyAccessGranted, _data, _reply, 0);
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

            public Policy getNotificationPolicy(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Policy policy;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getNotificationPolicy, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        policy = (Policy) Policy.CREATOR.createFromParcel(_reply);
                    } else {
                        policy = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return policy;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationPolicy(String pkg, Policy policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (policy != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setNotificationPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getPackagesRequestingNotificationPolicyAccess() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPackagesRequestingNotificationPolicyAccess, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_isNotificationPolicyAccessGrantedForPackage, _data, _reply, 0);
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

            public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (granted) {
                        i = Stub.TRANSACTION_cancelAllNotifications;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setNotificationPolicyAccessGranted, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AutomaticZenRule automaticZenRule;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(Stub.TRANSACTION_getAutomaticZenRule, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        automaticZenRule = (AutomaticZenRule) AutomaticZenRule.CREATOR.createFromParcel(_reply);
                    } else {
                        automaticZenRule = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return automaticZenRule;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ZenRule> getZenRules() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getZenRules, _data, _reply, 0);
                    _reply.readException();
                    List<ZenRule> _result = _reply.createTypedArrayList(ZenRule.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (automaticZenRule != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        automaticZenRule.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addAutomaticZenRule, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (automaticZenRule != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        automaticZenRule.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateAutomaticZenRule, _data, _reply, 0);
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

            public boolean removeAutomaticZenRule(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(Stub.TRANSACTION_removeAutomaticZenRule, _data, _reply, 0);
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

            public boolean removeAutomaticZenRules(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_removeAutomaticZenRules, _data, _reply, 0);
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

            public int getRuleInstanceCount(ComponentName owner) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (owner != null) {
                        _data.writeInt(Stub.TRANSACTION_cancelAllNotifications);
                        owner.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getRuleInstanceCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getBackupPayload(int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user);
                    this.mRemote.transact(Stub.TRANSACTION_getBackupPayload, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyRestore(byte[] payload, int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(payload);
                    _data.writeInt(user);
                    this.mRemote.transact(Stub.TRANSACTION_applyRestore, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAppActiveNotifications(String callingPkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getAppActiveNotifications, _data, _reply, 0);
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

            public void enqueueToastEx(String pkg, ITransientNotification callback, int duration, String toastStr) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(duration);
                    _data.writeString(toastStr);
                    this.mRemote.transact(Stub.TRANSACTION_enqueueToastEx, _data, _reply, 0);
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

        public static INotificationManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INotificationManager)) {
                return new Proxy(obj);
            }
            return (INotificationManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            boolean _result;
            int _result2;
            StatusBarNotification[] _result3;
            INotificationListener _arg02;
            ComponentName componentName;
            ParceledListSlice _result4;
            switch (code) {
                case TRANSACTION_cancelAllNotifications /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelAllNotifications(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enqueueToast /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    enqueueToast(data.readString(), android.app.ITransientNotification.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelToast /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelToast(data.readString(), android.app.ITransientNotification.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enqueueNotificationWithTag /*4*/:
                    Notification notification;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    int _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        notification = (Notification) Notification.CREATOR.createFromParcel(data);
                    } else {
                        notification = null;
                    }
                    int[] _arg5 = data.createIntArray();
                    enqueueNotificationWithTag(_arg0, _arg1, _arg2, _arg3, notification, _arg5, data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_arg5);
                    return true;
                case TRANSACTION_cancelNotificationWithTag /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelNotificationWithTag(data.readString(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setNotificationsEnabledForPackage /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNotificationsEnabledForPackage(data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_areNotificationsEnabledForPackage /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = areNotificationsEnabledForPackage(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_areNotificationsEnabled /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = areNotificationsEnabled(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_setVisibilityOverride /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVisibilityOverride(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getVisibilityOverride /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVisibilityOverride(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setPriority /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPriority(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPriority /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPriority(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setImportance /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImportance(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getImportance /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getImportance(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPackageImportance /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPackageImportance(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getActiveNotifications /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActiveNotifications(data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result3, TRANSACTION_cancelAllNotifications);
                    return true;
                case TRANSACTION_getHistoricalNotifications /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getHistoricalNotifications(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result3, TRANSACTION_cancelAllNotifications);
                    return true;
                case TRANSACTION_registerListener /*18*/:
                    ComponentName componentName2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        componentName2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName2 = null;
                    }
                    registerListener(_arg02, componentName2, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterListener /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelNotificationFromListener /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelNotificationFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelNotificationsFromListener /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelNotificationsFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestBindListener /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    requestBindListener(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestUnbindListener /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestUnbindListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setNotificationsShownFromListener /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNotificationsShownFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getActiveNotificationsFromListener /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getActiveNotificationsFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_cancelAllNotifications);
                        _result4.writeToParcel(reply, TRANSACTION_cancelAllNotifications);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_requestHintsFromListener /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestHintsFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getHintsFromListener /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getHintsFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_requestInterruptionFilterFromListener /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestInterruptionFilterFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getInterruptionFilterFromListener /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInterruptionFilterFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setOnNotificationPostedTrimFromListener /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOnNotificationPostedTrimFromListener(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInterruptionFilter /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterruptionFilter(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_applyAdjustmentFromRankerService /*32*/:
                    Adjustment adjustment;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        adjustment = (Adjustment) Adjustment.CREATOR.createFromParcel(data);
                    } else {
                        adjustment = null;
                    }
                    applyAdjustmentFromRankerService(_arg02, adjustment);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_applyAdjustmentsFromRankerService /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    applyAdjustmentsFromRankerService(android.service.notification.INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createTypedArrayList(Adjustment.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getEffectsSuppressor /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    ComponentName _result5 = getEffectsSuppressor();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_cancelAllNotifications);
                        _result5.writeToParcel(reply, (int) TRANSACTION_cancelAllNotifications);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_matchesCallFilter /*35*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = matchesCallFilter(bundle);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_isSystemConditionProviderEnabled /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSystemConditionProviderEnabled(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_getZenMode /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getZenMode();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getZenModeConfig /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    ZenModeConfig _result6 = getZenModeConfig();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_cancelAllNotifications);
                        _result6.writeToParcel(reply, TRANSACTION_cancelAllNotifications);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setZenMode /*39*/:
                    Uri uri;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg03 = data.readInt();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    setZenMode(_arg03, uri, data.readString());
                    return true;
                case TRANSACTION_notifyConditions /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyConditions(data.readString(), android.service.notification.IConditionProvider.Stub.asInterface(data.readStrongBinder()), (Condition[]) data.createTypedArray(Condition.CREATOR));
                    return true;
                case TRANSACTION_isNotificationPolicyAccessGranted /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isNotificationPolicyAccessGranted(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_getNotificationPolicy /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    Policy _result7 = getNotificationPolicy(data.readString());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_cancelAllNotifications);
                        _result7.writeToParcel(reply, TRANSACTION_cancelAllNotifications);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setNotificationPolicy /*43*/:
                    Policy policy;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        policy = (Policy) Policy.CREATOR.createFromParcel(data);
                    } else {
                        policy = null;
                    }
                    setNotificationPolicy(_arg0, policy);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPackagesRequestingNotificationPolicyAccess /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result8 = getPackagesRequestingNotificationPolicyAccess();
                    reply.writeNoException();
                    reply.writeStringArray(_result8);
                    return true;
                case TRANSACTION_isNotificationPolicyAccessGrantedForPackage /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isNotificationPolicyAccessGrantedForPackage(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_setNotificationPolicyAccessGranted /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNotificationPolicyAccessGranted(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAutomaticZenRule /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    AutomaticZenRule _result9 = getAutomaticZenRule(data.readString());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_cancelAllNotifications);
                        _result9.writeToParcel(reply, TRANSACTION_cancelAllNotifications);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getZenRules /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<ZenRule> _result10 = getZenRules();
                    reply.writeNoException();
                    reply.writeTypedList(_result10);
                    return true;
                case TRANSACTION_addAutomaticZenRule /*49*/:
                    AutomaticZenRule automaticZenRule;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        automaticZenRule = (AutomaticZenRule) AutomaticZenRule.CREATOR.createFromParcel(data);
                    } else {
                        automaticZenRule = null;
                    }
                    String _result11 = addAutomaticZenRule(automaticZenRule);
                    reply.writeNoException();
                    reply.writeString(_result11);
                    return true;
                case TRANSACTION_updateAutomaticZenRule /*50*/:
                    AutomaticZenRule automaticZenRule2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        automaticZenRule2 = (AutomaticZenRule) AutomaticZenRule.CREATOR.createFromParcel(data);
                    } else {
                        automaticZenRule2 = null;
                    }
                    _result = updateAutomaticZenRule(_arg0, automaticZenRule2);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_removeAutomaticZenRule /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = removeAutomaticZenRule(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_removeAutomaticZenRules /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = removeAutomaticZenRules(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_cancelAllNotifications : 0);
                    return true;
                case TRANSACTION_getRuleInstanceCount /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result2 = getRuleInstanceCount(componentName);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getBackupPayload /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result12 = getBackupPayload(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result12);
                    return true;
                case TRANSACTION_applyRestore /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    applyRestore(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAppActiveNotifications /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAppActiveNotifications(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_cancelAllNotifications);
                        _result4.writeToParcel(reply, TRANSACTION_cancelAllNotifications);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_enqueueToastEx /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    enqueueToastEx(data.readString(), android.app.ITransientNotification.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException;

    void applyAdjustmentFromRankerService(INotificationListener iNotificationListener, Adjustment adjustment) throws RemoteException;

    void applyAdjustmentsFromRankerService(INotificationListener iNotificationListener, List<Adjustment> list) throws RemoteException;

    void applyRestore(byte[] bArr, int i) throws RemoteException;

    boolean areNotificationsEnabled(String str) throws RemoteException;

    boolean areNotificationsEnabledForPackage(String str, int i) throws RemoteException;

    void cancelAllNotifications(String str, int i) throws RemoteException;

    void cancelNotificationFromListener(INotificationListener iNotificationListener, String str, String str2, int i) throws RemoteException;

    void cancelNotificationWithTag(String str, String str2, int i, int i2) throws RemoteException;

    void cancelNotificationsFromListener(INotificationListener iNotificationListener, String[] strArr) throws RemoteException;

    void cancelToast(String str, ITransientNotification iTransientNotification) throws RemoteException;

    void enqueueNotificationWithTag(String str, String str2, String str3, int i, Notification notification, int[] iArr, int i2) throws RemoteException;

    void enqueueToast(String str, ITransientNotification iTransientNotification, int i) throws RemoteException;

    void enqueueToastEx(String str, ITransientNotification iTransientNotification, int i, String str2) throws RemoteException;

    StatusBarNotification[] getActiveNotifications(String str) throws RemoteException;

    ParceledListSlice getActiveNotificationsFromListener(INotificationListener iNotificationListener, String[] strArr, int i) throws RemoteException;

    ParceledListSlice getAppActiveNotifications(String str, int i) throws RemoteException;

    AutomaticZenRule getAutomaticZenRule(String str) throws RemoteException;

    byte[] getBackupPayload(int i) throws RemoteException;

    ComponentName getEffectsSuppressor() throws RemoteException;

    int getHintsFromListener(INotificationListener iNotificationListener) throws RemoteException;

    StatusBarNotification[] getHistoricalNotifications(String str, int i) throws RemoteException;

    int getImportance(String str, int i) throws RemoteException;

    int getInterruptionFilterFromListener(INotificationListener iNotificationListener) throws RemoteException;

    Policy getNotificationPolicy(String str) throws RemoteException;

    int getPackageImportance(String str) throws RemoteException;

    String[] getPackagesRequestingNotificationPolicyAccess() throws RemoteException;

    int getPriority(String str, int i) throws RemoteException;

    int getRuleInstanceCount(ComponentName componentName) throws RemoteException;

    int getVisibilityOverride(String str, int i) throws RemoteException;

    int getZenMode() throws RemoteException;

    ZenModeConfig getZenModeConfig() throws RemoteException;

    List<ZenRule> getZenRules() throws RemoteException;

    boolean isNotificationPolicyAccessGranted(String str) throws RemoteException;

    boolean isNotificationPolicyAccessGrantedForPackage(String str) throws RemoteException;

    boolean isSystemConditionProviderEnabled(String str) throws RemoteException;

    boolean matchesCallFilter(Bundle bundle) throws RemoteException;

    void notifyConditions(String str, IConditionProvider iConditionProvider, Condition[] conditionArr) throws RemoteException;

    void registerListener(INotificationListener iNotificationListener, ComponentName componentName, int i) throws RemoteException;

    boolean removeAutomaticZenRule(String str) throws RemoteException;

    boolean removeAutomaticZenRules(String str) throws RemoteException;

    void requestBindListener(ComponentName componentName) throws RemoteException;

    void requestHintsFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void requestInterruptionFilterFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void requestUnbindListener(INotificationListener iNotificationListener) throws RemoteException;

    void setImportance(String str, int i, int i2) throws RemoteException;

    void setInterruptionFilter(String str, int i) throws RemoteException;

    void setNotificationPolicy(String str, Policy policy) throws RemoteException;

    void setNotificationPolicyAccessGranted(String str, boolean z) throws RemoteException;

    void setNotificationsEnabledForPackage(String str, int i, boolean z) throws RemoteException;

    void setNotificationsShownFromListener(INotificationListener iNotificationListener, String[] strArr) throws RemoteException;

    void setOnNotificationPostedTrimFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void setPriority(String str, int i, int i2) throws RemoteException;

    void setVisibilityOverride(String str, int i, int i2) throws RemoteException;

    void setZenMode(int i, Uri uri, String str) throws RemoteException;

    void unregisterListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    boolean updateAutomaticZenRule(String str, AutomaticZenRule automaticZenRule) throws RemoteException;
}

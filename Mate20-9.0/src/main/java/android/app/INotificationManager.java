package android.app;

import android.app.ITransientNotification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import java.util.List;

public interface INotificationManager extends IInterface {

    public static abstract class Stub extends Binder implements INotificationManager {
        private static final String DESCRIPTOR = "android.app.INotificationManager";
        static final int TRANSACTION_addAutomaticZenRule = 91;
        static final int TRANSACTION_applyAdjustmentFromAssistant = 64;
        static final int TRANSACTION_applyAdjustmentsFromAssistant = 65;
        static final int TRANSACTION_applyEnqueuedAdjustmentFromAssistant = 63;
        static final int TRANSACTION_applyRestore = 97;
        static final int TRANSACTION_areChannelsBypassingDnd = 37;
        static final int TRANSACTION_areNotificationsEnabled = 13;
        static final int TRANSACTION_areNotificationsEnabledForPackage = 12;
        static final int TRANSACTION_canShowBadge = 9;
        static final int TRANSACTION_cancelAllNotifications = 1;
        static final int TRANSACTION_cancelNotificationFromListener = 42;
        static final int TRANSACTION_cancelNotificationWithTag = 7;
        static final int TRANSACTION_cancelNotificationsFromListener = 43;
        static final int TRANSACTION_cancelToast = 4;
        static final int TRANSACTION_clearData = 2;
        static final int TRANSACTION_createNotificationChannelGroups = 15;
        static final int TRANSACTION_createNotificationChannels = 16;
        static final int TRANSACTION_createNotificationChannelsForPackage = 17;
        static final int TRANSACTION_deleteNotificationChannel = 25;
        static final int TRANSACTION_deleteNotificationChannelGroup = 31;
        static final int TRANSACTION_doBindRecSys = 100;
        static final int TRANSACTION_enqueueNotificationWithTag = 6;
        static final int TRANSACTION_enqueueToast = 3;
        static final int TRANSACTION_enqueueToastEx = 99;
        static final int TRANSACTION_finishToken = 5;
        static final int TRANSACTION_getActiveNotifications = 38;
        static final int TRANSACTION_getActiveNotificationsFromListener = 51;
        static final int TRANSACTION_getAppActiveNotifications = 98;
        static final int TRANSACTION_getAutomaticZenRule = 89;
        static final int TRANSACTION_getBackupPayload = 96;
        static final int TRANSACTION_getBlockedAppCount = 36;
        static final int TRANSACTION_getBlockedChannelCount = 30;
        static final int TRANSACTION_getDeletedChannelCount = 29;
        static final int TRANSACTION_getEffectsSuppressor = 67;
        static final int TRANSACTION_getEnabledNotificationListenerPackages = 77;
        static final int TRANSACTION_getEnabledNotificationListeners = 78;
        static final int TRANSACTION_getHintsFromListener = 54;
        static final int TRANSACTION_getHistoricalNotifications = 39;
        static final int TRANSACTION_getInterruptionFilterFromListener = 56;
        static final int TRANSACTION_getNotificationChannel = 23;
        static final int TRANSACTION_getNotificationChannelForPackage = 24;
        static final int TRANSACTION_getNotificationChannelGroup = 32;
        static final int TRANSACTION_getNotificationChannelGroupForPackage = 19;
        static final int TRANSACTION_getNotificationChannelGroups = 33;
        static final int TRANSACTION_getNotificationChannelGroupsForPackage = 18;
        static final int TRANSACTION_getNotificationChannelGroupsFromPrivilegedListener = 62;
        static final int TRANSACTION_getNotificationChannels = 26;
        static final int TRANSACTION_getNotificationChannelsForPackage = 27;
        static final int TRANSACTION_getNotificationChannelsFromPrivilegedListener = 61;
        static final int TRANSACTION_getNotificationPolicy = 84;
        static final int TRANSACTION_getNumNotificationChannelsForPackage = 28;
        static final int TRANSACTION_getPackageImportance = 14;
        static final int TRANSACTION_getPopulatedNotificationChannelGroupForPackage = 20;
        static final int TRANSACTION_getRecentNotifyingAppsForUser = 35;
        static final int TRANSACTION_getRuleInstanceCount = 95;
        static final int TRANSACTION_getSnoozedNotificationsFromListener = 52;
        static final int TRANSACTION_getZenMode = 79;
        static final int TRANSACTION_getZenModeConfig = 80;
        static final int TRANSACTION_getZenRules = 90;
        static final int TRANSACTION_isNotificationAssistantAccessGranted = 72;
        static final int TRANSACTION_isNotificationListenerAccessGranted = 70;
        static final int TRANSACTION_isNotificationListenerAccessGrantedForUser = 71;
        static final int TRANSACTION_isNotificationPolicyAccessGranted = 83;
        static final int TRANSACTION_isNotificationPolicyAccessGrantedForPackage = 86;
        static final int TRANSACTION_isSystemConditionProviderEnabled = 69;
        static final int TRANSACTION_matchesCallFilter = 68;
        static final int TRANSACTION_notifyConditions = 82;
        static final int TRANSACTION_onlyHasDefaultChannel = 34;
        static final int TRANSACTION_registerListener = 40;
        static final int TRANSACTION_removeAutomaticZenRule = 93;
        static final int TRANSACTION_removeAutomaticZenRules = 94;
        static final int TRANSACTION_requestBindListener = 46;
        static final int TRANSACTION_requestBindProvider = 48;
        static final int TRANSACTION_requestHintsFromListener = 53;
        static final int TRANSACTION_requestInterruptionFilterFromListener = 55;
        static final int TRANSACTION_requestUnbindListener = 47;
        static final int TRANSACTION_requestUnbindProvider = 49;
        static final int TRANSACTION_setInterruptionFilter = 58;
        static final int TRANSACTION_setNotificationAssistantAccessGranted = 74;
        static final int TRANSACTION_setNotificationAssistantAccessGrantedForUser = 76;
        static final int TRANSACTION_setNotificationListenerAccessGranted = 73;
        static final int TRANSACTION_setNotificationListenerAccessGrantedForUser = 75;
        static final int TRANSACTION_setNotificationPolicy = 85;
        static final int TRANSACTION_setNotificationPolicyAccessGranted = 87;
        static final int TRANSACTION_setNotificationPolicyAccessGrantedForUser = 88;
        static final int TRANSACTION_setNotificationsEnabledForPackage = 10;
        static final int TRANSACTION_setNotificationsEnabledWithImportanceLockForPackage = 11;
        static final int TRANSACTION_setNotificationsShownFromListener = 50;
        static final int TRANSACTION_setOnNotificationPostedTrimFromListener = 57;
        static final int TRANSACTION_setShowBadge = 8;
        static final int TRANSACTION_setZenMode = 81;
        static final int TRANSACTION_snoozeNotificationUntilContextFromListener = 44;
        static final int TRANSACTION_snoozeNotificationUntilFromListener = 45;
        static final int TRANSACTION_unregisterListener = 41;
        static final int TRANSACTION_unsnoozeNotificationFromAssistant = 66;
        static final int TRANSACTION_updateAutomaticZenRule = 92;
        static final int TRANSACTION_updateNotificationChannelForPackage = 22;
        static final int TRANSACTION_updateNotificationChannelFromPrivilegedListener = 60;
        static final int TRANSACTION_updateNotificationChannelGroupForPackage = 21;
        static final int TRANSACTION_updateNotificationChannelGroupFromPrivilegedListener = 59;

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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearData(String pkg, int uid, boolean fromApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(fromApp);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enqueueToast(String pkg, ITransientNotification callback, int duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(duration);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelToast(String pkg, ITransientNotification callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishToken(String pkg, ITransientNotification callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(opPkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    if (notification != null) {
                        _data.writeInt(1);
                        notification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setShowBadge(String pkg, int uid, boolean showBadge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(showBadge);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canShowBadge(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(enabled);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationsEnabledWithImportanceLockForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(enabled);
                    this.mRemote.transact(11, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
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
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createNotificationChannelGroups(String pkg, ParceledListSlice channelGroupList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (channelGroupList != null) {
                        _data.writeInt(1);
                        channelGroupList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createNotificationChannels(String pkg, ParceledListSlice channelsList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (channelsList != null) {
                        _data.writeInt(1);
                        channelsList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createNotificationChannelsForPackage(String pkg, int uid, ParceledListSlice channelsList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (channelsList != null) {
                        _data.writeInt(1);
                        channelsList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(includeDeleted);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) throws RemoteException {
                NotificationChannelGroup _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannelGroup.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String pkg, int uid, String groupId, boolean includeDeleted) throws RemoteException {
                NotificationChannelGroup _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeString(groupId);
                    _data.writeInt(includeDeleted);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannelGroup.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateNotificationChannelGroupForPackage(String pkg, int uid, NotificationChannelGroup group) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (group != null) {
                        _data.writeInt(1);
                        group.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (channel != null) {
                        _data.writeInt(1);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NotificationChannel getNotificationChannel(String pkg, String channelId) throws RemoteException {
                NotificationChannel _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannel.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) throws RemoteException {
                NotificationChannel _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeString(channelId);
                    _data.writeInt(includeDeleted);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannel.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteNotificationChannel(String pkg, String channelId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getNotificationChannels(String pkg) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(includeDeleted);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(includeDeleted);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDeletedChannelCount(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBlockedChannelCount(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteNotificationChannelGroup(String pkg, String channelGroupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelGroupId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NotificationChannelGroup getNotificationChannelGroup(String pkg, String channelGroupId) throws RemoteException {
                NotificationChannelGroup _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelGroupId);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannelGroup.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getNotificationChannelGroups(String pkg) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean onlyHasDefaultChannel(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getRecentNotifyingAppsForUser(int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBlockedAppCount(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean areChannelsBypassingDnd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
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
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    return (StatusBarNotification[]) _reply.createTypedArray(StatusBarNotification.CREATOR);
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
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return (StatusBarNotification[]) _reply.createTypedArray(StatusBarNotification.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerListener(INotificationListener listener, ComponentName component, int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userid);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(INotificationListener listener, int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(userid);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelNotificationsFromListener(INotificationListener token, String[] keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeStringArray(keys);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(key);
                    _data.writeString(snoozeCriterionId);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void snoozeNotificationUntilFromListener(INotificationListener token, String key, long until) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(key);
                    _data.writeLong(until);
                    this.mRemote.transact(45, _data, _reply, 0);
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
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestUnbindListener(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestBindProvider(ComponentName component) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestUnbindProvider(IConditionProvider token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationsShownFromListener(INotificationListener token, String[] keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeStringArray(keys);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    ParceledListSlice _result = null;
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeStringArray(keys);
                    _data.writeInt(trim);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getSnoozedNotificationsFromListener(INotificationListener token, int trim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    ParceledListSlice _result = null;
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(trim);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestHintsFromListener(INotificationListener token, int hints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(hints);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHintsFromListener(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(interruptionFilter);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(trim);
                    this.mRemote.transact(57, _data, _reply, 0);
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
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateNotificationChannelGroupFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannelGroup group) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(pkg);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (group != null) {
                        _data.writeInt(1);
                        group.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateNotificationChannelFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannel channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(pkg);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (channel != null) {
                        _data.writeInt(1);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    ParceledListSlice _result = null;
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(pkg);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    ParceledListSlice _result = null;
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(pkg);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (adjustment != null) {
                        _data.writeInt(1);
                        adjustment.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (adjustment != null) {
                        _data.writeInt(1);
                        adjustment.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeTypedList(adjustments);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unsnoozeNotificationFromAssistant(INotificationListener token, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(key);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getEffectsSuppressor() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean matchesCallFilter(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationListenerAccessGranted(ComponentName listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (listener != null) {
                        _data.writeInt(1);
                        listener.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationListenerAccessGrantedForUser(ComponentName listener, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (listener != null) {
                        _data.writeInt(1);
                        listener.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationAssistantAccessGranted(ComponentName assistant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (assistant != null) {
                        _data.writeInt(1);
                        assistant.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationListenerAccessGranted(ComponentName listener, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        _data.writeInt(1);
                        listener.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (assistant != null) {
                        _data.writeInt(1);
                        assistant.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        _data.writeInt(1);
                        listener.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeInt(enabled);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (assistant != null) {
                        _data.writeInt(1);
                        assistant.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeInt(enabled);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getEnabledNotificationListenerPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ComponentName> getEnabledNotificationListeners(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ComponentName.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getZenMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ZenModeConfig getZenModeConfig() throws RemoteException {
                ZenModeConfig _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ZenModeConfig) ZenModeConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                        _data.writeInt(1);
                        conditionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(reason);
                    this.mRemote.transact(81, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyConditions(String pkg, IConditionProvider provider, Condition[] conditions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(provider != null ? provider.asBinder() : null);
                    _data.writeTypedArray(conditions, 0);
                    this.mRemote.transact(82, _data, null, 1);
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
                    boolean _result = false;
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NotificationManager.Policy getNotificationPolicy(String pkg) throws RemoteException {
                NotificationManager.Policy _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationManager.Policy.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationPolicy(String pkg, NotificationManager.Policy policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (policy != null) {
                        _data.writeInt(1);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(85, _data, _reply, 0);
                    _reply.readException();
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
                    boolean _result = false;
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(granted);
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationPolicyAccessGrantedForUser(String pkg, int userId, boolean granted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    _data.writeInt(granted);
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
                AutomaticZenRule _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AutomaticZenRule.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ZenModeConfig.ZenRule> getZenRules() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ZenModeConfig.ZenRule.CREATOR);
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
                        _data.writeInt(1);
                        automaticZenRule.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    boolean _result = true;
                    if (automaticZenRule != null) {
                        _data.writeInt(1);
                        automaticZenRule.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                        _data.writeInt(1);
                        owner.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
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
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAppActiveNotifications(String callingPkg, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enqueueToastEx(String pkg, ITransientNotification callback, int duration, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(duration);
                    _data.writeInt(displayId);
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void doBindRecSys() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(100, _data, _reply, 0);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: android.app.Notification} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v20, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v23, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v27, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v28, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v31, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v35, resolved type: android.app.NotificationChannelGroup} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: android.app.NotificationChannelGroup} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v39, resolved type: android.app.NotificationChannel} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v42, resolved type: android.app.NotificationChannel} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v60, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v63, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v74, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v77, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v80, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v83, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v103, resolved type: android.app.NotificationChannelGroup} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v106, resolved type: android.app.NotificationChannelGroup} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v107, resolved type: android.app.NotificationChannel} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v110, resolved type: android.app.NotificationChannel} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v111, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v114, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v115, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v119, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v123, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v132, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v137, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v141, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v145, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v149, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v153, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v157, resolved type: android.service.notification.Adjustment} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v161, resolved type: android.service.notification.Adjustment} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            UserHandle _arg2;
            UserHandle _arg22;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                Adjustment _arg1 = null;
                boolean _arg23 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelAllNotifications(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        clearData(_arg0, _arg12, _arg23);
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        enqueueToast(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelToast(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        finishToken(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        String _arg13 = data.readString();
                        String _arg24 = data.readString();
                        int _arg3 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Notification) Notification.CREATOR.createFromParcel(parcel);
                        }
                        enqueueNotificationWithTag(_arg02, _arg13, _arg24, _arg3, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelNotificationWithTag(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setShowBadge(_arg03, _arg14, _arg23);
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = canShowBadge(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationsEnabledForPackage(_arg04, _arg15, _arg23);
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationsEnabledWithImportanceLockForPackage(_arg05, _arg16, _arg23);
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = areNotificationsEnabledForPackage(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = areNotificationsEnabled(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = getPackageImportance(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(parcel);
                        }
                        createNotificationChannelGroups(_arg06, _arg1);
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(parcel);
                        }
                        createNotificationChannels(_arg07, _arg1);
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(parcel);
                        }
                        createNotificationChannelsForPackage(_arg08, _arg17, _arg1);
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result5 = getNotificationChannelGroupsForPackage(data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        NotificationChannelGroup _result6 = getNotificationChannelGroupForPackage(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            parcel2.writeInt(1);
                            _result6.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        NotificationChannelGroup _result7 = getPopulatedNotificationChannelGroupForPackage(data.readString(), data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result7 != null) {
                            parcel2.writeInt(1);
                            _result7.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        int _arg18 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (NotificationChannelGroup) NotificationChannelGroup.CREATOR.createFromParcel(parcel);
                        }
                        updateNotificationChannelGroupForPackage(_arg09, _arg18, _arg1);
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (NotificationChannel) NotificationChannel.CREATOR.createFromParcel(parcel);
                        }
                        updateNotificationChannelForPackage(_arg010, _arg19, _arg1);
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        NotificationChannel _result8 = getNotificationChannel(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result8 != null) {
                            parcel2.writeInt(1);
                            _result8.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        NotificationChannel _result9 = getNotificationChannelForPackage(data.readString(), data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result9 != null) {
                            parcel2.writeInt(1);
                            _result9.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        deleteNotificationChannel(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result10 = getNotificationChannels(data.readString());
                        reply.writeNoException();
                        if (_result10 != null) {
                            parcel2.writeInt(1);
                            _result10.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result11 = getNotificationChannelsForPackage(data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result11 != null) {
                            parcel2.writeInt(1);
                            _result11.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        int _result12 = getNumNotificationChannelsForPackage(_arg011, _arg110, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result13 = getDeletedChannelCount(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result14 = getBlockedChannelCount(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        deleteNotificationChannelGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        NotificationChannelGroup _result15 = getNotificationChannelGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result15 != null) {
                            parcel2.writeInt(1);
                            _result15.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result16 = getNotificationChannelGroups(data.readString());
                        reply.writeNoException();
                        if (_result16 != null) {
                            parcel2.writeInt(1);
                            _result16.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result17 = onlyHasDefaultChannel(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result18 = getRecentNotifyingAppsForUser(data.readInt());
                        reply.writeNoException();
                        if (_result18 != null) {
                            parcel2.writeInt(1);
                            _result18.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result19 = getBlockedAppCount(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result20 = areChannelsBypassingDnd();
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        StatusBarNotification[] _result21 = getActiveNotifications(data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result21, 1);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        StatusBarNotification[] _result22 = getHistoricalNotifications(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result22, 1);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg012 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        registerListener(_arg012, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelNotificationFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelNotificationsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        snoozeNotificationUntilContextFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        snoozeNotificationUntilFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        requestBindListener(_arg1);
                        reply.writeNoException();
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestUnbindListener(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        requestBindProvider(_arg1);
                        reply.writeNoException();
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestUnbindProvider(IConditionProvider.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        setNotificationsShownFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result23 = getActiveNotificationsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        if (_result23 != null) {
                            parcel2.writeInt(1);
                            _result23.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result24 = getSnoozedNotificationsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        if (_result24 != null) {
                            parcel2.writeInt(1);
                            _result24.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestHintsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result25 = getHintsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestInterruptionFilterFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result26 = getInterruptionFilterFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        setOnNotificationPostedTrimFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        setInterruptionFilter(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg013 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (NotificationChannelGroup) NotificationChannelGroup.CREATOR.createFromParcel(parcel);
                        }
                        updateNotificationChannelGroupFromPrivilegedListener(_arg013, _arg111, _arg2, _arg1);
                        reply.writeNoException();
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg014 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (NotificationChannel) NotificationChannel.CREATOR.createFromParcel(parcel);
                        }
                        updateNotificationChannelFromPrivilegedListener(_arg014, _arg112, _arg22, _arg1);
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg015 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result27 = getNotificationChannelsFromPrivilegedListener(_arg015, _arg113, _arg1);
                        reply.writeNoException();
                        if (_result27 != null) {
                            parcel2.writeInt(1);
                            _result27.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg016 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result28 = getNotificationChannelGroupsFromPrivilegedListener(_arg016, _arg114, _arg1);
                        reply.writeNoException();
                        if (_result28 != null) {
                            parcel2.writeInt(1);
                            _result28.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg017 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (Adjustment) Adjustment.CREATOR.createFromParcel(parcel);
                        }
                        applyEnqueuedAdjustmentFromAssistant(_arg017, _arg1);
                        reply.writeNoException();
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg018 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (Adjustment) Adjustment.CREATOR.createFromParcel(parcel);
                        }
                        applyAdjustmentFromAssistant(_arg018, _arg1);
                        reply.writeNoException();
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        applyAdjustmentsFromAssistant(INotificationListener.Stub.asInterface(data.readStrongBinder()), parcel.createTypedArrayList(Adjustment.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        unsnoozeNotificationFromAssistant(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result29 = getEffectsSuppressor();
                        reply.writeNoException();
                        if (_result29 != null) {
                            parcel2.writeInt(1);
                            _result29.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result30 = matchesCallFilter(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result31 = isSystemConditionProviderEnabled(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result32 = isNotificationListenerAccessGranted(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result33 = isNotificationListenerAccessGrantedForUser(_arg1, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case 72:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result34 = isNotificationAssistantAccessGranted(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationListenerAccessGranted(_arg1, _arg23);
                        reply.writeNoException();
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationAssistantAccessGranted(_arg1, _arg23);
                        reply.writeNoException();
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationListenerAccessGrantedForUser(_arg1, _arg115, _arg23);
                        reply.writeNoException();
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg116 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationAssistantAccessGrantedForUser(_arg1, _arg116, _arg23);
                        reply.writeNoException();
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result35 = getEnabledNotificationListenerPackages();
                        reply.writeNoException();
                        parcel2.writeStringList(_result35);
                        return true;
                    case 78:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ComponentName> _result36 = getEnabledNotificationListeners(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result36);
                        return true;
                    case 79:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result37 = getZenMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result37);
                        return true;
                    case 80:
                        parcel.enforceInterface(DESCRIPTOR);
                        ZenModeConfig _result38 = getZenModeConfig();
                        reply.writeNoException();
                        if (_result38 != null) {
                            parcel2.writeInt(1);
                            _result38.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 81:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg019 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        setZenMode(_arg019, _arg1, data.readString());
                        return true;
                    case 82:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyConditions(data.readString(), IConditionProvider.Stub.asInterface(data.readStrongBinder()), (Condition[]) parcel.createTypedArray(Condition.CREATOR));
                        return true;
                    case 83:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result39 = isNotificationPolicyAccessGranted(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case 84:
                        parcel.enforceInterface(DESCRIPTOR);
                        NotificationManager.Policy _result40 = getNotificationPolicy(data.readString());
                        reply.writeNoException();
                        if (_result40 != null) {
                            parcel2.writeInt(1);
                            _result40.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 85:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (NotificationManager.Policy) NotificationManager.Policy.CREATOR.createFromParcel(parcel);
                        }
                        setNotificationPolicy(_arg020, _arg1);
                        reply.writeNoException();
                        return true;
                    case 86:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result41 = isNotificationPolicyAccessGrantedForPackage(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result41);
                        return true;
                    case 87:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg021 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationPolicyAccessGranted(_arg021, _arg23);
                        reply.writeNoException();
                        return true;
                    case 88:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        int _arg117 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setNotificationPolicyAccessGrantedForUser(_arg022, _arg117, _arg23);
                        reply.writeNoException();
                        return true;
                    case 89:
                        parcel.enforceInterface(DESCRIPTOR);
                        AutomaticZenRule _result42 = getAutomaticZenRule(data.readString());
                        reply.writeNoException();
                        if (_result42 != null) {
                            parcel2.writeInt(1);
                            _result42.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 90:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ZenModeConfig.ZenRule> _result43 = getZenRules();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result43);
                        return true;
                    case 91:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (AutomaticZenRule) AutomaticZenRule.CREATOR.createFromParcel(parcel);
                        }
                        String _result44 = addAutomaticZenRule(_arg1);
                        reply.writeNoException();
                        parcel2.writeString(_result44);
                        return true;
                    case 92:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg023 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (AutomaticZenRule) AutomaticZenRule.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result45 = updateAutomaticZenRule(_arg023, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result45);
                        return true;
                    case 93:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result46 = removeAutomaticZenRule(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 94:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result47 = removeAutomaticZenRules(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result47);
                        return true;
                    case 95:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result48 = getRuleInstanceCount(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result48);
                        return true;
                    case 96:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result49 = getBackupPayload(data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result49);
                        return true;
                    case 97:
                        parcel.enforceInterface(DESCRIPTOR);
                        applyRestore(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 98:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result50 = getAppActiveNotifications(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result50 != null) {
                            parcel2.writeInt(1);
                            _result50.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 99:
                        parcel.enforceInterface(DESCRIPTOR);
                        enqueueToastEx(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 100:
                        parcel.enforceInterface(DESCRIPTOR);
                        doBindRecSys();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException;

    void applyAdjustmentFromAssistant(INotificationListener iNotificationListener, Adjustment adjustment) throws RemoteException;

    void applyAdjustmentsFromAssistant(INotificationListener iNotificationListener, List<Adjustment> list) throws RemoteException;

    void applyEnqueuedAdjustmentFromAssistant(INotificationListener iNotificationListener, Adjustment adjustment) throws RemoteException;

    void applyRestore(byte[] bArr, int i) throws RemoteException;

    boolean areChannelsBypassingDnd() throws RemoteException;

    boolean areNotificationsEnabled(String str) throws RemoteException;

    boolean areNotificationsEnabledForPackage(String str, int i) throws RemoteException;

    boolean canShowBadge(String str, int i) throws RemoteException;

    void cancelAllNotifications(String str, int i) throws RemoteException;

    void cancelNotificationFromListener(INotificationListener iNotificationListener, String str, String str2, int i) throws RemoteException;

    void cancelNotificationWithTag(String str, String str2, int i, int i2) throws RemoteException;

    void cancelNotificationsFromListener(INotificationListener iNotificationListener, String[] strArr) throws RemoteException;

    void cancelToast(String str, ITransientNotification iTransientNotification) throws RemoteException;

    void clearData(String str, int i, boolean z) throws RemoteException;

    void createNotificationChannelGroups(String str, ParceledListSlice parceledListSlice) throws RemoteException;

    void createNotificationChannels(String str, ParceledListSlice parceledListSlice) throws RemoteException;

    void createNotificationChannelsForPackage(String str, int i, ParceledListSlice parceledListSlice) throws RemoteException;

    void deleteNotificationChannel(String str, String str2) throws RemoteException;

    void deleteNotificationChannelGroup(String str, String str2) throws RemoteException;

    void doBindRecSys() throws RemoteException;

    void enqueueNotificationWithTag(String str, String str2, String str3, int i, Notification notification, int i2) throws RemoteException;

    void enqueueToast(String str, ITransientNotification iTransientNotification, int i) throws RemoteException;

    void enqueueToastEx(String str, ITransientNotification iTransientNotification, int i, int i2) throws RemoteException;

    void finishToken(String str, ITransientNotification iTransientNotification) throws RemoteException;

    StatusBarNotification[] getActiveNotifications(String str) throws RemoteException;

    ParceledListSlice getActiveNotificationsFromListener(INotificationListener iNotificationListener, String[] strArr, int i) throws RemoteException;

    ParceledListSlice getAppActiveNotifications(String str, int i) throws RemoteException;

    AutomaticZenRule getAutomaticZenRule(String str) throws RemoteException;

    byte[] getBackupPayload(int i) throws RemoteException;

    int getBlockedAppCount(int i) throws RemoteException;

    int getBlockedChannelCount(String str, int i) throws RemoteException;

    int getDeletedChannelCount(String str, int i) throws RemoteException;

    ComponentName getEffectsSuppressor() throws RemoteException;

    List<String> getEnabledNotificationListenerPackages() throws RemoteException;

    List<ComponentName> getEnabledNotificationListeners(int i) throws RemoteException;

    int getHintsFromListener(INotificationListener iNotificationListener) throws RemoteException;

    StatusBarNotification[] getHistoricalNotifications(String str, int i) throws RemoteException;

    int getInterruptionFilterFromListener(INotificationListener iNotificationListener) throws RemoteException;

    NotificationChannel getNotificationChannel(String str, String str2) throws RemoteException;

    NotificationChannel getNotificationChannelForPackage(String str, int i, String str2, boolean z) throws RemoteException;

    NotificationChannelGroup getNotificationChannelGroup(String str, String str2) throws RemoteException;

    NotificationChannelGroup getNotificationChannelGroupForPackage(String str, String str2, int i) throws RemoteException;

    ParceledListSlice getNotificationChannelGroups(String str) throws RemoteException;

    ParceledListSlice getNotificationChannelGroupsForPackage(String str, int i, boolean z) throws RemoteException;

    ParceledListSlice getNotificationChannelGroupsFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle) throws RemoteException;

    ParceledListSlice getNotificationChannels(String str) throws RemoteException;

    ParceledListSlice getNotificationChannelsForPackage(String str, int i, boolean z) throws RemoteException;

    ParceledListSlice getNotificationChannelsFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle) throws RemoteException;

    NotificationManager.Policy getNotificationPolicy(String str) throws RemoteException;

    int getNumNotificationChannelsForPackage(String str, int i, boolean z) throws RemoteException;

    int getPackageImportance(String str) throws RemoteException;

    NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String str, int i, String str2, boolean z) throws RemoteException;

    ParceledListSlice getRecentNotifyingAppsForUser(int i) throws RemoteException;

    int getRuleInstanceCount(ComponentName componentName) throws RemoteException;

    ParceledListSlice getSnoozedNotificationsFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    int getZenMode() throws RemoteException;

    ZenModeConfig getZenModeConfig() throws RemoteException;

    List<ZenModeConfig.ZenRule> getZenRules() throws RemoteException;

    boolean isNotificationAssistantAccessGranted(ComponentName componentName) throws RemoteException;

    boolean isNotificationListenerAccessGranted(ComponentName componentName) throws RemoteException;

    boolean isNotificationListenerAccessGrantedForUser(ComponentName componentName, int i) throws RemoteException;

    boolean isNotificationPolicyAccessGranted(String str) throws RemoteException;

    boolean isNotificationPolicyAccessGrantedForPackage(String str) throws RemoteException;

    boolean isSystemConditionProviderEnabled(String str) throws RemoteException;

    boolean matchesCallFilter(Bundle bundle) throws RemoteException;

    void notifyConditions(String str, IConditionProvider iConditionProvider, Condition[] conditionArr) throws RemoteException;

    boolean onlyHasDefaultChannel(String str, int i) throws RemoteException;

    void registerListener(INotificationListener iNotificationListener, ComponentName componentName, int i) throws RemoteException;

    boolean removeAutomaticZenRule(String str) throws RemoteException;

    boolean removeAutomaticZenRules(String str) throws RemoteException;

    void requestBindListener(ComponentName componentName) throws RemoteException;

    void requestBindProvider(ComponentName componentName) throws RemoteException;

    void requestHintsFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void requestInterruptionFilterFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void requestUnbindListener(INotificationListener iNotificationListener) throws RemoteException;

    void requestUnbindProvider(IConditionProvider iConditionProvider) throws RemoteException;

    void setInterruptionFilter(String str, int i) throws RemoteException;

    void setNotificationAssistantAccessGranted(ComponentName componentName, boolean z) throws RemoteException;

    void setNotificationAssistantAccessGrantedForUser(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setNotificationListenerAccessGranted(ComponentName componentName, boolean z) throws RemoteException;

    void setNotificationListenerAccessGrantedForUser(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setNotificationPolicy(String str, NotificationManager.Policy policy) throws RemoteException;

    void setNotificationPolicyAccessGranted(String str, boolean z) throws RemoteException;

    void setNotificationPolicyAccessGrantedForUser(String str, int i, boolean z) throws RemoteException;

    void setNotificationsEnabledForPackage(String str, int i, boolean z) throws RemoteException;

    void setNotificationsEnabledWithImportanceLockForPackage(String str, int i, boolean z) throws RemoteException;

    void setNotificationsShownFromListener(INotificationListener iNotificationListener, String[] strArr) throws RemoteException;

    void setOnNotificationPostedTrimFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void setShowBadge(String str, int i, boolean z) throws RemoteException;

    void setZenMode(int i, Uri uri, String str) throws RemoteException;

    void snoozeNotificationUntilContextFromListener(INotificationListener iNotificationListener, String str, String str2) throws RemoteException;

    void snoozeNotificationUntilFromListener(INotificationListener iNotificationListener, String str, long j) throws RemoteException;

    void unregisterListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void unsnoozeNotificationFromAssistant(INotificationListener iNotificationListener, String str) throws RemoteException;

    boolean updateAutomaticZenRule(String str, AutomaticZenRule automaticZenRule) throws RemoteException;

    void updateNotificationChannelForPackage(String str, int i, NotificationChannel notificationChannel) throws RemoteException;

    void updateNotificationChannelFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle, NotificationChannel notificationChannel) throws RemoteException;

    void updateNotificationChannelGroupForPackage(String str, int i, NotificationChannelGroup notificationChannelGroup) throws RemoteException;

    void updateNotificationChannelGroupFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle, NotificationChannelGroup notificationChannelGroup) throws RemoteException;
}

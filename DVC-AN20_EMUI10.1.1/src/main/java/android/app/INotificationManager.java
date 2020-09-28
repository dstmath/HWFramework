package android.app;

import android.annotation.UnsupportedAppUsage;
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
    String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException;

    void allowAssistantAdjustment(String str) throws RemoteException;

    void applyAdjustmentFromAssistant(INotificationListener iNotificationListener, Adjustment adjustment) throws RemoteException;

    void applyAdjustmentsFromAssistant(INotificationListener iNotificationListener, List<Adjustment> list) throws RemoteException;

    void applyEnqueuedAdjustmentFromAssistant(INotificationListener iNotificationListener, Adjustment adjustment) throws RemoteException;

    void applyRestore(byte[] bArr, int i) throws RemoteException;

    boolean areBubblesAllowed(String str) throws RemoteException;

    boolean areBubblesAllowedForPackage(String str, int i) throws RemoteException;

    boolean areChannelsBypassingDnd() throws RemoteException;

    boolean areNotificationsEnabled(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean areNotificationsEnabledForPackage(String str, int i) throws RemoteException;

    boolean canNotifyAsPackage(String str, String str2, int i) throws RemoteException;

    boolean canShowBadge(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    void cancelAllNotifications(String str, int i) throws RemoteException;

    void cancelNotificationFromListener(INotificationListener iNotificationListener, String str, String str2, int i) throws RemoteException;

    @UnsupportedAppUsage
    void cancelNotificationWithTag(String str, String str2, int i, int i2) throws RemoteException;

    void cancelNotificationsFromListener(INotificationListener iNotificationListener, String[] strArr) throws RemoteException;

    @UnsupportedAppUsage
    void cancelToast(String str, ITransientNotification iTransientNotification) throws RemoteException;

    void clearData(String str, int i, boolean z) throws RemoteException;

    void clearRequestedListenerHints(INotificationListener iNotificationListener) throws RemoteException;

    void createNotificationChannelGroups(String str, ParceledListSlice parceledListSlice) throws RemoteException;

    void createNotificationChannels(String str, ParceledListSlice parceledListSlice) throws RemoteException;

    void createNotificationChannelsForPackage(String str, int i, ParceledListSlice parceledListSlice) throws RemoteException;

    void deleteNotificationChannel(String str, String str2) throws RemoteException;

    void deleteNotificationChannelGroup(String str, String str2) throws RemoteException;

    void disallowAssistantAdjustment(String str) throws RemoteException;

    void doBindRecSys() throws RemoteException;

    void enqueueNotificationWithTag(String str, String str2, String str3, int i, Notification notification, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void enqueueToast(String str, ITransientNotification iTransientNotification, int i, int i2) throws RemoteException;

    void finishToken(String str, ITransientNotification iTransientNotification) throws RemoteException;

    @UnsupportedAppUsage
    StatusBarNotification[] getActiveNotifications(String str) throws RemoteException;

    ParceledListSlice getActiveNotificationsFromListener(INotificationListener iNotificationListener, String[] strArr, int i) throws RemoteException;

    List<String> getAllowedAssistantAdjustments(String str) throws RemoteException;

    ComponentName getAllowedNotificationAssistant() throws RemoteException;

    ComponentName getAllowedNotificationAssistantForUser(int i) throws RemoteException;

    ParceledListSlice getAppActiveNotifications(String str, int i) throws RemoteException;

    int getAppsBypassingDndCount(int i) throws RemoteException;

    AutomaticZenRule getAutomaticZenRule(String str) throws RemoteException;

    byte[] getBackupPayload(int i) throws RemoteException;

    int getBlockedAppCount(int i) throws RemoteException;

    int getBlockedChannelCount(String str, int i) throws RemoteException;

    NotificationManager.Policy getConsolidatedNotificationPolicy() throws RemoteException;

    int getDeletedChannelCount(String str, int i) throws RemoteException;

    ComponentName getEffectsSuppressor() throws RemoteException;

    List<String> getEnabledNotificationListenerPackages() throws RemoteException;

    List<ComponentName> getEnabledNotificationListeners(int i) throws RemoteException;

    int getHintsFromListener(INotificationListener iNotificationListener) throws RemoteException;

    @UnsupportedAppUsage
    StatusBarNotification[] getHistoricalNotifications(String str, int i) throws RemoteException;

    int getInterruptionFilterFromListener(INotificationListener iNotificationListener) throws RemoteException;

    NotificationChannel getNotificationChannel(String str, int i, String str2, String str3) throws RemoteException;

    NotificationChannel getNotificationChannelForPackage(String str, int i, String str2, boolean z) throws RemoteException;

    NotificationChannelGroup getNotificationChannelGroup(String str, String str2) throws RemoteException;

    NotificationChannelGroup getNotificationChannelGroupForPackage(String str, String str2, int i) throws RemoteException;

    ParceledListSlice getNotificationChannelGroups(String str) throws RemoteException;

    ParceledListSlice getNotificationChannelGroupsForPackage(String str, int i, boolean z) throws RemoteException;

    ParceledListSlice getNotificationChannelGroupsFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle) throws RemoteException;

    ParceledListSlice getNotificationChannels(String str, String str2, int i) throws RemoteException;

    ParceledListSlice getNotificationChannelsBypassingDnd(String str, int i) throws RemoteException;

    ParceledListSlice getNotificationChannelsForPackage(String str, int i, boolean z) throws RemoteException;

    ParceledListSlice getNotificationChannelsFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle) throws RemoteException;

    String getNotificationDelegate(String str) throws RemoteException;

    NotificationManager.Policy getNotificationPolicy(String str) throws RemoteException;

    int getNumNotificationChannelsForPackage(String str, int i, boolean z) throws RemoteException;

    int getPackageImportance(String str) throws RemoteException;

    NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String str, int i, String str2, boolean z) throws RemoteException;

    boolean getPrivateNotificationsAllowed() throws RemoteException;

    int getRuleInstanceCount(ComponentName componentName) throws RemoteException;

    ParceledListSlice getSnoozedNotificationsFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    @UnsupportedAppUsage
    int getZenMode() throws RemoteException;

    @UnsupportedAppUsage
    ZenModeConfig getZenModeConfig() throws RemoteException;

    List<ZenModeConfig.ZenRule> getZenRules() throws RemoteException;

    boolean hasUserApprovedBubblesForPackage(String str, int i) throws RemoteException;

    boolean isNotificationAssistantAccessGranted(ComponentName componentName) throws RemoteException;

    boolean isNotificationListenerAccessGranted(ComponentName componentName) throws RemoteException;

    boolean isNotificationListenerAccessGrantedForUser(ComponentName componentName, int i) throws RemoteException;

    boolean isNotificationPolicyAccessGranted(String str) throws RemoteException;

    boolean isNotificationPolicyAccessGrantedForPackage(String str) throws RemoteException;

    boolean isPackagePaused(String str) throws RemoteException;

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

    void setAutomaticZenRuleState(String str, Condition condition) throws RemoteException;

    void setBubblesAllowed(String str, int i, boolean z) throws RemoteException;

    void setHideSilentStatusIcons(boolean z) throws RemoteException;

    void setInterruptionFilter(String str, int i) throws RemoteException;

    void setNotificationAssistantAccessGranted(ComponentName componentName, boolean z) throws RemoteException;

    void setNotificationAssistantAccessGrantedForUser(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setNotificationDelegate(String str, String str2) throws RemoteException;

    void setNotificationListenerAccessGranted(ComponentName componentName, boolean z) throws RemoteException;

    void setNotificationListenerAccessGrantedForUser(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setNotificationPolicy(String str, NotificationManager.Policy policy) throws RemoteException;

    void setNotificationPolicyAccessGranted(String str, boolean z) throws RemoteException;

    void setNotificationPolicyAccessGrantedForUser(String str, int i, boolean z) throws RemoteException;

    void setNotificationsEnabledForPackage(String str, int i, boolean z) throws RemoteException;

    void setNotificationsEnabledWithImportanceLockForPackage(String str, int i, boolean z) throws RemoteException;

    void setNotificationsShownFromListener(INotificationListener iNotificationListener, String[] strArr) throws RemoteException;

    void setOnNotificationPostedTrimFromListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void setPrivateNotificationsAllowed(boolean z) throws RemoteException;

    void setShowBadge(String str, int i, boolean z) throws RemoteException;

    void setZenMode(int i, Uri uri, String str) throws RemoteException;

    boolean shouldHideSilentStatusIcons(String str) throws RemoteException;

    void snoozeNotificationUntilContextFromListener(INotificationListener iNotificationListener, String str, String str2) throws RemoteException;

    void snoozeNotificationUntilFromListener(INotificationListener iNotificationListener, String str, long j) throws RemoteException;

    void unregisterListener(INotificationListener iNotificationListener, int i) throws RemoteException;

    void unsnoozeNotificationFromAssistant(INotificationListener iNotificationListener, String str) throws RemoteException;

    boolean updateAutomaticZenRule(String str, AutomaticZenRule automaticZenRule) throws RemoteException;

    void updateNotificationChannelForPackage(String str, int i, NotificationChannel notificationChannel) throws RemoteException;

    void updateNotificationChannelFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle, NotificationChannel notificationChannel) throws RemoteException;

    void updateNotificationChannelGroupForPackage(String str, int i, NotificationChannelGroup notificationChannelGroup) throws RemoteException;

    void updateNotificationChannelGroupFromPrivilegedListener(INotificationListener iNotificationListener, String str, UserHandle userHandle, NotificationChannelGroup notificationChannelGroup) throws RemoteException;

    public static class Default implements INotificationManager {
        @Override // android.app.INotificationManager
        public void cancelAllNotifications(String pkg, int userId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void clearData(String pkg, int uid, boolean fromApp) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void enqueueToast(String pkg, ITransientNotification callback, int duration, int displayId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void cancelToast(String pkg, ITransientNotification callback) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void finishToken(String pkg, ITransientNotification callback) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setShowBadge(String pkg, int uid, boolean showBadge) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean canShowBadge(String pkg, int uid) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setNotificationsEnabledWithImportanceLockForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean areNotificationsEnabledForPackage(String pkg, int uid) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean areNotificationsEnabled(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public int getPackageImportance(String pkg) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public List<String> getAllowedAssistantAdjustments(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void allowAssistantAdjustment(String adjustmentType) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void disallowAssistantAdjustment(String adjustmentType) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean shouldHideSilentStatusIcons(String callingPkg) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void setHideSilentStatusIcons(boolean hide) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setBubblesAllowed(String pkg, int uid, boolean allowed) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean areBubblesAllowed(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean areBubblesAllowedForPackage(String pkg, int uid) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean hasUserApprovedBubblesForPackage(String pkg, int uid) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void createNotificationChannelGroups(String pkg, ParceledListSlice channelGroupList) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void createNotificationChannels(String pkg, ParceledListSlice channelsList) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void createNotificationChannelsForPackage(String pkg, int uid, ParceledListSlice channelsList) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String pkg, int uid, String groupId, boolean includeDeleted) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void updateNotificationChannelGroupForPackage(String pkg, int uid, NotificationChannelGroup group) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public NotificationChannel getNotificationChannel(String callingPkg, int userId, String pkg, String channelId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void deleteNotificationChannel(String pkg, String channelId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannels(String callingPkg, String targetPkg, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public int getDeletedChannelCount(String pkg, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public int getBlockedChannelCount(String pkg, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public void deleteNotificationChannelGroup(String pkg, String channelGroupId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public NotificationChannelGroup getNotificationChannelGroup(String pkg, String channelGroupId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannelGroups(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public boolean onlyHasDefaultChannel(String pkg, int uid) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public int getBlockedAppCount(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public boolean areChannelsBypassingDnd() throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public int getAppsBypassingDndCount(int uid) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannelsBypassingDnd(String pkg, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public boolean isPackagePaused(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public StatusBarNotification[] getActiveNotifications(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void registerListener(INotificationListener listener, ComponentName component, int userid) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void unregisterListener(INotificationListener listener, int userid) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void cancelNotificationsFromListener(INotificationListener token, String[] keys) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void snoozeNotificationUntilFromListener(INotificationListener token, String key, long until) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void requestBindListener(ComponentName component) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void requestUnbindListener(INotificationListener token) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void requestBindProvider(ComponentName component) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void requestUnbindProvider(IConditionProvider token) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setNotificationsShownFromListener(INotificationListener token, String[] keys) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getSnoozedNotificationsFromListener(INotificationListener token, int trim) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void clearRequestedListenerHints(INotificationListener token) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void requestHintsFromListener(INotificationListener token, int hints) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public int getHintsFromListener(INotificationListener token) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setInterruptionFilter(String pkg, int interruptionFilter) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void updateNotificationChannelGroupFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannelGroup group) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void updateNotificationChannelFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannel channel) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> list) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void unsnoozeNotificationFromAssistant(INotificationListener token, String key) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public ComponentName getEffectsSuppressor() throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public boolean matchesCallFilter(Bundle extras) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean isSystemConditionProviderEnabled(String path) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean isNotificationListenerAccessGranted(ComponentName listener) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean isNotificationListenerAccessGrantedForUser(ComponentName listener, int userId) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean isNotificationAssistantAccessGranted(ComponentName assistant) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void setNotificationListenerAccessGranted(ComponentName listener, boolean enabled) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean enabled) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean enabled) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean enabled) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public List<String> getEnabledNotificationListenerPackages() throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public List<ComponentName> getEnabledNotificationListeners(int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public ComponentName getAllowedNotificationAssistantForUser(int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public ComponentName getAllowedNotificationAssistant() throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public int getZenMode() throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public ZenModeConfig getZenModeConfig() throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public NotificationManager.Policy getConsolidatedNotificationPolicy() throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void setZenMode(int mode, Uri conditionId, String reason) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void notifyConditions(String pkg, IConditionProvider provider, Condition[] conditions) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean isNotificationPolicyAccessGranted(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public NotificationManager.Policy getNotificationPolicy(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void setNotificationPolicy(String pkg, NotificationManager.Policy policy) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public void setNotificationPolicyAccessGrantedForUser(String pkg, int userId, boolean granted) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public List<ZenModeConfig.ZenRule> getZenRules() throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean removeAutomaticZenRule(String id) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public boolean removeAutomaticZenRules(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public int getRuleInstanceCount(ComponentName owner) throws RemoteException {
            return 0;
        }

        @Override // android.app.INotificationManager
        public void setAutomaticZenRuleState(String id, Condition condition) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public byte[] getBackupPayload(int user) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void applyRestore(byte[] payload, int user) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public ParceledListSlice getAppActiveNotifications(String callingPkg, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public void setNotificationDelegate(String callingPkg, String delegate) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public String getNotificationDelegate(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // android.app.INotificationManager
        public boolean canNotifyAsPackage(String callingPkg, String targetPkg, int userId) throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void setPrivateNotificationsAllowed(boolean allow) throws RemoteException {
        }

        @Override // android.app.INotificationManager
        public boolean getPrivateNotificationsAllowed() throws RemoteException {
            return false;
        }

        @Override // android.app.INotificationManager
        public void doBindRecSys() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INotificationManager {
        private static final String DESCRIPTOR = "android.app.INotificationManager";
        static final int TRANSACTION_addAutomaticZenRule = 106;
        static final int TRANSACTION_allowAssistantAdjustment = 16;
        static final int TRANSACTION_applyAdjustmentFromAssistant = 76;
        static final int TRANSACTION_applyAdjustmentsFromAssistant = 77;
        static final int TRANSACTION_applyEnqueuedAdjustmentFromAssistant = 75;
        static final int TRANSACTION_applyRestore = 113;
        static final int TRANSACTION_areBubblesAllowed = 21;
        static final int TRANSACTION_areBubblesAllowedForPackage = 22;
        static final int TRANSACTION_areChannelsBypassingDnd = 45;
        static final int TRANSACTION_areNotificationsEnabled = 13;
        static final int TRANSACTION_areNotificationsEnabledForPackage = 12;
        static final int TRANSACTION_canNotifyAsPackage = 117;
        static final int TRANSACTION_canShowBadge = 9;
        static final int TRANSACTION_cancelAllNotifications = 1;
        static final int TRANSACTION_cancelNotificationFromListener = 53;
        static final int TRANSACTION_cancelNotificationWithTag = 7;
        static final int TRANSACTION_cancelNotificationsFromListener = 54;
        static final int TRANSACTION_cancelToast = 4;
        static final int TRANSACTION_clearData = 2;
        static final int TRANSACTION_clearRequestedListenerHints = 64;
        static final int TRANSACTION_createNotificationChannelGroups = 24;
        static final int TRANSACTION_createNotificationChannels = 25;
        static final int TRANSACTION_createNotificationChannelsForPackage = 26;
        static final int TRANSACTION_deleteNotificationChannel = 34;
        static final int TRANSACTION_deleteNotificationChannelGroup = 40;
        static final int TRANSACTION_disallowAssistantAdjustment = 17;
        static final int TRANSACTION_doBindRecSys = 120;
        static final int TRANSACTION_enqueueNotificationWithTag = 6;
        static final int TRANSACTION_enqueueToast = 3;
        static final int TRANSACTION_finishToken = 5;
        static final int TRANSACTION_getActiveNotifications = 49;
        static final int TRANSACTION_getActiveNotificationsFromListener = 62;
        static final int TRANSACTION_getAllowedAssistantAdjustments = 15;
        static final int TRANSACTION_getAllowedNotificationAssistant = 92;
        static final int TRANSACTION_getAllowedNotificationAssistantForUser = 91;
        static final int TRANSACTION_getAppActiveNotifications = 114;
        static final int TRANSACTION_getAppsBypassingDndCount = 46;
        static final int TRANSACTION_getAutomaticZenRule = 104;
        static final int TRANSACTION_getBackupPayload = 112;
        static final int TRANSACTION_getBlockedAppCount = 44;
        static final int TRANSACTION_getBlockedChannelCount = 39;
        static final int TRANSACTION_getConsolidatedNotificationPolicy = 95;
        static final int TRANSACTION_getDeletedChannelCount = 38;
        static final int TRANSACTION_getEffectsSuppressor = 79;
        static final int TRANSACTION_getEnabledNotificationListenerPackages = 89;
        static final int TRANSACTION_getEnabledNotificationListeners = 90;
        static final int TRANSACTION_getHintsFromListener = 66;
        static final int TRANSACTION_getHistoricalNotifications = 50;
        static final int TRANSACTION_getInterruptionFilterFromListener = 68;
        static final int TRANSACTION_getNotificationChannel = 32;
        static final int TRANSACTION_getNotificationChannelForPackage = 33;
        static final int TRANSACTION_getNotificationChannelGroup = 41;
        static final int TRANSACTION_getNotificationChannelGroupForPackage = 28;
        static final int TRANSACTION_getNotificationChannelGroups = 42;
        static final int TRANSACTION_getNotificationChannelGroupsForPackage = 27;
        static final int TRANSACTION_getNotificationChannelGroupsFromPrivilegedListener = 74;
        static final int TRANSACTION_getNotificationChannels = 35;
        static final int TRANSACTION_getNotificationChannelsBypassingDnd = 47;
        static final int TRANSACTION_getNotificationChannelsForPackage = 36;
        static final int TRANSACTION_getNotificationChannelsFromPrivilegedListener = 73;
        static final int TRANSACTION_getNotificationDelegate = 116;
        static final int TRANSACTION_getNotificationPolicy = 99;
        static final int TRANSACTION_getNumNotificationChannelsForPackage = 37;
        static final int TRANSACTION_getPackageImportance = 14;
        static final int TRANSACTION_getPopulatedNotificationChannelGroupForPackage = 29;
        static final int TRANSACTION_getPrivateNotificationsAllowed = 119;
        static final int TRANSACTION_getRuleInstanceCount = 110;
        static final int TRANSACTION_getSnoozedNotificationsFromListener = 63;
        static final int TRANSACTION_getZenMode = 93;
        static final int TRANSACTION_getZenModeConfig = 94;
        static final int TRANSACTION_getZenRules = 105;
        static final int TRANSACTION_hasUserApprovedBubblesForPackage = 23;
        static final int TRANSACTION_isNotificationAssistantAccessGranted = 84;
        static final int TRANSACTION_isNotificationListenerAccessGranted = 82;
        static final int TRANSACTION_isNotificationListenerAccessGrantedForUser = 83;
        static final int TRANSACTION_isNotificationPolicyAccessGranted = 98;
        static final int TRANSACTION_isNotificationPolicyAccessGrantedForPackage = 101;
        static final int TRANSACTION_isPackagePaused = 48;
        static final int TRANSACTION_isSystemConditionProviderEnabled = 81;
        static final int TRANSACTION_matchesCallFilter = 80;
        static final int TRANSACTION_notifyConditions = 97;
        static final int TRANSACTION_onlyHasDefaultChannel = 43;
        static final int TRANSACTION_registerListener = 51;
        static final int TRANSACTION_removeAutomaticZenRule = 108;
        static final int TRANSACTION_removeAutomaticZenRules = 109;
        static final int TRANSACTION_requestBindListener = 57;
        static final int TRANSACTION_requestBindProvider = 59;
        static final int TRANSACTION_requestHintsFromListener = 65;
        static final int TRANSACTION_requestInterruptionFilterFromListener = 67;
        static final int TRANSACTION_requestUnbindListener = 58;
        static final int TRANSACTION_requestUnbindProvider = 60;
        static final int TRANSACTION_setAutomaticZenRuleState = 111;
        static final int TRANSACTION_setBubblesAllowed = 20;
        static final int TRANSACTION_setHideSilentStatusIcons = 19;
        static final int TRANSACTION_setInterruptionFilter = 70;
        static final int TRANSACTION_setNotificationAssistantAccessGranted = 86;
        static final int TRANSACTION_setNotificationAssistantAccessGrantedForUser = 88;
        static final int TRANSACTION_setNotificationDelegate = 115;
        static final int TRANSACTION_setNotificationListenerAccessGranted = 85;
        static final int TRANSACTION_setNotificationListenerAccessGrantedForUser = 87;
        static final int TRANSACTION_setNotificationPolicy = 100;
        static final int TRANSACTION_setNotificationPolicyAccessGranted = 102;
        static final int TRANSACTION_setNotificationPolicyAccessGrantedForUser = 103;
        static final int TRANSACTION_setNotificationsEnabledForPackage = 10;
        static final int TRANSACTION_setNotificationsEnabledWithImportanceLockForPackage = 11;
        static final int TRANSACTION_setNotificationsShownFromListener = 61;
        static final int TRANSACTION_setOnNotificationPostedTrimFromListener = 69;
        static final int TRANSACTION_setPrivateNotificationsAllowed = 118;
        static final int TRANSACTION_setShowBadge = 8;
        static final int TRANSACTION_setZenMode = 96;
        static final int TRANSACTION_shouldHideSilentStatusIcons = 18;
        static final int TRANSACTION_snoozeNotificationUntilContextFromListener = 55;
        static final int TRANSACTION_snoozeNotificationUntilFromListener = 56;
        static final int TRANSACTION_unregisterListener = 52;
        static final int TRANSACTION_unsnoozeNotificationFromAssistant = 78;
        static final int TRANSACTION_updateAutomaticZenRule = 107;
        static final int TRANSACTION_updateNotificationChannelForPackage = 31;
        static final int TRANSACTION_updateNotificationChannelFromPrivilegedListener = 72;
        static final int TRANSACTION_updateNotificationChannelGroupForPackage = 30;
        static final int TRANSACTION_updateNotificationChannelGroupFromPrivilegedListener = 71;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "cancelAllNotifications";
                case 2:
                    return "clearData";
                case 3:
                    return "enqueueToast";
                case 4:
                    return "cancelToast";
                case 5:
                    return "finishToken";
                case 6:
                    return "enqueueNotificationWithTag";
                case 7:
                    return "cancelNotificationWithTag";
                case 8:
                    return "setShowBadge";
                case 9:
                    return "canShowBadge";
                case 10:
                    return "setNotificationsEnabledForPackage";
                case 11:
                    return "setNotificationsEnabledWithImportanceLockForPackage";
                case 12:
                    return "areNotificationsEnabledForPackage";
                case 13:
                    return "areNotificationsEnabled";
                case 14:
                    return "getPackageImportance";
                case 15:
                    return "getAllowedAssistantAdjustments";
                case 16:
                    return "allowAssistantAdjustment";
                case 17:
                    return "disallowAssistantAdjustment";
                case 18:
                    return "shouldHideSilentStatusIcons";
                case 19:
                    return "setHideSilentStatusIcons";
                case 20:
                    return "setBubblesAllowed";
                case 21:
                    return "areBubblesAllowed";
                case 22:
                    return "areBubblesAllowedForPackage";
                case 23:
                    return "hasUserApprovedBubblesForPackage";
                case 24:
                    return "createNotificationChannelGroups";
                case 25:
                    return "createNotificationChannels";
                case 26:
                    return "createNotificationChannelsForPackage";
                case 27:
                    return "getNotificationChannelGroupsForPackage";
                case 28:
                    return "getNotificationChannelGroupForPackage";
                case 29:
                    return "getPopulatedNotificationChannelGroupForPackage";
                case 30:
                    return "updateNotificationChannelGroupForPackage";
                case 31:
                    return "updateNotificationChannelForPackage";
                case 32:
                    return "getNotificationChannel";
                case 33:
                    return "getNotificationChannelForPackage";
                case 34:
                    return "deleteNotificationChannel";
                case 35:
                    return "getNotificationChannels";
                case 36:
                    return "getNotificationChannelsForPackage";
                case 37:
                    return "getNumNotificationChannelsForPackage";
                case 38:
                    return "getDeletedChannelCount";
                case 39:
                    return "getBlockedChannelCount";
                case 40:
                    return "deleteNotificationChannelGroup";
                case 41:
                    return "getNotificationChannelGroup";
                case 42:
                    return "getNotificationChannelGroups";
                case 43:
                    return "onlyHasDefaultChannel";
                case 44:
                    return "getBlockedAppCount";
                case 45:
                    return "areChannelsBypassingDnd";
                case 46:
                    return "getAppsBypassingDndCount";
                case 47:
                    return "getNotificationChannelsBypassingDnd";
                case 48:
                    return "isPackagePaused";
                case 49:
                    return "getActiveNotifications";
                case 50:
                    return "getHistoricalNotifications";
                case 51:
                    return "registerListener";
                case 52:
                    return "unregisterListener";
                case 53:
                    return "cancelNotificationFromListener";
                case 54:
                    return "cancelNotificationsFromListener";
                case 55:
                    return "snoozeNotificationUntilContextFromListener";
                case 56:
                    return "snoozeNotificationUntilFromListener";
                case 57:
                    return "requestBindListener";
                case 58:
                    return "requestUnbindListener";
                case 59:
                    return "requestBindProvider";
                case 60:
                    return "requestUnbindProvider";
                case 61:
                    return "setNotificationsShownFromListener";
                case 62:
                    return "getActiveNotificationsFromListener";
                case 63:
                    return "getSnoozedNotificationsFromListener";
                case 64:
                    return "clearRequestedListenerHints";
                case 65:
                    return "requestHintsFromListener";
                case 66:
                    return "getHintsFromListener";
                case 67:
                    return "requestInterruptionFilterFromListener";
                case 68:
                    return "getInterruptionFilterFromListener";
                case 69:
                    return "setOnNotificationPostedTrimFromListener";
                case 70:
                    return "setInterruptionFilter";
                case 71:
                    return "updateNotificationChannelGroupFromPrivilegedListener";
                case 72:
                    return "updateNotificationChannelFromPrivilegedListener";
                case 73:
                    return "getNotificationChannelsFromPrivilegedListener";
                case 74:
                    return "getNotificationChannelGroupsFromPrivilegedListener";
                case 75:
                    return "applyEnqueuedAdjustmentFromAssistant";
                case 76:
                    return "applyAdjustmentFromAssistant";
                case 77:
                    return "applyAdjustmentsFromAssistant";
                case 78:
                    return "unsnoozeNotificationFromAssistant";
                case 79:
                    return "getEffectsSuppressor";
                case 80:
                    return "matchesCallFilter";
                case 81:
                    return "isSystemConditionProviderEnabled";
                case 82:
                    return "isNotificationListenerAccessGranted";
                case 83:
                    return "isNotificationListenerAccessGrantedForUser";
                case 84:
                    return "isNotificationAssistantAccessGranted";
                case 85:
                    return "setNotificationListenerAccessGranted";
                case 86:
                    return "setNotificationAssistantAccessGranted";
                case 87:
                    return "setNotificationListenerAccessGrantedForUser";
                case 88:
                    return "setNotificationAssistantAccessGrantedForUser";
                case 89:
                    return "getEnabledNotificationListenerPackages";
                case 90:
                    return "getEnabledNotificationListeners";
                case 91:
                    return "getAllowedNotificationAssistantForUser";
                case 92:
                    return "getAllowedNotificationAssistant";
                case 93:
                    return "getZenMode";
                case 94:
                    return "getZenModeConfig";
                case 95:
                    return "getConsolidatedNotificationPolicy";
                case 96:
                    return "setZenMode";
                case 97:
                    return "notifyConditions";
                case 98:
                    return "isNotificationPolicyAccessGranted";
                case 99:
                    return "getNotificationPolicy";
                case 100:
                    return "setNotificationPolicy";
                case 101:
                    return "isNotificationPolicyAccessGrantedForPackage";
                case 102:
                    return "setNotificationPolicyAccessGranted";
                case 103:
                    return "setNotificationPolicyAccessGrantedForUser";
                case 104:
                    return "getAutomaticZenRule";
                case 105:
                    return "getZenRules";
                case 106:
                    return "addAutomaticZenRule";
                case 107:
                    return "updateAutomaticZenRule";
                case 108:
                    return "removeAutomaticZenRule";
                case 109:
                    return "removeAutomaticZenRules";
                case 110:
                    return "getRuleInstanceCount";
                case 111:
                    return "setAutomaticZenRuleState";
                case 112:
                    return "getBackupPayload";
                case 113:
                    return "applyRestore";
                case 114:
                    return "getAppActiveNotifications";
                case 115:
                    return "setNotificationDelegate";
                case 116:
                    return "getNotificationDelegate";
                case 117:
                    return "canNotifyAsPackage";
                case 118:
                    return "setPrivateNotificationsAllowed";
                case 119:
                    return "getPrivateNotificationsAllowed";
                case 120:
                    return "doBindRecSys";
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
            Notification _arg4;
            ParceledListSlice _arg1;
            ParceledListSlice _arg12;
            ParceledListSlice _arg2;
            NotificationChannelGroup _arg22;
            NotificationChannel _arg23;
            ComponentName _arg13;
            ComponentName _arg0;
            ComponentName _arg02;
            UserHandle _arg24;
            NotificationChannelGroup _arg3;
            UserHandle _arg25;
            NotificationChannel _arg32;
            UserHandle _arg26;
            UserHandle _arg27;
            Adjustment _arg14;
            Adjustment _arg15;
            Bundle _arg03;
            ComponentName _arg04;
            ComponentName _arg05;
            ComponentName _arg06;
            ComponentName _arg07;
            ComponentName _arg08;
            ComponentName _arg09;
            ComponentName _arg010;
            Uri _arg16;
            NotificationManager.Policy _arg17;
            AutomaticZenRule _arg011;
            AutomaticZenRule _arg18;
            ComponentName _arg012;
            Condition _arg19;
            if (code != 1598968902) {
                boolean _arg28 = false;
                boolean _arg013 = false;
                boolean _arg29 = false;
                boolean _arg110 = false;
                boolean _arg210 = false;
                boolean _arg211 = false;
                boolean _arg111 = false;
                boolean _arg112 = false;
                boolean _arg212 = false;
                boolean _arg213 = false;
                boolean _arg014 = false;
                boolean _arg214 = false;
                boolean _arg215 = false;
                boolean _arg216 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAllNotifications(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        int _arg113 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg28 = true;
                        }
                        clearData(_arg015, _arg113, _arg28);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        enqueueToast(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        cancelToast(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        finishToken(data.readString(), ITransientNotification.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        String _arg114 = data.readString();
                        String _arg217 = data.readString();
                        int _arg33 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = Notification.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        enqueueNotificationWithTag(_arg016, _arg114, _arg217, _arg33, _arg4, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        cancelNotificationWithTag(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg216 = true;
                        }
                        setShowBadge(_arg017, _arg115, _arg216);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canShowBadge = canShowBadge(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(canShowBadge ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg018 = data.readString();
                        int _arg116 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg215 = true;
                        }
                        setNotificationsEnabledForPackage(_arg018, _arg116, _arg215);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg019 = data.readString();
                        int _arg117 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg214 = true;
                        }
                        setNotificationsEnabledWithImportanceLockForPackage(_arg019, _arg117, _arg214);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean areNotificationsEnabledForPackage = areNotificationsEnabledForPackage(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(areNotificationsEnabledForPackage ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean areNotificationsEnabled = areNotificationsEnabled(data.readString());
                        reply.writeNoException();
                        reply.writeInt(areNotificationsEnabled ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getPackageImportance(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getAllowedAssistantAdjustments(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        allowAssistantAdjustment(data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        disallowAssistantAdjustment(data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldHideSilentStatusIcons = shouldHideSilentStatusIcons(data.readString());
                        reply.writeNoException();
                        reply.writeInt(shouldHideSilentStatusIcons ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = true;
                        }
                        setHideSilentStatusIcons(_arg014);
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        int _arg118 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg213 = true;
                        }
                        setBubblesAllowed(_arg020, _arg118, _arg213);
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean areBubblesAllowed = areBubblesAllowed(data.readString());
                        reply.writeNoException();
                        reply.writeInt(areBubblesAllowed ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean areBubblesAllowedForPackage = areBubblesAllowedForPackage(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(areBubblesAllowedForPackage ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasUserApprovedBubblesForPackage = hasUserApprovedBubblesForPackage(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasUserApprovedBubblesForPackage ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg021 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        createNotificationChannelGroups(_arg021, _arg1);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        createNotificationChannels(_arg022, _arg12);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg023 = data.readString();
                        int _arg119 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        createNotificationChannelsForPackage(_arg023, _arg119, _arg2);
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result3 = getNotificationChannelGroupsForPackage(data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationChannelGroup _result4 = getNotificationChannelGroupForPackage(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationChannelGroup _result5 = getPopulatedNotificationChannelGroupForPackage(data.readString(), data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg024 = data.readString();
                        int _arg120 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = NotificationChannelGroup.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        updateNotificationChannelGroupForPackage(_arg024, _arg120, _arg22);
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg025 = data.readString();
                        int _arg121 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = NotificationChannel.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        updateNotificationChannelForPackage(_arg025, _arg121, _arg23);
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationChannel _result6 = getNotificationChannel(data.readString(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationChannel _result7 = getNotificationChannelForPackage(data.readString(), data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        deleteNotificationChannel(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result8 = getNotificationChannels(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result9 = getNotificationChannelsForPackage(data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg026 = data.readString();
                        int _arg122 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg212 = true;
                        }
                        int _result10 = getNumNotificationChannelsForPackage(_arg026, _arg122, _arg212);
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getDeletedChannelCount(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getBlockedChannelCount(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        deleteNotificationChannelGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationChannelGroup _result13 = getNotificationChannelGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result14 = getNotificationChannelGroups(data.readString());
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        boolean onlyHasDefaultChannel = onlyHasDefaultChannel(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(onlyHasDefaultChannel ? 1 : 0);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getBlockedAppCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        boolean areChannelsBypassingDnd = areChannelsBypassingDnd();
                        reply.writeNoException();
                        reply.writeInt(areChannelsBypassingDnd ? 1 : 0);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getAppsBypassingDndCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result17 = getNotificationChannelsBypassingDnd(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result17 != null) {
                            reply.writeInt(1);
                            _result17.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackagePaused = isPackagePaused(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPackagePaused ? 1 : 0);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        StatusBarNotification[] _result18 = getActiveNotifications(data.readString());
                        reply.writeNoException();
                        reply.writeTypedArray(_result18, 1);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        StatusBarNotification[] _result19 = getHistoricalNotifications(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result19, 1);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg027 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        registerListener(_arg027, _arg13, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        cancelNotificationFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        cancelNotificationsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        snoozeNotificationUntilContextFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        snoozeNotificationUntilFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        requestBindListener(_arg0);
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        requestUnbindListener(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        requestBindProvider(_arg02);
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        requestUnbindProvider(IConditionProvider.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        setNotificationsShownFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result20 = getActiveNotificationsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        if (_result20 != null) {
                            reply.writeInt(1);
                            _result20.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result21 = getSnoozedNotificationsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        if (_result21 != null) {
                            reply.writeInt(1);
                            _result21.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        clearRequestedListenerHints(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        requestHintsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = getHintsFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        requestInterruptionFilterFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getInterruptionFilterFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        setOnNotificationPostedTrimFromListener(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        setInterruptionFilter(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg028 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg123 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = NotificationChannelGroup.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        updateNotificationChannelGroupFromPrivilegedListener(_arg028, _arg123, _arg24, _arg3);
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg029 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg124 = data.readString();
                        if (data.readInt() != 0) {
                            _arg25 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = NotificationChannel.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        updateNotificationChannelFromPrivilegedListener(_arg029, _arg124, _arg25, _arg32);
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg030 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg125 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        ParceledListSlice _result24 = getNotificationChannelsFromPrivilegedListener(_arg030, _arg125, _arg26);
                        reply.writeNoException();
                        if (_result24 != null) {
                            reply.writeInt(1);
                            _result24.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg031 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        String _arg126 = data.readString();
                        if (data.readInt() != 0) {
                            _arg27 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg27 = null;
                        }
                        ParceledListSlice _result25 = getNotificationChannelGroupsFromPrivilegedListener(_arg031, _arg126, _arg27);
                        reply.writeNoException();
                        if (_result25 != null) {
                            reply.writeInt(1);
                            _result25.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg032 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = Adjustment.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        applyEnqueuedAdjustmentFromAssistant(_arg032, _arg14);
                        reply.writeNoException();
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        INotificationListener _arg033 = INotificationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = Adjustment.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        applyAdjustmentFromAssistant(_arg033, _arg15);
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        applyAdjustmentsFromAssistant(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.createTypedArrayList(Adjustment.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        unsnoozeNotificationFromAssistant(INotificationListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result26 = getEffectsSuppressor();
                        reply.writeNoException();
                        if (_result26 != null) {
                            reply.writeInt(1);
                            _result26.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean matchesCallFilter = matchesCallFilter(_arg03);
                        reply.writeNoException();
                        reply.writeInt(matchesCallFilter ? 1 : 0);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSystemConditionProviderEnabled = isSystemConditionProviderEnabled(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isSystemConditionProviderEnabled ? 1 : 0);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean isNotificationListenerAccessGranted = isNotificationListenerAccessGranted(_arg04);
                        reply.writeNoException();
                        reply.writeInt(isNotificationListenerAccessGranted ? 1 : 0);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        boolean isNotificationListenerAccessGrantedForUser = isNotificationListenerAccessGrantedForUser(_arg05, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isNotificationListenerAccessGrantedForUser ? 1 : 0);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        boolean isNotificationAssistantAccessGranted = isNotificationAssistantAccessGranted(_arg06);
                        reply.writeNoException();
                        reply.writeInt(isNotificationAssistantAccessGranted ? 1 : 0);
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg112 = true;
                        }
                        setNotificationListenerAccessGranted(_arg07, _arg112);
                        reply.writeNoException();
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg111 = true;
                        }
                        setNotificationAssistantAccessGranted(_arg08, _arg111);
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        int _arg127 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg211 = true;
                        }
                        setNotificationListenerAccessGrantedForUser(_arg09, _arg127, _arg211);
                        reply.writeNoException();
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        int _arg128 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg210 = true;
                        }
                        setNotificationAssistantAccessGrantedForUser(_arg010, _arg128, _arg210);
                        reply.writeNoException();
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result27 = getEnabledNotificationListenerPackages();
                        reply.writeNoException();
                        reply.writeStringList(_result27);
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        List<ComponentName> _result28 = getEnabledNotificationListeners(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result28);
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result29 = getAllowedNotificationAssistantForUser(data.readInt());
                        reply.writeNoException();
                        if (_result29 != null) {
                            reply.writeInt(1);
                            _result29.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result30 = getAllowedNotificationAssistant();
                        reply.writeNoException();
                        if (_result30 != null) {
                            reply.writeInt(1);
                            _result30.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = getZenMode();
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        ZenModeConfig _result32 = getZenModeConfig();
                        reply.writeNoException();
                        if (_result32 != null) {
                            reply.writeInt(1);
                            _result32.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationManager.Policy _result33 = getConsolidatedNotificationPolicy();
                        reply.writeNoException();
                        if (_result33 != null) {
                            reply.writeInt(1);
                            _result33.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg034 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        setZenMode(_arg034, _arg16, data.readString());
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        notifyConditions(data.readString(), IConditionProvider.Stub.asInterface(data.readStrongBinder()), (Condition[]) data.createTypedArray(Condition.CREATOR));
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNotificationPolicyAccessGranted = isNotificationPolicyAccessGranted(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isNotificationPolicyAccessGranted ? 1 : 0);
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        NotificationManager.Policy _result34 = getNotificationPolicy(data.readString());
                        reply.writeNoException();
                        if (_result34 != null) {
                            reply.writeInt(1);
                            _result34.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg035 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = NotificationManager.Policy.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        setNotificationPolicy(_arg035, _arg17);
                        reply.writeNoException();
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNotificationPolicyAccessGrantedForPackage = isNotificationPolicyAccessGrantedForPackage(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isNotificationPolicyAccessGrantedForPackage ? 1 : 0);
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg036 = data.readString();
                        if (data.readInt() != 0) {
                            _arg110 = true;
                        }
                        setNotificationPolicyAccessGranted(_arg036, _arg110);
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg037 = data.readString();
                        int _arg129 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg29 = true;
                        }
                        setNotificationPolicyAccessGrantedForUser(_arg037, _arg129, _arg29);
                        reply.writeNoException();
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        AutomaticZenRule _result35 = getAutomaticZenRule(data.readString());
                        reply.writeNoException();
                        if (_result35 != null) {
                            reply.writeInt(1);
                            _result35.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        List<ZenModeConfig.ZenRule> _result36 = getZenRules();
                        reply.writeNoException();
                        reply.writeTypedList(_result36);
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = AutomaticZenRule.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        String _result37 = addAutomaticZenRule(_arg011);
                        reply.writeNoException();
                        reply.writeString(_result37);
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg038 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = AutomaticZenRule.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        boolean updateAutomaticZenRule = updateAutomaticZenRule(_arg038, _arg18);
                        reply.writeNoException();
                        reply.writeInt(updateAutomaticZenRule ? 1 : 0);
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeAutomaticZenRule = removeAutomaticZenRule(data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeAutomaticZenRule ? 1 : 0);
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeAutomaticZenRules = removeAutomaticZenRules(data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeAutomaticZenRules ? 1 : 0);
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        int _result38 = getRuleInstanceCount(_arg012);
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg039 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = Condition.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        setAutomaticZenRuleState(_arg039, _arg19);
                        reply.writeNoException();
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result39 = getBackupPayload(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result39);
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        applyRestore(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result40 = getAppActiveNotifications(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result40 != null) {
                            reply.writeInt(1);
                            _result40.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        setNotificationDelegate(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        String _result41 = getNotificationDelegate(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result41);
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canNotifyAsPackage = canNotifyAsPackage(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(canNotifyAsPackage ? 1 : 0);
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = true;
                        }
                        setPrivateNotificationsAllowed(_arg013);
                        reply.writeNoException();
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        boolean privateNotificationsAllowed = getPrivateNotificationsAllowed();
                        reply.writeNoException();
                        reply.writeInt(privateNotificationsAllowed ? 1 : 0);
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        doBindRecSys();
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
        public static class Proxy implements INotificationManager {
            public static INotificationManager sDefaultImpl;
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

            @Override // android.app.INotificationManager
            public void cancelAllNotifications(String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelAllNotifications(pkg, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void clearData(String pkg, int uid, boolean fromApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(fromApp ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearData(pkg, uid, fromApp);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void enqueueToast(String pkg, ITransientNotification callback, int duration, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(duration);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enqueueToast(pkg, callback, duration, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void cancelToast(String pkg, ITransientNotification callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelToast(pkg, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void finishToken(String pkg, ITransientNotification callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishToken(pkg, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(pkg);
                        try {
                            _data.writeString(opPkg);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(tag);
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
                    try {
                        _data.writeInt(id);
                        if (notification != null) {
                            _data.writeInt(1);
                            notification.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(userId);
                            if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().enqueueNotificationWithTag(pkg, opPkg, tag, id, notification, userId);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.INotificationManager
            public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelNotificationWithTag(pkg, tag, id, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setShowBadge(String pkg, int uid, boolean showBadge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(showBadge ? 1 : 0);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setShowBadge(pkg, uid, showBadge);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean canShowBadge(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canShowBadge(pkg, uid);
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

            @Override // android.app.INotificationManager
            public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationsEnabledForPackage(pkg, uid, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setNotificationsEnabledWithImportanceLockForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationsEnabledWithImportanceLockForPackage(pkg, uid, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean areNotificationsEnabledForPackage(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().areNotificationsEnabledForPackage(pkg, uid);
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

            @Override // android.app.INotificationManager
            public boolean areNotificationsEnabled(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().areNotificationsEnabled(pkg);
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

            @Override // android.app.INotificationManager
            public int getPackageImportance(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageImportance(pkg);
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

            @Override // android.app.INotificationManager
            public List<String> getAllowedAssistantAdjustments(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllowedAssistantAdjustments(pkg);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void allowAssistantAdjustment(String adjustmentType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(adjustmentType);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowAssistantAdjustment(adjustmentType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void disallowAssistantAdjustment(String adjustmentType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(adjustmentType);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disallowAssistantAdjustment(adjustmentType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean shouldHideSilentStatusIcons(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldHideSilentStatusIcons(callingPkg);
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

            @Override // android.app.INotificationManager
            public void setHideSilentStatusIcons(boolean hide) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hide ? 1 : 0);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHideSilentStatusIcons(hide);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setBubblesAllowed(String pkg, int uid, boolean allowed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(allowed ? 1 : 0);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBubblesAllowed(pkg, uid, allowed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean areBubblesAllowed(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().areBubblesAllowed(pkg);
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

            @Override // android.app.INotificationManager
            public boolean areBubblesAllowedForPackage(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().areBubblesAllowedForPackage(pkg, uid);
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

            @Override // android.app.INotificationManager
            public boolean hasUserApprovedBubblesForPackage(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasUserApprovedBubblesForPackage(pkg, uid);
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

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createNotificationChannelGroups(pkg, channelGroupList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createNotificationChannels(pkg, channelsList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createNotificationChannelsForPackage(pkg, uid, channelsList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(includeDeleted ? 1 : 0);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelGroupsForPackage(pkg, uid, includeDeleted);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) throws RemoteException {
                NotificationChannelGroup _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelGroupForPackage(groupId, pkg, uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannelGroup.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String pkg, int uid, String groupId, boolean includeDeleted) throws RemoteException {
                NotificationChannelGroup _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeString(groupId);
                    _data.writeInt(includeDeleted ? 1 : 0);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPopulatedNotificationChannelGroupForPackage(pkg, uid, groupId, includeDeleted);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannelGroup.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateNotificationChannelGroupForPackage(pkg, uid, group);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateNotificationChannelForPackage(pkg, uid, channel);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public NotificationChannel getNotificationChannel(String callingPkg, int userId, String pkg, String channelId) throws RemoteException {
                NotificationChannel _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(userId);
                    _data.writeString(pkg);
                    _data.writeString(channelId);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannel(callingPkg, userId, pkg, channelId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannel.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) throws RemoteException {
                NotificationChannel _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeString(channelId);
                    _data.writeInt(includeDeleted ? 1 : 0);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelForPackage(pkg, uid, channelId, includeDeleted);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannel.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public void deleteNotificationChannel(String pkg, String channelId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelId);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteNotificationChannel(pkg, channelId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannels(String callingPkg, String targetPkg, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(targetPkg);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannels(callingPkg, targetPkg, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(includeDeleted ? 1 : 0);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelsForPackage(pkg, uid, includeDeleted);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    _data.writeInt(includeDeleted ? 1 : 0);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNumNotificationChannelsForPackage(pkg, uid, includeDeleted);
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

            @Override // android.app.INotificationManager
            public int getDeletedChannelCount(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeletedChannelCount(pkg, uid);
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

            @Override // android.app.INotificationManager
            public int getBlockedChannelCount(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBlockedChannelCount(pkg, uid);
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

            @Override // android.app.INotificationManager
            public void deleteNotificationChannelGroup(String pkg, String channelGroupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelGroupId);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteNotificationChannelGroup(pkg, channelGroupId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public NotificationChannelGroup getNotificationChannelGroup(String pkg, String channelGroupId) throws RemoteException {
                NotificationChannelGroup _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(channelGroupId);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelGroup(pkg, channelGroupId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationChannelGroup.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannelGroups(String pkg) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelGroups(pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public boolean onlyHasDefaultChannel(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onlyHasDefaultChannel(pkg, uid);
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

            @Override // android.app.INotificationManager
            public int getBlockedAppCount(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBlockedAppCount(userId);
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

            @Override // android.app.INotificationManager
            public boolean areChannelsBypassingDnd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().areChannelsBypassingDnd();
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

            @Override // android.app.INotificationManager
            public int getAppsBypassingDndCount(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppsBypassingDndCount(uid);
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

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannelsBypassingDnd(String pkg, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelsBypassingDnd(pkg, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public boolean isPackagePaused(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackagePaused(pkg);
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

            @Override // android.app.INotificationManager
            public StatusBarNotification[] getActiveNotifications(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNotifications(callingPkg);
                    }
                    _reply.readException();
                    StatusBarNotification[] _result = (StatusBarNotification[]) _reply.createTypedArray(StatusBarNotification.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(count);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoricalNotifications(callingPkg, count);
                    }
                    _reply.readException();
                    StatusBarNotification[] _result = (StatusBarNotification[]) _reply.createTypedArray(StatusBarNotification.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerListener(listener, component, userid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void unregisterListener(INotificationListener listener, int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(userid);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterListener(listener, userid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelNotificationFromListener(token, pkg, tag, id);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void cancelNotificationsFromListener(INotificationListener token, String[] keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeStringArray(keys);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelNotificationsFromListener(token, keys);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(key);
                    _data.writeString(snoozeCriterionId);
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().snoozeNotificationUntilContextFromListener(token, key, snoozeCriterionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void snoozeNotificationUntilFromListener(INotificationListener token, String key, long until) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(key);
                    _data.writeLong(until);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().snoozeNotificationUntilFromListener(token, key, until);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestBindListener(component);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void requestUnbindListener(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestUnbindListener(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestBindProvider(component);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void requestUnbindProvider(IConditionProvider token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestUnbindProvider(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setNotificationsShownFromListener(INotificationListener token, String[] keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeStringArray(keys);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationsShownFromListener(token, keys);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ParceledListSlice getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeStringArray(keys);
                    _data.writeInt(trim);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNotificationsFromListener(token, keys, trim);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public ParceledListSlice getSnoozedNotificationsFromListener(INotificationListener token, int trim) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(trim);
                    if (!this.mRemote.transact(63, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSnoozedNotificationsFromListener(token, trim);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public void clearRequestedListenerHints(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearRequestedListenerHints(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void requestHintsFromListener(INotificationListener token, int hints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(hints);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestHintsFromListener(token, hints);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public int getHintsFromListener(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHintsFromListener(token);
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

            @Override // android.app.INotificationManager
            public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(interruptionFilter);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestInterruptionFilterFromListener(token, interruptionFilter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInterruptionFilterFromListener(token);
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

            @Override // android.app.INotificationManager
            public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(trim);
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOnNotificationPostedTrimFromListener(token, trim);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setInterruptionFilter(String pkg, int interruptionFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(interruptionFilter);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInterruptionFilter(pkg, interruptionFilter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateNotificationChannelGroupFromPrivilegedListener(token, pkg, user, group);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateNotificationChannelFromPrivilegedListener(token, pkg, user, channel);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
                ParceledListSlice _result;
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
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelsFromPrivilegedListener(token, pkg, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public ParceledListSlice getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
                ParceledListSlice _result;
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
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationChannelGroupsFromPrivilegedListener(token, pkg, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(75, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyEnqueuedAdjustmentFromAssistant(token, adjustment);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyAdjustmentFromAssistant(token, adjustment);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeTypedList(adjustments);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyAdjustmentsFromAssistant(token, adjustments);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void unsnoozeNotificationFromAssistant(INotificationListener token, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeString(key);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unsnoozeNotificationFromAssistant(token, key);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ComponentName getEffectsSuppressor() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(79, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEffectsSuppressor();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchesCallFilter(extras);
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

            @Override // android.app.INotificationManager
            public boolean isSystemConditionProviderEnabled(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    boolean _result = false;
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSystemConditionProviderEnabled(path);
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

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNotificationListenerAccessGranted(listener);
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

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNotificationListenerAccessGrantedForUser(listener, userId);
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

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNotificationAssistantAccessGranted(assistant);
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

            @Override // android.app.INotificationManager
            public void setNotificationListenerAccessGranted(ComponentName listener, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (listener != null) {
                        _data.writeInt(1);
                        listener.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(85, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationListenerAccessGranted(listener, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (assistant != null) {
                        _data.writeInt(1);
                        assistant.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationAssistantAccessGranted(assistant, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (listener != null) {
                        _data.writeInt(1);
                        listener.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationListenerAccessGrantedForUser(listener, userId, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (assistant != null) {
                        _data.writeInt(1);
                        assistant.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationAssistantAccessGrantedForUser(assistant, userId, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public List<String> getEnabledNotificationListenerPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(89, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnabledNotificationListenerPackages();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public List<ComponentName> getEnabledNotificationListeners(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnabledNotificationListeners(userId);
                    }
                    _reply.readException();
                    List<ComponentName> _result = _reply.createTypedArrayList(ComponentName.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ComponentName getAllowedNotificationAssistantForUser(int userId) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(91, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllowedNotificationAssistantForUser(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public ComponentName getAllowedNotificationAssistant() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(92, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllowedNotificationAssistant();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public int getZenMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(93, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getZenMode();
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

            @Override // android.app.INotificationManager
            public ZenModeConfig getZenModeConfig() throws RemoteException {
                ZenModeConfig _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getZenModeConfig();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ZenModeConfig.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public NotificationManager.Policy getConsolidatedNotificationPolicy() throws RemoteException {
                NotificationManager.Policy _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(95, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConsolidatedNotificationPolicy();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationManager.Policy.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(96, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setZenMode(mode, conditionId, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void notifyConditions(String pkg, IConditionProvider provider, Condition[] conditions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(provider != null ? provider.asBinder() : null);
                    _data.writeTypedArray(conditions, 0);
                    if (this.mRemote.transact(97, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyConditions(pkg, provider, conditions);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean isNotificationPolicyAccessGranted(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(98, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNotificationPolicyAccessGranted(pkg);
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

            @Override // android.app.INotificationManager
            public NotificationManager.Policy getNotificationPolicy(String pkg) throws RemoteException {
                NotificationManager.Policy _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(99, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationPolicy(pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NotificationManager.Policy.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
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
                    if (this.mRemote.transact(100, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationPolicy(pkg, policy);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNotificationPolicyAccessGrantedForPackage(pkg);
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

            @Override // android.app.INotificationManager
            public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(granted ? 1 : 0);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationPolicyAccessGranted(pkg, granted);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void setNotificationPolicyAccessGrantedForUser(String pkg, int userId, boolean granted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    _data.writeInt(granted ? 1 : 0);
                    if (this.mRemote.transact(103, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationPolicyAccessGrantedForUser(pkg, userId, granted);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
                AutomaticZenRule _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (!this.mRemote.transact(104, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAutomaticZenRule(id);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AutomaticZenRule.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public List<ZenModeConfig.ZenRule> getZenRules() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(105, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getZenRules();
                    }
                    _reply.readException();
                    List<ZenModeConfig.ZenRule> _result = _reply.createTypedArrayList(ZenModeConfig.ZenRule.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(106, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addAutomaticZenRule(automaticZenRule);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateAutomaticZenRule(id, automaticZenRule);
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

            @Override // android.app.INotificationManager
            public boolean removeAutomaticZenRule(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    boolean _result = false;
                    if (!this.mRemote.transact(108, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAutomaticZenRule(id);
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

            @Override // android.app.INotificationManager
            public boolean removeAutomaticZenRules(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAutomaticZenRules(packageName);
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

            @Override // android.app.INotificationManager
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
                    if (!this.mRemote.transact(110, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRuleInstanceCount(owner);
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

            @Override // android.app.INotificationManager
            public void setAutomaticZenRuleState(String id, Condition condition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (condition != null) {
                        _data.writeInt(1);
                        condition.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(111, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAutomaticZenRuleState(id, condition);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public byte[] getBackupPayload(int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user);
                    if (!this.mRemote.transact(112, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBackupPayload(user);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public void applyRestore(byte[] payload, int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(payload);
                    _data.writeInt(user);
                    if (this.mRemote.transact(113, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyRestore(payload, user);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public ParceledListSlice getAppActiveNotifications(String callingPkg, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(114, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppActiveNotifications(callingPkg, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.INotificationManager
            public void setNotificationDelegate(String callingPkg, String delegate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(delegate);
                    if (this.mRemote.transact(115, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNotificationDelegate(callingPkg, delegate);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public String getNotificationDelegate(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(116, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationDelegate(callingPkg);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean canNotifyAsPackage(String callingPkg, String targetPkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(targetPkg);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(117, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canNotifyAsPackage(callingPkg, targetPkg, userId);
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

            @Override // android.app.INotificationManager
            public void setPrivateNotificationsAllowed(boolean allow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allow ? 1 : 0);
                    if (this.mRemote.transact(118, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPrivateNotificationsAllowed(allow);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.INotificationManager
            public boolean getPrivateNotificationsAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(119, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrivateNotificationsAllowed();
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

            @Override // android.app.INotificationManager
            public void doBindRecSys() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(120, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().doBindRecSys();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INotificationManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INotificationManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

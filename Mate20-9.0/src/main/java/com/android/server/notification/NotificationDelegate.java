package com.android.server.notification;

import com.android.internal.statusbar.NotificationVisibility;

public interface NotificationDelegate {
    void clearEffects();

    void onClearAll(int i, int i2, int i3);

    void onNotificationActionClick(int i, int i2, String str, int i3, NotificationVisibility notificationVisibility);

    void onNotificationClear(int i, int i2, String str, String str2, int i3, int i4, String str3, int i5, NotificationVisibility notificationVisibility);

    void onNotificationClick(int i, int i2, String str, NotificationVisibility notificationVisibility);

    void onNotificationDirectReplied(String str);

    void onNotificationError(int i, int i2, String str, String str2, int i3, int i4, int i5, String str3, int i6);

    void onNotificationExpansionChanged(String str, boolean z, boolean z2);

    void onNotificationSettingsViewed(String str);

    void onNotificationSmartRepliesAdded(String str, int i);

    void onNotificationSmartReplySent(String str, int i);

    void onNotificationVisibilityChanged(NotificationVisibility[] notificationVisibilityArr, NotificationVisibility[] notificationVisibilityArr2);

    void onPanelHidden();

    void onPanelRevealed(boolean z, int i);

    void onSetDisabled(int i);
}

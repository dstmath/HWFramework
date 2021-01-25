package android.support.v4.app;

import android.app.Notification;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public interface NotificationBuilderWithBuilderAccessor {
    Notification.Builder getBuilder();
}

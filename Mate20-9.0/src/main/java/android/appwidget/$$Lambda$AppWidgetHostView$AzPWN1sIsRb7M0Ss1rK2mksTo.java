package android.appwidget;

import android.view.LayoutInflater;
import android.widget.RemoteViews;

/* renamed from: android.appwidget.-$$Lambda$AppWidgetHostView$AzPWN1sIsRb7M-0Ss1rK2mksT-o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppWidgetHostView$AzPWN1sIsRb7M0Ss1rK2mksTo implements LayoutInflater.Filter {
    public static final /* synthetic */ $$Lambda$AppWidgetHostView$AzPWN1sIsRb7M0Ss1rK2mksTo INSTANCE = new $$Lambda$AppWidgetHostView$AzPWN1sIsRb7M0Ss1rK2mksTo();

    private /* synthetic */ $$Lambda$AppWidgetHostView$AzPWN1sIsRb7M0Ss1rK2mksTo() {
    }

    public final boolean onLoadClass(Class cls) {
        return cls.isAnnotationPresent(RemoteViews.RemoteView.class);
    }
}

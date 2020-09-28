package android.widget;

import android.app.PendingIntent;
import android.view.View;
import android.widget.RemoteViews;

/* renamed from: android.widget.-$$Lambda$RemoteViews$xYCMzfQwRCAW2azHo-bWqQ9R0Wk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemoteViews$xYCMzfQwRCAW2azHobWqQ9R0Wk implements RemoteViews.OnClickHandler {
    public static final /* synthetic */ $$Lambda$RemoteViews$xYCMzfQwRCAW2azHobWqQ9R0Wk INSTANCE = new $$Lambda$RemoteViews$xYCMzfQwRCAW2azHobWqQ9R0Wk();

    private /* synthetic */ $$Lambda$RemoteViews$xYCMzfQwRCAW2azHobWqQ9R0Wk() {
    }

    @Override // android.widget.RemoteViews.OnClickHandler
    public final boolean onClickHandler(View view, PendingIntent pendingIntent, RemoteViews.RemoteResponse remoteResponse) {
        return RemoteViews.startPendingIntent(view, pendingIntent, remoteResponse.getLaunchOptions(view));
    }
}

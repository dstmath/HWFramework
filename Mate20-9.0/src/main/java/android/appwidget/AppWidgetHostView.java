package android.appwidget;

import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import android.widget.TextView;
import com.android.internal.R;
import java.util.concurrent.Executor;

public class AppWidgetHostView extends FrameLayout {
    private static final LayoutInflater.Filter INFLATER_FILTER = $$Lambda$AppWidgetHostView$AzPWN1sIsRb7M0Ss1rK2mksTo.INSTANCE;
    private static final String KEY_JAILED_ARRAY = "jail";
    static final boolean LOGD = false;
    static final String TAG = "AppWidgetHostView";
    static final int VIEW_MODE_CONTENT = 1;
    static final int VIEW_MODE_DEFAULT = 3;
    static final int VIEW_MODE_ERROR = 2;
    static final int VIEW_MODE_NOINIT = 0;
    int mAppWidgetId;
    /* access modifiers changed from: private */
    public Executor mAsyncExecutor;
    Context mContext;
    AppWidgetProviderInfo mInfo;
    /* access modifiers changed from: private */
    public CancellationSignal mLastExecutionSignal;
    int mLayoutId;
    /* access modifiers changed from: private */
    public RemoteViews.OnClickHandler mOnClickHandler;
    Context mRemoteContext;
    View mView;
    int mViewMode;

    private class ViewApplyListener implements RemoteViews.OnViewAppliedListener {
        private final boolean mIsReapply;
        private final int mLayoutId;
        private final RemoteViews mViews;

        public ViewApplyListener(RemoteViews views, int layoutId, boolean isReapply) {
            this.mViews = views;
            this.mLayoutId = layoutId;
            this.mIsReapply = isReapply;
        }

        public void onViewApplied(View v) {
            AppWidgetHostView.this.mLayoutId = this.mLayoutId;
            AppWidgetHostView.this.mViewMode = 1;
            AppWidgetHostView.this.applyContent(v, this.mIsReapply, null);
        }

        public void onError(Exception e) {
            if (this.mIsReapply) {
                CancellationSignal unused = AppWidgetHostView.this.mLastExecutionSignal = this.mViews.applyAsync(AppWidgetHostView.this.mContext, AppWidgetHostView.this, AppWidgetHostView.this.mAsyncExecutor, new ViewApplyListener(this.mViews, this.mLayoutId, false), AppWidgetHostView.this.mOnClickHandler);
            } else {
                AppWidgetHostView.this.applyContent(null, false, e);
            }
        }
    }

    public AppWidgetHostView(Context context) {
        this(context, 17432576, 17432577);
    }

    public AppWidgetHostView(Context context, RemoteViews.OnClickHandler handler) {
        this(context, 17432576, 17432577);
        this.mOnClickHandler = handler;
    }

    public AppWidgetHostView(Context context, int animationIn, int animationOut) {
        super(context);
        this.mViewMode = 0;
        this.mLayoutId = -1;
        this.mContext = context;
        setIsRootNamespace(true);
    }

    public void setOnClickHandler(RemoteViews.OnClickHandler handler) {
        this.mOnClickHandler = handler;
    }

    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        this.mAppWidgetId = appWidgetId;
        this.mInfo = info;
        Rect padding = getDefaultPadding();
        setPadding(padding.left, padding.top, padding.right, padding.bottom);
        if (info != null) {
            String description = info.loadLabel(getContext().getPackageManager());
            if ((info.providerInfo.applicationInfo.flags & 1073741824) != 0) {
                description = Resources.getSystem().getString(R.string.suspended_widget_accessibility, description);
            }
            setContentDescription(description);
        }
    }

    public static Rect getDefaultPaddingForWidget(Context context, ComponentName component, Rect padding) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(component.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return getDefaultPaddingForWidget(context, appInfo, padding);
    }

    private static Rect getDefaultPaddingForWidget(Context context, ApplicationInfo appInfo, Rect padding) {
        if (padding == null) {
            padding = new Rect(0, 0, 0, 0);
        } else {
            padding.set(0, 0, 0, 0);
        }
        if (appInfo != null && appInfo.targetSdkVersion >= 14) {
            Resources r = context.getResources();
            padding.left = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_left);
            padding.right = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_right);
            padding.top = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_top);
            padding.bottom = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_bottom);
        }
        return padding;
    }

    private Rect getDefaultPadding() {
        return getDefaultPaddingForWidget(this.mContext, this.mInfo == null ? null : this.mInfo.providerInfo.applicationInfo, (Rect) null);
    }

    public int getAppWidgetId() {
        return this.mAppWidgetId;
    }

    public AppWidgetProviderInfo getAppWidgetInfo() {
        return this.mInfo;
    }

    /* access modifiers changed from: protected */
    public void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        SparseArray<Parcelable> jail = new SparseArray<>();
        super.dispatchSaveInstanceState(jail);
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(KEY_JAILED_ARRAY, jail);
        container.put(generateId(), bundle);
    }

    private int generateId() {
        int id = getId();
        return id == -1 ? this.mAppWidgetId : id;
    }

    /* access modifiers changed from: protected */
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        Parcelable parcelable = container.get(generateId());
        SparseArray<Parcelable> jail = null;
        if (parcelable instanceof Bundle) {
            jail = ((Bundle) parcelable).getSparseParcelableArray(KEY_JAILED_ARRAY);
        }
        if (jail == null) {
            jail = new SparseArray<>();
        }
        try {
            super.dispatchRestoreInstanceState(jail);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("failed to restoreInstanceState for widget id: ");
            sb.append(this.mAppWidgetId);
            sb.append(", ");
            sb.append(this.mInfo == null ? "null" : this.mInfo.provider);
            Log.e(TAG, sb.toString(), e);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (RuntimeException e) {
            Log.e(TAG, "Remote provider threw runtime exception, using error view instead.", e);
            removeViewInLayout(this.mView);
            View child = getErrorView();
            prepareView(child);
            addViewInLayout(child, 0, child.getLayoutParams());
            measureChild(child, View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
            child.layout(0, 0, child.getMeasuredWidth() + this.mPaddingLeft + this.mPaddingRight, child.getMeasuredHeight() + this.mPaddingTop + this.mPaddingBottom);
            this.mView = child;
            this.mViewMode = 2;
        }
    }

    public void updateAppWidgetSize(Bundle newOptions, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        updateAppWidgetSize(newOptions, minWidth, minHeight, maxWidth, maxHeight, false);
    }

    public void updateAppWidgetSize(Bundle newOptions, int minWidth, int minHeight, int maxWidth, int maxHeight, boolean ignorePadding) {
        Bundle newOptions2;
        if (newOptions == null) {
            newOptions2 = new Bundle();
        } else {
            newOptions2 = newOptions;
        }
        Rect padding = getDefaultPadding();
        float density = getResources().getDisplayMetrics().density;
        int xPaddingDips = (int) (((float) (padding.left + padding.right)) / density);
        int yPaddingDips = (int) (((float) (padding.top + padding.bottom)) / density);
        int i = 0;
        int newMinWidth = minWidth - (ignorePadding ? 0 : xPaddingDips);
        int newMinHeight = minHeight - (ignorePadding ? 0 : yPaddingDips);
        int newMaxWidth = maxWidth - (ignorePadding ? 0 : xPaddingDips);
        if (!ignorePadding) {
            i = yPaddingDips;
        }
        int newMaxHeight = maxHeight - i;
        Bundle oldOptions = AppWidgetManager.getInstance(this.mContext).getAppWidgetOptions(this.mAppWidgetId);
        boolean needsUpdate = false;
        Rect rect = padding;
        if (!(newMinWidth == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) && newMinHeight == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) && newMaxWidth == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) && newMaxHeight == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT))) {
            needsUpdate = true;
        }
        if (needsUpdate) {
            newOptions2.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, newMinWidth);
            newOptions2.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, newMinHeight);
            newOptions2.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, newMaxWidth);
            newOptions2.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, newMaxHeight);
            updateAppWidgetOptions(newOptions2);
        }
    }

    public void updateAppWidgetOptions(Bundle options) {
        AppWidgetManager.getInstance(this.mContext).updateAppWidgetOptions(this.mAppWidgetId, options);
    }

    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(this.mRemoteContext != null ? this.mRemoteContext : this.mContext, attrs);
    }

    public void setExecutor(Executor executor) {
        if (this.mLastExecutionSignal != null) {
            this.mLastExecutionSignal.cancel();
            this.mLastExecutionSignal = null;
        }
        this.mAsyncExecutor = executor;
    }

    /* access modifiers changed from: package-private */
    public void resetAppWidget(AppWidgetProviderInfo info) {
        if (!(info == null || info.providerInfo == null || info.providerInfo.applicationInfo == null || !"com.huawei.health".equals(info.providerInfo.packageName))) {
            Log.i(TAG, "resetAppWidget com.huawei.health, sourceDir=" + info.providerInfo.applicationInfo.sourceDir);
        }
        setAppWidget(this.mAppWidgetId, info);
        this.mViewMode = 0;
        updateAppWidget(null);
    }

    public void updateAppWidget(RemoteViews remoteViews) {
        applyRemoteViews(remoteViews, true);
    }

    /* access modifiers changed from: protected */
    public void applyRemoteViews(RemoteViews remoteViews, boolean useAsyncIfPossible) {
        boolean recycled = false;
        View content = null;
        Exception exception = null;
        if (this.mLastExecutionSignal != null) {
            this.mLastExecutionSignal.cancel();
            this.mLastExecutionSignal = null;
        }
        if (remoteViews == null) {
            if (this.mViewMode != 3) {
                this.mLayoutId = -1;
                content = getDefaultView();
                this.mViewMode = 3;
            } else {
                return;
            }
        } else if (this.mAsyncExecutor == null || !useAsyncIfPossible) {
            this.mRemoteContext = getRemoteContext();
            int layoutId = remoteViews.getLayoutId();
            if (0 == 0 && layoutId == this.mLayoutId) {
                try {
                    remoteViews.reapply(this.mContext, this.mView, this.mOnClickHandler);
                    content = this.mView;
                    recycled = true;
                } catch (RuntimeException e) {
                    exception = e;
                }
            }
            if (content == null) {
                try {
                    content = remoteViews.apply(this.mContext, this, this.mOnClickHandler);
                } catch (RuntimeException e2) {
                    exception = e2;
                }
            }
            this.mLayoutId = layoutId;
            this.mViewMode = 1;
        } else {
            inflateAsync(remoteViews);
            return;
        }
        applyContent(content, recycled, exception);
    }

    /* access modifiers changed from: private */
    public void applyContent(View content, boolean recycled, Exception exception) {
        if (content == null) {
            if (this.mViewMode != 2) {
                if (exception != null) {
                    Log.w(TAG, "Error inflating RemoteViews : " + exception.toString());
                }
                content = getErrorView();
                this.mViewMode = 2;
            } else {
                return;
            }
        }
        if (!recycled) {
            prepareView(content);
            addView(content);
        }
        if (this.mView != content) {
            removeView(this.mView);
            this.mView = content;
        }
    }

    private void inflateAsync(RemoteViews remoteViews) {
        this.mRemoteContext = getRemoteContext();
        int layoutId = remoteViews.getLayoutId();
        if (layoutId == this.mLayoutId && this.mView != null) {
            try {
                this.mLastExecutionSignal = remoteViews.reapplyAsync(this.mContext, this.mView, this.mAsyncExecutor, new ViewApplyListener(remoteViews, layoutId, true), this.mOnClickHandler);
            } catch (Exception e) {
            }
        }
        if (this.mLastExecutionSignal == null) {
            this.mLastExecutionSignal = remoteViews.applyAsync(this.mContext, this, this.mAsyncExecutor, new ViewApplyListener(remoteViews, layoutId, false), this.mOnClickHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public void viewDataChanged(int viewId) {
        View v = findViewById(viewId);
        if (v != null && (v instanceof AdapterView)) {
            RemoteViewsAdapter.RemoteAdapterConnectionCallback remoteAdapterConnectionCallback = (AdapterView) v;
            Adapter adapter = remoteAdapterConnectionCallback.getAdapter();
            if (adapter instanceof BaseAdapter) {
                ((BaseAdapter) adapter).notifyDataSetChanged();
            } else if (adapter == null && (remoteAdapterConnectionCallback instanceof RemoteViewsAdapter.RemoteAdapterConnectionCallback)) {
                remoteAdapterConnectionCallback.deferNotifyDataSetChanged();
            }
        }
    }

    /* access modifiers changed from: protected */
    public Context getRemoteContext() {
        try {
            return this.mContext.createApplicationContext(this.mInfo.providerInfo.applicationInfo, 4);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name " + this.mInfo.providerInfo.packageName + " not found");
            return this.mContext;
        }
    }

    /* access modifiers changed from: protected */
    public void prepareView(View view) {
        FrameLayout.LayoutParams requested = (FrameLayout.LayoutParams) view.getLayoutParams();
        if (requested == null) {
            requested = new FrameLayout.LayoutParams(-1, -1);
        }
        requested.gravity = 17;
        view.setLayoutParams(requested);
    }

    private CompatibilityInfo checkToApplyHostConfig(Context hostContext, Context remoteContext, Configuration outRemoteConf, DisplayMetrics outRemoteDm) {
        outRemoteDm.setTo(remoteContext.getResources().getDisplayMetrics());
        DisplayMetrics hostDM = hostContext.getResources().getDisplayMetrics();
        if (outRemoteDm.density == hostDM.density) {
            return null;
        }
        Configuration hostConf = hostContext.getResources().getConfiguration();
        CompatibilityInfo hostCI = hostContext.getResources().getCompatibilityInfo();
        outRemoteConf.setTo(remoteContext.getResources().getConfiguration());
        remoteContext.getResources().updateConfiguration(hostConf, hostDM, hostCI);
        return remoteContext.getResources().getCompatibilityInfo();
    }

    /* access modifiers changed from: protected */
    public View getDefaultView() {
        View defaultView = null;
        Exception exception = null;
        try {
            if (this.mInfo != null) {
                Context theirContext = getRemoteContext();
                this.mRemoteContext = theirContext;
                CompatibilityInfo checkToApplyHostConfig = checkToApplyHostConfig(this.mContext, theirContext, new Configuration(), new DisplayMetrics());
                LayoutInflater inflater = ((LayoutInflater) theirContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).cloneInContext(theirContext);
                inflater.setFilter(INFLATER_FILTER);
                Bundle options = AppWidgetManager.getInstance(this.mContext).getAppWidgetOptions(this.mAppWidgetId);
                int layoutId = this.mInfo.initialLayout;
                if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY) && options.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY) == 2) {
                    int kgLayoutId = this.mInfo.initialKeyguardLayout;
                    layoutId = kgLayoutId == 0 ? layoutId : kgLayoutId;
                }
                if (((this.mRemoteContext.getApplicationInfo() == null ? 0 : this.mRemoteContext.getApplicationInfo().flags) & 1) != 0) {
                    inflater.setFactory(HwFrameworkFactory.getHwWidgetManager().createWidgetFactoryHuaWei(this.mContext, this.mInfo.provider.getPackageName()));
                }
                defaultView = inflater.inflate(layoutId, this, false);
                this.mLayoutId = layoutId;
            } else {
                Log.w(TAG, "can't inflate defaultView because mInfo is missing");
            }
        } catch (IllegalStateException e) {
            exception = e;
        } catch (NullPointerException e2) {
            exception = e2;
        } catch (RuntimeException e3) {
            exception = e3;
        } catch (Exception e4) {
            exception = e4;
        }
        if (exception != null) {
            Log.w(TAG, "Error inflating AppWidget " + this.mInfo + ": " + exception.toString());
        }
        if (defaultView == null) {
            return getErrorView();
        }
        return defaultView;
    }

    /* access modifiers changed from: protected */
    public View getErrorView() {
        TextView tv = new TextView(this.mContext);
        tv.setText(R.string.gadget_host_error_inflating);
        tv.setBackgroundColor(Color.argb(127, 0, 0, 0));
        return tv;
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setClassName(AppWidgetHostView.class.getName());
    }
}

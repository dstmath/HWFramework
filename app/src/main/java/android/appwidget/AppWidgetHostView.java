package android.appwidget;

import android.R;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.wifi.ScanResult.InformationElement;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keymaster.KeymasterDefs;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Filter;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RemoteViews;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.RemoteViews.OnViewAppliedListener;
import android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback;
import android.widget.TextView;
import java.util.concurrent.Executor;

public class AppWidgetHostView extends FrameLayout {
    static final boolean CROSSFADE = false;
    static final int FADE_DURATION = 1000;
    static final boolean LOGD = false;
    static final String TAG = "AppWidgetHostView";
    static final int VIEW_MODE_CONTENT = 1;
    static final int VIEW_MODE_DEFAULT = 3;
    static final int VIEW_MODE_ERROR = 2;
    static final int VIEW_MODE_NOINIT = 0;
    static final Filter sInflaterFilter = null;
    int mAppWidgetId;
    private Executor mAsyncExecutor;
    Context mContext;
    long mFadeStartTime;
    AppWidgetProviderInfo mInfo;
    private CancellationSignal mLastExecutionSignal;
    int mLayoutId;
    Bitmap mOld;
    Paint mOldPaint;
    private OnClickHandler mOnClickHandler;
    Context mRemoteContext;
    View mView;
    int mViewMode;

    private static class ParcelableSparseArray extends SparseArray<Parcelable> implements Parcelable {
        public static final Creator<ParcelableSparseArray> CREATOR = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.appwidget.AppWidgetHostView.ParcelableSparseArray.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.appwidget.AppWidgetHostView.ParcelableSparseArray.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.appwidget.AppWidgetHostView.ParcelableSparseArray.<clinit>():void");
        }

        private ParcelableSparseArray() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int count = size();
            dest.writeInt(count);
            for (int i = 0; i < count; i += AppWidgetHostView.VIEW_MODE_CONTENT) {
                dest.writeInt(keyAt(i));
                dest.writeParcelable((Parcelable) valueAt(i), 0);
            }
        }
    }

    private class ViewApplyListener implements OnViewAppliedListener {
        private final boolean mIsReapply;
        private final int mLayoutId;
        private final RemoteViews mViews;
        final /* synthetic */ AppWidgetHostView this$0;

        public ViewApplyListener(AppWidgetHostView this$0, RemoteViews views, int layoutId, boolean isReapply) {
            this.this$0 = this$0;
            this.mViews = views;
            this.mLayoutId = layoutId;
            this.mIsReapply = isReapply;
        }

        public void onViewApplied(View v) {
            this.this$0.mLayoutId = this.mLayoutId;
            this.this$0.mViewMode = AppWidgetHostView.VIEW_MODE_CONTENT;
            this.this$0.applyContent(v, this.mIsReapply, null);
        }

        public void onError(Exception e) {
            if (this.mIsReapply) {
                this.this$0.mLastExecutionSignal = this.mViews.applyAsync(this.this$0.mContext, this.this$0, this.this$0.mAsyncExecutor, new ViewApplyListener(this.this$0, this.mViews, this.mLayoutId, AppWidgetHostView.LOGD), this.this$0.mOnClickHandler);
            } else {
                this.this$0.applyContent(null, AppWidgetHostView.LOGD, e);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.appwidget.AppWidgetHostView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.appwidget.AppWidgetHostView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.appwidget.AppWidgetHostView.<clinit>():void");
    }

    public AppWidgetHostView(Context context) {
        this(context, R.anim.fade_in, R.anim.fade_out);
    }

    public AppWidgetHostView(Context context, OnClickHandler handler) {
        this(context, R.anim.fade_in, R.anim.fade_out);
        this.mOnClickHandler = handler;
    }

    public AppWidgetHostView(Context context, int animationIn, int animationOut) {
        super(context);
        this.mViewMode = 0;
        this.mLayoutId = -1;
        this.mFadeStartTime = -1;
        this.mOldPaint = new Paint();
        this.mContext = context;
        setIsRootNamespace(true);
    }

    public void setOnClickHandler(OnClickHandler handler) {
        this.mOnClickHandler = handler;
    }

    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        this.mAppWidgetId = appWidgetId;
        this.mInfo = info;
        if (info != null) {
            Rect padding = getDefaultPaddingForWidget(this.mContext, info.provider, null);
            setPadding(padding.left, padding.top, padding.right, padding.bottom);
            updateContentDescription(info);
        }
    }

    public static Rect getDefaultPaddingForWidget(Context context, ComponentName component, Rect padding) {
        PackageManager packageManager = context.getPackageManager();
        if (padding == null) {
            padding = new Rect(0, 0, 0, 0);
        } else {
            padding.set(0, 0, 0, 0);
        }
        try {
            if (packageManager.getApplicationInfo(component.getPackageName(), 0).targetSdkVersion >= 14) {
                Resources r = context.getResources();
                padding.left = r.getDimensionPixelSize(17104995);
                padding.right = r.getDimensionPixelSize(17104997);
                padding.top = r.getDimensionPixelSize(17104996);
                padding.bottom = r.getDimensionPixelSize(17104998);
            }
            return padding;
        } catch (NameNotFoundException e) {
            return padding;
        }
    }

    public int getAppWidgetId() {
        return this.mAppWidgetId;
    }

    public AppWidgetProviderInfo getAppWidgetInfo() {
        return this.mInfo;
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        ParcelableSparseArray jail = new ParcelableSparseArray();
        super.dispatchSaveInstanceState(jail);
        container.put(generateId(), jail);
    }

    private int generateId() {
        int id = getId();
        return id == -1 ? this.mAppWidgetId : id;
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        Parcelable parcelable = (Parcelable) container.get(generateId());
        SparseArray jail = null;
        if (parcelable != null && (parcelable instanceof ParcelableSparseArray)) {
            jail = (ParcelableSparseArray) parcelable;
        }
        if (jail == null) {
            jail = new ParcelableSparseArray();
        }
        try {
            super.dispatchRestoreInstanceState(jail);
        } catch (Exception e) {
            Log.e(TAG, "failed to restoreInstanceState for widget id: " + this.mAppWidgetId + ", " + (this.mInfo == null ? "null" : this.mInfo.provider), e);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (RuntimeException e) {
            Log.e(TAG, "Remote provider threw runtime exception, using error view instead.", e);
            removeViewInLayout(this.mView);
            View child = getErrorView();
            prepareView(child);
            addViewInLayout(child, 0, child.getLayoutParams());
            measureChild(child, MeasureSpec.makeMeasureSpec(getMeasuredWidth(), KeymasterDefs.KM_UINT_REP), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), KeymasterDefs.KM_UINT_REP));
            child.layout(0, 0, (child.getMeasuredWidth() + this.mPaddingLeft) + this.mPaddingRight, (child.getMeasuredHeight() + this.mPaddingTop) + this.mPaddingBottom);
            this.mView = child;
            this.mViewMode = VIEW_MODE_ERROR;
        }
    }

    public void updateAppWidgetSize(Bundle newOptions, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        updateAppWidgetSize(newOptions, minWidth, minHeight, maxWidth, maxHeight, LOGD);
    }

    public void updateAppWidgetSize(Bundle newOptions, int minWidth, int minHeight, int maxWidth, int maxHeight, boolean ignorePadding) {
        int i;
        if (newOptions == null) {
            newOptions = new Bundle();
        }
        Rect padding = new Rect();
        if (this.mInfo != null) {
            padding = getDefaultPaddingForWidget(this.mContext, this.mInfo.provider, padding);
        }
        float density = getResources().getDisplayMetrics().density;
        int xPaddingDips = (int) (((float) (padding.left + padding.right)) / density);
        int yPaddingDips = (int) (((float) (padding.top + padding.bottom)) / density);
        if (ignorePadding) {
            i = 0;
        } else {
            i = xPaddingDips;
        }
        int newMinWidth = minWidth - i;
        if (ignorePadding) {
            i = 0;
        } else {
            i = yPaddingDips;
        }
        int newMinHeight = minHeight - i;
        if (ignorePadding) {
            xPaddingDips = 0;
        }
        int newMaxWidth = maxWidth - xPaddingDips;
        if (ignorePadding) {
            yPaddingDips = 0;
        }
        int newMaxHeight = maxHeight - yPaddingDips;
        Bundle oldOptions = AppWidgetManager.getInstance(this.mContext).getAppWidgetOptions(this.mAppWidgetId);
        boolean needsUpdate = LOGD;
        if (newMinWidth == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) && newMinHeight == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) && newMaxWidth == oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)) {
            if (newMaxHeight != oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)) {
            }
            if (needsUpdate) {
                newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, newMinWidth);
                newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, newMinHeight);
                newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, newMaxWidth);
                newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, newMaxHeight);
                updateAppWidgetOptions(newOptions);
            }
        }
        needsUpdate = true;
        if (needsUpdate) {
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, newMinWidth);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, newMinHeight);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, newMaxWidth);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, newMaxHeight);
            updateAppWidgetOptions(newOptions);
        }
    }

    public void updateAppWidgetOptions(Bundle options) {
        AppWidgetManager.getInstance(this.mContext).updateAppWidgetOptions(this.mAppWidgetId, options);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(this.mRemoteContext != null ? this.mRemoteContext : this.mContext, attrs);
    }

    public void setAsyncExecutor(Executor executor) {
        if (this.mLastExecutionSignal != null) {
            this.mLastExecutionSignal.cancel();
            this.mLastExecutionSignal = null;
        }
        this.mAsyncExecutor = executor;
    }

    void resetAppWidget(AppWidgetProviderInfo info) {
        this.mInfo = info;
        this.mViewMode = 0;
        updateAppWidget(null);
    }

    public void updateAppWidget(RemoteViews remoteViews) {
        applyRemoteViews(remoteViews);
    }

    protected void applyRemoteViews(RemoteViews remoteViews) {
        boolean recycled = LOGD;
        View view = null;
        Exception exception = null;
        if (this.mLastExecutionSignal != null) {
            this.mLastExecutionSignal.cancel();
            this.mLastExecutionSignal = null;
        }
        if (remoteViews == null) {
            if (this.mViewMode != VIEW_MODE_DEFAULT) {
                this.mLayoutId = -1;
                view = getDefaultView();
                this.mViewMode = VIEW_MODE_DEFAULT;
            } else {
                return;
            }
        } else if (this.mAsyncExecutor != null) {
            inflateAsync(remoteViews);
            return;
        } else {
            this.mRemoteContext = getRemoteContext();
            int layoutId = remoteViews.getLayoutId();
            if (layoutId == this.mLayoutId) {
                try {
                    remoteViews.reapply(this.mContext, this.mView, this.mOnClickHandler);
                    view = this.mView;
                    recycled = true;
                } catch (Exception e) {
                    exception = e;
                }
            }
            if (view == null) {
                try {
                    view = remoteViews.apply(this.mContext, this, this.mOnClickHandler);
                } catch (Exception e2) {
                    exception = e2;
                }
            }
            this.mLayoutId = layoutId;
            this.mViewMode = VIEW_MODE_CONTENT;
        }
        applyContent(view, recycled, exception);
        updateContentDescription(this.mInfo);
    }

    private void applyContent(View content, boolean recycled, Exception exception) {
        if (content == null) {
            if (this.mViewMode != VIEW_MODE_ERROR) {
                Log.w(TAG, "updateAppWidget couldn't find any view, using error view", exception);
                content = getErrorView();
                this.mViewMode = VIEW_MODE_ERROR;
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

    private void updateContentDescription(AppWidgetProviderInfo info) {
        if (info != null) {
            ApplicationInfo appInfo = ((LauncherApps) getContext().getSystemService(LauncherApps.class)).getApplicationInfo(info.provider.getPackageName(), 0, info.getProfile());
            if (appInfo == null || (appInfo.flags & KeymasterDefs.KM_UINT_REP) == 0) {
                setContentDescription(info.label);
                return;
            }
            Resources system = Resources.getSystem();
            Object[] objArr = new Object[VIEW_MODE_CONTENT];
            objArr[0] = info.label;
            setContentDescription(system.getString(17040892, objArr));
        }
    }

    private void inflateAsync(RemoteViews remoteViews) {
        this.mRemoteContext = getRemoteContext();
        int layoutId = remoteViews.getLayoutId();
        if (layoutId == this.mLayoutId && this.mView != null) {
            try {
                this.mLastExecutionSignal = remoteViews.reapplyAsync(this.mContext, this.mView, this.mAsyncExecutor, new ViewApplyListener(this, remoteViews, layoutId, true), this.mOnClickHandler);
            } catch (Exception e) {
            }
        }
        if (this.mLastExecutionSignal == null) {
            this.mLastExecutionSignal = remoteViews.applyAsync(this.mContext, this, this.mAsyncExecutor, new ViewApplyListener(this, remoteViews, layoutId, LOGD), this.mOnClickHandler);
        }
    }

    void viewDataChanged(int viewId) {
        View v = findViewById(viewId);
        if (v != null && (v instanceof AdapterView)) {
            AdapterView<?> adapterView = (AdapterView) v;
            Adapter adapter = adapterView.getAdapter();
            if (adapter instanceof BaseAdapter) {
                ((BaseAdapter) adapter).notifyDataSetChanged();
            } else if (adapter == null && (adapterView instanceof RemoteAdapterConnectionCallback)) {
                ((RemoteAdapterConnectionCallback) adapterView).deferNotifyDataSetChanged();
            }
        }
    }

    protected Context getRemoteContext() {
        try {
            return this.mContext.createApplicationContext(this.mInfo.providerInfo.applicationInfo, 4);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name " + this.mInfo.providerInfo.packageName + " not found");
            return this.mContext;
        }
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(canvas, child, drawingTime);
    }

    protected void prepareView(View view) {
        LayoutParams requested = (LayoutParams) view.getLayoutParams();
        if (requested == null) {
            requested = new LayoutParams(-1, -1);
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

    protected View getDefaultView() {
        View defaultView = null;
        Exception exception = null;
        try {
            if (this.mInfo != null) {
                int flags;
                Context theirContext = getRemoteContext();
                this.mRemoteContext = theirContext;
                Configuration remoteConf = new Configuration();
                DisplayMetrics remoteDM = new DisplayMetrics();
                CompatibilityInfo remoteCI = checkToApplyHostConfig(this.mContext, theirContext, remoteConf, remoteDM);
                LayoutInflater inflater = ((LayoutInflater) theirContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).cloneInContext(theirContext);
                inflater.setFilter(sInflaterFilter);
                Bundle options = AppWidgetManager.getInstance(this.mContext).getAppWidgetOptions(this.mAppWidgetId);
                int layoutId = this.mInfo.initialLayout;
                if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY)) {
                    if (options.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY) == VIEW_MODE_ERROR) {
                        int kgLayoutId = this.mInfo.initialKeyguardLayout;
                        if (kgLayoutId != 0) {
                            layoutId = kgLayoutId;
                        }
                    }
                }
                if (this.mRemoteContext.getApplicationInfo() == null) {
                    flags = 0;
                } else {
                    flags = this.mRemoteContext.getApplicationInfo().flags;
                }
                if ((flags & VIEW_MODE_CONTENT) != 0) {
                    inflater.setFactory(HwFrameworkFactory.getHwWidgetManager().createWidgetFactoryHuaWei(this.mContext, this.mInfo.provider.getPackageName()));
                }
                defaultView = inflater.inflate(layoutId, this, LOGD);
                if (remoteCI != null) {
                    theirContext.getResources().updateConfiguration(remoteConf, remoteDM, remoteCI);
                }
                this.mLayoutId = layoutId;
            } else {
                Log.w(TAG, "can't inflate defaultView because mInfo is missing");
            }
        } catch (Exception e) {
            exception = e;
        } catch (Exception e2) {
            exception = e2;
        } catch (Exception e3) {
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

    protected View getErrorView() {
        TextView tv = new TextView(this.mContext);
        tv.setText(17040452);
        tv.setBackgroundColor(Color.argb(InformationElement.EID_EXTENDED_CAPS, 0, 0, 0));
        return tv;
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setClassName(AppWidgetHostView.class.getName());
    }
}

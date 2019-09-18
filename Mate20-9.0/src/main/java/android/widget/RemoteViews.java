package android.widget;

import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.Application;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.appwidget.AppWidgetHostView;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.UserHandle;
import android.provider.BrowserContract;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RemoteViewsAdapter;
import com.android.internal.R;
import com.android.internal.util.NotificationColorUtil;
import com.android.internal.util.Preconditions;
import java.lang.annotation.ElementType;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class RemoteViews implements Parcelable, LayoutInflater.Filter {
    /* access modifiers changed from: private */
    public static final Action ACTION_NOOP = new RuntimeAction() {
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
        }
    };
    private static final String[] BASE_PACKAGES = {"android", "com.android.mediacenter", "com.huawei.systemmanager", "com.android.providers.applications", "com.android.providers.contacts", "com.android.providers.userdictionary", "com.android.providers.calendar", "com.android.providers.downloads", "com.android.providers.drm", "com.android.providers.media", "com.android.providers.downloads.ui", "com.android.calculator2", "com.android.htmlviewer", BrowserContract.AUTHORITY, "com.android.gallery3d", "com.android.exchange", "com.android.email", "com.huawei.android.hwlockscreen", "com.android.smspush"};
    private static final int BITMAP_REFLECTION_ACTION_TAG = 12;
    public static final Parcelable.Creator<RemoteViews> CREATOR = new Parcelable.Creator<RemoteViews>() {
        public RemoteViews createFromParcel(Parcel parcel) {
            return new RemoteViews(parcel);
        }

        public RemoteViews[] newArray(int size) {
            return new RemoteViews[size];
        }
    };
    /* access modifiers changed from: private */
    public static final OnClickHandler DEFAULT_ON_CLICK_HANDLER = new OnClickHandler();
    static final String EXTRA_REMOTEADAPTER_APPWIDGET_ID = "remoteAdapterAppWidgetId";
    private static final int LAYOUT_PARAM_ACTION_TAG = 19;
    private static final String LOG_TAG = "RemoteViews";
    private static final int MAX_NESTED_VIEWS = 10;
    private static final int MODE_HAS_LANDSCAPE_AND_PORTRAIT = 1;
    private static final int MODE_NORMAL = 0;
    private static final int OVERRIDE_TEXT_COLORS_TAG = 20;
    private static final int REFLECTION_ACTION_TAG = 2;
    /* access modifiers changed from: private */
    public static final boolean RUNTIME_MAPLE = (System.getenv("MAPLE_RUNTIME") != null);
    private static final int SET_DRAWABLE_TINT_TAG = 3;
    private static final int SET_EMPTY_VIEW_ACTION_TAG = 6;
    private static final int SET_ON_CLICK_FILL_IN_INTENT_TAG = 9;
    private static final int SET_ON_CLICK_PENDING_INTENT_TAG = 1;
    private static final int SET_PENDING_INTENT_TEMPLATE_TAG = 8;
    private static final int SET_REMOTE_INPUTS_ACTION_TAG = 18;
    private static final int SET_REMOTE_VIEW_ADAPTER_INTENT_TAG = 10;
    private static final int SET_REMOTE_VIEW_ADAPTER_LIST_TAG = 15;
    private static final int TEXT_VIEW_DRAWABLE_ACTION_TAG = 11;
    private static final int TEXT_VIEW_SIZE_ACTION_TAG = 13;
    private static final int VIEW_CONTENT_NAVIGATION_TAG = 5;
    private static final int VIEW_GROUP_ACTION_ADD_TAG = 4;
    private static final int VIEW_GROUP_ACTION_REMOVE_TAG = 7;
    private static final int VIEW_PADDING_ACTION_TAG = 14;
    private static final MethodKey sLookupKey = new MethodKey();
    private static final ArrayMap<Method, Method> sMapleAsyncMethods = new ArrayMap<>();
    private static final ThreadLocal<Object[]> sMapleInvokeArgsTls = new ThreadLocal<Object[]>() {
        /* access modifiers changed from: protected */
        public Object[] initialValue() {
            return new Object[1];
        }
    };
    private static final ArrayMap<Class<? extends View>, ArrayMap<MutablePair<String, Class<?>>, Method>> sMapleMethods = new ArrayMap<>();
    private static final ArrayMap<MethodKey, MethodArgs> sMethods = new ArrayMap<>();
    private static final Object[] sMethodsLock = new Object[0];
    /* access modifiers changed from: private */
    public ArrayList<Action> mActions;
    public ApplicationInfo mApplication;
    private int mApplyThemeResId;
    /* access modifiers changed from: private */
    public BitmapCache mBitmapCache;
    private final Map<Class, Object> mClassCookies;
    private boolean mIsRoot;
    /* access modifiers changed from: private */
    public boolean mIsWidgetCollectionChild;
    private RemoteViews mLandscape;
    private final int mLayoutId;
    private final MutablePair<String, Class<?>> mPair;
    private RemoteViews mPortrait;
    private boolean mReapplyDisallowed;
    private Set<Integer> mSignSet;
    /* access modifiers changed from: private */
    public boolean mUseAppContext;

    private static abstract class Action implements Parcelable {
        public static final int MERGE_APPEND = 1;
        public static final int MERGE_IGNORE = 2;
        public static final int MERGE_REPLACE = 0;
        int viewId;

        public abstract void apply(View view, ViewGroup viewGroup, OnClickHandler onClickHandler) throws ActionException;

        public abstract int getActionTag();

        private Action() {
        }

        public int describeContents() {
            return 0;
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
        }

        public int mergeBehavior() {
            return 0;
        }

        public String getUniqueKey() {
            return getActionTag() + Session.SESSION_SEPARATION_CHAR_CHILD + this.viewId;
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            return this;
        }

        public boolean prefersAsyncApply() {
            return false;
        }

        public boolean hasSameAppInfo(ApplicationInfo parentInfo) {
            return true;
        }

        public void visitUris(Consumer<Uri> consumer) {
        }
    }

    public static class ActionException extends RuntimeException {
        public ActionException(Exception ex) {
            super(ex);
        }

        public ActionException(String message) {
            super(message);
        }

        public ActionException(Throwable t) {
            super(t);
        }
    }

    private class AsyncApplyTask extends AsyncTask<Void, Void, ViewTree> implements CancellationSignal.OnCancelListener {
        private Action[] mActions;
        final Context mContext;
        /* access modifiers changed from: private */
        public Exception mError;
        final OnClickHandler mHandler;
        final OnViewAppliedListener mListener;
        final ViewGroup mParent;
        final RemoteViews mRV;
        /* access modifiers changed from: private */
        public View mResult;
        private ViewTree mTree;

        private AsyncApplyTask(RemoteViews rv, ViewGroup parent, Context context, OnViewAppliedListener listener, OnClickHandler handler, View result) {
            this.mRV = rv;
            this.mParent = parent;
            this.mContext = context;
            this.mListener = listener;
            this.mHandler = handler;
            this.mResult = result;
            RemoteViews.loadTransitionOverride(context, handler);
        }

        /* access modifiers changed from: protected */
        public ViewTree doInBackground(Void... params) {
            try {
                if (this.mResult == null) {
                    this.mResult = RemoteViews.this.inflateView(this.mContext, RemoteViews.this.getContextForResources(this.mContext), this.mRV, this.mParent, this.mHandler);
                }
                this.mTree = new ViewTree(this.mResult);
                if (this.mRV.mActions != null) {
                    int count = this.mRV.mActions.size();
                    this.mActions = new Action[count];
                    for (int i = 0; i < count && !isCancelled(); i++) {
                        this.mActions[i] = ((Action) this.mRV.mActions.get(i)).initActionAsync(this.mTree, this.mParent, this.mHandler);
                    }
                } else {
                    this.mActions = null;
                }
                return this.mTree;
            } catch (Exception e) {
                this.mError = e;
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ViewTree viewTree) {
            if (this.mError == null) {
                try {
                    if (this.mActions != null) {
                        OnClickHandler handler = this.mHandler == null ? RemoteViews.DEFAULT_ON_CLICK_HANDLER : this.mHandler;
                        for (Action a : this.mActions) {
                            a.apply(viewTree.mRoot, this.mParent, handler);
                        }
                    }
                } catch (Exception e) {
                    this.mError = e;
                }
            }
            if (this.mListener != null) {
                if (this.mError != null) {
                    this.mListener.onError(this.mError);
                } else {
                    this.mListener.onViewApplied(viewTree.mRoot);
                }
            } else if (this.mError == null) {
            } else {
                if (this.mError instanceof ActionException) {
                    throw ((ActionException) this.mError);
                }
                throw new ActionException(this.mError);
            }
        }

        public void onCancel() {
            cancel(true);
        }
    }

    private static class BitmapCache {
        static final int mMaxNumOfBitmaps = 15;
        int mBitmapMemory;
        ArrayList<Bitmap> mBitmaps;
        int mBitmapsCount;

        public BitmapCache() {
            this.mBitmapsCount = 0;
            this.mBitmapMemory = -1;
            this.mBitmaps = new ArrayList<>();
        }

        public int getSize() {
            return this.mBitmaps.size();
        }

        public BitmapCache(Parcel source) {
            this.mBitmapsCount = 0;
            this.mBitmapMemory = -1;
            this.mBitmaps = source.createTypedArrayList(Bitmap.CREATOR);
        }

        public int getBitmapId(Bitmap b) {
            if (b == null) {
                return -1;
            }
            if (this.mBitmaps.contains(b)) {
                return this.mBitmaps.indexOf(b);
            }
            this.mBitmaps.add(b);
            this.mBitmapMemory = -1;
            return this.mBitmaps.size() - 1;
        }

        public Bitmap getBitmapForId(int id) {
            if (id == -1 || id >= this.mBitmaps.size()) {
                return null;
            }
            return this.mBitmaps.get(id);
        }

        public void writeBitmapsToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.mBitmaps, flags);
        }

        private int addBitmapsForSafe(Bitmap b) {
            int index;
            if (this.mBitmaps == null) {
                return -1;
            }
            if (this.mBitmapsCount < 15) {
                this.mBitmaps.add(b);
                index = this.mBitmapsCount;
            } else {
                index = this.mBitmapsCount % 15;
                this.mBitmaps.set(index, b);
                Log.w(RemoteViews.LOG_TAG, "RemoteViews try to cache " + this.mBitmapsCount + " bitmaps, only allows " + 15 + ", replace bitmap at index " + index);
            }
            this.mBitmapsCount++;
            return index;
        }

        public int getBitmapMemory() {
            if (this.mBitmapMemory < 0) {
                this.mBitmapMemory = 0;
                int count = this.mBitmaps.size();
                for (int i = 0; i < count; i++) {
                    this.mBitmapMemory += this.mBitmaps.get(i).getAllocationByteCount();
                }
            }
            return this.mBitmapMemory;
        }
    }

    @RCUnownedThisRef
    private class BitmapReflectionAction extends Action {
        Bitmap bitmap;
        int bitmapId;
        String methodName;

        BitmapReflectionAction(int viewId, String methodName2, Bitmap bitmap2) {
            super();
            this.bitmap = bitmap2;
            this.viewId = viewId;
            this.methodName = methodName2;
            this.bitmapId = RemoteViews.this.mBitmapCache.getBitmapId(bitmap2);
        }

        BitmapReflectionAction(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.bitmapId = in.readInt();
            this.bitmap = RemoteViews.this.mBitmapCache.getBitmapForId(this.bitmapId);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeString(this.methodName);
            dest.writeInt(this.bitmapId);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
            ReflectionAction ra = new ReflectionAction(this.viewId, this.methodName, 12, this.bitmap);
            ra.apply(root, rootParent, handler);
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            this.bitmapId = bitmapCache.getBitmapId(this.bitmap);
        }

        public int getActionTag() {
            return 12;
        }
    }

    @RCUnownedThisRef
    private static class LayoutParamAction extends Action {
        public static final int LAYOUT_MARGIN_BOTTOM_DIMEN = 3;
        public static final int LAYOUT_MARGIN_END = 4;
        public static final int LAYOUT_MARGIN_END_DIMEN = 1;
        public static final int LAYOUT_MARGIN_TOP_DIMEN = 5;
        public static final int LAYOUT_WIDTH = 2;
        final int mProperty;
        final int mValue;

        public LayoutParamAction(int viewId, int property, int value) {
            super();
            this.viewId = viewId;
            this.mProperty = property;
            this.mValue = value;
        }

        public LayoutParamAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.mProperty = parcel.readInt();
            this.mValue = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.mProperty);
            dest.writeInt(this.mValue);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                ViewGroup.LayoutParams layoutParams = target.getLayoutParams();
                if (layoutParams != null) {
                    int value = this.mValue;
                    switch (this.mProperty) {
                        case 1:
                            value = resolveDimenPixelOffset(target, this.mValue);
                            break;
                        case 2:
                            layoutParams.width = this.mValue;
                            target.setLayoutParams(layoutParams);
                            break;
                        case 3:
                            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                                ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = resolveDimenPixelOffset(target, this.mValue);
                                target.setLayoutParams(layoutParams);
                                break;
                            }
                            break;
                        case 4:
                            break;
                        case 5:
                            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                                ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = resolveDimenPixelOffset(target, this.mValue);
                                target.setLayoutParams(layoutParams);
                                break;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown property " + this.mProperty);
                    }
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(value);
                        target.setLayoutParams(layoutParams);
                    }
                }
            }
        }

        private static int resolveDimenPixelOffset(View target, int value) {
            if (value == 0) {
                return 0;
            }
            return target.getContext().getResources().getDimensionPixelOffset(value);
        }

        public int getActionTag() {
            return 19;
        }

        public String getUniqueKey() {
            return super.getUniqueKey() + this.mProperty;
        }
    }

    static class MethodArgs {
        public MethodHandle asyncMethod;
        public String asyncMethodName;
        public MethodHandle syncMethod;

        MethodArgs() {
        }
    }

    static class MethodKey {
        public String methodName;
        public Class paramClass;
        public Class targetClass;

        MethodKey() {
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof MethodKey)) {
                return false;
            }
            MethodKey p = (MethodKey) o;
            if (Objects.equals(p.targetClass, this.targetClass) && Objects.equals(p.paramClass, this.paramClass) && Objects.equals(p.methodName, this.methodName)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (Objects.hashCode(this.targetClass) ^ Objects.hashCode(this.paramClass)) ^ Objects.hashCode(this.methodName);
        }

        public void set(Class targetClass2, Class paramClass2, String methodName2) {
            this.targetClass = targetClass2;
            this.paramClass = paramClass2;
            this.methodName = methodName2;
        }
    }

    static class MutablePair<F, S> {
        F first;
        S second;

        MutablePair(F first2, S second2) {
            this.first = first2;
            this.second = second2;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof MutablePair)) {
                return false;
            }
            MutablePair<?, ?> p = (MutablePair) o;
            if (Objects.equals(p.first, this.first) && Objects.equals(p.second, this.second)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.first == null ? 0 : this.first.hashCode();
            if (this.second != null) {
                i = this.second.hashCode();
            }
            return hashCode ^ i;
        }
    }

    public static class OnClickHandler {
        private int mEnterAnimationId;

        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
            return onClickHandler(view, pendingIntent, fillInIntent, 0);
        }

        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent, int windowingMode) {
            ActivityOptions opts;
            try {
                Context context = view.getContext();
                if (this.mEnterAnimationId != 0) {
                    opts = ActivityOptions.makeCustomAnimation(context, this.mEnterAnimationId, 0);
                } else {
                    opts = ActivityOptions.makeBasic();
                }
                ActivityOptions opts2 = opts;
                if (windowingMode != 0) {
                    opts2.setLaunchWindowingMode(windowingMode);
                }
                addLaunchFlagIfNeed(view, opts2);
                context.startIntentSender(pendingIntent.getIntentSender(), fillInIntent, 268435456, 268435456, 0, opts2.toBundle());
                return true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(RemoteViews.LOG_TAG, "Cannot send pending intent: ", e);
                return false;
            } catch (Exception e2) {
                Log.e(RemoteViews.LOG_TAG, "Cannot send pending intent due to unknown exception: ", e2);
                return false;
            }
        }

        public void setEnterAnimationId(int enterAnimationId) {
            this.mEnterAnimationId = enterAnimationId;
        }

        private void addLaunchFlagIfNeed(View view, ActivityOptions opts) {
            if (view != null && 34603061 == view.getId() && "freeform".equals(view.getTag())) {
                HwFreeFormUtils.log("ams", "Launch activity in freeform");
                opts.setLaunchWindowingMode(5);
            }
        }
    }

    public interface OnViewAppliedListener {
        void onError(Exception exc);

        void onViewApplied(View view);
    }

    @RCUnownedThisRef
    private class OverrideTextColorsAction extends Action {
        private final int textColor;

        public OverrideTextColorsAction(int textColor2) {
            super();
            this.textColor = textColor2;
        }

        public OverrideTextColorsAction(Parcel parcel) {
            super();
            this.textColor = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.textColor);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            Stack<View> viewsToProcess = new Stack<>();
            viewsToProcess.add(root);
            while (!viewsToProcess.isEmpty()) {
                View v = viewsToProcess.pop();
                if (v instanceof TextView) {
                    TextView textView = (TextView) v;
                    textView.setText(NotificationColorUtil.clearColorSpans(textView.getText()));
                    textView.setTextColor(this.textColor);
                }
                if (v instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) v;
                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                        viewsToProcess.push(viewGroup.getChildAt(i));
                    }
                }
            }
        }

        public int getActionTag() {
            return 20;
        }
    }

    @RCUnownedThisRef
    private final class ReflectionAction extends Action {
        static final int BITMAP = 12;
        static final int BOOLEAN = 1;
        static final int BUNDLE = 13;
        static final int BYTE = 2;
        static final int CHAR = 8;
        static final int CHAR_SEQUENCE = 10;
        static final int COLOR_STATE_LIST = 15;
        static final int DOUBLE = 7;
        static final int FLOAT = 6;
        static final int ICON = 16;
        static final int INT = 4;
        static final int INTENT = 14;
        static final int LONG = 5;
        static final int SHORT = 3;
        static final int STRING = 9;
        static final int URI = 11;
        String methodName;
        int type;
        Object value;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: android.widget.RemoteViews.ReflectionAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: boot-framework_classes2.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: android.widget.RemoteViews.ReflectionAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: boot-framework_classes2.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: android.widget.RemoteViews.ReflectionAction.initActionAsync(android.widget.RemoteViews$ViewTree, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):android.widget.RemoteViews$Action, dex: boot-framework_classes2.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public android.widget.RemoteViews.Action initActionAsync(android.widget.RemoteViews.ViewTree r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: android.widget.RemoteViews.ReflectionAction.initActionAsync(android.widget.RemoteViews$ViewTree, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):android.widget.RemoteViews$Action, dex: boot-framework_classes2.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionAction.initActionAsync(android.widget.RemoteViews$ViewTree, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):android.widget.RemoteViews$Action");
        }

        ReflectionAction(int viewId, String methodName2, int type2, Object value2) {
            super();
            this.viewId = viewId;
            this.methodName = methodName2;
            this.type = type2;
            this.value = value2;
        }

        ReflectionAction(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.type = in.readInt();
            switch (this.type) {
                case 1:
                    this.value = Boolean.valueOf(in.readBoolean());
                    return;
                case 2:
                    this.value = Byte.valueOf(in.readByte());
                    return;
                case 3:
                    this.value = Short.valueOf((short) in.readInt());
                    return;
                case 4:
                    this.value = Integer.valueOf(in.readInt());
                    return;
                case 5:
                    this.value = Long.valueOf(in.readLong());
                    return;
                case 6:
                    this.value = Float.valueOf(in.readFloat());
                    return;
                case 7:
                    this.value = Double.valueOf(in.readDouble());
                    return;
                case 8:
                    this.value = Character.valueOf((char) in.readInt());
                    return;
                case 9:
                    this.value = in.readString();
                    return;
                case 10:
                    this.value = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                    return;
                case 11:
                    this.value = in.readTypedObject(Uri.CREATOR);
                    return;
                case 12:
                    this.value = in.readTypedObject(Bitmap.CREATOR);
                    return;
                case 13:
                    this.value = in.readBundle();
                    return;
                case 14:
                    this.value = in.readTypedObject(Intent.CREATOR);
                    return;
                case 15:
                    this.value = in.readTypedObject(ColorStateList.CREATOR);
                    return;
                case 16:
                    this.value = in.readTypedObject(Icon.CREATOR);
                    return;
                default:
                    return;
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
            out.writeInt(this.type);
            switch (this.type) {
                case 1:
                    out.writeBoolean(((Boolean) this.value).booleanValue());
                    return;
                case 2:
                    out.writeByte(((Byte) this.value).byteValue());
                    return;
                case 3:
                    out.writeInt(((Short) this.value).shortValue());
                    return;
                case 4:
                    out.writeInt(((Integer) this.value).intValue());
                    return;
                case 5:
                    out.writeLong(((Long) this.value).longValue());
                    return;
                case 6:
                    out.writeFloat(((Float) this.value).floatValue());
                    return;
                case 7:
                    out.writeDouble(((Double) this.value).doubleValue());
                    return;
                case 8:
                    out.writeInt(((Character) this.value).charValue());
                    return;
                case 9:
                    out.writeString((String) this.value);
                    return;
                case 10:
                    TextUtils.writeToParcel((CharSequence) this.value, out, flags);
                    return;
                case 11:
                case 12:
                case 14:
                case 15:
                case 16:
                    out.writeTypedObject((Parcelable) this.value, flags);
                    return;
                case 13:
                    out.writeBundle((Bundle) this.value);
                    return;
                default:
                    return;
            }
        }

        private Class<?> getParameterType() {
            switch (this.type) {
                case 1:
                    return Boolean.TYPE;
                case 2:
                    return Byte.TYPE;
                case 3:
                    return Short.TYPE;
                case 4:
                    return Integer.TYPE;
                case 5:
                    return Long.TYPE;
                case 6:
                    return Float.TYPE;
                case 7:
                    return Double.TYPE;
                case 8:
                    return Character.TYPE;
                case 9:
                    return String.class;
                case 10:
                    return CharSequence.class;
                case 11:
                    return Uri.class;
                case 12:
                    return Bitmap.class;
                case 13:
                    return Bundle.class;
                case 14:
                    return Intent.class;
                case 15:
                    return ColorStateList.class;
                case 16:
                    return Icon.class;
                default:
                    return null;
            }
        }

        public int mergeBehavior() {
            if (this.methodName.equals("smoothScrollBy")) {
                return 1;
            }
            return 0;
        }

        public int getActionTag() {
            return 2;
        }

        public String getUniqueKey() {
            return super.getUniqueKey() + this.methodName + this.type;
        }

        public boolean prefersAsyncApply() {
            return this.type == 11 || this.type == 16;
        }

        public void visitUris(Consumer<Uri> visitor) {
            int i = this.type;
            if (i == 11) {
                visitor.accept((Uri) this.value);
            } else if (i == 16) {
                RemoteViews.visitIconUri((Icon) this.value, visitor);
            }
        }
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RemoteView {
    }

    private static class RemoteViewsContextWrapper extends ContextWrapper {
        private final Context mContextForResources;

        RemoteViewsContextWrapper(Context context, Context contextForResources) {
            super(context);
            this.mContextForResources = contextForResources;
        }

        public Resources getResources() {
            return this.mContextForResources.getResources();
        }

        public Resources.Theme getTheme() {
            return this.mContextForResources.getTheme();
        }

        public String getPackageName() {
            return this.mContextForResources.getPackageName();
        }
    }

    private static final class RunnableAction extends RuntimeAction {
        private final Runnable mRunnable;

        RunnableAction(Runnable r) {
            super();
            this.mRunnable = r;
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            this.mRunnable.run();
        }
    }

    private static abstract class RuntimeAction extends Action {
        private RuntimeAction() {
            super();
        }

        public final int getActionTag() {
            return 0;
        }

        public final void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException();
        }
    }

    @RCUnownedThisRef
    private class SetDrawableTint extends Action {
        int colorFilter;
        PorterDuff.Mode filterMode;
        boolean targetBackground;

        SetDrawableTint(int id, boolean targetBackground2, int colorFilter2, PorterDuff.Mode mode) {
            super();
            this.viewId = id;
            this.targetBackground = targetBackground2;
            this.colorFilter = colorFilter2;
            this.filterMode = mode;
        }

        SetDrawableTint(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.targetBackground = parcel.readInt() != 0;
            this.colorFilter = parcel.readInt();
            this.filterMode = PorterDuff.intToMode(parcel.readInt());
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.targetBackground ? 1 : 0);
            dest.writeInt(this.colorFilter);
            dest.writeInt(PorterDuff.modeToInt(this.filterMode));
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                Drawable targetDrawable = null;
                if (this.targetBackground) {
                    targetDrawable = target.getBackground();
                } else if (target instanceof ImageView) {
                    targetDrawable = ((ImageView) target).getDrawable();
                }
                if (targetDrawable != null) {
                    targetDrawable.mutate().setColorFilter(this.colorFilter, this.filterMode);
                }
            }
        }

        public int getActionTag() {
            return 3;
        }
    }

    private class SetEmptyView extends Action {
        int emptyViewId;

        SetEmptyView(int viewId, int emptyViewId2) {
            super();
            this.viewId = viewId;
            this.emptyViewId = emptyViewId2;
        }

        SetEmptyView(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.emptyViewId = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.viewId);
            out.writeInt(this.emptyViewId);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view instanceof AdapterView) {
                AdapterView<?> adapterView = (AdapterView) view;
                View emptyView = root.findViewById(this.emptyViewId);
                if (emptyView != null) {
                    adapterView.setEmptyView(emptyView);
                }
            }
        }

        public int getActionTag() {
            return 6;
        }
    }

    private class SetOnClickFillInIntent extends Action {
        Intent fillInIntent;

        public SetOnClickFillInIntent(int id, Intent fillInIntent2) {
            super();
            this.viewId = id;
            this.fillInIntent = fillInIntent2;
        }

        public SetOnClickFillInIntent(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.fillInIntent = (Intent) parcel.readTypedObject(Intent.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeTypedObject(this.fillInIntent, 0);
        }

        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (!RemoteViews.this.mIsWidgetCollectionChild) {
                    Log.e(RemoteViews.LOG_TAG, "The method setOnClickFillInIntent is available only from RemoteViewsFactory (ie. on collection items).");
                    return;
                }
                if (target == root) {
                    target.setTagInternal(16908897, this.fillInIntent);
                } else if (this.fillInIntent != null) {
                    target.setOnClickListener(new View.OnClickListener() {
                        /* JADX WARNING: type inference failed for: r1v9, types: [android.view.ViewParent] */
                        /* JADX WARNING: Multi-variable type inference failed */
                        public void onClick(View v) {
                            View parent = (View) v.getParent();
                            while (parent != null && !(parent instanceof AdapterView) && (!(parent instanceof AppWidgetHostView) || (parent instanceof RemoteViewsAdapter.RemoteViewsFrameLayout))) {
                                parent = parent.getParent();
                            }
                            if (!(parent instanceof AdapterView)) {
                                Log.e(RemoteViews.LOG_TAG, "Collection item doesn't have AdapterView parent");
                            } else if (!(parent.getTag() instanceof PendingIntent)) {
                                Log.e(RemoteViews.LOG_TAG, "Attempting setOnClickFillInIntent without calling setPendingIntentTemplate on parent.");
                            } else {
                                SetOnClickFillInIntent.this.fillInIntent.setSourceBounds(RemoteViews.getSourceBounds(v));
                                handler.onClickHandler(v, (PendingIntent) parent.getTag(), SetOnClickFillInIntent.this.fillInIntent);
                            }
                        }
                    });
                }
            }
        }

        public int getActionTag() {
            return 9;
        }
    }

    @RCUnownedThisRef
    private class SetOnClickPendingIntent extends Action {
        PendingIntent pendingIntent;

        public SetOnClickPendingIntent(int id, PendingIntent pendingIntent2) {
            super();
            this.viewId = id;
            this.pendingIntent = pendingIntent2;
        }

        public SetOnClickPendingIntent(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            PendingIntent.writePendingIntentOrNullToParcel(this.pendingIntent, dest);
        }

        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (RemoteViews.this.mIsWidgetCollectionChild) {
                    Log.w(RemoteViews.LOG_TAG, "Cannot setOnClickPendingIntent for collection item (id: " + this.viewId + ")");
                    ApplicationInfo appInfo = root.getContext().getApplicationInfo();
                    if (appInfo != null && appInfo.targetSdkVersion >= 16) {
                        return;
                    }
                }
                View.OnClickListener listener = null;
                if (this.pendingIntent != null) {
                    listener = new View.OnClickListener() {
                        public void onClick(View v) {
                            Rect rect = RemoteViews.getSourceBounds(v);
                            Intent intent = new Intent();
                            intent.setSourceBounds(rect);
                            handler.onClickHandler(v, SetOnClickPendingIntent.this.pendingIntent, intent);
                        }
                    };
                }
                target.setTagInternal(16909188, this.pendingIntent);
                target.setOnClickListener(listener);
            }
        }

        public int getActionTag() {
            return 1;
        }
    }

    private class SetPendingIntentTemplate extends Action {
        PendingIntent pendingIntentTemplate;

        public SetPendingIntentTemplate(int id, PendingIntent pendingIntentTemplate2) {
            super();
            this.viewId = id;
            this.pendingIntentTemplate = pendingIntentTemplate2;
        }

        public SetPendingIntentTemplate(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.pendingIntentTemplate = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            PendingIntent.writePendingIntentOrNullToParcel(this.pendingIntentTemplate, dest);
        }

        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (target instanceof AdapterView) {
                    AdapterView<?> av = (AdapterView) target;
                    av.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.Object} */
                        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.content.Intent} */
                        /* JADX WARNING: type inference failed for: r1v5, types: [android.view.View] */
                        /* JADX WARNING: Multi-variable type inference failed */
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (view instanceof ViewGroup) {
                                ViewGroup vg = (ViewGroup) view;
                                int i = 0;
                                if (parent instanceof AdapterViewAnimator) {
                                    vg = vg.getChildAt(0);
                                }
                                if (vg != null) {
                                    Intent fillInIntent = null;
                                    int childCount = vg.getChildCount();
                                    while (true) {
                                        if (i >= childCount) {
                                            break;
                                        }
                                        Object tag = vg.getChildAt(i).getTag(16908897);
                                        if (tag instanceof Intent) {
                                            fillInIntent = tag;
                                            break;
                                        }
                                        i++;
                                    }
                                    if (fillInIntent != null) {
                                        new Intent().setSourceBounds(RemoteViews.getSourceBounds(view));
                                        handler.onClickHandler(view, SetPendingIntentTemplate.this.pendingIntentTemplate, fillInIntent);
                                    }
                                }
                            }
                        }
                    });
                    av.setTag(this.pendingIntentTemplate);
                    return;
                }
                Log.e(RemoteViews.LOG_TAG, "Cannot setPendingIntentTemplate on a view which is notan AdapterView (id: " + this.viewId + ")");
            }
        }

        public int getActionTag() {
            return 8;
        }
    }

    @RCUnownedThisRef
    private class SetRemoteInputsAction extends Action {
        final Parcelable[] remoteInputs;

        public SetRemoteInputsAction(int viewId, RemoteInput[] remoteInputs2) {
            super();
            this.viewId = viewId;
            this.remoteInputs = remoteInputs2;
        }

        public SetRemoteInputsAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.remoteInputs = (Parcelable[]) parcel.createTypedArray(RemoteInput.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeTypedArray(this.remoteInputs, flags);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                target.setTagInternal(16909246, this.remoteInputs);
            }
        }

        public int getActionTag() {
            return 18;
        }
    }

    private class SetRemoteViewsAdapterIntent extends Action {
        Intent intent;
        boolean isAsync = false;

        public SetRemoteViewsAdapterIntent(int id, Intent intent2) {
            super();
            this.viewId = id;
            this.intent = intent2;
        }

        public SetRemoteViewsAdapterIntent(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.intent = (Intent) parcel.readTypedObject(Intent.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeTypedObject(this.intent, flags);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (!(rootParent instanceof AppWidgetHostView)) {
                    Log.e(RemoteViews.LOG_TAG, "SetRemoteViewsAdapterIntent action can only be used for AppWidgets (root id: " + this.viewId + ")");
                } else if ((target instanceof AbsListView) || (target instanceof AdapterViewAnimator)) {
                    this.intent.putExtra(RemoteViews.EXTRA_REMOTEADAPTER_APPWIDGET_ID, ((AppWidgetHostView) rootParent).getAppWidgetId());
                    if (target instanceof AbsListView) {
                        AbsListView v = (AbsListView) target;
                        v.setRemoteViewsAdapter(this.intent, this.isAsync);
                        v.setRemoteViewsOnClickHandler(handler);
                    } else if (target instanceof AdapterViewAnimator) {
                        AdapterViewAnimator v2 = (AdapterViewAnimator) target;
                        v2.setRemoteViewsAdapter(this.intent, this.isAsync);
                        v2.setRemoteViewsOnClickHandler(handler);
                    }
                } else {
                    Log.e(RemoteViews.LOG_TAG, "Cannot setRemoteViewsAdapter on a view which is not an AbsListView or AdapterViewAnimator (id: " + this.viewId + ")");
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            SetRemoteViewsAdapterIntent copy = new SetRemoteViewsAdapterIntent(this.viewId, this.intent);
            copy.isAsync = true;
            return copy;
        }

        public int getActionTag() {
            return 10;
        }
    }

    private class SetRemoteViewsAdapterList extends Action {
        ArrayList<RemoteViews> list;
        int viewTypeCount;

        public SetRemoteViewsAdapterList(int id, ArrayList<RemoteViews> list2, int viewTypeCount2) {
            super();
            this.viewId = id;
            this.list = list2;
            this.viewTypeCount = viewTypeCount2;
        }

        public SetRemoteViewsAdapterList(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.viewTypeCount = parcel.readInt();
            this.list = parcel.createTypedArrayList(RemoteViews.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.viewTypeCount);
            dest.writeTypedList(this.list, flags);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (!(rootParent instanceof AppWidgetHostView)) {
                    Log.e(RemoteViews.LOG_TAG, "SetRemoteViewsAdapterIntent action can only be used for AppWidgets (root id: " + this.viewId + ")");
                } else if ((target instanceof AbsListView) || (target instanceof AdapterViewAnimator)) {
                    if (target instanceof AbsListView) {
                        AbsListView v = (AbsListView) target;
                        ListAdapter adapter = v.getAdapter();
                        if (!(adapter instanceof RemoteViewsListAdapter) || this.viewTypeCount > adapter.getViewTypeCount()) {
                            v.setAdapter((ListAdapter) new RemoteViewsListAdapter(v.getContext(), this.list, this.viewTypeCount));
                        } else {
                            ((RemoteViewsListAdapter) adapter).setViewsList(this.list);
                        }
                    } else if (target instanceof AdapterViewAnimator) {
                        AdapterViewAnimator v2 = (AdapterViewAnimator) target;
                        Adapter a = v2.getAdapter();
                        if (!(a instanceof RemoteViewsListAdapter) || this.viewTypeCount > a.getViewTypeCount()) {
                            v2.setAdapter(new RemoteViewsListAdapter(v2.getContext(), this.list, this.viewTypeCount));
                        } else {
                            ((RemoteViewsListAdapter) a).setViewsList(this.list);
                        }
                    }
                } else {
                    Log.e(RemoteViews.LOG_TAG, "Cannot setRemoteViewsAdapter on a view which is not an AbsListView or AdapterViewAnimator (id: " + this.viewId + ")");
                }
            }
        }

        public int getActionTag() {
            return 15;
        }
    }

    @RCUnownedThisRef
    private class TextViewDrawableAction extends Action {
        int d1;
        int d2;
        int d3;
        int d4;
        boolean drawablesLoaded = false;
        int height;
        Icon i1;
        Icon i2;
        Icon i3;
        Icon i4;
        Drawable id1;
        Drawable id2;
        Drawable id3;
        Drawable id4;
        boolean isRelative = false;
        int padding;
        boolean useIcons = false;
        int width;

        public TextViewDrawableAction(int viewId, boolean isRelative2, int d12, int d22, int d32, int d42) {
            super();
            this.viewId = viewId;
            this.isRelative = isRelative2;
            this.useIcons = false;
            this.d1 = d12;
            this.d2 = d22;
            this.d3 = d32;
            this.d4 = d42;
        }

        public TextViewDrawableAction(int viewId, boolean isRelative2, Icon i12, Icon i22, Icon i32, Icon i42) {
            super();
            this.viewId = viewId;
            this.isRelative = isRelative2;
            this.useIcons = true;
            this.i1 = i12;
            this.i2 = i22;
            this.i3 = i32;
            this.i4 = i42;
        }

        public TextViewDrawableAction(int viewId, boolean isRelative2, Icon i12, Icon i22, Icon i32, Icon i42, int drawableWidth, int drawableHeight, int drawablePadding) {
            super();
            this.viewId = viewId;
            this.isRelative = isRelative2;
            this.useIcons = true;
            this.i1 = i12;
            this.i2 = i22;
            this.i3 = i32;
            this.i4 = i42;
            this.width = drawableWidth;
            this.height = drawableHeight;
            this.padding = drawablePadding;
        }

        public TextViewDrawableAction(Parcel parcel) {
            super();
            boolean z = false;
            this.viewId = parcel.readInt();
            this.isRelative = parcel.readInt() != 0;
            this.useIcons = parcel.readInt() != 0 ? true : z;
            if (this.useIcons) {
                this.i1 = (Icon) parcel.readTypedObject(Icon.CREATOR);
                this.i2 = (Icon) parcel.readTypedObject(Icon.CREATOR);
                this.i3 = (Icon) parcel.readTypedObject(Icon.CREATOR);
                this.i4 = (Icon) parcel.readTypedObject(Icon.CREATOR);
                return;
            }
            this.d1 = parcel.readInt();
            this.d2 = parcel.readInt();
            this.d3 = parcel.readInt();
            this.d4 = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.isRelative ? 1 : 0);
            dest.writeInt(this.useIcons ? 1 : 0);
            if (this.useIcons) {
                dest.writeTypedObject(this.i1, 0);
                dest.writeTypedObject(this.i2, 0);
                dest.writeTypedObject(this.i3, 0);
                dest.writeTypedObject(this.i4, 0);
                return;
            }
            dest.writeInt(this.d1);
            dest.writeInt(this.d2);
            dest.writeInt(this.d3);
            dest.writeInt(this.d4);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target != null) {
                if (this.drawablesLoaded) {
                    if (!this.isRelative) {
                        target.setCompoundDrawablesWithIntrinsicBounds(this.id1, this.id2, this.id3, this.id4);
                    } else if (this.width == 0 || this.height == 0) {
                        target.setCompoundDrawablesRelativeWithIntrinsicBounds(this.id1, this.id2, this.id3, this.id4);
                    } else {
                        setCompoundDrawablesRelativeBoundsForced();
                        target.setCompoundDrawablesRelative(this.id1, this.id2, this.id3, this.id4);
                        target.setCompoundDrawablePadding(this.padding);
                    }
                } else if (this.useIcons) {
                    Context ctx = target.getContext();
                    Drawable id42 = null;
                    Drawable id12 = this.i1 == null ? null : this.i1.loadDrawable(ctx);
                    Drawable id22 = this.i2 == null ? null : this.i2.loadDrawable(ctx);
                    Drawable id32 = this.i3 == null ? null : this.i3.loadDrawable(ctx);
                    if (this.i4 != null) {
                        id42 = this.i4.loadDrawable(ctx);
                    }
                    if (!this.isRelative) {
                        target.setCompoundDrawablesWithIntrinsicBounds(id12, id22, id32, id42);
                    } else if (this.width == 0 || this.height == 0) {
                        target.setCompoundDrawablesRelativeWithIntrinsicBounds(id12, id22, id32, id42);
                    } else {
                        setCompoundDrawablesRelativeBoundsForced();
                        target.setCompoundDrawablesRelative(id12, id22, id32, id42);
                        target.setCompoundDrawablePadding(this.padding);
                    }
                } else if (this.isRelative) {
                    target.setCompoundDrawablesRelativeWithIntrinsicBounds(this.d1, this.d2, this.d3, this.d4);
                } else {
                    target.setCompoundDrawablesWithIntrinsicBounds(this.d1, this.d2, this.d3, this.d4);
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            TextViewDrawableAction copy;
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target == null) {
                return RemoteViews.ACTION_NOOP;
            }
            if (this.useIcons) {
                copy = new TextViewDrawableAction(this.viewId, this.isRelative, this.i1, this.i2, this.i3, this.i4, this.width, this.height, this.padding);
            } else {
                copy = new TextViewDrawableAction(this.viewId, this.isRelative, this.d1, this.d2, this.d3, this.d4);
            }
            copy.drawablesLoaded = true;
            Context ctx = target.getContext();
            Drawable drawable = null;
            if (this.useIcons) {
                copy.id1 = this.i1 == null ? null : this.i1.loadDrawable(ctx);
                copy.id2 = this.i2 == null ? null : this.i2.loadDrawable(ctx);
                copy.id3 = this.i3 == null ? null : this.i3.loadDrawable(ctx);
                if (this.i4 != null) {
                    drawable = this.i4.loadDrawable(ctx);
                }
                copy.id4 = drawable;
            } else {
                copy.id1 = this.d1 == 0 ? null : ctx.getDrawable(this.d1);
                copy.id2 = this.d2 == 0 ? null : ctx.getDrawable(this.d2);
                copy.id3 = this.d3 == 0 ? null : ctx.getDrawable(this.d3);
                if (this.d4 != 0) {
                    drawable = ctx.getDrawable(this.d4);
                }
                copy.id4 = drawable;
            }
            return copy;
        }

        public boolean prefersAsyncApply() {
            return this.useIcons;
        }

        public int getActionTag() {
            return 11;
        }

        private void setCompoundDrawablesRelativeBoundsForced() {
            if (this.width != 0 && this.height != 0) {
                if (this.id1 != null) {
                    this.id1.setBounds(0, 0, this.width, this.height);
                }
                if (this.id2 != null) {
                    this.id2.setBounds(0, 0, this.width, this.height);
                }
                if (this.id3 != null) {
                    this.id3.setBounds(0, 0, this.width, this.height);
                }
                if (this.id4 != null) {
                    this.id4.setBounds(0, 0, this.width, this.height);
                }
            }
        }

        public void visitUris(Consumer<Uri> visitor) {
            if (this.useIcons) {
                RemoteViews.visitIconUri(this.i1, visitor);
                RemoteViews.visitIconUri(this.i2, visitor);
                RemoteViews.visitIconUri(this.i3, visitor);
                RemoteViews.visitIconUri(this.i4, visitor);
            }
        }
    }

    @RCUnownedThisRef
    private class TextViewSizeAction extends Action {
        float size;
        int units;

        public TextViewSizeAction(int viewId, int units2, float size2) {
            super();
            this.viewId = viewId;
            this.units = units2;
            this.size = size2;
        }

        public TextViewSizeAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.units = parcel.readInt();
            this.size = parcel.readFloat();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.units);
            dest.writeFloat(this.size);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target != null) {
                target.setTextSize(this.units, this.size);
            }
        }

        public int getActionTag() {
            return 13;
        }
    }

    private final class ViewContentNavigation extends Action {
        final boolean mNext;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: android.widget.RemoteViews.ViewContentNavigation.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: boot-framework_classes2.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: android.widget.RemoteViews.ViewContentNavigation.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: boot-framework_classes2.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewContentNavigation.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        ViewContentNavigation(int viewId, boolean next) {
            super();
            this.viewId = viewId;
            this.mNext = next;
        }

        ViewContentNavigation(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.mNext = in.readBoolean();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.viewId);
            out.writeBoolean(this.mNext);
        }

        public int mergeBehavior() {
            return 2;
        }

        public int getActionTag() {
            return 5;
        }
    }

    @RCUnownedThisRef
    private class ViewGroupActionAdd extends Action {
        /* access modifiers changed from: private */
        public int mIndex;
        private RemoteViews mNestedViews;

        ViewGroupActionAdd(RemoteViews remoteViews, int viewId, RemoteViews nestedViews) {
            this(viewId, nestedViews, -1);
        }

        ViewGroupActionAdd(int viewId, RemoteViews nestedViews, int index) {
            super();
            this.viewId = viewId;
            this.mNestedViews = nestedViews;
            this.mIndex = index;
            if (nestedViews != null) {
                RemoteViews.this.configureRemoteViewsAsChild(nestedViews);
            }
        }

        ViewGroupActionAdd(Parcel parcel, BitmapCache bitmapCache, ApplicationInfo info, int depth, Map<Class, Object> classCookies) {
            super();
            this.viewId = parcel.readInt();
            this.mIndex = parcel.readInt();
            RemoteViews remoteViews = new RemoteViews(parcel, bitmapCache, info, depth, classCookies);
            this.mNestedViews = remoteViews;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.mIndex);
            this.mNestedViews.writeToParcel(dest, flags);
        }

        public boolean hasSameAppInfo(ApplicationInfo parentInfo) {
            return this.mNestedViews.hasSameAppInfo(parentInfo);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            Context context = root.getContext();
            ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
            if (target != null) {
                this.mNestedViews.setUseAppContext(RemoteViews.this.mUseAppContext);
                target.addView(this.mNestedViews.apply(context, target, handler), this.mIndex);
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            root.createTree();
            ViewTree target = root.findViewTreeById(this.viewId);
            if (target == null || !(target.mRoot instanceof ViewGroup)) {
                return RemoteViews.ACTION_NOOP;
            }
            final ViewGroup targetVg = (ViewGroup) target.mRoot;
            final AsyncApplyTask task = this.mNestedViews.getAsyncApplyTask(root.mRoot.getContext(), targetVg, null, handler);
            final ViewTree tree = task.doInBackground(new Void[0]);
            if (tree != null) {
                target.addChild(tree, this.mIndex);
                return new RuntimeAction() {
                    public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
                        task.onPostExecute(tree);
                        targetVg.addView(task.mResult, ViewGroupActionAdd.this.mIndex);
                    }
                };
            }
            throw new ActionException(task.mError);
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            this.mNestedViews.setBitmapCache(bitmapCache);
        }

        public int mergeBehavior() {
            return 1;
        }

        public boolean prefersAsyncApply() {
            return this.mNestedViews.prefersAsyncApply();
        }

        public int getActionTag() {
            return 4;
        }
    }

    @RCUnownedThisRef
    private class ViewGroupActionRemove extends Action {
        private static final int REMOVE_ALL_VIEWS_ID = -2;
        /* access modifiers changed from: private */
        public int mViewIdToKeep;

        ViewGroupActionRemove(RemoteViews remoteViews, int viewId) {
            this(viewId, -2);
        }

        ViewGroupActionRemove(int viewId, int viewIdToKeep) {
            super();
            this.viewId = viewId;
            this.mViewIdToKeep = viewIdToKeep;
        }

        ViewGroupActionRemove(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.mViewIdToKeep = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.mViewIdToKeep);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
            if (target != null) {
                if (this.mViewIdToKeep == -2) {
                    target.removeAllViews();
                } else {
                    removeAllViewsExceptIdToKeep(target);
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            root.createTree();
            ViewTree target = root.findViewTreeById(this.viewId);
            if (target == null || !(target.mRoot instanceof ViewGroup)) {
                return RemoteViews.ACTION_NOOP;
            }
            final ViewGroup targetVg = (ViewGroup) target.mRoot;
            ArrayList unused = target.mChildren = null;
            return new RuntimeAction() {
                public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
                    if (ViewGroupActionRemove.this.mViewIdToKeep == -2) {
                        targetVg.removeAllViews();
                    } else {
                        ViewGroupActionRemove.this.removeAllViewsExceptIdToKeep(targetVg);
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        public void removeAllViewsExceptIdToKeep(ViewGroup viewGroup) {
            for (int index = viewGroup.getChildCount() - 1; index >= 0; index--) {
                if (viewGroup.getChildAt(index).getId() != this.mViewIdToKeep) {
                    viewGroup.removeViewAt(index);
                }
            }
        }

        public int getActionTag() {
            return 7;
        }

        public int mergeBehavior() {
            return 1;
        }
    }

    @RCUnownedThisRef
    private class ViewPaddingAction extends Action {
        int bottom;
        int left;
        int right;
        int top;

        public ViewPaddingAction(int viewId, int left2, int top2, int right2, int bottom2) {
            super();
            this.viewId = viewId;
            this.left = left2;
            this.top = top2;
            this.right = right2;
            this.bottom = bottom2;
        }

        public ViewPaddingAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.left = parcel.readInt();
            this.top = parcel.readInt();
            this.right = parcel.readInt();
            this.bottom = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.left);
            dest.writeInt(this.top);
            dest.writeInt(this.right);
            dest.writeInt(this.bottom);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                target.setPadding(this.left, this.top, this.right, this.bottom);
            }
        }

        public int getActionTag() {
            return 14;
        }
    }

    private static class ViewTree {
        private static final int INSERT_AT_END_INDEX = -1;
        /* access modifiers changed from: private */
        public ArrayList<ViewTree> mChildren;
        /* access modifiers changed from: private */
        public View mRoot;

        private ViewTree(View root) {
            this.mRoot = root;
        }

        public void createTree() {
            if (this.mChildren == null) {
                this.mChildren = new ArrayList<>();
                if (this.mRoot instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) this.mRoot;
                    int count = vg.getChildCount();
                    for (int i = 0; i < count; i++) {
                        addViewChild(vg.getChildAt(i));
                    }
                }
            }
        }

        public ViewTree findViewTreeById(int id) {
            if (this.mRoot.getId() == id) {
                return this;
            }
            if (this.mChildren == null) {
                return null;
            }
            Iterator<ViewTree> it = this.mChildren.iterator();
            while (it.hasNext()) {
                ViewTree result = it.next().findViewTreeById(id);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        public void replaceView(View v) {
            this.mRoot = v;
            this.mChildren = null;
            createTree();
        }

        public <T extends View> T findViewById(int id) {
            if (this.mChildren == null) {
                return this.mRoot.findViewById(id);
            }
            ViewTree tree = findViewTreeById(id);
            return tree == null ? null : tree.mRoot;
        }

        public void addChild(ViewTree child) {
            addChild(child, -1);
        }

        public void addChild(ViewTree child, int index) {
            if (this.mChildren == null) {
                this.mChildren = new ArrayList<>();
            }
            child.createTree();
            if (index == -1) {
                this.mChildren.add(child);
            } else {
                this.mChildren.add(index, child);
            }
        }

        private void addViewChild(View v) {
            ViewTree tree;
            if (!v.isRootNamespace()) {
                if (v.getId() != 0) {
                    tree = new ViewTree(v);
                    this.mChildren.add(tree);
                } else {
                    tree = this;
                }
                if ((v instanceof ViewGroup) && tree.mChildren == null) {
                    tree.mChildren = new ArrayList<>();
                    ViewGroup vg = (ViewGroup) v;
                    int count = vg.getChildCount();
                    for (int i = 0; i < count; i++) {
                        tree.addViewChild(vg.getChildAt(i));
                    }
                }
            }
        }
    }

    public void setRemoteInputs(int viewId, RemoteInput[] remoteInputs) {
        this.mActions.add(new SetRemoteInputsAction(viewId, remoteInputs));
    }

    public void reduceImageSizes(int maxWidth, int maxHeight) {
        ArrayList<Bitmap> cache = this.mBitmapCache.mBitmaps;
        int cacheSize = cache.size();
        for (int i = 0; i < cacheSize; i++) {
            cache.set(i, Icon.scaleDownIfNecessary(cache.get(i), maxWidth, maxHeight));
        }
    }

    public void overrideTextColors(int textColor) {
        addAction(new OverrideTextColorsAction(textColor));
    }

    public void setReapplyDisallowed() {
        this.mReapplyDisallowed = true;
    }

    public boolean isReapplyDisallowed() {
        return this.mReapplyDisallowed;
    }

    public void mergeRemoteViews(RemoteViews newRv) {
        if (newRv != null) {
            RemoteViews copy = new RemoteViews(newRv);
            HashMap<String, Action> map = new HashMap<>();
            if (this.mActions == null) {
                this.mActions = new ArrayList<>();
            }
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                Action a = this.mActions.get(i);
                map.put(a.getUniqueKey(), a);
            }
            ArrayList<Action> newActions = copy.mActions;
            if (newActions != null) {
                int count2 = newActions.size();
                for (int i2 = 0; i2 < count2; i2++) {
                    Action a2 = newActions.get(i2);
                    String key = newActions.get(i2).getUniqueKey();
                    int mergeBehavior = newActions.get(i2).mergeBehavior();
                    if (map.containsKey(key) && mergeBehavior == 0) {
                        this.mActions.remove(map.get(key));
                        map.remove(key);
                    }
                    if (mergeBehavior == 0 || mergeBehavior == 1) {
                        this.mActions.add(a2);
                    }
                }
                this.mBitmapCache = new BitmapCache();
                setBitmapCache(this.mBitmapCache);
            }
        }
    }

    public void visitUris(Consumer<Uri> visitor) {
        if (this.mActions != null) {
            for (int i = 0; i < this.mActions.size(); i++) {
                this.mActions.get(i).visitUris(visitor);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void visitIconUri(Icon icon, Consumer<Uri> visitor) {
        if (icon != null && icon.getType() == 4) {
            visitor.accept(icon.getUri());
        }
    }

    /* access modifiers changed from: private */
    public static Rect getSourceBounds(View v) {
        float appScale = v.getContext().getResources().getCompatibilityInfo().applicationScale;
        int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        Rect rect = new Rect();
        rect.left = (int) ((((float) pos[0]) * appScale) + 0.5f);
        rect.top = (int) ((((float) pos[1]) * appScale) + 0.5f);
        rect.right = (int) ((((float) (pos[0] + v.getWidth())) * appScale) + 0.5f);
        rect.bottom = (int) ((((float) (pos[1] + v.getHeight())) * appScale) + 0.5f);
        return rect;
    }

    /* access modifiers changed from: private */
    public MethodHandle getMethod(View view, String methodName, Class<?> paramType, boolean async) {
        MethodType asyncType;
        Method method;
        Class<?> cls = view.getClass();
        synchronized (sMethods) {
            sLookupKey.set(cls, paramType, methodName);
            MethodArgs result = sMethods.get(sLookupKey);
            if (result == null) {
                if (paramType == null) {
                    try {
                        method = cls.getMethod(methodName, new Class[0]);
                    } catch (IllegalAccessException | NoSuchMethodException e) {
                        throw new ActionException("Async implementation declared as " + result.asyncMethodName + " but not defined for " + methodName + ": public Runnable " + result.asyncMethodName + " (" + TextUtils.join((CharSequence) ",", (Object[]) asyncType.parameterArray()) + ")");
                    } catch (IllegalAccessException | NoSuchMethodException e2) {
                        throw new ActionException("view: " + cls.getName() + " doesn't have method: " + methodName + getParameters(paramType));
                    }
                } else {
                    method = cls.getMethod(methodName, new Class[]{paramType});
                }
                if (method.isAnnotationPresent(RemotableViewMethod.class)) {
                    result = new MethodArgs();
                    result.syncMethod = MethodHandles.publicLookup().unreflect(method);
                    result.asyncMethodName = ((RemotableViewMethod) method.getAnnotation(RemotableViewMethod.class)).asyncImpl();
                    MethodKey key = new MethodKey();
                    key.set(cls, paramType, methodName);
                    sMethods.put(key, result);
                } else {
                    throw new ActionException("view: " + cls.getName() + " can't use method with RemoteViews: " + methodName + getParameters(paramType));
                }
            }
            if (!async) {
                MethodHandle methodHandle = result.syncMethod;
                return methodHandle;
            } else if (result.asyncMethodName.isEmpty()) {
                return null;
            } else {
                if (result.asyncMethod == null) {
                    asyncType = result.syncMethod.type().dropParameterTypes(0, 1).changeReturnType(Runnable.class);
                    result.asyncMethod = MethodHandles.publicLookup().findVirtual(cls, result.asyncMethodName, asyncType);
                }
                MethodHandle methodHandle2 = result.asyncMethod;
                return methodHandle2;
            }
        }
    }

    /* access modifiers changed from: private */
    public Method getMapleMethod(View view, String methodName, Class<?> paramType) {
        Method method;
        Class<?> cls = view.getClass();
        synchronized (sMethodsLock) {
            ArrayMap<MutablePair<String, Class<?>>, Method> methods = sMapleMethods.get(cls);
            if (methods == null) {
                methods = new ArrayMap<>();
                sMapleMethods.put(cls, methods);
            }
            this.mPair.first = methodName;
            this.mPair.second = paramType;
            method = methods.get(this.mPair);
            if (method == null) {
                if (paramType == null) {
                    try {
                        method = cls.getMethod(methodName, new Class[0]);
                    } catch (NoSuchMethodException e) {
                        throw new ActionException("view: " + cls.getName() + " doesn't have method: " + methodName + getParameters(paramType));
                    }
                } else {
                    method = cls.getMethod(methodName, new Class[]{paramType});
                }
                if (method.isAnnotationPresent(RemotableViewMethod.class)) {
                    methods.put(new MutablePair(methodName, paramType), method);
                } else {
                    throw new ActionException("view: " + cls.getName() + " can't use method with RemoteViews: " + methodName + getParameters(paramType));
                }
            }
        }
        return method;
    }

    /* access modifiers changed from: private */
    public Method getMapleAsyncMethod(Method method) {
        synchronized (sMapleAsyncMethods) {
            int valueIndex = sMapleAsyncMethods.indexOfKey(method);
            if (valueIndex >= 0) {
                Method valueAt = sMapleAsyncMethods.valueAt(valueIndex);
                return valueAt;
            }
            RemotableViewMethod annotation = (RemotableViewMethod) method.getAnnotation(RemotableViewMethod.class);
            Method asyncMethod = null;
            if (!annotation.asyncImpl().isEmpty()) {
                try {
                    asyncMethod = method.getDeclaringClass().getMethod(annotation.asyncImpl(), method.getParameterTypes());
                    if (!asyncMethod.getReturnType().equals(Runnable.class)) {
                        throw new ActionException("Async implementation for " + method.getName() + " does not return a Runnable");
                    }
                } catch (NoSuchMethodException e) {
                    throw new ActionException("Async implementation declared but not defined for " + method.getName());
                }
            }
            sMapleAsyncMethods.put(method, asyncMethod);
            return asyncMethod;
        }
    }

    /* access modifiers changed from: private */
    public static Object[] wrapArg(Object value) {
        Object[] args = sMapleInvokeArgsTls.get();
        args[0] = value;
        return args;
    }

    private static String getParameters(Class<?> paramType) {
        if (paramType == null) {
            return "()";
        }
        return "(" + paramType + ")";
    }

    /* access modifiers changed from: private */
    public void configureRemoteViewsAsChild(RemoteViews rv) {
        rv.setBitmapCache(this.mBitmapCache);
        rv.setNotRoot();
    }

    /* access modifiers changed from: package-private */
    public void setNotRoot() {
        this.mIsRoot = false;
    }

    public RemoteViews(String packageName, int layoutId) {
        this(getApplicationInfo(packageName, UserHandle.myUserId()), layoutId);
    }

    public RemoteViews(String packageName, int userId, int layoutId) {
        this(getApplicationInfo(packageName, userId), layoutId);
    }

    protected RemoteViews(ApplicationInfo application, int layoutId) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mSignSet = new HashSet();
        this.mIsWidgetCollectionChild = false;
        this.mUseAppContext = false;
        this.mPair = new MutablePair<>(null, null);
        this.mApplication = application;
        this.mLayoutId = layoutId;
        this.mBitmapCache = new BitmapCache();
        this.mClassCookies = null;
    }

    private boolean hasLandscapeAndPortraitLayouts() {
        return (this.mLandscape == null || this.mPortrait == null) ? false : true;
    }

    public RemoteViews(RemoteViews landscape, RemoteViews portrait) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mSignSet = new HashSet();
        this.mIsWidgetCollectionChild = false;
        this.mUseAppContext = false;
        this.mPair = new MutablePair<>(null, null);
        if (landscape == null || portrait == null) {
            throw new RuntimeException("Both RemoteViews must be non-null");
        } else if (landscape.hasSameAppInfo(portrait.mApplication)) {
            this.mApplication = portrait.mApplication;
            this.mLayoutId = portrait.getLayoutId();
            this.mLandscape = landscape;
            this.mPortrait = portrait;
            this.mBitmapCache = new BitmapCache();
            configureRemoteViewsAsChild(landscape);
            configureRemoteViewsAsChild(portrait);
            this.mClassCookies = portrait.mClassCookies != null ? portrait.mClassCookies : landscape.mClassCookies;
        } else {
            throw new RuntimeException("Both RemoteViews must share the same package and user");
        }
    }

    public RemoteViews(RemoteViews src) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mSignSet = new HashSet();
        this.mIsWidgetCollectionChild = false;
        this.mUseAppContext = false;
        this.mPair = new MutablePair<>(null, null);
        this.mBitmapCache = src.mBitmapCache;
        this.mApplication = src.mApplication;
        this.mIsRoot = src.mIsRoot;
        this.mLayoutId = src.mLayoutId;
        this.mIsWidgetCollectionChild = src.mIsWidgetCollectionChild;
        this.mReapplyDisallowed = src.mReapplyDisallowed;
        this.mClassCookies = src.mClassCookies;
        if (src.hasLandscapeAndPortraitLayouts()) {
            this.mLandscape = new RemoteViews(src.mLandscape);
            this.mPortrait = new RemoteViews(src.mPortrait);
        }
        if (src.mActions != null) {
            Parcel p = Parcel.obtain();
            p.putClassCookies(this.mClassCookies);
            src.writeActionsToParcel(p);
            p.setDataPosition(0);
            readActionsFromParcel(p, 0);
            p.recycle();
        }
        setBitmapCache(new BitmapCache());
    }

    public RemoteViews(Parcel parcel) {
        this(parcel, null, null, 0, null);
    }

    private RemoteViews(Parcel parcel, BitmapCache bitmapCache, ApplicationInfo info, int depth, Map<Class, Object> classCookies) {
        boolean z = true;
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mSignSet = new HashSet();
        this.mIsWidgetCollectionChild = false;
        this.mUseAppContext = false;
        this.mPair = new MutablePair<>(null, null);
        if (depth <= 10 || UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            int depth2 = depth + 1;
            int mode = parcel.readInt();
            if (bitmapCache == null) {
                this.mBitmapCache = new BitmapCache(parcel);
                this.mClassCookies = parcel.copyClassCookies();
            } else {
                setBitmapCache(bitmapCache);
                this.mClassCookies = classCookies;
                setNotRoot();
            }
            if (mode == 0) {
                this.mApplication = parcel.readInt() == 0 ? info : (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(parcel);
                this.mLayoutId = parcel.readInt();
                this.mIsWidgetCollectionChild = parcel.readInt() == 1;
                readActionsFromParcel(parcel, depth2);
            } else {
                Parcel parcel2 = parcel;
                int i = depth2;
                RemoteViews remoteViews = new RemoteViews(parcel2, this.mBitmapCache, info, i, this.mClassCookies);
                this.mLandscape = remoteViews;
                RemoteViews remoteViews2 = new RemoteViews(parcel2, this.mBitmapCache, this.mLandscape.mApplication, i, this.mClassCookies);
                this.mPortrait = remoteViews2;
                this.mApplication = this.mPortrait.mApplication;
                this.mLayoutId = this.mPortrait.getLayoutId();
            }
            this.mReapplyDisallowed = parcel.readInt() != 0 ? false : z;
            return;
        }
        throw new IllegalArgumentException("Too many nested views.");
    }

    private void readActionsFromParcel(Parcel parcel, int depth) {
        int count = parcel.readInt();
        if (count > 0) {
            this.mActions = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                this.mActions.add(getActionFromParcel(parcel, depth));
            }
        }
    }

    private Action getActionFromParcel(Parcel parcel, int depth) {
        int tag = parcel.readInt();
        switch (tag) {
            case 1:
                return new SetOnClickPendingIntent(parcel);
            case 2:
                return new ReflectionAction(parcel);
            case 3:
                return new SetDrawableTint(parcel);
            case 4:
                ViewGroupActionAdd viewGroupActionAdd = new ViewGroupActionAdd(parcel, this.mBitmapCache, this.mApplication, depth, this.mClassCookies);
                return viewGroupActionAdd;
            case 5:
                return new ViewContentNavigation(parcel);
            case 6:
                return new SetEmptyView(parcel);
            case 7:
                return new ViewGroupActionRemove(parcel);
            case 8:
                return new SetPendingIntentTemplate(parcel);
            case 9:
                return new SetOnClickFillInIntent(parcel);
            case 10:
                return new SetRemoteViewsAdapterIntent(parcel);
            case 11:
                return new TextViewDrawableAction(parcel);
            case 12:
                return new BitmapReflectionAction(parcel);
            case 13:
                return new TextViewSizeAction(parcel);
            case 14:
                return new ViewPaddingAction(parcel);
            case 15:
                return new SetRemoteViewsAdapterList(parcel);
            case 18:
                return new SetRemoteInputsAction(parcel);
            case 19:
                return new LayoutParamAction(parcel);
            case 20:
                return new OverrideTextColorsAction(parcel);
            default:
                throw new ActionException("Tag " + tag + " not found");
        }
    }

    @Deprecated
    public RemoteViews clone() {
        Preconditions.checkState(this.mIsRoot, "RemoteView has been attached to another RemoteView. May only clone the root of a RemoteView hierarchy.");
        return new RemoteViews(this);
    }

    public String getPackage() {
        if (this.mApplication != null) {
            return this.mApplication.packageName;
        }
        return null;
    }

    public int getLayoutId() {
        return this.mLayoutId;
    }

    /* access modifiers changed from: package-private */
    public void setIsWidgetCollectionChild(boolean isWidgetCollectionChild) {
        this.mIsWidgetCollectionChild = isWidgetCollectionChild;
    }

    /* access modifiers changed from: private */
    public void setBitmapCache(BitmapCache bitmapCache) {
        this.mBitmapCache = bitmapCache;
        if (hasLandscapeAndPortraitLayouts()) {
            this.mLandscape.setBitmapCache(bitmapCache);
            this.mPortrait.setBitmapCache(bitmapCache);
        } else if (this.mActions != null) {
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                this.mActions.get(i).setBitmapCache(bitmapCache);
            }
        }
    }

    public int estimateMemoryUsage() {
        return this.mBitmapCache.getBitmapMemory();
    }

    private void addAction(Action a) {
        if (!hasLandscapeAndPortraitLayouts()) {
            if (this.mActions == null) {
                this.mActions = new ArrayList<>();
            }
            this.mActions.add(a);
            return;
        }
        throw new RuntimeException("RemoteViews specifying separate landscape and portrait layouts cannot be modified. Instead, fully configure the landscape and portrait layouts individually before constructing the combined layout.");
    }

    public void addView(int viewId, RemoteViews nestedView) {
        Action action;
        if (nestedView == null) {
            action = new ViewGroupActionRemove(this, viewId);
        } else {
            action = new ViewGroupActionAdd(this, viewId, nestedView);
        }
        addAction(action);
    }

    public void addView(int viewId, RemoteViews nestedView, int index) {
        addAction(new ViewGroupActionAdd(viewId, nestedView, index));
    }

    public void removeAllViews(int viewId) {
        addAction(new ViewGroupActionRemove(this, viewId));
    }

    public void removeAllViewsExceptId(int viewId, int viewIdToKeep) {
        addAction(new ViewGroupActionRemove(viewId, viewIdToKeep));
    }

    public void showNext(int viewId) {
        addAction(new ViewContentNavigation(viewId, true));
    }

    public void showPrevious(int viewId) {
        addAction(new ViewContentNavigation(viewId, false));
    }

    public void setDisplayedChild(int viewId, int childIndex) {
        setInt(viewId, "setDisplayedChild", childIndex);
    }

    public void setViewVisibility(int viewId, int visibility) {
        setInt(viewId, "setVisibility", visibility);
    }

    public void setTextViewText(int viewId, CharSequence text) {
        setCharSequence(viewId, "setText", text);
    }

    public void setTextViewTextSize(int viewId, int units, float size) {
        addAction(new TextViewSizeAction(viewId, units, size));
    }

    public void setTextViewCompoundDrawables(int viewId, int left, int top, int right, int bottom) {
        TextViewDrawableAction textViewDrawableAction = new TextViewDrawableAction(viewId, false, left, top, right, bottom);
        addAction(textViewDrawableAction);
    }

    public void setTextViewCompoundDrawablesRelative(int viewId, int start, int top, int end, int bottom) {
        TextViewDrawableAction textViewDrawableAction = new TextViewDrawableAction(viewId, true, start, top, end, bottom);
        addAction(textViewDrawableAction);
    }

    public void setTextViewCompoundDrawables(int viewId, Icon left, Icon top, Icon right, Icon bottom) {
        TextViewDrawableAction textViewDrawableAction = new TextViewDrawableAction(viewId, false, left, top, right, bottom);
        addAction(textViewDrawableAction);
    }

    public void setTextViewCompoundDrawablesWithBounds(int viewId, Icon left, Icon top, Icon right, Icon bottom, int width, int height, int padding) {
        TextViewDrawableAction textViewDrawableAction = new TextViewDrawableAction(viewId, true, left, top, right, bottom, width, height, padding);
        addAction(textViewDrawableAction);
    }

    public void setTextViewCompoundDrawablesRelative(int viewId, Icon start, Icon top, Icon end, Icon bottom) {
        TextViewDrawableAction textViewDrawableAction = new TextViewDrawableAction(viewId, true, start, top, end, bottom);
        addAction(textViewDrawableAction);
    }

    public void setImageViewResource(int viewId, int srcId) {
        setInt(viewId, "setImageResource", srcId);
    }

    public void setImageViewUri(int viewId, Uri uri) {
        setUri(viewId, "setImageURI", uri);
    }

    public void setImageViewBitmap(int viewId, Bitmap bitmap) {
        setBitmap(viewId, "setImageBitmap", bitmap);
    }

    public void setImageViewIcon(int viewId, Icon icon) {
        setIcon(viewId, "setImageIcon", icon);
    }

    public void setEmptyView(int viewId, int emptyViewId) {
        addAction(new SetEmptyView(viewId, emptyViewId));
    }

    public void setChronometer(int viewId, long base, String format, boolean started) {
        setLong(viewId, "setBase", base);
        setString(viewId, "setFormat", format);
        setBoolean(viewId, "setStarted", started);
    }

    public void setChronometerCountDown(int viewId, boolean isCountDown) {
        setBoolean(viewId, "setCountDown", isCountDown);
    }

    public void setProgressBar(int viewId, int max, int progress, boolean indeterminate) {
        setBoolean(viewId, "setIndeterminate", indeterminate);
        if (!indeterminate) {
            setInt(viewId, "setMax", max);
            setInt(viewId, "setProgress", progress);
        }
    }

    public void setOnClickPendingIntent(int viewId, PendingIntent pendingIntent) {
        addAction(new SetOnClickPendingIntent(viewId, pendingIntent));
    }

    public void setPendingIntentTemplate(int viewId, PendingIntent pendingIntentTemplate) {
        addAction(new SetPendingIntentTemplate(viewId, pendingIntentTemplate));
    }

    public void setOnClickFillInIntent(int viewId, Intent fillInIntent) {
        addAction(new SetOnClickFillInIntent(viewId, fillInIntent));
    }

    public void setDrawableTint(int viewId, boolean targetBackground, int colorFilter, PorterDuff.Mode mode) {
        SetDrawableTint setDrawableTint = new SetDrawableTint(viewId, targetBackground, colorFilter, mode);
        addAction(setDrawableTint);
    }

    public void setProgressTintList(int viewId, ColorStateList tint) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, "setProgressTintList", 15, tint);
        addAction(reflectionAction);
    }

    public void setProgressBackgroundTintList(int viewId, ColorStateList tint) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, "setProgressBackgroundTintList", 15, tint);
        addAction(reflectionAction);
    }

    public void setProgressIndeterminateTintList(int viewId, ColorStateList tint) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, "setIndeterminateTintList", 15, tint);
        addAction(reflectionAction);
    }

    public void setTextColor(int viewId, int color) {
        setInt(viewId, "setTextColor", color);
    }

    public void setTextColor(int viewId, ColorStateList colors) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, "setTextColor", 15, colors);
        addAction(reflectionAction);
    }

    @Deprecated
    public void setRemoteAdapter(int appWidgetId, int viewId, Intent intent) {
        setRemoteAdapter(viewId, intent);
    }

    public void setRemoteAdapter(int viewId, Intent intent) {
        addAction(new SetRemoteViewsAdapterIntent(viewId, intent));
    }

    public void setRemoteAdapter(int viewId, ArrayList<RemoteViews> list, int viewTypeCount) {
        addAction(new SetRemoteViewsAdapterList(viewId, list, viewTypeCount));
    }

    public void setScrollPosition(int viewId, int position) {
        setInt(viewId, "smoothScrollToPosition", position);
    }

    public void setRelativeScrollPosition(int viewId, int offset) {
        setInt(viewId, "smoothScrollByOffset", offset);
    }

    public void setViewPadding(int viewId, int left, int top, int right, int bottom) {
        ViewPaddingAction viewPaddingAction = new ViewPaddingAction(viewId, left, top, right, bottom);
        addAction(viewPaddingAction);
    }

    public void setViewLayoutMarginEndDimen(int viewId, int endMarginDimen) {
        addAction(new LayoutParamAction(viewId, 1, endMarginDimen));
    }

    public void setViewLayoutMarginEnd(int viewId, int endMargin) {
        addAction(new LayoutParamAction(viewId, 4, endMargin));
    }

    public void setViewLayoutMarginBottomDimen(int viewId, int bottomMarginDimen) {
        addAction(new LayoutParamAction(viewId, 3, bottomMarginDimen));
    }

    public void setViewLayoutMarginTopDimen(int viewId, int topMarginDimen) {
        addAction(new LayoutParamAction(viewId, 5, topMarginDimen));
    }

    public void setViewLayoutWidth(int viewId, int layoutWidth) {
        if (layoutWidth == 0 || layoutWidth == -1 || layoutWidth == -2) {
            this.mActions.add(new LayoutParamAction(viewId, 2, layoutWidth));
            return;
        }
        throw new IllegalArgumentException("Only supports 0, WRAP_CONTENT and MATCH_PARENT");
    }

    public void setBoolean(int viewId, String methodName, boolean value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 1, Boolean.valueOf(value));
        addAction(reflectionAction);
    }

    public void setUseAppContext(boolean useAppContext) {
        this.mUseAppContext = useAppContext;
    }

    public void setByte(int viewId, String methodName, byte value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 2, Byte.valueOf(value));
        addAction(reflectionAction);
    }

    public void setShort(int viewId, String methodName, short value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 3, Short.valueOf(value));
        addAction(reflectionAction);
    }

    public void setInt(int viewId, String methodName, int value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 4, Integer.valueOf(value));
        addAction(reflectionAction);
    }

    public void setColorStateList(int viewId, String methodName, ColorStateList value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 15, value);
        addAction(reflectionAction);
    }

    public void setLong(int viewId, String methodName, long value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 5, Long.valueOf(value));
        addAction(reflectionAction);
    }

    public void setFloat(int viewId, String methodName, float value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 6, Float.valueOf(value));
        addAction(reflectionAction);
    }

    public void setDouble(int viewId, String methodName, double value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 7, Double.valueOf(value));
        addAction(reflectionAction);
    }

    public void setChar(int viewId, String methodName, char value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 8, Character.valueOf(value));
        addAction(reflectionAction);
    }

    public void setString(int viewId, String methodName, String value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 9, value);
        addAction(reflectionAction);
    }

    public void setCharSequence(int viewId, String methodName, CharSequence value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 10, value);
        addAction(reflectionAction);
    }

    public void setUri(int viewId, String methodName, Uri value) {
        if (value != null) {
            value = value.getCanonicalUri();
            if (StrictMode.vmFileUriExposureEnabled()) {
                value.checkFileUriExposed("RemoteViews.setUri()");
            }
        }
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 11, value);
        addAction(reflectionAction);
    }

    public void setBitmap(int viewId, String methodName, Bitmap value) {
        addAction(new BitmapReflectionAction(viewId, methodName, value));
    }

    public void setBundle(int viewId, String methodName, Bundle value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 13, value);
        addAction(reflectionAction);
    }

    public void setIntent(int viewId, String methodName, Intent value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 14, value);
        addAction(reflectionAction);
    }

    public void setIcon(int viewId, String methodName, Icon value) {
        ReflectionAction reflectionAction = new ReflectionAction(viewId, methodName, 16, value);
        addAction(reflectionAction);
    }

    public void setContentDescription(int viewId, CharSequence contentDescription) {
        setCharSequence(viewId, "setContentDescription", contentDescription);
    }

    public void setAccessibilityTraversalBefore(int viewId, int nextId) {
        setInt(viewId, "setAccessibilityTraversalBefore", nextId);
    }

    public void setAccessibilityTraversalAfter(int viewId, int nextId) {
        setInt(viewId, "setAccessibilityTraversalAfter", nextId);
    }

    public void setLabelFor(int viewId, int labeledId) {
        setInt(viewId, "setLabelFor", labeledId);
    }

    private RemoteViews getRemoteViewsToApply(Context context) {
        if (!hasLandscapeAndPortraitLayouts()) {
            return this;
        }
        if (context.getResources().getConfiguration().orientation == 2) {
            return this.mLandscape;
        }
        return this.mPortrait;
    }

    public void setApplyTheme(int themeResId) {
        this.mApplyThemeResId = themeResId;
    }

    public View apply(Context context, ViewGroup parent) {
        return apply(context, parent, null);
    }

    private CompatibilityInfo checkToApplyHostConfig(Context remoteContext, ViewGroup parent, Configuration outRemoteConf, DisplayMetrics outRemoteDm) {
        if (parent != null) {
            Context hostContext = parent.getContext();
            outRemoteDm.setTo(remoteContext.getResources().getDisplayMetrics());
            DisplayMetrics hostDM = hostContext.getResources().getDisplayMetrics();
            if (outRemoteDm.density != hostDM.density) {
                Configuration hostConf = hostContext.getResources().getConfiguration();
                CompatibilityInfo hostCI = hostContext.getResources().getCompatibilityInfo();
                outRemoteConf.setTo(remoteContext.getResources().getConfiguration());
                remoteContext.getResources().updateConfiguration(hostConf, hostDM, hostCI);
                return remoteContext.getResources().getCompatibilityInfo();
            }
        }
        return null;
    }

    public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        Context remoteContext = getContextForResources(context);
        Configuration remoteConf = new Configuration();
        DisplayMetrics remoteDM = new DisplayMetrics();
        CompatibilityInfo remoteCI = checkToApplyHostConfig(remoteContext, parent, remoteConf, remoteDM);
        View result = inflateView(context, remoteContext, rvToApply, parent, handler);
        loadTransitionOverride(context, handler);
        rvToApply.performApply(result, parent, handler);
        if (remoteCI != null) {
            remoteContext.getResources().updateConfiguration(remoteConf, remoteDM, remoteCI);
        }
        return result;
    }

    private View inflateView(Context context, Context contextForResources, RemoteViews rv, ViewGroup parent) {
        return inflateView(context, contextForResources, rv, parent, null);
    }

    /* access modifiers changed from: private */
    public View inflateView(Context context, final Context contextForResources, RemoteViews rv, ViewGroup parent, OnClickHandler handler) {
        Context inflationContext = new ContextWrapper(context) {
            public Resources getResources() {
                return contextForResources.getResources();
            }

            public Resources.Theme getTheme() {
                return contextForResources.getTheme();
            }

            public String getPackageName() {
                return contextForResources.getPackageName();
            }
        };
        if (this.mApplyThemeResId != 0 && !this.mUseAppContext) {
            inflationContext = new ContextThemeWrapper(inflationContext, this.mApplyThemeResId);
        }
        LayoutInflater inflater = ((LayoutInflater) context.getSystemService("layout_inflater")).cloneInContext(inflationContext);
        inflater.setFilter(this);
        if (((contextForResources.getApplicationInfo() == null ? 0 : contextForResources.getApplicationInfo().flags) & 1) != 0 && isSystemSignature(context, getPackage())) {
            setWidgetFactoryHuaWei(context, inflater);
        }
        View v = inflater.inflate(rv.getLayoutId(), parent, false);
        v.setTagInternal(16908312, Integer.valueOf(rv.getLayoutId()));
        return v;
    }

    private boolean isSystemSignature(Context context, String packageName) {
        if (context == null || packageName == null) {
            return false;
        }
        Signature[] signature = getSignatures(context, packageName);
        if (signature == null) {
            return false;
        }
        if (this.mSignSet.size() == 0) {
            initSystemSignatures(context);
        }
        for (Signature hashCode : signature) {
            if (this.mSignSet.contains(Integer.valueOf(hashCode.hashCode()))) {
                return true;
            }
        }
        return false;
    }

    private Signature[] getSignatures(Context context, String packageName) {
        Signature[] signatures = new Signature[0];
        if (context == null) {
            return signatures;
        }
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 64);
            if (pInfo != null) {
                signatures = pInfo.signatures;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return signatures;
    }

    private void initSystemSignatures(Context context) {
        if (context != null) {
            for (String signatures : BASE_PACKAGES) {
                Signature[] signatures2 = getSignatures(context, signatures);
                if (signatures2 != null) {
                    for (Signature hashCode : signatures2) {
                        this.mSignSet.add(Integer.valueOf(hashCode.hashCode()));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void loadTransitionOverride(Context context, OnClickHandler handler) {
        if (handler != null && context.getResources().getBoolean(17956999)) {
            TypedArray windowStyle = context.getTheme().obtainStyledAttributes(R.styleable.Window);
            TypedArray windowAnimationStyle = context.obtainStyledAttributes(windowStyle.getResourceId(8, 0), R.styleable.WindowAnimation);
            handler.setEnterAnimationId(windowAnimationStyle.getResourceId(26, 0));
            windowStyle.recycle();
            windowAnimationStyle.recycle();
        }
    }

    public CancellationSignal applyAsync(Context context, ViewGroup parent, Executor executor, OnViewAppliedListener listener) {
        return applyAsync(context, parent, executor, listener, null);
    }

    private CancellationSignal startTaskOnExecutor(AsyncApplyTask task, Executor executor) {
        CancellationSignal cancelSignal = new CancellationSignal();
        cancelSignal.setOnCancelListener(task);
        task.executeOnExecutor(executor == null ? AsyncTask.THREAD_POOL_EXECUTOR : executor, new Void[0]);
        return cancelSignal;
    }

    public CancellationSignal applyAsync(Context context, ViewGroup parent, Executor executor, OnViewAppliedListener listener, OnClickHandler handler) {
        return startTaskOnExecutor(getAsyncApplyTask(context, parent, listener, handler), executor);
    }

    /* access modifiers changed from: private */
    public AsyncApplyTask getAsyncApplyTask(Context context, ViewGroup parent, OnViewAppliedListener listener, OnClickHandler handler) {
        AsyncApplyTask asyncApplyTask = new AsyncApplyTask(getRemoteViewsToApply(context), parent, context, listener, handler, null);
        return asyncApplyTask;
    }

    public void reapply(Context context, View v) {
        reapply(context, v, null);
    }

    public void reapply(Context context, View v, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        if (!hasLandscapeAndPortraitLayouts() || ((Integer) v.getTag(16908312)).intValue() == rvToApply.getLayoutId()) {
            rvToApply.performApply(v, (ViewGroup) v.getParent(), handler);
            return;
        }
        throw new RuntimeException("Attempting to re-apply RemoteViews to a view that that does not share the same root layout id.");
    }

    public CancellationSignal reapplyAsync(Context context, View v, Executor executor, OnViewAppliedListener listener) {
        return reapplyAsync(context, v, executor, listener, null);
    }

    public CancellationSignal reapplyAsync(Context context, View v, Executor executor, OnViewAppliedListener listener, OnClickHandler handler) {
        View view;
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        if (hasLandscapeAndPortraitLayouts()) {
            view = v;
            if (((Integer) view.getTag(16908312)).intValue() != rvToApply.getLayoutId()) {
                throw new RuntimeException("Attempting to re-apply RemoteViews to a view that that does not share the same root layout id.");
            }
        } else {
            view = v;
        }
        AsyncApplyTask asyncApplyTask = new AsyncApplyTask(rvToApply, (ViewGroup) view.getParent(), context, listener, handler, view);
        return startTaskOnExecutor(asyncApplyTask, executor);
    }

    private void performApply(View v, ViewGroup parent, OnClickHandler handler) {
        if (this.mActions != null) {
            OnClickHandler handler2 = handler == null ? DEFAULT_ON_CLICK_HANDLER : handler;
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                this.mActions.get(i).apply(v, parent, handler2);
            }
        }
    }

    public boolean prefersAsyncApply() {
        if (this.mActions != null) {
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                if (this.mActions.get(i).prefersAsyncApply()) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public Context getContextForResources(Context context) {
        if (this.mApplication != null) {
            if (context.getUserId() == UserHandle.getUserId(this.mApplication.uid) && context.getPackageName().equals(this.mApplication.packageName)) {
                return context;
            }
            try {
                return context.createApplicationContext(this.mApplication, 4);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_TAG, "Package name " + this.mApplication.packageName + " not found");
            }
        }
        return context;
    }

    public int getSequenceNumber() {
        if (this.mActions == null) {
            return 0;
        }
        return this.mActions.size();
    }

    public boolean onLoadClass(Class clazz) {
        return clazz.isAnnotationPresent(RemoteView.class);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (!hasLandscapeAndPortraitLayouts()) {
            dest.writeInt(0);
            if (this.mIsRoot) {
                this.mBitmapCache.writeBitmapsToParcel(dest, flags);
            }
            if (this.mIsRoot || (flags & 2) == 0) {
                dest.writeInt(1);
                this.mApplication.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.mLayoutId);
            dest.writeInt(this.mIsWidgetCollectionChild ? 1 : 0);
            writeActionsToParcel(dest);
        } else {
            dest.writeInt(1);
            if (this.mIsRoot) {
                this.mBitmapCache.writeBitmapsToParcel(dest, flags);
            }
            this.mLandscape.writeToParcel(dest, flags);
            this.mPortrait.writeToParcel(dest, flags | 2);
        }
        dest.writeInt(this.mReapplyDisallowed ? 1 : 0);
    }

    private void writeActionsToParcel(Parcel parcel) {
        int count;
        int i;
        if (this.mActions != null) {
            count = this.mActions.size();
        } else {
            count = 0;
        }
        parcel.writeInt(count);
        for (int i2 = 0; i2 < count; i2++) {
            Action a = this.mActions.get(i2);
            parcel.writeInt(a.getActionTag());
            if (a.hasSameAppInfo(this.mApplication)) {
                i = 2;
            } else {
                i = 0;
            }
            a.writeToParcel(parcel, i);
        }
    }

    private static ApplicationInfo getApplicationInfo(String packageName, int userId) {
        if (packageName == null) {
            return null;
        }
        Application application = ActivityThread.currentApplication();
        if (application != null) {
            ApplicationInfo applicationInfo = application.getApplicationInfo();
            if (UserHandle.getUserId(applicationInfo.uid) != userId || !applicationInfo.packageName.equals(packageName)) {
                try {
                    applicationInfo = application.getBaseContext().createPackageContextAsUser(packageName, 0, new UserHandle(userId)).getApplicationInfo();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new IllegalArgumentException("No such package " + packageName);
                }
            }
            return applicationInfo;
        }
        throw new IllegalStateException("Cannot create remote views out of an aplication.");
    }

    public boolean hasSameAppInfo(ApplicationInfo info) {
        return this.mApplication.packageName.equals(info.packageName) && this.mApplication.uid == info.uid;
    }

    private void setWidgetFactoryHuaWei(Context context, LayoutInflater inflater) {
        try {
            inflater.setFactory(HwFrameworkFactory.getHwWidgetManager().createWidgetFactoryHuaWei(context, getPackage()));
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, e.getMessage());
        } catch (NullPointerException e2) {
            Log.e(LOG_TAG, e2.getMessage());
        }
    }

    public int getCacheSize() {
        if (this.mBitmapCache != null) {
            return this.mBitmapCache.getSize();
        }
        return 0;
    }

    public void recycle() {
        int count = getCacheSize();
        int i = 0;
        while (i < count) {
            try {
                if (!(this.mBitmapCache == null || this.mBitmapCache.getBitmapForId(i) == null)) {
                    this.mBitmapCache.getBitmapForId(i).recycle();
                }
                i++;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return;
            }
        }
    }
}

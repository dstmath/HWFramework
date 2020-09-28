package android.widget;

import android.annotation.UnsupportedAppUsage;
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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.RippleDrawable;
import android.hwtheme.HwThemeManager;
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
import android.provider.CalendarContract;
import android.telecom.Logging.Session;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.IntArray;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import com.android.internal.R;
import com.android.internal.util.ContrastColorUtil;
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
    private static final Action ACTION_NOOP = new RuntimeAction() {
        /* class android.widget.RemoteViews.AnonymousClass1 */

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
        }
    };
    private static final String[] BASE_PACKAGES = {"android", "com.android.mediacenter", "com.huawei.music", "com.huawei.systemmanager", "com.android.providers.applications", "com.android.providers.contacts", "com.android.providers.userdictionary", "com.android.providers.calendar", CalendarContract.AUTHORITY, "com.huawei.calendar", "com.example.android.notepad", "com.huawei.notepad", "com.android.providers.downloads", "com.android.providers.drm", "com.android.providers.media", "com.android.providers.downloads.ui", "com.huawei.calculator", "com.android.htmlviewer", BrowserContract.AUTHORITY, "com.android.gallery3d", "com.huawei.photos", "com.android.exchange", "com.huawei.email", "com.android.email", HwThemeManager.HWT_OLD_KEYGUARD, "com.android.smspush", "com.huawei.deskclock"};
    private static final int BITMAP_REFLECTION_ACTION_TAG = 12;
    public static final Parcelable.Creator<RemoteViews> CREATOR = new Parcelable.Creator<RemoteViews>() {
        /* class android.widget.RemoteViews.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public RemoteViews createFromParcel(Parcel parcel) {
            return new RemoteViews(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteViews[] newArray(int size) {
            return new RemoteViews[size];
        }
    };
    private static final OnClickHandler DEFAULT_ON_CLICK_HANDLER = $$Lambda$RemoteViews$xYCMzfQwRCAW2azHobWqQ9R0Wk.INSTANCE;
    static final String EXTRA_REMOTEADAPTER_APPWIDGET_ID = "remoteAdapterAppWidgetId";
    static final String EXTRA_REMOTEADAPTER_ON_LIGHT_BACKGROUND = "remoteAdapterOnLightBackground";
    public static final String EXTRA_SHARED_ELEMENT_BOUNDS = "android.widget.extra.SHARED_ELEMENT_BOUNDS";
    public static final int FLAG_REAPPLY_DISALLOWED = 1;
    public static final int FLAG_USE_LIGHT_BACKGROUND_LAYOUT = 4;
    public static final int FLAG_WIDGET_IS_COLLECTION_CHILD = 2;
    private static final String[] FORBIDDEN_PACKAGES = {HwPCUtils.PKG_PHONE_SYSTEMUI};
    private static final String FREEFORM = "freeform";
    private static final int LAYOUT_PARAM_ACTION_TAG = 19;
    private static final String LOG_TAG = "RemoteViews";
    private static final int MAX_NESTED_VIEWS = 10;
    private static final int MODE_HAS_LANDSCAPE_AND_PORTRAIT = 1;
    private static final int MODE_NORMAL = 0;
    private static final String[] NOTIFICATION_PKGS = {"com.huawei.intelligent"};
    private static final int OVERRIDE_TEXT_COLORS_TAG = 20;
    private static final int REFLECTION_ACTION_TAG = 2;
    private static final int SET_DRAWABLE_TINT_TAG = 3;
    private static final int SET_EMPTY_VIEW_ACTION_TAG = 6;
    private static final int SET_INT_TAG_TAG = 22;
    private static final int SET_ON_CLICK_RESPONSE_TAG = 1;
    private static final int SET_PENDING_INTENT_TEMPLATE_TAG = 8;
    private static final int SET_REMOTE_INPUTS_ACTION_TAG = 18;
    private static final int SET_REMOTE_VIEW_ADAPTER_INTENT_TAG = 10;
    private static final int SET_REMOTE_VIEW_ADAPTER_LIST_TAG = 15;
    private static final int SET_RIPPLE_DRAWABLE_COLOR_TAG = 21;
    private static final int TEXT_VIEW_DRAWABLE_ACTION_TAG = 11;
    private static final int TEXT_VIEW_SIZE_ACTION_TAG = 13;
    private static final int VIEW_CONTENT_NAVIGATION_TAG = 5;
    private static final int VIEW_GROUP_ACTION_ADD_TAG = 4;
    private static final int VIEW_GROUP_ACTION_REMOVE_TAG = 7;
    private static final int VIEW_PADDING_ACTION_TAG = 14;
    private static final MethodKey sLookupKey = new MethodKey();
    private static final ArrayMap<MethodKey, MethodArgs> sMethods = new ArrayMap<>();
    @UnsupportedAppUsage
    private ArrayList<Action> mActions;
    @UnsupportedAppUsage
    public ApplicationInfo mApplication;
    private int mApplyFlags;
    @UnsupportedAppUsage
    private BitmapCache mBitmapCache;
    private final Map<Class, Object> mClassCookies;
    private boolean mIsRoot;
    private RemoteViews mLandscape;
    @UnsupportedAppUsage
    private final int mLayoutId;
    private int mLightBackgroundLayoutId;
    @UnsupportedAppUsage
    private RemoteViews mPortrait;
    private Set<Integer> mSignSet;
    private boolean mUseAppContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ApplyFlags {
    }

    public interface OnClickHandler {
        boolean onClickHandler(View view, PendingIntent pendingIntent, RemoteResponse remoteResponse);
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RemoteView {
    }

    public void setRemoteInputs(int viewId, RemoteInput[] remoteInputs) {
        this.mActions.add(new SetRemoteInputsAction(viewId, remoteInputs));
    }

    public void reduceImageSizes(int maxWidth, int maxHeight) {
        ArrayList<Bitmap> cache = this.mBitmapCache.mBitmaps;
        for (int i = 0; i < cache.size(); i++) {
            cache.set(i, Icon.scaleDownIfNecessary(cache.get(i), maxWidth, maxHeight));
        }
    }

    public void overrideTextColors(int textColor) {
        addAction(new OverrideTextColorsAction(textColor));
    }

    public void setIntTag(int viewId, int key, int tag) {
        addAction(new SetIntTagAction(viewId, key, tag));
    }

    public void addFlags(int flags) {
        this.mApplyFlags |= flags;
    }

    public boolean hasFlags(int flag) {
        return (this.mApplyFlags & flag) == flag;
    }

    /* access modifiers changed from: package-private */
    public static class MethodKey {
        public String methodName;
        public Class paramClass;
        public Class targetClass;

        MethodKey() {
        }

        public boolean equals(Object o) {
            if (!(o instanceof MethodKey)) {
                return false;
            }
            MethodKey p = (MethodKey) o;
            if (!Objects.equals(p.targetClass, this.targetClass) || !Objects.equals(p.paramClass, this.paramClass) || !Objects.equals(p.methodName, this.methodName)) {
                return false;
            }
            return true;
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

    /* access modifiers changed from: package-private */
    public static class MethodArgs {
        public MethodHandle asyncMethod;
        public String asyncMethodName;
        public MethodHandle syncMethod;

        MethodArgs() {
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

    /* access modifiers changed from: private */
    public static abstract class Action implements Parcelable {
        public static final int MERGE_APPEND = 1;
        public static final int MERGE_IGNORE = 2;
        public static final int MERGE_REPLACE = 0;
        @UnsupportedAppUsage
        int viewId;

        public abstract void apply(View view, ViewGroup viewGroup, OnClickHandler onClickHandler) throws ActionException;

        public abstract int getActionTag();

        private Action() {
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
        }

        @UnsupportedAppUsage
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

    private static abstract class RuntimeAction extends Action {
        private RuntimeAction() {
            super();
        }

        @Override // android.widget.RemoteViews.Action
        public final int getActionTag() {
            return 0;
        }

        @Override // android.os.Parcelable
        public final void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException();
        }
    }

    /* JADX INFO: Multiple debug info for r3v2 java.util.ArrayList<android.widget.RemoteViews$Action>: [D('i' int), D('newActions' java.util.ArrayList<android.widget.RemoteViews$Action>)] */
    @UnsupportedAppUsage
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
    public static class RemoteViewsContextWrapper extends ContextWrapper {
        private final Context mContextForResources;

        RemoteViewsContextWrapper(Context context, Context contextForResources) {
            super(context);
            this.mContextForResources = contextForResources;
        }

        @Override // android.content.Context, android.content.ContextWrapper
        public Resources getResources() {
            return this.mContextForResources.getResources();
        }

        @Override // android.content.Context, android.content.ContextWrapper
        public Resources.Theme getTheme() {
            return this.mContextForResources.getTheme();
        }

        @Override // android.content.Context, android.content.ContextWrapper
        public String getPackageName() {
            return this.mContextForResources.getPackageName();
        }
    }

    /* access modifiers changed from: private */
    public class SetEmptyView extends Action {
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.viewId);
            out.writeInt(this.emptyViewId);
        }

        @Override // android.widget.RemoteViews.Action
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

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 6;
        }
    }

    /* access modifiers changed from: private */
    public class SetPendingIntentTemplate extends Action {
        @UnsupportedAppUsage
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            PendingIntent.writePendingIntentOrNullToParcel(this.pendingIntentTemplate, dest);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (target instanceof AdapterView) {
                    AdapterView<?> av = (AdapterView) target;
                    av.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /* class android.widget.RemoteViews.SetPendingIntentTemplate.AnonymousClass1 */

                        @Override // android.widget.AdapterView.OnItemClickListener
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (view instanceof ViewGroup) {
                                ViewGroup vg = (ViewGroup) view;
                                if (parent instanceof AdapterViewAnimator) {
                                    vg = (ViewGroup) vg.getChildAt(0);
                                }
                                if (vg != null) {
                                    RemoteResponse response = null;
                                    int childCount = vg.getChildCount();
                                    int i = 0;
                                    while (true) {
                                        if (i >= childCount) {
                                            break;
                                        }
                                        Object tag = vg.getChildAt(i).getTag(R.id.fillInIntent);
                                        if (tag instanceof RemoteResponse) {
                                            response = (RemoteResponse) tag;
                                            break;
                                        }
                                        i++;
                                    }
                                    if (response != null) {
                                        response.handleViewClick(view, handler);
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

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 8;
        }
    }

    /* access modifiers changed from: private */
    public class SetRemoteViewsAdapterList extends Action {
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.viewTypeCount);
            dest.writeTypedList(this.list, flags);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (!(rootParent instanceof AppWidgetHostView)) {
                    Log.e(RemoteViews.LOG_TAG, "SetRemoteViewsAdapterIntent action can only be used for AppWidgets (root id: " + this.viewId + ")");
                } else if (!(target instanceof AbsListView) && !(target instanceof AdapterViewAnimator)) {
                    Log.e(RemoteViews.LOG_TAG, "Cannot setRemoteViewsAdapter on a view which is not an AbsListView or AdapterViewAnimator (id: " + this.viewId + ")");
                } else if (target instanceof AbsListView) {
                    AbsListView v = (AbsListView) target;
                    Adapter a = v.getAdapter();
                    if (!(a instanceof RemoteViewsListAdapter) || this.viewTypeCount > a.getViewTypeCount()) {
                        v.setAdapter((ListAdapter) new RemoteViewsListAdapter(v.getContext(), this.list, this.viewTypeCount));
                    } else {
                        ((RemoteViewsListAdapter) a).setViewsList(this.list);
                    }
                } else if (target instanceof AdapterViewAnimator) {
                    AdapterViewAnimator v2 = (AdapterViewAnimator) target;
                    Adapter a2 = v2.getAdapter();
                    if (!(a2 instanceof RemoteViewsListAdapter) || this.viewTypeCount > a2.getViewTypeCount()) {
                        v2.setAdapter(new RemoteViewsListAdapter(v2.getContext(), this.list, this.viewTypeCount));
                    } else {
                        ((RemoteViewsListAdapter) a2).setViewsList(this.list);
                    }
                }
            }
        }

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 15;
        }
    }

    /* access modifiers changed from: private */
    public class SetRemoteViewsAdapterIntent extends Action {
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeTypedObject(this.intent, flags);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (!(rootParent instanceof AppWidgetHostView)) {
                    Log.e(RemoteViews.LOG_TAG, "SetRemoteViewsAdapterIntent action can only be used for AppWidgets (root id: " + this.viewId + ")");
                } else if ((target instanceof AbsListView) || (target instanceof AdapterViewAnimator)) {
                    this.intent.putExtra(RemoteViews.EXTRA_REMOTEADAPTER_APPWIDGET_ID, ((AppWidgetHostView) rootParent).getAppWidgetId()).putExtra(RemoteViews.EXTRA_REMOTEADAPTER_ON_LIGHT_BACKGROUND, RemoteViews.this.hasFlags(4));
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

        @Override // android.widget.RemoteViews.Action
        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            SetRemoteViewsAdapterIntent copy = new SetRemoteViewsAdapterIntent(this.viewId, this.intent);
            copy.isAsync = true;
            return copy;
        }

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 10;
        }
    }

    /* access modifiers changed from: private */
    @RCUnownedThisRef
    public class SetOnClickResponse extends Action {
        final RemoteResponse mResponse;

        SetOnClickResponse(int id, RemoteResponse response) {
            super();
            this.viewId = id;
            this.mResponse = response;
        }

        SetOnClickResponse(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.mResponse = new RemoteResponse();
            this.mResponse.readFromParcel(parcel);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            this.mResponse.writeToParcel(dest, flags);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (this.mResponse.mPendingIntent != null) {
                    if (RemoteViews.this.hasFlags(2)) {
                        Log.w(RemoteViews.LOG_TAG, "Cannot SetOnClickResponse for collection item (id: " + this.viewId + ")");
                        ApplicationInfo appInfo = root.getContext().getApplicationInfo();
                        if (appInfo != null && appInfo.targetSdkVersion >= 16) {
                            return;
                        }
                    }
                    target.setTagInternal(R.id.pending_intent_tag, this.mResponse.mPendingIntent);
                } else if (this.mResponse.mFillIntent == null) {
                    target.setOnClickListener(null);
                    return;
                } else if (!RemoteViews.this.hasFlags(2)) {
                    Log.e(RemoteViews.LOG_TAG, "The method setOnClickFillInIntent is available only from RemoteViewsFactory (ie. on collection items).");
                    return;
                } else if (target == root) {
                    target.setTagInternal(R.id.fillInIntent, this.mResponse);
                    return;
                }
                target.setOnClickListener(new View.OnClickListener(handler) {
                    /* class android.widget.$$Lambda$RemoteViews$SetOnClickResponse$9rKnU2QqCzJhBC39ZrKYXob0MA */
                    private final /* synthetic */ RemoteViews.OnClickHandler f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        RemoteViews.SetOnClickResponse.this.lambda$apply$0$RemoteViews$SetOnClickResponse(this.f$1, view);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$apply$0$RemoteViews$SetOnClickResponse(OnClickHandler handler, View v) {
            this.mResponse.handleViewClick(v, handler);
        }

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 1;
        }
    }

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
    /* access modifiers changed from: public */
    private MethodHandle getMethod(View view, String methodName, Class<?> paramType, boolean async) {
        Method method;
        Class<?> cls = view.getClass();
        synchronized (sMethods) {
            sLookupKey.set(cls, paramType, methodName);
            MethodArgs result = sMethods.get(sLookupKey);
            if (result == null) {
                if (paramType == null) {
                    method = cls.getMethod(methodName, new Class[0]);
                } else {
                    method = cls.getMethod(methodName, paramType);
                }
                if (method.isAnnotationPresent(RemotableViewMethod.class)) {
                    result = new MethodArgs();
                    result.syncMethod = MethodHandles.publicLookup().unreflect(method);
                    result.asyncMethodName = ((RemotableViewMethod) method.getAnnotation(RemotableViewMethod.class)).asyncImpl();
                    MethodKey key = new MethodKey();
                    key.set(cls, paramType, methodName);
                    sMethods.put(key, result);
                } else {
                    try {
                        throw new ActionException("view: " + cls.getName() + " can't use method with RemoteViews: " + methodName + getParameters(paramType));
                    } catch (IllegalAccessException | NoSuchMethodException e) {
                        throw new ActionException("view: " + cls.getName() + " doesn't have method: " + methodName + getParameters(paramType));
                    }
                }
            }
            if (!async) {
                return result.syncMethod;
            } else if (result.asyncMethodName.isEmpty()) {
                return null;
            } else {
                if (result.asyncMethod == null) {
                    MethodType asyncType = result.syncMethod.type().dropParameterTypes(0, 1).changeReturnType(Runnable.class);
                    try {
                        result.asyncMethod = MethodHandles.publicLookup().findVirtual(cls, result.asyncMethodName, asyncType);
                    } catch (IllegalAccessException | NoSuchMethodException e2) {
                        throw new ActionException("Async implementation declared as " + result.asyncMethodName + " but not defined for " + methodName + ": public Runnable " + result.asyncMethodName + " (" + TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, asyncType.parameterArray()) + ")");
                    }
                }
                return result.asyncMethod;
            }
        }
    }

    private static String getParameters(Class<?> paramType) {
        if (paramType == null) {
            return "()";
        }
        return "(" + paramType + ")";
    }

    /* access modifiers changed from: private */
    @RCUnownedThisRef
    public class SetDrawableTint extends Action {
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeInt(this.targetBackground ? 1 : 0);
            dest.writeInt(this.colorFilter);
            dest.writeInt(PorterDuff.modeToInt(this.filterMode));
        }

        @Override // android.widget.RemoteViews.Action
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

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 3;
        }
    }

    /* access modifiers changed from: private */
    public class SetRippleDrawableColor extends Action {
        ColorStateList mColorStateList;

        SetRippleDrawableColor(int id, ColorStateList colorStateList) {
            super();
            this.viewId = id;
            this.mColorStateList = colorStateList;
        }

        SetRippleDrawableColor(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.mColorStateList = (ColorStateList) parcel.readParcelable(null);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeParcelable(this.mColorStateList, 0);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                Drawable targetDrawable = target.getBackground();
                if (targetDrawable instanceof RippleDrawable) {
                    ((RippleDrawable) targetDrawable.mutate()).setColor(this.mColorStateList);
                }
            }
        }

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 21;
        }
    }

    /* access modifiers changed from: private */
    public final class ViewContentNavigation extends Action {
        final boolean mNext;

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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.viewId);
            out.writeBoolean(this.mNext);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view != null) {
                try {
                    RemoteViews.this.getMethod(view, this.mNext ? "showNext" : "showPrevious", null, false);
                } catch (Throwable ex) {
                    throw new ActionException(ex);
                }
            }
        }

        @Override // android.widget.RemoteViews.Action
        public int mergeBehavior() {
            return 2;
        }

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 5;
        }
    }

    /* access modifiers changed from: private */
    public static class BitmapCache {
        int mBitmapMemory;
        @UnsupportedAppUsage
        ArrayList<Bitmap> mBitmaps;

        public BitmapCache() {
            this.mBitmapMemory = -1;
            this.mBitmaps = new ArrayList<>();
        }

        public int getSize() {
            return this.mBitmaps.size();
        }

        public BitmapCache(Parcel source) {
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

    /* access modifiers changed from: private */
    @RCUnownedThisRef
    public class BitmapReflectionAction extends Action {
        @UnsupportedAppUsage
        Bitmap bitmap;
        int bitmapId;
        @UnsupportedAppUsage
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeString(this.methodName);
            dest.writeInt(this.bitmapId);
        }

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
            new ReflectionAction(this.viewId, this.methodName, 12, this.bitmap).apply(root, rootParent, handler);
        }

        @Override // android.widget.RemoteViews.Action
        public void setBitmapCache(BitmapCache bitmapCache) {
            this.bitmapId = bitmapCache.getBitmapId(this.bitmap);
        }

        @Override // android.widget.RemoteViews.Action
        public int getActionTag() {
            return 12;
        }
    }

    /* access modifiers changed from: private */
    @RCUnownedThisRef
    public final class ReflectionAction extends Action {
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
        @UnsupportedAppUsage
        String methodName;
        int type;
        @UnsupportedAppUsage
        Object value;

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

        @Override // android.os.Parcelable
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

        @Override // android.widget.RemoteViews.Action
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view != null) {
                Class<?> param = getParameterType();
                if (param != null) {
                    try {
                        RemoteViews.this.getMethod(view, this.methodName, param, false);
                        Object obj = this.value;
                    } catch (Throwable ex) {
                        throw new ActionException(ex);
                    }
                } else {
                    throw new ActionException("bad type: " + this.type);
                }
            }
        }

        @Override // android.widget.RemoteViews.Action
        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view == null) {
                return RemoteViews.ACTION_NOOP;
            }
            Class<?> param = getParameterType();
            if (param != null) {
                try {
                    if (RemoteViews.this.getMethod(view, this.methodName, param, true) == null) {
                        return this;
                    }
                    Object obj = this.value;
                    Runnable endAction = 

                    private static final class RunnableAction extends RuntimeAction {
                        private final Runnable mRunnable;

                        RunnableAction(Runnable r) {
                            super();
                            this.mRunnable = r;
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            this.mRunnable.run();
                        }
                    }

                    /* access modifiers changed from: private */
                    /* access modifiers changed from: public */
                    private void configureRemoteViewsAsChild(RemoteViews rv) {
                        rv.setBitmapCache(this.mBitmapCache);
                        rv.setNotRoot();
                    }

                    /* access modifiers changed from: package-private */
                    public void setNotRoot() {
                        this.mIsRoot = false;
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class ViewGroupActionAdd extends Action {
                        private int mIndex;
                        @UnsupportedAppUsage
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
                            this.mNestedViews = new RemoteViews(parcel, bitmapCache, info, depth, classCookies);
                            this.mNestedViews.addFlags(RemoteViews.this.mApplyFlags);
                        }

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.viewId);
                            dest.writeInt(this.mIndex);
                            this.mNestedViews.writeToParcel(dest, flags);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public boolean hasSameAppInfo(ApplicationInfo parentInfo) {
                            return this.mNestedViews.hasSameAppInfo(parentInfo);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            Context context = root.getContext();
                            ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
                            if (target != null) {
                                this.mNestedViews.setUseAppContext(RemoteViews.this.mUseAppContext);
                                target.addView(this.mNestedViews.apply(context, target, handler), this.mIndex);
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
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
                                    /* class android.widget.RemoteViews.ViewGroupActionAdd.AnonymousClass1 */

                                    @Override // android.widget.RemoteViews.Action
                                    public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
                                        task.onPostExecute(tree);
                                        targetVg.addView(task.mResult, ViewGroupActionAdd.this.mIndex);
                                    }
                                };
                            }
                            throw new ActionException(task.mError);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void setBitmapCache(BitmapCache bitmapCache) {
                            this.mNestedViews.setBitmapCache(bitmapCache);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int mergeBehavior() {
                            return 1;
                        }

                        @Override // android.widget.RemoteViews.Action
                        public boolean prefersAsyncApply() {
                            return this.mNestedViews.prefersAsyncApply();
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 4;
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class ViewGroupActionRemove extends Action {
                        private static final int REMOVE_ALL_VIEWS_ID = -2;
                        private int mViewIdToKeep;

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

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.viewId);
                            dest.writeInt(this.mViewIdToKeep);
                        }

                        @Override // android.widget.RemoteViews.Action
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

                        @Override // android.widget.RemoteViews.Action
                        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
                            root.createTree();
                            ViewTree target = root.findViewTreeById(this.viewId);
                            if (target == null || !(target.mRoot instanceof ViewGroup)) {
                                return RemoteViews.ACTION_NOOP;
                            }
                            final ViewGroup targetVg = (ViewGroup) target.mRoot;
                            target.mChildren = null;
                            return new RuntimeAction() {
                                /* class android.widget.RemoteViews.ViewGroupActionRemove.AnonymousClass1 */

                                @Override // android.widget.RemoteViews.Action
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
                        /* access modifiers changed from: public */
                        private void removeAllViewsExceptIdToKeep(ViewGroup viewGroup) {
                            for (int index = viewGroup.getChildCount() - 1; index >= 0; index--) {
                                if (viewGroup.getChildAt(index).getId() != this.mViewIdToKeep) {
                                    viewGroup.removeViewAt(index);
                                }
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 7;
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int mergeBehavior() {
                            return 1;
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class TextViewDrawableAction extends Action {
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

                        @Override // android.os.Parcelable
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

                        @Override // android.widget.RemoteViews.Action
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
                                    Icon icon = this.i1;
                                    Drawable id42 = null;
                                    Drawable id12 = icon == null ? null : icon.loadDrawable(ctx);
                                    Icon icon2 = this.i2;
                                    Drawable id22 = icon2 == null ? null : icon2.loadDrawable(ctx);
                                    Icon icon3 = this.i3;
                                    Drawable id32 = icon3 == null ? null : icon3.loadDrawable(ctx);
                                    Icon icon4 = this.i4;
                                    if (icon4 != null) {
                                        id42 = icon4.loadDrawable(ctx);
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

                        @Override // android.widget.RemoteViews.Action
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
                                Icon icon = this.i1;
                                copy.id1 = icon == null ? null : icon.loadDrawable(ctx);
                                Icon icon2 = this.i2;
                                copy.id2 = icon2 == null ? null : icon2.loadDrawable(ctx);
                                Icon icon3 = this.i3;
                                copy.id3 = icon3 == null ? null : icon3.loadDrawable(ctx);
                                Icon icon4 = this.i4;
                                if (icon4 != null) {
                                    drawable = icon4.loadDrawable(ctx);
                                }
                                copy.id4 = drawable;
                            } else {
                                int i = this.d1;
                                copy.id1 = i == 0 ? null : ctx.getDrawable(i);
                                int i5 = this.d2;
                                copy.id2 = i5 == 0 ? null : ctx.getDrawable(i5);
                                int i6 = this.d3;
                                copy.id3 = i6 == 0 ? null : ctx.getDrawable(i6);
                                int i7 = this.d4;
                                if (i7 != 0) {
                                    drawable = ctx.getDrawable(i7);
                                }
                                copy.id4 = drawable;
                            }
                            return copy;
                        }

                        @Override // android.widget.RemoteViews.Action
                        public boolean prefersAsyncApply() {
                            return this.useIcons;
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 11;
                        }

                        private void setCompoundDrawablesRelativeBoundsForced() {
                            int i;
                            int i5 = this.width;
                            if (i5 != 0 && (i = this.height) != 0) {
                                Drawable drawable = this.id1;
                                if (drawable != null) {
                                    drawable.setBounds(0, 0, i5, i);
                                }
                                Drawable drawable2 = this.id2;
                                if (drawable2 != null) {
                                    drawable2.setBounds(0, 0, this.width, this.height);
                                }
                                Drawable drawable3 = this.id3;
                                if (drawable3 != null) {
                                    drawable3.setBounds(0, 0, this.width, this.height);
                                }
                                Drawable drawable4 = this.id4;
                                if (drawable4 != null) {
                                    drawable4.setBounds(0, 0, this.width, this.height);
                                }
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void visitUris(Consumer<Uri> visitor) {
                            if (this.useIcons) {
                                RemoteViews.visitIconUri(this.i1, visitor);
                                RemoteViews.visitIconUri(this.i2, visitor);
                                RemoteViews.visitIconUri(this.i3, visitor);
                                RemoteViews.visitIconUri(this.i4, visitor);
                            }
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class TextViewSizeAction extends Action {
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

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.viewId);
                            dest.writeInt(this.units);
                            dest.writeFloat(this.size);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            TextView target = (TextView) root.findViewById(this.viewId);
                            if (target != null) {
                                target.setTextSize(this.units, this.size);
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 13;
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class ViewPaddingAction extends Action {
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

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.viewId);
                            dest.writeInt(this.left);
                            dest.writeInt(this.top);
                            dest.writeInt(this.right);
                            dest.writeInt(this.bottom);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            View target = root.findViewById(this.viewId);
                            if (target != null) {
                                target.setPadding(this.left, this.top, this.right, this.bottom);
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 14;
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public static class LayoutParamAction extends Action {
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

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.viewId);
                            dest.writeInt(this.mProperty);
                            dest.writeInt(this.mValue);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            ViewGroup.LayoutParams layoutParams;
                            View target = root.findViewById(this.viewId);
                            if (target != null && (layoutParams = target.getLayoutParams()) != null) {
                                int value = this.mValue;
                                int i = this.mProperty;
                                if (i == 1) {
                                    value = resolveDimenPixelOffset(target, this.mValue);
                                } else if (i == 2) {
                                    layoutParams.width = this.mValue;
                                    target.setLayoutParams(layoutParams);
                                    return;
                                } else if (i != 3) {
                                    if (i != 4) {
                                        if (i != 5) {
                                            throw new IllegalArgumentException("Unknown property " + this.mProperty);
                                        } else if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                                            ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = resolveDimenPixelOffset(target, this.mValue);
                                            target.setLayoutParams(layoutParams);
                                            return;
                                        } else {
                                            return;
                                        }
                                    }
                                } else if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = resolveDimenPixelOffset(target, this.mValue);
                                    target.setLayoutParams(layoutParams);
                                    return;
                                } else {
                                    return;
                                }
                                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(value);
                                    target.setLayoutParams(layoutParams);
                                }
                            }
                        }

                        private static int resolveDimenPixelOffset(View target, int value) {
                            if (value == 0) {
                                return 0;
                            }
                            return target.getContext().getResources().getDimensionPixelOffset(value);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 19;
                        }

                        @Override // android.widget.RemoteViews.Action
                        public String getUniqueKey() {
                            return super.getUniqueKey() + this.mProperty;
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class SetRemoteInputsAction extends Action {
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

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.viewId);
                            dest.writeTypedArray(this.remoteInputs, flags);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            View target = root.findViewById(this.viewId);
                            if (target != null) {
                                target.setTagInternal(R.id.remote_input_tag, this.remoteInputs);
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 18;
                        }
                    }

                    /* access modifiers changed from: private */
                    @RCUnownedThisRef
                    public class OverrideTextColorsAction extends Action {
                        private final int textColor;

                        public OverrideTextColorsAction(int textColor2) {
                            super();
                            this.textColor = textColor2;
                        }

                        public OverrideTextColorsAction(Parcel parcel) {
                            super();
                            this.textColor = parcel.readInt();
                        }

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.textColor);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            Stack<View> viewsToProcess = new Stack<>();
                            viewsToProcess.add(root);
                            while (!viewsToProcess.isEmpty()) {
                                View v = viewsToProcess.pop();
                                if (v instanceof TextView) {
                                    TextView textView = (TextView) v;
                                    textView.setText(ContrastColorUtil.clearColorSpans(textView.getText()));
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

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 20;
                        }
                    }

                    /* access modifiers changed from: private */
                    public class SetIntTagAction extends Action {
                        private final int mKey;
                        private final int mTag;
                        private final int mViewId;

                        SetIntTagAction(int viewId, int key, int tag) {
                            super();
                            this.mViewId = viewId;
                            this.mKey = key;
                            this.mTag = tag;
                        }

                        SetIntTagAction(Parcel parcel) {
                            super();
                            this.mViewId = parcel.readInt();
                            this.mKey = parcel.readInt();
                            this.mTag = parcel.readInt();
                        }

                        @Override // android.os.Parcelable
                        public void writeToParcel(Parcel dest, int flags) {
                            dest.writeInt(this.mViewId);
                            dest.writeInt(this.mKey);
                            dest.writeInt(this.mTag);
                        }

                        @Override // android.widget.RemoteViews.Action
                        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
                            View target = root.findViewById(this.mViewId);
                            if (target != null) {
                                target.setTagInternal(this.mKey, Integer.valueOf(this.mTag));
                            }
                        }

                        @Override // android.widget.RemoteViews.Action
                        public int getActionTag() {
                            return 22;
                        }
                    }

                    public RemoteViews(String packageName, int layoutId) {
                        this(getApplicationInfo(packageName, UserHandle.myUserId()), layoutId);
                    }

                    public RemoteViews(String packageName, int userId, int layoutId) {
                        this(getApplicationInfo(packageName, userId), layoutId);
                    }

                    protected RemoteViews(ApplicationInfo application, int layoutId) {
                        this.mLightBackgroundLayoutId = 0;
                        this.mIsRoot = true;
                        this.mLandscape = null;
                        this.mPortrait = null;
                        this.mApplyFlags = 0;
                        this.mSignSet = new HashSet();
                        this.mUseAppContext = false;
                        this.mApplication = application;
                        this.mLayoutId = layoutId;
                        this.mBitmapCache = new BitmapCache();
                        this.mClassCookies = null;
                    }

                    private boolean hasLandscapeAndPortraitLayouts() {
                        return (this.mLandscape == null || this.mPortrait == null) ? false : true;
                    }

                    public RemoteViews(RemoteViews landscape, RemoteViews portrait) {
                        this.mLightBackgroundLayoutId = 0;
                        this.mIsRoot = true;
                        this.mLandscape = null;
                        this.mPortrait = null;
                        this.mApplyFlags = 0;
                        this.mSignSet = new HashSet();
                        this.mUseAppContext = false;
                        if (landscape == null || portrait == null) {
                            throw new RuntimeException("Both RemoteViews must be non-null");
                        } else if (landscape.hasSameAppInfo(portrait.mApplication)) {
                            this.mApplication = portrait.mApplication;
                            this.mLayoutId = portrait.mLayoutId;
                            this.mLightBackgroundLayoutId = portrait.mLightBackgroundLayoutId;
                            this.mLandscape = landscape;
                            this.mPortrait = portrait;
                            this.mBitmapCache = new BitmapCache();
                            configureRemoteViewsAsChild(landscape);
                            configureRemoteViewsAsChild(portrait);
                            Map<Class, Object> map = portrait.mClassCookies;
                            this.mClassCookies = map == null ? landscape.mClassCookies : map;
                        } else {
                            throw new RuntimeException("Both RemoteViews must share the same package and user");
                        }
                    }

                    public RemoteViews(RemoteViews src) {
                        this.mLightBackgroundLayoutId = 0;
                        this.mIsRoot = true;
                        this.mLandscape = null;
                        this.mPortrait = null;
                        this.mApplyFlags = 0;
                        this.mSignSet = new HashSet();
                        this.mUseAppContext = false;
                        this.mBitmapCache = src.mBitmapCache;
                        this.mApplication = src.mApplication;
                        this.mIsRoot = src.mIsRoot;
                        this.mLayoutId = src.mLayoutId;
                        this.mLightBackgroundLayoutId = src.mLightBackgroundLayoutId;
                        this.mApplyFlags = src.mApplyFlags;
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
                        ApplicationInfo applicationInfo;
                        this.mLightBackgroundLayoutId = 0;
                        this.mIsRoot = true;
                        this.mLandscape = null;
                        this.mPortrait = null;
                        this.mApplyFlags = 0;
                        this.mSignSet = new HashSet();
                        this.mUseAppContext = false;
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
                                if (parcel.readInt() == 0) {
                                    applicationInfo = info;
                                } else {
                                    applicationInfo = ApplicationInfo.CREATOR.createFromParcel(parcel);
                                }
                                this.mApplication = applicationInfo;
                                this.mLayoutId = parcel.readInt();
                                this.mLightBackgroundLayoutId = parcel.readInt();
                                readActionsFromParcel(parcel, depth2);
                            } else {
                                this.mLandscape = new RemoteViews(parcel, this.mBitmapCache, info, depth2, this.mClassCookies);
                                this.mPortrait = new RemoteViews(parcel, this.mBitmapCache, this.mLandscape.mApplication, depth2, this.mClassCookies);
                                RemoteViews remoteViews = this.mPortrait;
                                this.mApplication = remoteViews.mApplication;
                                this.mLayoutId = remoteViews.mLayoutId;
                                this.mLightBackgroundLayoutId = remoteViews.mLightBackgroundLayoutId;
                            }
                            this.mApplyFlags = parcel.readInt();
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
                                return new SetOnClickResponse(parcel);
                            case 2:
                                return new ReflectionAction(parcel);
                            case 3:
                                return new SetDrawableTint(parcel);
                            case 4:
                                return new ViewGroupActionAdd(parcel, this.mBitmapCache, this.mApplication, depth, this.mClassCookies);
                            case 5:
                                return new ViewContentNavigation(parcel);
                            case 6:
                                return new SetEmptyView(parcel);
                            case 7:
                                return new ViewGroupActionRemove(parcel);
                            case 8:
                                return new SetPendingIntentTemplate(parcel);
                            case 9:
                            case 16:
                            case 17:
                            default:
                                throw new ActionException("Tag " + tag + " not found");
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
                            case 21:
                                return new SetRippleDrawableColor(parcel);
                            case 22:
                                return new SetIntTagAction(parcel);
                        }
                    }

                    @Deprecated
                    public RemoteViews clone() {
                        Preconditions.checkState(this.mIsRoot, "RemoteView has been attached to another RemoteView. May only clone the root of a RemoteView hierarchy.");
                        return new RemoteViews(this);
                    }

                    public String getPackage() {
                        ApplicationInfo applicationInfo = this.mApplication;
                        if (applicationInfo != null) {
                            return applicationInfo.packageName;
                        }
                        return null;
                    }

                    public int getLayoutId() {
                        int i;
                        if (!hasFlags(4) || (i = this.mLightBackgroundLayoutId) == 0) {
                            return this.mLayoutId;
                        }
                        return i;
                    }

                    /* access modifiers changed from: private */
                    /* access modifiers changed from: public */
                    private void setBitmapCache(BitmapCache bitmapCache) {
                        this.mBitmapCache = bitmapCache;
                        if (!hasLandscapeAndPortraitLayouts()) {
                            ArrayList<Action> arrayList = this.mActions;
                            if (arrayList != null) {
                                int count = arrayList.size();
                                for (int i = 0; i < count; i++) {
                                    this.mActions.get(i).setBitmapCache(bitmapCache);
                                }
                                return;
                            }
                            return;
                        }
                        this.mLandscape.setBitmapCache(bitmapCache);
                        this.mPortrait.setBitmapCache(bitmapCache);
                    }

                    @UnsupportedAppUsage
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

                    @UnsupportedAppUsage
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
                        addAction(new TextViewDrawableAction(viewId, false, left, top, right, bottom));
                    }

                    public void setTextViewCompoundDrawablesRelative(int viewId, int start, int top, int end, int bottom) {
                        addAction(new TextViewDrawableAction(viewId, true, start, top, end, bottom));
                    }

                    public void setTextViewCompoundDrawables(int viewId, Icon left, Icon top, Icon right, Icon bottom) {
                        addAction(new TextViewDrawableAction(viewId, false, left, top, right, bottom));
                    }

                    public void setTextViewCompoundDrawablesWithBounds(int viewId, Icon left, Icon top, Icon right, Icon bottom, int width, int height, int padding) {
                        addAction(new TextViewDrawableAction(viewId, true, left, top, right, bottom, width, height, padding));
                    }

                    public void setTextViewCompoundDrawablesRelative(int viewId, Icon start, Icon top, Icon end, Icon bottom) {
                        addAction(new TextViewDrawableAction(viewId, true, start, top, end, bottom));
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
                        setOnClickResponse(viewId, RemoteResponse.fromPendingIntent(pendingIntent));
                    }

                    public void setOnClickResponse(int viewId, RemoteResponse response) {
                        addAction(new SetOnClickResponse(viewId, response));
                    }

                    public void setPendingIntentTemplate(int viewId, PendingIntent pendingIntentTemplate) {
                        addAction(new SetPendingIntentTemplate(viewId, pendingIntentTemplate));
                    }

                    public void setOnClickFillInIntent(int viewId, Intent fillInIntent) {
                        setOnClickResponse(viewId, RemoteResponse.fromFillInIntent(fillInIntent));
                    }

                    public void setDrawableTint(int viewId, boolean targetBackground, int colorFilter, PorterDuff.Mode mode) {
                        addAction(new SetDrawableTint(viewId, targetBackground, colorFilter, mode));
                    }

                    public void setRippleDrawableColor(int viewId, ColorStateList colorStateList) {
                        addAction(new SetRippleDrawableColor(viewId, colorStateList));
                    }

                    public void setProgressTintList(int viewId, ColorStateList tint) {
                        addAction(new ReflectionAction(viewId, "setProgressTintList", 15, tint));
                    }

                    public void setProgressBackgroundTintList(int viewId, ColorStateList tint) {
                        addAction(new ReflectionAction(viewId, "setProgressBackgroundTintList", 15, tint));
                    }

                    public void setProgressIndeterminateTintList(int viewId, ColorStateList tint) {
                        addAction(new ReflectionAction(viewId, "setIndeterminateTintList", 15, tint));
                    }

                    public void setTextColor(int viewId, int color) {
                        setInt(viewId, "setTextColor", color);
                    }

                    public void setTextColor(int viewId, ColorStateList colors) {
                        addAction(new ReflectionAction(viewId, "setTextColor", 15, colors));
                    }

                    @Deprecated
                    public void setRemoteAdapter(int appWidgetId, int viewId, Intent intent) {
                        setRemoteAdapter(viewId, intent);
                    }

                    public void setRemoteAdapter(int viewId, Intent intent) {
                        addAction(new SetRemoteViewsAdapterIntent(viewId, intent));
                    }

                    @UnsupportedAppUsage
                    @Deprecated
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
                        addAction(new ViewPaddingAction(viewId, left, top, right, bottom));
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
                        addAction(new ReflectionAction(viewId, methodName, 1, Boolean.valueOf(value)));
                    }

                    public void setUseAppContext(boolean useAppContext) {
                        this.mUseAppContext = useAppContext;
                    }

                    public void setByte(int viewId, String methodName, byte value) {
                        addAction(new ReflectionAction(viewId, methodName, 2, Byte.valueOf(value)));
                    }

                    public void setShort(int viewId, String methodName, short value) {
                        addAction(new ReflectionAction(viewId, methodName, 3, Short.valueOf(value)));
                    }

                    public void setInt(int viewId, String methodName, int value) {
                        addAction(new ReflectionAction(viewId, methodName, 4, Integer.valueOf(value)));
                    }

                    public void setColorStateList(int viewId, String methodName, ColorStateList value) {
                        addAction(new ReflectionAction(viewId, methodName, 15, value));
                    }

                    public void setLong(int viewId, String methodName, long value) {
                        addAction(new ReflectionAction(viewId, methodName, 5, Long.valueOf(value)));
                    }

                    public void setFloat(int viewId, String methodName, float value) {
                        addAction(new ReflectionAction(viewId, methodName, 6, Float.valueOf(value)));
                    }

                    public void setDouble(int viewId, String methodName, double value) {
                        addAction(new ReflectionAction(viewId, methodName, 7, Double.valueOf(value)));
                    }

                    public void setChar(int viewId, String methodName, char value) {
                        addAction(new ReflectionAction(viewId, methodName, 8, Character.valueOf(value)));
                    }

                    public void setString(int viewId, String methodName, String value) {
                        addAction(new ReflectionAction(viewId, methodName, 9, value));
                    }

                    public void setCharSequence(int viewId, String methodName, CharSequence value) {
                        addAction(new ReflectionAction(viewId, methodName, 10, value));
                    }

                    public void setUri(int viewId, String methodName, Uri value) {
                        if (value != null) {
                            value = value.getCanonicalUri();
                            if (StrictMode.vmFileUriExposureEnabled()) {
                                value.checkFileUriExposed("RemoteViews.setUri()");
                            }
                        }
                        addAction(new ReflectionAction(viewId, methodName, 11, value));
                    }

                    public void setBitmap(int viewId, String methodName, Bitmap value) {
                        addAction(new BitmapReflectionAction(viewId, methodName, value));
                    }

                    public void setBundle(int viewId, String methodName, Bundle value) {
                        addAction(new ReflectionAction(viewId, methodName, 13, value));
                    }

                    public void setIntent(int viewId, String methodName, Intent value) {
                        addAction(new ReflectionAction(viewId, methodName, 14, value));
                    }

                    public void setIcon(int viewId, String methodName, Icon value) {
                        addAction(new ReflectionAction(viewId, methodName, 16, value));
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

                    public void setLightBackgroundLayoutId(int layoutId) {
                        this.mLightBackgroundLayoutId = layoutId;
                    }

                    public RemoteViews getDarkTextViews() {
                        if (hasFlags(4)) {
                            return this;
                        }
                        try {
                            addFlags(4);
                            return new RemoteViews(this);
                        } finally {
                            this.mApplyFlags &= -5;
                        }
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

                    public View apply(Context context, ViewGroup parent) {
                        return apply(context, parent, null);
                    }

                    public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
                        RemoteViews rvToApply = getRemoteViewsToApply(context);
                        View result = inflateView(context, rvToApply, parent);
                        rvToApply.performApply(result, parent, handler);
                        return result;
                    }

                    public View applyWithTheme(Context context, ViewGroup parent, OnClickHandler handler, int applyThemeResId) {
                        RemoteViews rvToApply = getRemoteViewsToApply(context);
                        View result = inflateView(context, rvToApply, parent, applyThemeResId);
                        rvToApply.performApply(result, parent, handler);
                        return result;
                    }

                    /* access modifiers changed from: private */
                    /* access modifiers changed from: public */
                    private View inflateView(Context context, RemoteViews rv, ViewGroup parent) {
                        return inflateView(context, rv, parent, 0);
                    }

                    private View inflateView(Context context, RemoteViews rv, ViewGroup parent, int applyThemeResId) {
                        Context contextForResources = getContextForResources(context);
                        Context inflationContext = new RemoteViewsContextWrapper(context, contextForResources);
                        if (applyThemeResId != 0 && !this.mUseAppContext) {
                            inflationContext = new ContextThemeWrapper(inflationContext, applyThemeResId);
                        }
                        LayoutInflater inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).cloneInContext(inflationContext);
                        inflater.setFilter(this);
                        if (isUseHuaweiWidgetFactory(context, contextForResources)) {
                            setWidgetFactoryHuaWei(context, inflater);
                        }
                        View v = inflater.inflate(rv.getLayoutId(), parent, false);
                        v.setTagInternal(16908312, Integer.valueOf(rv.getLayoutId()));
                        return v;
                    }

                    private boolean isUseHuaweiWidgetFactory(Context localContext, Context resContext) {
                        String myPkg = ActivityThread.currentApplication() == null ? "" : Application.getProcessName();
                        String remotePkg = getPackage();
                        if (contains(FORBIDDEN_PACKAGES, myPkg) && !contains(NOTIFICATION_PKGS, remotePkg)) {
                            return false;
                        }
                        if (((resContext.getApplicationInfo() == null ? 0 : resContext.getApplicationInfo().flags) & 1) == 0 || !isSystemSignature(localContext, remotePkg)) {
                            return false;
                        }
                        return true;
                    }

                    private static boolean contains(String[] strArray, String target) {
                        for (String str : strArray) {
                            if (str.equals(target)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    private boolean isSystemSignature(Context context, String packageName) {
                        Signature[] signature;
                        if (context == null || packageName == null || (signature = getSignatures(context, packageName)) == null) {
                            return false;
                        }
                        if (this.mSignSet.size() == 0) {
                            initSystemSignatures(context);
                        }
                        for (Signature signature2 : signature) {
                            if (this.mSignSet.contains(Integer.valueOf(signature2.hashCode()))) {
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
                                return pInfo.signatures;
                            }
                            return signatures;
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.w(LOG_TAG, "getSignatures NameNotFoundException " + packageName);
                            return signatures;
                        }
                    }

                    private void initSystemSignatures(Context context) {
                        if (context != null) {
                            int i = 0;
                            while (true) {
                                String[] strArr = BASE_PACKAGES;
                                if (i < strArr.length) {
                                    Signature[] signatures = getSignatures(context, strArr[i]);
                                    if (signatures != null) {
                                        for (Signature signature : signatures) {
                                            this.mSignSet.add(Integer.valueOf(signature.hashCode()));
                                        }
                                    }
                                    i++;
                                } else {
                                    return;
                                }
                            }
                        }
                    }

                    public interface OnViewAppliedListener {
                        void onError(Exception exc);

                        void onViewApplied(View view);

                        default void onViewInflated(View v) {
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
                    /* access modifiers changed from: public */
                    private AsyncApplyTask getAsyncApplyTask(Context context, ViewGroup parent, OnViewAppliedListener listener, OnClickHandler handler) {
                        return new AsyncApplyTask(getRemoteViewsToApply(context), parent, context, listener, handler, null);
                    }

                    /* access modifiers changed from: private */
                    public class AsyncApplyTask extends AsyncTask<Void, Void, ViewTree> implements CancellationSignal.OnCancelListener {
                        private Action[] mActions;
                        final Context mContext;
                        private Exception mError;
                        final OnClickHandler mHandler;
                        final OnViewAppliedListener mListener;
                        final ViewGroup mParent;
                        final RemoteViews mRV;
                        private View mResult;
                        private ViewTree mTree;

                        private AsyncApplyTask(RemoteViews rv, ViewGroup parent, Context context, OnViewAppliedListener listener, OnClickHandler handler, View result) {
                            this.mRV = rv;
                            this.mParent = parent;
                            this.mContext = context;
                            this.mListener = listener;
                            this.mHandler = handler;
                            this.mResult = result;
                        }

                        /* access modifiers changed from: protected */
                        public ViewTree doInBackground(Void... params) {
                            try {
                                if (this.mResult == null) {
                                    this.mResult = RemoteViews.this.inflateView(this.mContext, this.mRV, this.mParent);
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
                                OnViewAppliedListener onViewAppliedListener = this.mListener;
                                if (onViewAppliedListener != null) {
                                    onViewAppliedListener.onViewInflated(viewTree.mRoot);
                                }
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
                            OnViewAppliedListener onViewAppliedListener2 = this.mListener;
                            if (onViewAppliedListener2 != null) {
                                Exception exc = this.mError;
                                if (exc != null) {
                                    onViewAppliedListener2.onError(exc);
                                } else {
                                    onViewAppliedListener2.onViewApplied(viewTree.mRoot);
                                }
                            } else {
                                Exception exc2 = this.mError;
                                if (exc2 == null) {
                                    return;
                                }
                                if (exc2 instanceof ActionException) {
                                    throw ((ActionException) exc2);
                                }
                                throw new ActionException(exc2);
                            }
                        }

                        @Override // android.os.CancellationSignal.OnCancelListener
                        public void onCancel() {
                            cancel(true);
                        }
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
                        RemoteViews rvToApply = getRemoteViewsToApply(context);
                        if (hasLandscapeAndPortraitLayouts()) {
                            if (((Integer) v.getTag(16908312)).intValue() != rvToApply.getLayoutId()) {
                                throw new RuntimeException("Attempting to re-apply RemoteViews to a view that that does not share the same root layout id.");
                            }
                        }
                        return startTaskOnExecutor(new AsyncApplyTask(rvToApply, (ViewGroup) v.getParent(), context, listener, handler, v), executor);
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
                        ArrayList<Action> arrayList = this.mActions;
                        if (arrayList == null) {
                            return false;
                        }
                        int count = arrayList.size();
                        for (int i = 0; i < count; i++) {
                            if (this.mActions.get(i).prefersAsyncApply()) {
                                return true;
                            }
                        }
                        return false;
                    }

                    private Context getContextForResources(Context context) {
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
                        ArrayList<Action> arrayList = this.mActions;
                        if (arrayList == null) {
                            return 0;
                        }
                        return arrayList.size();
                    }

                    @Override // android.view.LayoutInflater.Filter
                    public boolean onLoadClass(Class clazz) {
                        return clazz.isAnnotationPresent(RemoteView.class);
                    }

                    @Override // android.os.Parcelable
                    public int describeContents() {
                        return 0;
                    }

                    @Override // android.os.Parcelable
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
                            dest.writeInt(this.mLightBackgroundLayoutId);
                            writeActionsToParcel(dest);
                        } else {
                            dest.writeInt(1);
                            if (this.mIsRoot) {
                                this.mBitmapCache.writeBitmapsToParcel(dest, flags);
                            }
                            this.mLandscape.writeToParcel(dest, flags);
                            this.mPortrait.writeToParcel(dest, flags | 2);
                        }
                        dest.writeInt(this.mApplyFlags);
                    }

                    private void writeActionsToParcel(Parcel parcel) {
                        int count;
                        ArrayList<Action> arrayList = this.mActions;
                        if (arrayList != null) {
                            count = arrayList.size();
                        } else {
                            count = 0;
                        }
                        parcel.writeInt(count);
                        for (int i = 0; i < count; i++) {
                            Action action = this.mActions.get(i);
                            if (action != null) {
                                parcel.writeInt(action.getActionTag());
                                action.writeToParcel(parcel, action.hasSameAppInfo(this.mApplication) ? 2 : 0);
                            }
                        }
                    }

                    private static ApplicationInfo getApplicationInfo(String packageName, int userId) {
                        if (packageName == null) {
                            return null;
                        }
                        Application application = ActivityThread.currentApplication();
                        if (application != null) {
                            ApplicationInfo applicationInfo = application.getApplicationInfo();
                            if (UserHandle.getUserId(applicationInfo.uid) == userId && applicationInfo.packageName.equals(packageName)) {
                                return applicationInfo;
                            }
                            try {
                                return application.getBaseContext().createPackageContextAsUser(packageName, 0, new UserHandle(userId)).getApplicationInfo();
                            } catch (PackageManager.NameNotFoundException e) {
                                throw new IllegalArgumentException("No such package " + packageName);
                            }
                        } else {
                            throw new IllegalStateException("Cannot create remote views out of an aplication.");
                        }
                    }

                    public boolean hasSameAppInfo(ApplicationInfo info) {
                        return this.mApplication.packageName.equals(info.packageName) && this.mApplication.uid == info.uid;
                    }

                    /* access modifiers changed from: private */
                    public static class ViewTree {
                        private static final int INSERT_AT_END_INDEX = -1;
                        private ArrayList<ViewTree> mChildren;
                        private View mRoot;

                        private ViewTree(View root) {
                            this.mRoot = root;
                        }

                        public void createTree() {
                            if (this.mChildren == null) {
                                this.mChildren = new ArrayList<>();
                                View view = this.mRoot;
                                if (view instanceof ViewGroup) {
                                    ViewGroup vg = (ViewGroup) view;
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
                            ArrayList<ViewTree> arrayList = this.mChildren;
                            if (arrayList == null) {
                                return null;
                            }
                            Iterator<ViewTree> it = arrayList.iterator();
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
                                return (T) this.mRoot.findViewById(id);
                            }
                            ViewTree tree = findViewTreeById(id);
                            if (tree == null) {
                                return null;
                            }
                            return (T) tree.mRoot;
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

                    private void setWidgetFactoryHuaWei(Context context, LayoutInflater inflater) {
                        try {
                            inflater.setFactory(HwFrameworkFactory.getHwWidgetManager().createWidgetFactoryHuaWei(context, getPackage()));
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        } catch (NullPointerException e2) {
                            Log.e(LOG_TAG, e2.getMessage());
                        }
                    }

                    public static class RemoteResponse {
                        private ArrayList<String> mElementNames;
                        private Intent mFillIntent;
                        private PendingIntent mPendingIntent;
                        private IntArray mViewIds;

                        public static RemoteResponse fromPendingIntent(PendingIntent pendingIntent) {
                            RemoteResponse response = new RemoteResponse();
                            response.mPendingIntent = pendingIntent;
                            return response;
                        }

                        public static RemoteResponse fromFillInIntent(Intent fillIntent) {
                            RemoteResponse response = new RemoteResponse();
                            response.mFillIntent = fillIntent;
                            return response;
                        }

                        public RemoteResponse addSharedElement(int viewId, String sharedElementName) {
                            if (this.mViewIds == null) {
                                this.mViewIds = new IntArray();
                                this.mElementNames = new ArrayList<>();
                            }
                            this.mViewIds.add(viewId);
                            this.mElementNames.add(sharedElementName);
                            return this;
                        }

                        /* access modifiers changed from: private */
                        /* access modifiers changed from: public */
                        private void writeToParcel(Parcel dest, int flags) {
                            PendingIntent.writePendingIntentOrNullToParcel(this.mPendingIntent, dest);
                            if (this.mPendingIntent == null) {
                                dest.writeTypedObject(this.mFillIntent, flags);
                            }
                            IntArray intArray = this.mViewIds;
                            dest.writeIntArray(intArray == null ? null : intArray.toArray());
                            dest.writeStringList(this.mElementNames);
                        }

                        /* access modifiers changed from: private */
                        /* access modifiers changed from: public */
                        private void readFromParcel(Parcel parcel) {
                            this.mPendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
                            if (this.mPendingIntent == null) {
                                this.mFillIntent = (Intent) parcel.readTypedObject(Intent.CREATOR);
                            }
                            int[] viewIds = parcel.createIntArray();
                            this.mViewIds = viewIds == null ? null : IntArray.wrap(viewIds);
                            this.mElementNames = parcel.createStringArrayList();
                        }

                        /* access modifiers changed from: private */
                        /* access modifiers changed from: public */
                        private void handleViewClick(View v, OnClickHandler handler) {
                            PendingIntent pi;
                            if (this.mPendingIntent != null) {
                                pi = this.mPendingIntent;
                            } else if (this.mFillIntent != null) {
                                View parent = (View) v.getParent();
                                while (parent != null && !(parent instanceof AdapterView) && (!(parent instanceof AppWidgetHostView) || (parent instanceof RemoteViewsAdapter.RemoteViewsFrameLayout))) {
                                    parent = (View) parent.getParent();
                                }
                                if (!(parent instanceof AdapterView)) {
                                    Log.e(RemoteViews.LOG_TAG, "Collection item doesn't have AdapterView parent");
                                    return;
                                } else if (!(parent.getTag() instanceof PendingIntent)) {
                                    Log.e(RemoteViews.LOG_TAG, "Attempting setOnClickFillInIntent without calling setPendingIntentTemplate on parent.");
                                    return;
                                } else {
                                    pi = (PendingIntent) parent.getTag();
                                }
                            } else {
                                Log.e(RemoteViews.LOG_TAG, "Response has neither pendingIntent nor fillInIntent");
                                return;
                            }
                            handler.onClickHandler(v, pi, this);
                        }

                        public Pair<Intent, ActivityOptions> getLaunchOptions(View view) {
                            Intent intent = this.mPendingIntent != null ? new Intent() : new Intent(this.mFillIntent);
                            intent.setSourceBounds(RemoteViews.getSourceBounds(view));
                            ActivityOptions opts = null;
                            Context context = view.getContext();
                            if (context.getResources().getBoolean(R.bool.config_overrideRemoteViewsActivityTransition)) {
                                TypedArray windowStyle = context.getTheme().obtainStyledAttributes(R.styleable.Window);
                                TypedArray windowAnimationStyle = context.obtainStyledAttributes(windowStyle.getResourceId(8, 0), R.styleable.WindowAnimation);
                                int enterAnimationId = windowAnimationStyle.getResourceId(26, 0);
                                windowStyle.recycle();
                                windowAnimationStyle.recycle();
                                if (enterAnimationId != 0) {
                                    opts = ActivityOptions.makeCustomAnimation(context, enterAnimationId, 0);
                                    opts.setPendingIntentLaunchFlags(268435456);
                                }
                            }
                            if (!(opts != null || this.mViewIds == null || this.mElementNames == null)) {
                                View parent = (View) view.getParent();
                                while (parent != null && !(parent instanceof AppWidgetHostView)) {
                                    parent = (View) parent.getParent();
                                }
                                if (parent instanceof AppWidgetHostView) {
                                    int[] array = this.mViewIds.toArray();
                                    ArrayList<String> arrayList = this.mElementNames;
                                    opts = ((AppWidgetHostView) parent).createSharedElementActivityOptions(array, (String[]) arrayList.toArray(new String[arrayList.size()]), intent);
                                }
                            }
                            if (opts == null) {
                                opts = ActivityOptions.makeBasic();
                                opts.setPendingIntentLaunchFlags(268435456);
                            }
                            return Pair.create(intent, opts);
                        }
                    }

                    public static boolean startPendingIntent(View view, PendingIntent pendingIntent, Pair<Intent, ActivityOptions> options) {
                        try {
                            Context context = view.getContext();
                            addLaunchFlagIfNeed(view, options.second);
                            context.startIntentSender(pendingIntent.getIntentSender(), options.first, 0, 0, 0, options.second.toBundle());
                            return true;
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(LOG_TAG, "Cannot send pending intent: ", e);
                            return false;
                        } catch (Exception e2) {
                            Log.e(LOG_TAG, "Cannot send pending intent due to unknown exception: ", e2);
                            return false;
                        }
                    }

                    public int getCacheSize() {
                        BitmapCache bitmapCache = this.mBitmapCache;
                        if (bitmapCache != null) {
                            return bitmapCache.getSize();
                        }
                        return 0;
                    }

                    private static void addLaunchFlagIfNeed(View view, ActivityOptions opts) {
                        if (view != null && 34603197 == view.getId() && FREEFORM.equals(view.getTag())) {
                            Log.i(LOG_TAG, "Launch activity in freeform");
                            opts.setLaunchWindowingMode(5);
                        }
                    }
                }

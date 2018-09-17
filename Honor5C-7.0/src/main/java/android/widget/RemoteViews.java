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
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.StrictMode;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Filter;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView.OnItemClickListener;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import libcore.util.Objects;

public class RemoteViews implements Parcelable, Filter {
    private static final Action ACTION_NOOP = null;
    public static final Creator<RemoteViews> CREATOR = null;
    private static final OnClickHandler DEFAULT_ON_CLICK_HANDLER = null;
    static final String EXTRA_REMOTEADAPTER_APPWIDGET_ID = "remoteAdapterAppWidgetId";
    private static final String LOG_TAG = "RemoteViews";
    private static final int MODE_HAS_LANDSCAPE_AND_PORTRAIT = 1;
    private static final int MODE_NORMAL = 0;
    private static final ArrayMap<Method, Method> sAsyncMethods = null;
    private static final ThreadLocal<Object[]> sInvokeArgsTls = null;
    private static final ArrayMap<Class<? extends View>, ArrayMap<MutablePair<String, Class<?>>, Method>> sMethods = null;
    private static final Object[] sMethodsLock = null;
    private ArrayList<Action> mActions;
    private ApplicationInfo mApplication;
    private BitmapCache mBitmapCache;
    private boolean mIsRoot;
    private boolean mIsWidgetCollectionChild;
    private RemoteViews mLandscape;
    private final int mLayoutId;
    private MemoryUsageCounter mMemoryUsageCounter;
    private final MutablePair<String, Class<?>> mPair;
    private RemoteViews mPortrait;

    private static abstract class Action implements Parcelable {
        public static final int MERGE_APPEND = 1;
        public static final int MERGE_IGNORE = 2;
        public static final int MERGE_REPLACE = 0;
        int viewId;

        public abstract void apply(View view, ViewGroup viewGroup, OnClickHandler onClickHandler) throws ActionException;

        public abstract String getActionName();

        private Action() {
        }

        public int describeContents() {
            return 0;
        }

        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
        }

        public int mergeBehavior() {
            return 0;
        }

        public String getUniqueKey() {
            return getActionName() + this.viewId;
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            return this;
        }
    }

    private static abstract class RuntimeAction extends Action {
        private RuntimeAction() {
            super();
        }

        public final String getActionName() {
            return "RuntimeAction";
        }

        public final void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException();
        }
    }

    /* renamed from: android.widget.RemoteViews.4 */
    class AnonymousClass4 extends ContextWrapper {
        final /* synthetic */ Context val$contextForResources;

        AnonymousClass4(Context $anonymous0, Context val$contextForResources) {
            this.val$contextForResources = val$contextForResources;
            super($anonymous0);
        }

        public Resources getResources() {
            return this.val$contextForResources.getResources();
        }

        public Theme getTheme() {
            return this.val$contextForResources.getTheme();
        }

        public String getPackageName() {
            return this.val$contextForResources.getPackageName();
        }
    }

    public static class ActionException extends RuntimeException {
        public ActionException(Exception ex) {
            super(ex);
        }

        public ActionException(String message) {
            super(message);
        }
    }

    private class AsyncApplyTask extends AsyncTask<Void, Void, ViewTree> implements OnCancelListener {
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
            RemoteViews.loadTransitionOverride(context, handler);
        }

        protected ViewTree doInBackground(Void... params) {
            try {
                if (this.mResult == null) {
                    this.mResult = RemoteViews.this.inflateView(this.mContext, RemoteViews.this.getContextForResources(this.mContext), this.mRV, this.mParent);
                }
                this.mTree = new ViewTree(null);
                if (this.mRV.mActions != null) {
                    int count = this.mRV.mActions.size();
                    this.mActions = new Action[count];
                    for (int i = 0; i < count && !isCancelled(); i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
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

        protected void onPostExecute(ViewTree viewTree) {
            if (this.mError == null) {
                try {
                    if (this.mActions != null) {
                        OnClickHandler handler = this.mHandler == null ? RemoteViews.DEFAULT_ON_CLICK_HANDLER : this.mHandler;
                        Action[] actionArr = this.mActions;
                        int length = actionArr.length;
                        for (int i = 0; i < length; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                            actionArr[i].apply(viewTree.mRoot, this.mParent, handler);
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
        static final int mMaxNumOfBitmaps = 50;
        ArrayList<Bitmap> mBitmaps;
        int mBitmapsCount;

        public BitmapCache() {
            this.mBitmapsCount = 0;
            this.mBitmaps = new ArrayList();
        }

        public int getSize() {
            return this.mBitmaps.size();
        }

        public BitmapCache(Parcel source) {
            this.mBitmapsCount = 0;
            int count = source.readInt();
            this.mBitmaps = new ArrayList();
            for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                addBitmapsForSafe((Bitmap) Bitmap.CREATOR.createFromParcel(source));
            }
        }

        public int getBitmapId(Bitmap b) {
            if (b == null) {
                return -1;
            }
            if (this.mBitmaps.contains(b)) {
                return this.mBitmaps.indexOf(b);
            }
            return addBitmapsForSafe(b);
        }

        public Bitmap getBitmapForId(int id) {
            if (id == -1 || id >= this.mBitmaps.size()) {
                return null;
            }
            return (Bitmap) this.mBitmaps.get(id);
        }

        public void writeBitmapsToParcel(Parcel dest, int flags) {
            int count = this.mBitmaps.size();
            dest.writeInt(count);
            for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                ((Bitmap) this.mBitmaps.get(i)).writeToParcel(dest, flags);
            }
        }

        private int addBitmapsForSafe(Bitmap b) {
            if (this.mBitmaps == null) {
                return -1;
            }
            int index;
            if (this.mBitmapsCount < mMaxNumOfBitmaps) {
                this.mBitmaps.add(b);
                index = this.mBitmapsCount;
            } else {
                index = this.mBitmapsCount % mMaxNumOfBitmaps;
                this.mBitmaps.set(index, b);
                Log.w(RemoteViews.LOG_TAG, "RemoteViews try to cache " + this.mBitmapsCount + " bitmaps, only allows " + mMaxNumOfBitmaps + ", replace bitmap at index " + index);
            }
            this.mBitmapsCount += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
            return index;
        }

        public void assimilate(BitmapCache bitmapCache) {
            ArrayList<Bitmap> bitmapsToBeAdded = bitmapCache.mBitmaps;
            int count = bitmapsToBeAdded.size();
            for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                Bitmap b = (Bitmap) bitmapsToBeAdded.get(i);
                if (!this.mBitmaps.contains(b)) {
                    addBitmapsForSafe(b);
                }
            }
        }

        public void addBitmapMemory(MemoryUsageCounter memoryCounter) {
            for (int i = 0; i < this.mBitmaps.size(); i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                memoryCounter.addBitmapMemory((Bitmap) this.mBitmaps.get(i));
            }
        }

        protected BitmapCache clone() {
            BitmapCache bitmapCache = new BitmapCache();
            bitmapCache.mBitmaps.addAll(this.mBitmaps);
            return bitmapCache;
        }
    }

    private class BitmapReflectionAction extends Action {
        public static final int TAG = 12;
        Bitmap bitmap;
        int bitmapId;
        String methodName;

        BitmapReflectionAction(int viewId, String methodName, Bitmap bitmap) {
            super();
            this.bitmap = bitmap;
            this.viewId = viewId;
            this.methodName = methodName;
            this.bitmapId = RemoteViews.this.mBitmapCache.getBitmapId(bitmap);
        }

        BitmapReflectionAction(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.bitmapId = in.readInt();
            this.bitmap = RemoteViews.this.mBitmapCache.getBitmapForId(this.bitmapId);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            dest.writeString(this.methodName);
            dest.writeInt(this.bitmapId);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
            new ReflectionAction(this.viewId, this.methodName, TAG, this.bitmap).apply(root, rootParent, handler);
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            this.bitmapId = bitmapCache.getBitmapId(this.bitmap);
        }

        public String getActionName() {
            return "BitmapReflectionAction";
        }
    }

    private static class LayoutParamAction extends Action {
        public static final int LAYOUT_MARGIN_BOTTOM_DIMEN = 3;
        public static final int LAYOUT_MARGIN_END_DIMEN = 1;
        public static final int LAYOUT_WIDTH = 2;
        public static final int TAG = 19;
        int property;
        int value;

        public LayoutParamAction(int viewId, int property, int value) {
            super();
            this.viewId = viewId;
            this.property = property;
            this.value = value;
        }

        public LayoutParamAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.property = parcel.readInt();
            this.value = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            dest.writeInt(this.property);
            dest.writeInt(this.value);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                LayoutParams layoutParams = target.getLayoutParams();
                if (layoutParams != null) {
                    switch (this.property) {
                        case LAYOUT_MARGIN_END_DIMEN /*1*/:
                            if (layoutParams instanceof MarginLayoutParams) {
                                ((MarginLayoutParams) layoutParams).setMarginEnd(resolveDimenPixelOffset(target, this.value));
                                target.setLayoutParams(layoutParams);
                                break;
                            }
                            break;
                        case LAYOUT_WIDTH /*2*/:
                            layoutParams.width = this.value;
                            target.setLayoutParams(layoutParams);
                            break;
                        case LAYOUT_MARGIN_BOTTOM_DIMEN /*3*/:
                            if (layoutParams instanceof MarginLayoutParams) {
                                ((MarginLayoutParams) layoutParams).bottomMargin = resolveDimenPixelOffset(target, this.value);
                                target.setLayoutParams(layoutParams);
                                break;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown property " + this.property);
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

        public String getActionName() {
            return "LayoutParamAction" + this.property + ".";
        }
    }

    private class MemoryUsageCounter {
        private static final /* synthetic */ int[] -android-graphics-Bitmap$ConfigSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$graphics$Bitmap$Config;
        int mMemoryUsage;

        private static /* synthetic */ int[] -getandroid-graphics-Bitmap$ConfigSwitchesValues() {
            if (-android-graphics-Bitmap$ConfigSwitchesValues != null) {
                return -android-graphics-Bitmap$ConfigSwitchesValues;
            }
            int[] iArr = new int[Config.values().length];
            try {
                iArr[Config.ALPHA_8.ordinal()] = RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Config.ARGB_4444.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Config.ARGB_8888.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Config.RGB_565.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -android-graphics-Bitmap$ConfigSwitchesValues = iArr;
            return iArr;
        }

        private MemoryUsageCounter() {
        }

        public void clear() {
            this.mMemoryUsage = 0;
        }

        public void increment(int numBytes) {
            this.mMemoryUsage += numBytes;
        }

        public int getMemoryUsage() {
            return this.mMemoryUsage;
        }

        public void addBitmapMemory(Bitmap b) {
            Config c = b.getConfig();
            int bpp = 4;
            if (c != null) {
                switch (-getandroid-graphics-Bitmap$ConfigSwitchesValues()[c.ordinal()]) {
                    case RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT /*1*/:
                        bpp = RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
                        break;
                    case HwCfgFilePolicy.PC /*2*/:
                    case HwCfgFilePolicy.CUST /*4*/:
                        bpp = 2;
                        break;
                    case HwCfgFilePolicy.BASE /*3*/:
                        bpp = 4;
                        break;
                }
            }
            increment((b.getWidth() * b.getHeight()) * bpp);
        }
    }

    static class MutablePair<F, S> {
        F first;
        S second;

        MutablePair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof MutablePair)) {
                return false;
            }
            MutablePair<?, ?> p = (MutablePair) o;
            if (Objects.equal(p.first, this.first)) {
                z = Objects.equal(p.second, this.second);
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
            return onClickHandler(view, pendingIntent, fillInIntent, -1);
        }

        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent, int launchStackId) {
            try {
                ActivityOptions opts;
                Context context = view.getContext();
                if (this.mEnterAnimationId != 0) {
                    opts = ActivityOptions.makeCustomAnimation(context, this.mEnterAnimationId, 0);
                } else {
                    opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                }
                if (launchStackId != -1) {
                    opts.setLaunchStackId(launchStackId);
                }
                context.startIntentSender(pendingIntent.getIntentSender(), fillInIntent, EditorInfo.IME_FLAG_NO_EXTRACT_UI, EditorInfo.IME_FLAG_NO_EXTRACT_UI, 0, opts.toBundle());
                return true;
            } catch (SendIntentException e) {
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
    }

    public interface OnViewAppliedListener {
        void onError(Exception exception);

        void onViewApplied(View view);
    }

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
        static final int TAG = 2;
        static final int URI = 11;
        String methodName;
        int type;
        Object value;

        ReflectionAction(int viewId, String methodName, int type, Object value) {
            super();
            this.viewId = viewId;
            this.methodName = methodName;
            this.type = type;
            this.value = value;
        }

        ReflectionAction(RemoteViews this$0, Parcel in) {
            boolean z = false;
            RemoteViews.this = this$0;
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.type = in.readInt();
            switch (this.type) {
                case BOOLEAN /*1*/:
                    if (in.readInt() != 0) {
                        z = true;
                    }
                    this.value = Boolean.valueOf(z);
                case TAG /*2*/:
                    this.value = Byte.valueOf(in.readByte());
                case SHORT /*3*/:
                    this.value = Short.valueOf((short) in.readInt());
                case INT /*4*/:
                    this.value = Integer.valueOf(in.readInt());
                case LONG /*5*/:
                    this.value = Long.valueOf(in.readLong());
                case FLOAT /*6*/:
                    this.value = Float.valueOf(in.readFloat());
                case DOUBLE /*7*/:
                    this.value = Double.valueOf(in.readDouble());
                case CHAR /*8*/:
                    this.value = Character.valueOf((char) in.readInt());
                case STRING /*9*/:
                    this.value = in.readString();
                case CHAR_SEQUENCE /*10*/:
                    this.value = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                case URI /*11*/:
                    if (in.readInt() != 0) {
                        this.value = Uri.CREATOR.createFromParcel(in);
                    }
                case BITMAP /*12*/:
                    if (in.readInt() != 0) {
                        this.value = Bitmap.CREATOR.createFromParcel(in);
                    }
                case BUNDLE /*13*/:
                    this.value = in.readBundle();
                case INTENT /*14*/:
                    if (in.readInt() != 0) {
                        this.value = Intent.CREATOR.createFromParcel(in);
                    }
                case COLOR_STATE_LIST /*15*/:
                    if (in.readInt() != 0) {
                        this.value = ColorStateList.CREATOR.createFromParcel(in);
                    }
                case ICON /*16*/:
                    if (in.readInt() != 0) {
                        this.value = Icon.CREATOR.createFromParcel(in);
                    }
                default:
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            int i = BOOLEAN;
            out.writeInt(TAG);
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
            out.writeInt(this.type);
            switch (this.type) {
                case BOOLEAN /*1*/:
                    out.writeInt(((Boolean) this.value).booleanValue() ? BOOLEAN : 0);
                case TAG /*2*/:
                    out.writeByte(((Byte) this.value).byteValue());
                case SHORT /*3*/:
                    out.writeInt(((Short) this.value).shortValue());
                case INT /*4*/:
                    out.writeInt(((Integer) this.value).intValue());
                case LONG /*5*/:
                    out.writeLong(((Long) this.value).longValue());
                case FLOAT /*6*/:
                    out.writeFloat(((Float) this.value).floatValue());
                case DOUBLE /*7*/:
                    out.writeDouble(((Double) this.value).doubleValue());
                case CHAR /*8*/:
                    out.writeInt(((Character) this.value).charValue());
                case STRING /*9*/:
                    out.writeString((String) this.value);
                case CHAR_SEQUENCE /*10*/:
                    TextUtils.writeToParcel((CharSequence) this.value, out, flags);
                case URI /*11*/:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Uri) this.value).writeToParcel(out, flags);
                    }
                case BITMAP /*12*/:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Bitmap) this.value).writeToParcel(out, flags);
                    }
                case BUNDLE /*13*/:
                    out.writeBundle((Bundle) this.value);
                case INTENT /*14*/:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Intent) this.value).writeToParcel(out, flags);
                    }
                case COLOR_STATE_LIST /*15*/:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((ColorStateList) this.value).writeToParcel(out, flags);
                    }
                case ICON /*16*/:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Icon) this.value).writeToParcel(out, flags);
                    }
                default:
            }
        }

        private Class<?> getParameterType() {
            switch (this.type) {
                case BOOLEAN /*1*/:
                    return Boolean.TYPE;
                case TAG /*2*/:
                    return Byte.TYPE;
                case SHORT /*3*/:
                    return Short.TYPE;
                case INT /*4*/:
                    return Integer.TYPE;
                case LONG /*5*/:
                    return Long.TYPE;
                case FLOAT /*6*/:
                    return Float.TYPE;
                case DOUBLE /*7*/:
                    return Double.TYPE;
                case CHAR /*8*/:
                    return Character.TYPE;
                case STRING /*9*/:
                    return String.class;
                case CHAR_SEQUENCE /*10*/:
                    return CharSequence.class;
                case URI /*11*/:
                    return Uri.class;
                case BITMAP /*12*/:
                    return Bitmap.class;
                case BUNDLE /*13*/:
                    return Bundle.class;
                case INTENT /*14*/:
                    return Intent.class;
                case COLOR_STATE_LIST /*15*/:
                    return ColorStateList.class;
                case ICON /*16*/:
                    return Icon.class;
                default:
                    return null;
            }
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view != null) {
                Class<?> param = getParameterType();
                if (param == null) {
                    throw new ActionException("bad type: " + this.type);
                }
                try {
                    RemoteViews.this.getMethod(view, this.methodName, param).invoke(view, RemoteViews.wrapArg(this.value));
                } catch (ActionException e) {
                    throw e;
                } catch (Exception ex) {
                    throw new ActionException(ex);
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view == null) {
                return RemoteViews.ACTION_NOOP;
            }
            Class<?> param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + this.type);
            }
            try {
                Method asyncMethod = RemoteViews.this.getAsyncMethod(RemoteViews.this.getMethod(view, this.methodName, param));
                if (asyncMethod == null) {
                    return this;
                }
                Runnable endAction = (Runnable) asyncMethod.invoke(view, RemoteViews.wrapArg(this.value));
                if (endAction == null) {
                    return RemoteViews.ACTION_NOOP;
                }
                return new RunnableAction(endAction);
            } catch (ActionException e) {
                throw e;
            } catch (Exception ex) {
                throw new ActionException(ex);
            }
        }

        public int mergeBehavior() {
            if (this.methodName.equals("smoothScrollBy")) {
                return BOOLEAN;
            }
            return 0;
        }

        public String getActionName() {
            return "ReflectionAction" + this.methodName + this.type;
        }
    }

    private final class ReflectionActionWithoutParams extends Action {
        public static final int TAG = 5;
        final String methodName;

        ReflectionActionWithoutParams(int viewId, String methodName) {
            super();
            this.viewId = viewId;
            this.methodName = methodName;
        }

        ReflectionActionWithoutParams(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(TAG);
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view != null) {
                try {
                    RemoteViews.this.getMethod(view, this.methodName, null).invoke(view, new Object[0]);
                } catch (ActionException e) {
                    throw e;
                } catch (Exception ex) {
                    throw new ActionException(ex);
                }
            }
        }

        public int mergeBehavior() {
            if (this.methodName.equals("showNext") || this.methodName.equals("showPrevious")) {
                return 2;
            }
            return 0;
        }

        public String getActionName() {
            return "ReflectionActionWithoutParams";
        }
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RemoteView {
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

    private class SetDrawableParameters extends Action {
        public static final int TAG = 3;
        int alpha;
        int colorFilter;
        Mode filterMode;
        int level;
        boolean targetBackground;

        public SetDrawableParameters(int id, boolean targetBackground, int alpha, int colorFilter, Mode mode, int level) {
            super();
            this.viewId = id;
            this.targetBackground = targetBackground;
            this.alpha = alpha;
            this.colorFilter = colorFilter;
            this.filterMode = mode;
            this.level = level;
        }

        public SetDrawableParameters(Parcel parcel) {
            boolean z;
            boolean hasMode;
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.targetBackground = z;
            this.alpha = parcel.readInt();
            this.colorFilter = parcel.readInt();
            if (parcel.readInt() != 0) {
                hasMode = true;
            } else {
                hasMode = false;
            }
            if (hasMode) {
                this.filterMode = Mode.valueOf(parcel.readString());
            } else {
                this.filterMode = null;
            }
            this.level = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            if (this.targetBackground) {
                i = RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.alpha);
            dest.writeInt(this.colorFilter);
            if (this.filterMode != null) {
                dest.writeInt(RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT);
                dest.writeString(this.filterMode.toString());
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.level);
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
                    if (this.alpha != -1) {
                        targetDrawable.mutate().setAlpha(this.alpha);
                    }
                    if (this.filterMode != null) {
                        targetDrawable.mutate().setColorFilter(this.colorFilter, this.filterMode);
                    }
                    if (this.level != -1) {
                        targetDrawable.mutate().setLevel(this.level);
                    }
                }
            }
        }

        public String getActionName() {
            return "SetDrawableParameters";
        }
    }

    private class SetEmptyView extends Action {
        public static final int TAG = 6;
        int emptyViewId;
        int viewId;

        SetEmptyView(int viewId, int emptyViewId) {
            super();
            this.viewId = viewId;
            this.emptyViewId = emptyViewId;
        }

        SetEmptyView(Parcel in) {
            super();
            this.viewId = in.readInt();
            this.emptyViewId = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(TAG);
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

        public String getActionName() {
            return "SetEmptyView";
        }
    }

    private class SetOnClickFillInIntent extends Action {
        public static final int TAG = 9;
        Intent fillInIntent;

        /* renamed from: android.widget.RemoteViews.SetOnClickFillInIntent.1 */
        class AnonymousClass1 implements OnClickListener {
            final /* synthetic */ OnClickHandler val$handler;

            AnonymousClass1(OnClickHandler val$handler) {
                this.val$handler = val$handler;
            }

            public void onClick(View v) {
                View parent = (View) v.getParent();
                while (parent != null && !(parent instanceof AdapterView) && (!(parent instanceof AppWidgetHostView) || (parent instanceof RemoteViewsFrameLayout))) {
                    parent = (View) parent.getParent();
                }
                if (!(parent instanceof AdapterView)) {
                    Log.e(RemoteViews.LOG_TAG, "Collection item doesn't have AdapterView parent");
                } else if (parent.getTag() instanceof PendingIntent) {
                    PendingIntent pendingIntent = (PendingIntent) parent.getTag();
                    SetOnClickFillInIntent.this.fillInIntent.setSourceBounds(RemoteViews.getSourceBounds(v));
                    this.val$handler.onClickHandler(v, pendingIntent, SetOnClickFillInIntent.this.fillInIntent);
                } else {
                    Log.e(RemoteViews.LOG_TAG, "Attempting setOnClickFillInIntent without calling setPendingIntentTemplate on parent.");
                }
            }
        }

        public SetOnClickFillInIntent(int id, Intent fillInIntent) {
            super();
            this.viewId = id;
            this.fillInIntent = fillInIntent;
        }

        public SetOnClickFillInIntent(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.fillInIntent = (Intent) Intent.CREATOR.createFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            this.fillInIntent.writeToParcel(dest, 0);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (RemoteViews.this.mIsWidgetCollectionChild) {
                    if (target == root) {
                        target.setTagInternal(R.id.fillInIntent, this.fillInIntent);
                    } else if (this.fillInIntent != null) {
                        target.setOnClickListener(new AnonymousClass1(handler));
                    }
                    return;
                }
                Log.e(RemoteViews.LOG_TAG, "The method setOnClickFillInIntent is available only from RemoteViewsFactory (ie. on collection items).");
            }
        }

        public String getActionName() {
            return "SetOnClickFillInIntent";
        }
    }

    private class SetOnClickPendingIntent extends Action {
        public static final int TAG = 1;
        PendingIntent pendingIntent;

        /* renamed from: android.widget.RemoteViews.SetOnClickPendingIntent.1 */
        class AnonymousClass1 implements OnClickListener {
            final /* synthetic */ OnClickHandler val$handler;

            AnonymousClass1(OnClickHandler val$handler) {
                this.val$handler = val$handler;
            }

            public void onClick(View v) {
                Rect rect = RemoteViews.getSourceBounds(v);
                Intent intent = new Intent();
                intent.setSourceBounds(rect);
                this.val$handler.onClickHandler(v, SetOnClickPendingIntent.this.pendingIntent, intent);
            }
        }

        public SetOnClickPendingIntent(int id, PendingIntent pendingIntent) {
            super();
            this.viewId = id;
            this.pendingIntent = pendingIntent;
        }

        public SetOnClickPendingIntent(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                this.pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = TAG;
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            if (this.pendingIntent == null) {
                i = 0;
            }
            dest.writeInt(i);
            if (this.pendingIntent != null) {
                this.pendingIntent.writeToParcel(dest, 0);
            }
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (RemoteViews.this.mIsWidgetCollectionChild) {
                    Log.w(RemoteViews.LOG_TAG, "Cannot setOnClickPendingIntent for collection item (id: " + this.viewId + ")");
                    ApplicationInfo appInfo = root.getContext().getApplicationInfo();
                    if (appInfo != null && appInfo.targetSdkVersion >= 16) {
                        return;
                    }
                }
                OnClickListener onClickListener = null;
                if (this.pendingIntent != null) {
                    onClickListener = new AnonymousClass1(handler);
                }
                target.setOnClickListener(onClickListener);
            }
        }

        public String getActionName() {
            return "SetOnClickPendingIntent";
        }
    }

    private class SetPendingIntentTemplate extends Action {
        public static final int TAG = 8;
        PendingIntent pendingIntentTemplate;

        /* renamed from: android.widget.RemoteViews.SetPendingIntentTemplate.1 */
        class AnonymousClass1 implements OnItemClickListener {
            final /* synthetic */ OnClickHandler val$handler;

            AnonymousClass1(OnClickHandler val$handler) {
                this.val$handler = val$handler;
            }

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) view;
                    if (parent instanceof AdapterViewAnimator) {
                        vg = (ViewGroup) vg.getChildAt(0);
                    }
                    if (vg != null) {
                        Intent fillInIntent = null;
                        int childCount = vg.getChildCount();
                        for (int i = 0; i < childCount; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                            Intent tag = vg.getChildAt(i).getTag(R.id.fillInIntent);
                            if (tag instanceof Intent) {
                                fillInIntent = tag;
                                break;
                            }
                        }
                        if (fillInIntent != null) {
                            new Intent().setSourceBounds(RemoteViews.getSourceBounds(view));
                            this.val$handler.onClickHandler(view, SetPendingIntentTemplate.this.pendingIntentTemplate, fillInIntent);
                        }
                    }
                }
            }
        }

        public SetPendingIntentTemplate(int id, PendingIntent pendingIntentTemplate) {
            super();
            this.viewId = id;
            this.pendingIntentTemplate = pendingIntentTemplate;
        }

        public SetPendingIntentTemplate(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.pendingIntentTemplate = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            this.pendingIntentTemplate.writeToParcel(dest, 0);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (target instanceof AdapterView) {
                    AdapterView<?> av = (AdapterView) target;
                    av.setOnItemClickListener(new AnonymousClass1(handler));
                    av.setTag(this.pendingIntentTemplate);
                    return;
                }
                Log.e(RemoteViews.LOG_TAG, "Cannot setPendingIntentTemplate on a view which is notan AdapterView (id: " + this.viewId + ")");
            }
        }

        public String getActionName() {
            return "SetPendingIntentTemplate";
        }
    }

    private class SetRemoteInputsAction extends Action {
        public static final int TAG = 18;
        final Parcelable[] remoteInputs;

        public SetRemoteInputsAction(int viewId, RemoteInput[] remoteInputs) {
            super();
            this.viewId = viewId;
            this.remoteInputs = remoteInputs;
        }

        public SetRemoteInputsAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.remoteInputs = (Parcelable[]) parcel.createTypedArray(RemoteInput.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            dest.writeTypedArray(this.remoteInputs, flags);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target != null) {
                target.setTagInternal(R.id.remote_input_tag, this.remoteInputs);
            }
        }

        public String getActionName() {
            return "SetRemoteInputsAction";
        }
    }

    private class SetRemoteViewsAdapterIntent extends Action {
        public static final int TAG = 10;
        Intent intent;

        public SetRemoteViewsAdapterIntent(int id, Intent intent) {
            super();
            this.viewId = id;
            this.intent = intent;
        }

        public SetRemoteViewsAdapterIntent(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.intent = (Intent) Intent.CREATOR.createFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            this.intent.writeToParcel(dest, flags);
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
                        v.setRemoteViewsAdapter(this.intent);
                        v.setRemoteViewsOnClickHandler(handler);
                    } else if (target instanceof AdapterViewAnimator) {
                        AdapterViewAnimator v2 = (AdapterViewAnimator) target;
                        v2.setRemoteViewsAdapter(this.intent);
                        v2.setRemoteViewsOnClickHandler(handler);
                    }
                } else {
                    Log.e(RemoteViews.LOG_TAG, "Cannot setRemoteViewsAdapter on a view which is not an AbsListView or AdapterViewAnimator (id: " + this.viewId + ")");
                }
            }
        }

        public String getActionName() {
            return "SetRemoteViewsAdapterIntent";
        }
    }

    private class SetRemoteViewsAdapterList extends Action {
        public static final int TAG = 15;
        ArrayList<RemoteViews> list;
        int viewTypeCount;

        public SetRemoteViewsAdapterList(int id, ArrayList<RemoteViews> list, int viewTypeCount) {
            super();
            this.viewId = id;
            this.list = list;
            this.viewTypeCount = viewTypeCount;
        }

        public SetRemoteViewsAdapterList(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.viewTypeCount = parcel.readInt();
            int count = parcel.readInt();
            this.list = new ArrayList();
            for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                this.list.add((RemoteViews) RemoteViews.CREATOR.createFromParcel(parcel));
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            dest.writeInt(this.viewTypeCount);
            if (this.list == null || this.list.size() == 0) {
                dest.writeInt(0);
                return;
            }
            int count = this.list.size();
            dest.writeInt(count);
            for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                ((RemoteViews) this.list.get(i)).writeToParcel(dest, flags);
            }
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (!(rootParent instanceof AppWidgetHostView)) {
                    Log.e(RemoteViews.LOG_TAG, "SetRemoteViewsAdapterIntent action can only be used for AppWidgets (root id: " + this.viewId + ")");
                } else if ((target instanceof AbsListView) || (target instanceof AdapterViewAnimator)) {
                    Adapter a;
                    if (target instanceof AbsListView) {
                        AbsListView v = (AbsListView) target;
                        a = v.getAdapter();
                        if (!(a instanceof RemoteViewsListAdapter) || this.viewTypeCount > a.getViewTypeCount()) {
                            v.setAdapter(new RemoteViewsListAdapter(v.getContext(), this.list, this.viewTypeCount));
                        } else {
                            ((RemoteViewsListAdapter) a).setViewsList(this.list);
                        }
                    } else if (target instanceof AdapterViewAnimator) {
                        AdapterViewAnimator v2 = (AdapterViewAnimator) target;
                        a = v2.getAdapter();
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

        public String getActionName() {
            return "SetRemoteViewsAdapterList";
        }
    }

    private class TextViewDrawableAction extends Action {
        public static final int TAG = 11;
        int d1;
        int d2;
        int d3;
        int d4;
        boolean drawablesLoaded;
        int height;
        Icon i1;
        Icon i2;
        Icon i3;
        Icon i4;
        Drawable id1;
        Drawable id2;
        Drawable id3;
        Drawable id4;
        boolean isRelative;
        int padding;
        boolean useIcons;
        int width;

        public TextViewDrawableAction(int viewId, boolean isRelative, int d1, int d2, int d3, int d4) {
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.useIcons = false;
            this.d1 = d1;
            this.d2 = d2;
            this.d3 = d3;
            this.d4 = d4;
        }

        public TextViewDrawableAction(int viewId, boolean isRelative, Icon i1, Icon i2, Icon i3, Icon i4) {
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.useIcons = true;
            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
            this.i4 = i4;
        }

        public TextViewDrawableAction(int viewId, boolean isRelative, Icon i1, Icon i2, Icon i3, Icon i4, int drawableWidth, int drawableHeight, int drawablePadding) {
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.useIcons = true;
            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
            this.i4 = i4;
            this.width = drawableWidth;
            this.height = drawableHeight;
            this.padding = drawablePadding;
        }

        public TextViewDrawableAction(RemoteViews this$0, Parcel parcel) {
            boolean z;
            boolean z2 = true;
            RemoteViews.this = this$0;
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.isRelative = z;
            if (parcel.readInt() == 0) {
                z2 = false;
            }
            this.useIcons = z2;
            if (this.useIcons) {
                if (parcel.readInt() != 0) {
                    this.i1 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                }
                if (parcel.readInt() != 0) {
                    this.i2 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                }
                if (parcel.readInt() != 0) {
                    this.i3 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                }
                if (parcel.readInt() != 0) {
                    this.i4 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                    return;
                }
                return;
            }
            this.d1 = parcel.readInt();
            this.d2 = parcel.readInt();
            this.d3 = parcel.readInt();
            this.d4 = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            if (this.isRelative) {
                i = RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (this.useIcons) {
                i = RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (this.useIcons) {
                if (this.i1 != null) {
                    dest.writeInt(RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT);
                    this.i1.writeToParcel(dest, 0);
                } else {
                    dest.writeInt(0);
                }
                if (this.i2 != null) {
                    dest.writeInt(RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT);
                    this.i2.writeToParcel(dest, 0);
                } else {
                    dest.writeInt(0);
                }
                if (this.i3 != null) {
                    dest.writeInt(RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT);
                    this.i3.writeToParcel(dest, 0);
                } else {
                    dest.writeInt(0);
                }
                if (this.i4 != null) {
                    dest.writeInt(RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT);
                    this.i4.writeToParcel(dest, 0);
                    return;
                }
                dest.writeInt(0);
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
                    if (this.isRelative) {
                        target.setCompoundDrawablesRelativeWithIntrinsicBounds(this.id1, this.id2, this.id3, this.id4);
                    } else {
                        target.setCompoundDrawablesWithIntrinsicBounds(this.id1, this.id2, this.id3, this.id4);
                    }
                } else if (this.useIcons) {
                    Context ctx = target.getContext();
                    Drawable loadDrawable = this.i1 == null ? null : this.i1.loadDrawable(ctx);
                    Drawable loadDrawable2 = this.i2 == null ? null : this.i2.loadDrawable(ctx);
                    Drawable loadDrawable3 = this.i3 == null ? null : this.i3.loadDrawable(ctx);
                    Drawable loadDrawable4 = this.i4 == null ? null : this.i4.loadDrawable(ctx);
                    if (!this.isRelative) {
                        target.setCompoundDrawablesWithIntrinsicBounds(loadDrawable, loadDrawable2, loadDrawable3, loadDrawable4);
                    } else if (this.width == 0 || this.height == 0) {
                        target.setCompoundDrawablesRelativeWithIntrinsicBounds(loadDrawable, loadDrawable2, loadDrawable3, loadDrawable4);
                    } else {
                        if (loadDrawable != null) {
                            loadDrawable.setBounds(0, 0, this.width, this.height);
                        }
                        if (loadDrawable2 != null) {
                            loadDrawable2.setBounds(0, 0, this.width, this.height);
                        }
                        if (loadDrawable3 != null) {
                            loadDrawable3.setBounds(0, 0, this.width, this.height);
                        }
                        if (loadDrawable4 != null) {
                            loadDrawable4.setBounds(0, 0, this.width, this.height);
                        }
                        target.setCompoundDrawablesRelative(loadDrawable, loadDrawable2, loadDrawable3, loadDrawable4);
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
            Drawable drawable = null;
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target == null) {
                return RemoteViews.ACTION_NOOP;
            }
            TextViewDrawableAction copy;
            if (this.useIcons) {
                copy = new TextViewDrawableAction(this.viewId, this.isRelative, this.i1, this.i2, this.i3, this.i4);
            } else {
                copy = new TextViewDrawableAction(this.viewId, this.isRelative, this.d1, this.d2, this.d3, this.d4);
            }
            copy.drawablesLoaded = true;
            Context ctx = target.getContext();
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

        public String getActionName() {
            return "TextViewDrawableAction";
        }
    }

    private class TextViewDrawableColorFilterAction extends Action {
        public static final int TAG = 17;
        final int color;
        final int index;
        final boolean isRelative;
        final Mode mode;

        public TextViewDrawableColorFilterAction(int viewId, boolean isRelative, int index, int color, Mode mode) {
            super();
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.index = index;
            this.color = color;
            this.mode = mode;
        }

        public TextViewDrawableColorFilterAction(RemoteViews this$0, Parcel parcel) {
            boolean z = false;
            RemoteViews.this = this$0;
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                z = true;
            }
            this.isRelative = z;
            this.index = parcel.readInt();
            this.color = parcel.readInt();
            this.mode = readPorterDuffMode(parcel);
        }

        private Mode readPorterDuffMode(Parcel parcel) {
            int mode = parcel.readInt();
            if (mode < 0 || mode >= Mode.values().length) {
                return Mode.CLEAR;
            }
            return Mode.values()[mode];
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            dest.writeInt(this.isRelative ? RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT : 0);
            dest.writeInt(this.index);
            dest.writeInt(this.color);
            dest.writeInt(this.mode.ordinal());
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target != null) {
                Drawable[] drawables;
                if (this.isRelative) {
                    drawables = target.getCompoundDrawablesRelative();
                } else {
                    drawables = target.getCompoundDrawables();
                }
                if (this.index < 0 || this.index >= 4) {
                    throw new IllegalStateException("index must be in range [0, 3].");
                }
                Drawable d = drawables[this.index];
                if (d != null) {
                    d.mutate();
                    d.setColorFilter(this.color, this.mode);
                }
            }
        }

        public String getActionName() {
            return "TextViewDrawableColorFilterAction";
        }
    }

    private class TextViewSizeAction extends Action {
        public static final int TAG = 13;
        float size;
        int units;

        public TextViewSizeAction(int viewId, int units, float size) {
            super();
            this.viewId = viewId;
            this.units = units;
            this.size = size;
        }

        public TextViewSizeAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.units = parcel.readInt();
            this.size = parcel.readFloat();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
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

        public String getActionName() {
            return "TextViewSizeAction";
        }
    }

    private class ViewGroupAction extends Action {
        public static final int TAG = 4;
        RemoteViews nestedViews;

        /* renamed from: android.widget.RemoteViews.ViewGroupAction.1 */
        class AnonymousClass1 extends RuntimeAction {
            final /* synthetic */ AsyncApplyTask val$task;
            final /* synthetic */ ViewTree val$tree;

            AnonymousClass1(AsyncApplyTask val$task, ViewTree val$tree) {
                this.val$task = val$task;
                this.val$tree = val$tree;
                super();
            }

            public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
                ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
                this.val$task.onPostExecute(this.val$tree);
                target.addView(this.val$task.mResult);
            }
        }

        public ViewGroupAction(int viewId, RemoteViews nestedViews) {
            super();
            this.viewId = viewId;
            this.nestedViews = nestedViews;
            if (nestedViews != null) {
                RemoteViews.this.configureRemoteViewsAsChild(nestedViews);
            }
        }

        public ViewGroupAction(RemoteViews this$0, Parcel parcel, BitmapCache bitmapCache) {
            boolean nestedViewsNull = false;
            RemoteViews.this = this$0;
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() == 0) {
                nestedViewsNull = true;
            }
            if (nestedViewsNull) {
                this.nestedViews = null;
            } else {
                this.nestedViews = new RemoteViews(bitmapCache, null);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(this.viewId);
            if (this.nestedViews != null) {
                dest.writeInt(RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT);
                this.nestedViews.writeToParcel(dest, flags);
                return;
            }
            dest.writeInt(0);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            Context context = root.getContext();
            ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
            if (target != null) {
                if (this.nestedViews != null) {
                    target.addView(this.nestedViews.apply(context, target, handler));
                } else {
                    target.removeAllViews();
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            root.createTree();
            ViewTree target = root.findViewTreeById(this.viewId);
            if (target == null || !(target.mRoot instanceof ViewGroup)) {
                return RemoteViews.ACTION_NOOP;
            }
            if (this.nestedViews == null) {
                target.mChildren = null;
                return this;
            }
            AsyncApplyTask task = this.nestedViews.getAsyncApplyTask(root.mRoot.getContext(), (ViewGroup) target.mRoot, null, handler);
            ViewTree tree = task.doInBackground(new Void[0]);
            target.addChild(tree);
            return new AnonymousClass1(task, tree);
        }

        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
            if (this.nestedViews != null) {
                counter.increment(this.nestedViews.estimateMemoryUsage());
            }
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            if (this.nestedViews != null) {
                this.nestedViews.setBitmapCache(bitmapCache);
            }
        }

        public String getActionName() {
            return "ViewGroupAction" + (this.nestedViews == null ? "Remove" : "Add");
        }

        public int mergeBehavior() {
            return RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT;
        }
    }

    private class ViewPaddingAction extends Action {
        public static final int TAG = 14;
        int bottom;
        int left;
        int right;
        int top;

        public ViewPaddingAction(int viewId, int left, int top, int right, int bottom) {
            super();
            this.viewId = viewId;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
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
            dest.writeInt(TAG);
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

        public String getActionName() {
            return "ViewPaddingAction";
        }
    }

    private static class ViewTree {
        private ArrayList<ViewTree> mChildren;
        private final View mRoot;

        private ViewTree(View root) {
            this.mRoot = root;
        }

        public void createTree() {
            if (this.mChildren == null) {
                this.mChildren = new ArrayList();
                if ((this.mRoot instanceof ViewGroup) && this.mRoot.isRootNamespace()) {
                    ViewGroup vg = this.mRoot;
                    int count = vg.getChildCount();
                    for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
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
            for (ViewTree tree : this.mChildren) {
                ViewTree result = tree.findViewTreeById(id);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        public View findViewById(int id) {
            View view = null;
            if (this.mChildren == null) {
                return this.mRoot.findViewById(id);
            }
            ViewTree tree = findViewTreeById(id);
            if (tree != null) {
                view = tree.mRoot;
            }
            return view;
        }

        public void addChild(ViewTree child) {
            if (this.mChildren == null) {
                this.mChildren = new ArrayList();
            }
            child.createTree();
            this.mChildren.add(child);
        }

        private void addViewChild(View v) {
            ViewTree target;
            if (v.getId() != 0) {
                ViewTree tree = new ViewTree(v);
                this.mChildren.add(tree);
                target = tree;
            } else {
                target = this;
            }
            if ((v instanceof ViewGroup) && v.isRootNamespace() && target.mChildren == null) {
                target.mChildren = new ArrayList();
                ViewGroup vg = (ViewGroup) v;
                int count = vg.getChildCount();
                for (int i = 0; i < count; i += RemoteViews.MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                    target.addViewChild(vg.getChildAt(i));
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.RemoteViews.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.RemoteViews.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.<clinit>():void");
    }

    public void setRemoteInputs(int viewId, RemoteInput[] remoteInputs) {
        this.mActions.add(new SetRemoteInputsAction(viewId, remoteInputs));
    }

    public void mergeRemoteViews(RemoteViews newRv) {
        if (newRv != null) {
            int i;
            Action a;
            RemoteViews copy = newRv.clone();
            HashMap<String, Action> map = new HashMap();
            if (this.mActions == null) {
                this.mActions = new ArrayList();
            }
            int count = this.mActions.size();
            for (i = 0; i < count; i += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                a = (Action) this.mActions.get(i);
                map.put(a.getUniqueKey(), a);
            }
            ArrayList<Action> newActions = copy.mActions;
            if (newActions != null) {
                count = newActions.size();
                for (i = 0; i < count; i += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                    a = (Action) newActions.get(i);
                    String key = ((Action) newActions.get(i)).getUniqueKey();
                    int mergeBehavior = ((Action) newActions.get(i)).mergeBehavior();
                    if (map.containsKey(key) && mergeBehavior == 0) {
                        this.mActions.remove(map.get(key));
                        map.remove(key);
                    }
                    if (mergeBehavior == 0 || mergeBehavior == MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                        this.mActions.add(a);
                    }
                }
                this.mBitmapCache = new BitmapCache();
                setBitmapCache(this.mBitmapCache);
            }
        }
    }

    private static Rect getSourceBounds(View v) {
        float appScale = v.getContext().getResources().getCompatibilityInfo().applicationScale;
        int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        Rect rect = new Rect();
        rect.left = (int) ((((float) pos[0]) * appScale) + 0.5f);
        rect.top = (int) ((((float) pos[MODE_HAS_LANDSCAPE_AND_PORTRAIT]) * appScale) + 0.5f);
        rect.right = (int) ((((float) (pos[0] + v.getWidth())) * appScale) + 0.5f);
        rect.bottom = (int) ((((float) (pos[MODE_HAS_LANDSCAPE_AND_PORTRAIT] + v.getHeight())) * appScale) + 0.5f);
        return rect;
    }

    private Method getMethod(View view, String methodName, Class<?> paramType) {
        Method method;
        Class<? extends View> klass = view.getClass();
        synchronized (sMethodsLock) {
            ArrayMap<MutablePair<String, Class<?>>, Method> methods = (ArrayMap) sMethods.get(klass);
            if (methods == null) {
                methods = new ArrayMap();
                sMethods.put(klass, methods);
            }
            this.mPair.first = methodName;
            this.mPair.second = paramType;
            method = (Method) methods.get(this.mPair);
            if (method == null) {
                if (paramType == null) {
                    try {
                        method = klass.getMethod(methodName, new Class[0]);
                    } catch (NoSuchMethodException e) {
                        throw new ActionException("view: " + klass.getName() + " doesn't have method: " + methodName + getParameters(paramType));
                    }
                }
                Class[] clsArr = new Class[MODE_HAS_LANDSCAPE_AND_PORTRAIT];
                clsArr[0] = paramType;
                method = klass.getMethod(methodName, clsArr);
                if (method.isAnnotationPresent(RemotableViewMethod.class)) {
                    methods.put(new MutablePair(methodName, paramType), method);
                } else {
                    throw new ActionException("view: " + klass.getName() + " can't use method with RemoteViews: " + methodName + getParameters(paramType));
                }
            }
        }
        return method;
    }

    private Method getAsyncMethod(Method method) {
        synchronized (sAsyncMethods) {
            int valueIndex = sAsyncMethods.indexOfKey(method);
            if (valueIndex >= 0) {
                Method method2 = (Method) sAsyncMethods.valueAt(valueIndex);
                return method2;
            }
            RemotableViewMethod annotation = (RemotableViewMethod) method.getAnnotation(RemotableViewMethod.class);
            Method method3 = null;
            if (!annotation.asyncImpl().isEmpty()) {
                try {
                    method3 = method.getDeclaringClass().getMethod(annotation.asyncImpl(), method.getParameterTypes());
                    if (!method3.getReturnType().equals(Runnable.class)) {
                        throw new ActionException("Async implementation for " + method.getName() + " does not return a Runnable");
                    }
                } catch (NoSuchMethodException e) {
                    throw new ActionException("Async implementation declared but not defined for " + method.getName());
                }
            }
            sAsyncMethods.put(method, method3);
            return method3;
        }
    }

    private static String getParameters(Class<?> paramType) {
        if (paramType == null) {
            return "()";
        }
        return "(" + paramType + ")";
    }

    private static Object[] wrapArg(Object value) {
        Object[] args = (Object[]) sInvokeArgsTls.get();
        args[0] = value;
        return args;
    }

    private void configureRemoteViewsAsChild(RemoteViews rv) {
        this.mBitmapCache.assimilate(rv.mBitmapCache);
        rv.setBitmapCache(this.mBitmapCache);
        rv.setNotRoot();
    }

    void setNotRoot() {
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
        this.mIsWidgetCollectionChild = false;
        this.mPair = new MutablePair(null, null);
        this.mApplication = application;
        this.mLayoutId = layoutId;
        this.mBitmapCache = new BitmapCache();
        this.mMemoryUsageCounter = new MemoryUsageCounter();
        recalculateMemoryUsage();
    }

    private boolean hasLandscapeAndPortraitLayouts() {
        return (this.mLandscape == null || this.mPortrait == null) ? false : true;
    }

    public RemoteViews(RemoteViews landscape, RemoteViews portrait) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mIsWidgetCollectionChild = false;
        this.mPair = new MutablePair(null, null);
        if (landscape == null || portrait == null) {
            throw new RuntimeException("Both RemoteViews must be non-null");
        } else if (landscape.mApplication.uid == portrait.mApplication.uid && landscape.mApplication.packageName.equals(portrait.mApplication.packageName)) {
            this.mApplication = portrait.mApplication;
            this.mLayoutId = portrait.getLayoutId();
            this.mLandscape = landscape;
            this.mPortrait = portrait;
            this.mMemoryUsageCounter = new MemoryUsageCounter();
            this.mBitmapCache = new BitmapCache();
            configureRemoteViewsAsChild(landscape);
            configureRemoteViewsAsChild(portrait);
            recalculateMemoryUsage();
        } else {
            throw new RuntimeException("Both RemoteViews must share the same package and user");
        }
    }

    public RemoteViews(Parcel parcel) {
        this(parcel, null);
    }

    private RemoteViews(Parcel parcel, BitmapCache bitmapCache) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mIsWidgetCollectionChild = false;
        this.mPair = new MutablePair(null, null);
        int mode = parcel.readInt();
        if (bitmapCache == null) {
            this.mBitmapCache = new BitmapCache(parcel);
        } else {
            setBitmapCache(bitmapCache);
            setNotRoot();
        }
        if (mode == 0) {
            boolean z;
            this.mApplication = (ApplicationInfo) parcel.readParcelable(null);
            this.mLayoutId = parcel.readInt();
            if (parcel.readInt() == MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                z = true;
            } else {
                z = false;
            }
            this.mIsWidgetCollectionChild = z;
            int count = parcel.readInt();
            if (count > 0) {
                this.mActions = new ArrayList(count);
                for (int i = 0; i < count; i += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                    int tag = parcel.readInt();
                    switch (tag) {
                        case MODE_HAS_LANDSCAPE_AND_PORTRAIT /*1*/:
                            this.mActions.add(new SetOnClickPendingIntent(parcel));
                            break;
                        case HwCfgFilePolicy.PC /*2*/:
                            this.mActions.add(new ReflectionAction(this, parcel));
                            break;
                        case HwCfgFilePolicy.BASE /*3*/:
                            this.mActions.add(new SetDrawableParameters(parcel));
                            break;
                        case HwCfgFilePolicy.CUST /*4*/:
                            this.mActions.add(new ViewGroupAction(this, parcel, this.mBitmapCache));
                            break;
                        case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                            this.mActions.add(new ReflectionActionWithoutParams(parcel));
                            break;
                        case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                            this.mActions.add(new SetEmptyView(parcel));
                            break;
                        case PGSdk.TYPE_VIDEO /*8*/:
                            this.mActions.add(new SetPendingIntentTemplate(parcel));
                            break;
                        case PGSdk.TYPE_SCRLOCK /*9*/:
                            this.mActions.add(new SetOnClickFillInIntent(parcel));
                            break;
                        case PGSdk.TYPE_CLOCK /*10*/:
                            this.mActions.add(new SetRemoteViewsAdapterIntent(parcel));
                            break;
                        case PGSdk.TYPE_IM /*11*/:
                            this.mActions.add(new TextViewDrawableAction(this, parcel));
                            break;
                        case PGSdk.TYPE_MUSIC /*12*/:
                            this.mActions.add(new BitmapReflectionAction(parcel));
                            break;
                        case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                            this.mActions.add(new TextViewSizeAction(parcel));
                            break;
                        case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                            this.mActions.add(new ViewPaddingAction(parcel));
                            break;
                        case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                            this.mActions.add(new SetRemoteViewsAdapterList(parcel));
                            break;
                        case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                            this.mActions.add(new TextViewDrawableColorFilterAction(this, parcel));
                            break;
                        case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                            this.mActions.add(new SetRemoteInputsAction(parcel));
                            break;
                        case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                            this.mActions.add(new LayoutParamAction(parcel));
                            break;
                        default:
                            throw new ActionException("Tag " + tag + " not found");
                    }
                }
            }
        } else {
            this.mLandscape = new RemoteViews(parcel, this.mBitmapCache);
            this.mPortrait = new RemoteViews(parcel, this.mBitmapCache);
            this.mApplication = this.mPortrait.mApplication;
            this.mLayoutId = this.mPortrait.getLayoutId();
        }
        this.mMemoryUsageCounter = new MemoryUsageCounter();
        recalculateMemoryUsage();
    }

    public RemoteViews clone() {
        Preconditions.checkState(this.mIsRoot, "RemoteView has been attached to another RemoteView. May only clone the root of a RemoteView hierarchy.");
        Parcel p = Parcel.obtain();
        this.mIsRoot = false;
        writeToParcel(p, 0);
        p.setDataPosition(0);
        this.mIsRoot = true;
        RemoteViews rv = new RemoteViews(p, this.mBitmapCache.clone());
        rv.mIsRoot = true;
        p.recycle();
        return rv;
    }

    public String getPackage() {
        return this.mApplication != null ? this.mApplication.packageName : null;
    }

    public int getLayoutId() {
        return this.mLayoutId;
    }

    void setIsWidgetCollectionChild(boolean isWidgetCollectionChild) {
        this.mIsWidgetCollectionChild = isWidgetCollectionChild;
    }

    private void recalculateMemoryUsage() {
        this.mMemoryUsageCounter.clear();
        if (hasLandscapeAndPortraitLayouts()) {
            this.mMemoryUsageCounter.increment(this.mLandscape.estimateMemoryUsage());
            this.mMemoryUsageCounter.increment(this.mPortrait.estimateMemoryUsage());
            this.mBitmapCache.addBitmapMemory(this.mMemoryUsageCounter);
            return;
        }
        if (this.mActions != null) {
            int count = this.mActions.size();
            for (int i = 0; i < count; i += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                ((Action) this.mActions.get(i)).updateMemoryUsageEstimate(this.mMemoryUsageCounter);
            }
        }
        if (this.mIsRoot) {
            this.mBitmapCache.addBitmapMemory(this.mMemoryUsageCounter);
        }
    }

    private void setBitmapCache(BitmapCache bitmapCache) {
        this.mBitmapCache = bitmapCache;
        if (hasLandscapeAndPortraitLayouts()) {
            this.mLandscape.setBitmapCache(bitmapCache);
            this.mPortrait.setBitmapCache(bitmapCache);
        } else if (this.mActions != null) {
            int count = this.mActions.size();
            for (int i = 0; i < count; i += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                ((Action) this.mActions.get(i)).setBitmapCache(bitmapCache);
            }
        }
    }

    public int estimateMemoryUsage() {
        return this.mMemoryUsageCounter.getMemoryUsage();
    }

    private void addAction(Action a) {
        if (hasLandscapeAndPortraitLayouts()) {
            throw new RuntimeException("RemoteViews specifying separate landscape and portrait layouts cannot be modified. Instead, fully configure the landscape and portrait layouts individually before constructing the combined layout.");
        }
        if (this.mActions == null) {
            this.mActions = new ArrayList();
        }
        this.mActions.add(a);
        a.updateMemoryUsageEstimate(this.mMemoryUsageCounter);
    }

    public void addView(int viewId, RemoteViews nestedView) {
        addAction(new ViewGroupAction(viewId, nestedView));
    }

    public void removeAllViews(int viewId) {
        addAction(new ViewGroupAction(viewId, null));
    }

    public void showNext(int viewId) {
        addAction(new ReflectionActionWithoutParams(viewId, "showNext"));
    }

    public void showPrevious(int viewId) {
        addAction(new ReflectionActionWithoutParams(viewId, "showPrevious"));
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

    public void setTextViewCompoundDrawablesRelativeColorFilter(int viewId, int index, int color, Mode mode) {
        if (index < 0 || index >= 4) {
            throw new IllegalArgumentException("index must be in range [0, 3].");
        }
        addAction(new TextViewDrawableColorFilterAction(viewId, true, index, color, mode));
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
        addAction(new SetOnClickPendingIntent(viewId, pendingIntent));
    }

    public void setPendingIntentTemplate(int viewId, PendingIntent pendingIntentTemplate) {
        addAction(new SetPendingIntentTemplate(viewId, pendingIntentTemplate));
    }

    public void setOnClickFillInIntent(int viewId, Intent fillInIntent) {
        addAction(new SetOnClickFillInIntent(viewId, fillInIntent));
    }

    public void setDrawableParameters(int viewId, boolean targetBackground, int alpha, int colorFilter, Mode mode, int level) {
        addAction(new SetDrawableParameters(viewId, targetBackground, alpha, colorFilter, mode, level));
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
        addAction(new ViewPaddingAction(viewId, left, top, right, bottom));
    }

    public void setViewLayoutMarginEndDimen(int viewId, int endMarginDimen) {
        addAction(new LayoutParamAction(viewId, MODE_HAS_LANDSCAPE_AND_PORTRAIT, endMarginDimen));
    }

    public void setViewLayoutMarginBottomDimen(int viewId, int bottomMarginDimen) {
        addAction(new LayoutParamAction(viewId, 3, bottomMarginDimen));
    }

    public void setViewLayoutWidth(int viewId, int layoutWidth) {
        if (layoutWidth == 0 || layoutWidth == -1 || layoutWidth == -2) {
            this.mActions.add(new LayoutParamAction(viewId, 2, layoutWidth));
            return;
        }
        throw new IllegalArgumentException("Only supports 0, WRAP_CONTENT and MATCH_PARENT");
    }

    public void setBoolean(int viewId, String methodName, boolean value) {
        addAction(new ReflectionAction(viewId, methodName, MODE_HAS_LANDSCAPE_AND_PORTRAIT, Boolean.valueOf(value)));
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

    private View inflateView(Context context, Context contextForResources, RemoteViews rv, ViewGroup parent, OnClickHandler handler) {
        Context inflationContext = new AnonymousClass4(context, contextForResources);
        if (handler != null && handler.getClass().getName().contains("BaseStatusBar")) {
            inflationContext = new ContextThemeWrapper(inflationContext, (int) androidhwext.R.style.Theme_Emui_Dark_Notification);
        }
        LayoutInflater inflater = ((LayoutInflater) context.getSystemService("layout_inflater")).cloneInContext(inflationContext);
        inflater.setFilter(this);
        if (((contextForResources.getApplicationInfo() == null ? 0 : contextForResources.getApplicationInfo().flags) & MODE_HAS_LANDSCAPE_AND_PORTRAIT) != 0) {
            setWidgetFactoryHuaWei(context, inflater);
        }
        return inflater.inflate(rv.getLayoutId(), parent, false);
    }

    private static void loadTransitionOverride(Context context, OnClickHandler handler) {
        if (handler != null && context.getResources().getBoolean(R.bool.config_overrideRemoteViewsActivityTransition)) {
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
        if (executor == null) {
            executor = AsyncTask.THREAD_POOL_EXECUTOR;
        }
        task.executeOnExecutor(executor, new Void[0]);
        return cancelSignal;
    }

    public CancellationSignal applyAsync(Context context, ViewGroup parent, Executor executor, OnViewAppliedListener listener, OnClickHandler handler) {
        return startTaskOnExecutor(getAsyncApplyTask(context, parent, listener, handler), executor);
    }

    private AsyncApplyTask getAsyncApplyTask(Context context, ViewGroup parent, OnViewAppliedListener listener, OnClickHandler handler) {
        return new AsyncApplyTask(getRemoteViewsToApply(context), parent, context, listener, handler, null, null);
    }

    public void reapply(Context context, View v) {
        reapply(context, v, null);
    }

    public void reapply(Context context, View v, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        if (!hasLandscapeAndPortraitLayouts() || v.getId() == rvToApply.getLayoutId()) {
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
        if (!hasLandscapeAndPortraitLayouts() || v.getId() == rvToApply.getLayoutId()) {
            return startTaskOnExecutor(new AsyncApplyTask(rvToApply, (ViewGroup) v.getParent(), context, listener, handler, v, null), executor);
        }
        throw new RuntimeException("Attempting to re-apply RemoteViews to a view that that does not share the same root layout id.");
    }

    private void performApply(View v, ViewGroup parent, OnClickHandler handler) {
        if (this.mActions != null) {
            if (handler == null) {
                handler = DEFAULT_ON_CLICK_HANDLER;
            }
            int count = this.mActions.size();
            for (int i = 0; i < count; i += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
                ((Action) this.mActions.get(i)).apply(v, parent, handler);
            }
        }
    }

    private Context getContextForResources(Context context) {
        if (this.mApplication != null) {
            if (context.getUserId() == UserHandle.getUserId(this.mApplication.uid) && context.getPackageName().equals(this.mApplication.packageName)) {
                return context;
            }
            try {
                return context.createApplicationContext(this.mApplication, 4);
            } catch (NameNotFoundException e) {
                Log.e(LOG_TAG, "Package name " + this.mApplication.packageName + " not found");
            }
        }
        return context;
    }

    public int getSequenceNumber() {
        return this.mActions == null ? 0 : this.mActions.size();
    }

    public boolean onLoadClass(Class clazz) {
        return clazz.isAnnotationPresent(RemoteView.class);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = MODE_HAS_LANDSCAPE_AND_PORTRAIT;
        if (hasLandscapeAndPortraitLayouts()) {
            dest.writeInt(MODE_HAS_LANDSCAPE_AND_PORTRAIT);
            if (this.mIsRoot) {
                this.mBitmapCache.writeBitmapsToParcel(dest, flags);
            }
            this.mLandscape.writeToParcel(dest, flags);
            this.mPortrait.writeToParcel(dest, flags);
            return;
        }
        int count;
        dest.writeInt(0);
        if (this.mIsRoot) {
            this.mBitmapCache.writeBitmapsToParcel(dest, flags);
        }
        dest.writeParcelable(this.mApplication, flags);
        dest.writeInt(this.mLayoutId);
        if (!this.mIsWidgetCollectionChild) {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mActions != null) {
            count = this.mActions.size();
        } else {
            count = 0;
        }
        dest.writeInt(count);
        for (int i2 = 0; i2 < count; i2 += MODE_HAS_LANDSCAPE_AND_PORTRAIT) {
            Action a = (Action) this.mActions.get(i2);
            if (a != null) {
                a.writeToParcel(dest, 0);
            }
        }
    }

    private static ApplicationInfo getApplicationInfo(String packageName, int userId) {
        if (packageName == null) {
            return null;
        }
        Application application = ActivityThread.currentApplication();
        if (application == null) {
            throw new IllegalStateException("Cannot create remote views out of an aplication.");
        }
        ApplicationInfo applicationInfo = application.getApplicationInfo();
        if (!(UserHandle.getUserId(applicationInfo.uid) == userId && applicationInfo.packageName.equals(packageName))) {
            try {
                applicationInfo = application.getBaseContext().createPackageContextAsUser(packageName, 0, new UserHandle(userId)).getApplicationInfo();
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException("No such package " + packageName);
            }
        }
        return applicationInfo;
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
        return this.mBitmapCache != null ? this.mBitmapCache.getSize() : 0;
    }

    public void recycle() {
        int count = getCacheSize();
        int i = 0;
        while (i < count) {
            try {
                if (!(this.mBitmapCache == null || this.mBitmapCache.getBitmapForId(i) == null)) {
                    this.mBitmapCache.getBitmapForId(i).recycle();
                }
                i += MODE_HAS_LANDSCAPE_AND_PORTRAIT;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return;
            }
        }
    }
}

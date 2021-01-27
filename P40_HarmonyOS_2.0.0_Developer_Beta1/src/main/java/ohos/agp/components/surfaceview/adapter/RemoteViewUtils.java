package ohos.agp.components.surfaceview.adapter;

import android.app.ActivityThread;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.UserHandle;
import android.util.IntArray;
import android.view.View;
import android.widget.RemoteViews;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentProvider;
import ohos.agp.components.Text;
import ohos.agp.styles.attributes.ProgressBarAttrsConstants;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentAdapterUtils;
import ohos.global.resource.ResourceManagerInner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.rpc.MessageParcel;
import ohos.utils.Parcel;

public class RemoteViewUtils {
    private static final int COMPLEX_UNIT_DP = 1;
    private static final int COMPLEX_UNIT_PX = 0;
    private static final int COMPLEX_UNIT_SP = 2;
    private static final int DYNAMIC_TYPE_BOOL = 7;
    private static final int DYNAMIC_TYPE_DOUBLE = 5;
    private static final int DYNAMIC_TYPE_FLOAT = 3;
    private static final int DYNAMIC_TYPE_INT = 1;
    private static final int DYNAMIC_TYPE_LONG = 6;
    private static final int DYNAMIC_TYPE_PIXELMAP = 4;
    private static final int DYNAMIC_TYPE_STRING = 2;
    private static final HiLogLabel LOG_TAG = new HiLogLabel(3, (int) LogDomain.END, "RemoteViewUtils");
    private static final int TAG_BITMAP_REFLECTION_ACTION = 12;
    private static final int TAG_LAYOUT_PARAM_ACTION = 19;
    private static final int TAG_OVERRIDE_TEXT_COLORS = 20;
    private static final int TAG_REFLECTION_ACTION = 2;
    private static final int TAG_SET_DRAWABLE_TINT = 3;
    private static final int TAG_SET_EMPTY_VIEW_ACTION = 6;
    private static final int TAG_SET_INT_TAG = 22;
    private static final int TAG_SET_ON_CLICK_RESPONSE = 1;
    private static final int TAG_SET_PENDING_INTENT_TEMPLATE = 8;
    private static final int TAG_SET_REMOTE_INPUTS_ACTION = 18;
    private static final int TAG_SET_REMOTE_VIEW_ADAPTER_INTENT = 10;
    private static final int TAG_SET_REMOTE_VIEW_ADAPTER_LIST = 15;
    private static final int TAG_SET_RIPPLE_DRAWABLE_COLOR = 21;
    private static final int TAG_TEXT_VIEW_DRAWABLE_ACTION = 11;
    private static final int TAG_TEXT_VIEW_SIZE_ACTION = 13;
    private static final int TAG_VIEW_CONTENT_NAVIGATION = 5;
    private static final int TAG_VIEW_GROUP_ACTION_ADD = 4;
    private static final int TAG_VIEW_GROUP_ACTION_REMOVE = 7;
    private static final int TAG_VIEW_PADDING_ACTION = 14;
    private Context mAOSPContext;
    private RemoteViews mARemoteView;
    private ArrayList<Action> mActions;
    private int mApplyFlags = 0;
    private final HashMap<String, AFunctionParam> mDynamicFunctionMap = new HashMap<>();
    private int mLayoutId;
    private int mNativeLayoutId;
    private PixelMapCache mPixelMapCache;
    private String mResourceBundleName;
    private int mViewId;

    /* access modifiers changed from: private */
    public interface Action {
        int getActionTag();

        void writeToParcel(Parcel parcel);
    }

    public String getARemoteViewClass() {
        return "android.widget.RemoteViews";
    }

    public RemoteViewUtils(ohos.app.Context context) {
        HiLog.debug(LOG_TAG, "RemoteViewUtils Create enter.", new Object[0]);
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            this.mAOSPContext = (Context) hostContext;
            initFunctionMap();
        } else {
            HiLog.error(LOG_TAG, "RemoteViewUtils cannot get A Context.", new Object[0]);
        }
        this.mPixelMapCache = new PixelMapCache();
        this.mActions = new ArrayList<>();
    }

    public Optional<RemoteViews> getARemoteViews(ComponentProvider componentProvider) {
        HiLog.debug(LOG_TAG, "getARemoveView enter getARemoteView", new Object[0]);
        if (componentProvider == null) {
            return Optional.empty();
        }
        setResourceBundleName(componentProvider);
        if (this.mNativeLayoutId == 0) {
            this.mNativeLayoutId = componentProvider.getLayoutId();
        }
        String str = getResourceBundleName() + ".ResourceTable";
        try {
            Class<?> cls = Class.forName(str, false, this.mAOSPContext.getClassLoader());
            this.mLayoutId = ResourceManagerInner.getAResId(this.mNativeLayoutId, cls, this.mAOSPContext);
            HiLog.debug(LOG_TAG, "RemoteViewUtils mLayoutId=0x%{public}x.", new Object[]{Integer.valueOf(this.mLayoutId)});
            this.mARemoteView = new RemoteViews(this.mAOSPContext.getPackageName(), this.mLayoutId);
            for (ComponentProvider.Action action : componentProvider.getActions()) {
                Optional<Object> privateValue = getPrivateValue(action, "viewId");
                int i = action.viewId;
                if (!privateValue.isPresent()) {
                    HiLog.debug(LOG_TAG, "getARemoveView viewId not Present z viewID=0x%{public}x.", new Object[]{Integer.valueOf(action.viewId)});
                } else {
                    i = getInt(privateValue.get());
                    HiLog.debug(LOG_TAG, "getARemoveView z viewID=0x%{public}x.", new Object[]{Integer.valueOf(i)});
                }
                this.mViewId = ResourceManagerInner.getAResId(i, cls, this.mAOSPContext);
                HiLog.debug(LOG_TAG, "getARemoveView mViewId=0x%{public}x.", new Object[]{Integer.valueOf(this.mViewId)});
                dealWithAction(action);
            }
            return Optional.of(this.mARemoteView);
        } catch (ClassNotFoundException unused) {
            HiLog.error(LOG_TAG, "RemoteViewUtils ClassNotFoundException ClassName = %{public}s", new Object[]{str});
            return Optional.empty();
        }
    }

    public byte[] getARemoteViewBytes(ComponentProvider componentProvider, boolean z) {
        HiLog.debug(LOG_TAG, "getARemoteViewParcel enter.", new Object[0]);
        Optional<RemoteViews> aRemoteViews = getARemoteViews(componentProvider);
        if (!aRemoteViews.isPresent()) {
            return new byte[0];
        }
        RemoteViews remoteViews = aRemoteViews.get();
        android.os.Parcel obtain = android.os.Parcel.obtain();
        if (z) {
            obtain.writeParcelable(remoteViews, 0);
        } else {
            remoteViews.writeToParcel(obtain, 2);
        }
        obtain.setDataPosition(0);
        byte[] marshall = obtain.marshall();
        obtain.recycle();
        return marshall;
    }

    private void setResourceBundleName(ComponentProvider componentProvider) {
        this.mResourceBundleName = componentProvider.getDefaultBundleName();
        if (this.mResourceBundleName == null) {
            this.mResourceBundleName = this.mAOSPContext.getPackageName();
        }
    }

    private String getResourceBundleName() {
        String str = this.mResourceBundleName;
        return str != null ? str : this.mAOSPContext.getPackageName();
    }

    private void initFunctionMap() {
        this.mDynamicFunctionMap.put("setContentDescription", new AFunctionParam("setContentDescription", 2));
        this.mDynamicFunctionMap.put("setVisibility", new AFunctionParam("setVisibility", 1));
        this.mDynamicFunctionMap.put("setEnabled", new AFunctionParam("setEnabled", 7));
        this.mDynamicFunctionMap.put("setMinimumHeight", new AFunctionParam("setMinimumHeight", 1));
        this.mDynamicFunctionMap.put("setIndeterminate", new AFunctionParam("setIndeterminate", 7));
        this.mDynamicFunctionMap.put("setProgress", new AFunctionParam("setProgress", 1));
        this.mDynamicFunctionMap.put("setSecondaryProgress", new AFunctionParam("setSecondaryProgress", 1));
        this.mDynamicFunctionMap.put("setMin", new AFunctionParam("setMin", 1));
        this.mDynamicFunctionMap.put("setMax", new AFunctionParam("setMax", 1));
        this.mDynamicFunctionMap.put("setTextSize", new AFunctionParam("setTextSize", 3));
        this.mDynamicFunctionMap.put("setMaxLines", new AFunctionParam("setMaxLines", 1));
        this.mDynamicFunctionMap.put("setMaxHeight", new AFunctionParam("setMaxHeight", 1));
        this.mDynamicFunctionMap.put("setMaxWidth", new AFunctionParam("setMaxWidth", 1));
        this.mDynamicFunctionMap.put("setText", new AFunctionParam("setText", 2));
        this.mDynamicFunctionMap.put("setAccessibilityDescription", new AFunctionParam("setContentDescription", 2));
        this.mDynamicFunctionMap.put("setHint", new AFunctionParam("setHint", 2));
        this.mDynamicFunctionMap.put("setCursorVisible", new AFunctionParam("setCursorVisible", 7));
        this.mDynamicFunctionMap.put("setPixelMap", new AFunctionParam("setImageBitmap", 4));
        this.mDynamicFunctionMap.put("setImageResource", new AFunctionParam("setImageResource", 1));
        this.mDynamicFunctionMap.put("setMeasureWithLargestChildEnabled", new AFunctionParam("setMeasureWithLargestChildEnabled", 7));
        this.mDynamicFunctionMap.put("setWeightSum", new AFunctionParam("setWeightSum", 3));
        this.mDynamicFunctionMap.put("setGravity", new AFunctionParam("setGravity", 1));
        this.mDynamicFunctionMap.put("setMeasureAllChildren", new AFunctionParam("setMeasureAllChildren", 7));
        this.mDynamicFunctionMap.put("setIgnoreGravity", new AFunctionParam("setIgnoreGravity", 1));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void dealWithAction(ComponentProvider.Action action) {
        char c;
        if (action == null) {
            HiLog.error(LOG_TAG, "dealWithAction action is null.", new Object[0]);
            return;
        }
        String name = action.getClass().getName();
        HiLog.debug(LOG_TAG, "dealWithAction mViewId=%{public}s.", new Object[]{name});
        switch (name.hashCode()) {
            case -1030354205:
                if (name.equals("ohos.agp.components.ComponentProvider$SetTextColorAction")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -762566419:
                if (name.equals("ohos.agp.components.ComponentProvider$SetTextSizeAction")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -268420068:
                if (name.equals("ohos.agp.components.ComponentProvider$DynamicAction")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 464037539:
                if (name.equals("ohos.agp.components.ComponentProvider$SetBackgroundPixelMapAction")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 780063294:
                if (name.equals("ohos.agp.components.ComponentProvider$SetPaddingAction")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1050038086:
                if (name.equals("ohos.agp.components.ComponentProvider$OnClickAction")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1233605181:
                if (name.equals("ohos.agp.components.ComponentProvider$ComponentContainerLayoutAction")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1476322707:
                if (name.equals("ohos.agp.components.ComponentProvider$SetProgressBarAction")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                doDynamicAction(action);
                return;
            case 1:
                doSetTextColorAction(action);
                return;
            case 2:
                doViewGroupMarginLayoutAction(action);
                return;
            case 3:
                doSetProgressBarAction(action);
                return;
            case 4:
                doSetPaddingAction(action);
                return;
            case 5:
                doSetBackgroundPixelMapAction(action);
                return;
            case 6:
                doOnClickAction(action);
                return;
            case 7:
                doSetTextSizeAction(action);
                return;
            default:
                HiLog.error(LOG_TAG, "dealWithAction no used action.", new Object[0]);
                return;
        }
    }

    private void doDynamicAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "type");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doDynamicAction type not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, Constants.ATTRNAME_OUTPUT_METHOD);
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "doDynamicAction method not Present.", new Object[0]);
            return;
        }
        String string = getString(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "value");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "doDynamicAction value not Present.", new Object[0]);
            return;
        }
        Object obj = privateValue3.get();
        HiLog.debug(LOG_TAG, "dealWithAction actionType=%{public}d, method=%{public}s.", new Object[]{Integer.valueOf(i), string});
        dynamicFunctionMap(i, string, obj);
    }

    private void dynamicFunctionMap(int i, String str, Object obj) {
        AFunctionParam aFunctionParam;
        Iterator<Map.Entry<String, AFunctionParam>> it = this.mDynamicFunctionMap.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                aFunctionParam = null;
                break;
            }
            Map.Entry<String, AFunctionParam> next = it.next();
            if (next.getKey().equals(str)) {
                aFunctionParam = next.getValue();
                break;
            }
        }
        if (aFunctionParam == null) {
            HiLog.error(LOG_TAG, "dynamicFunctionMap function not found in map.", new Object[0]);
        } else {
            dynamicAction(i, aFunctionParam.getType(), aFunctionParam.getName(), obj);
        }
    }

    private void dynamicAction(int i, int i2, String str, Object obj) {
        HiLog.debug(LOG_TAG, "dynamicAction enter", new Object[0]);
        switch (i2) {
            case 1:
                this.mARemoteView.setInt(this.mViewId, str, convertIntValueIfNecessary(str, getInt(obj)));
                return;
            case 2:
                this.mARemoteView.setCharSequence(this.mViewId, str, getCharSequence(obj));
                return;
            case 3:
                dynamicSetFloat(i, str, obj);
                return;
            case 4:
                if (obj instanceof PixelMap) {
                    Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap((PixelMap) obj);
                    if (createShadowBitmap != null) {
                        this.mARemoteView.setBitmap(this.mViewId, "setImageBitmap", createShadowBitmap.copy(createShadowBitmap.getConfig(), false));
                        return;
                    }
                    HiLog.error(LOG_TAG, "doDynamicAction bitmap is null.", new Object[0]);
                    return;
                }
                return;
            case 5:
                this.mARemoteView.setDouble(this.mViewId, str, getDouble(obj));
                return;
            case 6:
                this.mARemoteView.setLong(this.mViewId, str, getLong(obj));
                return;
            case 7:
                this.mARemoteView.setBoolean(this.mViewId, str, getBoolean(obj));
                return;
            default:
                HiLog.error(LOG_TAG, "doDynamicAction no used type.", new Object[0]);
                return;
        }
    }

    private int convertIntValueIfNecessary(String str, int i) {
        if (!str.equals("setVisibility")) {
            return (str.equals("setImageResource") || str.equals("setIgnoreGravity")) ? convertResourceId(i) : i;
        }
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return 4;
        }
        if (i != 2) {
            return i;
        }
        return 8;
    }

    private void dynamicSetFloat(int i, String str, Object obj) {
        float f;
        if (i == 1) {
            f = (float) getInt(obj);
        } else {
            f = getFloat(obj);
        }
        this.mARemoteView.setFloat(this.mViewId, str, f);
    }

    private void doViewGroupLayoutParamAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "layoutWidth");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doViewGroupLayoutParamAction layoutWidth not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        if (i < -2) {
            HiLog.error(LOG_TAG, "doViewGroupLayoutParamAction width not unused.", new Object[0]);
            return;
        }
        HiLog.debug(LOG_TAG, "doViewGroupLayoutParamAction width=%{public}d.", new Object[]{Integer.valueOf(i)});
        this.mARemoteView.setViewLayoutWidth(this.mViewId, i);
    }

    private void doSetTextColorAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "color");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doSetTextColorAction color not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        HiLog.debug(LOG_TAG, "doSetTextColorAction color=%{public}d.", new Object[]{Integer.valueOf(i)});
        this.mARemoteView.setTextColor(this.mViewId, i);
    }

    private void doSetTextSizeAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "size");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doSetTextSizeAction size not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "textSizeType");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "doSetTextSizeAction textSizeType not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        HiLog.debug(LOG_TAG, "doSetTextSizeAction size=%{public}d textSizeType=%{public}d.", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        this.mARemoteView.setTextViewTextSize(this.mViewId, textSizeTypeTransfer(i2), (float) i);
    }

    private int textSizeTypeTransfer(int i) {
        if (Text.TextSizeType.VP.textSizeTypeValue() == i) {
            return 1;
        }
        if (Text.TextSizeType.FP.textSizeTypeValue() == i) {
            return 2;
        }
        if (Text.TextSizeType.PX.textSizeTypeValue() == i) {
        }
        return 0;
    }

    private void doViewGroupMarginLayoutAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "topMargin");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doViewGroupMarginLayoutAction topMargin not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "bottomMargin");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "doViewGroupMarginLayoutAction bottomMargin not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "rightMargin");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "doViewGroupMarginLayoutAction rightMargin not Present.", new Object[0]);
            return;
        }
        int i3 = getInt(privateValue3.get());
        HiLog.debug(LOG_TAG, "doViewGroupMarginLayoutAction topMargin=%{public}d.", new Object[]{Integer.valueOf(i)});
        this.mARemoteView.setViewLayoutMarginTopDimen(this.mViewId, i);
        this.mARemoteView.setViewLayoutMarginBottomDimen(this.mViewId, i2);
        this.mARemoteView.setViewLayoutMarginEnd(this.mViewId, i3);
        doViewGroupLayoutParamAction(action);
    }

    private void doSetProgressBarAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, ProgressBarAttrsConstants.MAX);
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doSetProgressBarAction max not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "progress_value");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "doSetProgressBarAction progress not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "isInfinite");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "doSetProgressBarAction indeterminate not Present.", new Object[0]);
            return;
        }
        boolean z = getBoolean(privateValue3.get());
        HiLog.debug(LOG_TAG, "doSetProgressBarAction progress=%{public}d.", new Object[]{Integer.valueOf(i2)});
        this.mARemoteView.setProgressBar(this.mViewId, i, i2, z);
    }

    private void doSetPaddingAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "left");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doSetPaddingAction left not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "top");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "doSetPaddingAction top not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "right");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "doSetPaddingAction right not Present.", new Object[0]);
            return;
        }
        int i3 = getInt(privateValue3.get());
        Optional<Object> privateValue4 = getPrivateValue(action, "bottom");
        if (!privateValue4.isPresent()) {
            HiLog.error(LOG_TAG, "doSetPaddingAction bottom not Present.", new Object[0]);
            return;
        }
        int i4 = getInt(privateValue4.get());
        HiLog.debug(LOG_TAG, "doSetProgressBarAction top=%{public}d.", new Object[]{Integer.valueOf(i2)});
        this.mARemoteView.setViewPadding(this.mViewId, i, i2, i3, i4);
    }

    private void doSetBackgroundPixelMapAction(ComponentProvider.Action action) {
        Bitmap createShadowBitmap;
        View findViewById;
        Optional<Object> privateValue = getPrivateValue(action, "value");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doSetBackgroundPixelMapAction value not Present.", new Object[0]);
            return;
        }
        Object obj = privateValue.get();
        HiLog.debug(LOG_TAG, "doSetBackgroundPixelMapAction.", new Object[0]);
        if ((obj instanceof PixelMap) && (createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap((PixelMap) obj)) != null && this.mLayoutId > 0) {
            Bitmap copy = createShadowBitmap.copy(createShadowBitmap.getConfig(), false);
            View inflate = View.inflate(this.mAOSPContext, this.mLayoutId, null);
            if (inflate != null && (findViewById = inflate.findViewById(this.mViewId)) != null) {
                findViewById.setBackground(new BitmapDrawable(copy));
            }
        }
    }

    private void doOnClickAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "intent");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doOnClickAction value not Present.", new Object[0]);
            return;
        }
        Object obj = privateValue.get();
        HiLog.debug(LOG_TAG, "doOnClickAction enter.", new Object[0]);
        if (obj instanceof IntentAgent) {
            PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent((IntentAgent) obj);
            if (pendingIntent == null) {
                HiLog.error(LOG_TAG, "doOnClickAction pendingIntent is null.", new Object[0]);
            } else {
                this.mARemoteView.setOnClickPendingIntent(this.mViewId, pendingIntent);
            }
        } else {
            HiLog.error(LOG_TAG, "doOnClickAction value not IntentAgent.", new Object[0]);
        }
    }

    private static Optional<Object> getPrivateValue(ComponentProvider.Action action, String str) {
        try {
            Field declaredField = action.getClass().getDeclaredField(str);
            declaredField.setAccessible(true);
            Object obj = declaredField.get(action);
            if (obj != null) {
                return Optional.of(obj);
            }
            HiLog.error(LOG_TAG, "getPrivateValue obj is null.", new Object[0]);
            return Optional.empty();
        } catch (NoSuchFieldException unused) {
            HiLog.error(LOG_TAG, "getPrivateValue NoSuchFieldException.", new Object[0]);
            return Optional.empty();
        } catch (IllegalAccessException unused2) {
            HiLog.error(LOG_TAG, "getPrivateValue IllegalAccessException.", new Object[0]);
            return Optional.empty();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getInt(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        HiLog.error(LOG_TAG, "getInt obj not int.", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CharSequence getCharSequence(Object obj) {
        if (obj instanceof CharSequence) {
            return (CharSequence) obj;
        }
        HiLog.error(LOG_TAG, "getCharSequence obj not CharSequence.", new Object[0]);
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        HiLog.error(LOG_TAG, "getString obj not String.", new Object[0]);
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getFloat(Object obj) {
        if (obj instanceof Float) {
            return ((Float) obj).floatValue();
        }
        HiLog.error(LOG_TAG, "getFloat obj not float.", new Object[0]);
        return 0.0f;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private double getDouble(Object obj) {
        if (obj instanceof Double) {
            return ((Double) obj).doubleValue();
        }
        HiLog.error(LOG_TAG, "getDouble obj not double.", new Object[0]);
        return XPath.MATCH_SCORE_QNAME;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getLong(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        }
        HiLog.error(LOG_TAG, "getLong obj not long.", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        HiLog.error(LOG_TAG, "getBoolean obj not boolean.", new Object[0]);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private char getChar(Object obj) {
        if (obj instanceof Character) {
            return ((Character) obj).charValue();
        }
        HiLog.error(LOG_TAG, "getChar obj not char.", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private byte getByte(Object obj) {
        if (obj instanceof Byte) {
            return ((Byte) obj).byteValue();
        }
        HiLog.error(LOG_TAG, "getByte obj not byte.", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private short getShort(Object obj) {
        if (obj instanceof Short) {
            return ((Short) obj).shortValue();
        }
        HiLog.error(LOG_TAG, "getShort obj not short.", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: private */
    public static class AFunctionParam {
        private final String mFunctionName;
        private final int mFunctionType;

        AFunctionParam(String str, int i) {
            this.mFunctionName = str;
            this.mFunctionType = i;
        }

        public int getType() {
            return this.mFunctionType;
        }

        public String getName() {
            return this.mFunctionName;
        }
    }

    public void marshallRemoteViewEx(ComponentProvider componentProvider, Parcel parcel) {
        int i = 0;
        if (componentProvider == null || parcel == null) {
            HiLog.error(LOG_TAG, "marshallRemoteViewEx remoteView or out is null.", new Object[0]);
            return;
        }
        setResourceBundleName(componentProvider);
        for (ComponentProvider.Action action : componentProvider.getActions()) {
            traverseAction(action);
        }
        parcel.writeString(getARemoteViewClass());
        parcel.writeInt(0);
        this.mPixelMapCache.writePixelMapsToParcel(parcel);
        parcel.writeInt(1);
        marshallApplicationInfo(parcel);
        marshallLayoutId(componentProvider.getLayoutId(), parcel);
        parcel.writeInt(0);
        ArrayList<Action> arrayList = this.mActions;
        if (arrayList != null) {
            i = arrayList.size();
        }
        parcel.writeInt(i);
        Iterator<Action> it = this.mActions.iterator();
        while (it.hasNext()) {
            Action next = it.next();
            parcel.writeInt(next.getActionTag());
            next.writeToParcel(parcel);
        }
        parcel.writeInt(this.mApplyFlags);
    }

    private void marshallApplicationInfo(Parcel parcel) {
        try {
            Optional<ApplicationInfo> applicationInfo = getApplicationInfo(this.mAOSPContext.getPackageName(), UserHandle.myUserId());
            if (applicationInfo.isPresent()) {
                android.os.Parcel obtain = android.os.Parcel.obtain();
                applicationInfo.get().writeToParcel(obtain, 0);
                obtain.setDataPosition(0);
                byte[] marshall = obtain.marshall();
                obtain.recycle();
                parcel.writeBytes(marshall);
                return;
            }
            HiLog.error(LOG_TAG, "getApplicationInfo returns null", new Object[0]);
        } catch (IllegalArgumentException | IllegalStateException e) {
            HiLog.error(LOG_TAG, "getApplicationInfo exception=%{public}s", new Object[]{e.getMessage()});
        }
    }

    private static Optional<ApplicationInfo> getApplicationInfo(String str, int i) {
        if (str == null) {
            return Optional.empty();
        }
        Application currentApplication = ActivityThread.currentApplication();
        if (currentApplication != null) {
            ApplicationInfo applicationInfo = currentApplication.getApplicationInfo();
            if (UserHandle.getUserId(applicationInfo.uid) != i || !applicationInfo.packageName.equals(str)) {
                try {
                    applicationInfo = currentApplication.getBaseContext().createPackageContextAsUser(str, 0, new UserHandle(i)).getApplicationInfo();
                } catch (PackageManager.NameNotFoundException unused) {
                    throw new IllegalArgumentException("No such bundle " + str);
                }
            }
            return Optional.of(applicationInfo);
        }
        throw new IllegalStateException("Cannot create remote views out of an aplication.");
    }

    private void marshallLayoutId(int i, Parcel parcel) {
        String str = getResourceBundleName() + ".ResourceTable";
        try {
            int aResId = ResourceManagerInner.getAResId(i, Class.forName(str, false, this.mAOSPContext.getClassLoader()), this.mAOSPContext);
            parcel.writeInt(aResId);
            HiLog.debug(LOG_TAG, "remote layoutId=0x%{public}x.", new Object[]{Integer.valueOf(aResId)});
        } catch (ClassNotFoundException unused) {
            HiLog.error(LOG_TAG, "ClassNotFoundException ClassName=%{public}s", new Object[]{str});
        }
    }

    private int getRemoteViewId(ComponentProvider.Action action) {
        int i = action.viewId;
        Optional<Object> privateValue = getPrivateValue(action, "viewId");
        if (!privateValue.isPresent()) {
            HiLog.debug(LOG_TAG, "viewId not Present, z viewId=0x%{public}x.", new Object[]{Integer.valueOf(action.viewId)});
        } else {
            i = getInt(privateValue.get());
            HiLog.debug(LOG_TAG, "z viewId=0x%{public}x.", new Object[]{Integer.valueOf(i)});
        }
        return convertResourceId(i);
    }

    public int convertResourceId(int i) {
        String str = getResourceBundleName() + ".ResourceTable";
        try {
            i = ResourceManagerInner.getAResId(i, Class.forName(str, false, this.mAOSPContext.getClassLoader()), this.mAOSPContext);
            HiLog.debug(LOG_TAG, "remote resourceId=0x%{public}x.", new Object[]{Integer.valueOf(i)});
            return i;
        } catch (ClassNotFoundException unused) {
            HiLog.error(LOG_TAG, "ClassNotFoundException ClassName=%{public}s", new Object[]{str});
            return i;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void traverseAction(ComponentProvider.Action action) {
        char c;
        if (action == null) {
            HiLog.error(LOG_TAG, "action is null.", new Object[0]);
            return;
        }
        String name = action.getClass().getName();
        HiLog.debug(LOG_TAG, "actionName=%{public}s.", new Object[]{name});
        switch (name.hashCode()) {
            case -1030354205:
                if (name.equals("ohos.agp.components.ComponentProvider$SetTextColorAction")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -268420068:
                if (name.equals("ohos.agp.components.ComponentProvider$DynamicAction")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 464037539:
                if (name.equals("ohos.agp.components.ComponentProvider$SetBackgroundPixelMapAction")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 780063294:
                if (name.equals("ohos.agp.components.ComponentProvider$SetPaddingAction")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1050038086:
                if (name.equals("ohos.agp.components.ComponentProvider$OnClickAction")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1233605181:
                if (name.equals("ohos.agp.components.ComponentProvider$ComponentContainerLayoutAction")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1476322707:
                if (name.equals("ohos.agp.components.ComponentProvider$SetProgressBarAction")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                marshallDynamicAction(action);
                return;
            case 1:
                marshallSetTextColorAction(action);
                return;
            case 2:
                marshallSetPaddingAction(action);
                return;
            case 3:
                marshallSetProgressBarAction(action);
                return;
            case 4:
                marshallComponentContainerLayoutAction(action);
                return;
            case 5:
                marshallOnClickAction(action);
                return;
            case 6:
                return;
            default:
                HiLog.error(LOG_TAG, "traverseAction: unknown action.", new Object[0]);
                return;
        }
    }

    private void marshallDynamicAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "type");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "actionType not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, Constants.ATTRNAME_OUTPUT_METHOD);
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "method not Present.", new Object[0]);
            return;
        }
        String string = getString(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "value");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "value not Present.", new Object[0]);
            return;
        }
        Object obj = privateValue3.get();
        AFunctionParam aFunctionParam = null;
        Iterator<Map.Entry<String, AFunctionParam>> it = this.mDynamicFunctionMap.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<String, AFunctionParam> next = it.next();
            if (next.getKey().equals(string)) {
                aFunctionParam = next.getValue();
                break;
            }
        }
        if (aFunctionParam == null) {
            HiLog.error(LOG_TAG, "method=%{public}s not found in DynamicFunctionMap.", new Object[]{string});
            return;
        }
        int remoteViewId = getRemoteViewId(action);
        String name = aFunctionParam.getName();
        if ("setText".equals(name) || "setHint".equals(name) || "setAccessibilityDescription".equals(name)) {
            addAction(new ReflectionAction(remoteViewId, name, 10, getCharSequence(obj)));
        } else {
            finishDynamicAction(remoteViewId, i, name, obj);
        }
    }

    private void finishDynamicAction(int i, int i2, String str, Object obj) {
        HiLog.debug(LOG_TAG, "finishDynamicAction: actionType=%{public}d, method=%{public}s.", new Object[]{Integer.valueOf(i2), str});
        switch (i2) {
            case 1:
                addAction(new ReflectionAction(i, str, 4, Integer.valueOf(convertIntValueIfNecessary(str, getInt(obj)))));
                return;
            case 2:
                addAction(new ReflectionAction(i, str, 9, getString(obj)));
                return;
            case 3:
                addAction(new ReflectionAction(i, str, 6, Float.valueOf(getFloat(obj))));
                return;
            case 4:
                if ((obj instanceof PixelMap) && str.equals("setImageBitmap")) {
                    addAction(new PixelMapReflectionAction(i, str, (PixelMap) obj));
                    return;
                }
                return;
            case 5:
                addAction(new ReflectionAction(i, str, 7, Double.valueOf(getDouble(obj))));
                return;
            case 6:
                addAction(new ReflectionAction(i, str, 5, Long.valueOf(getLong(obj))));
                return;
            case 7:
                addAction(new ReflectionAction(i, str, 1, Boolean.valueOf(getBoolean(obj))));
                return;
            default:
                HiLog.error(LOG_TAG, "finishDynamicAction: unknown actionType.", new Object[0]);
                return;
        }
    }

    private void marshallSetTextColorAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "color");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "color not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        HiLog.debug(LOG_TAG, "color=0x%{public}x.", new Object[]{Integer.valueOf(i)});
        addAction(new ReflectionAction(getRemoteViewId(action), "setTextColor", 4, Integer.valueOf(i)));
    }

    private void marshallSetPaddingAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "left");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "left not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "top");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "top not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "right");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "right not Present.", new Object[0]);
            return;
        }
        int i3 = getInt(privateValue3.get());
        Optional<Object> privateValue4 = getPrivateValue(action, "bottom");
        if (!privateValue4.isPresent()) {
            HiLog.error(LOG_TAG, "bottom not Present.", new Object[0]);
            return;
        }
        int i4 = getInt(privateValue4.get());
        HiLog.debug(LOG_TAG, "left=%{public}d top=%{public}d right=%{public}d bottom=%{public}d.", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Integer.valueOf(i4)});
        addAction(new ViewPaddingAction(getRemoteViewId(action), i, i2, i3, i4));
    }

    private void marshallSetProgressBarAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, ProgressBarAttrsConstants.MAX);
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "max not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "progress_value");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "progress not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, "isInfinite");
        if (!privateValue3.isPresent()) {
            HiLog.error(LOG_TAG, "indeterminate not Present.", new Object[0]);
            return;
        }
        boolean z = getBoolean(privateValue3.get());
        HiLog.debug(LOG_TAG, "max=%{public}d progress=%{public}d indeterminate=%{public}d.", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), Boolean.valueOf(z)});
        int remoteViewId = getRemoteViewId(action);
        addAction(new ReflectionAction(remoteViewId, "setIndeterminate", 1, Boolean.valueOf(z)));
        if (!z) {
            addAction(new ReflectionAction(remoteViewId, "setMax", 4, Integer.valueOf(i)));
            addAction(new ReflectionAction(remoteViewId, "setProgress", 4, Integer.valueOf(i2)));
        }
    }

    private void marshallComponentContainerLayoutAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "layoutWidth");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "layoutWidth not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        if (i == 0 || i == -2 || i == -1) {
            HiLog.debug(LOG_TAG, "layoutWidth=%{public}d.", new Object[]{Integer.valueOf(i)});
            Optional<Object> privateValue2 = getPrivateValue(action, "topMargin");
            if (!privateValue2.isPresent()) {
                HiLog.error(LOG_TAG, "topMargin not Present.", new Object[0]);
                return;
            }
            int i2 = getInt(privateValue2.get());
            Optional<Object> privateValue3 = getPrivateValue(action, "bottomMargin");
            if (!privateValue3.isPresent()) {
                HiLog.error(LOG_TAG, "bottomMargin not Present.", new Object[0]);
                return;
            }
            int i3 = getInt(privateValue3.get());
            Optional<Object> privateValue4 = getPrivateValue(action, "rightMargin");
            if (!privateValue4.isPresent()) {
                HiLog.error(LOG_TAG, "rightMargin not Present.", new Object[0]);
                return;
            }
            int i4 = getInt(privateValue4.get());
            HiLog.debug(LOG_TAG, "topMargin=%{public}d bottomMargin=%{public}d rightMargin=%{public}d.", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3), Integer.valueOf(i4)});
            int remoteViewId = getRemoteViewId(action);
            addAction(new LayoutParamAction(remoteViewId, 2, i));
            addAction(new LayoutParamAction(remoteViewId, 5, i2));
            addAction(new LayoutParamAction(remoteViewId, 3, i3));
            addAction(new LayoutParamAction(remoteViewId, 4, i4));
            return;
        }
        HiLog.error(LOG_TAG, "layoutWidth unused.", new Object[0]);
    }

    private void marshallOnClickAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, "intent");
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "intent not Present.", new Object[0]);
            return;
        }
        Object obj = privateValue.get();
        if (obj instanceof IntentAgent) {
            addAction(new SetOnClickResponse(getRemoteViewId(action), RemoteResponse.fromIntentAgent((IntentAgent) obj)));
        } else {
            HiLog.error(LOG_TAG, "value not IntentAgent.", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public static class PixelMapCache {
        ArrayList<PixelMap> mPixelMaps = new ArrayList<>();

        public int getPixelMapId(PixelMap pixelMap) {
            if (pixelMap == null) {
                return -1;
            }
            if (this.mPixelMaps.contains(pixelMap)) {
                return this.mPixelMaps.indexOf(pixelMap);
            }
            this.mPixelMaps.add(pixelMap);
            return this.mPixelMaps.size() - 1;
        }

        public void writePixelMapsToParcel(Parcel parcel) {
            ArrayList<PixelMap> arrayList = this.mPixelMaps;
            if (arrayList == null) {
                parcel.writeInt(-1);
                return;
            }
            parcel.writeInt(arrayList.size());
            for (int i = 0; i < this.mPixelMaps.size(); i++) {
                parcel.writeInt(1);
                ImageDoubleFwConverter.writeToParcel(this.mPixelMaps.get(i), parcel);
            }
        }
    }

    /* access modifiers changed from: private */
    public class PixelMapReflectionAction implements Action {
        String mMethodName;
        PixelMap mPixelMap;
        int mPixelMapId;
        int mViewId;

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public int getActionTag() {
            return 12;
        }

        PixelMapReflectionAction(int i, String str, PixelMap pixelMap) {
            this.mViewId = i;
            this.mMethodName = str;
            this.mPixelMap = pixelMap;
            this.mPixelMapId = RemoteViewUtils.this.mPixelMapCache.getPixelMapId(pixelMap);
        }

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mViewId);
            parcel.writeString(this.mMethodName);
            parcel.writeInt(this.mPixelMapId);
        }
    }

    /* access modifiers changed from: private */
    public static class ViewPaddingAction implements Action {
        int mBottom;
        int mLeft;
        int mRight;
        int mTop;
        int mViewId;

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public int getActionTag() {
            return 14;
        }

        public ViewPaddingAction(int i, int i2, int i3, int i4, int i5) {
            this.mViewId = i;
            this.mLeft = i2;
            this.mTop = i3;
            this.mRight = i4;
            this.mBottom = i5;
        }

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mViewId);
            parcel.writeInt(this.mLeft);
            parcel.writeInt(this.mTop);
            parcel.writeInt(this.mRight);
            parcel.writeInt(this.mBottom);
        }
    }

    /* access modifiers changed from: private */
    public static class LayoutParamAction implements Action {
        static final int LAYOUT_MARGIN_BOTTOM_DIMEN = 3;
        static final int LAYOUT_MARGIN_END = 4;
        static final int LAYOUT_MARGIN_END_DIMEN = 1;
        static final int LAYOUT_MARGIN_TOP_DIMEN = 5;
        static final int LAYOUT_WIDTH = 2;
        final int mProperty;
        final int mValue;
        int mViewId;

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public int getActionTag() {
            return 19;
        }

        public LayoutParamAction(int i, int i2, int i3) {
            this.mViewId = i;
            this.mProperty = i2;
            this.mValue = i3;
        }

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mViewId);
            parcel.writeInt(this.mProperty);
            parcel.writeInt(this.mValue);
        }
    }

    private static class TextViewSizeAction implements Action {
        float mSize;
        int mUnits;
        int mViewId;

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public int getActionTag() {
            return 13;
        }

        public TextViewSizeAction(int i, int i2, float f) {
            this.mViewId = i;
            this.mUnits = i2;
            this.mSize = f;
        }

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mViewId);
            parcel.writeInt(this.mUnits);
            parcel.writeFloat(this.mSize);
        }
    }

    /* access modifiers changed from: private */
    public final class ReflectionAction implements Action {
        static final int TYPE_BITMAP = 12;
        static final int TYPE_BOOLEAN = 1;
        static final int TYPE_BUNDLE = 13;
        static final int TYPE_BYTE = 2;
        static final int TYPE_CHAR = 8;
        static final int TYPE_CHAR_SEQUENCE = 10;
        static final int TYPE_COLOR_STATE_LIST = 15;
        static final int TYPE_DOUBLE = 7;
        static final int TYPE_FLOAT = 6;
        static final int TYPE_ICON = 16;
        static final int TYPE_INT = 4;
        static final int TYPE_INTENT = 14;
        static final int TYPE_LONG = 5;
        static final int TYPE_SHORT = 3;
        static final int TYPE_STRING = 9;
        static final int TYPE_URI = 11;
        String mMethodName;
        int mParamType;
        Object mParamValue;
        int mViewId;

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public int getActionTag() {
            return 2;
        }

        ReflectionAction(int i, String str, int i2, Object obj) {
            this.mViewId = i;
            this.mMethodName = str;
            this.mParamType = i2;
            this.mParamValue = obj;
        }

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mViewId);
            parcel.writeString(this.mMethodName);
            parcel.writeInt(this.mParamType);
            switch (this.mParamType) {
                case 1:
                    parcel.writeBoolean(RemoteViewUtils.this.getBoolean(this.mParamValue));
                    return;
                case 2:
                    parcel.writeByte(RemoteViewUtils.this.getByte(this.mParamValue));
                    return;
                case 3:
                    parcel.writeInt(RemoteViewUtils.this.getShort(this.mParamValue));
                    return;
                case 4:
                    parcel.writeInt(RemoteViewUtils.this.getInt(this.mParamValue));
                    return;
                case 5:
                    parcel.writeLong(RemoteViewUtils.this.getLong(this.mParamValue));
                    return;
                case 6:
                    parcel.writeFloat(RemoteViewUtils.this.getFloat(this.mParamValue));
                    return;
                case 7:
                    parcel.writeDouble(RemoteViewUtils.this.getDouble(this.mParamValue));
                    return;
                case 8:
                    parcel.writeInt(RemoteViewUtils.this.getChar(this.mParamValue));
                    return;
                case 9:
                    parcel.writeString(RemoteViewUtils.this.getString(this.mParamValue));
                    return;
                case 10:
                    RemoteViewUtils.writeCharSequenceEx(RemoteViewUtils.this.getCharSequence(this.mParamValue), parcel);
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SetOnClickResponse implements Action {
        final RemoteResponse mResponse;
        int mViewId;

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public int getActionTag() {
            return 1;
        }

        SetOnClickResponse(int i, RemoteResponse remoteResponse) {
            this.mViewId = i;
            this.mResponse = remoteResponse;
        }

        @Override // ohos.agp.components.surfaceview.adapter.RemoteViewUtils.Action
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mViewId);
            this.mResponse.writeToParcel(parcel);
        }
    }

    /* access modifiers changed from: private */
    public static class RemoteResponse {
        private ArrayList<String> mElementNames;
        private IntentAgent mIntentAgent;
        private IntArray mViewIds;

        private RemoteResponse() {
        }

        public static RemoteResponse fromIntentAgent(IntentAgent intentAgent) {
            RemoteResponse remoteResponse = new RemoteResponse();
            remoteResponse.mIntentAgent = intentAgent;
            return remoteResponse;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void writeToParcel(Parcel parcel) {
            if (parcel instanceof MessageParcel) {
                IntentAgentAdapterUtils.writeToParcel(this.mIntentAgent, (MessageParcel) parcel);
                IntArray intArray = this.mViewIds;
                RemoteViewUtils.writeIntArrayEx(intArray == null ? null : intArray.toArray(), parcel);
                RemoteViewUtils.writeStringListEx(this.mElementNames, parcel);
                return;
            }
            HiLog.error(RemoteViewUtils.LOG_TAG, "IntentAgent object should be written to MessageParcel.", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public static void writeCharSequenceEx(CharSequence charSequence, Parcel parcel) {
        parcel.writeInt(1);
        if (charSequence != null) {
            parcel.writeString(charSequence.toString());
        } else {
            parcel.writeString((String) null);
        }
    }

    /* access modifiers changed from: private */
    public static void writeIntArrayEx(int[] iArr, Parcel parcel) {
        if (iArr != null) {
            parcel.writeInt(iArr.length);
            for (int i : iArr) {
                parcel.writeInt(i);
            }
            return;
        }
        parcel.writeInt(-1);
    }

    /* access modifiers changed from: private */
    public static void writeStringListEx(List<String> list, Parcel parcel) {
        if (list == null) {
            parcel.writeInt(-1);
            return;
        }
        parcel.writeInt(list.size());
        for (int i = 0; i < list.size(); i++) {
            parcel.writeString(list.get(i));
        }
    }

    private void addAction(Action action) {
        if (this.mActions == null) {
            this.mActions = new ArrayList<>();
        }
        this.mActions.add(action);
    }
}

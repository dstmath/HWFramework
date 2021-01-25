package ohos.agp.components.surfaceview.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.view.View;
import android.widget.RemoteViews;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentProvider;
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

public class RemoteViewUtils {
    private static final int DYNAMIC_TYPE_BOOL = 7;
    private static final int DYNAMIC_TYPE_DOUBLE = 5;
    private static final int DYNAMIC_TYPE_FLOAT = 3;
    private static final int DYNAMIC_TYPE_INT = 1;
    private static final int DYNAMIC_TYPE_LONG = 6;
    private static final int DYNAMIC_TYPE_PIXELMAP = 4;
    private static final int DYNAMIC_TYPE_STRING = 2;
    private static final HiLogLabel LOG_TAG = new HiLogLabel(3, (int) LogDomain.END, "RemoteViewUtils");
    private Context mAOSPContext;
    private RemoteViews mARemoteView;
    private HashMap<String, AFunctionParam> mDynamicFunctionMap;
    private int mHarmonyLayoutId;
    private int mLayoutId;
    private int mViewId;

    public String getARemoteViewClass() {
        return "android.widget.RemoteViews";
    }

    public RemoteViewUtils(ohos.app.Context context) {
        this.mDynamicFunctionMap = new HashMap<>();
        HiLog.debug(LOG_TAG, "RemoteViewUtils Create enter.", new Object[0]);
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            this.mAOSPContext = (Context) hostContext;
            initFunctionMap();
            return;
        }
        HiLog.error(LOG_TAG, "RemoteViewUtils cannot get A Context.", new Object[0]);
    }

    @Deprecated
    public RemoteViewUtils(ohos.app.Context context, int i) {
        this(context);
        this.mHarmonyLayoutId = i;
    }

    @Deprecated
    public RemoteViews getARemoteView(ComponentProvider componentProvider) {
        return getARemoteViews(componentProvider).orElse(null);
    }

    public Optional<RemoteViews> getARemoteViews(ComponentProvider componentProvider) {
        HiLog.debug(LOG_TAG, "getARemoveView enter getARemoteView", new Object[0]);
        if (componentProvider == null) {
            return Optional.empty();
        }
        if (this.mHarmonyLayoutId == 0) {
            this.mHarmonyLayoutId = componentProvider.getLayoutId();
        }
        String str = this.mAOSPContext.getPackageName() + ".ResourceTable";
        try {
            Class<?> cls = Class.forName(str, false, this.mAOSPContext.getClassLoader());
            this.mLayoutId = ResourceManagerInner.getAResId(this.mHarmonyLayoutId, cls, this.mAOSPContext);
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
        Parcel obtain = Parcel.obtain();
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

    private void initFunctionMap() {
        this.mDynamicFunctionMap.put("setTextSize", new AFunctionParam("setTextSize", 3));
        this.mDynamicFunctionMap.put("setVisibility", new AFunctionParam("setVisibility", 1));
        this.mDynamicFunctionMap.put("setText", new AFunctionParam("setText", 2));
        this.mDynamicFunctionMap.put("setPixelMap", new AFunctionParam("setImageBitmap", 4));
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
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -377039240:
                if (name.equals("ohos.agp.components.ComponentProvider$ViewGroupLayoutParamAction")) {
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
            case 495401935:
                if (name.equals("ohos.agp.components.ComponentProvider$ViewGroupMarginLayoutAction")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 780063294:
                if (name.equals("ohos.agp.components.ComponentProvider$SetPaddingAction")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1050038086:
                if (name.equals("ohos.agp.components.ComponentProvider$OnClickAction")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1476322707:
                if (name.equals("ohos.agp.components.ComponentProvider$SetProgressBarAction")) {
                    c = 4;
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
                doViewGroupLayoutParamAction(action);
                return;
            case 2:
                doSetTextColorAction(action);
                return;
            case 3:
                doViewGroupMarginLayoutAction(action);
                return;
            case 4:
                doSetProgressBarAction(action);
                return;
            case 5:
                doSetPaddingAction(action);
                return;
            case 6:
                doSetBackgroundPixelMapAction(action);
                return;
            case 7:
                doOnClickAction(action);
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
                this.mARemoteView.setInt(this.mViewId, str, getInt(obj));
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
    }

    private void doSetProgressBarAction(ComponentProvider.Action action) {
        Optional<Object> privateValue = getPrivateValue(action, ProgressBarAttrsConstants.MAX);
        if (!privateValue.isPresent()) {
            HiLog.error(LOG_TAG, "doSetProgressBarAction max not Present.", new Object[0]);
            return;
        }
        int i = getInt(privateValue.get());
        Optional<Object> privateValue2 = getPrivateValue(action, "progress");
        if (!privateValue2.isPresent()) {
            HiLog.error(LOG_TAG, "doSetProgressBarAction progress not Present.", new Object[0]);
            return;
        }
        int i2 = getInt(privateValue2.get());
        Optional<Object> privateValue3 = getPrivateValue(action, ProgressBarAttrsConstants.INDETERMINATE);
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

    private int getInt(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        HiLog.error(LOG_TAG, "getInt obj not int.", new Object[0]);
        return 0;
    }

    private CharSequence getCharSequence(Object obj) {
        if (obj instanceof CharSequence) {
            return (CharSequence) obj;
        }
        HiLog.error(LOG_TAG, "getCharSequence obj not CharSequence.", new Object[0]);
        return "";
    }

    private String getString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        HiLog.error(LOG_TAG, "getString obj not String.", new Object[0]);
        return "";
    }

    private float getFloat(Object obj) {
        if (obj instanceof Float) {
            return ((Float) obj).floatValue();
        }
        HiLog.error(LOG_TAG, "getFloat obj not float.", new Object[0]);
        return 0.0f;
    }

    private double getDouble(Object obj) {
        if (obj instanceof Double) {
            return ((Double) obj).doubleValue();
        }
        HiLog.error(LOG_TAG, "getDouble obj not double.", new Object[0]);
        return XPath.MATCH_SCORE_QNAME;
    }

    private long getLong(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        }
        HiLog.error(LOG_TAG, "getLong obj not long.", new Object[0]);
        return 0;
    }

    private boolean getBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        HiLog.error(LOG_TAG, "getBoolean obj not boolean.", new Object[0]);
        return false;
    }

    /* access modifiers changed from: private */
    public static class AFunctionParam {
        private String mFunctionName;
        private int mFunctionType;

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
}

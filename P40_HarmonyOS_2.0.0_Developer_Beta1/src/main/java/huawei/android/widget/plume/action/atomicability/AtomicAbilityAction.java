package huawei.android.widget.plume.action.atomicability;

import android.content.Context;
import android.util.Log;
import android.view.View;
import huawei.android.widget.plume.action.PlumeAction;
import huawei.android.widget.plume.model.AttrInfo;
import huawei.android.widget.plume.util.ConvertUtil;
import huawei.android.widget.plume.util.ReflectUtil;
import java.util.HashMap;
import java.util.Map;

public class AtomicAbilityAction extends PlumeAction {
    private static final int DEFAULT_EXTEND_REVEAL_DIP = 16;
    private static final float DEFAULT_SCALE_RATIO_VALUE = -1.0f;
    private static final int DEFAULT_WRAP_REFERENCE_SIZE = -1;
    private static final int INITIAL_MAP_SIZE = 40;
    private static final float INVALID_WEIGHT_VALUE = -1.0f;
    private static final String TAG = AtomicAbilityAction.class.getSimpleName();
    private AttrInfo mAttrInfo = null;
    private Map<String, AtomicAbilityAttrProcessor> mAttrInfoMap = new HashMap(40);

    /* access modifiers changed from: private */
    public interface AtomicAbilityAttrProcessor {
        void run(String str);
    }

    public AtomicAbilityAction(Context context, View view) {
        super(context, view);
        initAttrInfoMap();
    }

    @Override // huawei.android.widget.plume.action.PlumeAction
    public void apply(String attrName, String value) {
        initAttrInfo(attrName, value);
        AttrInfo attrInfo = this.mAttrInfo;
        if (attrInfo != null) {
            ReflectUtil.invokeReflect(attrInfo);
        }
    }

    private void initAttrInfo(String attrName, String value) {
        AtomicAbilityAttrProcessor processor = this.mAttrInfoMap.get(attrName);
        if (processor != null) {
            processor.run(value);
            return;
        }
        String str = TAG;
        Log.e(str, "Plume: map doesn't contain attr " + attrName);
    }

    private void initAttrInfoMap() {
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_HORIZONTAL_STRETCH_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$xrtCRsEeWmTmBqh5LelqjKpOOg */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initHorizontalStretchEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_VERTICAL_STRETCH_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$CXlhYVrRmRgcdhkTNgPRAp4iHCY */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initVerticalStretchEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_MAX_WIDTH, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$dcdd6HVlu818rtJFrHNZEyM9do */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initMaxWidth(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_MIN_WIDTH, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$KrDK0A050PqhS5fh0knCC3TfkKU */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initMinWidth(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_MAX_HEIGHT, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$Hk6j1ab72B3Z6JMevWAyhiw_2bE */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initMaxHeight(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_MIN_HEIGHT, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$wc_GPS0nOQy1mcj0yeEpQ3k1F2U */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initMinHeight(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_PADDING_START, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$sVklJndNodMizTw1M2kZNOpq9K0 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initPaddingStart(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_PADDING_END, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$Q1GicGXb7baScarJvxWtB95Y_yE */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initPaddingEnd(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_PADDING_TOP, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$mIiyETLNFDWW5IISNmoScuUZHM */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initPaddingTop(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_PADDING_BOTTOM, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$8w7Z7TiRREbED9Yl7icfrJRgVr8 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initPaddingBottom(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_SCALE_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$UEKzkKoKTBuGEEDa0JvNjZUGRs */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initScaleEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_SCALE_RATE_WIDTH, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$NL3DIQZrJOqjKxEB30_042gw9_A */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutScaleRateWidth(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_SCALE_RATE_HEIGHT, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$U4CStYNmQ6WqZTZSwji4OxWo */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutScaleRateHeight(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_MAX_SCALE_WIDTH, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$oftZyd_RgwrboawAAH09GRi20E */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutMaxScaleWidth(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_MIN_SCALE_WIDTH, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$QH3n2H8fNG1vrfIW_VrmWXOIZ3U */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutMinScaleWidth(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_MAX_SCALE_HEIGHT, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$icpFFDNh0ibF20oaCZIEnq3xZAE */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutMaxScaleHeight(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_MIN_SCALE_HEIGHT, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$FfDdj__AcmB3MuMtLpODP9FSwF4 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutMinScaleHeight(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_HORIZONTAL_HIDE_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$_XwwjWvOsech57iOJwU_mOHk7Uo */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initHorizontalHideEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_VERTICAL_HIDE_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$F68NvHa9Ujt4adGo5wrtIfxdr_4 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initVerticalHideEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_HORIZONTAL_HIDE_PRIORITY, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$EUiF6iX005Zgsx7f8w6rRJQ4M0 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutHorizontalHidePriority(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_VERTICAL_HIDE_PRIORITY, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$Zlg9E_qO7uBRcub_VVr4dZ6PvU */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutVerticalHidePriority(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_WRAP_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$RRoNzqarDWKpNfGA3BXNSG0yWE */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initWrapEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_WRAP_DIRECTION, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$evPqzLebuWeCXkb0PzMAur47GjU */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initWrapDirection(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_WRAP_GRAVITY, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$TB5LKzvlKv_sMsCBOHf3KTwZF24 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initWrapGravity(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_WRAP_REFERENCE_SIZE, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$Me_Ac_RUsgwzbTMl7DiJ7pSeopE */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initWrapReferenceSize(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_SPREAD_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$WI7pphP9rZjeAHLIyfzya2hnf1w */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initSpreadEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_SPREAD_TYPE, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$1OR8cXb2ghZew6xk2T_r8nz91Ow */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initSpreadType(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_SPREAD_MAX_MARGIN, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$gZhu3rPa1DIF1_sJf80SJknGEY */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initSpreadMaxMargin(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_SPREAD_MIN_MARGIN, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$nmq8f0FeE69Z4n5MrPJRXxkmE4c */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initSpreadMinMargin(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_WEIGHT_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$sxkMfW30yNm9UKzFcyP4n6HxWSs */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initWeightEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_LAYOUT_WEIGHT, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$QCGtNMbJq6tRwb9AEcpYpdWwrHY */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initLayoutWeight(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_EXTEND_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$_hx1amy24BB8WTifxxqjzIWtQzE */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initExtendEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_EXTEND_REVEAL_ENABLED, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$3rcAMWEVgAjuKRFDRQwGQbKgl3s */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initExtendRevealEnabled(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_EXTEND_REVEAL_SIZE, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$Yjh6hQtejFgIlaXqt9MF3DnkcUA */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initExtendRevealSize(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_EXTEND_DEFAULT_MARGIN, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$4H92DGfWJL6XSFY_md1JZRL8pJI */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initExtendDefaultMargin(str);
            }
        });
        this.mAttrInfoMap.put(AtomicAbilityConstants.ATTR_EXTEND_MIN_MARGIN, new AtomicAbilityAttrProcessor() {
            /* class huawei.android.widget.plume.action.atomicability.$$Lambda$AtomicAbilityAction$tept5sYiXhQROYzYrGRaE_bUP8 */

            @Override // huawei.android.widget.plume.action.atomicability.AtomicAbilityAction.AtomicAbilityAttrProcessor
            public final void run(String str) {
                AtomicAbilityAction.this.initExtendMinMargin(str);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initHorizontalStretchEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_HORIZONTAL_STRETCH_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initVerticalStretchEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_VERTICAL_STRETCH_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initMaxWidth(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_MAX_WIDTH, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 2.14748365E9f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initMinWidth(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_MIN_WIDTH, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initMaxHeight(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_MAX_HEIGHT, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 2.14748365E9f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initMinHeight(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_MIN_HEIGHT, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPaddingStart(String value) {
        if (this.mTarget == null) {
            Log.w(TAG, "Plume: initPaddingStart mTarget is null");
        } else {
            this.mTarget.setPaddingRelative(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f), this.mTarget.getPaddingTop(), this.mTarget.getPaddingEnd(), this.mTarget.getPaddingBottom());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPaddingEnd(String value) {
        if (this.mTarget == null) {
            Log.w(TAG, "Plume: initPaddingEnd mTarget is null");
        } else {
            this.mTarget.setPaddingRelative(this.mTarget.getPaddingStart(), this.mTarget.getPaddingTop(), ConvertUtil.convertConfigValue(this.mContext, value, 0.0f), this.mTarget.getPaddingBottom());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPaddingTop(String value) {
        if (this.mTarget == null) {
            Log.w(TAG, "Plume: initPaddingTop mTarget is null");
        } else {
            this.mTarget.setPaddingRelative(this.mTarget.getPaddingStart(), ConvertUtil.convertConfigValue(this.mContext, value, 0.0f), this.mTarget.getPaddingEnd(), this.mTarget.getPaddingBottom());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPaddingBottom(String value) {
        if (this.mTarget == null) {
            Log.w(TAG, "Plume: initPaddingBottom mTarget is null");
        } else {
            this.mTarget.setPaddingRelative(this.mTarget.getPaddingStart(), this.mTarget.getPaddingTop(), this.mTarget.getPaddingEnd(), ConvertUtil.convertConfigValue(this.mContext, value, 0.0f));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initScaleEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_SCALE_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutScaleRateWidth(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_SCALE_RATE_WIDTH, this.mTarget, new Class[]{Float.TYPE}, new Object[]{Float.valueOf(ConvertUtil.convertFractionToFloat(value, -1.0f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutScaleRateHeight(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_SCALE_RATE_HEIGHT, this.mTarget, new Class[]{Float.TYPE}, new Object[]{Float.valueOf(ConvertUtil.convertFractionToFloat(value, -1.0f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutMaxScaleWidth(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_MAX_SCALE_WIDTH, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 2.14748365E9f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutMinScaleWidth(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_MIN_SCALE_WIDTH, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutMaxScaleHeight(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_MAX_SCALE_HEIGHT, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 2.14748365E9f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutMinScaleHeight(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_MIN_SCALE_HEIGHT, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initHorizontalHideEnabled(String value) {
        this.mAttrInfo = new AttrInfo("setLayoutHideEnabled", this.mTarget, new Class[]{Integer.TYPE, Boolean.TYPE}, new Object[]{0, Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initVerticalHideEnabled(String value) {
        this.mAttrInfo = new AttrInfo("setLayoutHideEnabled", this.mTarget, new Class[]{Integer.TYPE, Boolean.TYPE}, new Object[]{1, Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutHorizontalHidePriority(String value) {
        this.mAttrInfo = new AttrInfo("setHidePriority", this.mTarget, new Class[]{Integer.TYPE, Integer.TYPE}, new Object[]{0, Integer.valueOf(ConvertUtil.convertInteger(value, 0))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutVerticalHidePriority(String value) {
        this.mAttrInfo = new AttrInfo("setHidePriority", this.mTarget, new Class[]{Integer.TYPE, Integer.TYPE}, new Object[]{1, Integer.valueOf(ConvertUtil.convertInteger(value, 0))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWrapEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_WRAP_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWrapDirection(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_WRAP_DIRECTION, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertWrapDirection(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWrapGravity(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_WRAP_GRAVITY, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertWrapGravity(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWrapReferenceSize(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_WRAP_REFERENCE_SIZE, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, -1.0f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSpreadEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_SPREAD_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSpreadType(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_SPREAD_TYPE, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertSpreadType(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSpreadMaxMargin(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_SPREAD_MAX_MARGIN, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 2.14748365E9f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSpreadMinMargin(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_SPREAD_MIN_MARGIN, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWeightEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_WEIGHT_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLayoutWeight(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_LAYOUT_WEIGHT, this.mTarget, new Class[]{Float.TYPE}, new Object[]{Float.valueOf(ConvertUtil.convertFractionToFloat(value, -1.0f))}, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initExtendEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_EXTEND_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initExtendRevealEnabled(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_EXTEND_REVEAL_ENABLED, this.mTarget, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Boolean.parseBoolean(value))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initExtendRevealSize(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_EXTEND_REVEAL_SIZE, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, ConvertUtil.dpToPx(16.0f)))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initExtendDefaultMargin(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_EXTEND_DEFAULT_MARGIN, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, -2.14748365E9f))}, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initExtendMinMargin(String value) {
        this.mAttrInfo = new AttrInfo(AtomicAbilityConstants.METHOD_EXTEND_MIN_MARGIN, this.mTarget, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(ConvertUtil.convertConfigValue(this.mContext, value, 0.0f))}, 1);
    }
}

package huawei.android.widget.plume.action.interaction;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import huawei.android.widget.plume.action.PlumeAction;
import huawei.android.widget.plume.model.AttrInfo;
import huawei.android.widget.plume.util.ReflectUtil;

public class AttributeAction extends PlumeAction {
    private static final boolean DEBUG = false;
    private static final String TAG = AttributeAction.class.getSimpleName();

    public AttributeAction(Context context, View view) {
        super(context, view);
    }

    @Override // huawei.android.widget.plume.action.PlumeAction
    public void apply(String attrName, String value) {
        if (attrName == null || value == null) {
            Log.e(TAG, "Plume: attrName or value is null");
        } else {
            initAttrInfo(attrName, value);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void initAttrInfo(String attrName, String value) {
        char c;
        switch (attrName.hashCode()) {
            case -2121101752:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_INSERT)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 704624889:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_SEEK_BAR_SCROLL)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 764500918:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_LIST_SCROLL)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 830857029:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_SWITCH_TAB_WHEN_FOCUSED)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1228303800:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_QUICK_SELECT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1694090304:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_SWITCH_TAB)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1755945966:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_ZOOM)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1827655451:
                if (attrName.equals(UnifiedInteractionConstants.ATTR_CONSECUTIVE_SELECT)) {
                    c = 1;
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
                setInsert(value);
                return;
            case 1:
                setConsecutiveSelect(value);
                return;
            case 2:
                setQuickSelect(value);
                return;
            case 3:
                setListScroll(value);
                return;
            case 4:
                setSeekBarScroll(value);
                return;
            case 5:
                setZoom(value);
                return;
            case 6:
                unifiedInteractionReflect(value, "setExtendedNextTabEnabled", true);
                return;
            case 7:
                unifiedInteractionReflect(value, "setExtendedNextTabEnabled", false);
                return;
            default:
                Log.e(TAG, "Plume: incorrect attribute name, " + attrName);
                return;
        }
    }

    private void unifiedInteractionReflect(String value, String tag, boolean isGlobal) {
        ReflectUtil.invokeReflect(new AttrInfo(tag, this.mTarget, new Class[]{Boolean.TYPE, Boolean.TYPE}, new Object[]{Boolean.valueOf(isGlobal), Boolean.valueOf(Boolean.parseBoolean(value))}, 1));
    }

    private void setZoom(String value) {
        if (!(this.mTarget instanceof ImageView)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to ImageView, when executing setZoom method.");
            return;
        }
        ((ImageView) this.mTarget).setOnZoomEnabled(Boolean.parseBoolean(value));
    }

    private void setSeekBarScroll(String value) {
        if (!(this.mTarget instanceof SeekBar)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to SeekBar, when executing setSeekBarScroll method.");
            return;
        }
        ((SeekBar) this.mTarget).setExtendProgressEnabled(Boolean.parseBoolean(value));
    }

    private void setListScroll(String value) {
        if (this.mTarget instanceof AbsListView) {
            ((AbsListView) this.mTarget).setExtendScrollEnabled(Boolean.parseBoolean(value));
        } else if (this.mTarget instanceof ScrollView) {
            ((ScrollView) this.mTarget).setExtendScrollEnabled(Boolean.parseBoolean(value));
        } else if (this.mTarget instanceof HorizontalScrollView) {
            ((HorizontalScrollView) this.mTarget).setExtendScrollEnabled(Boolean.parseBoolean(value));
        } else {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to AbsListView, ScrollView or HorizontalScrollView, when executing setListScroll method.");
        }
    }

    private void setQuickSelect(String value) {
        if (!(this.mTarget instanceof AbsListView)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to AbsListView, when executing setQuickSelect method.");
            return;
        }
        ((AbsListView) this.mTarget).setExtendedMultiChoiceEnabled(false, Boolean.parseBoolean(value));
    }

    private void setConsecutiveSelect(String value) {
        if (!(this.mTarget instanceof AbsListView)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to AbsListView, when executing setConsecutiveSelect method.");
            return;
        }
        ((AbsListView) this.mTarget).setExtendedMultiChoiceEnabled(true, Boolean.parseBoolean(value));
    }

    private void setInsert(String value) {
        if (!(this.mTarget instanceof EditText)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to EditText, when executing setInsert method.");
            return;
        }
        ((EditText) this.mTarget).setExtendedEditEnabled(Boolean.parseBoolean(value));
    }
}

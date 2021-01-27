package huawei.android.widget.plume.action;

import android.content.Context;
import android.view.View;
import huawei.android.widget.plume.action.atomicability.AtomicAbilityAction;
import huawei.android.widget.plume.action.atomicability.AtomicAbilityConstants;
import huawei.android.widget.plume.action.interaction.AttributeAction;
import huawei.android.widget.plume.action.interaction.ListenerAction;
import huawei.android.widget.plume.action.interaction.UnifiedInteractionConstants;

public class PlumeActionProvider {
    private static final String[] ATOMIC_ABILITY_ATTRS = {AtomicAbilityConstants.ATTR_HORIZONTAL_STRETCH_ENABLED, AtomicAbilityConstants.ATTR_VERTICAL_STRETCH_ENABLED, AtomicAbilityConstants.ATTR_MAX_WIDTH, AtomicAbilityConstants.ATTR_MIN_WIDTH, AtomicAbilityConstants.ATTR_MAX_HEIGHT, AtomicAbilityConstants.ATTR_MIN_HEIGHT, AtomicAbilityConstants.ATTR_PADDING_START, AtomicAbilityConstants.ATTR_PADDING_END, AtomicAbilityConstants.ATTR_PADDING_TOP, AtomicAbilityConstants.ATTR_PADDING_BOTTOM, AtomicAbilityConstants.ATTR_SCALE_ENABLED, AtomicAbilityConstants.ATTR_LAYOUT_SCALE_RATE_WIDTH, AtomicAbilityConstants.ATTR_LAYOUT_SCALE_RATE_HEIGHT, AtomicAbilityConstants.ATTR_LAYOUT_MAX_SCALE_WIDTH, AtomicAbilityConstants.ATTR_LAYOUT_MIN_SCALE_WIDTH, AtomicAbilityConstants.ATTR_LAYOUT_MAX_SCALE_HEIGHT, AtomicAbilityConstants.ATTR_LAYOUT_MIN_SCALE_HEIGHT, AtomicAbilityConstants.ATTR_HORIZONTAL_HIDE_ENABLED, AtomicAbilityConstants.ATTR_VERTICAL_HIDE_ENABLED, AtomicAbilityConstants.ATTR_LAYOUT_HORIZONTAL_HIDE_PRIORITY, AtomicAbilityConstants.ATTR_LAYOUT_VERTICAL_HIDE_PRIORITY, AtomicAbilityConstants.ATTR_WRAP_ENABLED, AtomicAbilityConstants.ATTR_WRAP_DIRECTION, AtomicAbilityConstants.ATTR_WRAP_GRAVITY, AtomicAbilityConstants.ATTR_LAYOUT_WRAP_REFERENCE_SIZE, AtomicAbilityConstants.ATTR_SPREAD_ENABLED, AtomicAbilityConstants.ATTR_SPREAD_TYPE, AtomicAbilityConstants.ATTR_SPREAD_MAX_MARGIN, AtomicAbilityConstants.ATTR_SPREAD_MIN_MARGIN, AtomicAbilityConstants.ATTR_WEIGHT_ENABLED, AtomicAbilityConstants.ATTR_LAYOUT_WEIGHT, AtomicAbilityConstants.ATTR_EXTEND_ENABLED, AtomicAbilityConstants.ATTR_EXTEND_REVEAL_ENABLED, AtomicAbilityConstants.ATTR_EXTEND_REVEAL_SIZE, AtomicAbilityConstants.ATTR_EXTEND_DEFAULT_MARGIN, AtomicAbilityConstants.ATTR_EXTEND_MIN_MARGIN};
    private static final String[] UNIFIED_INTERACTION_ATTRS = {UnifiedInteractionConstants.ATTR_INSERT, UnifiedInteractionConstants.ATTR_CONSECUTIVE_SELECT, UnifiedInteractionConstants.ATTR_QUICK_SELECT, UnifiedInteractionConstants.ATTR_LIST_SCROLL, UnifiedInteractionConstants.ATTR_SEEK_BAR_SCROLL, UnifiedInteractionConstants.ATTR_ZOOM, UnifiedInteractionConstants.ATTR_SWITCH_TAB, UnifiedInteractionConstants.ATTR_SWITCH_TAB_WHEN_FOCUSED};
    private static final String[] UNIFIED_INTERACTION_EVENTS = {UnifiedInteractionConstants.EVENT_ZOOM, UnifiedInteractionConstants.EVENT_SEARCH, UnifiedInteractionConstants.EVENT_COPY, UnifiedInteractionConstants.EVENT_PASTE, UnifiedInteractionConstants.EVENT_CUT, UnifiedInteractionConstants.EVENT_SELECT_ALL, UnifiedInteractionConstants.EVENT_UNDO, UnifiedInteractionConstants.EVENT_DELETE};

    private PlumeActionProvider() {
    }

    private static boolean isContainedInArray(String[] array, String name) {
        if (name == null || array == null) {
            return false;
        }
        for (String attrName : array) {
            if (name.equals(attrName)) {
                return true;
            }
        }
        return false;
    }

    public static PlumeAction getAction(Context context, View widget, String name) {
        if (context == null || widget == null || name == null) {
            return null;
        }
        if (isContainedInArray(UNIFIED_INTERACTION_ATTRS, name)) {
            return new AttributeAction(context, widget);
        }
        if (isContainedInArray(UNIFIED_INTERACTION_EVENTS, name)) {
            Object object = widget.getTag(34603463);
            if (object == null) {
                ListenerAction action = new ListenerAction(context, widget);
                widget.setTag(34603463, action);
                return action;
            } else if (!(object instanceof ListenerAction)) {
                return null;
            } else {
                return (ListenerAction) object;
            }
        } else if (isContainedInArray(ATOMIC_ABILITY_ATTRS, name)) {
            return new AtomicAbilityAction(context, widget);
        } else {
            return null;
        }
    }
}

package ohos.accessibility.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsSeekBar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.widget.ViewPager;
import java.util.HashMap;
import java.util.Map;

public class AccessibilityConst {
    public static final int ACCESSIBILITY_EVENT_TYPE = 1;
    public static final int ACCESSIBILITY_SERVICE_CLOSED = 2;
    public static final int ACCESSIBILITY_SERVICE_ENABLE = 1;
    public static final int ACCESSIBILITY_SERVICE_INSTALLED = 3;
    public static final int ACE_EVENT_BLUR = 108;
    public static final int ACE_EVENT_CHANGE = 100;
    public static final int ACE_EVENT_CLICK = 105;
    public static final int ACE_EVENT_FOCUS = 107;
    public static final int ACE_EVENT_LONG_PRESS = 106;
    public static final int ACE_EVENT_PAGE_ROUTE_CHANGED = 200;
    public static final int ACE_EVENT_TOUCH_CANCEL = 103;
    public static final int ACE_EVENT_TOUCH_END = 104;
    public static final int ACE_EVENT_TOUCH_MOVE = 102;
    public static final int ACE_EVENT_TOUCH_START = 101;
    public static final int ACE_ROOT_VIEW_ID = 0;
    public static final int ENABLE = 0;
    public static final int ROOT_CONTENT_VIEW_ID = 2147483547;
    public static final int TOUCHEXPLORATION_EVENT_TYPE = 2;
    public static final int UNENABLE = 1;
    private static final Map<String, String> VIEW_CLASS_MAP = new HashMap();

    static {
        VIEW_CLASS_MAP.put("UIView", View.class.getName());
        VIEW_CLASS_MAP.put("UIViewGroup", ViewGroup.class.getName());
        VIEW_CLASS_MAP.put("UIButton", Button.class.getName());
        VIEW_CLASS_MAP.put("UIAbsSeekBar", AbsSeekBar.class.getName());
        VIEW_CLASS_MAP.put("UICheckbox", CheckBox.class.getName());
        VIEW_CLASS_MAP.put("UIEditText", EditText.class.getName());
        VIEW_CLASS_MAP.put("UIListView", AbsListView.class.getName());
        VIEW_CLASS_MAP.put("UINumberPicker", NumberPicker.class.getName());
        VIEW_CLASS_MAP.put("UIProgressBar", ProgressBar.class.getName());
        VIEW_CLASS_MAP.put("UIRadioButton", RadioButton.class.getName());
        VIEW_CLASS_MAP.put("UIRadioGroup", RadioGroup.class.getName());
        VIEW_CLASS_MAP.put("UIRatingBar", RatingBar.class.getName());
        VIEW_CLASS_MAP.put("UIRoundProgressBar", ProgressBar.class.getName());
        VIEW_CLASS_MAP.put("UIScrollView", ScrollView.class.getName());
        VIEW_CLASS_MAP.put("UISeekBar", SeekBar.class.getName());
        VIEW_CLASS_MAP.put("UISwitch", Switch.class.getName());
        VIEW_CLASS_MAP.put("UITextView", TextView.class.getName());
        VIEW_CLASS_MAP.put("UIViewPager", ViewPager.class.getName());
    }

    private AccessibilityConst() {
    }

    public static String getViewTypeClass(String str, String str2) {
        return VIEW_CLASS_MAP.getOrDefault(str, str2);
    }
}

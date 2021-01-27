package ohos.accessibility.adapter;

import android.app.ActionBar;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import android.widget.AbsListView;
import android.widget.AbsSeekBar;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.android.internal.widget.ViewPager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.ai.engine.bigreport.BigReportKeyValue;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;

public class AccessibilityConst {
    public static final int ACCESSIBILITY_EVENT_TYPE = 1;
    public static final int ACCESSIBILITY_SERVICE_CLOSED = 2;
    public static final int ACCESSIBILITY_SERVICE_ENABLE = 1;
    public static final int ACCESSIBILITY_SERVICE_INSTALLED = 3;
    public static final int ACE_EVENT_PAGE_ROUTE_CHANGED = 200;
    public static final int ACE_ROOT_VIEW_ID = 0;
    public static final int ENABLE = 0;
    public static final int ROOT_CONTENT_VIEW_ID = 2147483547;
    public static final int TOUCH_EXPLORATION_EVENT_TYPE = 2;
    public static final int UNABLE = 1;
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
        VIEW_CLASS_MAP.put("text", TextView.class.getName());
        VIEW_CLASS_MAP.put("span", TextView.class.getName());
        VIEW_CLASS_MAP.put("titleText", TextView.class.getName());
        VIEW_CLASS_MAP.put("headerText", TextView.class.getName());
        VIEW_CLASS_MAP.put("textarea", EditText.class.getName());
        VIEW_CLASS_MAP.put("div", ViewGroup.class.getName());
        VIEW_CLASS_MAP.put("progress", ProgressBar.class.getName());
        VIEW_CLASS_MAP.put("list-item", ViewGroup.class.getName());
        VIEW_CLASS_MAP.put(SchemaSymbols.ATTVAL_LIST, ListView.class.getName());
        VIEW_CLASS_MAP.put("rating", RatingBar.class.getName());
        VIEW_CLASS_MAP.put("switch", Switch.class.getName());
        VIEW_CLASS_MAP.put(BigReportKeyValue.TYPE_IMAGE, ImageView.class.getName());
        VIEW_CLASS_MAP.put("button", Button.class.getName());
        VIEW_CLASS_MAP.put("dialogButton", Button.class.getName());
        VIEW_CLASS_MAP.put("checkbox", CheckBox.class.getName());
        VIEW_CLASS_MAP.put("radio", RadioButton.class.getName());
        VIEW_CLASS_MAP.put("search", SearchView.class.getName());
        VIEW_CLASS_MAP.put(Constants.ATTRNAME_SELECT, Spinner.class.getName());
        VIEW_CLASS_MAP.put("menu", ActionMenuView.class.getName());
        VIEW_CLASS_MAP.put("navigation-bar", ActionBar.class.getName());
        VIEW_CLASS_MAP.put("slider", SeekBar.class.getName());
        VIEW_CLASS_MAP.put("Toast", Toast.class.getName());
        VIEW_CLASS_MAP.put(BigReportKeyValue.TYPE_VIDEO, VideoView.class.getName());
        VIEW_CLASS_MAP.put("canvas", Canvas.class.getName());
    }

    private AccessibilityConst() {
    }

    public static String getViewTypeClass(String str, String str2) {
        return VIEW_CLASS_MAP.getOrDefault(str, str2);
    }

    public static void convertEventToEventInfo(AccessibilityEvent accessibilityEvent, AccessibilityEventInfo accessibilityEventInfo) {
        if (!(accessibilityEvent == null || accessibilityEventInfo == null)) {
            accessibilityEventInfo.setAccessibilityEventType(accessibilityEvent.getEventType());
            accessibilityEventInfo.setWindowChangeTypes(accessibilityEvent.getContentChangeTypes());
            accessibilityEventInfo.setTextMoveStep(accessibilityEvent.getMovementGranularity());
            accessibilityEventInfo.setBundleName(accessibilityEvent.getPackageName());
            accessibilityEventInfo.setTriggerAction(accessibilityEvent.getAction());
            fillEventInfoWithRecord(accessibilityEventInfo, accessibilityEvent);
            for (int i = 0; i < accessibilityEvent.getRecordCount(); i++) {
                AccessibilityRecord record = accessibilityEvent.getRecord(i);
                AccessibilityEventInfo accessibilityEventInfo2 = new AccessibilityEventInfo(accessibilityEvent.getEventType());
                fillEventInfoWithRecord(accessibilityEventInfo2, record);
                accessibilityEventInfo.addRecord(accessibilityEventInfo2);
            }
        }
    }

    private static void fillEventInfoWithRecord(AccessibilityEventInfo accessibilityEventInfo, AccessibilityRecord accessibilityRecord) {
        accessibilityEventInfo.setViewId(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityRecord.getSourceNodeId()));
        accessibilityEventInfo.setClassName(accessibilityRecord.getClassName());
        accessibilityEventInfo.setDescription(accessibilityRecord.getContentDescription());
        accessibilityEventInfo.setCount(accessibilityRecord.getItemCount());
        accessibilityEventInfo.setPresentIndex(accessibilityRecord.getCurrentItemIndex());
        accessibilityEventInfo.setStartIndex(accessibilityRecord.getFromIndex());
        accessibilityEventInfo.setEndIndex(accessibilityRecord.getToIndex());
        accessibilityEventInfo.setLastContent(accessibilityRecord.getBeforeText());
        accessibilityEventInfo.getContentList().addAll(accessibilityRecord.getText());
    }

    public static void convertEventInfoToEvent(View view, AccessibilityEventInfo accessibilityEventInfo, AccessibilityEvent accessibilityEvent) {
        if (!(accessibilityEventInfo == null || accessibilityEvent == null)) {
            accessibilityEvent.setEventType(accessibilityEventInfo.getAccessibilityEventType());
            accessibilityEvent.setAction(accessibilityEventInfo.getTriggerAction());
            accessibilityEvent.setContentChangeTypes(accessibilityEventInfo.getWindowChangeTypes());
            accessibilityEvent.setMovementGranularity(accessibilityEventInfo.getTextMoveStep());
            if (view == null || view.getContext() == null) {
                accessibilityEvent.setPackageName(accessibilityEventInfo.getBundleName());
            } else {
                accessibilityEvent.setPackageName(view.getContext().getPackageName());
            }
            fillEventWithSourceInfo(view, accessibilityEvent, accessibilityEventInfo);
            ArrayList<AccessibilityEventInfo> records = accessibilityEventInfo.getRecords();
            if (!records.isEmpty()) {
                for (AccessibilityEventInfo accessibilityEventInfo2 : records) {
                    if (accessibilityEventInfo2 != null) {
                        AccessibilityRecord obtain = AccessibilityRecord.obtain();
                        fillEventWithSourceInfo(view, obtain, accessibilityEventInfo2);
                        accessibilityEvent.appendRecord(obtain);
                    }
                }
            }
        }
    }

    private static void fillEventWithSourceInfo(View view, AccessibilityRecord accessibilityRecord, AccessibilityEventInfo accessibilityEventInfo) {
        accessibilityRecord.setSource(view, accessibilityEventInfo.getViewId());
        accessibilityRecord.setImportantForAccessibility(true);
        String valueOf = String.valueOf(accessibilityEventInfo.getClassName());
        accessibilityRecord.setClassName(getViewTypeClass(valueOf, valueOf));
        accessibilityRecord.setContentDescription(accessibilityEventInfo.getDescription());
        accessibilityRecord.setItemCount(accessibilityEventInfo.getCount());
        accessibilityRecord.setCurrentItemIndex(accessibilityEventInfo.getPresentIndex());
        accessibilityRecord.setFromIndex(accessibilityEventInfo.getStartIndex());
        accessibilityRecord.setToIndex(accessibilityEventInfo.getEndIndex());
        accessibilityRecord.setBeforeText(accessibilityEventInfo.getLastContent());
        accessibilityRecord.getText().addAll(accessibilityEventInfo.getContentList());
    }
}

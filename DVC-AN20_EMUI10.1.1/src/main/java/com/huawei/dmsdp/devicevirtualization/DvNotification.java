package com.huawei.dmsdp.devicevirtualization;

import android.util.Log;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import java.util.HashMap;
import java.util.Map;

public class DvNotification {
    public static final int COMMON_LAYOUT_TEMPLATE = 1;
    public static final int GUIDE_LAYOUT_TEMPLATE = 2;
    public static final String KEY_CONTENT = "Content";
    public static final String KEY_DATE = "Date";
    public static final String KEY_DIRECTION_INDEX = "GuideDirectionId";
    public static final String KEY_DISTANCE_UNIT = "GuideDistanceUnit";
    public static final String KEY_GUIDE_DISTANCE = "GuideDistance";
    public static final String KEY_GUIDE_TEXT = "GuideText";
    public static final String KEY_ICON_INDEX = "IconId";
    public static final String KEY_PACKAGE_NAME = "PackageName";
    public static final String KEY_SUBTITLE = "Subtitle";
    public static final String KEY_TEMPLATE = "Template";
    public static final String KEY_TITLE = "Title";
    public static final String KEY_VIBRATE = "Vibrate";
    private static final int MAX_LENGTH = 8;
    private static final String TAG = "DvNotification";
    private String mContent;
    private long mDate;
    private int mGuideDirectionId;
    private int mGuideDistance;
    private String mGuideDistanceUnit;
    private String mGuideText;
    private int mIconId;
    private String mPackageName;
    private Map<String, Object> mPropertyMap = new HashMap(8);
    private String mSubtitle;
    private int mTemplate;
    private String mTitle;
    private int mVibrate;

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public int getTemplate() {
        return this.mTemplate;
    }

    public void setTemplate(int template) {
        this.mTemplate = template;
    }

    public int getIconId() {
        return this.mIconId;
    }

    public void setIconId(int iconId) {
        this.mIconId = iconId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getSubtitle() {
        return this.mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        this.mSubtitle = subtitle;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public long getDate() {
        return this.mDate;
    }

    public void setDate(long date) {
        this.mDate = date;
    }

    public int getGuideDistance() {
        return this.mGuideDistance;
    }

    public void setGuideDistance(int guideDistance) {
        this.mGuideDistance = guideDistance;
    }

    public String getGuideDistanceUnit() {
        return this.mGuideDistanceUnit;
    }

    public void setGuideDistanceUnit(String guideDistanceUnit) {
        this.mGuideDistanceUnit = guideDistanceUnit;
    }

    public int getGuideDirectionId() {
        return this.mGuideDirectionId;
    }

    public void setGuideDirectionId(int guideDirectionId) {
        this.mGuideDirectionId = guideDirectionId;
    }

    public String getGuideText() {
        return this.mGuideText;
    }

    public void setGuideText(String guideText) {
        this.mGuideText = guideText;
    }

    public int getVibrate() {
        return this.mVibrate;
    }

    public void setVibrate(int vibrate) {
        this.mVibrate = vibrate;
    }

    public String getNotificationProperty(int messsageTemplate) {
        Map<String, Object> map = this.mPropertyMap;
        if (map == null || messsageTemplate < 0) {
            return BuildConfig.FLAVOR;
        }
        Object templateObject = map.get(KEY_TEMPLATE);
        if (templateObject instanceof Integer) {
            try {
                int template = Integer.parseInt(templateObject.toString());
                if (messsageTemplate == template) {
                    return this.mPropertyMap.toString();
                }
                Log.w(TAG, "messsageTemplate is " + template);
                return BuildConfig.FLAVOR;
            } catch (NumberFormatException e) {
                Log.w(TAG, "templateObject numberFormatException.");
            }
        }
        return BuildConfig.FLAVOR;
    }

    public void setNotificationProperty(int messsageTemplate, Map<String, Object> notificationProperty) {
        Map<String, Object> map = this.mPropertyMap;
        if (map == null || notificationProperty == null || messsageTemplate < 0) {
            Log.w(TAG, "notificaitonProperty is null or template is small than 0.");
            return;
        }
        map.clear();
        Object object = notificationProperty.get(KEY_PACKAGE_NAME);
        if (object instanceof String) {
            try {
                setPackageName((String) object);
                this.mPropertyMap.put(KEY_PACKAGE_NAME, notificationProperty.get(KEY_PACKAGE_NAME));
                Object object2 = notificationProperty.get(KEY_DATE);
                if (object2 instanceof Long) {
                    try {
                        setDate(Long.parseLong(object2.toString()));
                        this.mPropertyMap.put(KEY_DATE, notificationProperty.get(KEY_DATE));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "dateObject numberFormatException.");
                        return;
                    }
                }
                Object object3 = notificationProperty.get(KEY_VIBRATE);
                if (object3 instanceof Integer) {
                    try {
                        setVibrate(Integer.parseInt(object3.toString()));
                        this.mPropertyMap.put(KEY_VIBRATE, notificationProperty.get(KEY_VIBRATE));
                    } catch (NumberFormatException e2) {
                        Log.w(TAG, "dateObject numberFormatException.");
                        return;
                    }
                }
                if (messsageTemplate == 1) {
                    setTemplateOne(notificationProperty);
                } else if (messsageTemplate == 2) {
                    setTemplateTwo(notificationProperty);
                } else {
                    Log.w(TAG, "other messsageTemplate has not define.");
                    this.mPropertyMap.clear();
                }
            } catch (NumberFormatException e3) {
                Log.w(TAG, "templateObject numberFormatException.");
            }
        } else {
            Log.w(TAG, "setNotificationProperty package name is not String.");
        }
    }

    private void setTemplateOne(Map<String, Object> notificaitonProperty) {
        this.mPropertyMap.put(KEY_TEMPLATE, 1);
        setTemplate(1);
        if (notificaitonProperty != null) {
            Object object = notificaitonProperty.get(KEY_ICON_INDEX);
            if (object instanceof Integer) {
                try {
                    setIconId(Integer.parseInt(object.toString()));
                    this.mPropertyMap.put(KEY_ICON_INDEX, notificaitonProperty.get(KEY_ICON_INDEX));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "setIconId numberFormatException.");
                    return;
                }
            }
            Object object2 = notificaitonProperty.get(KEY_TITLE);
            if (object2 instanceof String) {
                try {
                    setTitle((String) object2);
                    this.mPropertyMap.put(KEY_TITLE, notificaitonProperty.get(KEY_TITLE));
                } catch (NumberFormatException e2) {
                    Log.w(TAG, "setTitle numberFormatException.");
                    return;
                }
            }
            Object object3 = notificaitonProperty.get(KEY_SUBTITLE);
            if (object3 instanceof String) {
                try {
                    setSubtitle((String) object3);
                    this.mPropertyMap.put(KEY_SUBTITLE, notificaitonProperty.get(KEY_SUBTITLE));
                } catch (NumberFormatException e3) {
                    Log.w(TAG, "setSubtitle numberFormatException.");
                    return;
                }
            }
            Object object4 = notificaitonProperty.get(KEY_CONTENT);
            if (object4 instanceof String) {
                try {
                    setContent((String) object4);
                    this.mPropertyMap.put(KEY_CONTENT, notificaitonProperty.get(KEY_CONTENT));
                } catch (NumberFormatException e4) {
                    Log.w(TAG, "setContent numberFormatException.");
                }
            }
        }
    }

    private void setTemplateTwo(Map<String, Object> notificaitonProperty) {
        this.mPropertyMap.put(KEY_TEMPLATE, 2);
        setTemplate(2);
        if (notificaitonProperty != null) {
            Object object = notificaitonProperty.get(KEY_GUIDE_DISTANCE);
            if (object instanceof Integer) {
                try {
                    setGuideDistance(Integer.parseInt(object.toString()));
                    this.mPropertyMap.put(KEY_GUIDE_DISTANCE, notificaitonProperty.get(KEY_GUIDE_DISTANCE));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "dateObject numberFormatException.");
                    return;
                }
            }
            Object object2 = notificaitonProperty.get(KEY_DISTANCE_UNIT);
            if (object2 instanceof String) {
                try {
                    setGuideDistanceUnit((String) object2);
                    this.mPropertyMap.put(KEY_DISTANCE_UNIT, notificaitonProperty.get(KEY_DISTANCE_UNIT));
                } catch (NumberFormatException e2) {
                    Log.w(TAG, "setContent numberFormatException.");
                    return;
                }
            }
            Object object3 = notificaitonProperty.get(KEY_DIRECTION_INDEX);
            if (object3 instanceof Integer) {
                try {
                    setGuideDirectionId(Integer.parseInt(object3.toString()));
                    this.mPropertyMap.put(KEY_DIRECTION_INDEX, notificaitonProperty.get(KEY_DIRECTION_INDEX));
                } catch (NumberFormatException e3) {
                    Log.w(TAG, "dateObject numberFormatException.");
                    return;
                }
            }
            Object object4 = notificaitonProperty.get(KEY_GUIDE_TEXT);
            if (object4 instanceof String) {
                try {
                    setGuideText((String) object4);
                    this.mPropertyMap.put(KEY_GUIDE_TEXT, notificaitonProperty.get(KEY_GUIDE_TEXT));
                } catch (NumberFormatException e4) {
                    Log.w(TAG, "setContent numberFormatException.");
                }
            }
        }
    }
}

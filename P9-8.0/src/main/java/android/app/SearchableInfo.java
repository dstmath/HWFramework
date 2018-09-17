package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class SearchableInfo implements Parcelable {
    public static final Creator<SearchableInfo> CREATOR = new Creator<SearchableInfo>() {
        public SearchableInfo createFromParcel(Parcel in) {
            return new SearchableInfo(in);
        }

        public SearchableInfo[] newArray(int size) {
            return new SearchableInfo[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "SearchableInfo";
    private static final String MD_LABEL_ONLINESEARCHABLE = "android.app.online_searchable";
    private static final String MD_LABEL_SEARCHABLE = "android.app.searchable";
    private static final String MD_XML_ELEMENT_SEARCHABLE = "searchable";
    private static final String MD_XML_ELEMENT_SEARCHABLE_ACTION_KEY = "actionkey";
    private static final String MD_XML_ELEMENT_SEARCHABLE_FULLTEXTSEARCHFIELDS = "fullTextSearchFields";
    private static final String MD_XML_ELEMENT_SEARCHABLE_LAYOUTTYPE = "layoutType";
    private static final String MD_XML_ELEMENT_SEARCHABLE_MORELABEL = "moreLabel";
    private static final String MD_XML_ELEMENT_SEARCHABLE_SEARCHSUGGESTOTHERSINTENT = "searchSuggestOthersIntent";
    private static final String MD_XML_ELEMENT_SEARCHABLE_USEFULLTEXTSEARCH = "useFullTextSearch";
    private static final int SEARCH_MODE_BADGE_ICON = 8;
    private static final int SEARCH_MODE_BADGE_LABEL = 4;
    private static final int SEARCH_MODE_QUERY_REWRITE_FROM_DATA = 16;
    private static final int SEARCH_MODE_QUERY_REWRITE_FROM_TEXT = 32;
    private static final int VOICE_SEARCH_LAUNCH_RECOGNIZER = 4;
    private static final int VOICE_SEARCH_LAUNCH_WEB_SEARCH = 2;
    private static final int VOICE_SEARCH_SHOW_BUTTON = 1;
    private HashMap<Integer, ActionKeyInfo> mActionKeys = null;
    private final boolean mAutoUrlDetect;
    private String mFullTextSearchFields;
    private final int mHintId;
    private final int mIconId;
    private final boolean mIncludeInGlobalSearch;
    private final int mLabelId;
    private String mLayoutType;
    private String mMoreLabel;
    private final boolean mQueryAfterZeroResults;
    private final ComponentName mSearchActivity;
    private final int mSearchButtonText;
    private final int mSearchImeOptions;
    private final int mSearchInputType;
    private final int mSearchMode;
    private String mSearchSuggestOthersIntent;
    private final int mSettingsDescriptionId;
    private final String mSuggestAuthority;
    private final String mSuggestIntentAction;
    private final String mSuggestIntentData;
    private final String mSuggestPath;
    private final String mSuggestProviderPackage;
    private final String mSuggestSelection;
    private final int mSuggestThreshold;
    private boolean mUseFullTextSearch;
    private final int mVoiceLanguageId;
    private final int mVoiceLanguageModeId;
    private final int mVoiceMaxResults;
    private final int mVoicePromptTextId;
    private final int mVoiceSearchMode;

    public static class ActionKeyInfo implements Parcelable {
        private final int mKeyCode;
        private final String mQueryActionMsg;
        private final String mSuggestActionMsg;
        private final String mSuggestActionMsgColumn;

        ActionKeyInfo(Context activityContext, AttributeSet attr) {
            TypedArray a = activityContext.obtainStyledAttributes(attr, R.styleable.SearchableActionKey);
            this.mKeyCode = a.getInt(0, 0);
            this.mQueryActionMsg = a.getString(1);
            this.mSuggestActionMsg = a.getString(2);
            this.mSuggestActionMsgColumn = a.getString(3);
            a.recycle();
            if (this.mKeyCode == 0) {
                throw new IllegalArgumentException("No keycode.");
            } else if (this.mQueryActionMsg == null && this.mSuggestActionMsg == null && this.mSuggestActionMsgColumn == null) {
                throw new IllegalArgumentException("No message information.");
            }
        }

        private ActionKeyInfo(Parcel in) {
            this.mKeyCode = in.readInt();
            this.mQueryActionMsg = in.readString();
            this.mSuggestActionMsg = in.readString();
            this.mSuggestActionMsgColumn = in.readString();
        }

        public int getKeyCode() {
            return this.mKeyCode;
        }

        public String getQueryActionMsg() {
            return this.mQueryActionMsg;
        }

        public String getSuggestActionMsg() {
            return this.mSuggestActionMsg;
        }

        public String getSuggestActionMsgColumn() {
            return this.mSuggestActionMsgColumn;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mKeyCode);
            dest.writeString(this.mQueryActionMsg);
            dest.writeString(this.mSuggestActionMsg);
            dest.writeString(this.mSuggestActionMsgColumn);
        }
    }

    public String getSuggestAuthority() {
        return this.mSuggestAuthority;
    }

    public String getSuggestPackage() {
        return this.mSuggestProviderPackage;
    }

    public ComponentName getSearchActivity() {
        return this.mSearchActivity;
    }

    public boolean useBadgeLabel() {
        return (this.mSearchMode & 4) != 0;
    }

    public boolean useBadgeIcon() {
        return ((this.mSearchMode & 8) == 0 || this.mIconId == 0) ? false : true;
    }

    public boolean shouldRewriteQueryFromData() {
        return (this.mSearchMode & 16) != 0;
    }

    public boolean shouldRewriteQueryFromText() {
        return (this.mSearchMode & 32) != 0;
    }

    public int getSettingsDescriptionId() {
        return this.mSettingsDescriptionId;
    }

    public String getSuggestPath() {
        return this.mSuggestPath;
    }

    public String getSuggestSelection() {
        return this.mSuggestSelection;
    }

    public String getSuggestIntentAction() {
        return this.mSuggestIntentAction;
    }

    public String getSuggestIntentData() {
        return this.mSuggestIntentData;
    }

    public int getSuggestThreshold() {
        return this.mSuggestThreshold;
    }

    public Context getActivityContext(Context context) {
        return createActivityContext(context, this.mSearchActivity);
    }

    private static Context createActivityContext(Context context, ComponentName activity) {
        Context theirContext = null;
        try {
            return context.createPackageContext(activity.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Package not found " + activity.getPackageName());
            return theirContext;
        } catch (SecurityException e2) {
            Log.e(LOG_TAG, "Can't make context for " + activity.getPackageName(), e2);
            return theirContext;
        }
    }

    public Context getProviderContext(Context context, Context activityContext) {
        Context theirContext = null;
        if (this.mSearchActivity.getPackageName().equals(this.mSuggestProviderPackage)) {
            return activityContext;
        }
        if (this.mSuggestProviderPackage != null) {
            try {
                theirContext = context.createPackageContext(this.mSuggestProviderPackage, 0);
            } catch (NameNotFoundException e) {
            } catch (SecurityException e2) {
            }
        }
        return theirContext;
    }

    public String getMoreLabel() {
        return this.mMoreLabel;
    }

    public String getLayoutType() {
        return this.mLayoutType;
    }

    public String getSearchSuggestOthersIntent() {
        return this.mSearchSuggestOthersIntent;
    }

    public boolean getUseFullTextSearch() {
        return this.mUseFullTextSearch;
    }

    public String getFullTextSearchFields() {
        return this.mFullTextSearchFields;
    }

    private SearchableInfo(Context activityContext, AttributeSet attr, ComponentName cName) {
        this.mSearchActivity = cName;
        TypedArray a = activityContext.obtainStyledAttributes(attr, R.styleable.Searchable);
        this.mSearchMode = a.getInt(3, 0);
        this.mLabelId = a.getResourceId(0, 0);
        this.mHintId = a.getResourceId(2, 0);
        this.mIconId = a.getResourceId(1, 0);
        this.mSearchButtonText = a.getResourceId(9, 0);
        this.mSearchInputType = a.getInt(10, 1);
        this.mSearchImeOptions = a.getInt(16, 2);
        this.mIncludeInGlobalSearch = a.getBoolean(18, false);
        this.mQueryAfterZeroResults = a.getBoolean(19, false);
        this.mAutoUrlDetect = a.getBoolean(21, false);
        this.mSettingsDescriptionId = a.getResourceId(20, 0);
        this.mSuggestAuthority = a.getString(4);
        this.mSuggestPath = a.getString(5);
        this.mSuggestSelection = a.getString(6);
        this.mSuggestIntentAction = a.getString(7);
        this.mSuggestIntentData = a.getString(8);
        this.mSuggestThreshold = a.getInt(17, 0);
        this.mVoiceSearchMode = a.getInt(11, 0);
        this.mVoiceLanguageModeId = a.getResourceId(12, 0);
        this.mVoicePromptTextId = a.getResourceId(13, 0);
        this.mVoiceLanguageId = a.getResourceId(14, 0);
        this.mVoiceMaxResults = a.getInt(15, 0);
        a.recycle();
        String suggestProviderPackage = null;
        if (this.mSuggestAuthority != null) {
            ProviderInfo pi = activityContext.getPackageManager().resolveContentProvider(this.mSuggestAuthority, 268435456);
            if (pi != null) {
                suggestProviderPackage = pi.packageName;
            }
        }
        this.mSuggestProviderPackage = suggestProviderPackage;
        if (this.mLabelId == 0) {
            throw new IllegalArgumentException("Search label must be a resource reference.");
        }
    }

    public ActionKeyInfo findActionKey(int keyCode) {
        if (this.mActionKeys == null) {
            return null;
        }
        return (ActionKeyInfo) this.mActionKeys.get(Integer.valueOf(keyCode));
    }

    private void addActionKey(ActionKeyInfo keyInfo) {
        if (this.mActionKeys == null) {
            this.mActionKeys = new HashMap();
        }
        this.mActionKeys.put(Integer.valueOf(keyInfo.getKeyCode()), keyInfo);
    }

    public static SearchableInfo getActivityMetaDataOnline(Context context, ActivityInfo activityInfo, int userId) {
        try {
            Context userContext = context.createPackageContextAsUser("system", 0, new UserHandle(userId));
            XmlPullParser xml = activityInfo.loadXmlMetaData(userContext.getPackageManager(), MD_LABEL_ONLINESEARCHABLE);
            if (xml == null) {
                return null;
            }
            SearchableInfo searchable = getActivityMetaData(userContext, xml, new ComponentName(activityInfo.packageName, activityInfo.name));
            xml.close();
            return searchable;
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Couldn't create package context for user " + userId);
            return null;
        }
    }

    public static SearchableInfo getActivityMetaData(Context context, ActivityInfo activityInfo, int userId) {
        try {
            Context userContext = context.createPackageContextAsUser("system", 0, new UserHandle(userId));
            XmlPullParser xml = activityInfo.loadXmlMetaData(userContext.getPackageManager(), MD_LABEL_SEARCHABLE);
            if (xml == null) {
                return null;
            }
            SearchableInfo searchable = getActivityMetaData(userContext, xml, new ComponentName(activityInfo.packageName, activityInfo.name));
            xml.close();
            return searchable;
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Couldn't create package context for user " + userId);
            return null;
        }
    }

    private static SearchableInfo getActivityMetaData(Context context, XmlPullParser xml, ComponentName cName) {
        XmlPullParserException e;
        IOException e2;
        SearchableInfo result = null;
        Context activityContext = createActivityContext(context, cName);
        if (activityContext == null) {
            return null;
        }
        try {
            int tagType = xml.next();
            while (true) {
                SearchableInfo result2 = result;
                if (tagType == 1) {
                    return result2;
                }
                if (tagType == 2) {
                    try {
                        AttributeSet attr;
                        if (xml.getName().equals("searchable")) {
                            attr = Xml.asAttributeSet(xml);
                            if (attr != null) {
                                try {
                                    result = new SearchableInfo(activityContext, attr, cName);
                                    tagType = xml.next();
                                } catch (IllegalArgumentException ex) {
                                    Log.w(LOG_TAG, "Invalid searchable metadata for " + cName.flattenToShortString() + ": " + ex.getMessage());
                                    return null;
                                }
                            }
                        } else if (!xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_ACTION_KEY)) {
                            if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_LAYOUTTYPE)) {
                                if (result2 == null) {
                                    return null;
                                }
                                result2.mLayoutType = xml.nextText();
                                result = result2;
                            } else if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_SEARCHSUGGESTOTHERSINTENT)) {
                                if (result2 == null) {
                                    return null;
                                }
                                result2.mSearchSuggestOthersIntent = xml.nextText();
                                result = result2;
                            } else if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_USEFULLTEXTSEARCH)) {
                                if (result2 == null) {
                                    return null;
                                }
                                result2.mUseFullTextSearch = "true".equalsIgnoreCase(xml.nextText());
                                result = result2;
                            } else if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_FULLTEXTSEARCHFIELDS)) {
                                if (result2 == null) {
                                    return null;
                                }
                                result2.mFullTextSearchFields = xml.nextText();
                                result = result2;
                            } else if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_MORELABEL)) {
                                if (result2 == null) {
                                    return null;
                                }
                                result2.mMoreLabel = xml.nextText();
                                result = result2;
                            }
                            tagType = xml.next();
                        } else if (result2 == null) {
                            return null;
                        } else {
                            attr = Xml.asAttributeSet(xml);
                            if (attr != null) {
                                try {
                                    result2.addActionKey(new ActionKeyInfo(activityContext, attr));
                                } catch (IllegalArgumentException ex2) {
                                    Log.w(LOG_TAG, "Invalid action key for " + cName.flattenToShortString() + ": " + ex2.getMessage());
                                    return null;
                                }
                            }
                        }
                    } catch (XmlPullParserException e3) {
                        e = e3;
                        result = result2;
                        Log.w(LOG_TAG, "Reading searchable metadata for " + cName.flattenToShortString(), e);
                        return null;
                    } catch (IOException e4) {
                        e2 = e4;
                        result = result2;
                        Log.w(LOG_TAG, "Reading searchable metadata for " + cName.flattenToShortString(), e2);
                        return null;
                    }
                }
                result = result2;
                tagType = xml.next();
            }
        } catch (XmlPullParserException e5) {
            e = e5;
        } catch (IOException e6) {
            e2 = e6;
        }
    }

    public int getLabelId() {
        return this.mLabelId;
    }

    public int getHintId() {
        return this.mHintId;
    }

    public int getIconId() {
        return this.mIconId;
    }

    public boolean getVoiceSearchEnabled() {
        return (this.mVoiceSearchMode & 1) != 0;
    }

    public boolean getVoiceSearchLaunchWebSearch() {
        return (this.mVoiceSearchMode & 2) != 0;
    }

    public boolean getVoiceSearchLaunchRecognizer() {
        return (this.mVoiceSearchMode & 4) != 0;
    }

    public int getVoiceLanguageModeId() {
        return this.mVoiceLanguageModeId;
    }

    public int getVoicePromptTextId() {
        return this.mVoicePromptTextId;
    }

    public int getVoiceLanguageId() {
        return this.mVoiceLanguageId;
    }

    public int getVoiceMaxResults() {
        return this.mVoiceMaxResults;
    }

    public int getSearchButtonText() {
        return this.mSearchButtonText;
    }

    public int getInputType() {
        return this.mSearchInputType;
    }

    public int getImeOptions() {
        return this.mSearchImeOptions;
    }

    public boolean shouldIncludeInGlobalSearch() {
        return this.mIncludeInGlobalSearch;
    }

    public boolean queryAfterZeroResults() {
        return this.mQueryAfterZeroResults;
    }

    public boolean autoUrlDetect() {
        return this.mAutoUrlDetect;
    }

    SearchableInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mLabelId = in.readInt();
        this.mSearchActivity = ComponentName.readFromParcel(in);
        this.mHintId = in.readInt();
        this.mSearchMode = in.readInt();
        this.mIconId = in.readInt();
        this.mSearchButtonText = in.readInt();
        this.mSearchInputType = in.readInt();
        this.mSearchImeOptions = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIncludeInGlobalSearch = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mQueryAfterZeroResults = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mAutoUrlDetect = z;
        this.mSettingsDescriptionId = in.readInt();
        this.mSuggestAuthority = in.readString();
        this.mSuggestPath = in.readString();
        this.mSuggestSelection = in.readString();
        this.mSuggestIntentAction = in.readString();
        this.mSuggestIntentData = in.readString();
        this.mSuggestThreshold = in.readInt();
        for (int count = in.readInt(); count > 0; count--) {
            addActionKey(new ActionKeyInfo(in, null));
        }
        this.mSuggestProviderPackage = in.readString();
        this.mVoiceSearchMode = in.readInt();
        this.mVoiceLanguageModeId = in.readInt();
        this.mVoicePromptTextId = in.readInt();
        this.mVoiceLanguageId = in.readInt();
        this.mVoiceMaxResults = in.readInt();
        this.mLayoutType = in.readString();
        this.mSearchSuggestOthersIntent = in.readString();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mUseFullTextSearch = z2;
        this.mFullTextSearchFields = in.readString();
        this.mMoreLabel = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mLabelId);
        this.mSearchActivity.writeToParcel(dest, flags);
        dest.writeInt(this.mHintId);
        dest.writeInt(this.mSearchMode);
        dest.writeInt(this.mIconId);
        dest.writeInt(this.mSearchButtonText);
        dest.writeInt(this.mSearchInputType);
        dest.writeInt(this.mSearchImeOptions);
        dest.writeInt(this.mIncludeInGlobalSearch ? 1 : 0);
        if (this.mQueryAfterZeroResults) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mAutoUrlDetect) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mSettingsDescriptionId);
        dest.writeString(this.mSuggestAuthority);
        dest.writeString(this.mSuggestPath);
        dest.writeString(this.mSuggestSelection);
        dest.writeString(this.mSuggestIntentAction);
        dest.writeString(this.mSuggestIntentData);
        dest.writeInt(this.mSuggestThreshold);
        if (this.mActionKeys == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(this.mActionKeys.size());
            for (ActionKeyInfo actionKey : this.mActionKeys.values()) {
                actionKey.writeToParcel(dest, flags);
            }
        }
        dest.writeString(this.mSuggestProviderPackage);
        dest.writeInt(this.mVoiceSearchMode);
        dest.writeInt(this.mVoiceLanguageModeId);
        dest.writeInt(this.mVoicePromptTextId);
        dest.writeInt(this.mVoiceLanguageId);
        dest.writeInt(this.mVoiceMaxResults);
        dest.writeString(this.mLayoutType);
        dest.writeString(this.mSearchSuggestOthersIntent);
        if (!this.mUseFullTextSearch) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeString(this.mFullTextSearchFields);
        dest.writeString(this.mMoreLabel);
    }
}

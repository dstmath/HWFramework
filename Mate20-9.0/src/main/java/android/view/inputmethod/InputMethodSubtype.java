package android.view.inputmethod;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.icu.text.DisplayContext;
import android.icu.text.LocaleDisplayNames;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.inputmethod.InputMethodUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class InputMethodSubtype implements Parcelable {
    public static final Parcelable.Creator<InputMethodSubtype> CREATOR = new Parcelable.Creator<InputMethodSubtype>() {
        public InputMethodSubtype createFromParcel(Parcel source) {
            return new InputMethodSubtype(source);
        }

        public InputMethodSubtype[] newArray(int size) {
            return new InputMethodSubtype[size];
        }
    };
    private static final String EXTRA_KEY_UNTRANSLATABLE_STRING_IN_SUBTYPE_NAME = "UntranslatableReplacementStringInSubtypeName";
    private static final String EXTRA_VALUE_KEY_VALUE_SEPARATOR = "=";
    private static final String EXTRA_VALUE_PAIR_SEPARATOR = ",";
    private static final String LANGUAGE_TAG_NONE = "";
    private static final int SUBTYPE_ID_NONE = 0;
    private static final String TAG = InputMethodSubtype.class.getSimpleName();
    private volatile Locale mCachedLocaleObj;
    private volatile HashMap<String, String> mExtraValueHashMapCache;
    private final boolean mIsAsciiCapable;
    private final boolean mIsAuxiliary;
    private final Object mLock;
    private final boolean mOverridesImplicitlyEnabledSubtype;
    private final String mSubtypeExtraValue;
    private final int mSubtypeHashCode;
    private final int mSubtypeIconResId;
    private final int mSubtypeId;
    private final String mSubtypeLanguageTag;
    private final String mSubtypeLocale;
    private final String mSubtypeMode;
    private final int mSubtypeNameResId;

    public static class InputMethodSubtypeBuilder {
        /* access modifiers changed from: private */
        public boolean mIsAsciiCapable = false;
        /* access modifiers changed from: private */
        public boolean mIsAuxiliary = false;
        /* access modifiers changed from: private */
        public boolean mOverridesImplicitlyEnabledSubtype = false;
        /* access modifiers changed from: private */
        public String mSubtypeExtraValue = "";
        /* access modifiers changed from: private */
        public int mSubtypeIconResId = 0;
        /* access modifiers changed from: private */
        public int mSubtypeId = 0;
        /* access modifiers changed from: private */
        public String mSubtypeLanguageTag = "";
        /* access modifiers changed from: private */
        public String mSubtypeLocale = "";
        /* access modifiers changed from: private */
        public String mSubtypeMode = "";
        /* access modifiers changed from: private */
        public int mSubtypeNameResId = 0;

        public InputMethodSubtypeBuilder setIsAuxiliary(boolean isAuxiliary) {
            this.mIsAuxiliary = isAuxiliary;
            return this;
        }

        public InputMethodSubtypeBuilder setOverridesImplicitlyEnabledSubtype(boolean overridesImplicitlyEnabledSubtype) {
            this.mOverridesImplicitlyEnabledSubtype = overridesImplicitlyEnabledSubtype;
            return this;
        }

        public InputMethodSubtypeBuilder setIsAsciiCapable(boolean isAsciiCapable) {
            this.mIsAsciiCapable = isAsciiCapable;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeIconResId(int subtypeIconResId) {
            this.mSubtypeIconResId = subtypeIconResId;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeNameResId(int subtypeNameResId) {
            this.mSubtypeNameResId = subtypeNameResId;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeId(int subtypeId) {
            this.mSubtypeId = subtypeId;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeLocale(String subtypeLocale) {
            this.mSubtypeLocale = subtypeLocale == null ? "" : subtypeLocale;
            return this;
        }

        public InputMethodSubtypeBuilder setLanguageTag(String languageTag) {
            this.mSubtypeLanguageTag = languageTag == null ? "" : languageTag;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeMode(String subtypeMode) {
            this.mSubtypeMode = subtypeMode == null ? "" : subtypeMode;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeExtraValue(String subtypeExtraValue) {
            this.mSubtypeExtraValue = subtypeExtraValue == null ? "" : subtypeExtraValue;
            return this;
        }

        public InputMethodSubtype build() {
            return new InputMethodSubtype(this);
        }
    }

    private static InputMethodSubtypeBuilder getBuilder(int nameId, int iconId, String locale, String mode, String extraValue, boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype, int id, boolean isAsciiCapable) {
        InputMethodSubtypeBuilder builder = new InputMethodSubtypeBuilder();
        int unused = builder.mSubtypeNameResId = nameId;
        int unused2 = builder.mSubtypeIconResId = iconId;
        String unused3 = builder.mSubtypeLocale = locale;
        String unused4 = builder.mSubtypeMode = mode;
        String unused5 = builder.mSubtypeExtraValue = extraValue;
        boolean unused6 = builder.mIsAuxiliary = isAuxiliary;
        boolean unused7 = builder.mOverridesImplicitlyEnabledSubtype = overridesImplicitlyEnabledSubtype;
        int unused8 = builder.mSubtypeId = id;
        boolean unused9 = builder.mIsAsciiCapable = isAsciiCapable;
        return builder;
    }

    @Deprecated
    public InputMethodSubtype(int nameId, int iconId, String locale, String mode, String extraValue, boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype) {
        this(nameId, iconId, locale, mode, extraValue, isAuxiliary, overridesImplicitlyEnabledSubtype, 0);
    }

    @Deprecated
    public InputMethodSubtype(int nameId, int iconId, String locale, String mode, String extraValue, boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype, int id) {
        this(getBuilder(nameId, iconId, locale, mode, extraValue, isAuxiliary, overridesImplicitlyEnabledSubtype, id, false));
    }

    private InputMethodSubtype(InputMethodSubtypeBuilder builder) {
        this.mLock = new Object();
        this.mSubtypeNameResId = builder.mSubtypeNameResId;
        this.mSubtypeIconResId = builder.mSubtypeIconResId;
        this.mSubtypeLocale = builder.mSubtypeLocale;
        this.mSubtypeLanguageTag = builder.mSubtypeLanguageTag;
        this.mSubtypeMode = builder.mSubtypeMode;
        this.mSubtypeExtraValue = builder.mSubtypeExtraValue;
        this.mIsAuxiliary = builder.mIsAuxiliary;
        this.mOverridesImplicitlyEnabledSubtype = builder.mOverridesImplicitlyEnabledSubtype;
        this.mSubtypeId = builder.mSubtypeId;
        this.mIsAsciiCapable = builder.mIsAsciiCapable;
        if (this.mSubtypeId != 0) {
            this.mSubtypeHashCode = this.mSubtypeId;
        } else {
            this.mSubtypeHashCode = hashCodeInternal(this.mSubtypeLocale, this.mSubtypeMode, this.mSubtypeExtraValue, this.mIsAuxiliary, this.mOverridesImplicitlyEnabledSubtype, this.mIsAsciiCapable);
        }
    }

    InputMethodSubtype(Parcel source) {
        this.mLock = new Object();
        this.mSubtypeNameResId = source.readInt();
        this.mSubtypeIconResId = source.readInt();
        String s = source.readString();
        this.mSubtypeLocale = s != null ? s : "";
        String s2 = source.readString();
        this.mSubtypeLanguageTag = s2 != null ? s2 : "";
        String s3 = source.readString();
        this.mSubtypeMode = s3 != null ? s3 : "";
        String s4 = source.readString();
        this.mSubtypeExtraValue = s4 != null ? s4 : "";
        boolean z = false;
        this.mIsAuxiliary = source.readInt() == 1;
        this.mOverridesImplicitlyEnabledSubtype = source.readInt() == 1;
        this.mSubtypeHashCode = source.readInt();
        this.mSubtypeId = source.readInt();
        this.mIsAsciiCapable = source.readInt() == 1 ? true : z;
    }

    public int getNameResId() {
        return this.mSubtypeNameResId;
    }

    public int getIconResId() {
        return this.mSubtypeIconResId;
    }

    @Deprecated
    public String getLocale() {
        return this.mSubtypeLocale;
    }

    public String getLanguageTag() {
        return this.mSubtypeLanguageTag;
    }

    public Locale getLocaleObject() {
        if (this.mCachedLocaleObj != null) {
            return this.mCachedLocaleObj;
        }
        synchronized (this.mLock) {
            if (this.mCachedLocaleObj != null) {
                Locale locale = this.mCachedLocaleObj;
                return locale;
            }
            if (!TextUtils.isEmpty(this.mSubtypeLanguageTag)) {
                this.mCachedLocaleObj = Locale.forLanguageTag(this.mSubtypeLanguageTag);
            } else {
                this.mCachedLocaleObj = InputMethodUtils.constructLocaleFromString(this.mSubtypeLocale);
            }
            Locale locale2 = this.mCachedLocaleObj;
            return locale2;
        }
    }

    public String getMode() {
        return this.mSubtypeMode;
    }

    public String getExtraValue() {
        return this.mSubtypeExtraValue;
    }

    public boolean isAuxiliary() {
        return this.mIsAuxiliary;
    }

    public boolean overridesImplicitlyEnabledSubtype() {
        return this.mOverridesImplicitlyEnabledSubtype;
    }

    public boolean isAsciiCapable() {
        return this.mIsAsciiCapable;
    }

    public CharSequence getDisplayName(Context context, String packageName, ApplicationInfo appInfo) {
        String replacementString;
        DisplayContext displayContext;
        if (this.mSubtypeNameResId == 0) {
            return getLocaleDisplayName(getLocaleFromContext(context), getLocaleObject(), DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU);
        }
        CharSequence subtypeName = context.getPackageManager().getText(packageName, this.mSubtypeNameResId, appInfo);
        if (TextUtils.isEmpty(subtypeName)) {
            return "";
        }
        String subtypeNameString = subtypeName.toString();
        if (containsExtraValueKey(EXTRA_KEY_UNTRANSLATABLE_STRING_IN_SUBTYPE_NAME)) {
            replacementString = getExtraValueOf(EXTRA_KEY_UNTRANSLATABLE_STRING_IN_SUBTYPE_NAME);
        } else {
            if (TextUtils.equals(subtypeNameString, "%s")) {
                displayContext = DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU;
            } else if (subtypeNameString.startsWith("%s")) {
                displayContext = DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE;
            } else {
                displayContext = DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE;
            }
            replacementString = getLocaleDisplayName(getLocaleFromContext(context), getLocaleObject(), displayContext);
        }
        if (replacementString == null) {
            replacementString = "";
        }
        try {
            if ("FYROM".equals(subtypeNameString)) {
                return replacementString;
            }
            return String.format(subtypeNameString, new Object[]{replacementString});
        } catch (IllegalFormatException e) {
            String str = TAG;
            Slog.w(str, "Found illegal format in subtype name(" + subtypeName + "): " + e);
            return "";
        }
    }

    private static Locale getLocaleFromContext(Context context) {
        if (context == null || context.getResources() == null) {
            return null;
        }
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration == null) {
            return null;
        }
        return configuration.getLocales().get(0);
    }

    private static String getLocaleDisplayName(Locale displayLocale, Locale localeToDisplay, DisplayContext displayContext) {
        if (localeToDisplay == null) {
            return "";
        }
        return LocaleDisplayNames.getInstance(displayLocale != null ? displayLocale : Locale.getDefault(), new DisplayContext[]{displayContext}).localeDisplayName(localeToDisplay);
    }

    private HashMap<String, String> getExtraValueHashMap() {
        synchronized (this) {
            HashMap<String, String> extraValueMap = this.mExtraValueHashMapCache;
            if (extraValueMap != null) {
                return extraValueMap;
            }
            HashMap<String, String> extraValueMap2 = new HashMap<>();
            String[] pairs = this.mSubtypeExtraValue.split(EXTRA_VALUE_PAIR_SEPARATOR);
            for (String split : pairs) {
                String[] pair = split.split(EXTRA_VALUE_KEY_VALUE_SEPARATOR);
                if (pair.length == 1) {
                    extraValueMap2.put(pair[0], null);
                } else if (pair.length > 1) {
                    if (pair.length > 2) {
                        Slog.w(TAG, "ExtraValue has two or more '='s");
                    }
                    extraValueMap2.put(pair[0], pair[1]);
                }
            }
            this.mExtraValueHashMapCache = extraValueMap2;
            return extraValueMap2;
        }
    }

    public boolean containsExtraValueKey(String key) {
        return getExtraValueHashMap().containsKey(key);
    }

    public String getExtraValueOf(String key) {
        return getExtraValueHashMap().get(key);
    }

    public int hashCode() {
        return this.mSubtypeHashCode;
    }

    public final boolean hasSubtypeId() {
        return this.mSubtypeId != 0;
    }

    public final int getSubtypeId() {
        return this.mSubtypeId;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof InputMethodSubtype)) {
            return false;
        }
        InputMethodSubtype subtype = (InputMethodSubtype) o;
        if (subtype.mSubtypeId == 0 && this.mSubtypeId == 0) {
            if (subtype.hashCode() == hashCode() && subtype.getLocale().equals(getLocale()) && subtype.getLanguageTag().equals(getLanguageTag()) && subtype.getMode().equals(getMode()) && subtype.getExtraValue().equals(getExtraValue()) && subtype.isAuxiliary() == isAuxiliary() && subtype.overridesImplicitlyEnabledSubtype() == overridesImplicitlyEnabledSubtype() && subtype.isAsciiCapable() == isAsciiCapable()) {
                z = true;
            }
            return z;
        }
        if (subtype.hashCode() == hashCode()) {
            z = true;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.mSubtypeNameResId);
        dest.writeInt(this.mSubtypeIconResId);
        dest.writeString(this.mSubtypeLocale);
        dest.writeString(this.mSubtypeLanguageTag);
        dest.writeString(this.mSubtypeMode);
        dest.writeString(this.mSubtypeExtraValue);
        dest.writeInt(this.mIsAuxiliary ? 1 : 0);
        dest.writeInt(this.mOverridesImplicitlyEnabledSubtype ? 1 : 0);
        dest.writeInt(this.mSubtypeHashCode);
        dest.writeInt(this.mSubtypeId);
        dest.writeInt(this.mIsAsciiCapable ? 1 : 0);
    }

    private static int hashCodeInternal(String locale, String mode, String extraValue, boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype, boolean isAsciiCapable) {
        if (!isAsciiCapable) {
            return Arrays.hashCode(new Object[]{locale, mode, extraValue, Boolean.valueOf(isAuxiliary), Boolean.valueOf(overridesImplicitlyEnabledSubtype)});
        }
        return Arrays.hashCode(new Object[]{locale, mode, extraValue, Boolean.valueOf(isAuxiliary), Boolean.valueOf(overridesImplicitlyEnabledSubtype), Boolean.valueOf(isAsciiCapable)});
    }

    public static List<InputMethodSubtype> sort(Context context, int flags, InputMethodInfo imi, List<InputMethodSubtype> subtypeList) {
        if (imi == null) {
            return subtypeList;
        }
        HashSet<InputMethodSubtype> inputSubtypesSet = new HashSet<>(subtypeList);
        ArrayList<InputMethodSubtype> sortedList = new ArrayList<>();
        int N = imi.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            InputMethodSubtype subtype = imi.getSubtypeAt(i);
            if (inputSubtypesSet.contains(subtype)) {
                sortedList.add(subtype);
                inputSubtypesSet.remove(subtype);
            }
        }
        Iterator<InputMethodSubtype> it = inputSubtypesSet.iterator();
        while (it.hasNext()) {
            sortedList.add(it.next());
        }
        return sortedList;
    }
}

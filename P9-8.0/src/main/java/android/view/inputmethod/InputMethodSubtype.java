package android.view.inputmethod;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.icu.text.DisplayContext;
import android.icu.text.LocaleDisplayNames;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.inputmethod.InputMethodUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;

public final class InputMethodSubtype implements Parcelable {
    public static final Creator<InputMethodSubtype> CREATOR = new Creator<InputMethodSubtype>() {
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
    private volatile HashMap<String, String> mExtraValueHashMapCache;
    private final boolean mIsAsciiCapable;
    private final boolean mIsAuxiliary;
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
        private boolean mIsAsciiCapable = false;
        private boolean mIsAuxiliary = false;
        private boolean mOverridesImplicitlyEnabledSubtype = false;
        private String mSubtypeExtraValue = "";
        private int mSubtypeIconResId = 0;
        private int mSubtypeId = 0;
        private String mSubtypeLanguageTag = "";
        private String mSubtypeLocale = "";
        private String mSubtypeMode = "";
        private int mSubtypeNameResId = 0;

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
            if (subtypeLocale == null) {
                subtypeLocale = "";
            }
            this.mSubtypeLocale = subtypeLocale;
            return this;
        }

        public InputMethodSubtypeBuilder setLanguageTag(String languageTag) {
            if (languageTag == null) {
                languageTag = "";
            }
            this.mSubtypeLanguageTag = languageTag;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeMode(String subtypeMode) {
            if (subtypeMode == null) {
                subtypeMode = "";
            }
            this.mSubtypeMode = subtypeMode;
            return this;
        }

        public InputMethodSubtypeBuilder setSubtypeExtraValue(String subtypeExtraValue) {
            if (subtypeExtraValue == null) {
                subtypeExtraValue = "";
            }
            this.mSubtypeExtraValue = subtypeExtraValue;
            return this;
        }

        public InputMethodSubtype build() {
            return new InputMethodSubtype(this, null);
        }
    }

    /* synthetic */ InputMethodSubtype(InputMethodSubtypeBuilder builder, InputMethodSubtype -this1) {
        this(builder);
    }

    private static InputMethodSubtypeBuilder getBuilder(int nameId, int iconId, String locale, String mode, String extraValue, boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype, int id, boolean isAsciiCapable) {
        InputMethodSubtypeBuilder builder = new InputMethodSubtypeBuilder();
        builder.mSubtypeNameResId = nameId;
        builder.mSubtypeIconResId = iconId;
        builder.mSubtypeLocale = locale;
        builder.mSubtypeMode = mode;
        builder.mSubtypeExtraValue = extraValue;
        builder.mIsAuxiliary = isAuxiliary;
        builder.mOverridesImplicitlyEnabledSubtype = overridesImplicitlyEnabledSubtype;
        builder.mSubtypeId = id;
        builder.mIsAsciiCapable = isAsciiCapable;
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
        boolean z;
        boolean z2 = true;
        this.mSubtypeNameResId = source.readInt();
        this.mSubtypeIconResId = source.readInt();
        String s = source.readString();
        if (s == null) {
            s = "";
        }
        this.mSubtypeLocale = s;
        s = source.readString();
        if (s == null) {
            s = "";
        }
        this.mSubtypeLanguageTag = s;
        s = source.readString();
        if (s == null) {
            s = "";
        }
        this.mSubtypeMode = s;
        s = source.readString();
        if (s == null) {
            s = "";
        }
        this.mSubtypeExtraValue = s;
        if (source.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIsAuxiliary = z;
        if (source.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mOverridesImplicitlyEnabledSubtype = z;
        this.mSubtypeHashCode = source.readInt();
        this.mSubtypeId = source.readInt();
        if (source.readInt() != 1) {
            z2 = false;
        }
        this.mIsAsciiCapable = z2;
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
        if (TextUtils.isEmpty(this.mSubtypeLanguageTag)) {
            return InputMethodUtils.constructLocaleFromString(this.mSubtypeLocale);
        }
        return Locale.forLanguageTag(this.mSubtypeLanguageTag);
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
        if (this.mSubtypeNameResId == 0) {
            return getLocaleDisplayName(getLocaleFromContext(context), getLocaleObject(), DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU);
        }
        CharSequence subtypeName = context.getPackageManager().getText(packageName, this.mSubtypeNameResId, appInfo);
        if (TextUtils.isEmpty(subtypeName)) {
            return "";
        }
        String replacementString;
        String subtypeNameString = subtypeName.toString();
        if (containsExtraValueKey(EXTRA_KEY_UNTRANSLATABLE_STRING_IN_SUBTYPE_NAME)) {
            replacementString = getExtraValueOf(EXTRA_KEY_UNTRANSLATABLE_STRING_IN_SUBTYPE_NAME);
        } else {
            DisplayContext displayContext;
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
            Slog.w(TAG, "Found illegal format in subtype name(" + subtypeName + "): " + e);
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
            extraValueMap = new HashMap();
            String[] pairs = this.mSubtypeExtraValue.split(EXTRA_VALUE_PAIR_SEPARATOR);
            for (String split : pairs) {
                String[] pair = split.split(EXTRA_VALUE_KEY_VALUE_SEPARATOR);
                if (pair.length == 1) {
                    extraValueMap.put(pair[0], null);
                } else if (pair.length > 1) {
                    if (pair.length > 2) {
                        Slog.w(TAG, "ExtraValue has two or more '='s");
                    }
                    extraValueMap.put(pair[0], pair[1]);
                }
            }
            this.mExtraValueHashMapCache = extraValueMap;
            return extraValueMap;
        }
    }

    public boolean containsExtraValueKey(String key) {
        return getExtraValueHashMap().containsKey(key);
    }

    public String getExtraValueOf(String key) {
        return (String) getExtraValueHashMap().get(key);
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
        boolean z = true;
        if (!(o instanceof InputMethodSubtype)) {
            return false;
        }
        InputMethodSubtype subtype = (InputMethodSubtype) o;
        if (subtype.mSubtypeId == 0 && this.mSubtypeId == 0) {
            if (subtype.hashCode() != hashCode() || !subtype.getLocale().equals(getLocale()) || !subtype.getLanguageTag().equals(getLanguageTag()) || !subtype.getMode().equals(getMode()) || !subtype.getExtraValue().equals(getExtraValue()) || subtype.isAuxiliary() != isAuxiliary() || subtype.overridesImplicitlyEnabledSubtype() != overridesImplicitlyEnabledSubtype()) {
                z = false;
            } else if (subtype.isAsciiCapable() != isAsciiCapable()) {
                z = false;
            }
            return z;
        }
        if (subtype.hashCode() != hashCode()) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mSubtypeNameResId);
        dest.writeInt(this.mSubtypeIconResId);
        dest.writeString(this.mSubtypeLocale);
        dest.writeString(this.mSubtypeLanguageTag);
        dest.writeString(this.mSubtypeMode);
        dest.writeString(this.mSubtypeExtraValue);
        dest.writeInt(this.mIsAuxiliary ? 1 : 0);
        if (this.mOverridesImplicitlyEnabledSubtype) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mSubtypeHashCode);
        dest.writeInt(this.mSubtypeId);
        if (!this.mIsAsciiCapable) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    private static int hashCodeInternal(String locale, String mode, String extraValue, boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype, boolean isAsciiCapable) {
        if (isAsciiCapable ^ 1) {
            return Arrays.hashCode(new Object[]{locale, mode, extraValue, Boolean.valueOf(isAuxiliary), Boolean.valueOf(overridesImplicitlyEnabledSubtype)});
        }
        return Arrays.hashCode(new Object[]{locale, mode, extraValue, Boolean.valueOf(isAuxiliary), Boolean.valueOf(overridesImplicitlyEnabledSubtype), Boolean.valueOf(isAsciiCapable)});
    }

    public static List<InputMethodSubtype> sort(Context context, int flags, InputMethodInfo imi, List<InputMethodSubtype> subtypeList) {
        if (imi == null) {
            return subtypeList;
        }
        InputMethodSubtype subtype;
        HashSet<InputMethodSubtype> inputSubtypesSet = new HashSet(subtypeList);
        ArrayList<InputMethodSubtype> sortedList = new ArrayList();
        int N = imi.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            subtype = imi.getSubtypeAt(i);
            if (inputSubtypesSet.contains(subtype)) {
                sortedList.add(subtype);
                inputSubtypesSet.remove(subtype);
            }
        }
        for (InputMethodSubtype subtype2 : inputSubtypesSet) {
            sortedList.add(subtype2);
        }
        return sortedList;
    }
}

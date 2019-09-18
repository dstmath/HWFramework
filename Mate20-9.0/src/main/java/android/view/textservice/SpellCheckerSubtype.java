package android.view.textservice;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.inputmethod.InputMethodUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class SpellCheckerSubtype implements Parcelable {
    public static final Parcelable.Creator<SpellCheckerSubtype> CREATOR = new Parcelable.Creator<SpellCheckerSubtype>() {
        public SpellCheckerSubtype createFromParcel(Parcel source) {
            return new SpellCheckerSubtype(source);
        }

        public SpellCheckerSubtype[] newArray(int size) {
            return new SpellCheckerSubtype[size];
        }
    };
    private static final String EXTRA_VALUE_KEY_VALUE_SEPARATOR = "=";
    private static final String EXTRA_VALUE_PAIR_SEPARATOR = ",";
    public static final int SUBTYPE_ID_NONE = 0;
    private static final String SUBTYPE_LANGUAGE_TAG_NONE = "";
    private static final String TAG = SpellCheckerSubtype.class.getSimpleName();
    private HashMap<String, String> mExtraValueHashMapCache;
    private final String mSubtypeExtraValue;
    private final int mSubtypeHashCode;
    private final int mSubtypeId;
    private final String mSubtypeLanguageTag;
    private final String mSubtypeLocale;
    private final int mSubtypeNameResId;

    public SpellCheckerSubtype(int nameId, String locale, String languageTag, String extraValue, int subtypeId) {
        this.mSubtypeNameResId = nameId;
        this.mSubtypeLocale = locale != null ? locale : "";
        this.mSubtypeLanguageTag = languageTag != null ? languageTag : "";
        this.mSubtypeExtraValue = extraValue != null ? extraValue : "";
        this.mSubtypeId = subtypeId;
        this.mSubtypeHashCode = this.mSubtypeId != 0 ? this.mSubtypeId : hashCodeInternal(this.mSubtypeLocale, this.mSubtypeExtraValue);
    }

    @Deprecated
    public SpellCheckerSubtype(int nameId, String locale, String extraValue) {
        this(nameId, locale, "", extraValue, 0);
    }

    SpellCheckerSubtype(Parcel source) {
        this.mSubtypeNameResId = source.readInt();
        String s = source.readString();
        this.mSubtypeLocale = s != null ? s : "";
        String s2 = source.readString();
        this.mSubtypeLanguageTag = s2 != null ? s2 : "";
        String s3 = source.readString();
        this.mSubtypeExtraValue = s3 != null ? s3 : "";
        this.mSubtypeId = source.readInt();
        this.mSubtypeHashCode = this.mSubtypeId != 0 ? this.mSubtypeId : hashCodeInternal(this.mSubtypeLocale, this.mSubtypeExtraValue);
    }

    public int getNameResId() {
        return this.mSubtypeNameResId;
    }

    @Deprecated
    public String getLocale() {
        return this.mSubtypeLocale;
    }

    public String getLanguageTag() {
        return this.mSubtypeLanguageTag;
    }

    public String getExtraValue() {
        return this.mSubtypeExtraValue;
    }

    private HashMap<String, String> getExtraValueHashMap() {
        if (this.mExtraValueHashMapCache == null) {
            this.mExtraValueHashMapCache = new HashMap<>();
            for (String split : this.mSubtypeExtraValue.split(EXTRA_VALUE_PAIR_SEPARATOR)) {
                String[] pair = split.split(EXTRA_VALUE_KEY_VALUE_SEPARATOR);
                if (pair.length == 1) {
                    this.mExtraValueHashMapCache.put(pair[0], null);
                } else if (pair.length > 1) {
                    if (pair.length > 2) {
                        Slog.w(TAG, "ExtraValue has two or more '='s");
                    }
                    this.mExtraValueHashMapCache.put(pair[0], pair[1]);
                }
            }
        }
        return this.mExtraValueHashMapCache;
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

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof SpellCheckerSubtype)) {
            return false;
        }
        SpellCheckerSubtype subtype = (SpellCheckerSubtype) o;
        if (subtype.mSubtypeId == 0 && this.mSubtypeId == 0) {
            if (subtype.hashCode() == hashCode() && subtype.getNameResId() == getNameResId() && subtype.getLocale().equals(getLocale()) && subtype.getLanguageTag().equals(getLanguageTag()) && subtype.getExtraValue().equals(getExtraValue())) {
                z = true;
            }
            return z;
        }
        if (subtype.hashCode() == hashCode()) {
            z = true;
        }
        return z;
    }

    public Locale getLocaleObject() {
        if (!TextUtils.isEmpty(this.mSubtypeLanguageTag)) {
            return Locale.forLanguageTag(this.mSubtypeLanguageTag);
        }
        return InputMethodUtils.constructLocaleFromString(this.mSubtypeLocale);
    }

    public CharSequence getDisplayName(Context context, String packageName, ApplicationInfo appInfo) {
        Locale locale = getLocaleObject();
        String localeStr = locale != null ? locale.getDisplayName() : this.mSubtypeLocale;
        if (this.mSubtypeNameResId == 0) {
            return localeStr;
        }
        CharSequence subtypeName = context.getPackageManager().getText(packageName, this.mSubtypeNameResId, appInfo);
        if (TextUtils.isEmpty(subtypeName)) {
            return localeStr;
        }
        return String.format(subtypeName.toString(), new Object[]{localeStr});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.mSubtypeNameResId);
        dest.writeString(this.mSubtypeLocale);
        dest.writeString(this.mSubtypeLanguageTag);
        dest.writeString(this.mSubtypeExtraValue);
        dest.writeInt(this.mSubtypeId);
    }

    private static int hashCodeInternal(String locale, String extraValue) {
        return Arrays.hashCode(new Object[]{locale, extraValue});
    }

    public static List<SpellCheckerSubtype> sort(Context context, int flags, SpellCheckerInfo sci, List<SpellCheckerSubtype> subtypeList) {
        if (sci == null) {
            return subtypeList;
        }
        HashSet<SpellCheckerSubtype> subtypesSet = new HashSet<>(subtypeList);
        ArrayList<SpellCheckerSubtype> sortedList = new ArrayList<>();
        int N = sci.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            SpellCheckerSubtype subtype = sci.getSubtypeAt(i);
            if (subtypesSet.contains(subtype)) {
                sortedList.add(subtype);
                subtypesSet.remove(subtype);
            }
        }
        Iterator<SpellCheckerSubtype> it = subtypesSet.iterator();
        while (it.hasNext()) {
            sortedList.add(it.next());
        }
        return sortedList;
    }
}

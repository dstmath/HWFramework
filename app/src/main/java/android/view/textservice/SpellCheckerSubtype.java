package android.view.textservice;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import java.util.List;
import java.util.Locale;

public final class SpellCheckerSubtype implements Parcelable {
    public static final Creator<SpellCheckerSubtype> CREATOR = null;
    private static final String EXTRA_VALUE_KEY_VALUE_SEPARATOR = "=";
    private static final String EXTRA_VALUE_PAIR_SEPARATOR = ",";
    public static final int SUBTYPE_ID_NONE = 0;
    private static final String SUBTYPE_LANGUAGE_TAG_NONE = "";
    private static final String TAG = null;
    private HashMap<String, String> mExtraValueHashMapCache;
    private final String mSubtypeExtraValue;
    private final int mSubtypeHashCode;
    private final int mSubtypeId;
    private final String mSubtypeLanguageTag;
    private final String mSubtypeLocale;
    private final int mSubtypeNameResId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.textservice.SpellCheckerSubtype.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.textservice.SpellCheckerSubtype.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerSubtype.<clinit>():void");
    }

    public SpellCheckerSubtype(int nameId, String locale, String languageTag, String extraValue, int subtypeId) {
        this.mSubtypeNameResId = nameId;
        if (locale == null) {
            locale = SUBTYPE_LANGUAGE_TAG_NONE;
        }
        this.mSubtypeLocale = locale;
        if (languageTag == null) {
            languageTag = SUBTYPE_LANGUAGE_TAG_NONE;
        }
        this.mSubtypeLanguageTag = languageTag;
        if (extraValue == null) {
            extraValue = SUBTYPE_LANGUAGE_TAG_NONE;
        }
        this.mSubtypeExtraValue = extraValue;
        this.mSubtypeId = subtypeId;
        this.mSubtypeHashCode = this.mSubtypeId != 0 ? this.mSubtypeId : hashCodeInternal(this.mSubtypeLocale, this.mSubtypeExtraValue);
    }

    public SpellCheckerSubtype(int nameId, String locale, String extraValue) {
        this(nameId, locale, SUBTYPE_LANGUAGE_TAG_NONE, extraValue, SUBTYPE_ID_NONE);
    }

    SpellCheckerSubtype(Parcel source) {
        this.mSubtypeNameResId = source.readInt();
        String s = source.readString();
        if (s == null) {
            s = SUBTYPE_LANGUAGE_TAG_NONE;
        }
        this.mSubtypeLocale = s;
        s = source.readString();
        if (s == null) {
            s = SUBTYPE_LANGUAGE_TAG_NONE;
        }
        this.mSubtypeLanguageTag = s;
        s = source.readString();
        if (s == null) {
            s = SUBTYPE_LANGUAGE_TAG_NONE;
        }
        this.mSubtypeExtraValue = s;
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
            this.mExtraValueHashMapCache = new HashMap();
            String[] pairs = this.mSubtypeExtraValue.split(EXTRA_VALUE_PAIR_SEPARATOR);
            int N = pairs.length;
            for (int i = SUBTYPE_ID_NONE; i < N; i++) {
                String[] pair = pairs[i].split(EXTRA_VALUE_KEY_VALUE_SEPARATOR);
                if (pair.length == 1) {
                    this.mExtraValueHashMapCache.put(pair[SUBTYPE_ID_NONE], null);
                } else if (pair.length > 1) {
                    if (pair.length > 2) {
                        Slog.w(TAG, "ExtraValue has two or more '='s");
                    }
                    this.mExtraValueHashMapCache.put(pair[SUBTYPE_ID_NONE], pair[1]);
                }
            }
        }
        return this.mExtraValueHashMapCache;
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

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof SpellCheckerSubtype)) {
            return false;
        }
        SpellCheckerSubtype subtype = (SpellCheckerSubtype) o;
        if (subtype.mSubtypeId == 0 && this.mSubtypeId == 0) {
            if (subtype.hashCode() == hashCode() && subtype.getNameResId() == getNameResId() && subtype.getLocale().equals(getLocale()) && subtype.getLanguageTag().equals(getLanguageTag())) {
                z = subtype.getExtraValue().equals(getExtraValue());
            }
            return z;
        }
        if (subtype.hashCode() == hashCode()) {
            z = true;
        }
        return z;
    }

    public Locale getLocaleObject() {
        if (TextUtils.isEmpty(this.mSubtypeLanguageTag)) {
            return InputMethodUtils.constructLocaleFromString(this.mSubtypeLocale);
        }
        return Locale.forLanguageTag(this.mSubtypeLanguageTag);
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
        return SUBTYPE_ID_NONE;
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
        HashSet<SpellCheckerSubtype> subtypesSet = new HashSet(subtypeList);
        ArrayList<SpellCheckerSubtype> sortedList = new ArrayList();
        int N = sci.getSubtypeCount();
        for (int i = SUBTYPE_ID_NONE; i < N; i++) {
            SpellCheckerSubtype subtype = sci.getSubtypeAt(i);
            if (subtypesSet.contains(subtype)) {
                sortedList.add(subtype);
                subtypesSet.remove(subtype);
            }
        }
        for (SpellCheckerSubtype subtype2 : subtypesSet) {
            sortedList.add(subtype2);
        }
        return sortedList;
    }
}

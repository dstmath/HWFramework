package ohos.miscservices.inputmethod;

import java.util.Arrays;
import java.util.HashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class InputMethodSubMode implements Sequenceable {
    private static final int ASCII_CAPABLE_FALSE = 0;
    private static final int ASCII_CAPABLE_TRUE = 1;
    public static final Sequenceable.Producer<InputMethodSubMode> CREATOR = new Sequenceable.Producer<InputMethodSubMode>() {
        /* class ohos.miscservices.inputmethod.InputMethodSubMode.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public InputMethodSubMode createFromParcel(Parcel parcel) {
            if (parcel != null) {
                return new InputMethodSubMode(parcel);
            }
            HiLog.debug(InputMethodSubMode.TAG, "createFromParcel: source is null", new Object[0]);
            return null;
        }
    };
    private static final int DEFAULT_CAPACITY = 16;
    private static final String DISPLAY_NAME_NONE = "";
    private static final String EXTRA_VALUE_KEY_VALUE_SEPARATOR = "=";
    private static final String EXTRA_VALUE_NONE = "";
    private static final String EXTRA_VALUE_PAIR_SEPARATOR = ",";
    private static final int EXTRA_VALUE_TWO_SUBPAIRS = 2;
    private static final int ID_NONE = 0;
    private static final String INPUT_SOURCE_NONE = "";
    private static final String LANGUAGE_TYPE_NONE = "";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodSubMode");
    private String mExtraValue;
    private volatile HashMap<String, String> mExtraValueHashMapCache;
    private int mHashCode;
    private int mIconResId;
    private int mId;
    private String mInputSource;
    private boolean mIsAsciiCapable;
    private String mLanguageType;
    private int mNameResId;
    private Object mapLock = new Object();

    public InputMethodSubMode() {
        HiLog.info(TAG, "InputMethodSubMode default constructor", new Object[0]);
    }

    public InputMethodSubMode(Parcel parcel) {
        if (parcel != null) {
            String readString = parcel.readString();
            this.mLanguageType = readString == null ? "" : readString;
            String readString2 = parcel.readString();
            this.mInputSource = readString2 == null ? "" : readString2;
            String readString3 = parcel.readString();
            this.mExtraValue = readString3 == null ? "" : readString3;
            this.mHashCode = parcel.readInt();
            this.mId = parcel.readInt();
            this.mNameResId = parcel.readInt();
            this.mIconResId = parcel.readInt();
            this.mIsAsciiCapable = parcel.readInt() != 1 ? false : true;
        }
    }

    public void setId(int i) {
        this.mId = i;
        if (i != 0) {
            this.mHashCode = i;
        } else {
            this.mHashCode = hashCodeInternal(this.mLanguageType, this.mInputSource, this.mExtraValue, this.mIsAsciiCapable);
        }
    }

    public void setNameResId(int i) {
        this.mNameResId = i;
    }

    public void setIconResId(int i) {
        this.mIconResId = i;
    }

    public void setLanguageType(String str) {
        if (str == null) {
            str = "";
        }
        this.mLanguageType = str;
    }

    public void setInputSource(String str) {
        if (str == null) {
            str = "";
        }
        this.mInputSource = str;
    }

    public void setAsciiCapable(boolean z) {
        this.mIsAsciiCapable = z;
    }

    public void setExtraValue(String str) {
        if (str == null) {
            str = "";
        }
        this.mExtraValue = str;
    }

    public int getId() {
        return this.mId;
    }

    public int getNameResId() {
        return this.mNameResId;
    }

    public int getIconResId() {
        return this.mIconResId;
    }

    public String getLanguageType() {
        String str = this.mLanguageType;
        return str == null ? "" : str;
    }

    public String getInputSource() {
        String str = this.mInputSource;
        return str == null ? "" : str;
    }

    public String getExtraValue() {
        String str = this.mExtraValue;
        return str == null ? "" : str;
    }

    public boolean isAsciiCapable() {
        return this.mIsAsciiCapable;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.mLanguageType) || !parcel.writeString(this.mInputSource) || !parcel.writeString(this.mExtraValue)) {
            return false;
        } else {
            parcel.writeInt(this.mHashCode);
            parcel.writeInt(this.mId);
            parcel.writeInt(this.mNameResId);
            parcel.writeInt(this.mIconResId);
            parcel.writeInt(this.mIsAsciiCapable ? 1 : 0);
            return true;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        HiLog.info(TAG, "InputMethodSubMode: unmarshalling not implemented", new Object[0]);
        return false;
    }

    public int hashCode() {
        return this.mHashCode;
    }

    public boolean hasId() {
        return this.mId != 0;
    }

    public boolean hasExtraValueKey(String str) {
        return getExtraValueHashMap().containsKey(str);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InputMethodSubMode)) {
            return false;
        }
        InputMethodSubMode inputMethodSubMode = (InputMethodSubMode) obj;
        if (inputMethodSubMode.mId == 0 && this.mId == 0) {
            boolean z = inputMethodSubMode.hashCode() == hashCode();
            boolean equals = inputMethodSubMode.getLanguageType().equals(getLanguageType());
            boolean equals2 = inputMethodSubMode.getInputSource().equals(getInputSource());
            boolean equals3 = inputMethodSubMode.getExtraValue().equals(getExtraValue());
            boolean z2 = z && equals;
            boolean z3 = equals2 && equals3;
            if (!z2 || !z3) {
                return false;
            }
            return true;
        } else if (inputMethodSubMode.hashCode() == hashCode()) {
            return true;
        } else {
            return false;
        }
    }

    private static int hashCodeInternal(String str, String str2, String str3, boolean z) {
        if (!z) {
            return Arrays.hashCode(new Object[]{str, str2, str3});
        }
        return Arrays.hashCode(new Object[]{str, str2, str3, Boolean.valueOf(z)});
    }

    private HashMap<String, String> getExtraValueHashMap() {
        String[] split;
        synchronized (this.mapLock) {
            HashMap<String, String> hashMap = this.mExtraValueHashMapCache;
            if (hashMap != null) {
                return hashMap;
            }
            HashMap<String, String> hashMap2 = new HashMap<>(16);
            if (this.mExtraValue == null) {
                this.mExtraValue = "";
            }
            for (String str : this.mExtraValue.split(EXTRA_VALUE_PAIR_SEPARATOR)) {
                String[] split2 = str.split(EXTRA_VALUE_KEY_VALUE_SEPARATOR);
                if (split2.length == 1) {
                    hashMap2.put(split2[0], null);
                } else if (split2.length > 1) {
                    if (split2.length > 2) {
                        HiLog.info(TAG, "InputMethodSubMode: ExtraValue has two or more '='s", new Object[0]);
                    }
                    hashMap2.put(split2[0], split2[1]);
                } else {
                    HiLog.error(TAG, "InputMethodSubMode: mExtraValue is null", new Object[0]);
                }
            }
            this.mExtraValueHashMapCache = hashMap2;
            return hashMap2;
        }
    }
}

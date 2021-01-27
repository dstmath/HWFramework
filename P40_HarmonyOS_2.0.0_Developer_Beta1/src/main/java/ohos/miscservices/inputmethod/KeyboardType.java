package ohos.miscservices.inputmethod;

import java.util.Arrays;
import java.util.HashMap;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public class KeyboardType implements Sequenceable {
    private static final int ASCII_CAPABLE_FALSE = 0;
    private static final int ASCII_CAPABLE_TRUE = 1;
    public static final Sequenceable.Producer<KeyboardType> CREATOR = new Sequenceable.Producer<KeyboardType>() {
        /* class ohos.miscservices.inputmethod.KeyboardType.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public KeyboardType createFromParcel(Parcel parcel) {
            if (parcel != null) {
                return new KeyboardType(parcel);
            }
            HiLog.debug(KeyboardType.TAG, "createFromParcel: source is null", new Object[0]);
            return null;
        }
    };
    private static final String CUSTOMIZED_VALUE_KEY_VALUE_SEPARATOR = "=";
    private static final String CUSTOMIZED_VALUE_NONE = "";
    private static final String CUSTOMIZED_VALUE_PAIR_SEPARATOR = ",";
    private static final int CUSTOMIZED_VALUE_TWO_SUBPAIRS = 2;
    private static final int DEFAULT_CAPACITY = 16;
    private static final int ID_NONE = 0;
    private static final String INPUT_SOURCE_NONE = "";
    private static final String LANGUAGE_TYPE_NONE = "";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "KeyboardType");
    private String mCustomizedValue;
    private volatile HashMap<String, String> mCustomizedValueHashMapCache;
    private int mHashCode;
    private int mIconId;
    private int mId;
    private String mInputSource;
    private boolean mIsAsciiCapable;
    private int mLabelId;
    private String mLanguage;
    private Object mapLock;

    public KeyboardType() {
        this(null);
        HiLog.info(TAG, "KeyboardType default constructor", new Object[0]);
    }

    public KeyboardType(Parcel parcel) {
        this.mapLock = new Object();
        if (parcel != null) {
            String readString = parcel.readString();
            this.mLanguage = readString == null ? "" : readString;
            String readString2 = parcel.readString();
            this.mInputSource = readString2 == null ? "" : readString2;
            String readString3 = parcel.readString();
            this.mCustomizedValue = readString3 == null ? "" : readString3;
            this.mHashCode = parcel.readInt();
            this.mId = parcel.readInt();
            this.mLabelId = parcel.readInt();
            this.mIconId = parcel.readInt();
            this.mIsAsciiCapable = parcel.readInt() != 1 ? false : true;
        }
    }

    public void setId(int i) {
        this.mId = i;
        if (i != 0) {
            this.mHashCode = i;
        } else {
            this.mHashCode = hashCodeInternal(this.mLanguage, this.mInputSource, this.mCustomizedValue, this.mIsAsciiCapable);
        }
    }

    public void setLabelId(int i) {
        this.mLabelId = i;
    }

    public void setIconId(int i) {
        this.mIconId = i;
    }

    public void setLanguage(String str) {
        if (str == null) {
            str = "";
        }
        this.mLanguage = str;
    }

    public void setInputSource(String str) {
        if (str == null) {
            str = "";
        }
        this.mInputSource = str;
    }

    public void setAsciiCapability(boolean z) {
        this.mIsAsciiCapable = z;
    }

    public void setCustomizedValue(String str) {
        if (str == null) {
            str = "";
        }
        this.mCustomizedValue = str;
    }

    public int getId() {
        return this.mId;
    }

    public int getLabelId() {
        return this.mLabelId;
    }

    public int getIconId() {
        return this.mIconId;
    }

    public String getLanguage() {
        String str = this.mLanguage;
        return str == null ? "" : str;
    }

    public String getInputSource() {
        String str = this.mInputSource;
        return str == null ? "" : str;
    }

    public String getCustomizedValue() {
        String str = this.mCustomizedValue;
        return str == null ? "" : str;
    }

    public boolean supportsAscii() {
        return this.mIsAsciiCapable;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.mLanguage) || !parcel.writeString(this.mInputSource) || !parcel.writeString(this.mCustomizedValue)) {
            return false;
        } else {
            parcel.writeInt(this.mHashCode);
            parcel.writeInt(this.mId);
            parcel.writeInt(this.mLabelId);
            parcel.writeInt(this.mIconId);
            parcel.writeInt(this.mIsAsciiCapable ? 1 : 0);
            return true;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        HiLog.info(TAG, "KeyboardType: unmarshalling not implemented", new Object[0]);
        return false;
    }

    public int hashCode() {
        return this.mHashCode;
    }

    public boolean hasId() {
        return this.mId != 0;
    }

    public boolean hasCustomizedValue(String str) {
        return getCustomizedValueHashMap().containsKey(str);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof KeyboardType)) {
            return false;
        }
        KeyboardType keyboardType = (KeyboardType) obj;
        if (keyboardType.mId == 0 && this.mId == 0) {
            boolean z = keyboardType.hashCode() == hashCode();
            boolean equals = keyboardType.getLanguage().equals(getLanguage());
            boolean equals2 = keyboardType.getInputSource().equals(getInputSource());
            boolean equals3 = keyboardType.getCustomizedValue().equals(getCustomizedValue());
            boolean z2 = z && equals;
            boolean z3 = equals2 && equals3;
            if (!z2 || !z3) {
                return false;
            }
            return true;
        } else if (keyboardType.hashCode() == hashCode()) {
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

    private HashMap<String, String> getCustomizedValueHashMap() {
        String[] split;
        synchronized (this.mapLock) {
            HashMap<String, String> hashMap = this.mCustomizedValueHashMapCache;
            if (hashMap != null) {
                return hashMap;
            }
            HashMap<String, String> hashMap2 = new HashMap<>(16);
            if (this.mCustomizedValue == null) {
                this.mCustomizedValue = "";
            }
            for (String str : this.mCustomizedValue.split(CUSTOMIZED_VALUE_PAIR_SEPARATOR)) {
                String[] split2 = str.split(CUSTOMIZED_VALUE_KEY_VALUE_SEPARATOR);
                if (split2.length == 1) {
                    hashMap2.put(split2[0], null);
                } else if (split2.length > 1) {
                    if (split2.length > 2) {
                        HiLog.info(TAG, "KeyBoardType: ExtraValue has two or more '='s", new Object[0]);
                    }
                    hashMap2.put(split2[0], split2[1]);
                } else {
                    HiLog.error(TAG, "KeyBoardType: mExtraValue is null", new Object[0]);
                }
            }
            this.mCustomizedValueHashMapCache = hashMap2;
            return hashMap2;
        }
    }
}

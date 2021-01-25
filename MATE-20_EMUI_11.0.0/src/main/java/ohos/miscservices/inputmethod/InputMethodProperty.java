package ohos.miscservices.inputmethod;

import java.util.ArrayList;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public class InputMethodProperty implements Sequenceable {
    public static final int MAX_TYPE_NUM = 128;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodPropery");
    private boolean isSystemIme;
    private String mAbilityName;
    private String mConfigurationPage;
    private int mDefaultImeId;
    private String mImeId;
    private String mPackageName;
    private List<KeyboardType> mTypes = new ArrayList();

    public InputMethodProperty(String str) {
        this.mImeId = str;
    }

    public String getId() {
        return this.mImeId;
    }

    public void setId(String str) {
        this.mImeId = str;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String str) {
        this.mPackageName = str;
    }

    public String getAbilityName() {
        return this.mAbilityName;
    }

    public void setAbilityName(String str) {
        this.mAbilityName = str;
    }

    public String getConfigurationPage() {
        return this.mConfigurationPage;
    }

    public void setConfigurationPage(String str) {
        this.mConfigurationPage = str;
    }

    public boolean getSystemImeFlag() {
        return this.isSystemIme;
    }

    public void setSystemImeFlag(boolean z) {
        this.isSystemIme = z;
    }

    public KeyboardType getTypeAt(int i) {
        if (i >= 0 && i <= this.mTypes.size()) {
            return this.mTypes.get(i);
        }
        HiLog.error(TAG, "get KeyboardType fail due to ouf of bound.", new Object[0]);
        return null;
    }

    public void addType(KeyboardType keyboardType) {
        List<KeyboardType> list = this.mTypes;
        if (list == null) {
            HiLog.error(TAG, "set KeyboardType fail.", new Object[0]);
        } else {
            list.add(keyboardType);
        }
    }

    public int getTypeCount() {
        return this.mTypes.size();
    }

    public int getDefaultImeId() {
        return this.mDefaultImeId;
    }

    public void setDefaultImeId(int i) {
        this.mDefaultImeId = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        HiLog.info(TAG, "marshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.error(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        boolean writeString = parcel.writeString(this.mImeId);
        if (!parcel.writeString(this.mPackageName)) {
            writeString = false;
        }
        if (!parcel.writeString(this.mAbilityName)) {
            writeString = false;
        }
        if (!parcel.writeString(this.mConfigurationPage)) {
            writeString = false;
        }
        if (!parcel.writeBoolean(this.isSystemIme)) {
            writeString = false;
        }
        if (!parcel.writeInt(this.mDefaultImeId)) {
            writeString = false;
        }
        parcel.writeInt(getTypeCount());
        for (KeyboardType keyboardType : this.mTypes) {
            if (!keyboardType.marshalling(parcel)) {
                HiLog.error(TAG, "marshalling inputMethodSubMode fail.", new Object[0]);
                return false;
            }
        }
        return writeString;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        HiLog.info(TAG, "unmarshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "unmarshalling in is null", new Object[0]);
            return false;
        }
        this.mImeId = parcel.readString();
        this.mPackageName = parcel.readString();
        this.mAbilityName = parcel.readString();
        this.mConfigurationPage = parcel.readString();
        this.isSystemIme = parcel.readBoolean();
        this.mDefaultImeId = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt > 128) {
            HiLog.error(TAG, "unmarshalling fail when read submode size.", new Object[0]);
            return false;
        }
        this.mTypes.clear();
        for (int i = 0; i < readInt; i++) {
            addType(KeyboardType.CREATOR.createFromParcel(parcel));
        }
        return true;
    }
}

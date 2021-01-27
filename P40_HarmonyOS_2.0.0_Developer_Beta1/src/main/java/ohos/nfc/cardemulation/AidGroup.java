package ohos.nfc.cardemulation;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AidGroup implements Sequenceable {
    public static final int MAX_SIZE_AIDS = 256;
    private List<String> mAids;
    private String mType;

    public AidGroup() {
        this.mAids = null;
        this.mAids = new ArrayList();
        this.mType = "";
    }

    public AidGroup(List<String> list, String str) {
        this.mAids = null;
        if (list == null || list.size() == 0) {
            throw new NullPointerException("aids is null");
        } else if (list.size() <= 256) {
            for (String str2 : list) {
                if (!CardEmulation.isAidValid(str2)) {
                    throw new IllegalArgumentException("aid is not valid");
                }
            }
            if (CardEmulation.CATEGORY_PAYMENT.equals(str) || "other".equals(str)) {
                this.mType = str;
            } else {
                this.mType = "other";
            }
            this.mAids = new ArrayList(list.size());
            for (String str3 : list) {
                this.mAids.add(str3.toUpperCase());
            }
        } else {
            throw new IllegalArgumentException("Too many aids");
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.mType);
        parcel.writeInt(this.mAids.size());
        if (this.mAids.size() <= 0) {
            return true;
        }
        parcel.writeStringList(this.mAids);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.mType = parcel.readString();
        if (parcel.readInt() <= 0) {
            return true;
        }
        this.mAids = parcel.readStringList();
        return true;
    }

    public List<String> getAids() {
        List<String> list = this.mAids;
        if (list == null || list.size() == 0) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(this.mAids.size());
        for (String str : this.mAids) {
            arrayList.add(str);
        }
        return arrayList;
    }

    public String getType() {
        return this.mType;
    }
}

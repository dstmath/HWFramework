package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class RecommendationInfo implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "RecommendationInfo");
    private long id;
    private int offset;
    private String reference;
    private String textContent;

    public RecommendationInfo(int i, String str, long j, String str2) {
        this.offset = i;
        this.textContent = str;
        this.id = j;
        this.reference = str2;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        HiLog.info(TAG, "marshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        parcel.writeInt(this.offset);
        parcel.writeLong(this.id);
        boolean writeString = parcel.writeString(this.textContent);
        if (!parcel.writeString(this.reference)) {
            return false;
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
        this.offset = parcel.readInt();
        this.id = parcel.readLong();
        this.textContent = parcel.readString();
        this.reference = parcel.readString();
        return true;
    }

    public int getOffset() {
        return this.offset;
    }

    public String getTextContent() {
        return this.textContent;
    }

    public long getId() {
        return this.id;
    }

    public String getReference() {
        return this.reference;
    }
}

package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CompletionText implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "CompletionText");
    private long id;
    private int order;
    private String textContent;
    private String type;

    public CompletionText(int i, String str, long j, String str2) {
        this.order = i;
        this.textContent = str;
        this.id = j;
        this.type = str2;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        HiLog.info(TAG, "marshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        parcel.writeInt(this.order);
        parcel.writeLong(this.id);
        boolean writeString = parcel.writeString(this.textContent);
        if (!parcel.writeString(this.type)) {
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
        this.order = parcel.readInt();
        this.id = parcel.readLong();
        this.textContent = parcel.readString();
        this.type = parcel.readString();
        return true;
    }

    public int getOrder() {
        return this.order;
    }

    public String getTextContent() {
        return this.textContent;
    }

    public long getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }
}

package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CorrectionText implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "CorrectionText");
    private String correctedText;
    private String originalText;
    private int startOffset;

    public CorrectionText(int i, String str, String str2) {
        this.startOffset = i;
        this.originalText = str;
        this.correctedText = str2;
    }

    public int getStartOffset() {
        return this.startOffset;
    }

    public String getOriginalText() {
        return this.originalText;
    }

    public String getCorrectedText() {
        return this.correctedText;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        HiLog.info(TAG, "marshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        parcel.writeInt(this.startOffset);
        boolean writeString = parcel.writeString(this.originalText);
        if (!parcel.writeString(this.correctedText)) {
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
        this.startOffset = parcel.readInt();
        this.originalText = parcel.readString();
        this.correctedText = parcel.readString();
        return true;
    }
}

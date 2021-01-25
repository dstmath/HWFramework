package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class EditingTextRequest implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "EditingTextRequest");
    private int maxChars;
    private int maxLines;
    private int token;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        HiLog.info(TAG, "EditingTextRequest: marshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        parcel.writeInt(this.token);
        parcel.writeInt(this.maxLines);
        parcel.writeInt(this.maxChars);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        HiLog.info(TAG, "EditingTextRequest: unmarshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "unmarshalling in is null", new Object[0]);
            return false;
        }
        this.token = parcel.readInt();
        this.maxLines = parcel.readInt();
        this.maxChars = parcel.readInt();
        return true;
    }

    public int getToken() {
        return this.token;
    }

    public void setToken(int i) {
        this.token = i;
    }

    public int getMaxLines() {
        return this.maxLines;
    }

    public void setMaxLines(int i) {
        this.maxLines = i;
    }

    public int getMaxChars() {
        return this.maxChars;
    }

    public void setMaxChars(int i) {
        this.maxChars = i;
    }
}

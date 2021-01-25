package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class EditingText implements Sequenceable {
    public static final int FLAG_SELECTING = 2;
    public static final int FLAG_SINGLE_LINE = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "EditingText");
    private int changedEnd;
    private int changedStart;
    private int flags;
    private int offset = 0;
    private String prompt;
    private int selectionEnd;
    private int selectionStart;
    private String textContent;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        HiLog.info(TAG, "EditingText: marshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        boolean writeString = parcel.writeString(this.textContent);
        parcel.writeInt(this.offset);
        parcel.writeInt(this.changedStart);
        parcel.writeInt(this.changedEnd);
        parcel.writeInt(this.selectionStart);
        parcel.writeInt(this.selectionEnd);
        parcel.writeInt(this.flags);
        if (!parcel.writeString(this.prompt)) {
            return false;
        }
        return writeString;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        HiLog.info(TAG, "EditingText: unmarshalling begin", new Object[0]);
        if (parcel == null) {
            HiLog.debug(TAG, "unmarshalling in is null", new Object[0]);
            return false;
        }
        this.textContent = parcel.readString();
        this.offset = parcel.readInt();
        this.changedStart = parcel.readInt();
        this.changedEnd = parcel.readInt();
        this.selectionStart = parcel.readInt();
        this.selectionEnd = parcel.readInt();
        this.flags = parcel.readInt();
        this.prompt = parcel.readString();
        return true;
    }

    public String getTextContent() {
        return this.textContent;
    }

    public void setTextContent(String str) {
        this.textContent = str;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int i) {
        if (i < 0) {
            i = 0;
        }
        this.offset = i;
    }

    public int getChangedStart() {
        return this.changedStart;
    }

    public void setChangedStart(int i) {
        this.changedStart = i;
    }

    public int getChangedEnd() {
        return this.changedEnd;
    }

    public void setChangedEnd(int i) {
        this.changedEnd = i;
    }

    public int getSelectionStart() {
        return this.selectionStart;
    }

    public void setSelectionStart(int i) {
        this.selectionStart = i;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public void setSelectionEnd(int i) {
        this.selectionEnd = i;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int i) {
        this.flags = i;
    }

    public String getHint() {
        return this.prompt;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public void setHint(String str) {
        this.prompt = str;
    }

    public void setPrompt(String str) {
        this.prompt = str;
    }
}

package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class InputAttribute implements Sequenceable {
    public static final int ENTER_KEY_TYPE_DONE = 4;
    public static final int ENTER_KEY_TYPE_GO = 2;
    public static final int ENTER_KEY_TYPE_NEXT = 5;
    public static final int ENTER_KEY_TYPE_PREVIOUS = 6;
    public static final int ENTER_KEY_TYPE_SEARCH = 1;
    public static final int ENTER_KEY_TYPE_SEND = 3;
    public static final int ENTER_KEY_TYPE_UNSPECIFIED = 0;
    public static final int OPTION_ASCII = 32;
    public static final int OPTION_AUTO_CAP_CHARACTERS = 2;
    public static final int OPTION_AUTO_CAP_SENTENCES = 8;
    public static final int OPTION_AUTO_CAP_WORDS = 4;
    public static final int OPTION_MULTI_LINE = 1;
    public static final int OPTION_NONE = 0;
    public static final int OPTION_NO_FULLSCREEN = 16;
    public static final int PATTERN_DATETIME = 4;
    public static final int PATTERN_EMAIL = 6;
    public static final int PATTERN_NULL = 0;
    public static final int PATTERN_NUMBER = 2;
    public static final int PATTERN_PASSWORD = 7;
    public static final int PATTERN_PHONE = 3;
    public static final int PATTERN_TEXT = 1;
    public static final int PATTERN_URI = 5;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputAttribute");
    private String clientPackage;
    private int enterKeyType = 0;
    private int inputOption = 0;
    private int inputPattern = 0;

    public int getInputPattern() {
        return this.inputPattern;
    }

    public void setInputPattern(int i) {
        this.inputPattern = i;
    }

    public int getEnterKeyType() {
        return this.enterKeyType;
    }

    public void setEnterKeyType(int i) {
        this.enterKeyType = i;
    }

    public void setInputOption(int i) {
        this.inputOption = i;
    }

    public int getInputOption() {
        return this.inputOption;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            HiLog.error(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        parcel.writeInt(this.inputPattern);
        parcel.writeInt(this.enterKeyType);
        parcel.writeInt(this.inputOption);
        parcel.writeString(this.clientPackage);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            HiLog.error(TAG, "unmarshalling in is null", new Object[0]);
            return false;
        }
        this.inputPattern = parcel.readInt();
        this.enterKeyType = parcel.readInt();
        this.inputOption = parcel.readInt();
        this.clientPackage = parcel.readString();
        return true;
    }
}

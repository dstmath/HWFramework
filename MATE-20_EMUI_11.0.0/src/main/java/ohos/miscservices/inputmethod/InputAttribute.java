package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class InputAttribute implements Sequenceable {
    public static final int ACTION_DONE = 6;
    public static final int ACTION_GO = 2;
    public static final int ACTION_NEXT = 5;
    public static final int ACTION_NONE = 1;
    public static final int ACTION_PREVIOUS = 7;
    public static final int ACTION_SEARCH = 3;
    public static final int ACTION_SEND = 4;
    public static final int ACTION_UNSPECIFIED = 0;
    public static final int ENTER_KEY_TYPE_DONE = 4;
    public static final int ENTER_KEY_TYPE_GO = 2;
    public static final int ENTER_KEY_TYPE_NEXT = 5;
    public static final int ENTER_KEY_TYPE_PREVIOUS = 6;
    public static final int ENTER_KEY_TYPE_SEARCH = 1;
    public static final int ENTER_KEY_TYPE_SEND = 3;
    public static final int ENTER_KEY_TYPE_UNSPECIFIED = 0;
    public static final int FLAG_NO_FULLSCREEN = 33554432;
    public static final int MASK_ACTION = 255;
    public static final int OPTION_ASCII = 32;
    public static final int OPTION_AUTO_CAP_CHARACTERS = 2;
    public static final int OPTION_AUTO_CAP_SENTENCES = 8;
    public static final int OPTION_AUTO_CAP_WORDS = 4;
    public static final int OPTION_MULTI_LINE = 1;
    public static final int OPTION_NONE = 0;
    public static final int OPTION_NO_FULLSCREEN = 16;
    public static final int PATTERN_CATEGORY_DATETIME = 4;
    public static final int PATTERN_CATEGORY_NUMBER = 2;
    public static final int PATTERN_CATEGORY_PHONE = 3;
    public static final int PATTERN_CATEGORY_TEXT = 1;
    public static final int PATTERN_DATETIME = 4;
    public static final int PATTERN_EMAIL = 6;
    public static final int PATTERN_MASK_CATEGORY = 15;
    public static final int PATTERN_MASK_FLAGS = 16773120;
    public static final int PATTERN_MASK_VARIATION = 4080;
    public static final int PATTERN_NULL = 0;
    public static final int PATTERN_NUMBER = 2;
    public static final int PATTERN_PASSWORD = 7;
    public static final int PATTERN_PHONE = 3;
    public static final int PATTERN_TEXT = 1;
    public static final int PATTERN_TEXT_CAP_CHARACTERS = 4096;
    public static final int PATTERN_TEXT_CAP_SENTENCES = 16384;
    public static final int PATTERN_TEXT_CAP_WORDS = 8192;
    public static final int PATTERN_TEXT_FLAG_MULTI_LINE = 131072;
    public static final int PATTERN_TEXT_FLAG_NO_SUGGESTIONS = 524288;
    public static final int PATTERN_TEXT_VARIATION_EMAIL_ADDRESS = 32;
    public static final int PATTERN_TEXT_VARIATION_PASSWORD = 128;
    public static final int PATTERN_TEXT_VARIATION_URI = 16;
    public static final int PATTERN_TEXT_VARIATION_VISIBLE_PASSWORD = 144;
    public static final int PATTERN_URI = 5;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputAttribute");
    private int enterKeyType = 0;
    private int inputOption = 0;
    private int inputPattern = 0;

    public int getInputPattern() {
        return this.inputPattern;
    }

    public void setInputPattern(int i) {
        this.inputPattern = i;
    }

    public int getImeAction() {
        return this.enterKeyType;
    }

    public int getEnterKeyType() {
        return this.enterKeyType;
    }

    public void setImeAction(int i) {
        this.enterKeyType = i;
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
        return true;
    }
}

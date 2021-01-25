package ohos.telephony;

import java.util.ArrayList;
import java.util.Optional;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ShortMessage {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "ShortMessage");
    private ShortMessageBase wrappedShortMessage;

    public enum ShortMessageClass {
        UNKNOWN,
        INSTANT_MESSAGE,
        OPTIONAL_MESSAGE,
        SIM_MESSAGE,
        FORWARD_MESSAGE
    }

    private ShortMessage(ShortMessageBase shortMessageBase) {
        this.wrappedShortMessage = shortMessageBase;
    }

    public int getProtocolId() {
        return this.wrappedShortMessage.getProtocolId();
    }

    public ShortMessageClass getMessageClass() {
        int messageClass = this.wrappedShortMessage.getMessageClass();
        if (messageClass == 1) {
            return ShortMessageClass.INSTANT_MESSAGE;
        }
        if (messageClass == 2) {
            return ShortMessageClass.OPTIONAL_MESSAGE;
        }
        if (messageClass == 3) {
            return ShortMessageClass.SIM_MESSAGE;
        }
        if (messageClass != 4) {
            return ShortMessageClass.UNKNOWN;
        }
        return ShortMessageClass.FORWARD_MESSAGE;
    }

    public static Optional<ShortMessage> createMessage(byte[] bArr, String str) {
        if (bArr == null) {
            HiLog.error(TAG, "createMessage(): pdu is null", new Object[0]);
            return Optional.empty();
        }
        ShortMessageBase orElse = ShortMessageBase.createMessage(bArr, str).orElse(null);
        if (orElse != null) {
            return Optional.of(new ShortMessage(orElse));
        }
        return Optional.empty();
    }

    public static ArrayList<String> splitMessageBySlotId(String str, int i) {
        return ShortMessageBase.splitMessageBySlotId(str, i);
    }

    public String getVisibleMessageBody() {
        return this.wrappedShortMessage.getVisibleMessageBody();
    }

    public String getVisibleRawAddress() {
        return this.wrappedShortMessage.getVisibleRawAddress();
    }

    public String getScAddress() {
        return this.wrappedShortMessage.getScAddress();
    }

    public long getScTimestamp() {
        return this.wrappedShortMessage.getScTimestamp();
    }

    public byte[] getUserRawData() {
        return this.wrappedShortMessage.getUserRawData();
    }

    @SystemApi
    public boolean isEmailMessage() {
        return this.wrappedShortMessage.isEmailMessage();
    }

    public boolean isReplaceMessage() {
        return this.wrappedShortMessage.isReplaceMessage();
    }

    public boolean hasReplyPath() {
        return this.wrappedShortMessage.hasReplyPath();
    }

    @SystemApi
    public String getEmailMessageBody() {
        return this.wrappedShortMessage.getEmailMessageBody();
    }

    @SystemApi
    public String getEmailAddress() {
        return this.wrappedShortMessage.getEmailAddress();
    }
}

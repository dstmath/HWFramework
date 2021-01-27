package ohos.media.image.common;

public enum DecodeEvent {
    EVENT_COMPLETE_DECODE(0),
    EVENT_PARTIAL_DECODE(1);
    
    private final int decodeEventValue;

    private DecodeEvent(int i) {
        this.decodeEventValue = i;
    }

    public int getValue() {
        return this.decodeEventValue;
    }
}

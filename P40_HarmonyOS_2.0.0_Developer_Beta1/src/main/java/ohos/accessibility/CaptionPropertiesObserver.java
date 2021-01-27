package ohos.accessibility;

public interface CaptionPropertiesObserver {
    public static final int STATE_CHANGED_CAPTION_ENABLED = 1;
    public static final int STATE_CHANGED_CAPTION_STYLE = 2;
    public static final int STATE_CHANGED_FONT_TYPE_SIZE = 4;
    public static final int STATE_CHANGED_LOCALE = 3;

    void onStateChanged(int i, CaptionProperties captionProperties);
}

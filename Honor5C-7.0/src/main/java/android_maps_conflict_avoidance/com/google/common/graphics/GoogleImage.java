package android_maps_conflict_avoidance.com.google.common.graphics;

public interface GoogleImage {
    GoogleImage createScaledImage(int i, int i2, int i3, int i4, int i5, int i6);

    void drawImage(GoogleGraphics googleGraphics, int i, int i2);

    GoogleGraphics getGraphics();

    int getHeight();

    int getWidth();

    void recycle();
}

package android.media;

/* access modifiers changed from: package-private */
/* compiled from: WebVttRenderer */
public interface WebVttCueListener {
    void onCueParsed(TextTrackCue textTrackCue);

    void onRegionParsed(TextTrackRegion textTrackRegion);
}

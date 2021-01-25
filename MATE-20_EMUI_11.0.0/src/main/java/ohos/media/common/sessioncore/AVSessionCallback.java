package ohos.media.common.sessioncore;

import ohos.aafwk.content.Intent;
import ohos.app.GeneralReceiver;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public abstract class AVSessionCallback {
    private boolean onMediaButtonEventOverridden = true;

    public void onCommand(String str, PacMap pacMap, GeneralReceiver generalReceiver) {
    }

    public void onPause() {
    }

    public void onPlay() {
    }

    public void onPlayByMediaId(String str, PacMap pacMap) {
    }

    public void onPlayBySearch(String str, PacMap pacMap) {
    }

    public void onPlayByUri(Uri uri, PacMap pacMap) {
    }

    public void onPlayFastForward() {
    }

    public void onPlayNext() {
    }

    public void onPlayPrevious() {
    }

    public void onPrepareToPlay() {
    }

    public void onPrepareToPlayByMediaId(String str, PacMap pacMap) {
    }

    public void onPrepareToPlayBySearch(String str, PacMap pacMap) {
    }

    public void onPrepareToPlayByUri(Uri uri, PacMap pacMap) {
    }

    public void onRewind() {
    }

    public void onSeekTo(long j) {
    }

    public void onSetAVPlaybackCustomAction(String str, PacMap pacMap) {
    }

    public void onSetAVPlaybackSpeed(float f) {
    }

    public void onSetAVRatingStyle(AVRating aVRating) {
    }

    public void onSkipToAVQueueElement(long j) {
    }

    public void onStop() {
    }

    public boolean onMediaButtonEvent(Intent intent) {
        this.onMediaButtonEventOverridden = false;
        return true;
    }

    public final boolean isOnMediaButtonEventOverridden() {
        return this.onMediaButtonEventOverridden;
    }
}

package android.media;

public interface MediaTimeProvider {
    public static final long NO_TIME = -1;

    public interface OnMediaTimeListener {
        void onSeek(long j);

        void onStop();

        void onTimedEvent(long j);
    }

    void cancelNotifications(OnMediaTimeListener onMediaTimeListener);

    long getCurrentTimeUs(boolean z, boolean z2) throws IllegalStateException;

    void notifyAt(long j, OnMediaTimeListener onMediaTimeListener);

    void scheduleUpdate(OnMediaTimeListener onMediaTimeListener);
}

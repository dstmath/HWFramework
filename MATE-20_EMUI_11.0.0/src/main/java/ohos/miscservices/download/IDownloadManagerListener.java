package ohos.miscservices.download;

public interface IDownloadManagerListener {
    default void onCompleted(long j) {
    }

    default void onFailed(long j, int i) {
    }

    default void onPaused(long j) {
    }

    default void onProgress(long j, long j2, long j3) {
    }

    default void onRemoved(long j) {
    }
}

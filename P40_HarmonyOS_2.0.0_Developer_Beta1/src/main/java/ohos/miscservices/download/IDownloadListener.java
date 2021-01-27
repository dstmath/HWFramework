package ohos.miscservices.download;

public interface IDownloadListener {
    default void onCompleted() {
    }

    default void onFailed(int i) {
    }

    default void onPaused() {
    }

    default void onProgress(long j, long j2) {
    }

    default void onRemoved() {
    }
}

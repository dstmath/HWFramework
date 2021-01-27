package ohos.ai.cv.common;

public interface VisionCallback<T> {
    void onError(int i);

    void onProcessing(float f);

    void onResult(T t);
}

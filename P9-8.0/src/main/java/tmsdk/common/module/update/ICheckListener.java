package tmsdk.common.module.update;

public interface ICheckListener {
    void onCheckCanceled();

    void onCheckEvent(int i);

    void onCheckFinished(CheckResult checkResult);

    void onCheckStarted();
}

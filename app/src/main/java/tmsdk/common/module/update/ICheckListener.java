package tmsdk.common.module.update;

/* compiled from: Unknown */
public interface ICheckListener {
    void onCheckCanceled();

    void onCheckEvent(int i);

    void onCheckFinished(CheckResult checkResult);

    void onCheckStarted();
}

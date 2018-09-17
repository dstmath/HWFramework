package tmsdk.fg.module.cleanV2;

public interface ICleanTaskCallBack {
    void onCleanCanceled();

    void onCleanError(int i);

    void onCleanFinished();

    void onCleanProcessChange(int i, String str);

    void onCleanStarted();
}

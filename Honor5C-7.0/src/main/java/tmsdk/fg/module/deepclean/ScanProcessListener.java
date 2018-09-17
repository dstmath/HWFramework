package tmsdk.fg.module.deepclean;

/* compiled from: Unknown */
public interface ScanProcessListener {
    void onCleanCancel();

    void onCleanError(int i);

    void onCleanFinish();

    void onCleanProcessChange(long j, int i);

    void onCleanStart();

    void onRubbishFound(RubbishEntity rubbishEntity);

    void onScanCanceled();

    void onScanError(int i);

    void onScanFinished();

    void onScanProcessChange(int i, String str);

    void onScanStarted();
}

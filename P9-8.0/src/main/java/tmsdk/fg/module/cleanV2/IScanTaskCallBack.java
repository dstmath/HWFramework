package tmsdk.fg.module.cleanV2;

public interface IScanTaskCallBack {
    void onDirectoryChange(String str, int i);

    void onRubbishFound(RubbishEntity rubbishEntity);

    void onScanCanceled(RubbishHolder rubbishHolder);

    void onScanError(int i, RubbishHolder rubbishHolder);

    void onScanFinished(RubbishHolder rubbishHolder);

    void onScanStarted();
}

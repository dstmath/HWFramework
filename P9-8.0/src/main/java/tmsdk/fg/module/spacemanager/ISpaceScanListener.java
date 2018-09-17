package tmsdk.fg.module.spacemanager;

public interface ISpaceScanListener {
    void onCancelFinished();

    void onFinish(int i, Object obj);

    void onFound(Object obj);

    void onProgressChanged(int i);

    void onStart();
}

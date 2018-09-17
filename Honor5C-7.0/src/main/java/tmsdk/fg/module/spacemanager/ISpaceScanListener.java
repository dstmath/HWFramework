package tmsdk.fg.module.spacemanager;

/* compiled from: Unknown */
public interface ISpaceScanListener {
    void onCancelFinished();

    void onFinish(int i, Object obj);

    void onFound(Object obj);

    void onProgressChanged(int i);

    void onStart();
}

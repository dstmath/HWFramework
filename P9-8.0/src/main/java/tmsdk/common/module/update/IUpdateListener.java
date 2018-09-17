package tmsdk.common.module.update;

public interface IUpdateListener {
    void onProgressChanged(UpdateInfo updateInfo, int i);

    void onUpdateCanceled();

    void onUpdateEvent(UpdateInfo updateInfo, int i);

    void onUpdateFinished();

    void onUpdateStarted();
}

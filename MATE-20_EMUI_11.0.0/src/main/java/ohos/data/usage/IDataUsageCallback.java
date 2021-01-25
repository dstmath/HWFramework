package ohos.data.usage;

public interface IDataUsageCallback {
    void onStorageStateChanged(String str, MountState mountState, MountState mountState2);

    void onVolumeStateChanged(VolumeView volumeView, VolumeState volumeState, VolumeState volumeState2);
}

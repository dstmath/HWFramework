package ohos.media.audio;

public interface AudioDeviceChangeObserver {
    void onAudioDeviceChange(int i, AudioDeviceDescriptor[] audioDeviceDescriptorArr);
}

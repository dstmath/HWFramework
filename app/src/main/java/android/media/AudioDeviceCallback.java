package android.media;

public abstract class AudioDeviceCallback {
    public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
    }

    public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
    }
}

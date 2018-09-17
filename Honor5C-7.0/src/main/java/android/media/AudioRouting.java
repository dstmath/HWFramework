package android.media;

import android.os.Handler;

public interface AudioRouting {

    public interface OnRoutingChangedListener {
        void onRoutingChanged(AudioRouting audioRouting);
    }

    void addOnRoutingChangedListener(OnRoutingChangedListener onRoutingChangedListener, Handler handler);

    AudioDeviceInfo getPreferredDevice();

    AudioDeviceInfo getRoutedDevice();

    void removeOnRoutingChangedListener(OnRoutingChangedListener onRoutingChangedListener);

    boolean setPreferredDevice(AudioDeviceInfo audioDeviceInfo);
}

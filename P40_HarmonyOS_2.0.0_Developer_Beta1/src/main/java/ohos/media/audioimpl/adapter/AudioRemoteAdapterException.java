package ohos.media.audioimpl.adapter;

import android.os.RemoteException;

public class AudioRemoteAdapterException extends RemoteException {
    private static final long serialVersionUID = 2875326278983721061L;

    public AudioRemoteAdapterException() {
    }

    public AudioRemoteAdapterException(String str) {
        super(str);
    }
}

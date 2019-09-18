package com.android.server.rms.dump;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.scene.DownloadScene;
import com.android.server.rms.scene.MediaScene;
import com.android.server.rms.scene.NonIdleScene;
import com.android.server.rms.scene.PhoneScene;

public final class DumpScene {
    public static final void dumpDownloadScene(Context context) {
        new DownloadScene(context);
        Log.d("RMS.dump", "DownloadScene state is " + scene.identify(null));
    }

    public static final void dumpMediaScene(Context context) {
        new MediaScene(context);
        Log.d("RMS.dump", "MediaScene state is " + scene.identify(null));
    }

    public static final void dumpNonIdleScene(Context context) {
        new NonIdleScene(context);
        Log.d("RMS.dump", "NonIdleScene state is " + scene.identify(null));
    }

    public static final void dumpPhoneScene(Context context) {
        new PhoneScene(context);
        Log.d("RMS.dump", "PhoneScene state is " + scene.identify(null));
    }
}

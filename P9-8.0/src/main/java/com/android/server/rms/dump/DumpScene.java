package com.android.server.rms.dump;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.scene.DownloadScene;
import com.android.server.rms.scene.MediaScene;
import com.android.server.rms.scene.NonIdleScene;
import com.android.server.rms.scene.PhoneScene;

public final class DumpScene {
    public static final void dumpDownloadScene(Context context) {
        Log.d("RMS.dump", "DownloadScene state is " + new DownloadScene(context).identify(null));
    }

    public static final void dumpMediaScene(Context context) {
        Log.d("RMS.dump", "MediaScene state is " + new MediaScene(context).identify(null));
    }

    public static final void dumpNonIdleScene(Context context) {
        Log.d("RMS.dump", "NonIdleScene state is " + new NonIdleScene(context).identify(null));
    }

    public static final void dumpPhoneScene(Context context) {
        Log.d("RMS.dump", "PhoneScene state is " + new PhoneScene(context).identify(null));
    }
}

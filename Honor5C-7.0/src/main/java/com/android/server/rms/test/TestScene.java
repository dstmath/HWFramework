package com.android.server.rms.test;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.scene.DownloadScene;
import com.android.server.rms.scene.MediaScene;
import com.android.server.rms.scene.NonIdleScene;
import com.android.server.rms.scene.PhoneScene;

public final class TestScene {
    public static final void testDownloadScene(Context context) {
        Log.d("RMS.test", "DownloadScene state is " + new DownloadScene(context).identify(null));
    }

    public static final void testMediaScene(Context context) {
        Log.d("RMS.test", "MediaScene state is " + new MediaScene(context).identify(null));
    }

    public static final void testNonIdleScene(Context context) {
        Log.d("RMS.test", "NonIdleScene state is " + new NonIdleScene(context).identify(null));
    }

    public static final void testPhoneScene(Context context) {
        Log.d("RMS.test", "PhoneScene state is " + new PhoneScene(context).identify(null));
    }
}

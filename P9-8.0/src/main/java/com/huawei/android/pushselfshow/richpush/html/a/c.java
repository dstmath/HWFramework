package com.huawei.android.pushselfshow.richpush.html.a;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

class c implements OnLoadCompleteListener {
    final /* synthetic */ a a;

    c(a aVar) {
        this.a = aVar;
    }

    public void onLoadComplete(SoundPool soundPool, int i, int i2) {
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "onSensorChanged and SoundPool onLoadComplete" + i);
        AudioManager audioManager = (AudioManager) this.a.e.getSystemService("audio");
        String str = "PushSelfShowLog";
        com.huawei.android.pushagent.a.a.c.a(str, "actualVolume is " + ((float) audioManager.getStreamVolume(3)));
        float streamMaxVolume = (float) audioManager.getStreamMaxVolume(3);
        audioManager.setStreamVolume(3, (((int) streamMaxVolume) * 2) / 3, 0);
        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "maxVolume is " + streamMaxVolume);
        soundPool.play(i, 1.0f, 1.0f, 1, 0, 1.0f);
    }
}

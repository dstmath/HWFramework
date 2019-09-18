package com.android.server.audio;

import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.os.IBinder;
import com.android.server.audio.MediaFocusControl;

public class HwFocusRequester extends FocusRequester {
    private static final String TAG = "HwFocusRequester";

    public boolean getIsInExternal() {
        return this.mIsInExternal;
    }

    public void setIsInExternal(boolean isInExternal) {
        this.mIsInExternal = isInExternal;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public HwFocusRequester(AudioAttributes aa, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk, boolean isInExternal) {
        super(aa, focusRequest, grantFlags, afl, source, id, hdlr, pn, uid, ctlr, sdk);
        this.mIsInExternal = isInExternal;
    }

    public HwFocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, MediaFocusControl.AudioFocusDeathHandler hdlr, MediaFocusControl ctlr, boolean isInExternal) {
        super(afi, afl, source, hdlr, ctlr);
        this.mIsInExternal = isInExternal;
    }
}

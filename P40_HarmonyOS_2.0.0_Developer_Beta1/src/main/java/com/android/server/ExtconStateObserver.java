package com.android.server;

import android.os.FileUtils;
import android.os.UEventObserver;
import com.android.server.ExtconUEventObserver;
import java.io.File;
import java.io.IOException;

public abstract class ExtconStateObserver<S> extends ExtconUEventObserver {
    private static final boolean LOG = false;
    private static final String TAG = "ExtconStateObserver";

    public abstract S parseState(ExtconUEventObserver.ExtconInfo extconInfo, String str);

    public abstract void updateState(ExtconUEventObserver.ExtconInfo extconInfo, String str, S s);

    public S parseStateFromFile(ExtconUEventObserver.ExtconInfo extconInfo) throws IOException {
        return parseState(extconInfo, FileUtils.readTextFile(new File(extconInfo.getStatePath()), 0, null).trim());
    }

    @Override // com.android.server.ExtconUEventObserver
    public void onUEvent(ExtconUEventObserver.ExtconInfo extconInfo, UEventObserver.UEvent event) {
        String name = event.get("NAME");
        S state = parseState(extconInfo, event.get("STATE"));
        if (state != null) {
            updateState(extconInfo, name, state);
        }
    }
}

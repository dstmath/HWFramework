package ohos.media.common.sessioncore;

import ohos.utils.PacMap;

public final class AVBrowserRoot {
    private final PacMap options;
    private final String rootMediaId;

    public AVBrowserRoot(String str, PacMap pacMap) {
        if (str != null) {
            this.rootMediaId = str;
            this.options = pacMap;
            return;
        }
        throw new IllegalArgumentException("The rootMediaId cannot be null");
    }

    public String getRootMediaId() {
        return this.rootMediaId;
    }

    public PacMap getOptions() {
        return this.options;
    }
}

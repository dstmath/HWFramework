package tmsdk.common.roach.nest;

import android.os.Bundle;
import tmsdkobf.pu;

public abstract class ActionI {
    public static final String privDirKey = "privDirKey";

    public abstract void clean();

    public abstract void onStart(Bundle bundle);

    public abstract void onStop();

    public void finish() {
        onStop();
        pu.hW().a(this);
    }
}

package android.magicwin;

import android.os.Bundle;

public interface HwMagicWindow {
    IHwMagicWindow getService();

    Bundle performHwMagicWindowPolicy(int i, Object... objArr);
}

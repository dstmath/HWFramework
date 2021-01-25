package com.android.server.location;

import java.util.List;

public abstract class GpsFreezeListener {
    public abstract void onFreezeProChange(String str);

    public void onWhiteListChange(int type, List<String> list) {
    }
}

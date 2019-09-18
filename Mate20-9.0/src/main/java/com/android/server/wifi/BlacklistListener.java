package com.android.server.wifi;

import java.util.List;

public abstract class BlacklistListener {
    public void onBlacklistChange(List<String> list) {
    }
}

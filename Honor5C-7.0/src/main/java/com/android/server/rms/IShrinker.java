package com.android.server.rms;

import android.os.Bundle;

public interface IShrinker {
    void interrupt();

    int reclaim(String str, Bundle bundle);
}

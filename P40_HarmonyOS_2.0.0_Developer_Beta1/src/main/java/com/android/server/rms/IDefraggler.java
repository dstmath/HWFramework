package com.android.server.rms;

import android.os.Bundle;

public interface IDefraggler {
    int compact(String str, Bundle bundle);

    void interrupt();
}

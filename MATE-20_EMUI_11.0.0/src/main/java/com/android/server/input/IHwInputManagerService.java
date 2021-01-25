package com.android.server.input;

import android.content.Context;
import android.os.Handler;

public interface IHwInputManagerService {
    InputManagerService getInstance(Context context, Handler handler);
}

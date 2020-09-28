package android.view;

import android.content.Context;

public interface IHwView {
    void onClick(View view, Context context);

    void scheduleFrameNow(boolean z, View view);
}

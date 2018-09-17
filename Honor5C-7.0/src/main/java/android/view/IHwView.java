package android.view;

import android.content.Context;

public interface IHwView {
    boolean cancelAnimation(View view, Context context);

    void onClick(View view, Context context);
}

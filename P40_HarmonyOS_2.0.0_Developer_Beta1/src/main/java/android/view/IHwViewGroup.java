package android.view;

import android.content.Context;

public interface IHwViewGroup {
    boolean accelerateSliding(Context context, boolean z, MotionEvent motionEvent);

    void accelerateWhiteBlockDisplay(MotionEvent motionEvent, ViewGroup viewGroup);

    void getWhiteBlockWhiteList(ViewGroup viewGroup, Context context);

    boolean isSubRecyclerView(ViewGroup viewGroup);

    Boolean isWhiteBlockEnable(ViewGroup viewGroup);

    IHwViewGroup newInstance();
}

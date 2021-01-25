package android.view;

import android.content.Context;

public interface IHwViewGroup {
    boolean accelerateSliding(Context context, boolean z, MotionEvent motionEvent);

    boolean isSubRecyclerView(ViewGroup viewGroup);

    IHwViewGroup newInstance();
}

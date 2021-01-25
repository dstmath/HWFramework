package android.pc;

import android.content.Context;
import android.view.KeyEvent;
import com.android.internal.widget.DecorCaptionViewEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AbsHwDecorCaptionView extends DecorCaptionViewEx {
    public AbsHwDecorCaptionView(Context context) {
    }

    @Override // com.android.internal.widget.DecorCaptionViewEx
    @HwSystemApi
    public void updateShade(boolean isLight) {
    }

    @Override // com.android.internal.widget.DecorCaptionViewEx
    @HwSystemApi
    public boolean processKeyEvent(KeyEvent event) {
        return false;
    }
}

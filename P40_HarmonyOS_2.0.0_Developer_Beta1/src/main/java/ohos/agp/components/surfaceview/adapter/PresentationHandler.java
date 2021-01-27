package ohos.agp.components.surfaceview.adapter;

import android.app.Presentation;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PresentationHandler extends Presentation {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "PresentationHandler");
    private TextView mDefaultView;
    private View mOutView;
    private final Context mOuterContext;
    private FrameLayout mRootView;
    private final Window mWindow;

    public PresentationHandler(Context context, Display display, Object obj) {
        super(context, display);
        this.mOuterContext = context;
        if (obj instanceof View) {
            this.mOutView = (View) obj;
        }
        this.mWindow = getWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = this.mWindow;
        if (window != null) {
            window.setFlags(8, 8);
            this.mWindow.setType(2030);
            this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        }
        this.mRootView = new FrameLayout(getContext());
        this.mDefaultView = new TextView(getContext());
        this.mDefaultView.setBackgroundColor(-16711936);
        View view = this.mOutView;
        if (view != null) {
            this.mRootView.addView(view);
        } else {
            HiLog.error(LABEL, "outView is not null,use the default view", new Object[0]);
            this.mRootView.addView(this.mDefaultView);
        }
        this.mRootView.setFocusableInTouchMode(true);
        this.mRootView.requestFocus();
        setContentView(this.mRootView);
    }

    public View getRootView() {
        return this.mRootView;
    }
}

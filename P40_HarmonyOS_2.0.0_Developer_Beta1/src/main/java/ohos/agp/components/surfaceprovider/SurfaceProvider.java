package ohos.agp.components.surfaceprovider;

import java.util.Optional;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.surfaceview.adapter.SurfaceViewAdapter;
import ohos.agp.graphics.SurfaceOps;
import ohos.app.Context;

public class SurfaceProvider extends Component {
    private static final OnLayoutChangeListener ON_LAYOUT_CHANGE_LISTENER = new OnLayoutChangeListener() {
        /* class ohos.agp.components.surfaceprovider.SurfaceProvider.AnonymousClass1 */

        @Override // ohos.agp.components.surfaceprovider.SurfaceProvider.OnLayoutChangeListener
        public void onSetWidth(SurfaceProvider surfaceProvider, int i) {
            if (surfaceProvider.mSurfaceViewAdapter != null) {
                surfaceProvider.mSurfaceViewAdapter.setWidth(i);
            }
        }

        @Override // ohos.agp.components.surfaceprovider.SurfaceProvider.OnLayoutChangeListener
        public void onSetHeight(SurfaceProvider surfaceProvider, int i) {
            if (surfaceProvider.mSurfaceViewAdapter != null) {
                surfaceProvider.mSurfaceViewAdapter.setHeight(i);
            }
        }

        @Override // ohos.agp.components.surfaceprovider.SurfaceProvider.OnLayoutChangeListener
        public void onSetPosition(SurfaceProvider surfaceProvider, float f, float f2) {
            if (surfaceProvider.mSurfaceViewAdapter != null) {
                surfaceProvider.mSurfaceViewAdapter.setPosition(f, f2);
            }
        }

        @Override // ohos.agp.components.surfaceprovider.SurfaceProvider.OnLayoutChangeListener
        public void onFirstLayout(SurfaceProvider surfaceProvider) {
            if (surfaceProvider.mSurfaceViewAdapter != null) {
                surfaceProvider.mSurfaceViewAdapter.setWidth(surfaceProvider.getWidth());
                surfaceProvider.mSurfaceViewAdapter.setHeight(surfaceProvider.getHeight());
                surfaceProvider.mSurfaceViewAdapter.addToWindow();
            }
        }
    };
    private final SurfaceViewAdapter mSurfaceViewAdapter;

    private interface OnLayoutChangeListener {
        void onFirstLayout(SurfaceProvider surfaceProvider);

        void onSetHeight(SurfaceProvider surfaceProvider, int i);

        void onSetPosition(SurfaceProvider surfaceProvider, float f, float f2);

        void onSetWidth(SurfaceProvider surfaceProvider, int i);
    }

    private native long nativeGetAGPSurfaceViewHandle();

    private native void nativeRemoveFromWindow(long j);

    private native void nativeSetOnLayoutChangeCallback(long j, OnLayoutChangeListener onLayoutChangeListener);

    public SurfaceProvider(Context context) {
        this(context, null);
    }

    public SurfaceProvider(Context context, AttrSet attrSet) {
        this(context, attrSet, "SurfaceProviderDefaultStyle");
    }

    public SurfaceProvider(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        setAlpha(0.0f);
        this.mSurfaceViewAdapter = new SurfaceViewAdapter(context);
        nativeSetOnLayoutChangeCallback(this.mNativeViewPtr, ON_LAYOUT_CHANGE_LISTENER);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetAGPSurfaceViewHandle();
        }
    }

    public void pinToZTop(boolean z) {
        SurfaceViewAdapter surfaceViewAdapter = this.mSurfaceViewAdapter;
        if (surfaceViewAdapter != null) {
            surfaceViewAdapter.pinToZTop(z);
        }
    }

    public void removeFromWindow() {
        SurfaceViewAdapter surfaceViewAdapter = this.mSurfaceViewAdapter;
        if (surfaceViewAdapter != null) {
            surfaceViewAdapter.removeFromWindow();
        }
        nativeRemoveFromWindow(this.mNativeViewPtr);
    }

    public Optional<SurfaceOps> getSurfaceOps() {
        SurfaceViewAdapter surfaceViewAdapter = this.mSurfaceViewAdapter;
        if (surfaceViewAdapter != null) {
            return Optional.of(surfaceViewAdapter.getSurfaceOps());
        }
        return Optional.empty();
    }
}

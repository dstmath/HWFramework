package ohos.agp.components.surfaceview.adapter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public class TextureComponentExAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "TextureComponentExAdapter");
    private static AtomicLong nextTextureId = new AtomicLong(0);
    private Context mAContext;
    private final ohos.app.Context mContext;
    private IFrameAvailableListener mFrameAvailableListener;
    private final int mHeight;
    private Object mOutView;
    private final SurfaceTextureHandler mTextureHandler = createSurfaceTextureHandler();
    private VirtualDisplayHandler mVirtualDisplayHandler;
    private final int mWidth;

    public interface IFrameAvailableListener {
        void notifyAvailable();
    }

    public TextureComponentExAdapter(ohos.app.Context context, int i, int i2) {
        this.mContext = context;
        this.mWidth = i;
        this.mHeight = i2;
    }

    public void registerObject(Object obj) {
        if (obj == null) {
            HiLog.error(LABEL, "registerObj is null", new Object[0]);
        } else {
            this.mOutView = obj;
        }
    }

    public void setFrameAvailableListener(IFrameAvailableListener iFrameAvailableListener) {
        this.mFrameAvailableListener = iFrameAvailableListener;
    }

    public boolean createVirtualDisplayHandler() {
        if (!init()) {
            HiLog.error(LABEL, "fail to init adapter", new Object[0]);
            return false;
        }
        this.mVirtualDisplayHandler = new VirtualDisplayHandler(this.mAContext, this.mTextureHandler.getSurfaceTexture(), this.mWidth, this.mHeight);
        Object obj = this.mOutView;
        if (obj == null) {
            HiLog.error(LABEL, "outView is null", new Object[0]);
            return false;
        }
        this.mVirtualDisplayHandler.registerObject(obj);
        if (this.mVirtualDisplayHandler.createPresentationShow()) {
            return true;
        }
        HiLog.error(LABEL, "fail to create presentation and show", new Object[0]);
        return false;
    }

    public void dispatchEvent(TouchEvent touchEvent) {
        Optional<View> prententationRootView = this.mVirtualDisplayHandler.getPrententationRootView();
        Optional hostMotionEvent = MultimodalEventFactory.getHostMotionEvent(touchEvent);
        if (!hostMotionEvent.isPresent()) {
            HiLog.error(LABEL, "faile to get touchEvent", new Object[0]);
        } else if (prententationRootView.isPresent()) {
            prententationRootView.get().dispatchTouchEvent((MotionEvent) hostMotionEvent.get());
        }
    }

    public Object getSurfaceTextureObj() {
        return this.mTextureHandler.getSurfaceTexture();
    }

    public long getSurfaceTextureId() {
        return this.mTextureHandler.getId();
    }

    private boolean init() {
        ohos.app.Context context = this.mContext;
        if (context == null) {
            HiLog.error(LABEL, "context is null", new Object[0]);
            return false;
        }
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            this.mAContext = (Context) hostContext;
            return true;
        }
        HiLog.error(LABEL, "failed to get AContext", new Object[0]);
        return false;
    }

    private SurfaceTextureHandler createSurfaceTextureHandler() {
        SurfaceTexture surfaceTexture = new SurfaceTexture(0);
        surfaceTexture.detachFromGLContext();
        return new SurfaceTextureHandler(nextTextureId.getAndIncrement(), surfaceTexture);
    }

    /* access modifiers changed from: package-private */
    public final class SurfaceTextureHandler {
        private final long mId;
        private final SurfaceTexture mSurfaceTexture;
        private SurfaceTexture.OnFrameAvailableListener onFrameListener = new SurfaceTexture.OnFrameAvailableListener() {
            /* class ohos.agp.components.surfaceview.adapter.TextureComponentExAdapter.SurfaceTextureHandler.AnonymousClass1 */

            @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (TextureComponentExAdapter.this.mFrameAvailableListener != null) {
                    TextureComponentExAdapter.this.mFrameAvailableListener.notifyAvailable();
                }
            }
        };

        SurfaceTextureHandler(long j, SurfaceTexture surfaceTexture) {
            this.mId = j;
            this.mSurfaceTexture = surfaceTexture;
            this.mSurfaceTexture.setOnFrameAvailableListener(this.onFrameListener, new Handler());
        }

        public SurfaceTexture getSurfaceTexture() {
            return this.mSurfaceTexture;
        }

        public long getId() {
            return this.mId;
        }
    }
}

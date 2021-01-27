package ohos.interwork.ui;

import ohos.agp.components.Component;
import ohos.agp.components.surfaceview.adapter.TextureComponentExAdapter;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;

public class TextureComponentEx extends Component {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108928, "TextureViewEx");
    private final Context mContex;
    private TextureComponentExAdapter mTextureComponentExAdapter;
    private Object mTextureObj;

    private native long nativeGetTextureComponentExHandle();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeNotifyFrame(long j, long j2);

    private native boolean nativeRegisterTexture(long j, long j2, Object obj);

    public TextureComponentEx(Context context) {
        super(context);
        this.mContex = context;
    }

    public boolean registerObject(Object obj, int i, int i2) {
        if (obj == null || i <= 0 || i2 <= 0) {
            HiLog.error(LABEL, "invalid input", new Object[0]);
            return false;
        }
        TextureComponentEx.super.setWidth(i);
        TextureComponentEx.super.setHeight(i2);
        this.mTextureComponentExAdapter = new TextureComponentExAdapter(this.mContex, i, i2);
        this.mTextureObj = this.mTextureComponentExAdapter.getSurfaceTextureObj();
        this.mTextureComponentExAdapter.registerObject(obj);
        if (initAdapter()) {
            return true;
        }
        HiLog.error(LABEL, "fail to init adapter", new Object[0]);
        return false;
    }

    /* access modifiers changed from: protected */
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetTextureComponentExHandle();
        }
    }

    private boolean initAdapter() {
        if (this.mTextureComponentExAdapter == null) {
            HiLog.error(LABEL, "mTextureComponentExAdapter is null", new Object[0]);
            return false;
        } else if (!nativeRegisterTexture(this.mNativeViewPtr, this.mTextureComponentExAdapter.getSurfaceTextureId(), this.mTextureObj)) {
            HiLog.error(LABEL, "faile to Register Texture", new Object[0]);
            return false;
        } else {
            this.mTextureComponentExAdapter.setFrameAvailableListener(new TextureComponentExAdapter.IFrameAvailableListener() {
                /* class ohos.interwork.ui.TextureComponentEx.AnonymousClass1 */

                public void notifyAvailable() {
                    TextureComponentEx textureComponentEx = TextureComponentEx.this;
                    textureComponentEx.nativeNotifyFrame(textureComponentEx.mNativeViewPtr, TextureComponentEx.this.mTextureComponentExAdapter.getSurfaceTextureId());
                }
            });
            if (!this.mTextureComponentExAdapter.createVirtualDisplayHandler()) {
                HiLog.error(LABEL, "fail to createVirtualDisplayHandler", new Object[0]);
                return false;
            }
            TextureComponentEx.super.setTouchEventListener(new Component.TouchEventListener() {
                /* class ohos.interwork.ui.TextureComponentEx.AnonymousClass2 */

                public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                    TextureComponentEx.this.mTextureComponentExAdapter.dispatchEvent(touchEvent);
                    return true;
                }
            });
            return true;
        }
    }
}

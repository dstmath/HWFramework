package com.huawei.ace.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.view.TextureLayer;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.huawei.ace.activity.AceVsyncWaiter;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

public class AceViewAdapter extends View {
    private static final long INVALID_TEXTURELAYER_HANDLE = 0;
    private static final WindowManager.LayoutParams MATCH_PARENT = new WindowManager.LayoutParams(-1, -1);
    private final Context context;
    private int offsetY = 0;

    public void createInputConnection(AceTextInputAdapter aceTextInputAdapter) {
    }

    /* access modifiers changed from: protected */
    public void processDraw(long j) {
    }

    public boolean processKeyEvent(KeyEvent keyEvent) {
        return false;
    }

    public boolean processTouchEvent(TouchEvent touchEvent) {
        return false;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public AceViewAdapter(Object obj) {
        super(r2);
        Context context2 = (Context) obj;
        this.context = context2;
        Object systemService = this.context.getSystemService("window");
        if (systemService instanceof WindowManager) {
            AceVsyncWaiter.getInstance((WindowManager) systemService);
        }
    }

    public void addToContent() {
        Context context2 = this.context;
        if (context2 != null && (context2 instanceof Activity)) {
            ((Activity) context2).setContentView(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        processDraw(canvas.getNativeCanvasWrapper());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        int[] iArr = new int[2];
        getLocationOnScreen(iArr);
        this.offsetY = iArr[1];
        super.onSizeChanged(i, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        AceTextInputAdapter aceTextInputAdapter = new AceTextInputAdapter();
        aceTextInputAdapter.setEditorInfo(editorInfo);
        createInputConnection(aceTextInputAdapter);
        if (aceTextInputAdapter.getInputConnection() != null) {
            return aceTextInputAdapter.getInputConnection();
        }
        return super.onCreateInputConnection(editorInfo);
    }

    public long getTextureLayerHandle(Object obj) {
        if (obj instanceof TextureLayer) {
            return ((TextureLayer) obj).getLayerHandle();
        }
        return 0;
    }

    public IAceTextureLayer createIAceTextureLayer() {
        return new IAceTextureLayer() {
            /* class com.huawei.ace.adapter.AceViewAdapter.AnonymousClass1 */

            @Override // com.huawei.ace.adapter.IAceTextureLayer
            public TextureLayer createTextureLayer() {
                return AceViewAdapter.this.getThreadedRenderer().createTextureLayer();
            }
        };
    }
}

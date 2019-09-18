package android.text;

import android.graphics.BaseCanvas;
import android.graphics.Paint;

public interface GraphicsOperations extends CharSequence {
    void drawText(BaseCanvas baseCanvas, int i, int i2, float f, float f2, Paint paint);

    void drawTextRun(BaseCanvas baseCanvas, int i, int i2, int i3, int i4, float f, float f2, boolean z, Paint paint);

    float getTextRunAdvances(int i, int i2, int i3, int i4, boolean z, float[] fArr, int i5, Paint paint);

    int getTextRunCursor(int i, int i2, int i3, int i4, int i5, Paint paint);

    int getTextWidths(int i, int i2, float[] fArr, Paint paint);

    float measureText(int i, int i2, Paint paint);
}

package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;

public class AndroidGraphics implements GoogleGraphics {
    private static final Rect clipRect = null;
    private static final Rect destRect = null;
    private static final RectF oval = null;
    private static final Rect sourceRect = null;
    private Canvas canvas;
    private final Paint paint;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics.<clinit>():void");
    }

    public AndroidGraphics(Canvas c) {
        this.paint = new Paint();
        this.paint.setStrokeWidth(1.0f);
        this.canvas = c;
    }

    public void setCanvas(Canvas c) {
        this.canvas = c;
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    public void setColor(int color) {
        this.paint.setColor(-16777216 | color);
    }

    public void fillRect(int x, int y, int width, int height) {
        this.paint.setStyle(Style.FILL);
        this.canvas.drawRect((float) x, (float) y, (float) (x + width), (float) (y + height), this.paint);
    }

    public void drawImage(GoogleImage img, int x, int y) {
        if (img != null) {
            img.drawImage(this, x, y);
        }
    }

    public boolean drawScaledImage(GoogleImage image, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh) {
        if (image == null) {
            return false;
        }
        Bitmap bitmap = ((AndroidImage) image).getBitmap();
        if (bitmap == null) {
            return false;
        }
        sourceRect.set(sx, sy, sx + sw, sy + sh);
        destRect.set(dx, dy, dx + dw, dy + dh);
        this.canvas.drawBitmap(bitmap, sourceRect, destRect, null);
        return true;
    }
}

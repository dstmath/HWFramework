package android.gesture;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Gesture implements Parcelable {
    private static final boolean BITMAP_RENDERING_ANTIALIAS = true;
    private static final boolean BITMAP_RENDERING_DITHER = true;
    private static final int BITMAP_RENDERING_WIDTH = 2;
    public static final Creator<Gesture> CREATOR = null;
    private static final long GESTURE_ID_BASE = 0;
    private static final AtomicInteger sGestureCount = null;
    private final RectF mBoundingBox;
    private long mGestureID;
    private final ArrayList<GestureStroke> mStrokes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.gesture.Gesture.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.gesture.Gesture.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.gesture.Gesture.<clinit>():void");
    }

    public Gesture() {
        this.mBoundingBox = new RectF();
        this.mStrokes = new ArrayList();
        this.mGestureID = GESTURE_ID_BASE + ((long) sGestureCount.incrementAndGet());
    }

    public Object clone() {
        Gesture gesture = new Gesture();
        gesture.mBoundingBox.set(this.mBoundingBox.left, this.mBoundingBox.top, this.mBoundingBox.right, this.mBoundingBox.bottom);
        int count = this.mStrokes.size();
        for (int i = 0; i < count; i++) {
            gesture.mStrokes.add((GestureStroke) ((GestureStroke) this.mStrokes.get(i)).clone());
        }
        return gesture;
    }

    public ArrayList<GestureStroke> getStrokes() {
        return this.mStrokes;
    }

    public int getStrokesCount() {
        return this.mStrokes.size();
    }

    public void addStroke(GestureStroke stroke) {
        this.mStrokes.add(stroke);
        this.mBoundingBox.union(stroke.boundingBox);
    }

    public float getLength() {
        int len = 0;
        ArrayList<GestureStroke> strokes = this.mStrokes;
        for (int i = 0; i < strokes.size(); i++) {
            len = (int) (((GestureStroke) strokes.get(i)).length + ((float) len));
        }
        return (float) len;
    }

    public RectF getBoundingBox() {
        return this.mBoundingBox;
    }

    public Path toPath() {
        return toPath(null);
    }

    public Path toPath(Path path) {
        if (path == null) {
            path = new Path();
        }
        ArrayList<GestureStroke> strokes = this.mStrokes;
        int count = strokes.size();
        for (int i = 0; i < count; i++) {
            path.addPath(((GestureStroke) strokes.get(i)).getPath());
        }
        return path;
    }

    public Path toPath(int width, int height, int edge, int numSample) {
        return toPath(null, width, height, edge, numSample);
    }

    public Path toPath(Path path, int width, int height, int edge, int numSample) {
        if (path == null) {
            path = new Path();
        }
        ArrayList<GestureStroke> strokes = this.mStrokes;
        int count = strokes.size();
        for (int i = 0; i < count; i++) {
            path.addPath(((GestureStroke) strokes.get(i)).toPath((float) (width - (edge * BITMAP_RENDERING_WIDTH)), (float) (height - (edge * BITMAP_RENDERING_WIDTH)), numSample));
        }
        return path;
    }

    void setID(long id) {
        this.mGestureID = id;
    }

    public long getID() {
        return this.mGestureID;
    }

    public Bitmap toBitmap(int width, int height, int edge, int numSample, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate((float) edge, (float) edge);
        Paint paint = new Paint();
        paint.setAntiAlias(BITMAP_RENDERING_DITHER);
        paint.setDither(BITMAP_RENDERING_DITHER);
        paint.setColor(color);
        paint.setStyle(Style.STROKE);
        paint.setStrokeJoin(Join.ROUND);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeWidth(2.0f);
        ArrayList<GestureStroke> strokes = this.mStrokes;
        int count = strokes.size();
        for (int i = 0; i < count; i++) {
            canvas.drawPath(((GestureStroke) strokes.get(i)).toPath((float) (width - (edge * BITMAP_RENDERING_WIDTH)), (float) (height - (edge * BITMAP_RENDERING_WIDTH)), numSample), paint);
        }
        return bitmap;
    }

    public Bitmap toBitmap(int width, int height, int inset, int color) {
        float scale;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(BITMAP_RENDERING_DITHER);
        paint.setDither(BITMAP_RENDERING_DITHER);
        paint.setColor(color);
        paint.setStyle(Style.STROKE);
        paint.setStrokeJoin(Join.ROUND);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeWidth(2.0f);
        Path path = toPath();
        RectF bounds = new RectF();
        path.computeBounds(bounds, BITMAP_RENDERING_DITHER);
        float sx = ((float) (width - (inset * BITMAP_RENDERING_WIDTH))) / bounds.width();
        float sy = ((float) (height - (inset * BITMAP_RENDERING_WIDTH))) / bounds.height();
        if (sx > sy) {
            scale = sy;
        } else {
            scale = sx;
        }
        paint.setStrokeWidth(2.0f / scale);
        path.offset((-bounds.left) + ((((float) width) - (bounds.width() * scale)) / 2.0f), (-bounds.top) + ((((float) height) - (bounds.height() * scale)) / 2.0f));
        canvas.translate((float) inset, (float) inset);
        canvas.scale(scale, scale);
        canvas.drawPath(path, paint);
        return bitmap;
    }

    void serialize(DataOutputStream out) throws IOException {
        ArrayList<GestureStroke> strokes = this.mStrokes;
        int count = strokes.size();
        out.writeLong(this.mGestureID);
        out.writeInt(count);
        for (int i = 0; i < count; i++) {
            ((GestureStroke) strokes.get(i)).serialize(out);
        }
    }

    static Gesture deserialize(DataInputStream in) throws IOException {
        Gesture gesture = new Gesture();
        gesture.mGestureID = in.readLong();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            gesture.addStroke(GestureStroke.deserialize(in));
        }
        return gesture;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mGestureID);
        boolean result = false;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Document.FLAG_ARCHIVE);
        DataOutputStream outStream = new DataOutputStream(byteStream);
        try {
            serialize(outStream);
            result = BITMAP_RENDERING_DITHER;
        } catch (IOException e) {
            Log.e(GestureConstants.LOG_TAG, "Error writing Gesture to parcel:", e);
        } finally {
            GestureUtils.closeStream(outStream);
            GestureUtils.closeStream(byteStream);
        }
        if (result) {
            out.writeByteArray(byteStream.toByteArray());
        }
    }

    public int describeContents() {
        return 0;
    }
}

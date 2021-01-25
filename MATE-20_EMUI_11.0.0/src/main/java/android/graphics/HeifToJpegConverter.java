package android.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Trace;
import android.util.Log;
import java.io.OutputStream;

public class HeifToJpegConverter {
    public static boolean convert(String pathName, OutputStream stream) {
        if (pathName == null || stream == null) {
            return false;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.YCRCB_420_SP;
        opts.inSampleSize = 1;
        opts.inScaled = false;
        Trace.traceBegin(2, "convertHeifToJpeg_decode");
        Bitmap bmp = BitmapFactory.decodeFile(pathName, opts);
        Trace.traceEnd(2);
        if (bmp == null) {
            Log.e("BitmapFactory::HEIFCONVERT", "Decode heif to yuv fail!");
            return false;
        }
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        Trace.traceBegin(2, "convertHeifToJpeg_encode");
        boolean isSuccessful = bmp.compressYuvToJpeg(format, 90, stream);
        Trace.traceEnd(2);
        if (isSuccessful) {
            return true;
        }
        Log.e("BitmapFactory::HEIFCONVERT", "Encode yuv to jpeg fail!");
        return false;
    }
}

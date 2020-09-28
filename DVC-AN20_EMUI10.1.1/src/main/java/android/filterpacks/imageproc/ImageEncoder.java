package android.filterpacks.imageproc;

import android.app.Instrumentation;
import android.app.slice.SliceItem;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ImageFormat;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import java.io.OutputStream;

public class ImageEncoder extends Filter {
    @GenerateFieldPort(name = Instrumentation.REPORT_KEY_STREAMRESULT)
    private OutputStream mOutputStream;
    @GenerateFieldPort(hasDefault = true, name = MediaFormat.KEY_QUALITY)
    private int mQuality = 80;

    public ImageEncoder(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addMaskedInputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3, 0));
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext env) {
        pullInput(SliceItem.FORMAT_IMAGE).getBitmap().compress(Bitmap.CompressFormat.JPEG, this.mQuality, this.mOutputStream);
    }
}

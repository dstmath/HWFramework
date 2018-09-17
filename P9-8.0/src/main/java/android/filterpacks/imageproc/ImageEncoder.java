package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ImageFormat;
import android.graphics.Bitmap.CompressFormat;
import java.io.OutputStream;

public class ImageEncoder extends Filter {
    @GenerateFieldPort(name = "stream")
    private OutputStream mOutputStream;
    @GenerateFieldPort(hasDefault = true, name = "quality")
    private int mQuality = 80;

    public ImageEncoder(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3, 0));
    }

    public void process(FilterContext env) {
        pullInput("image").getBitmap().compress(CompressFormat.JPEG, this.mQuality, this.mOutputStream);
    }
}

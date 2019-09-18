package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ImageFormat;

public class GLTextureTarget extends Filter {
    @GenerateFieldPort(name = "texId")
    private int mTexId;

    public GLTextureTarget(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort("frame", ImageFormat.create(3));
    }

    public void process(FilterContext context) {
        Frame input = pullInput("frame");
        Frame frame = context.getFrameManager().newBoundFrame(ImageFormat.create(input.getFormat().getWidth(), input.getFormat().getHeight(), 3, 3), 100, (long) this.mTexId);
        frame.setDataFromFrame(input);
        frame.release();
    }
}

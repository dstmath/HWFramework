package android.filterpacks.imageproc;

import android.content.Context;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.geometry.Quad;

public class DrawOverlayFilter extends Filter {
    private ShaderProgram mProgram;

    public DrawOverlayFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormatMask = ImageFormat.create(3, 3);
        addMaskedInputPort("source", imageFormatMask);
        addMaskedInputPort(Context.OVERLAY_SERVICE, imageFormatMask);
        addMaskedInputPort("box", ObjectFormat.fromClass(Quad.class, 1));
        addOutputBasedOnInput("image", "source");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void prepare(FilterContext context) {
        this.mProgram = ShaderProgram.createIdentity(context);
    }

    public void process(FilterContext env) {
        Frame sourceFrame = pullInput("source");
        Frame overlayFrame = pullInput(Context.OVERLAY_SERVICE);
        this.mProgram.setTargetRegion(((Quad) pullInput("box").getObjectValue()).translated(1.0f, 1.0f).scaled(2.0f));
        Frame output = env.getFrameManager().newFrame(sourceFrame.getFormat());
        output.setDataFromFrame(sourceFrame);
        this.mProgram.process(overlayFrame, output);
        pushOutput("image", output);
        output.release();
    }
}

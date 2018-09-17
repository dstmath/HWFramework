package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.NativeProgram;
import android.filterfw.core.Program;
import android.filterfw.format.ImageFormat;

public class ToRGBFilter extends Filter {
    private int mInputBPP;
    private FrameFormat mLastFormat = null;
    private Program mProgram;

    public ToRGBFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        MutableFrameFormat mask = new MutableFrameFormat(2, 2);
        mask.setDimensionCount(2);
        addMaskedInputPort("image", mask);
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return getConvertedFormat(inputFormat);
    }

    public FrameFormat getConvertedFormat(FrameFormat format) {
        MutableFrameFormat result = format.mutableCopy();
        result.setMetaValue(ImageFormat.COLORSPACE_KEY, Integer.valueOf(2));
        result.setBytesPerSample(3);
        return result;
    }

    public void createProgram(FilterContext context, FrameFormat format) {
        this.mInputBPP = format.getBytesPerSample();
        if (this.mLastFormat == null || this.mLastFormat.getBytesPerSample() != this.mInputBPP) {
            this.mLastFormat = format;
            switch (this.mInputBPP) {
                case 1:
                    this.mProgram = new NativeProgram("filterpack_imageproc", "gray_to_rgb");
                    break;
                case 4:
                    this.mProgram = new NativeProgram("filterpack_imageproc", "rgba_to_rgb");
                    break;
                default:
                    throw new RuntimeException("Unsupported BytesPerPixel: " + this.mInputBPP + "!");
            }
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        createProgram(context, input.getFormat());
        Frame output = context.getFrameManager().newFrame(getConvertedFormat(input.getFormat()));
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }
}

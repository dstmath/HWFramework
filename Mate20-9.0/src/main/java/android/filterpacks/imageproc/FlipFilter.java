package android.filterpacks.imageproc;

import android.app.slice.SliceItem;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;

public class FlipFilter extends Filter {
    @GenerateFieldPort(hasDefault = true, name = "horizontal")
    private boolean mHorizontal = false;
    private Program mProgram;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;
    @GenerateFieldPort(hasDefault = true, name = "vertical")
    private boolean mVertical = false;

    public FlipFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3));
        addOutputBasedOnInput(SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE);
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void initProgram(FilterContext context, int target) {
        if (target == 3) {
            ShaderProgram shaderProgram = ShaderProgram.createIdentity(context);
            shaderProgram.setMaximumTileSize(this.mTileSize);
            this.mProgram = shaderProgram;
            this.mTarget = target;
            updateParameters();
            return;
        }
        throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mProgram != null) {
            updateParameters();
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput(SliceItem.FORMAT_IMAGE);
        FrameFormat inputFormat = input.getFormat();
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        this.mProgram.process(input, output);
        pushOutput(SliceItem.FORMAT_IMAGE, output);
        output.release();
    }

    private void updateParameters() {
        float y_origin = 0.0f;
        float height = 1.0f;
        float x_origin = this.mHorizontal ? 1.0f : 0.0f;
        if (this.mVertical) {
            y_origin = 1.0f;
        }
        float width = this.mHorizontal ? -1.0f : 1.0f;
        if (this.mVertical) {
            height = -1.0f;
        }
        ((ShaderProgram) this.mProgram).setSourceRect(x_origin, y_origin, width, height);
    }
}

package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.speech.tts.TextToSpeech.Engine;

public class FlipFilter extends Filter {
    @GenerateFieldPort(hasDefault = true, name = "horizontal")
    private boolean mHorizontal;
    private Program mProgram;
    private int mTarget;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize;
    @GenerateFieldPort(hasDefault = true, name = "vertical")
    private boolean mVertical;

    public FlipFilter(String name) {
        super(name);
        this.mVertical = false;
        this.mHorizontal = false;
        this.mTileSize = 640;
        this.mTarget = 0;
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3));
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void initProgram(FilterContext context, int target) {
        switch (target) {
            case Engine.DEFAULT_STREAM /*3*/:
                ShaderProgram shaderProgram = ShaderProgram.createIdentity(context);
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mProgram = shaderProgram;
                this.mTarget = target;
                updateParameters();
            default:
                throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mProgram != null) {
            updateParameters();
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }

    private void updateParameters() {
        ((ShaderProgram) this.mProgram).setSourceRect(this.mHorizontal ? Engine.DEFAULT_VOLUME : 0.0f, this.mVertical ? Engine.DEFAULT_VOLUME : 0.0f, this.mHorizontal ? ScaledLayoutParams.SCALE_UNSPECIFIED : Engine.DEFAULT_VOLUME, this.mVertical ? ScaledLayoutParams.SCALE_UNSPECIFIED : Engine.DEFAULT_VOLUME);
    }
}

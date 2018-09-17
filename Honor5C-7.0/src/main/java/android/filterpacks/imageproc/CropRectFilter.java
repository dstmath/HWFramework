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

public class CropRectFilter extends Filter {
    private int mHeight;
    @GenerateFieldPort(name = "height")
    private int mOutputHeight;
    @GenerateFieldPort(name = "width")
    private int mOutputWidth;
    private Program mProgram;
    private int mTarget;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize;
    private int mWidth;
    @GenerateFieldPort(name = "xorigin")
    private int mXorigin;
    @GenerateFieldPort(name = "yorigin")
    private int mYorigin;

    public CropRectFilter(String name) {
        super(name);
        this.mTileSize = 640;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mTarget = 0;
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3));
        addOutputBasedOnInput("image", "image");
    }

    public void initProgram(FilterContext context, int target) {
        switch (target) {
            case Engine.DEFAULT_STREAM /*3*/:
                ShaderProgram shaderProgram = ShaderProgram.createIdentity(context);
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mProgram = shaderProgram;
                this.mTarget = target;
            default:
                throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mProgram != null) {
            updateSourceRect(this.mWidth, this.mHeight);
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        Frame output = context.getFrameManager().newFrame(ImageFormat.create(this.mOutputWidth, this.mOutputHeight, 3, 3));
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            updateSourceRect(inputFormat.getWidth(), inputFormat.getHeight());
        }
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }

    void updateSourceRect(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        ((ShaderProgram) this.mProgram).setSourceRect(((float) this.mXorigin) / ((float) this.mWidth), ((float) this.mYorigin) / ((float) this.mHeight), ((float) this.mOutputWidth) / ((float) this.mWidth), ((float) this.mOutputHeight) / ((float) this.mHeight));
    }
}

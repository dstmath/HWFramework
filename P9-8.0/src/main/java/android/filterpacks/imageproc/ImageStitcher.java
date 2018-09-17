package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;

public class ImageStitcher extends Filter {
    private int mImageHeight;
    private int mImageWidth;
    private int mInputHeight;
    private int mInputWidth;
    private Frame mOutputFrame;
    @GenerateFieldPort(name = "padSize")
    private int mPadSize;
    private Program mProgram;
    private int mSliceHeight;
    private int mSliceIndex = 0;
    private int mSliceWidth;
    @GenerateFieldPort(name = "xSlices")
    private int mXSlices;
    @GenerateFieldPort(name = "ySlices")
    private int mYSlices;

    public ImageStitcher(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3, 3));
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    private FrameFormat calcOutputFormatForInput(FrameFormat format) {
        MutableFrameFormat outputFormat = format.mutableCopy();
        this.mInputWidth = format.getWidth();
        this.mInputHeight = format.getHeight();
        this.mSliceWidth = this.mInputWidth - (this.mPadSize * 2);
        this.mSliceHeight = this.mInputHeight - (this.mPadSize * 2);
        this.mImageWidth = this.mSliceWidth * this.mXSlices;
        this.mImageHeight = this.mSliceHeight * this.mYSlices;
        outputFormat.setDimensions(this.mImageWidth, this.mImageHeight);
        return outputFormat;
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat format = input.getFormat();
        if (this.mSliceIndex == 0) {
            this.mOutputFrame = context.getFrameManager().newFrame(calcOutputFormatForInput(format));
        } else if (!(format.getWidth() == this.mInputWidth && format.getHeight() == this.mInputHeight)) {
            throw new RuntimeException("Image size should not change.");
        }
        if (this.mProgram == null) {
            this.mProgram = ShaderProgram.createIdentity(context);
        }
        int outputOffsetX = (this.mSliceIndex % this.mXSlices) * this.mSliceWidth;
        int outputOffsetY = (this.mSliceIndex / this.mXSlices) * this.mSliceHeight;
        float outputWidth = (float) Math.min(this.mSliceWidth, this.mImageWidth - outputOffsetX);
        float outputHeight = (float) Math.min(this.mSliceHeight, this.mImageHeight - outputOffsetY);
        ((ShaderProgram) this.mProgram).setSourceRect(((float) this.mPadSize) / ((float) this.mInputWidth), ((float) this.mPadSize) / ((float) this.mInputHeight), outputWidth / ((float) this.mInputWidth), outputHeight / ((float) this.mInputHeight));
        ((ShaderProgram) this.mProgram).setTargetRect(((float) outputOffsetX) / ((float) this.mImageWidth), ((float) outputOffsetY) / ((float) this.mImageHeight), outputWidth / ((float) this.mImageWidth), outputHeight / ((float) this.mImageHeight));
        this.mProgram.process(input, this.mOutputFrame);
        this.mSliceIndex++;
        if (this.mSliceIndex == this.mXSlices * this.mYSlices) {
            pushOutput("image", this.mOutputFrame);
            this.mOutputFrame.release();
            this.mSliceIndex = 0;
        }
    }
}

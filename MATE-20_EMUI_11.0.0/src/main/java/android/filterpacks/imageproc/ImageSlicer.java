package android.filterpacks.imageproc;

import android.app.slice.SliceItem;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;

public class ImageSlicer extends Filter {
    private int mInputHeight;
    private int mInputWidth;
    private Frame mOriginalFrame;
    private int mOutputHeight;
    private int mOutputWidth;
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

    public ImageSlicer(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addMaskedInputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3, 3));
        addOutputBasedOnInput(SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE);
    }

    @Override // android.filterfw.core.Filter
    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    private void calcOutputFormatForInput(Frame frame) {
        this.mInputWidth = frame.getFormat().getWidth();
        this.mInputHeight = frame.getFormat().getHeight();
        int i = this.mInputWidth;
        int i2 = this.mXSlices;
        this.mSliceWidth = ((i + i2) - 1) / i2;
        int i3 = this.mInputHeight;
        int i4 = this.mYSlices;
        this.mSliceHeight = ((i3 + i4) - 1) / i4;
        int i5 = this.mSliceWidth;
        int i6 = this.mPadSize;
        this.mOutputWidth = i5 + (i6 * 2);
        this.mOutputHeight = this.mSliceHeight + (i6 * 2);
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        if (this.mSliceIndex == 0) {
            this.mOriginalFrame = pullInput(SliceItem.FORMAT_IMAGE);
            calcOutputFormatForInput(this.mOriginalFrame);
        }
        MutableFrameFormat outputFormat = this.mOriginalFrame.getFormat().mutableCopy();
        outputFormat.setDimensions(this.mOutputWidth, this.mOutputHeight);
        Frame output = context.getFrameManager().newFrame(outputFormat);
        if (this.mProgram == null) {
            this.mProgram = ShaderProgram.createIdentity(context);
        }
        int i = this.mSliceIndex;
        int i2 = this.mXSlices;
        int xSliceIndex = i % i2;
        int ySliceIndex = i / i2;
        int i3 = this.mPadSize;
        int i4 = this.mInputWidth;
        float x0 = ((float) ((this.mSliceWidth * xSliceIndex) - i3)) / ((float) i4);
        float f = (float) ((this.mSliceHeight * ySliceIndex) - i3);
        int i5 = this.mInputHeight;
        ((ShaderProgram) this.mProgram).setSourceRect(x0, f / ((float) i5), ((float) this.mOutputWidth) / ((float) i4), ((float) this.mOutputHeight) / ((float) i5));
        this.mProgram.process(this.mOriginalFrame, output);
        this.mSliceIndex++;
        if (this.mSliceIndex == this.mXSlices * this.mYSlices) {
            this.mSliceIndex = 0;
            this.mOriginalFrame.release();
            setWaitsOnInputPort(SliceItem.FORMAT_IMAGE, true);
        } else {
            this.mOriginalFrame.retain();
            setWaitsOnInputPort(SliceItem.FORMAT_IMAGE, false);
        }
        pushOutput(SliceItem.FORMAT_IMAGE, output);
        output.release();
    }
}

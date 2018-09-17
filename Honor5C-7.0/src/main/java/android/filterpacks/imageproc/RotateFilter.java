package android.filterpacks.imageproc;

import android.app.IActivityManager;
import android.bluetooth.BluetoothAssignedNumbers;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech.Engine;

public class RotateFilter extends Filter {
    @GenerateFieldPort(name = "angle")
    private int mAngle;
    private int mHeight;
    private int mOutputHeight;
    private int mOutputWidth;
    private Program mProgram;
    private int mTarget;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize;
    private int mWidth;

    public RotateFilter(String name) {
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
                shaderProgram.setClearsOutput(true);
                this.mProgram = shaderProgram;
                this.mTarget = target;
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
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
            this.mOutputWidth = this.mWidth;
            this.mOutputHeight = this.mHeight;
            updateParameters();
        }
        Frame output = context.getFrameManager().newFrame(ImageFormat.create(this.mOutputWidth, this.mOutputHeight, 3, 3));
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }

    private void updateParameters() {
        if (this.mAngle % 90 == 0) {
            float sinTheta;
            float cosTheta;
            if (this.mAngle % BluetoothAssignedNumbers.BDE_TECHNOLOGY == 0) {
                sinTheta = 0.0f;
                if (this.mAngle % IActivityManager.SET_VR_MODE_TRANSACTION == 0) {
                    cosTheta = Engine.DEFAULT_VOLUME;
                } else {
                    cosTheta = ScaledLayoutParams.SCALE_UNSPECIFIED;
                }
            } else {
                cosTheta = 0.0f;
                sinTheta = (this.mAngle + 90) % IActivityManager.SET_VR_MODE_TRANSACTION == 0 ? ScaledLayoutParams.SCALE_UNSPECIFIED : Engine.DEFAULT_VOLUME;
                this.mOutputWidth = this.mHeight;
                this.mOutputHeight = this.mWidth;
            }
            ((ShaderProgram) this.mProgram).setTargetRegion(new Quad(new Point((((-cosTheta) + sinTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE, (((-sinTheta) - cosTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE), new Point(((cosTheta + sinTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE, ((sinTheta - cosTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE), new Point((((-cosTheta) - sinTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE, (((-sinTheta) + cosTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE), new Point(((cosTheta - sinTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE, ((sinTheta + cosTheta) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE)));
            return;
        }
        throw new RuntimeException("degree has to be multiply of 90.");
    }
}

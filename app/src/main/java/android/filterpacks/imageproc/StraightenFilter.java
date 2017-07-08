package android.filterpacks.imageproc;

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

public class StraightenFilter extends Filter {
    private static final float DEGREE_TO_RADIAN = 0.017453292f;
    @GenerateFieldPort(hasDefault = true, name = "angle")
    private float mAngle;
    private int mHeight;
    @GenerateFieldPort(hasDefault = true, name = "maxAngle")
    private float mMaxAngle;
    private Program mProgram;
    private int mTarget;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize;
    private int mWidth;

    public StraightenFilter(String name) {
        super(name);
        this.mAngle = 0.0f;
        this.mMaxAngle = 45.0f;
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
            updateParameters();
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }

    private void updateParameters() {
        float cosTheta = (float) Math.cos((double) (this.mAngle * DEGREE_TO_RADIAN));
        float sinTheta = (float) Math.sin((double) (this.mAngle * DEGREE_TO_RADIAN));
        if (this.mMaxAngle <= 0.0f) {
            throw new RuntimeException("Max angle is out of range (0-180).");
        }
        this.mMaxAngle = this.mMaxAngle > 90.0f ? 90.0f : this.mMaxAngle;
        Point p0 = new Point(((-cosTheta) * ((float) this.mWidth)) + (((float) this.mHeight) * sinTheta), ((-sinTheta) * ((float) this.mWidth)) - (((float) this.mHeight) * cosTheta));
        Point p1 = new Point((((float) this.mWidth) * cosTheta) + (((float) this.mHeight) * sinTheta), (((float) this.mWidth) * sinTheta) - (((float) this.mHeight) * cosTheta));
        Point p2 = new Point(((-cosTheta) * ((float) this.mWidth)) - (((float) this.mHeight) * sinTheta), ((-sinTheta) * ((float) this.mWidth)) + (((float) this.mHeight) * cosTheta));
        Point p3 = new Point((((float) this.mWidth) * cosTheta) - (((float) this.mHeight) * sinTheta), (((float) this.mWidth) * sinTheta) + (((float) this.mHeight) * cosTheta));
        float scale = NetworkHistoryUtils.RECOVERY_PERCENTAGE * Math.min(((float) this.mWidth) / Math.max(Math.abs(p0.x), Math.abs(p1.x)), ((float) this.mHeight) / Math.max(Math.abs(p0.y), Math.abs(p1.y)));
        p0.set(((p0.x * scale) / ((float) this.mWidth)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE, ((p0.y * scale) / ((float) this.mHeight)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        p1.set(((p1.x * scale) / ((float) this.mWidth)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE, ((p1.y * scale) / ((float) this.mHeight)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        p2.set(((p2.x * scale) / ((float) this.mWidth)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE, ((p2.y * scale) / ((float) this.mHeight)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        p3.set(((p3.x * scale) / ((float) this.mWidth)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE, ((p3.y * scale) / ((float) this.mHeight)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        ((ShaderProgram) this.mProgram).setSourceRegion(new Quad(p0, p1, p2, p3));
    }
}

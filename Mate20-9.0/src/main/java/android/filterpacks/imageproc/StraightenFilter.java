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
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;

public class StraightenFilter extends Filter {
    private static final float DEGREE_TO_RADIAN = 0.017453292f;
    @GenerateFieldPort(hasDefault = true, name = "angle")
    private float mAngle = 0.0f;
    private int mHeight = 0;
    @GenerateFieldPort(hasDefault = true, name = "maxAngle")
    private float mMaxAngle = 45.0f;
    private Program mProgram;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;
    private int mWidth = 0;

    public StraightenFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3));
        addOutputBasedOnInput(SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE);
    }

    public void initProgram(FilterContext context, int target) {
        if (target == 3) {
            ShaderProgram shaderProgram = ShaderProgram.createIdentity(context);
            shaderProgram.setMaximumTileSize(this.mTileSize);
            this.mProgram = shaderProgram;
            this.mTarget = target;
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
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
            updateParameters();
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        this.mProgram.process(input, output);
        pushOutput(SliceItem.FORMAT_IMAGE, output);
        output.release();
    }

    private void updateParameters() {
        float cosTheta = (float) Math.cos((double) (this.mAngle * DEGREE_TO_RADIAN));
        float sinTheta = (float) Math.sin((double) (this.mAngle * DEGREE_TO_RADIAN));
        if (this.mMaxAngle > 0.0f) {
            float f = 90.0f;
            if (this.mMaxAngle <= 90.0f) {
                f = this.mMaxAngle;
            }
            this.mMaxAngle = f;
            Point p0 = new Point(((-cosTheta) * ((float) this.mWidth)) + (((float) this.mHeight) * sinTheta), ((-sinTheta) * ((float) this.mWidth)) - (((float) this.mHeight) * cosTheta));
            Point p1 = new Point((((float) this.mWidth) * cosTheta) + (((float) this.mHeight) * sinTheta), (((float) this.mWidth) * sinTheta) - (((float) this.mHeight) * cosTheta));
            Point p2 = new Point(((-cosTheta) * ((float) this.mWidth)) - (((float) this.mHeight) * sinTheta), ((-sinTheta) * ((float) this.mWidth)) + (((float) this.mHeight) * cosTheta));
            Point p3 = new Point((((float) this.mWidth) * cosTheta) - (((float) this.mHeight) * sinTheta), (((float) this.mWidth) * sinTheta) + (((float) this.mHeight) * cosTheta));
            float scale = Math.min(((float) this.mWidth) / Math.max(Math.abs(p0.x), Math.abs(p1.x)), ((float) this.mHeight) / Math.max(Math.abs(p0.y), Math.abs(p1.y))) * 0.5f;
            p0.set(((p0.x * scale) / ((float) this.mWidth)) + 0.5f, ((p0.y * scale) / ((float) this.mHeight)) + 0.5f);
            p1.set(((p1.x * scale) / ((float) this.mWidth)) + 0.5f, ((p1.y * scale) / ((float) this.mHeight)) + 0.5f);
            p2.set(((p2.x * scale) / ((float) this.mWidth)) + 0.5f, ((p2.y * scale) / ((float) this.mHeight)) + 0.5f);
            p3.set(((p3.x * scale) / ((float) this.mWidth)) + 0.5f, ((p3.y * scale) / ((float) this.mHeight)) + 0.5f);
            ((ShaderProgram) this.mProgram).setSourceRegion(new Quad(p0, p1, p2, p3));
            return;
        }
        throw new RuntimeException("Max angle is out of range (0-180).");
    }
}

package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.hardware.camera2.params.TonemapCurve;
import android.os.BatteryManager;

public class FisheyeFilter extends Filter {
    private static final String TAG = "FisheyeFilter";
    private static final String mFisheyeShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 scale;\nuniform float alpha;\nuniform float radius2;\nuniform float factor;\nvarying vec2 v_texcoord;\nvoid main() {\n  const float m_pi_2 = 1.570963;\n  vec2 coord = v_texcoord - vec2(0.5, 0.5);\n  float dist = length(coord * scale);\n  float radian = m_pi_2 - atan(alpha * sqrt(radius2 - dist * dist), dist);\n  float scalar = radian * factor / dist;\n  vec2 new_coord = coord * scalar + vec2(0.5, 0.5);\n  gl_FragColor = texture2D(tex_sampler_0, new_coord);\n}\n";
    private int mHeight = 0;
    private Program mProgram;
    @GenerateFieldPort(hasDefault = true, name = "scale")
    private float mScale = TonemapCurve.LEVEL_BLACK;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;
    private int mWidth = 0;

    public FisheyeFilter(String name) {
        super(name);
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
            case 3:
                ShaderProgram shaderProgram = new ShaderProgram(context, mFisheyeShader);
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mProgram = shaderProgram;
                this.mTarget = target;
                return;
            default:
                throw new RuntimeException("Filter FisheyeFilter does not support frames of target " + target + "!");
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        Frame output = context.getFrameManager().newFrame(inputFormat);
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            updateFrameSize(inputFormat.getWidth(), inputFormat.getHeight());
        }
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mProgram != null) {
            updateProgramParams();
        }
    }

    private void updateFrameSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        updateProgramParams();
    }

    private void updateProgramParams() {
        float[] scale = new float[2];
        if (this.mWidth > this.mHeight) {
            scale[0] = 1.0f;
            scale[1] = ((float) this.mHeight) / ((float) this.mWidth);
        } else {
            scale[0] = ((float) this.mWidth) / ((float) this.mHeight);
            scale[1] = 1.0f;
        }
        float alpha = (this.mScale * 2.0f) + 0.75f;
        float bound2 = 0.25f * ((scale[0] * scale[0]) + (scale[1] * scale[1]));
        float bound = (float) Math.sqrt((double) bound2);
        float radius = 1.15f * bound;
        float radius2 = radius * radius;
        float factor = bound / (1.5707964f - ((float) Math.atan((double) ((alpha / bound) * ((float) Math.sqrt((double) (radius2 - bound2)))))));
        this.mProgram.setHostValue(BatteryManager.EXTRA_SCALE, scale);
        this.mProgram.setHostValue("radius2", Float.valueOf(radius2));
        this.mProgram.setHostValue("factor", Float.valueOf(factor));
        this.mProgram.setHostValue("alpha", Float.valueOf(alpha));
    }
}

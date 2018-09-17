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

public class SaturateFilter extends Filter {
    private Program mBenProgram;
    private final String mBenSaturateShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float scale;\nuniform float shift;\nuniform vec3 weights;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float kv = dot(color.rgb, weights) + shift;\n  vec3 new_color = scale * color.rgb + (1.0 - scale) * kv;\n  gl_FragColor = vec4(new_color, color.a);\n}\n";
    private Program mHerfProgram;
    private final String mHerfSaturateShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec3 weights;\nuniform vec3 exponents;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float de = dot(color.rgb, weights);\n  float inv_de = 1.0 / de;\n  vec3 new_color = de * pow(color.rgb * inv_de, exponents);\n  float max_color = max(max(max(new_color.r, new_color.g), new_color.b), 1.0);\n  gl_FragColor = vec4(new_color / max_color, color.a);\n}\n";
    @GenerateFieldPort(hasDefault = true, name = "scale")
    private float mScale = TonemapCurve.LEVEL_BLACK;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;

    public SaturateFilter(String name) {
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
                ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float scale;\nuniform float shift;\nuniform vec3 weights;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float kv = dot(color.rgb, weights) + shift;\n  vec3 new_color = scale * color.rgb + (1.0 - scale) * kv;\n  gl_FragColor = vec4(new_color, color.a);\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mBenProgram = shaderProgram;
                shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec3 weights;\nuniform vec3 exponents;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float de = dot(color.rgb, weights);\n  float inv_de = 1.0 / de;\n  vec3 new_color = de * pow(color.rgb * inv_de, exponents);\n  float max_color = max(max(max(new_color.r, new_color.g), new_color.b), 1.0);\n  gl_FragColor = vec4(new_color / max_color, color.a);\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mHerfProgram = shaderProgram;
                this.mTarget = target;
                return;
            default:
                throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mBenProgram != null && this.mHerfProgram != null) {
            updateParameters();
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        if (this.mBenProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
            initParameters();
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        if (this.mScale > TonemapCurve.LEVEL_BLACK) {
            this.mHerfProgram.process(input, output);
        } else {
            this.mBenProgram.process(input, output);
        }
        pushOutput("image", output);
        output.release();
    }

    private void initParameters() {
        float[] weights = new float[]{0.25f, 0.625f, 0.125f};
        this.mBenProgram.setHostValue("weights", weights);
        this.mBenProgram.setHostValue("shift", Float.valueOf(0.003921569f));
        this.mHerfProgram.setHostValue("weights", weights);
        updateParameters();
    }

    private void updateParameters() {
        if (this.mScale > TonemapCurve.LEVEL_BLACK) {
            this.mHerfProgram.setHostValue("exponents", new float[]{(this.mScale * 0.9f) + 1.0f, (this.mScale * 2.1f) + 1.0f, (this.mScale * 2.7f) + 1.0f});
            return;
        }
        this.mBenProgram.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(this.mScale + 1.0f));
    }
}

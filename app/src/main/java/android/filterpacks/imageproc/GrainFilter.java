package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.BatteryManager;
import android.speech.tts.TextToSpeech.Engine;
import java.util.Date;
import java.util.Random;

public class GrainFilter extends Filter {
    private static final int RAND_THRESHOLD = 128;
    private Program mGrainProgram;
    private final String mGrainShader;
    private int mHeight;
    private Program mNoiseProgram;
    private final String mNoiseShader;
    private Random mRandom;
    @GenerateFieldPort(hasDefault = true, name = "strength")
    private float mScale;
    private int mTarget;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize;
    private int mWidth;

    public GrainFilter(String name) {
        super(name);
        this.mScale = 0.0f;
        this.mTileSize = 640;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mTarget = 0;
        this.mNoiseShader = "precision mediump float;\nuniform vec2 seed;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  float theta1 = dot(loc, vec2(0.9898, 0.233));\n  float theta2 = dot(loc, vec2(12.0, 78.0));\n  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n  float temp = mod(197.0 * value, 1.0) + value;\n  float part1 = mod(220.0 * temp, 1.0) + temp;\n  float part2 = value * 0.5453;\n  float part3 = cos(theta1 + theta2) * 0.43758;\n  return fract(part1 + part2 + part3);\n}\nvoid main() {\n  gl_FragColor = vec4(rand(v_texcoord + seed), 0.0, 0.0, 1.0);\n}\n";
        this.mGrainShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float scale;\nuniform float stepX;\nuniform float stepY;\nvarying vec2 v_texcoord;\nvoid main() {\n  float noise = texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, stepY)).r * 0.224;\n  noise += 0.4448;\n  noise *= scale;\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;\n  float mask = (1.0 - sqrt(energy));\n  float weight = 1.0 - 1.333 * mask * noise;\n  gl_FragColor = vec4(color.rgb * weight, color.a);\n}\n";
        this.mRandom = new Random(new Date().getTime());
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
                ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform vec2 seed;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  float theta1 = dot(loc, vec2(0.9898, 0.233));\n  float theta2 = dot(loc, vec2(12.0, 78.0));\n  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n  float temp = mod(197.0 * value, 1.0) + value;\n  float part1 = mod(220.0 * temp, 1.0) + temp;\n  float part2 = value * 0.5453;\n  float part3 = cos(theta1 + theta2) * 0.43758;\n  return fract(part1 + part2 + part3);\n}\nvoid main() {\n  gl_FragColor = vec4(rand(v_texcoord + seed), 0.0, 0.0, 1.0);\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mNoiseProgram = shaderProgram;
                shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float scale;\nuniform float stepX;\nuniform float stepY;\nvarying vec2 v_texcoord;\nvoid main() {\n  float noise = texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, stepY)).r * 0.224;\n  noise += 0.4448;\n  noise *= scale;\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;\n  float mask = (1.0 - sqrt(energy));\n  float weight = 1.0 - 1.333 * mask * noise;\n  gl_FragColor = vec4(color.rgb * weight, color.a);\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mGrainProgram = shaderProgram;
                this.mTarget = target;
            default:
                throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
        }
    }

    private void updateParameters() {
        this.mNoiseProgram.setHostValue("seed", new float[]{this.mRandom.nextFloat(), this.mRandom.nextFloat()});
        this.mGrainProgram.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(this.mScale));
    }

    private void updateFrameSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (this.mGrainProgram != null) {
            this.mGrainProgram.setHostValue("stepX", Float.valueOf(NetworkHistoryUtils.RECOVERY_PERCENTAGE / ((float) this.mWidth)));
            this.mGrainProgram.setHostValue("stepY", Float.valueOf(NetworkHistoryUtils.RECOVERY_PERCENTAGE / ((float) this.mHeight)));
            updateParameters();
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mGrainProgram != null && this.mNoiseProgram != null) {
            updateParameters();
        }
    }

    public void process(FilterContext context) {
        FrameFormat inputFormat = pullInput("image").getFormat();
        FrameFormat noiseFormat = ImageFormat.create(inputFormat.getWidth() / 2, inputFormat.getHeight() / 2, 3, 3);
        Frame noiseFrame = context.getFrameManager().newFrame(inputFormat);
        Frame output = context.getFrameManager().newFrame(inputFormat);
        if (!(this.mNoiseProgram == null || this.mGrainProgram == null)) {
            if (inputFormat.getTarget() != this.mTarget) {
            }
            if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
                updateFrameSize(inputFormat.getWidth(), inputFormat.getHeight());
            }
            this.mNoiseProgram.process(new Frame[0], noiseFrame);
            this.mGrainProgram.process(new Frame[]{input, noiseFrame}, output);
            pushOutput("image", output);
            output.release();
            noiseFrame.release();
        }
        initProgram(context, inputFormat.getTarget());
        updateParameters();
        updateFrameSize(inputFormat.getWidth(), inputFormat.getHeight());
        this.mNoiseProgram.process(new Frame[0], noiseFrame);
        this.mGrainProgram.process(new Frame[]{input, noiseFrame}, output);
        pushOutput("image", output);
        output.release();
        noiseFrame.release();
    }
}

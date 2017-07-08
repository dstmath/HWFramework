package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.os.BatteryManager;
import android.speech.tts.TextToSpeech.Engine;
import java.util.Date;
import java.util.Random;

public class BlackWhiteFilter extends Filter {
    @GenerateFieldPort(hasDefault = true, name = "black")
    private float mBlack;
    private final String mBlackWhiteShader;
    private Program mProgram;
    private Random mRandom;
    private int mTarget;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize;
    @GenerateFieldPort(hasDefault = true, name = "white")
    private float mWhite;

    public BlackWhiteFilter(String name) {
        super(name);
        this.mBlack = 0.0f;
        this.mWhite = Engine.DEFAULT_VOLUME;
        this.mTileSize = 640;
        this.mTarget = 0;
        this.mBlackWhiteShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 seed;\nuniform float black;\nuniform float scale;\nuniform float stepsize;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  float theta1 = dot(loc, vec2(0.9898, 0.233));\n  float theta2 = dot(loc, vec2(12.0, 78.0));\n  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n  float temp = mod(197.0 * value, 1.0) + value;\n  float part1 = mod(220.0 * temp, 1.0) + temp;\n  float part2 = value * 0.5453;\n  float part3 = cos(theta1 + theta2) * 0.43758;\n  return fract(part1 + part2 + part3);\n}\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float dither = rand(v_texcoord + seed);\n  vec3 xform = clamp((color.rgb - black) * scale, 0.0, 1.0);\n  vec3 temp = clamp((color.rgb + stepsize - black) * scale, 0.0, 1.0);\n  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n  gl_FragColor = vec4(new_color, color.a);\n}\n";
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
                ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 seed;\nuniform float black;\nuniform float scale;\nuniform float stepsize;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  float theta1 = dot(loc, vec2(0.9898, 0.233));\n  float theta2 = dot(loc, vec2(12.0, 78.0));\n  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n  float temp = mod(197.0 * value, 1.0) + value;\n  float part1 = mod(220.0 * temp, 1.0) + temp;\n  float part2 = value * 0.5453;\n  float part3 = cos(theta1 + theta2) * 0.43758;\n  return fract(part1 + part2 + part3);\n}\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float dither = rand(v_texcoord + seed);\n  vec3 xform = clamp((color.rgb - black) * scale, 0.0, 1.0);\n  vec3 temp = clamp((color.rgb + stepsize - black) * scale, 0.0, 1.0);\n  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n  gl_FragColor = vec4(new_color, color.a);\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mProgram = shaderProgram;
                updateParameters();
                this.mTarget = target;
            default:
                throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
        }
    }

    private void updateParameters() {
        float scale = this.mBlack != this.mWhite ? Engine.DEFAULT_VOLUME / (this.mWhite - this.mBlack) : 2000.0f;
        this.mProgram.setHostValue("black", Float.valueOf(this.mBlack));
        this.mProgram.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(scale));
        this.mProgram.setHostValue("stepsize", Float.valueOf(0.003921569f));
        this.mProgram.setHostValue("seed", new float[]{this.mRandom.nextFloat(), this.mRandom.nextFloat()});
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
}

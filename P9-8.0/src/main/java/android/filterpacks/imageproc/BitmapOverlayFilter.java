package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.graphics.Bitmap;

public class BitmapOverlayFilter extends Filter {
    @GenerateFieldPort(name = "bitmap")
    private Bitmap mBitmap;
    private Frame mFrame;
    private final String mOverlayShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 original = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  gl_FragColor = vec4(original.rgb * (1.0 - mask.a) + mask.rgb, 1.0);\n}\n";
    private Program mProgram;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;

    public BitmapOverlayFilter(String name) {
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
                ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 original = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  gl_FragColor = vec4(original.rgb * (1.0 - mask.a) + mask.rgb, 1.0);\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mProgram = shaderProgram;
                this.mTarget = target;
                return;
            default:
                throw new RuntimeException("Filter FisheyeFilter does not support frames of target " + target + "!");
        }
    }

    public void tearDown(FilterContext context) {
        if (this.mFrame != null) {
            this.mFrame.release();
            this.mFrame = null;
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        Frame output = context.getFrameManager().newFrame(inputFormat);
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        if (this.mBitmap != null) {
            this.mProgram.process(new Frame[]{input, createBitmapFrame(context)}, output);
            frame.release();
        } else {
            output.setDataFromFrame(input);
        }
        pushOutput("image", output);
        output.release();
    }

    private Frame createBitmapFrame(FilterContext context) {
        Frame frame = context.getFrameManager().newFrame(ImageFormat.create(this.mBitmap.getWidth(), this.mBitmap.getHeight(), 3, 3));
        frame.setBitmap(this.mBitmap);
        this.mBitmap.recycle();
        this.mBitmap = null;
        return frame;
    }
}

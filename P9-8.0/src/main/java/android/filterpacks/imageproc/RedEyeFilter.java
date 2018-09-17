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
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;

public class RedEyeFilter extends Filter {
    private static final float DEFAULT_RED_INTENSITY = 1.3f;
    private static final float MIN_RADIUS = 10.0f;
    private static final float RADIUS_RATIO = 0.06f;
    private final Canvas mCanvas = new Canvas();
    @GenerateFieldPort(name = "centers")
    private float[] mCenters;
    private int mHeight = 0;
    private final Paint mPaint = new Paint();
    private Program mProgram;
    private float mRadius;
    private Bitmap mRedEyeBitmap;
    private Frame mRedEyeFrame;
    private final String mRedEyeShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float intensity;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  if (mask.a > 0.0) {\n    float green_blue = color.g + color.b;\n    float red_intensity = color.r / green_blue;\n    if (red_intensity > intensity) {\n      color.r = 0.5 * green_blue;\n    }\n  }\n  gl_FragColor = color;\n}\n";
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;
    private int mWidth = 0;

    public RedEyeFilter(String name) {
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
                ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float intensity;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  if (mask.a > 0.0) {\n    float green_blue = color.g + color.b;\n    float red_intensity = color.r / green_blue;\n    if (red_intensity > intensity) {\n      color.r = 0.5 * green_blue;\n    }\n  }\n  gl_FragColor = color;\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mProgram = shaderProgram;
                this.mProgram.setHostValue("intensity", Float.valueOf(DEFAULT_RED_INTENSITY));
                this.mTarget = target;
                return;
            default:
                throw new RuntimeException("Filter RedEye does not support frames of target " + target + "!");
        }
    }

    public void process(FilterContext context) {
        FrameFormat inputFormat = pullInput("image").getFormat();
        Frame output = context.getFrameManager().newFrame(inputFormat);
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
        }
        createRedEyeFrame(context);
        this.mProgram.process(new Frame[]{input, this.mRedEyeFrame}, output);
        pushOutput("image", output);
        output.release();
        this.mRedEyeFrame.release();
        this.mRedEyeFrame = null;
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mProgram != null) {
            updateProgramParams();
        }
    }

    private void createRedEyeFrame(FilterContext context) {
        int bitmapWidth = this.mWidth / 2;
        int bitmapHeight = this.mHeight / 2;
        Bitmap redEyeBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        this.mCanvas.setBitmap(redEyeBitmap);
        this.mPaint.setColor(-1);
        this.mRadius = Math.max(MIN_RADIUS, ((float) Math.min(bitmapWidth, bitmapHeight)) * RADIUS_RATIO);
        for (int i = 0; i < this.mCenters.length; i += 2) {
            this.mCanvas.drawCircle(this.mCenters[i] * ((float) bitmapWidth), this.mCenters[i + 1] * ((float) bitmapHeight), this.mRadius, this.mPaint);
        }
        this.mRedEyeFrame = context.getFrameManager().newFrame(ImageFormat.create(bitmapWidth, bitmapHeight, 3, 3));
        this.mRedEyeFrame.setBitmap(redEyeBitmap);
        redEyeBitmap.recycle();
    }

    private void updateProgramParams() {
        if (this.mCenters.length % 2 == 1) {
            throw new RuntimeException("The size of center array must be even.");
        }
    }
}

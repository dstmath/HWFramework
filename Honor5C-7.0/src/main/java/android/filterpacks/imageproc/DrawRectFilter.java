package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.geometry.Quad;
import android.opengl.GLES20;
import android.speech.tts.TextToSpeech.Engine;

public class DrawRectFilter extends Filter {
    @GenerateFieldPort(hasDefault = true, name = "colorBlue")
    private float mColorBlue;
    @GenerateFieldPort(hasDefault = true, name = "colorGreen")
    private float mColorGreen;
    @GenerateFieldPort(hasDefault = true, name = "colorRed")
    private float mColorRed;
    private final String mFixedColorFragmentShader;
    private ShaderProgram mProgram;
    private final String mVertexShader;

    public DrawRectFilter(String name) {
        super(name);
        this.mColorRed = 0.8f;
        this.mColorGreen = 0.8f;
        this.mColorBlue = 0.0f;
        this.mVertexShader = "attribute vec4 aPosition;\nvoid main() {\n  gl_Position = aPosition;\n}\n";
        this.mFixedColorFragmentShader = "precision mediump float;\nuniform vec4 color;\nvoid main() {\n  gl_FragColor = color;\n}\n";
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3, 3));
        addMaskedInputPort("box", ObjectFormat.fromClass(Quad.class, 1));
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void prepare(FilterContext context) {
        this.mProgram = new ShaderProgram(context, "attribute vec4 aPosition;\nvoid main() {\n  gl_Position = aPosition;\n}\n", "precision mediump float;\nuniform vec4 color;\nvoid main() {\n  gl_FragColor = color;\n}\n");
    }

    public void process(FilterContext env) {
        Frame imageFrame = pullInput("image");
        Quad box = ((Quad) pullInput("box").getObjectValue()).scaled(2.0f).translated(ScaledLayoutParams.SCALE_UNSPECIFIED, ScaledLayoutParams.SCALE_UNSPECIFIED);
        GLFrame output = (GLFrame) env.getFrameManager().duplicateFrame(imageFrame);
        output.focus();
        renderBox(box);
        pushOutput("image", output);
        output.release();
    }

    private void renderBox(Quad box) {
        float[] vertexValues = new float[]{box.p0.x, box.p0.y, box.p1.x, box.p1.y, box.p3.x, box.p3.y, box.p2.x, box.p2.y};
        this.mProgram.setHostValue(ColorsColumns.COLOR, new float[]{this.mColorRed, this.mColorGreen, this.mColorBlue, Engine.DEFAULT_VOLUME});
        this.mProgram.setAttributeValues("aPosition", vertexValues, 2);
        this.mProgram.setVertexCount(4);
        this.mProgram.beginDrawing();
        GLES20.glLineWidth(Engine.DEFAULT_VOLUME);
        GLES20.glDrawArrays(2, 0, 4);
    }
}

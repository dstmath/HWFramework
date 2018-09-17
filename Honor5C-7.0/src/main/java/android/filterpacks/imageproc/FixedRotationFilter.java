package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public class FixedRotationFilter extends Filter {
    private ShaderProgram mProgram;
    @GenerateFieldPort(hasDefault = true, name = "rotation")
    private int mRotation;

    public FixedRotationFilter(String name) {
        super(name);
        this.mRotation = 0;
        this.mProgram = null;
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3, 3));
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        if (this.mRotation == 0) {
            pushOutput("image", input);
            return;
        }
        Quad sourceRegion;
        FrameFormat inputFormat = input.getFormat();
        if (this.mProgram == null) {
            this.mProgram = ShaderProgram.createIdentity(context);
        }
        MutableFrameFormat outputFormat = inputFormat.mutableCopy();
        int width = inputFormat.getWidth();
        int height = inputFormat.getHeight();
        Point p1 = new Point(0.0f, 0.0f);
        Point p2 = new Point(Engine.DEFAULT_VOLUME, 0.0f);
        Point p3 = new Point(0.0f, Engine.DEFAULT_VOLUME);
        Point p4 = new Point(Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME);
        switch (Math.round(((float) this.mRotation) / 90.0f) % 4) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                sourceRegion = new Quad(p3, p1, p4, p2);
                outputFormat.setDimensions(height, width);
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                sourceRegion = new Quad(p4, p3, p2, p1);
                break;
            case Engine.DEFAULT_STREAM /*3*/:
                sourceRegion = new Quad(p2, p4, p1, p3);
                outputFormat.setDimensions(height, width);
                break;
            default:
                sourceRegion = new Quad(p1, p2, p3, p4);
                break;
        }
        Frame output = context.getFrameManager().newFrame(outputFormat);
        this.mProgram.setSourceRegion(sourceRegion);
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }
}

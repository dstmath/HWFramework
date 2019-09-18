package android.media.effect;

import android.filterfw.core.Frame;

public class SizeChangeEffect extends SingleFilterEffect {
    public SizeChangeEffect(EffectContext context, String name, Class filterClass, String inputName, String outputName, Object... finalParameters) {
        super(context, name, filterClass, inputName, outputName, finalParameters);
    }

    public void apply(int inputTexId, int width, int height, int outputTexId) {
        beginGLEffect();
        Frame inputFrame = frameFromTexture(inputTexId, width, height);
        Frame resultFrame = this.mFunction.executeWithArgList(this.mInputName, inputFrame);
        Frame outputFrame = frameFromTexture(outputTexId, resultFrame.getFormat().getWidth(), resultFrame.getFormat().getHeight());
        outputFrame.setDataFromFrame(resultFrame);
        inputFrame.release();
        outputFrame.release();
        resultFrame.release();
        endGLEffect();
    }
}

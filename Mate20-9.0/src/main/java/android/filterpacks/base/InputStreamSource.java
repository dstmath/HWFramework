package android.filterpacks.base;

import android.app.ActivityManagerInternal;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.format.PrimitiveFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class InputStreamSource extends Filter {
    @GenerateFieldPort(name = "stream")
    private InputStream mInputStream;
    @GenerateFinalPort(hasDefault = true, name = "format")
    private MutableFrameFormat mOutputFormat = null;
    @GenerateFinalPort(name = "target")
    private String mTarget;

    public InputStreamSource(String name) {
        super(name);
    }

    public void setupPorts() {
        int target = FrameFormat.readTargetString(this.mTarget);
        if (this.mOutputFormat == null) {
            this.mOutputFormat = PrimitiveFormat.createByteFormat(target);
        }
        addOutputPort(ActivityManagerInternal.ASSIST_KEY_DATA, this.mOutputFormat);
    }

    public void process(FilterContext context) {
        int fileSize = 0;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int read = this.mInputStream.read(buffer);
                int bytesRead = read;
                if (read > 0) {
                    byteStream.write(buffer, 0, bytesRead);
                    fileSize += bytesRead;
                } else {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(byteStream.toByteArray());
                    this.mOutputFormat.setDimensions(fileSize);
                    Frame output = context.getFrameManager().newFrame(this.mOutputFormat);
                    output.setData(byteBuffer);
                    pushOutput(ActivityManagerInternal.ASSIST_KEY_DATA, output);
                    output.release();
                    closeOutputPort(ActivityManagerInternal.ASSIST_KEY_DATA);
                    return;
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("InputStreamSource: Could not read stream: " + exception.getMessage() + "!");
        }
    }
}

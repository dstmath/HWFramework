package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ImageFormat;
import android.graphics.Bitmap;

public class BitmapSource extends Filter {
    @GenerateFieldPort(name = "bitmap")
    private Bitmap mBitmap;
    private Frame mImageFrame;
    @GenerateFieldPort(hasDefault = true, name = "recycleBitmap")
    private boolean mRecycleBitmap = true;
    @GenerateFieldPort(hasDefault = true, name = "repeatFrame")
    boolean mRepeatFrame = false;
    private int mTarget;
    @GenerateFieldPort(name = "target")
    String mTargetString;

    public BitmapSource(String name) {
        super(name);
    }

    public void setupPorts() {
        addOutputPort("image", ImageFormat.create(3, 0));
    }

    public void loadImage(FilterContext filterContext) {
        this.mTarget = FrameFormat.readTargetString(this.mTargetString);
        this.mImageFrame = filterContext.getFrameManager().newFrame(ImageFormat.create(this.mBitmap.getWidth(), this.mBitmap.getHeight(), 3, this.mTarget));
        this.mImageFrame.setBitmap(this.mBitmap);
        this.mImageFrame.setTimestamp(-1);
        if (this.mRecycleBitmap) {
            this.mBitmap.recycle();
        }
        this.mBitmap = null;
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if ((name.equals("bitmap") || name.equals("target")) && this.mImageFrame != null) {
            this.mImageFrame.release();
            this.mImageFrame = null;
        }
    }

    public void process(FilterContext context) {
        if (this.mImageFrame == null) {
            loadImage(context);
        }
        pushOutput("image", this.mImageFrame);
        if (!this.mRepeatFrame) {
            closeOutputPort("image");
        }
    }

    public void tearDown(FilterContext env) {
        if (this.mImageFrame != null) {
            this.mImageFrame.release();
            this.mImageFrame = null;
        }
    }
}

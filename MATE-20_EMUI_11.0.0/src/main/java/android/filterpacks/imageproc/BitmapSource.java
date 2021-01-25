package android.filterpacks.imageproc;

import android.app.slice.SliceItem;
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

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addOutputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3, 0));
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

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        Frame frame;
        if ((name.equals("bitmap") || name.equals("target")) && (frame = this.mImageFrame) != null) {
            frame.release();
            this.mImageFrame = null;
        }
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        if (this.mImageFrame == null) {
            loadImage(context);
        }
        pushOutput(SliceItem.FORMAT_IMAGE, this.mImageFrame);
        if (!this.mRepeatFrame) {
            closeOutputPort(SliceItem.FORMAT_IMAGE);
        }
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext env) {
        Frame frame = this.mImageFrame;
        if (frame != null) {
            frame.release();
            this.mImageFrame = null;
        }
    }
}

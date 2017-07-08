package android.text.style;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import java.io.InputStream;

public class ImageSpan extends DynamicDrawableSpan {
    private Uri mContentUri;
    private Context mContext;
    private Drawable mDrawable;
    private int mResourceId;
    private String mSource;

    @Deprecated
    public ImageSpan(Bitmap b) {
        this(null, b, 0);
    }

    @Deprecated
    public ImageSpan(Bitmap b, int verticalAlignment) {
        this(null, b, verticalAlignment);
    }

    public ImageSpan(Context context, Bitmap b) {
        this(context, b, 0);
    }

    public ImageSpan(Context context, Bitmap b, int verticalAlignment) {
        Drawable bitmapDrawable;
        super(verticalAlignment);
        this.mContext = context;
        if (context != null) {
            bitmapDrawable = new BitmapDrawable(context.getResources(), b);
        } else {
            bitmapDrawable = new BitmapDrawable(b);
        }
        this.mDrawable = bitmapDrawable;
        int width = this.mDrawable.getIntrinsicWidth();
        int height = this.mDrawable.getIntrinsicHeight();
        bitmapDrawable = this.mDrawable;
        if (width <= 0) {
            width = 0;
        }
        if (height <= 0) {
            height = 0;
        }
        bitmapDrawable.setBounds(0, 0, width, height);
    }

    public ImageSpan(Drawable d) {
        this(d, 0);
    }

    public ImageSpan(Drawable d, int verticalAlignment) {
        super(verticalAlignment);
        this.mDrawable = d;
    }

    public ImageSpan(Drawable d, String source) {
        this(d, source, 0);
    }

    public ImageSpan(Drawable d, String source, int verticalAlignment) {
        super(verticalAlignment);
        this.mDrawable = d;
        this.mSource = source;
    }

    public ImageSpan(Context context, Uri uri) {
        this(context, uri, 0);
    }

    public ImageSpan(Context context, Uri uri, int verticalAlignment) {
        super(verticalAlignment);
        this.mContext = context;
        this.mContentUri = uri;
        this.mSource = uri.toString();
    }

    public ImageSpan(Context context, int resourceId) {
        this(context, resourceId, 0);
    }

    public ImageSpan(Context context, int resourceId, int verticalAlignment) {
        super(verticalAlignment);
        this.mContext = context;
        this.mResourceId = resourceId;
    }

    public Drawable getDrawable() {
        Exception e;
        Drawable drawable = null;
        if (this.mDrawable != null) {
            return this.mDrawable;
        }
        if (this.mContentUri != null) {
            try {
                InputStream is = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                Drawable drawable2 = new BitmapDrawable(this.mContext.getResources(), BitmapFactory.decodeStream(is));
                try {
                    drawable2.setBounds(0, 0, drawable2.getIntrinsicWidth(), drawable2.getIntrinsicHeight());
                    is.close();
                    return drawable2;
                } catch (Exception e2) {
                    e = e2;
                    drawable = drawable2;
                    Log.e("sms", "Failed to loaded content " + this.mContentUri, e);
                    return drawable;
                }
            } catch (Exception e3) {
                e = e3;
                Log.e("sms", "Failed to loaded content " + this.mContentUri, e);
                return drawable;
            }
        }
        try {
            drawable = this.mContext.getDrawable(this.mResourceId);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        } catch (Exception e4) {
            Log.e("sms", "Unable to find resource: " + this.mResourceId);
            return drawable;
        }
    }

    public String getSource() {
        return this.mSource;
    }
}

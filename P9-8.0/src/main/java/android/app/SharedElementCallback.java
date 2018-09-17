package android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.TransitionUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.List;
import java.util.Map;

public abstract class SharedElementCallback {
    private static final String BUNDLE_SNAPSHOT_BITMAP = "sharedElement:snapshot:bitmap";
    private static final String BUNDLE_SNAPSHOT_IMAGE_MATRIX = "sharedElement:snapshot:imageMatrix";
    private static final String BUNDLE_SNAPSHOT_IMAGE_SCALETYPE = "sharedElement:snapshot:imageScaleType";
    static final SharedElementCallback NULL_CALLBACK = new SharedElementCallback() {
    };
    private Matrix mTempMatrix;

    public interface OnSharedElementsReadyListener {
        void onSharedElementsReady();
    }

    public void onSharedElementStart(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onSharedElementEnd(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onRejectSharedElements(List<View> list) {
    }

    public void onMapSharedElements(List<String> list, Map<String, View> map) {
    }

    public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
        if (sharedElement instanceof ImageView) {
            ImageView imageView = (ImageView) sharedElement;
            Drawable d = imageView.getDrawable();
            Drawable bg = imageView.getBackground();
            if (d != null && (bg == null || bg.getAlpha() == 0)) {
                Bitmap bitmap = TransitionUtils.createDrawableBitmap(d);
                if (bitmap != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(BUNDLE_SNAPSHOT_BITMAP, bitmap);
                    bundle.putString(BUNDLE_SNAPSHOT_IMAGE_SCALETYPE, imageView.getScaleType().toString());
                    if (imageView.getScaleType() == ScaleType.MATRIX) {
                        float[] values = new float[9];
                        imageView.getImageMatrix().getValues(values);
                        bundle.putFloatArray(BUNDLE_SNAPSHOT_IMAGE_MATRIX, values);
                    }
                    return bundle;
                }
            }
        }
        if (this.mTempMatrix == null) {
            this.mTempMatrix = new Matrix(viewToGlobalMatrix);
        } else {
            this.mTempMatrix.set(viewToGlobalMatrix);
        }
        return TransitionUtils.createViewBitmap(sharedElement, this.mTempMatrix, screenBounds);
    }

    public View onCreateSnapshotView(Context context, Parcelable snapshot) {
        View view = null;
        Bitmap bitmap;
        if (snapshot instanceof Bundle) {
            Bundle bundle = (Bundle) snapshot;
            bitmap = (Bitmap) bundle.getParcelable(BUNDLE_SNAPSHOT_BITMAP);
            if (bitmap == null) {
                return null;
            }
            View imageView = new ImageView(context);
            view = imageView;
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ScaleType.valueOf(bundle.getString(BUNDLE_SNAPSHOT_IMAGE_SCALETYPE)));
            if (imageView.getScaleType() == ScaleType.MATRIX) {
                float[] values = bundle.getFloatArray(BUNDLE_SNAPSHOT_IMAGE_MATRIX);
                Matrix matrix = new Matrix();
                matrix.setValues(values);
                imageView.setImageMatrix(matrix);
            }
        } else if (snapshot instanceof Bitmap) {
            bitmap = (Bitmap) snapshot;
            view = new View(context);
            view.setBackground(new BitmapDrawable(context.getResources(), bitmap));
        }
        return view;
    }

    public void onSharedElementsArrived(List<String> list, List<View> list2, OnSharedElementsReadyListener listener) {
        listener.onSharedElementsReady();
    }
}

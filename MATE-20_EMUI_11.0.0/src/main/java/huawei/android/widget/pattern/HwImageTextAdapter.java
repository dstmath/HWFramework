package huawei.android.widget.pattern;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.internal.widget.PagerAdapter;
import java.util.ArrayList;

class HwImageTextAdapter extends PagerAdapter {
    private static final int INITIAL_CAPACITY_SIZE = 10;
    private ArrayList<HwImageTextBean> mBeanList;
    private Context mContext;
    private ArrayList<ImageView> mImageViews = new ArrayList<>((int) INITIAL_CAPACITY_SIZE);
    private ImageView.ScaleType mScaleType;

    HwImageTextAdapter(Context context, ArrayList<HwImageTextBean> hwImageTextBeans, ImageView.ScaleType scaleType) {
        this.mContext = context;
        this.mBeanList = hwImageTextBeans;
        this.mScaleType = scaleType;
    }

    public int getCount() {
        ArrayList<HwImageTextBean> arrayList = this.mBeanList;
        if (arrayList == null) {
            return 0;
        }
        return arrayList.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView;
        if (this.mImageViews.size() == 0) {
            imageView = new ImageView(this.mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        } else {
            imageView = this.mImageViews.remove(0);
        }
        imageView.setScaleType(this.mScaleType);
        if (position >= 0 && this.mBeanList.size() > position) {
            imageView.setImageDrawable(this.mBeanList.get(position).mDrawable);
        }
        container.addView(imageView);
        return imageView;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof ImageView) {
            ImageView imageView = (ImageView) object;
            container.removeView(imageView);
            this.mImageViews.add(imageView);
        }
    }
}

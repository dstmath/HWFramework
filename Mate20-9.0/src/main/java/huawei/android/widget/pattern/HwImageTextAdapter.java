package huawei.android.widget.pattern;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.internal.widget.PagerAdapter;
import java.util.ArrayList;

class HwImageTextAdapter extends PagerAdapter {
    private ArrayList<HwImageTextBean> mBeanList;
    private Context mContext;
    private ImageView.ScaleType mScaleType;
    private ArrayList<ImageView> mViewCache = new ArrayList<>();

    HwImageTextAdapter(Context context, ArrayList<HwImageTextBean> hwImageTextBeans, ImageView.ScaleType scaleType) {
        this.mContext = context;
        this.mBeanList = hwImageTextBeans;
        this.mScaleType = scaleType;
    }

    public int getCount() {
        if (this.mBeanList == null) {
            return 0;
        }
        return this.mBeanList.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView;
        if (this.mViewCache.size() == 0) {
            imageView = new ImageView(this.mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        } else {
            imageView = this.mViewCache.remove(0);
        }
        imageView.setScaleType(this.mScaleType);
        imageView.setImageDrawable(this.mBeanList.get(position).mDrawable);
        container.addView(imageView);
        return imageView;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
        this.mViewCache.add((ImageView) object);
    }
}

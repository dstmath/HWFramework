package android.freeform.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.PagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class FloatPagerAdapter extends PagerAdapter {
    private List<View> viewList = new ArrayList();

    public FloatPagerAdapter(List<View> data) {
        this.viewList = data;
    }

    public int getCount() {
        return this.viewList.size();
    }

    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(this.viewList.get(position));
        return this.viewList.get(position);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(this.viewList.get(position));
    }
}

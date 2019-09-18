package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.List;

public class HwGridPatternView {
    public static final int LAYOUT_STYLE_LARGE = 1;
    public static final int LAYOUT_STYLE_LARGE_DOUBLE_SUBTITLE = 3;
    public static final int LAYOUT_STYLE_MEDIUM = 2;
    private HwGridPatternViewAdapter<HwGridPatternViewBean> adapter;
    /* access modifiers changed from: private */
    public Context mContext;

    private abstract class HwGridPatternViewAdapter<T> extends BaseAdapter {
        private Context context;
        private int itemLayoutId;
        private List<T> list;
        private ViewHolder viewHolder;

        public abstract void convert(ViewHolder viewHolder2, T t);

        public HwGridPatternViewAdapter(Context context2, List<T> list2, int itemLayoutId2) {
            this.context = context2;
            this.list = list2;
            this.itemLayoutId = itemLayoutId2;
        }

        public int getCount() {
            if (this.list == null) {
                return 0;
            }
            return this.list.size();
        }

        public T getItem(int position) {
            if (this.list == null || position >= this.list.size()) {
                return null;
            }
            return this.list.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(this.context, parent, this.itemLayoutId, position);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convert(holder, getItem(position));
            return holder.getConvertView();
        }
    }

    private class ViewHolder {
        private Context context;
        private View convertView;
        private final SparseArray<View> views = new SparseArray<>();

        public ViewHolder(Context context2, ViewGroup parent, int itemLayoutId, int position) {
            this.context = context2;
            this.convertView = LayoutInflater.from(context2).inflate(itemLayoutId, parent, false);
            if (this.convertView != null) {
                this.convertView.setTag(this);
            }
        }

        public <T extends View> T getView(int viewId) {
            View view = this.views.get(viewId);
            if (view == null && this.convertView != null) {
                view = this.convertView.findViewById(viewId);
                if (view != null) {
                    this.views.put(viewId, view);
                }
            }
            return view;
        }

        public View getConvertView() {
            return this.convertView;
        }

        public ViewHolder setText(int viewId, String text) {
            TextView tv = (TextView) getView(viewId);
            if (tv != null) {
                tv.setText(text);
            }
            return this;
        }

        public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
            View view = getView(viewId);
            if (view != null) {
                view.setOnClickListener(listener);
            }
            return this;
        }

        public ViewHolder setImageDrawable(int viewId, Drawable drawable) {
            ImageView iv = (ImageView) getView(viewId);
            if (iv != null) {
                iv.setImageDrawable(drawable);
            }
            return this;
        }

        public ViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
            ImageView iv = (ImageView) getView(viewId);
            if (iv != null) {
                iv.setImageBitmap(bitmap);
            }
            return this;
        }
    }

    public HwGridPatternView(Context context) {
        this.mContext = context;
    }

    public void setData(List<HwGridPatternViewBean> datas, GridView gridView, int layoutStyle) {
        String layoutName = "hwpattern_gridpattern_l";
        switch (layoutStyle) {
            case 1:
                layoutName = "hwpattern_gridpattern_l";
                break;
            case 2:
                layoutName = "hwpattern_gridpattern_m";
                break;
            case 3:
                layoutName = "hwpattern_gridpattern_double_subtitle";
                break;
        }
        this.adapter = new HwGridPatternViewAdapter<HwGridPatternViewBean>(this.mContext, datas, ResLoaderUtil.getLayoutId(this.mContext, layoutName)) {
            public void convert(ViewHolder holder, HwGridPatternViewBean gridBean) {
                if (holder != null && gridBean != null) {
                    int playBackViewId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_play_back");
                    int playIconId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_play_icon");
                    int TitleIconId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_title_icon");
                    holder.setImageDrawable(playBackViewId, gridBean.getPlayBackDrawable());
                    holder.setImageDrawable(playIconId, gridBean.getPlayIconDrawable());
                    holder.setImageDrawable(TitleIconId, gridBean.getTitleIconDrawable());
                    int titleId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_title");
                    int subtititleId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle");
                    int secondSubtitleId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle_second");
                    int buttonId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_button");
                    holder.setText(titleId, gridBean.getTitle());
                    holder.setText(subtititleId, gridBean.getSubTitle());
                    holder.setText(secondSubtitleId, gridBean.getSubTitleSecond());
                    holder.setText(buttonId, gridBean.getButtonText());
                    holder.setOnClickListener(buttonId, gridBean.getButtonListener());
                    if (!TextUtils.isEmpty(gridBean.getSubTitleSecond()) && !TextUtils.isEmpty(gridBean.getSubTitle())) {
                        TextView subTitleTv = (TextView) holder.getView(subtititleId);
                        subTitleTv.post(HwGridPatternView.this.getAction(holder, gridBean, subTitleTv, (TextView) holder.getView(secondSubtitleId)));
                    }
                }
            }
        };
        if (gridView != null) {
            gridView.setAdapter(this.adapter);
        }
    }

    /* access modifiers changed from: private */
    public Runnable getAction(ViewHolder holder, HwGridPatternViewBean gridBean, TextView subTitleTv, TextView subTitleSecondTv) {
        final TextView textView = subTitleTv;
        final TextView textView2 = subTitleSecondTv;
        final ViewHolder viewHolder = holder;
        final HwGridPatternViewBean hwGridPatternViewBean = gridBean;
        AnonymousClass2 r0 = new Runnable() {
            public void run() {
                int subtitleLayoutId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle_layout");
                int dividingId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle_dividing");
                if (textView != null && textView2 != null) {
                    View subtitleLayout = viewHolder.getView(subtitleLayoutId);
                    View dividingView = viewHolder.getView(dividingId);
                    if (subtitleLayout != null) {
                        int dividingWidth = 0;
                        if (dividingView != null) {
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) dividingView.getLayoutParams();
                            dividingWidth = dividingView.getWidth() + layoutParams.getMarginStart() + layoutParams.getMarginEnd();
                        }
                        TextPaint textPaint = new TextPaint(textView.getPaint());
                        float subTitleTvWidth = textPaint.measureText(hwGridPatternViewBean.getSubTitle());
                        float subTitleSecondTvWidth = textPaint.measureText(hwGridPatternViewBean.getSubTitleSecond());
                        float availableWidth = (float) (subtitleLayout.getMeasuredWidth() - dividingWidth);
                        float halfAvailableWidth = availableWidth / 2.0f;
                        if (subTitleTvWidth + subTitleSecondTvWidth <= availableWidth) {
                            textView.setWidth((int) subTitleTvWidth);
                        } else if (subTitleTvWidth < halfAvailableWidth) {
                            textView.setWidth((int) subTitleTvWidth);
                        } else if (subTitleSecondTvWidth < halfAvailableWidth) {
                            textView.setWidth((int) (availableWidth - subTitleSecondTvWidth));
                        } else {
                            textView.setWidth((int) halfAvailableWidth);
                        }
                    }
                }
            }
        };
        return r0;
    }
}

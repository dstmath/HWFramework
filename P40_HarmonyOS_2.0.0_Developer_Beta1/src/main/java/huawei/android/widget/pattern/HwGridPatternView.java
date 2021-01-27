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
import android.widget.ListAdapter;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.List;

public class HwGridPatternView {
    public static final int LAYOUT_STYLE_LARGE = 1;
    public static final int LAYOUT_STYLE_LARGE_DOUBLE_SUBTITLE = 3;
    public static final int LAYOUT_STYLE_MEDIUM = 2;
    private static final int WIDTH_DIV = 2;
    private HwGridPatternViewAdapter<HwGridPatternViewBean> mAdapter;
    private Context mContext;

    public HwGridPatternView(Context context) {
        this.mContext = context;
    }

    public void setData(List<HwGridPatternViewBean> datas, GridView gridView, int layoutStyle) {
        String layoutName = "hwpattern_gridpattern_l";
        if (layoutStyle == 1) {
            layoutName = "hwpattern_gridpattern_l";
        } else if (layoutStyle == 2) {
            layoutName = "hwpattern_gridpattern_m";
        } else if (layoutStyle == 3) {
            layoutName = "hwpattern_gridpattern_double_subtitle";
        }
        this.mAdapter = new HwGridPatternViewAdapter<HwGridPatternViewBean>(this.mContext, datas, ResLoaderUtil.getLayoutId(this.mContext, layoutName)) {
            /* class huawei.android.widget.pattern.HwGridPatternView.AnonymousClass1 */

            public void convert(ViewHolder holder, HwGridPatternViewBean gridBean) {
                TextView subTitleTv;
                if (holder != null && gridBean != null) {
                    int playBackViewId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_play_back");
                    int playIconId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_play_icon");
                    int titleIconId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_title_icon");
                    holder.setImageDrawable(playBackViewId, gridBean.getPlayBackDrawable());
                    holder.setImageDrawable(playIconId, gridBean.getPlayIconDrawable());
                    holder.setImageDrawable(titleIconId, gridBean.getTitleIconDrawable());
                    int titleId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_title");
                    int subtititleId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle");
                    int secondSubtitleId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle_second");
                    int buttonId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_button");
                    holder.setText(titleId, gridBean.getTitle());
                    holder.setText(subtititleId, gridBean.getSubTitle());
                    holder.setText(secondSubtitleId, gridBean.getSubTitleSecond());
                    holder.setText(buttonId, gridBean.getButtonText());
                    holder.setOnClickListener(buttonId, gridBean.getButtonListener());
                    if (!TextUtils.isEmpty(gridBean.getSubTitleSecond()) && !TextUtils.isEmpty(gridBean.getSubTitle()) && (subTitleTv = (TextView) holder.getView(subtititleId)) != null) {
                        subTitleTv.post(HwGridPatternView.this.getAction(holder, gridBean, subTitleTv, (TextView) holder.getView(secondSubtitleId)));
                    }
                }
            }
        };
        if (gridView != null) {
            gridView.setAdapter((ListAdapter) this.mAdapter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Runnable getAction(final ViewHolder holder, final HwGridPatternViewBean gridBean, final TextView subTitleTv, final TextView subTitleSecondTv) {
        return new Runnable() {
            /* class huawei.android.widget.pattern.HwGridPatternView.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                ViewGroup.MarginLayoutParams layoutParams;
                int subtitleLayoutId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle_layout");
                int dividingId = ResLoaderUtil.getViewId(HwGridPatternView.this.mContext, "hwpattern_gridpattern_subtitle_dividing");
                if (subTitleTv != null && subTitleSecondTv != null) {
                    View subtitleLayout = holder.getView(subtitleLayoutId);
                    View dividingView = holder.getView(dividingId);
                    if (subtitleLayout != null) {
                        int dividingWidth = 0;
                        if (dividingView != null) {
                            ViewGroup.LayoutParams params = dividingView.getLayoutParams();
                            if (params instanceof ViewGroup.MarginLayoutParams) {
                                layoutParams = (ViewGroup.MarginLayoutParams) params;
                            } else {
                                layoutParams = new ViewGroup.MarginLayoutParams(params);
                            }
                            dividingWidth = dividingView.getWidth() + layoutParams.getMarginStart() + layoutParams.getMarginEnd();
                        }
                        TextPaint textPaint = new TextPaint(subTitleTv.getPaint());
                        float subTitleTvWidth = textPaint.measureText(gridBean.getSubTitle());
                        float subTitleSecondTvWidth = textPaint.measureText(gridBean.getSubTitleSecond());
                        float availableWidth = (float) (subtitleLayout.getMeasuredWidth() - dividingWidth);
                        float halfAvailableWidth = availableWidth / 2.0f;
                        if (subTitleTvWidth + subTitleSecondTvWidth <= availableWidth) {
                            subTitleTv.setWidth((int) subTitleTvWidth);
                        } else if (subTitleTvWidth < halfAvailableWidth) {
                            subTitleTv.setWidth((int) subTitleTvWidth);
                        } else if (subTitleSecondTvWidth < halfAvailableWidth) {
                            subTitleTv.setWidth((int) (availableWidth - subTitleSecondTvWidth));
                        } else {
                            subTitleTv.setWidth((int) halfAvailableWidth);
                        }
                    }
                }
            }
        };
    }

    public abstract class HwGridPatternViewAdapter<T> extends BaseAdapter {
        private Context mContext;
        private int mItemLayoutId;
        private List<T> mLists;
        private ViewHolder mViewHolder;

        public abstract void convert(ViewHolder viewHolder, T t);

        public HwGridPatternViewAdapter(Context context, List<T> list, int itemLayoutId) {
            this.mContext = context;
            this.mLists = list;
            this.mItemLayoutId = itemLayoutId;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            List<T> list = this.mLists;
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        @Override // android.widget.Adapter
        public T getItem(int position) {
            List<T> list = this.mLists;
            if (list == null || position >= list.size()) {
                return null;
            }
            return this.mLists.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(this.mContext, parent, this.mItemLayoutId, position);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convert(holder, getItem(position));
            return holder.getConvertView();
        }
    }

    /* access modifiers changed from: private */
    public class ViewHolder {
        private Context mContext;
        private View mConvertView;
        private final SparseArray<View> mViews;

        private ViewHolder(Context context, ViewGroup parent, int itemLayoutId, int position) {
            this.mContext = context;
            this.mViews = new SparseArray<>();
            this.mConvertView = LayoutInflater.from(this.mContext).inflate(itemLayoutId, parent, false);
            View view = this.mConvertView;
            if (view != null) {
                view.setTag(this);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private <T extends View> T getView(int viewId) {
            View view;
            View view2 = (T) this.mViews.get(viewId);
            if (!(view2 != null || (view = this.mConvertView) == null || (view2 = (T) view.findViewById(viewId)) == null)) {
                this.mViews.put(viewId, view2);
            }
            return (T) view2;
        }

        public View getConvertView() {
            return this.mConvertView;
        }

        public ViewHolder setText(int viewId, String text) {
            TextView view = (TextView) getView(viewId);
            if (view != null) {
                view.setText(text);
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
            ImageView view = (ImageView) getView(viewId);
            if (view != null) {
                view.setImageDrawable(drawable);
            }
            return this;
        }

        public ViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
            ImageView view = (ImageView) getView(viewId);
            if (view != null) {
                view.setImageBitmap(bitmap);
            }
            return this;
        }
    }
}

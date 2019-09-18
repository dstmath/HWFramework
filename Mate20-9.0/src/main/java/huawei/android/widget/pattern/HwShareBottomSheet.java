package huawei.android.widget.pattern;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.widget.LinearLayoutManager;
import com.android.internal.widget.RecyclerView;
import huawei.android.widget.DownLoadWidget;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HwShareBottomSheet extends RelativeLayout {
    /* access modifiers changed from: private */
    public Context mContext;
    private int mDuration;
    private boolean mHasInitData;
    private boolean mHasStartAnim;
    private boolean mIsHideMask;
    /* access modifiers changed from: private */
    public ItemClickListener mItemClickListener;
    private int mMaxHeight;
    private RecyclerView mRecyclerView;
    private ShareAdapter mShareAdapter;
    /* access modifiers changed from: private */
    public Intent mShareIntent;

    public interface ItemClickListener {
        void onDownLoadWidgetClick(int i, ShareBean shareBean);

        void onMoreItemClick();

        void onNormalItemClick(int i, ShareBean shareBean);
    }

    private class ShareAdapter extends RecyclerView.Adapter<ShareViewHolder> {
        private static final int REFRESH_DOWNLOAD = 1;
        private final LayoutInflater mInflater;
        /* access modifiers changed from: private */
        public List<ShareBean> mList;
        private int mMaxTextheight = 0;
        private int mSelectIndex = -1;

        private class ShareViewHolder extends RecyclerView.ViewHolder {
            public DownLoadWidget downLoadWidget;
            public ImageView ivIcon;
            public LinearLayout lltOutContaner;
            public RelativeLayout rltContainer;
            public TextView tvDes;

            public ShareViewHolder(View view) {
                super(view);
                this.tvDes = (TextView) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_tv_des"));
                this.ivIcon = (ImageView) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_iv_icon"));
                this.rltContainer = (RelativeLayout) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_rlt_item_contain"));
                this.lltOutContaner = (LinearLayout) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_llt_out_contain"));
                this.downLoadWidget = (DownLoadWidget) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_hwdownloadwidget"));
                this.downLoadWidget.setIdleText(ResLoaderUtil.getString(HwShareBottomSheet.this.getContext(), "share_dialog_install"));
                this.downLoadWidget.setPatternStyle();
                this.rltContainer.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(HwShareBottomSheet.this.mContext, 0));
                this.lltOutContaner.setOnClickListener(new View.OnClickListener(ShareAdapter.this) {
                    public void onClick(View view) {
                        int position = ShareViewHolder.this.getAdapterPosition();
                        ShareBean itemData = (ShareBean) ShareAdapter.this.mList.get(position);
                        if (HwShareBottomSheet.this.mItemClickListener != null) {
                            if (position != ShareAdapter.this.mList.size() - 1) {
                                HwShareBottomSheet.this.mItemClickListener.onNormalItemClick(position, itemData);
                            } else {
                                HwShareBottomSheet.this.mItemClickListener.onMoreItemClick();
                                if (HwShareBottomSheet.this.mShareIntent != null) {
                                    HwShareBottomSheet.this.mContext.startActivity(Intent.createChooser(HwShareBottomSheet.this.mShareIntent, ""));
                                }
                            }
                        }
                        itemData.setChecked(true);
                    }
                });
                this.downLoadWidget.setOnClickListener(new View.OnClickListener(ShareAdapter.this) {
                    public void onClick(View v) {
                        int position = ShareViewHolder.this.getAdapterPosition();
                        ShareBean itemData = (ShareBean) ShareAdapter.this.mList.get(position);
                        if (HwShareBottomSheet.this.mItemClickListener != null) {
                            HwShareBottomSheet.this.mItemClickListener.onDownLoadWidgetClick(position, itemData);
                        }
                    }
                });
            }
        }

        /* access modifiers changed from: protected */
        public void refresh(int index, ShareBean itemData) {
            if (this.mList != null && index >= 0 && index < this.mList.size()) {
                this.mList.set(index, itemData);
                notifyItemChanged(index, 1);
            }
        }

        public ShareAdapter(List<ShareBean> list) {
            if (list != null && !list.isEmpty()) {
                this.mList = new ArrayList();
                this.mList.addAll(list);
                String des = ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_more");
                this.mList.add(new ShareBean(ResLoaderUtil.getDrawableId(HwShareBottomSheet.this.mContext, "hwpattern_ic_more"), des));
            }
            this.mInflater = LayoutInflater.from(HwShareBottomSheet.this.mContext);
        }

        public ShareViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ShareViewHolder(this.mInflater.inflate(ResLoaderUtil.getLayoutId(HwShareBottomSheet.this.mContext, "hwpattern_item_share"), viewGroup, false));
        }

        public void onBindViewHolder(ShareViewHolder viewHolder, int position) {
            ShareBean itemData = this.mList.get(position);
            viewHolder.tvDes.setText(itemData.getDes());
            viewHolder.ivIcon.setImageResource(itemData.getIcon());
            resetDownLoadWidget(viewHolder.downLoadWidget, itemData);
            if (this.mMaxTextheight != 0) {
                ViewGroup.LayoutParams lp = viewHolder.tvDes.getLayoutParams();
                lp.height = this.mMaxTextheight;
                viewHolder.tvDes.setLayoutParams(lp);
            }
        }

        public void onBindViewHolder(ShareViewHolder viewHolder, int position, List payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(viewHolder, position);
                return;
            }
            ShareBean itemData = this.mList.get(position);
            if (1 == ((Integer) payloads.get(0)).intValue()) {
                resetDownLoadWidget(viewHolder.downLoadWidget, itemData);
                if (itemData.getStatus() == 2) {
                    if (viewHolder.tvDes.getLineCount() == viewHolder.tvDes.getMaxLines()) {
                        this.mMaxTextheight = viewHolder.tvDes.getHeight();
                    }
                    HwShareBottomSheet.this.post(new Runnable() {
                        public void run() {
                            ShareAdapter.this.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        private void resetDownLoadWidget(DownLoadWidget downLoadWidget, ShareBean itemData) {
            int status = itemData.getStatus();
            if (status != 0) {
                switch (status) {
                    case 2:
                        downLoadWidget.resetUpdate();
                        downLoadWidget.setVisibility(0);
                        downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_install"));
                        downLoadWidget.setIdleText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_install"));
                        return;
                    case 3:
                        downLoadWidget.setVisibility(0);
                        downLoadWidget.incrementProgressBy(0);
                        downLoadWidget.setProgress(itemData.getDownloadProgress());
                        if (itemData.getDownloadProgress() == 100) {
                            itemData.setStatus(4);
                            downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_open"));
                            downLoadWidget.stop();
                            downLoadWidget.resetUpdate();
                            return;
                        }
                        return;
                    case 4:
                        downLoadWidget.setProgress(100);
                        downLoadWidget.stop();
                        downLoadWidget.resetUpdate();
                        downLoadWidget.setVisibility(0);
                        downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_open"));
                        downLoadWidget.setIdleText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_open"));
                        return;
                    case 5:
                        downLoadWidget.setVisibility(0);
                        downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_pause"));
                        downLoadWidget.incrementProgressBy(0);
                        downLoadWidget.setProgress(itemData.getDownloadProgress());
                        downLoadWidget.stop();
                        return;
                    default:
                        return;
                }
            } else {
                downLoadWidget.setVisibility(8);
            }
        }

        public int getItemCount() {
            if (this.mList == null) {
                return 0;
            }
            return this.mList.size();
        }
    }

    public static class ShareBean implements Serializable {
        private boolean mChecked = false;
        private String mDes;
        private int mDownloadProgress = 0;
        private int mIcon;
        private Object mParam = "";
        private int mStatus = 0;

        public ShareBean(int icon, String des) {
            this.mIcon = icon;
            this.mDes = des;
            this.mChecked = false;
        }

        public int getIcon() {
            return this.mIcon;
        }

        public void setIcon(int icon) {
            this.mIcon = icon;
        }

        public String getDes() {
            return this.mDes;
        }

        public void setDes(String des) {
            this.mDes = des;
        }

        public boolean isChecked() {
            return this.mChecked;
        }

        public void setChecked(boolean checked) {
            this.mChecked = checked;
        }

        public int getStatus() {
            return this.mStatus;
        }

        public void setStatus(int status) {
            this.mStatus = status;
        }

        public int getDownloadProgress() {
            return this.mDownloadProgress;
        }

        public void setDownloadProgress(int downloadProgress) {
            this.mDownloadProgress = downloadProgress;
        }

        public Object getParam() {
            return this.mParam;
        }

        public void setParam(Object param) {
            this.mParam = param;
        }
    }

    private class ShareDecoration extends RecyclerView.ItemDecoration {
        private ShareDecoration() {
        }

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int childCount = parent.getAdapter().getItemCount();
            Context context = ResLoader.getInstance().getContext(parent.getContext());
            int paddingM = ResLoaderUtil.getDimensionPixelSize(context, "padding_m");
            int paddingL = ResLoaderUtil.getDimensionPixelSize(context, "padding_l");
            if (position == 0) {
                outRect.set(paddingL, paddingM, paddingM, paddingM);
            } else if (position == childCount - 1) {
                outRect.set(paddingM, paddingM, paddingL, paddingM);
            } else {
                outRect.set(paddingM, paddingM, paddingM, paddingM);
            }
        }
    }

    public static class Status {
        public static final int DEFAULT = 0;
        public static final int DOWNLOADING = 3;
        public static final int DOWNLOAD_PAUSE = 5;
        public static final int NO_INSTALL = 2;
        public static final int WAIT_TO_OPEN = 4;
    }

    public HwShareBottomSheet(Context context) {
        this(context, null);
    }

    public HwShareBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwShareBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwShareBottomSheet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHasStartAnim = false;
        this.mMaxHeight = 0;
        this.mDuration = 200;
        this.mHasInitData = false;
        this.mContext = context;
    }

    public void setData(List<ShareBean> list, Intent shareIntent, boolean isHideMask, ItemClickListener itemClickListener) {
        this.mIsHideMask = isHideMask;
        this.mShareIntent = shareIntent;
        this.mItemClickListener = itemClickListener;
        if (this.mIsHideMask) {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_share_nomask", this, true);
        } else {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_share", this, true);
            View maskView = findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_mask_view"));
            if (maskView != null) {
                maskView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (HwShareBottomSheet.this.mContext instanceof Activity) {
                            ((Activity) HwShareBottomSheet.this.mContext).finish();
                        }
                    }
                });
            }
        }
        this.mRecyclerView = findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_recyclerview"));
        this.mShareAdapter = new ShareAdapter(list);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.mContext, 0, false));
        this.mRecyclerView.setAdapter(this.mShareAdapter);
        this.mRecyclerView.addItemDecoration(new ShareDecoration());
        this.mHasInitData = true;
    }

    public void refresh(int index, ShareBean itemData) {
        if (this.mShareAdapter != null) {
            this.mShareAdapter.refresh(index, itemData);
        }
    }

    public void runExitAnim(AnimatorListenerAdapter animatorListenerAdapter) {
        if (!this.mIsHideMask) {
            int recyclerviewHeight = this.mRecyclerView.getMeasuredHeight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.mRecyclerView, "TranslationY", new float[]{0.0f, (float) recyclerviewHeight});
            if (animatorListenerAdapter != null) {
                objectAnimator.addListener(animatorListenerAdapter);
            }
            objectAnimator.setDuration((long) this.mDuration);
            objectAnimator.start();
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mHasInitData) {
            this.mRecyclerView.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, 0));
            if (this.mRecyclerView.getMeasuredHeight() > this.mMaxHeight) {
                this.mMaxHeight = this.mRecyclerView.getMeasuredHeight();
            }
            ViewGroup.LayoutParams lp = this.mRecyclerView.getLayoutParams();
            lp.height = this.mMaxHeight;
            this.mRecyclerView.setLayoutParams(lp);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mHasInitData && !this.mHasStartAnim && !this.mIsHideMask) {
            this.mHasStartAnim = true;
            int recyclerviewHeight = this.mRecyclerView.getMeasuredHeight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.mRecyclerView, "TranslationY", new float[]{(float) recyclerviewHeight, 0.0f});
            objectAnimator.setDuration((long) this.mDuration);
            objectAnimator.start();
        }
    }
}

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
    private static final int DURATION = 200;
    private static final int MAX_PROGRESS = 100;
    private Context mContext;
    private int mDuration;
    private boolean mIsHasInitData;
    private boolean mIsHasStartAnim;
    private boolean mIsHideMask;
    private ItemClickListener mItemClickListener;
    private int mMaxHeight;
    private RecyclerView mRecyclerView;
    private ShareAdapter mShareAdapter;
    private Intent mShareIntent;

    public interface ItemClickListener {
        void onDownLoadWidgetClick(int i, ShareBean shareBean);

        void onMoreItemClick();

        void onNormalItemClick(int i, ShareBean shareBean);
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
        this.mIsHasStartAnim = false;
        this.mMaxHeight = 0;
        this.mDuration = DURATION;
        this.mIsHasInitData = false;
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
                    /* class huawei.android.widget.pattern.HwShareBottomSheet.AnonymousClass1 */

                    @Override // android.view.View.OnClickListener
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
        this.mIsHasInitData = true;
    }

    public void refresh(int index, ShareBean itemData) {
        ShareAdapter shareAdapter = this.mShareAdapter;
        if (shareAdapter != null) {
            shareAdapter.refresh(index, itemData);
        }
    }

    public void runExitAnim(AnimatorListenerAdapter animatorListenerAdapter) {
        if (!this.mIsHideMask) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.mRecyclerView, "TranslationY", 0.0f, (float) this.mRecyclerView.getMeasuredHeight());
            if (animatorListenerAdapter != null) {
                objectAnimator.addListener(animatorListenerAdapter);
            }
            objectAnimator.setDuration((long) this.mDuration);
            objectAnimator.start();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.RelativeLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mIsHasInitData) {
            this.mRecyclerView.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, 0));
            if (this.mRecyclerView.getMeasuredHeight() > this.mMaxHeight) {
                this.mMaxHeight = this.mRecyclerView.getMeasuredHeight();
            }
            ViewGroup.LayoutParams layoutParams = this.mRecyclerView.getLayoutParams();
            layoutParams.height = this.mMaxHeight;
            this.mRecyclerView.setLayoutParams(layoutParams);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.RelativeLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        if (this.mIsHasInitData && !this.mIsHasStartAnim && !this.mIsHideMask) {
            this.mIsHasStartAnim = true;
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.mRecyclerView, "TranslationY", (float) this.mRecyclerView.getMeasuredHeight(), 0.0f);
            objectAnimator.setDuration((long) this.mDuration);
            objectAnimator.start();
        }
    }

    private class ShareAdapter extends RecyclerView.Adapter<ShareViewHolder> {
        private static final int INIT_ARRAY_LIST_SIZE = 10;
        private static final int REFRESH_DOWNLOAD = 1;
        private final LayoutInflater mInflater;
        private List<ShareBean> mList;
        private int mMaxTextheight = 0;
        private int mSelectIndex = -1;

        ShareAdapter(List<ShareBean> list) {
            if (list != null && !list.isEmpty()) {
                this.mList = new ArrayList((int) INIT_ARRAY_LIST_SIZE);
                this.mList.addAll(list);
                String describe = ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_more");
                this.mList.add(new ShareBean(ResLoaderUtil.getDrawableId(HwShareBottomSheet.this.mContext, "hwpattern_ic_more"), describe));
            }
            this.mInflater = LayoutInflater.from(HwShareBottomSheet.this.mContext);
        }

        /* access modifiers changed from: protected */
        public void refresh(int index, ShareBean itemData) {
            List<ShareBean> list = this.mList;
            if (list != null && index >= 0 && index < list.size()) {
                this.mList.set(index, itemData);
                notifyItemChanged(index, 1);
            }
        }

        public ShareViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ShareViewHolder(this.mInflater.inflate(ResLoaderUtil.getLayoutId(HwShareBottomSheet.this.mContext, "hwpattern_item_share"), viewGroup, false));
        }

        public void onBindViewHolder(ShareViewHolder viewHolder, int position) {
            List<ShareBean> list = this.mList;
            if (list != null && list.size() != 0 && position >= 0 && this.mList.size() > position) {
                ShareBean itemData = this.mList.get(position);
                viewHolder.mTvDescibe.setText(itemData.getDes());
                viewHolder.mIvIcon.setImageResource(itemData.getIcon());
                resetDownLoadWidget(viewHolder.mDownLoadWidget, itemData);
            }
            if (this.mMaxTextheight != 0) {
                ViewGroup.LayoutParams layoutParams = viewHolder.mTvDescibe.getLayoutParams();
                layoutParams.height = this.mMaxTextheight;
                viewHolder.mTvDescibe.setLayoutParams(layoutParams);
            }
        }

        public void onBindViewHolder(ShareViewHolder viewHolder, int position, List payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(viewHolder, position);
                return;
            }
            List<ShareBean> list = this.mList;
            if (list != null && list.size() != 0 && position >= 0 && position < this.mList.size()) {
                ShareBean itemData = this.mList.get(position);
                if (((Integer) payloads.get(0)).intValue() == 1) {
                    resetDownLoadWidget(viewHolder.mDownLoadWidget, itemData);
                    if (itemData.getStatus() == 2) {
                        adjustMaxTextHeight(viewHolder);
                        HwShareBottomSheet.this.post(new Runnable() {
                            /* class huawei.android.widget.pattern.HwShareBottomSheet.ShareAdapter.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                ShareAdapter.this.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        }

        private void adjustMaxTextHeight(ShareViewHolder viewHolder) {
            if (viewHolder.mTvDescibe.getLineCount() == viewHolder.mTvDescibe.getMaxLines()) {
                this.mMaxTextheight = viewHolder.mTvDescibe.getHeight();
            }
        }

        private void resetDownLoadWidget(DownLoadWidget downLoadWidget, ShareBean itemData) {
            int status = itemData.getStatus();
            if (status == 0) {
                downLoadWidget.setVisibility(8);
            } else if (status == 2) {
                downLoadWidget.resetUpdate();
                downLoadWidget.setVisibility(0);
                downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_install"));
                downLoadWidget.setIdleText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_install"));
            } else if (status == 3) {
                downLoadWidget.setVisibility(0);
                downLoadWidget.incrementProgressBy(0);
                downLoadWidget.setProgress(itemData.getDownloadProgress());
                if (itemData.getDownloadProgress() == HwShareBottomSheet.MAX_PROGRESS) {
                    itemData.setStatus(4);
                    downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_open"));
                    downLoadWidget.stop();
                    downLoadWidget.resetUpdate();
                }
            } else if (status == 4) {
                downLoadWidget.setProgress(HwShareBottomSheet.MAX_PROGRESS);
                downLoadWidget.stop();
                downLoadWidget.resetUpdate();
                downLoadWidget.setVisibility(0);
                downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_open"));
                downLoadWidget.setIdleText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_open"));
            } else if (status == 5) {
                downLoadWidget.setVisibility(0);
                downLoadWidget.setPauseText(ResLoaderUtil.getString(HwShareBottomSheet.this.mContext, "share_dialog_pause"));
                downLoadWidget.incrementProgressBy(0);
                downLoadWidget.setProgress(itemData.getDownloadProgress());
                downLoadWidget.stop();
            }
        }

        public int getItemCount() {
            List<ShareBean> list = this.mList;
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        /* access modifiers changed from: private */
        public class ShareViewHolder extends RecyclerView.ViewHolder {
            private DownLoadWidget mDownLoadWidget;
            private ImageView mIvIcon;
            private LinearLayout mLltOutContainer;
            private RelativeLayout mRltContainer;
            private TextView mTvDescibe;

            ShareViewHolder(View view) {
                super(view);
                this.mTvDescibe = (TextView) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_tv_des"));
                this.mIvIcon = (ImageView) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_iv_icon"));
                this.mRltContainer = (RelativeLayout) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_rlt_item_contain"));
                this.mLltOutContainer = (LinearLayout) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_llt_out_contain"));
                this.mDownLoadWidget = (DownLoadWidget) view.findViewById(ResLoaderUtil.getViewId(HwShareBottomSheet.this.getContext(), "hwpattern_hwdownloadwidget"));
                this.mDownLoadWidget.setIdleText(ResLoaderUtil.getString(HwShareBottomSheet.this.getContext(), "share_dialog_install"));
                this.mDownLoadWidget.setPatternStyle();
                this.mRltContainer.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(HwShareBottomSheet.this.mContext, 0));
                this.mLltOutContainer.setOnClickListener(new View.OnClickListener(ShareAdapter.this) {
                    /* class huawei.android.widget.pattern.HwShareBottomSheet.ShareAdapter.ShareViewHolder.AnonymousClass1 */

                    @Override // android.view.View.OnClickListener
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
                this.mDownLoadWidget.setOnClickListener(new View.OnClickListener(ShareAdapter.this) {
                    /* class huawei.android.widget.pattern.HwShareBottomSheet.ShareAdapter.ShareViewHolder.AnonymousClass2 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        int position = ShareViewHolder.this.getAdapterPosition();
                        if (ShareAdapter.this.mList != null && ShareAdapter.this.mList.size() != 0 && position >= 0 && position < ShareAdapter.this.mList.size()) {
                            ShareBean itemData = (ShareBean) ShareAdapter.this.mList.get(position);
                            if (HwShareBottomSheet.this.mItemClickListener != null) {
                                HwShareBottomSheet.this.mItemClickListener.onDownLoadWidgetClick(position, itemData);
                            }
                        }
                    }
                });
            }
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

    public static class ShareBean implements Serializable {
        private String mDescribe;
        private int mDownloadProgress = 0;
        private int mIcon;
        private boolean mIsChecked = false;
        private Object mParam = "";
        private int mStatus = 0;

        public ShareBean(int icon, String describe) {
            this.mIcon = icon;
            this.mDescribe = describe;
            this.mIsChecked = false;
        }

        public int getIcon() {
            return this.mIcon;
        }

        public void setIcon(int icon) {
            this.mIcon = icon;
        }

        public String getDes() {
            return this.mDescribe;
        }

        public void setDes(String describe) {
            this.mDescribe = describe;
        }

        public boolean isChecked() {
            return this.mIsChecked;
        }

        public void setChecked(boolean isChecked) {
            this.mIsChecked = isChecked;
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
}

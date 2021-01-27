package huawei.android.widget.pattern;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.android.widget.pattern.HwShareBottomSheet;
import java.util.List;

public class HwSharePreview extends FrameLayout {
    private FrameLayout mParent;
    private HwShareBottomSheet mShareView;

    public HwSharePreview(Context context) {
        super(context);
        init(context);
    }

    public HwSharePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwSharePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public HwSharePreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(ResLoaderUtil.getLayoutId(context, "hwpattern_share_preview"), (ViewGroup) this, true);
        int parentId = ResLoaderUtil.getViewId(context, "container");
        int shareViewId = ResLoaderUtil.getViewId(context, "share_view");
        this.mParent = (FrameLayout) view.findViewById(parentId);
        this.mShareView = (HwShareBottomSheet) view.findViewById(shareViewId);
    }

    @Override // android.view.ViewGroup
    public void addView(View preView) {
        if (preView != null) {
            this.mParent.addView(preView);
        }
    }

    public void setData(List<HwShareBottomSheet.ShareBean> list, Intent shareIntent, boolean isHideMask, HwShareBottomSheet.ItemClickListener itemClickListener) {
        HwShareBottomSheet hwShareBottomSheet = this.mShareView;
        if (hwShareBottomSheet != null) {
            hwShareBottomSheet.setData(list, shareIntent, isHideMask, itemClickListener);
        }
    }

    public void refresh(int index, HwShareBottomSheet.ShareBean itemData) {
        HwShareBottomSheet hwShareBottomSheet = this.mShareView;
        if (hwShareBottomSheet != null) {
            hwShareBottomSheet.refresh(index, itemData);
        }
    }
}

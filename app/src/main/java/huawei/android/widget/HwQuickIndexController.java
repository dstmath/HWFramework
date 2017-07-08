package huawei.android.widget;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import huawei.android.widget.AlphaIndexerListView.OnItemClickListener;

public class HwQuickIndexController {
    private static final String TAG = "HwQuickIndexController";
    private AlphaIndexerListView mAlphaView;
    private HwSortedTextListAdapter mDataAdapter;
    private int mFlingStartPos;
    private boolean mIsShowPopup;
    private ListView mListView;

    public HwQuickIndexController(ListView listview, AlphaIndexerListView alphaView) {
        this.mFlingStartPos = 0;
        this.mListView = listview;
        this.mDataAdapter = (HwSortedTextListAdapter) listview.getAdapter();
        this.mAlphaView = alphaView;
        this.mAlphaView.setListViewAttachTo(this.mListView);
        this.mAlphaView.setOverLayInfo(this.mDataAdapter.getSections()[this.mDataAdapter.getSectionForPosition(this.mListView.getFirstVisiblePosition())]);
    }

    public void setOnListen() {
        this.mListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case ViewDragHelper.STATE_IDLE /*0*/:
                        HwQuickIndexController.this.mIsShowPopup = false;
                        HwQuickIndexController.this.mAlphaView.dismissPopup();
                        Log.e(HwQuickIndexController.TAG, "SCROLL_STATE_IDLE");
                    case ViewDragHelper.STATE_SETTLING /*2*/:
                        HwQuickIndexController.this.mIsShowPopup = true;
                        HwQuickIndexController.this.mFlingStartPos = HwQuickIndexController.this.mListView.getFirstVisiblePosition();
                        Log.e(HwQuickIndexController.TAG, "SCROLL_STATE_FLING_IN: " + HwQuickIndexController.this.mFlingStartPos);
                    default:
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                HwQuickIndexController.this.mAlphaView.invalidate();
                HwQuickIndexController.this.mAlphaView.setOverLayInfo(HwQuickIndexController.this.mDataAdapter.getSections()[HwQuickIndexController.this.mDataAdapter.getSectionForPosition(firstVisibleItem)]);
                if (HwQuickIndexController.this.mIsShowPopup && Math.abs(firstVisibleItem - HwQuickIndexController.this.mFlingStartPos) > 2) {
                    HwQuickIndexController.this.mAlphaView.showPopup();
                }
            }
        });
        this.mAlphaView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(String s, int pos) {
                if (s != null) {
                    String[] sections = (String[]) HwQuickIndexController.this.mDataAdapter.getSections();
                    int sectionPos = pos;
                    String str = null;
                    for (int i = 0; i < sections.length; i++) {
                        if (HwQuickIndexController.this.mAlphaView.equalsChar(s, sections[i], pos)) {
                            str = sections[i];
                            sectionPos = i;
                            break;
                        }
                    }
                    if (str != null) {
                        HwQuickIndexController.this.mAlphaView.showPopup(str);
                        int position = HwQuickIndexController.this.mDataAdapter.getPositionForSection(sectionPos);
                        if (-1 != position) {
                            HwQuickIndexController.this.mListView.setSelection(position);
                        }
                        int countOfScreen = (HwQuickIndexController.this.mListView.getLastVisiblePosition() - HwQuickIndexController.this.mListView.getFirstVisiblePosition()) + 1;
                        if (position + countOfScreen > HwQuickIndexController.this.mListView.getCount()) {
                            str = (String) HwQuickIndexController.this.mDataAdapter.getSectionNameForPosition(HwQuickIndexController.this.mListView.getCount() - countOfScreen);
                        }
                        HwQuickIndexController.this.mAlphaView.setOverLayInfo(pos, str);
                        return;
                    }
                    if (HwQuickIndexController.this.mAlphaView.needSwitchIndexer(pos)) {
                        if (HwQuickIndexController.this.mAlphaView.isNativeIndexerShow()) {
                            HwQuickIndexController.this.mListView.setSelection(HwQuickIndexController.this.mListView.getCount() - 1);
                        } else {
                            HwQuickIndexController.this.mListView.setSelection(0);
                        }
                    }
                    HwQuickIndexController.this.mAlphaView.setOverLayInfo(pos, (String) HwQuickIndexController.this.mDataAdapter.getSectionNameForPosition(HwQuickIndexController.this.mListView.getFirstVisiblePosition()));
                }
            }
        });
    }
}

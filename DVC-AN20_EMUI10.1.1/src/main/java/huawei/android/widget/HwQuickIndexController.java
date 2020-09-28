package huawei.android.widget;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.widget.AlphaIndexerListView;

public class HwQuickIndexController {
    private static final int MIN_DISTANCE = 2;
    private static final String TAG = "HwQuickIndexController";
    private AlphaIndexerListView mAlphaView;
    private HwSortedTextListAdapter mDataAdapter;
    private int mFlingStartPos = 0;
    private boolean mIsShowPopup;
    private ListView mListView;
    private AlphaIndexerListView.OnItemClickListener mOnItemClickListener = new AlphaIndexerListView.OnItemClickListener() {
        /* class huawei.android.widget.HwQuickIndexController.AnonymousClass2 */

        @Override // huawei.android.widget.AlphaIndexerListView.OnItemClickListener
        public void onItemClick(String str, int pos) {
            if (str != null) {
                String[] sections = (String[]) HwQuickIndexController.this.mDataAdapter.getSections();
                int sectionPos = pos;
                String sectionText = null;
                int i = 0;
                while (true) {
                    if (i >= sections.length) {
                        break;
                    } else if (HwQuickIndexController.this.mAlphaView.equalsChar(str, sections[i], pos)) {
                        sectionText = sections[i];
                        sectionPos = i;
                        break;
                    } else {
                        i++;
                    }
                }
                if (sectionText != null) {
                    HwQuickIndexController.this.mAlphaView.showPopup(sectionText);
                    int position = HwQuickIndexController.this.mDataAdapter.getPositionForSection(sectionPos);
                    if (position != -1) {
                        HwQuickIndexController.this.mListView.setSelection(position);
                    }
                    int countOfScreen = (HwQuickIndexController.this.mListView.getLastVisiblePosition() - HwQuickIndexController.this.mListView.getFirstVisiblePosition()) + 1;
                    if (position + countOfScreen > HwQuickIndexController.this.mListView.getCount()) {
                        sectionText = (String) HwQuickIndexController.this.mDataAdapter.getSectionNameForPosition(HwQuickIndexController.this.mListView.getCount() - countOfScreen);
                    }
                    HwQuickIndexController.this.mAlphaView.setOverLayInfo(pos, sectionText);
                    return;
                }
                if (HwQuickIndexController.this.mAlphaView.needSwitchIndexer(pos)) {
                    if (HwQuickIndexController.this.mAlphaView.isNativeIndexerShow()) {
                        HwQuickIndexController.this.mListView.setSelection(HwQuickIndexController.this.mListView.getCount() - 1);
                    } else {
                        HwQuickIndexController.this.mListView.setSelection(0);
                    }
                }
                HwQuickIndexController.this.mAlphaView.setOverLayInfo(pos, HwQuickIndexController.this.getCurrentSection(HwQuickIndexController.this.mDataAdapter.getSectionForPosition(HwQuickIndexController.this.mListView.getFirstVisiblePosition())));
            }
        }
    };
    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        /* class huawei.android.widget.HwQuickIndexController.AnonymousClass1 */

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0) {
                HwQuickIndexController.this.mIsShowPopup = false;
                HwQuickIndexController.this.mAlphaView.dismissPopup();
                Log.i(HwQuickIndexController.TAG, "SCROLL_STATE_IDLE");
            } else if (scrollState == 2) {
                HwQuickIndexController.this.mIsShowPopup = true;
                HwQuickIndexController hwQuickIndexController = HwQuickIndexController.this;
                hwQuickIndexController.mFlingStartPos = hwQuickIndexController.mListView.getFirstVisiblePosition();
                Log.i(HwQuickIndexController.TAG, "SCROLL_STATE_FLING_IN: " + HwQuickIndexController.this.mFlingStartPos);
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            HwQuickIndexController.this.mAlphaView.invalidate();
            HwQuickIndexController.this.mAlphaView.setOverLayInfo(HwQuickIndexController.this.getCurrentSection(HwQuickIndexController.this.mDataAdapter.getSectionForPosition(firstVisibleItem)));
            if (HwQuickIndexController.this.mIsShowPopup && Math.abs(firstVisibleItem - HwQuickIndexController.this.mFlingStartPos) > 2) {
                HwQuickIndexController.this.mAlphaView.showPopup();
            }
        }
    };

    public HwQuickIndexController(ListView listview, AlphaIndexerListView alphaView) {
        this.mListView = listview;
        this.mDataAdapter = (HwSortedTextListAdapter) listview.getAdapter();
        this.mAlphaView = alphaView;
        this.mAlphaView.setListViewAttachTo(this.mListView);
        this.mAlphaView.setOverLayInfo(getCurrentSection(this.mDataAdapter.getSectionForPosition(this.mListView.getFirstVisiblePosition())));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCurrentSection(int sectionPos) {
        if (this.mDataAdapter.getSections().length <= sectionPos || sectionPos < 0) {
            return BuildConfig.FLAVOR;
        }
        return (String) this.mDataAdapter.getSections()[sectionPos];
    }

    public void setOnListen() {
        this.mListView.setOnScrollListener(this.mOnScrollListener);
        this.mAlphaView.setOnItemClickListener(this.mOnItemClickListener);
    }
}

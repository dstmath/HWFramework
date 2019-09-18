package huawei.android.widget;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import huawei.android.widget.AlphaIndexerListView;

public class HwQuickIndexController {
    private static final String TAG = "HwQuickIndexController";
    /* access modifiers changed from: private */
    public AlphaIndexerListView mAlphaView;
    /* access modifiers changed from: private */
    public HwSortedTextListAdapter mDataAdapter;
    /* access modifiers changed from: private */
    public int mFlingStartPos = 0;
    /* access modifiers changed from: private */
    public boolean mIsShowPopup;
    /* access modifiers changed from: private */
    public ListView mListView;

    public HwQuickIndexController(ListView listview, AlphaIndexerListView alphaView) {
        this.mListView = listview;
        this.mDataAdapter = (HwSortedTextListAdapter) listview.getAdapter();
        this.mAlphaView = alphaView;
        this.mAlphaView.setListViewAttachTo(this.mListView);
        this.mAlphaView.setOverLayInfo(getCurrentSection(this.mDataAdapter.getSectionForPosition(this.mListView.getFirstVisiblePosition())));
    }

    /* access modifiers changed from: private */
    public String getCurrentSection(int sectionPos) {
        if (this.mDataAdapter.getSections().length <= sectionPos || sectionPos < 0) {
            return "";
        }
        return (String) this.mDataAdapter.getSections()[sectionPos];
    }

    public void setOnListen() {
        this.mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    boolean unused = HwQuickIndexController.this.mIsShowPopup = false;
                    HwQuickIndexController.this.mAlphaView.dismissPopup();
                    Log.e(HwQuickIndexController.TAG, "SCROLL_STATE_IDLE");
                } else if (scrollState == 2) {
                    boolean unused2 = HwQuickIndexController.this.mIsShowPopup = true;
                    int unused3 = HwQuickIndexController.this.mFlingStartPos = HwQuickIndexController.this.mListView.getFirstVisiblePosition();
                    Log.e(HwQuickIndexController.TAG, "SCROLL_STATE_FLING_IN: " + HwQuickIndexController.this.mFlingStartPos);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                HwQuickIndexController.this.mAlphaView.invalidate();
                HwQuickIndexController.this.mAlphaView.setOverLayInfo(HwQuickIndexController.this.getCurrentSection(HwQuickIndexController.this.mDataAdapter.getSectionForPosition(firstVisibleItem)));
                if (HwQuickIndexController.this.mIsShowPopup && Math.abs(firstVisibleItem - HwQuickIndexController.this.mFlingStartPos) > 2) {
                    HwQuickIndexController.this.mAlphaView.showPopup();
                }
            }
        });
        this.mAlphaView.setOnItemClickListener(new AlphaIndexerListView.OnItemClickListener() {
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: java.lang.Object} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.lang.String} */
            /* JADX WARNING: Multi-variable type inference failed */
            public void onItemClick(String s, int pos) {
                if (s != null) {
                    String[] sections = (String[]) HwQuickIndexController.this.mDataAdapter.getSections();
                    int sectionPos = pos;
                    String sectionText = null;
                    int i = 0;
                    while (true) {
                        if (i >= sections.length) {
                            break;
                        } else if (HwQuickIndexController.this.mAlphaView.equalsChar(s, sections[i], pos)) {
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
                        if (-1 != position) {
                            HwQuickIndexController.this.mListView.setSelection(position);
                        }
                        int countOfScreen = (HwQuickIndexController.this.mListView.getLastVisiblePosition() - HwQuickIndexController.this.mListView.getFirstVisiblePosition()) + 1;
                        if (position + countOfScreen > HwQuickIndexController.this.mListView.getCount()) {
                            sectionText = HwQuickIndexController.this.mDataAdapter.getSectionNameForPosition(HwQuickIndexController.this.mListView.getCount() - countOfScreen);
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
        });
    }
}

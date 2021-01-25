package android.support.v4.media.subtitle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.media.SubtitleData2;
import android.support.v4.media.subtitle.Cea708CCParser;
import android.support.v4.media.subtitle.ClosedCaptionWidget;
import android.support.v4.media.subtitle.SubtitleController;
import android.support.v4.media.subtitle.SubtitleTrack;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.RelativeLayout;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@RequiresApi(28)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class Cea708CaptionRenderer extends SubtitleController.Renderer {
    private Cea708CCWidget mCCWidget;
    private final Context mContext;

    public Cea708CaptionRenderer(Context context) {
        this.mContext = context;
    }

    @Override // android.support.v4.media.subtitle.SubtitleController.Renderer
    public boolean supports(MediaFormat format) {
        if (format.containsKey("mime")) {
            return SubtitleData2.MIMETYPE_TEXT_CEA_708.equals(format.getString("mime"));
        }
        return false;
    }

    @Override // android.support.v4.media.subtitle.SubtitleController.Renderer
    public SubtitleTrack createTrack(MediaFormat format) {
        if (SubtitleData2.MIMETYPE_TEXT_CEA_708.equals(format.getString("mime"))) {
            if (this.mCCWidget == null) {
                this.mCCWidget = new Cea708CCWidget(this, this.mContext);
            }
            return new Cea708CaptionTrack(this.mCCWidget, format);
        }
        throw new RuntimeException("No matching format: " + format.toString());
    }

    static class Cea708CaptionTrack extends SubtitleTrack {
        private final Cea708CCParser mCCParser = new Cea708CCParser(this.mRenderingWidget);
        private final Cea708CCWidget mRenderingWidget;

        Cea708CaptionTrack(Cea708CCWidget renderingWidget, MediaFormat format) {
            super(format);
            this.mRenderingWidget = renderingWidget;
        }

        @Override // android.support.v4.media.subtitle.SubtitleTrack
        public void onData(byte[] data, boolean eos, long runID) {
            this.mCCParser.parse(data);
        }

        @Override // android.support.v4.media.subtitle.SubtitleTrack
        public SubtitleTrack.RenderingWidget getRenderingWidget() {
            return this.mRenderingWidget;
        }

        @Override // android.support.v4.media.subtitle.SubtitleTrack
        public void updateView(ArrayList<SubtitleTrack.Cue> arrayList) {
        }
    }

    /* access modifiers changed from: package-private */
    public class Cea708CCWidget extends ClosedCaptionWidget implements Cea708CCParser.DisplayListener {
        private final CCHandler mCCHandler;

        Cea708CCWidget(Cea708CaptionRenderer this$02, Context context) {
            this(this$02, context, null);
        }

        Cea708CCWidget(Cea708CaptionRenderer this$02, Context context, AttributeSet attrs) {
            this(this$02, context, attrs, 0);
        }

        Cea708CCWidget(Cea708CaptionRenderer this$02, Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        Cea708CCWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            this.mCCHandler = new CCHandler((CCLayout) this.mClosedCaptionLayout);
        }

        @Override // android.support.v4.media.subtitle.ClosedCaptionWidget
        public ClosedCaptionWidget.ClosedCaptionLayout createCaptionLayout(Context context) {
            return new CCLayout(context);
        }

        @Override // android.support.v4.media.subtitle.Cea708CCParser.DisplayListener
        public void emitEvent(Cea708CCParser.CaptionEvent event) {
            this.mCCHandler.processCaptionEvent(event);
            setSize(getWidth(), getHeight());
            if (this.mListener != null) {
                this.mListener.onChanged(this);
            }
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            ((ViewGroup) this.mClosedCaptionLayout).draw(canvas);
        }

        /* access modifiers changed from: package-private */
        public class ScaledLayout extends ViewGroup {
            private static final boolean DEBUG = false;
            private static final String TAG = "ScaledLayout";
            private Rect[] mRectArray;
            private final Comparator<Rect> mRectTopLeftSorter = new Comparator<Rect>() {
                /* class android.support.v4.media.subtitle.Cea708CaptionRenderer.Cea708CCWidget.ScaledLayout.AnonymousClass1 */

                public int compare(Rect lhs, Rect rhs) {
                    if (lhs.top != rhs.top) {
                        return lhs.top - rhs.top;
                    }
                    return lhs.left - rhs.left;
                }
            };

            ScaledLayout(Context context) {
                super(context);
            }

            /* access modifiers changed from: package-private */
            public class ScaledLayoutParams extends ViewGroup.LayoutParams {
                public static final float SCALE_UNSPECIFIED = -1.0f;
                public float scaleEndCol;
                public float scaleEndRow;
                public float scaleStartCol;
                public float scaleStartRow;

                ScaledLayoutParams(float scaleStartRow2, float scaleEndRow2, float scaleStartCol2, float scaleEndCol2) {
                    super(-1, -1);
                    this.scaleStartRow = scaleStartRow2;
                    this.scaleEndRow = scaleEndRow2;
                    this.scaleStartCol = scaleStartCol2;
                    this.scaleEndCol = scaleEndCol2;
                }

                ScaledLayoutParams(Context context, AttributeSet attrs) {
                    super(-1, -1);
                }
            }

            @Override // android.view.ViewGroup
            public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
                return new ScaledLayoutParams(getContext(), attrs);
            }

            /* access modifiers changed from: protected */
            @Override // android.view.ViewGroup
            public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
                return p instanceof ScaledLayoutParams;
            }

            /* access modifiers changed from: protected */
            @Override // android.view.View
            public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
                int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
                int width = (widthSpecSize - getPaddingLeft()) - getPaddingRight();
                int height = (heightSpecSize - getPaddingTop()) - getPaddingBottom();
                int count = getChildCount();
                this.mRectArray = new Rect[count];
                int i = 0;
                while (i < count) {
                    View child = getChildAt(i);
                    ViewGroup.LayoutParams params = child.getLayoutParams();
                    if (params instanceof ScaledLayoutParams) {
                        float scaleStartRow = ((ScaledLayoutParams) params).scaleStartRow;
                        float scaleEndRow = ((ScaledLayoutParams) params).scaleEndRow;
                        float scaleStartCol = ((ScaledLayoutParams) params).scaleStartCol;
                        float scaleEndCol = ((ScaledLayoutParams) params).scaleEndCol;
                        if (scaleStartRow < 0.0f || scaleStartRow > 1.0f) {
                            throw new RuntimeException("A child of ScaledLayout should have a range of scaleStartRow between 0 and 1");
                        } else if (scaleEndRow < scaleStartRow || scaleStartRow > 1.0f) {
                            throw new RuntimeException("A child of ScaledLayout should have a range of scaleEndRow between scaleStartRow and 1");
                        } else if (scaleEndCol < 0.0f || scaleEndCol > 1.0f) {
                            throw new RuntimeException("A child of ScaledLayout should have a range of scaleStartCol between 0 and 1");
                        } else if (scaleEndCol < scaleStartCol || scaleEndCol > 1.0f) {
                            throw new RuntimeException("A child of ScaledLayout should have a range of scaleEndCol between scaleStartCol and 1");
                        } else {
                            this.mRectArray[i] = new Rect((int) (((float) width) * scaleStartCol), (int) (((float) height) * scaleStartRow), (int) (((float) width) * scaleEndCol), (int) (((float) height) * scaleEndRow));
                            int childWidthSpec = View.MeasureSpec.makeMeasureSpec((int) (((float) width) * (scaleEndCol - scaleStartCol)), 1073741824);
                            child.measure(childWidthSpec, View.MeasureSpec.makeMeasureSpec(0, 0));
                            if (child.getMeasuredHeight() > this.mRectArray[i].height()) {
                                int overflowedHeight = ((child.getMeasuredHeight() - this.mRectArray[i].height()) + 1) / 2;
                                this.mRectArray[i].bottom += overflowedHeight;
                                this.mRectArray[i].top -= overflowedHeight;
                                if (this.mRectArray[i].top < 0) {
                                    this.mRectArray[i].bottom -= this.mRectArray[i].top;
                                    this.mRectArray[i].top = 0;
                                }
                                if (this.mRectArray[i].bottom > height) {
                                    this.mRectArray[i].top -= this.mRectArray[i].bottom - height;
                                    this.mRectArray[i].bottom = height;
                                }
                            }
                            child.measure(childWidthSpec, View.MeasureSpec.makeMeasureSpec((int) (((float) height) * (scaleEndRow - scaleStartRow)), 1073741824));
                            i++;
                            widthSpecSize = widthSpecSize;
                            heightSpecSize = heightSpecSize;
                        }
                    } else {
                        throw new RuntimeException("A child of ScaledLayout cannot have the UNSPECIFIED scale factors");
                    }
                }
                int[] visibleRectGroup = new int[count];
                Rect[] visibleRectArray = new Rect[count];
                int visibleRectCount = 0;
                for (int i2 = 0; i2 < count; i2++) {
                    if (getChildAt(i2).getVisibility() == 0) {
                        visibleRectGroup[visibleRectCount] = visibleRectCount;
                        visibleRectArray[visibleRectCount] = this.mRectArray[i2];
                        visibleRectCount++;
                    }
                }
                Arrays.sort(visibleRectArray, 0, visibleRectCount, this.mRectTopLeftSorter);
                for (int i3 = 0; i3 < visibleRectCount - 1; i3++) {
                    for (int j = i3 + 1; j < visibleRectCount; j++) {
                        if (Rect.intersects(visibleRectArray[i3], visibleRectArray[j])) {
                            visibleRectGroup[j] = visibleRectGroup[i3];
                            visibleRectArray[j].set(visibleRectArray[j].left, visibleRectArray[i3].bottom, visibleRectArray[j].right, visibleRectArray[i3].bottom + visibleRectArray[j].height());
                        }
                    }
                }
                for (int i4 = visibleRectCount - 1; i4 >= 0; i4--) {
                    if (visibleRectArray[i4].bottom > height) {
                        int overflowedHeight2 = visibleRectArray[i4].bottom - height;
                        for (int j2 = 0; j2 <= i4; j2++) {
                            if (visibleRectGroup[i4] == visibleRectGroup[j2]) {
                                visibleRectArray[j2].set(visibleRectArray[j2].left, visibleRectArray[j2].top - overflowedHeight2, visibleRectArray[j2].right, visibleRectArray[j2].bottom - overflowedHeight2);
                            }
                        }
                    }
                }
                setMeasuredDimension(widthSpecSize, heightSpecSize);
            }

            /* access modifiers changed from: protected */
            @Override // android.view.ViewGroup, android.view.View
            public void onLayout(boolean changed, int l, int t, int r, int b) {
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() != 8) {
                        child.layout(this.mRectArray[i].left + paddingLeft, this.mRectArray[i].top + paddingTop, this.mRectArray[i].right + paddingTop, this.mRectArray[i].bottom + paddingLeft);
                    }
                }
            }

            @Override // android.view.ViewGroup, android.view.View
            public void dispatchDraw(Canvas canvas) {
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() != 8) {
                        if (i < this.mRectArray.length) {
                            int saveCount = canvas.save();
                            canvas.translate((float) (this.mRectArray[i].left + paddingLeft), (float) (this.mRectArray[i].top + paddingTop));
                            child.draw(canvas);
                            canvas.restoreToCount(saveCount);
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class CCLayout extends ScaledLayout implements ClosedCaptionWidget.ClosedCaptionLayout {
            private static final float SAFE_TITLE_AREA_SCALE_END_X = 0.9f;
            private static final float SAFE_TITLE_AREA_SCALE_END_Y = 0.9f;
            private static final float SAFE_TITLE_AREA_SCALE_START_X = 0.1f;
            private static final float SAFE_TITLE_AREA_SCALE_START_Y = 0.1f;
            private final ScaledLayout mSafeTitleAreaLayout;

            CCLayout(Context context) {
                super(context);
                this.mSafeTitleAreaLayout = new ScaledLayout(context);
                addView(this.mSafeTitleAreaLayout, new ScaledLayout.ScaledLayoutParams(0.1f, 0.9f, 0.1f, 0.9f));
            }

            public void addOrUpdateViewToSafeTitleArea(CCWindowLayout captionWindowLayout, ScaledLayout.ScaledLayoutParams scaledLayoutParams) {
                if (this.mSafeTitleAreaLayout.indexOfChild(captionWindowLayout) < 0) {
                    this.mSafeTitleAreaLayout.addView(captionWindowLayout, scaledLayoutParams);
                } else {
                    this.mSafeTitleAreaLayout.updateViewLayout(captionWindowLayout, scaledLayoutParams);
                }
            }

            public void removeViewFromSafeTitleArea(CCWindowLayout captionWindowLayout) {
                this.mSafeTitleAreaLayout.removeView(captionWindowLayout);
            }

            @Override // android.support.v4.media.subtitle.ClosedCaptionWidget.ClosedCaptionLayout
            public void setCaptionStyle(CaptioningManager.CaptionStyle style) {
                int count = this.mSafeTitleAreaLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    ((CCWindowLayout) this.mSafeTitleAreaLayout.getChildAt(i)).setCaptionStyle(style);
                }
            }

            @Override // android.support.v4.media.subtitle.ClosedCaptionWidget.ClosedCaptionLayout
            public void setFontScale(float fontScale) {
                int count = this.mSafeTitleAreaLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    ((CCWindowLayout) this.mSafeTitleAreaLayout.getChildAt(i)).setFontScale(fontScale);
                }
            }
        }

        class CCHandler implements Handler.Callback {
            private static final int CAPTION_ALL_WINDOWS_BITMAP = 255;
            private static final long CAPTION_CLEAR_INTERVAL_MS = 60000;
            private static final int CAPTION_WINDOWS_MAX = 8;
            private static final boolean DEBUG = false;
            private static final int MSG_CAPTION_CLEAR = 2;
            private static final int MSG_DELAY_CANCEL = 1;
            private static final String TAG = "CCHandler";
            private static final int TENTHS_OF_SECOND_IN_MILLIS = 100;
            private final CCLayout mCCLayout;
            private final CCWindowLayout[] mCaptionWindowLayouts = new CCWindowLayout[8];
            private CCWindowLayout mCurrentWindowLayout;
            private final Handler mHandler;
            private boolean mIsDelayed = DEBUG;
            private final ArrayList<Cea708CCParser.CaptionEvent> mPendingCaptionEvents = new ArrayList<>();

            CCHandler(CCLayout ccLayout) {
                this.mCCLayout = ccLayout;
                this.mHandler = new Handler(this);
            }

            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        delayCancel();
                        return true;
                    case 2:
                        clearWindows(255);
                        return true;
                    default:
                        return DEBUG;
                }
            }

            public void processCaptionEvent(Cea708CCParser.CaptionEvent event) {
                if (this.mIsDelayed) {
                    this.mPendingCaptionEvents.add(event);
                    return;
                }
                switch (event.type) {
                    case 1:
                        sendBufferToCurrentWindow((String) event.obj);
                        return;
                    case 2:
                        sendControlToCurrentWindow(((Character) event.obj).charValue());
                        return;
                    case 3:
                        setCurrentWindowLayout(((Integer) event.obj).intValue());
                        return;
                    case 4:
                        clearWindows(((Integer) event.obj).intValue());
                        return;
                    case 5:
                        displayWindows(((Integer) event.obj).intValue());
                        return;
                    case 6:
                        hideWindows(((Integer) event.obj).intValue());
                        return;
                    case 7:
                        toggleWindows(((Integer) event.obj).intValue());
                        return;
                    case 8:
                        deleteWindows(((Integer) event.obj).intValue());
                        return;
                    case 9:
                        delay(((Integer) event.obj).intValue());
                        return;
                    case 10:
                        delayCancel();
                        return;
                    case 11:
                        reset();
                        return;
                    case 12:
                        setPenAttr((Cea708CCParser.CaptionPenAttr) event.obj);
                        return;
                    case 13:
                        setPenColor((Cea708CCParser.CaptionPenColor) event.obj);
                        return;
                    case 14:
                        setPenLocation((Cea708CCParser.CaptionPenLocation) event.obj);
                        return;
                    case 15:
                        setWindowAttr((Cea708CCParser.CaptionWindowAttr) event.obj);
                        return;
                    case 16:
                        defineWindow((Cea708CCParser.CaptionWindow) event.obj);
                        return;
                    default:
                        return;
                }
            }

            private void setCurrentWindowLayout(int windowId) {
                CCWindowLayout windowLayout;
                if (windowId >= 0 && windowId < this.mCaptionWindowLayouts.length && (windowLayout = this.mCaptionWindowLayouts[windowId]) != null) {
                    this.mCurrentWindowLayout = windowLayout;
                }
            }

            private ArrayList<CCWindowLayout> getWindowsFromBitmap(int windowBitmap) {
                CCWindowLayout windowLayout;
                ArrayList<CCWindowLayout> windows = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    if (!(((1 << i) & windowBitmap) == 0 || (windowLayout = this.mCaptionWindowLayouts[i]) == null)) {
                        windows.add(windowLayout);
                    }
                }
                return windows;
            }

            private void clearWindows(int windowBitmap) {
                if (windowBitmap != 0) {
                    Iterator<CCWindowLayout> it = getWindowsFromBitmap(windowBitmap).iterator();
                    while (it.hasNext()) {
                        it.next().clear();
                    }
                }
            }

            private void displayWindows(int windowBitmap) {
                if (windowBitmap != 0) {
                    Iterator<CCWindowLayout> it = getWindowsFromBitmap(windowBitmap).iterator();
                    while (it.hasNext()) {
                        it.next().show();
                    }
                }
            }

            private void hideWindows(int windowBitmap) {
                if (windowBitmap != 0) {
                    Iterator<CCWindowLayout> it = getWindowsFromBitmap(windowBitmap).iterator();
                    while (it.hasNext()) {
                        it.next().hide();
                    }
                }
            }

            private void toggleWindows(int windowBitmap) {
                if (windowBitmap != 0) {
                    Iterator<CCWindowLayout> it = getWindowsFromBitmap(windowBitmap).iterator();
                    while (it.hasNext()) {
                        CCWindowLayout windowLayout = it.next();
                        if (windowLayout.isShown()) {
                            windowLayout.hide();
                        } else {
                            windowLayout.show();
                        }
                    }
                }
            }

            private void deleteWindows(int windowBitmap) {
                if (windowBitmap != 0) {
                    Iterator<CCWindowLayout> it = getWindowsFromBitmap(windowBitmap).iterator();
                    while (it.hasNext()) {
                        CCWindowLayout windowLayout = it.next();
                        windowLayout.removeFromCaptionView();
                        this.mCaptionWindowLayouts[windowLayout.getCaptionWindowId()] = null;
                    }
                }
            }

            public void reset() {
                this.mCurrentWindowLayout = null;
                this.mIsDelayed = DEBUG;
                this.mPendingCaptionEvents.clear();
                for (int i = 0; i < 8; i++) {
                    if (this.mCaptionWindowLayouts[i] != null) {
                        this.mCaptionWindowLayouts[i].removeFromCaptionView();
                    }
                    this.mCaptionWindowLayouts[i] = null;
                }
                this.mCCLayout.setVisibility(4);
                this.mHandler.removeMessages(2);
            }

            private void setWindowAttr(Cea708CCParser.CaptionWindowAttr windowAttr) {
                if (this.mCurrentWindowLayout != null) {
                    this.mCurrentWindowLayout.setWindowAttr(windowAttr);
                }
            }

            private void defineWindow(Cea708CCParser.CaptionWindow window) {
                int windowId;
                if (window != null && (windowId = window.id) >= 0 && windowId < this.mCaptionWindowLayouts.length) {
                    CCWindowLayout windowLayout = this.mCaptionWindowLayouts[windowId];
                    if (windowLayout == null) {
                        windowLayout = new CCWindowLayout(Cea708CCWidget.this, this.mCCLayout.getContext());
                    }
                    windowLayout.initWindow(this.mCCLayout, window);
                    this.mCaptionWindowLayouts[windowId] = windowLayout;
                    this.mCurrentWindowLayout = windowLayout;
                }
            }

            private void delay(int tenthsOfSeconds) {
                if (tenthsOfSeconds >= 0 && tenthsOfSeconds <= 255) {
                    this.mIsDelayed = true;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), (long) (tenthsOfSeconds * 100));
                }
            }

            private void delayCancel() {
                this.mIsDelayed = DEBUG;
                processPendingBuffer();
            }

            private void processPendingBuffer() {
                Iterator<Cea708CCParser.CaptionEvent> it = this.mPendingCaptionEvents.iterator();
                while (it.hasNext()) {
                    processCaptionEvent(it.next());
                }
                this.mPendingCaptionEvents.clear();
            }

            private void sendControlToCurrentWindow(char control) {
                if (this.mCurrentWindowLayout != null) {
                    this.mCurrentWindowLayout.sendControl(control);
                }
            }

            private void sendBufferToCurrentWindow(String buffer) {
                if (this.mCurrentWindowLayout != null) {
                    this.mCurrentWindowLayout.sendBuffer(buffer);
                    this.mHandler.removeMessages(2);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), CAPTION_CLEAR_INTERVAL_MS);
                }
            }

            private void setPenAttr(Cea708CCParser.CaptionPenAttr attr) {
                if (this.mCurrentWindowLayout != null) {
                    this.mCurrentWindowLayout.setPenAttr(attr);
                }
            }

            private void setPenColor(Cea708CCParser.CaptionPenColor color) {
                if (this.mCurrentWindowLayout != null) {
                    this.mCurrentWindowLayout.setPenColor(color);
                }
            }

            private void setPenLocation(Cea708CCParser.CaptionPenLocation location) {
                if (this.mCurrentWindowLayout != null) {
                    this.mCurrentWindowLayout.setPenLocation(location.row, location.column);
                }
            }
        }

        /* access modifiers changed from: private */
        public class CCWindowLayout extends RelativeLayout implements View.OnLayoutChangeListener {
            private static final int ANCHOR_HORIZONTAL_16_9_MAX = 209;
            private static final int ANCHOR_HORIZONTAL_MODE_CENTER = 1;
            private static final int ANCHOR_HORIZONTAL_MODE_LEFT = 0;
            private static final int ANCHOR_HORIZONTAL_MODE_RIGHT = 2;
            private static final int ANCHOR_MODE_DIVIDER = 3;
            private static final int ANCHOR_RELATIVE_POSITIONING_MAX = 99;
            private static final int ANCHOR_VERTICAL_MAX = 74;
            private static final int ANCHOR_VERTICAL_MODE_BOTTOM = 2;
            private static final int ANCHOR_VERTICAL_MODE_CENTER = 1;
            private static final int ANCHOR_VERTICAL_MODE_TOP = 0;
            private static final int MAX_COLUMN_COUNT_16_9 = 42;
            private static final float PROPORTION_PEN_SIZE_LARGE = 1.25f;
            private static final float PROPORTION_PEN_SIZE_SMALL = 0.75f;
            private static final String TAG = "CCWindowLayout";
            private final SpannableStringBuilder mBuilder;
            private CCLayout mCCLayout;
            private CCView mCCView;
            private CaptioningManager.CaptionStyle mCaptionStyle;
            private int mCaptionWindowId;
            private final List<CharacterStyle> mCharacterStyles;
            private float mFontScale;
            private int mLastCaptionLayoutHeight;
            private int mLastCaptionLayoutWidth;
            private int mRow;
            private int mRowLimit;
            private float mTextSize;
            private String mWidestChar;

            CCWindowLayout(Cea708CCWidget cea708CCWidget, Context context) {
                this(cea708CCWidget, context, null);
            }

            CCWindowLayout(Cea708CCWidget cea708CCWidget, Context context, AttributeSet attrs) {
                this(cea708CCWidget, context, attrs, 0);
            }

            CCWindowLayout(Cea708CCWidget cea708CCWidget, Context context, AttributeSet attrs, int defStyleAttr) {
                this(context, attrs, defStyleAttr, 0);
            }

            CCWindowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
                super(context, attrs, defStyleAttr, defStyleRes);
                this.mRowLimit = 0;
                this.mBuilder = new SpannableStringBuilder();
                this.mCharacterStyles = new ArrayList();
                this.mRow = -1;
                this.mCCView = new CCView(Cea708CCWidget.this, context);
                addView(this.mCCView, new RelativeLayout.LayoutParams(-2, -2));
                CaptioningManager captioningManager = (CaptioningManager) context.getSystemService("captioning");
                this.mFontScale = captioningManager.getFontScale();
                setCaptionStyle(captioningManager.getUserStyle());
                this.mCCView.setText("");
                updateWidestChar();
            }

            public void setCaptionStyle(CaptioningManager.CaptionStyle style) {
                this.mCaptionStyle = style;
                this.mCCView.setCaptionStyle(style);
            }

            public void setFontScale(float fontScale) {
                this.mFontScale = fontScale;
                updateTextSize();
            }

            public int getCaptionWindowId() {
                return this.mCaptionWindowId;
            }

            public void setCaptionWindowId(int captionWindowId) {
                this.mCaptionWindowId = captionWindowId;
            }

            public void clear() {
                clearText();
                hide();
            }

            public void show() {
                setVisibility(0);
                requestLayout();
            }

            public void hide() {
                setVisibility(4);
                requestLayout();
            }

            public void setPenAttr(Cea708CCParser.CaptionPenAttr penAttr) {
                this.mCharacterStyles.clear();
                if (penAttr.italic) {
                    this.mCharacterStyles.add(new StyleSpan(2));
                }
                if (penAttr.underline) {
                    this.mCharacterStyles.add(new UnderlineSpan());
                }
                int i = penAttr.penSize;
                if (i == 0) {
                    this.mCharacterStyles.add(new RelativeSizeSpan((float) PROPORTION_PEN_SIZE_SMALL));
                } else if (i == 2) {
                    this.mCharacterStyles.add(new RelativeSizeSpan((float) PROPORTION_PEN_SIZE_LARGE));
                }
                int i2 = penAttr.penOffset;
                if (i2 == 0) {
                    this.mCharacterStyles.add(new SubscriptSpan());
                } else if (i2 == 2) {
                    this.mCharacterStyles.add(new SuperscriptSpan());
                }
            }

            public void setPenColor(Cea708CCParser.CaptionPenColor penColor) {
            }

            public void setPenLocation(int row, int column) {
                if (this.mRow >= 0) {
                    for (int r = this.mRow; r < row; r++) {
                        appendText("\n");
                    }
                }
                this.mRow = row;
            }

            public void setWindowAttr(Cea708CCParser.CaptionWindowAttr windowAttr) {
            }

            public void sendBuffer(String buffer) {
                appendText(buffer);
            }

            public void sendControl(char control) {
            }

            public void initWindow(CCLayout ccLayout, Cea708CCParser.CaptionWindow captionWindow) {
                if (this.mCCLayout != ccLayout) {
                    if (this.mCCLayout != null) {
                        this.mCCLayout.removeOnLayoutChangeListener(this);
                    }
                    this.mCCLayout = ccLayout;
                    this.mCCLayout.addOnLayoutChangeListener(this);
                    updateWidestChar();
                }
                float f = (float) captionWindow.anchorVertical;
                boolean z = captionWindow.relativePositioning;
                int i = ANCHOR_RELATIVE_POSITIONING_MAX;
                float scaleRow = f / ((float) (z ? ANCHOR_RELATIVE_POSITIONING_MAX : ANCHOR_VERTICAL_MAX));
                float f2 = (float) captionWindow.anchorHorizontal;
                if (!captionWindow.relativePositioning) {
                    i = ANCHOR_HORIZONTAL_16_9_MAX;
                }
                float scaleCol = f2 / ((float) i);
                if (scaleRow < 0.0f || scaleRow > 1.0f) {
                    Log.i(TAG, "The vertical position of the anchor point should be at the range of 0 and 1 but " + scaleRow);
                    scaleRow = Math.max(0.0f, Math.min(scaleRow, 1.0f));
                }
                if (scaleCol < 0.0f || scaleCol > 1.0f) {
                    Log.i(TAG, "The horizontal position of the anchor point should be at the range of 0 and 1 but " + scaleCol);
                    scaleCol = Math.max(0.0f, Math.min(scaleCol, 1.0f));
                }
                int gravity = 17;
                int horizontalMode = captionWindow.anchorId % 3;
                int verticalMode = captionWindow.anchorId / 3;
                float scaleStartRow = 0.0f;
                float scaleEndRow = 1.0f;
                float scaleStartCol = 0.0f;
                float scaleEndCol = 1.0f;
                switch (horizontalMode) {
                    case 0:
                        gravity = 3;
                        this.mCCView.setAlignment(Layout.Alignment.ALIGN_NORMAL);
                        scaleStartCol = scaleCol;
                        break;
                    case 1:
                        float gap = Math.min(1.0f - scaleCol, scaleCol);
                        int columnCount = Math.min(getScreenColumnCount(), captionWindow.columnCount + 1);
                        StringBuilder widestTextBuilder = new StringBuilder();
                        for (int i2 = 0; i2 < columnCount; i2++) {
                            widestTextBuilder.append(this.mWidestChar);
                        }
                        Paint paint = new Paint();
                        paint.setTypeface(this.mCaptionStyle.getTypeface());
                        paint.setTextSize(this.mTextSize);
                        float maxWindowWidth = this.mCCLayout.getWidth() > 0 ? (paint.measureText(widestTextBuilder.toString()) / 2.0f) / (((float) this.mCCLayout.getWidth()) * 0.8f) : 0.0f;
                        if (maxWindowWidth <= 0.0f || maxWindowWidth >= scaleCol) {
                            gravity = 1;
                            this.mCCView.setAlignment(Layout.Alignment.ALIGN_CENTER);
                            scaleStartCol = scaleCol - gap;
                            scaleEndCol = scaleCol + gap;
                            break;
                        } else {
                            this.mCCView.setAlignment(Layout.Alignment.ALIGN_NORMAL);
                            scaleStartCol = scaleCol - maxWindowWidth;
                            scaleEndCol = 1.0f;
                            gravity = 3;
                            break;
                        }
                        break;
                    case 2:
                        gravity = 5;
                        scaleEndCol = scaleCol;
                        break;
                }
                switch (verticalMode) {
                    case 0:
                        gravity |= 48;
                        scaleStartRow = scaleRow;
                        break;
                    case 1:
                        gravity |= 16;
                        float gap2 = Math.min(1.0f - scaleRow, scaleRow);
                        scaleStartRow = scaleRow - gap2;
                        scaleEndRow = scaleRow + gap2;
                        break;
                    case 2:
                        gravity |= 80;
                        scaleEndRow = scaleRow;
                        break;
                }
                CCLayout cCLayout = this.mCCLayout;
                CCLayout cCLayout2 = this.mCCLayout;
                cCLayout2.getClass();
                cCLayout.addOrUpdateViewToSafeTitleArea(this, new ScaledLayout.ScaledLayoutParams(scaleStartRow, scaleEndRow, scaleStartCol, scaleEndCol));
                setCaptionWindowId(captionWindow.id);
                setRowLimit(captionWindow.rowCount);
                setGravity(gravity);
                if (captionWindow.visible) {
                    show();
                } else {
                    hide();
                }
            }

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int width = right - left;
                int height = bottom - top;
                if (width != this.mLastCaptionLayoutWidth || height != this.mLastCaptionLayoutHeight) {
                    this.mLastCaptionLayoutWidth = width;
                    this.mLastCaptionLayoutHeight = height;
                    updateTextSize();
                }
            }

            private void updateWidestChar() {
                Paint paint = new Paint();
                paint.setTypeface(this.mCaptionStyle.getTypeface());
                Charset latin1 = Charset.forName("ISO-8859-1");
                float widestCharWidth = 0.0f;
                for (int i = 0; i < 256; i++) {
                    String ch = new String(new byte[]{(byte) i}, latin1);
                    float charWidth = paint.measureText(ch);
                    if (widestCharWidth < charWidth) {
                        widestCharWidth = charWidth;
                        this.mWidestChar = ch;
                    }
                }
                updateTextSize();
            }

            /* JADX INFO: Multiple debug info for r6v6 float: [D('testTextSize' float), D('endFontSize' float)] */
            private void updateTextSize() {
                if (this.mCCLayout != null) {
                    StringBuilder widestTextBuilder = new StringBuilder();
                    int screenColumnCount = getScreenColumnCount();
                    for (int i = 0; i < screenColumnCount; i++) {
                        widestTextBuilder.append(this.mWidestChar);
                    }
                    String widestText = widestTextBuilder.toString();
                    Paint paint = new Paint();
                    paint.setTypeface(this.mCaptionStyle.getTypeface());
                    float startFontSize = 0.0f;
                    float endFontSize = 255.0f;
                    while (startFontSize < endFontSize) {
                        float testTextSize = (startFontSize + endFontSize) / 2.0f;
                        paint.setTextSize(testTextSize);
                        if (((float) this.mCCLayout.getWidth()) * 0.8f > paint.measureText(widestText)) {
                            startFontSize = 0.01f + testTextSize;
                        } else {
                            endFontSize = testTextSize - 0.01f;
                        }
                    }
                    this.mTextSize = this.mFontScale * endFontSize;
                    this.mCCView.setTextSize(this.mTextSize);
                }
            }

            private int getScreenColumnCount() {
                return 42;
            }

            public void removeFromCaptionView() {
                if (this.mCCLayout != null) {
                    this.mCCLayout.removeViewFromSafeTitleArea(this);
                    this.mCCLayout.removeOnLayoutChangeListener(this);
                    this.mCCLayout = null;
                }
            }

            public void setText(String text) {
                updateText(text, false);
            }

            public void appendText(String text) {
                updateText(text, true);
            }

            public void clearText() {
                this.mBuilder.clear();
                this.mCCView.setText("");
            }

            /* JADX INFO: Multiple debug info for r2v6 int: [D('end' int), D('start' int)] */
            private void updateText(String text, boolean appended) {
                if (!appended) {
                    this.mBuilder.clear();
                }
                if (text != null && text.length() > 0) {
                    int length = this.mBuilder.length();
                    this.mBuilder.append((CharSequence) text);
                    for (CharacterStyle characterStyle : this.mCharacterStyles) {
                        this.mBuilder.setSpan(characterStyle, length, this.mBuilder.length(), 33);
                    }
                }
                String[] lines = TextUtils.split(this.mBuilder.toString(), "\n");
                this.mBuilder.delete(0, this.mBuilder.length() - TextUtils.join("\n", Arrays.copyOfRange(lines, Math.max(0, lines.length - (this.mRowLimit + 1)), lines.length)).length());
                int last = this.mBuilder.length() - 1;
                int start = 0;
                int end = last;
                while (start <= end && this.mBuilder.charAt(start) <= ' ') {
                    start++;
                }
                while (end >= start && this.mBuilder.charAt(end) <= ' ') {
                    end--;
                }
                if (start == 0 && end == last) {
                    this.mCCView.setText(this.mBuilder);
                    return;
                }
                SpannableStringBuilder trim = new SpannableStringBuilder();
                trim.append((CharSequence) this.mBuilder);
                if (end < last) {
                    trim.delete(end + 1, last + 1);
                }
                if (start > 0) {
                    trim.delete(0, start);
                }
                this.mCCView.setText(trim);
            }

            public void setRowLimit(int rowLimit) {
                if (rowLimit >= 0) {
                    this.mRowLimit = rowLimit;
                    return;
                }
                throw new IllegalArgumentException("A rowLimit should have a positive number");
            }
        }

        /* access modifiers changed from: package-private */
        public class CCView extends SubtitleView {
            CCView(Cea708CCWidget this$12, Context context) {
                this(this$12, context, null);
            }

            CCView(Cea708CCWidget this$12, Context context, AttributeSet attrs) {
                this(this$12, context, attrs, 0);
            }

            CCView(Cea708CCWidget this$12, Context context, AttributeSet attrs, int defStyleAttr) {
                this(context, attrs, defStyleAttr, 0);
            }

            CCView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
                super(context, attrs, defStyleAttr, defStyleRes);
            }

            /* access modifiers changed from: package-private */
            public void setCaptionStyle(CaptioningManager.CaptionStyle style) {
                if (style.hasForegroundColor()) {
                    setForegroundColor(style.foregroundColor);
                }
                if (style.hasBackgroundColor()) {
                    setBackgroundColor(style.backgroundColor);
                }
                if (style.hasEdgeType()) {
                    setEdgeType(style.edgeType);
                }
                if (style.hasEdgeColor()) {
                    setEdgeColor(style.edgeColor);
                }
                setTypeface(style.getTypeface());
            }
        }
    }
}

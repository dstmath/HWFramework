package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.media.SubtitleTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.accessibility.CaptioningManager;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

public class SubtitleController {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int WHAT_HIDE = 2;
    private static final int WHAT_SELECT_DEFAULT_TRACK = 4;
    private static final int WHAT_SELECT_TRACK = 3;
    private static final int WHAT_SHOW = 1;
    private Anchor mAnchor;
    private final Handler.Callback mCallback = new Handler.Callback() {
        /* class android.media.SubtitleController.AnonymousClass1 */

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                SubtitleController.this.doShow();
                return true;
            } else if (i == 2) {
                SubtitleController.this.doHide();
                return true;
            } else if (i == 3) {
                SubtitleController.this.doSelectTrack((SubtitleTrack) msg.obj);
                return true;
            } else if (i != 4) {
                return false;
            } else {
                SubtitleController.this.doSelectDefaultTrack();
                return true;
            }
        }
    };
    private CaptioningManager.CaptioningChangeListener mCaptioningChangeListener = new CaptioningManager.CaptioningChangeListener() {
        /* class android.media.SubtitleController.AnonymousClass2 */

        @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
        public void onEnabledChanged(boolean enabled) {
            SubtitleController.this.selectDefaultTrack();
        }

        @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
        public void onLocaleChanged(Locale locale) {
            SubtitleController.this.selectDefaultTrack();
        }
    };
    private CaptioningManager mCaptioningManager;
    @UnsupportedAppUsage
    private Handler mHandler;
    private Listener mListener;
    private Vector<Renderer> mRenderers;
    private SubtitleTrack mSelectedTrack;
    private boolean mShowing;
    private MediaTimeProvider mTimeProvider;
    private boolean mTrackIsExplicit = false;
    private Vector<SubtitleTrack> mTracks;
    private boolean mVisibilityIsExplicit = false;

    public interface Anchor {
        Looper getSubtitleLooper();

        void setSubtitleWidget(SubtitleTrack.RenderingWidget renderingWidget);
    }

    public interface Listener {
        void onSubtitleTrackSelected(SubtitleTrack subtitleTrack);
    }

    public static abstract class Renderer {
        public abstract SubtitleTrack createTrack(MediaFormat mediaFormat);

        public abstract boolean supports(MediaFormat mediaFormat);
    }

    @UnsupportedAppUsage
    public SubtitleController(Context context, MediaTimeProvider timeProvider, Listener listener) {
        this.mTimeProvider = timeProvider;
        this.mListener = listener;
        this.mRenderers = new Vector<>();
        this.mShowing = false;
        this.mTracks = new Vector<>();
        this.mCaptioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.mCaptioningManager.removeCaptioningChangeListener(this.mCaptioningChangeListener);
        super.finalize();
    }

    public SubtitleTrack[] getTracks() {
        SubtitleTrack[] tracks;
        synchronized (this.mTracks) {
            tracks = new SubtitleTrack[this.mTracks.size()];
            this.mTracks.toArray(tracks);
        }
        return tracks;
    }

    public SubtitleTrack getSelectedTrack() {
        return this.mSelectedTrack;
    }

    private SubtitleTrack.RenderingWidget getRenderingWidget() {
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack == null) {
            return null;
        }
        return subtitleTrack.getRenderingWidget();
    }

    public boolean selectTrack(SubtitleTrack track) {
        if (track != null && !this.mTracks.contains(track)) {
            return false;
        }
        processOnAnchor(this.mHandler.obtainMessage(3, track));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doSelectTrack(SubtitleTrack track) {
        this.mTrackIsExplicit = true;
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack != track) {
            if (subtitleTrack != null) {
                subtitleTrack.hide();
                this.mSelectedTrack.setTimeProvider(null);
            }
            this.mSelectedTrack = track;
            Anchor anchor = this.mAnchor;
            if (anchor != null) {
                anchor.setSubtitleWidget(getRenderingWidget());
            }
            SubtitleTrack subtitleTrack2 = this.mSelectedTrack;
            if (subtitleTrack2 != null) {
                subtitleTrack2.setTimeProvider(this.mTimeProvider);
                this.mSelectedTrack.show();
            }
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onSubtitleTrackSelected(track);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00a7  */
    public SubtitleTrack getDefaultTrack() {
        Locale locale;
        boolean languageMatches;
        int score;
        SubtitleTrack bestTrack = null;
        int bestScore = -1;
        Locale selectedLocale = this.mCaptioningManager.getLocale();
        if (selectedLocale == null) {
            locale = Locale.getDefault();
        } else {
            locale = selectedLocale;
        }
        int i = 1;
        boolean selectForced = !this.mCaptioningManager.isEnabled();
        synchronized (this.mTracks) {
            Iterator<SubtitleTrack> it = this.mTracks.iterator();
            while (it.hasNext()) {
                SubtitleTrack track = it.next();
                MediaFormat format = track.getFormat();
                String language = format.getString("language");
                int i2 = format.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, 0) != 0 ? i : 0;
                int i3 = format.getInteger(MediaFormat.KEY_IS_AUTOSELECT, i) != 0 ? i : 0;
                int i4 = format.getInteger(MediaFormat.KEY_IS_DEFAULT, 0) != 0 ? i : 0;
                if (locale != null && !locale.getLanguage().equals("") && !locale.getISO3Language().equals(language)) {
                    if (!locale.getLanguage().equals(language)) {
                        languageMatches = false;
                        score = (i2 == 0 ? 0 : 8) + ((selectedLocale == null || i4 == 0) ? 0 : 4) + (i3 == 0 ? 0 : 2) + (!languageMatches ? 1 : 0);
                        if (selectForced || i2 != 0) {
                            if (((selectedLocale == null && i4 != 0) || (languageMatches && !(i3 == 0 && i2 == 0 && selectedLocale == null))) && score > bestScore) {
                                bestScore = score;
                                bestTrack = track;
                            }
                            i = 1;
                        } else {
                            i = 1;
                        }
                    }
                }
                languageMatches = true;
                score = (i2 == 0 ? 0 : 8) + ((selectedLocale == null || i4 == 0) ? 0 : 4) + (i3 == 0 ? 0 : 2) + (!languageMatches ? 1 : 0);
                if (selectForced) {
                }
                bestScore = score;
                bestTrack = track;
                i = 1;
            }
        }
        return bestTrack;
    }

    public void selectDefaultTrack() {
        processOnAnchor(this.mHandler.obtainMessage(4));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doSelectDefaultTrack() {
        SubtitleTrack subtitleTrack;
        if (!this.mTrackIsExplicit) {
            SubtitleTrack track = getDefaultTrack();
            if (track != null) {
                selectTrack(track);
                this.mTrackIsExplicit = false;
                if (!this.mVisibilityIsExplicit) {
                    show();
                    this.mVisibilityIsExplicit = false;
                }
            }
        } else if (!this.mVisibilityIsExplicit) {
            if (this.mCaptioningManager.isEnabled() || !((subtitleTrack = this.mSelectedTrack) == null || subtitleTrack.getFormat().getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, 0) == 0)) {
                show();
            } else {
                SubtitleTrack subtitleTrack2 = this.mSelectedTrack;
                if (subtitleTrack2 != null && subtitleTrack2.getTrackType() == 4) {
                    hide();
                }
            }
            this.mVisibilityIsExplicit = false;
        }
    }

    @UnsupportedAppUsage
    public void reset() {
        checkAnchorLooper();
        hide();
        selectTrack(null);
        this.mTracks.clear();
        this.mTrackIsExplicit = false;
        this.mVisibilityIsExplicit = false;
        this.mCaptioningManager.removeCaptioningChangeListener(this.mCaptioningChangeListener);
    }

    public SubtitleTrack addTrack(MediaFormat format) {
        SubtitleTrack track;
        synchronized (this.mRenderers) {
            Iterator<Renderer> it = this.mRenderers.iterator();
            while (it.hasNext()) {
                Renderer renderer = it.next();
                if (renderer.supports(format) && (track = renderer.createTrack(format)) != null) {
                    synchronized (this.mTracks) {
                        if (this.mTracks.size() == 0) {
                            this.mCaptioningManager.addCaptioningChangeListener(this.mCaptioningChangeListener);
                        }
                        this.mTracks.add(track);
                    }
                    return track;
                }
            }
            return null;
        }
    }

    @UnsupportedAppUsage
    public void show() {
        processOnAnchor(this.mHandler.obtainMessage(1));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doShow() {
        this.mShowing = true;
        this.mVisibilityIsExplicit = true;
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack != null) {
            subtitleTrack.show();
        }
    }

    @UnsupportedAppUsage
    public void hide() {
        processOnAnchor(this.mHandler.obtainMessage(2));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHide() {
        this.mVisibilityIsExplicit = true;
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack != null) {
            subtitleTrack.hide();
        }
        this.mShowing = false;
    }

    @UnsupportedAppUsage
    public void registerRenderer(Renderer renderer) {
        synchronized (this.mRenderers) {
            if (!this.mRenderers.contains(renderer)) {
                this.mRenderers.add(renderer);
            }
        }
    }

    public boolean hasRendererFor(MediaFormat format) {
        synchronized (this.mRenderers) {
            Iterator<Renderer> it = this.mRenderers.iterator();
            while (it.hasNext()) {
                if (it.next().supports(format)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setAnchor(Anchor anchor) {
        Anchor anchor2 = this.mAnchor;
        if (anchor2 != anchor) {
            if (anchor2 != null) {
                checkAnchorLooper();
                this.mAnchor.setSubtitleWidget(null);
            }
            this.mAnchor = anchor;
            this.mHandler = null;
            Anchor anchor3 = this.mAnchor;
            if (anchor3 != null) {
                this.mHandler = new Handler(anchor3.getSubtitleLooper(), this.mCallback);
                checkAnchorLooper();
                this.mAnchor.setSubtitleWidget(getRenderingWidget());
            }
        }
    }

    private void checkAnchorLooper() {
    }

    private void processOnAnchor(Message m) {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.mHandler.dispatchMessage(m);
        } else {
            this.mHandler.sendMessage(m);
        }
    }
}

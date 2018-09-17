package android.media;

import android.content.Context;
import android.media.SubtitleTrack.RenderingWidget;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import java.util.Locale;
import java.util.Vector;

public class SubtitleController {
    static final /* synthetic */ boolean -assertionsDisabled = (SubtitleController.class.desiredAssertionStatus() ^ 1);
    private static final String TAG = "SubtitleController";
    private static final int WHAT_HIDE = 2;
    private static final int WHAT_SELECT_DEFAULT_TRACK = 4;
    private static final int WHAT_SELECT_TRACK = 3;
    private static final int WHAT_SHOW = 1;
    private Anchor mAnchor;
    private final Callback mCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SubtitleController.this.doShow();
                    return true;
                case 2:
                    SubtitleController.this.doHide();
                    return true;
                case 3:
                    SubtitleController.this.doSelectTrack((SubtitleTrack) msg.obj);
                    return true;
                case 4:
                    SubtitleController.this.doSelectDefaultTrack();
                    return true;
                default:
                    return false;
            }
        }
    };
    private CaptioningChangeListener mCaptioningChangeListener = new CaptioningChangeListener() {
        public void onEnabledChanged(boolean enabled) {
            SubtitleController.this.selectDefaultTrack();
        }

        public void onLocaleChanged(Locale locale) {
            SubtitleController.this.selectDefaultTrack();
        }
    };
    private CaptioningManager mCaptioningManager;
    private Handler mHandler;
    private Listener mListener;
    private Vector<Renderer> mRenderers;
    private SubtitleTrack mSelectedTrack;
    private boolean mShowing;
    private MediaTimeProvider mTimeProvider;
    private boolean mTrackIsExplicit = false;
    private Vector<SubtitleTrack> mTracks;
    private boolean mVisibilityIsExplicit = false;

    public static abstract class Renderer {
        public abstract SubtitleTrack createTrack(MediaFormat mediaFormat);

        public abstract boolean supports(MediaFormat mediaFormat);
    }

    public interface Anchor {
        Looper getSubtitleLooper();

        void setSubtitleWidget(RenderingWidget renderingWidget);
    }

    public interface Listener {
        void onSubtitleTrackSelected(SubtitleTrack subtitleTrack);
    }

    public SubtitleController(Context context, MediaTimeProvider timeProvider, Listener listener) {
        this.mTimeProvider = timeProvider;
        this.mListener = listener;
        this.mRenderers = new Vector();
        this.mShowing = false;
        this.mTracks = new Vector();
        this.mCaptioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
    }

    protected void finalize() throws Throwable {
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

    private RenderingWidget getRenderingWidget() {
        if (this.mSelectedTrack == null) {
            return null;
        }
        return this.mSelectedTrack.getRenderingWidget();
    }

    public boolean selectTrack(SubtitleTrack track) {
        if (track != null && (this.mTracks.contains(track) ^ 1) != 0) {
            return false;
        }
        processOnAnchor(this.mHandler.obtainMessage(3, track));
        return true;
    }

    private void doSelectTrack(SubtitleTrack track) {
        this.mTrackIsExplicit = true;
        if (this.mSelectedTrack != track) {
            if (this.mSelectedTrack != null) {
                this.mSelectedTrack.hide();
                this.mSelectedTrack.setTimeProvider(null);
            }
            this.mSelectedTrack = track;
            if (this.mAnchor != null) {
                this.mAnchor.setSubtitleWidget(getRenderingWidget());
            }
            if (this.mSelectedTrack != null) {
                this.mSelectedTrack.setTimeProvider(this.mTimeProvider);
                this.mSelectedTrack.show();
            }
            if (this.mListener != null) {
                this.mListener.onSubtitleTrackSelected(track);
            }
        }
    }

    public SubtitleTrack getDefaultTrack() {
        SubtitleTrack bestTrack = null;
        int bestScore = -1;
        Locale selectedLocale = this.mCaptioningManager.getLocale();
        Locale locale = selectedLocale;
        if (selectedLocale == null) {
            locale = Locale.getDefault();
        }
        boolean selectForced = this.mCaptioningManager.isEnabled() ^ 1;
        synchronized (this.mTracks) {
            for (SubtitleTrack track : this.mTracks) {
                boolean languageMatches;
                MediaFormat format = track.getFormat();
                String language = format.getString(MediaFormat.KEY_LANGUAGE);
                boolean forced = format.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, 0) != 0;
                boolean autoselect = format.getInteger(MediaFormat.KEY_IS_AUTOSELECT, 1) != 0;
                boolean is_default = format.getInteger(MediaFormat.KEY_IS_DEFAULT, 0) != 0;
                if (locale == null || locale.getLanguage().equals(ProxyInfo.LOCAL_EXCL_LIST) || locale.getISO3Language().equals(language)) {
                    languageMatches = true;
                } else {
                    languageMatches = locale.getLanguage().equals(language);
                }
                int i = forced ? 0 : 8;
                int i2 = (selectedLocale == null && is_default) ? 4 : 0;
                int score = ((i + i2) + (autoselect ? 0 : 2)) + (languageMatches ? 1 : 0);
                if ((!selectForced || (forced ^ 1) == 0) && (((selectedLocale == null && is_default) || (languageMatches && (autoselect || forced || selectedLocale != null))) && score > bestScore)) {
                    bestScore = score;
                    bestTrack = track;
                }
            }
        }
        return bestTrack;
    }

    public void selectDefaultTrack() {
        if (this.mHandler == null) {
            Log.e(TAG, "selectDefaultTrack: mHandler is null");
        } else {
            processOnAnchor(this.mHandler.obtainMessage(4));
        }
    }

    private void doSelectDefaultTrack() {
        if (this.mTrackIsExplicit) {
            if (!this.mVisibilityIsExplicit) {
                if (this.mCaptioningManager.isEnabled() || !(this.mSelectedTrack == null || this.mSelectedTrack.getFormat().getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, 0) == 0)) {
                    show();
                } else if (this.mSelectedTrack != null && this.mSelectedTrack.getTrackType() == 4) {
                    hide();
                }
                this.mVisibilityIsExplicit = false;
            }
            return;
        }
        SubtitleTrack track = getDefaultTrack();
        if (track != null) {
            selectTrack(track);
            this.mTrackIsExplicit = false;
            if (!this.mVisibilityIsExplicit) {
                show();
                this.mVisibilityIsExplicit = false;
            }
        }
    }

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
        synchronized (this.mRenderers) {
            for (Renderer renderer : this.mRenderers) {
                if (renderer.supports(format)) {
                    SubtitleTrack track = renderer.createTrack(format);
                    if (track != null) {
                        synchronized (this.mTracks) {
                            if (this.mTracks.size() == 0) {
                                this.mCaptioningManager.addCaptioningChangeListener(this.mCaptioningChangeListener);
                            }
                            this.mTracks.add(track);
                        }
                        return track;
                    }
                }
            }
            return null;
        }
    }

    public void show() {
        processOnAnchor(this.mHandler.obtainMessage(1));
    }

    private void doShow() {
        this.mShowing = true;
        this.mVisibilityIsExplicit = true;
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.show();
        }
    }

    public void hide() {
        processOnAnchor(this.mHandler.obtainMessage(2));
    }

    private void doHide() {
        this.mVisibilityIsExplicit = true;
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.hide();
        }
        this.mShowing = false;
    }

    public void registerRenderer(Renderer renderer) {
        synchronized (this.mRenderers) {
            if (!this.mRenderers.contains(renderer)) {
                this.mRenderers.add(renderer);
            }
        }
    }

    public boolean hasRendererFor(MediaFormat format) {
        synchronized (this.mRenderers) {
            for (Renderer renderer : this.mRenderers) {
                if (renderer.supports(format)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setAnchor(Anchor anchor) {
        if (this.mAnchor != anchor) {
            if (this.mAnchor != null) {
                checkAnchorLooper();
                this.mAnchor.setSubtitleWidget(null);
            }
            this.mAnchor = anchor;
            this.mHandler = null;
            if (this.mAnchor != null) {
                this.mHandler = new Handler(this.mAnchor.getSubtitleLooper(), this.mCallback);
                checkAnchorLooper();
                this.mAnchor.setSubtitleWidget(getRenderingWidget());
            }
        }
    }

    private void checkAnchorLooper() {
        if (!-assertionsDisabled && this.mHandler == null) {
            throw new AssertionError("Should have a looper already");
        } else if (!-assertionsDisabled && Looper.myLooper() != this.mHandler.getLooper()) {
            throw new AssertionError("Must be called from the anchor's looper");
        }
    }

    private void processOnAnchor(Message m) {
        if (!-assertionsDisabled && this.mHandler == null) {
            throw new AssertionError("Should have a looper already");
        } else if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.mHandler.dispatchMessage(m);
        } else {
            this.mHandler.sendMessage(m);
        }
    }
}

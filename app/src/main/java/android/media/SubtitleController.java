package android.media;

import android.content.Context;
import android.media.SubtitleTrack.RenderingWidget;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech.Engine;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import java.util.Locale;
import java.util.Vector;

public class SubtitleController {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int WHAT_HIDE = 2;
    private static final int WHAT_SELECT_DEFAULT_TRACK = 4;
    private static final int WHAT_SELECT_TRACK = 3;
    private static final int WHAT_SHOW = 1;
    private Anchor mAnchor;
    private final Callback mCallback;
    private CaptioningChangeListener mCaptioningChangeListener;
    private CaptioningManager mCaptioningManager;
    private Handler mHandler;
    private Listener mListener;
    private Vector<Renderer> mRenderers;
    private SubtitleTrack mSelectedTrack;
    private boolean mShowing;
    private MediaTimeProvider mTimeProvider;
    private boolean mTrackIsExplicit;
    private Vector<SubtitleTrack> mTracks;
    private boolean mVisibilityIsExplicit;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.SubtitleController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.SubtitleController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleController.<clinit>():void");
    }

    public SubtitleController(Context context, MediaTimeProvider timeProvider, Listener listener) {
        this.mCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case SubtitleController.WHAT_SHOW /*1*/:
                        SubtitleController.this.doShow();
                        return true;
                    case SubtitleController.WHAT_HIDE /*2*/:
                        SubtitleController.this.doHide();
                        return true;
                    case SubtitleController.WHAT_SELECT_TRACK /*3*/:
                        SubtitleController.this.doSelectTrack((SubtitleTrack) msg.obj);
                        return true;
                    case SubtitleController.WHAT_SELECT_DEFAULT_TRACK /*4*/:
                        SubtitleController.this.doSelectDefaultTrack();
                        return true;
                    default:
                        return SubtitleController.-assertionsDisabled;
                }
            }
        };
        this.mCaptioningChangeListener = new CaptioningChangeListener() {
            public void onEnabledChanged(boolean enabled) {
                SubtitleController.this.selectDefaultTrack();
            }

            public void onLocaleChanged(Locale locale) {
                SubtitleController.this.selectDefaultTrack();
            }
        };
        this.mTrackIsExplicit = -assertionsDisabled;
        this.mVisibilityIsExplicit = -assertionsDisabled;
        this.mTimeProvider = timeProvider;
        this.mListener = listener;
        this.mRenderers = new Vector();
        this.mShowing = -assertionsDisabled;
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
        if (track != null && !this.mTracks.contains(track)) {
            return -assertionsDisabled;
        }
        processOnAnchor(this.mHandler.obtainMessage(WHAT_SELECT_TRACK, track));
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
        boolean selectForced = this.mCaptioningManager.isEnabled() ? -assertionsDisabled : true;
        synchronized (this.mTracks) {
            for (SubtitleTrack track : this.mTracks) {
                boolean languageMatches;
                MediaFormat format = track.getFormat();
                String language = format.getString(Engine.KEY_PARAM_LANGUAGE);
                boolean forced = format.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, 0) != 0 ? true : -assertionsDisabled;
                boolean autoselect = format.getInteger(MediaFormat.KEY_IS_AUTOSELECT, WHAT_SHOW) != 0 ? true : -assertionsDisabled;
                boolean is_default = format.getInteger(MediaFormat.KEY_IS_DEFAULT, 0) != 0 ? true : -assertionsDisabled;
                if (locale == null || locale.getLanguage().equals(ProxyInfo.LOCAL_EXCL_LIST) || locale.getISO3Language().equals(language)) {
                    languageMatches = true;
                } else {
                    languageMatches = locale.getLanguage().equals(language);
                }
                int i = forced ? 0 : 8;
                int i2 = (selectedLocale == null && is_default) ? WHAT_SELECT_DEFAULT_TRACK : 0;
                int score = ((i + i2) + (autoselect ? 0 : WHAT_HIDE)) + (languageMatches ? WHAT_SHOW : 0);
                if ((!selectForced || forced) && (((selectedLocale == null && is_default) || (languageMatches && (autoselect || forced || selectedLocale != null))) && score > bestScore)) {
                    bestScore = score;
                    bestTrack = track;
                }
            }
        }
        return bestTrack;
    }

    public void selectDefaultTrack() {
        processOnAnchor(this.mHandler.obtainMessage(WHAT_SELECT_DEFAULT_TRACK));
    }

    private void doSelectDefaultTrack() {
        if (this.mTrackIsExplicit) {
            if (!this.mVisibilityIsExplicit) {
                if (this.mCaptioningManager.isEnabled() || !(this.mSelectedTrack == null || this.mSelectedTrack.getFormat().getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, 0) == 0)) {
                    show();
                } else if (this.mSelectedTrack != null && this.mSelectedTrack.getTrackType() == WHAT_SELECT_DEFAULT_TRACK) {
                    hide();
                }
                this.mVisibilityIsExplicit = -assertionsDisabled;
            }
            return;
        }
        SubtitleTrack track = getDefaultTrack();
        if (track != null) {
            selectTrack(track);
            this.mTrackIsExplicit = -assertionsDisabled;
            if (!this.mVisibilityIsExplicit) {
                show();
                this.mVisibilityIsExplicit = -assertionsDisabled;
            }
        }
    }

    public void reset() {
        checkAnchorLooper();
        hide();
        selectTrack(null);
        this.mTracks.clear();
        this.mTrackIsExplicit = -assertionsDisabled;
        this.mVisibilityIsExplicit = -assertionsDisabled;
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
        processOnAnchor(this.mHandler.obtainMessage(WHAT_SHOW));
    }

    private void doShow() {
        this.mShowing = true;
        this.mVisibilityIsExplicit = true;
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.show();
        }
    }

    public void hide() {
        processOnAnchor(this.mHandler.obtainMessage(WHAT_HIDE));
    }

    private void doHide() {
        this.mVisibilityIsExplicit = true;
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.hide();
        }
        this.mShowing = -assertionsDisabled;
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
            return -assertionsDisabled;
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
        Object obj = WHAT_SHOW;
        if (!-assertionsDisabled) {
            if ((this.mHandler != null ? WHAT_SHOW : null) == null) {
                throw new AssertionError("Should have a looper already");
            }
        }
        if (!-assertionsDisabled) {
            if (Looper.myLooper() != this.mHandler.getLooper()) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError("Must be called from the anchor's looper");
            }
        }
    }

    private void processOnAnchor(Message m) {
        if (!-assertionsDisabled) {
            if ((this.mHandler != null ? WHAT_SHOW : null) == null) {
                throw new AssertionError("Should have a looper already");
            }
        }
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.mHandler.dispatchMessage(m);
        } else {
            this.mHandler.sendMessage(m);
        }
    }
}

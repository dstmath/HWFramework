package ohos.media.common.sessioncore;

import java.util.ArrayList;
import java.util.List;
import ohos.media.common.utils.AVUtils;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class AVPlaybackState implements Sequenceable {
    public static Sequenceable.Producer<AVPlaybackState> CREATOR = $$Lambda$AVPlaybackState$gpqafLTXnbMT_jaWlUSkO2xLURk.INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVPlaybackState.class);
    public static final long PLAYBACK_ACTION_FAST_FORWARD = 64;
    public static final long PLAYBACK_ACTION_PAUSE = 2;
    public static final long PLAYBACK_ACTION_PLAY = 4;
    public static final long PLAYBACK_ACTION_PLAY_FROM_MEDIA_ID = 1024;
    public static final long PLAYBACK_ACTION_PLAY_FROM_SEARCH = 2048;
    public static final long PLAYBACK_ACTION_PLAY_FROM_URI = 8192;
    public static final long PLAYBACK_ACTION_PLAY_PAUSE = 512;
    public static final long PLAYBACK_ACTION_PREPARE = 16384;
    public static final long PLAYBACK_ACTION_PREPARE_FROM_MEDIA_ID = 32768;
    public static final long PLAYBACK_ACTION_PREPARE_FROM_SEARCH = 65536;
    public static final long PLAYBACK_ACTION_PREPARE_FROM_URI = 131072;
    public static final long PLAYBACK_ACTION_REWIND = 8;
    public static final long PLAYBACK_ACTION_SEEK_TO = 256;
    public static final long PLAYBACK_ACTION_SET_RATING = 128;
    public static final long PLAYBACK_ACTION_SKIP_TO_NEXT = 32;
    public static final long PLAYBACK_ACTION_SKIP_TO_PREVIOUS = 16;
    public static final long PLAYBACK_ACTION_SKIP_TO_QUEUE_ITEM = 4096;
    public static final long PLAYBACK_ACTION_STOP = 1;
    public static final long PLAYBACK_POSITION_ERROR = -1;
    public static final int PLAYBACK_STATE_BUFFERING = 6;
    public static final int PLAYBACK_STATE_CONNECTING = 8;
    public static final int PLAYBACK_STATE_ERROR = 7;
    public static final int PLAYBACK_STATE_FAST_FORWARDING = 4;
    public static final int PLAYBACK_STATE_NONE = 0;
    public static final int PLAYBACK_STATE_PAUSED = 2;
    public static final int PLAYBACK_STATE_PLAYING = 3;
    public static final int PLAYBACK_STATE_REWINDING = 5;
    public static final int PLAYBACK_STATE_SKIPPING_TO_NEXT = 10;
    public static final int PLAYBACK_STATE_SKIPPING_TO_PREVIOUS = 9;
    public static final int PLAYBACK_STATE_SKIPPING_TO_QUEUE_ITEM = 11;
    public static final int PLAYBACK_STATE_STOPPED = 1;
    private final long actions;
    private final long activeElementId;
    private final long bufferedPosition;
    private List<AVPlaybackCustomAction> customActions;
    private final CharSequence errorMessage;
    private final PacMap options;
    private final long position;
    private final float speed;
    private final int state;
    private final long updateTime;

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    static /* synthetic */ AVPlaybackState lambda$static$0(Parcel parcel) {
        return new AVPlaybackState(parcel);
    }

    private AVPlaybackState(int i, long j, long j2, float f, long j3, long j4, List<AVPlaybackCustomAction> list, long j5, CharSequence charSequence, PacMap pacMap) {
        this.state = i;
        this.position = j;
        this.speed = f;
        this.updateTime = j2;
        this.bufferedPosition = j3;
        this.actions = j4;
        this.customActions = new ArrayList(list);
        this.activeElementId = j5;
        this.errorMessage = charSequence;
        this.options = pacMap;
    }

    private AVPlaybackState(Parcel parcel) {
        this.state = parcel.readInt();
        this.position = parcel.readLong();
        this.speed = parcel.readFloat();
        this.updateTime = parcel.readLong();
        this.bufferedPosition = parcel.readLong();
        this.actions = parcel.readLong();
        this.customActions = parcel.readSequenceableList(AVPlaybackCustomAction.class);
        this.activeElementId = parcel.readLong();
        this.errorMessage = parcel.readString();
        this.options = new PacMap();
        parcel.readSequenceable(this.options);
    }

    public String toString() {
        return "AVPlaybackState {state=" + this.state + ", position=" + this.position + ", buffered position=" + this.bufferedPosition + ", speed=" + this.speed + ", updated=" + this.updateTime + ", actions=" + this.actions + ", custom actions=" + this.customActions + ", active element id=" + this.activeElementId + ", error=" + this.errorMessage + "}";
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.state);
        parcel.writeLong(this.position);
        parcel.writeFloat(this.speed);
        parcel.writeLong(this.updateTime);
        parcel.writeLong(this.bufferedPosition);
        parcel.writeLong(this.actions);
        parcel.writeSequenceableList(this.customActions);
        parcel.writeLong(this.activeElementId);
        parcel.writeString(this.errorMessage.toString());
        parcel.writeSequenceable(this.options);
        return true;
    }

    public int getAVPlaybackState() {
        return this.state;
    }

    public long getCurrentPosition() {
        return this.position;
    }

    public long getCurrentBufferedPosition() {
        return this.bufferedPosition;
    }

    public float getAVPlaybackSpeed() {
        return this.speed;
    }

    public long getAVPlaybackActions() {
        return this.actions;
    }

    public List<AVPlaybackCustomAction> getAVPlaybackCustomActions() {
        return this.customActions;
    }

    public CharSequence getAVPlaybackErrorMessage() {
        return this.errorMessage;
    }

    public long getLastPositionChangedTime() {
        return this.updateTime;
    }

    public long getActiveQueueElementId() {
        return this.activeElementId;
    }

    public PacMap getOptions() {
        return this.options;
    }

    public static final class AVPlaybackCustomAction implements Sequenceable {
        public static final Sequenceable.Producer<AVPlaybackCustomAction> CREATOR = $$Lambda$AVPlaybackState$AVPlaybackCustomAction$h021TNzJnjTEesbRJusxdsFyDM.INSTANCE;
        private final String action;
        private final int icon;
        private final CharSequence name;
        private final PacMap options;

        @Override // ohos.utils.Sequenceable
        public boolean unmarshalling(Parcel parcel) {
            return false;
        }

        static /* synthetic */ AVPlaybackCustomAction lambda$static$0(Parcel parcel) {
            return new AVPlaybackCustomAction(parcel);
        }

        private AVPlaybackCustomAction(String str, CharSequence charSequence, int i, PacMap pacMap) {
            this.action = str;
            this.name = charSequence;
            this.icon = i;
            this.options = pacMap;
        }

        private AVPlaybackCustomAction(Parcel parcel) {
            this.action = parcel.readString();
            this.name = parcel.readString();
            this.icon = parcel.readInt();
            this.options = new PacMap();
            parcel.readSequenceable(this.options);
        }

        @Override // ohos.utils.Sequenceable
        public boolean marshalling(Parcel parcel) {
            parcel.writeString(this.action);
            parcel.writeString(this.name.toString());
            parcel.writeInt(this.icon);
            parcel.writeSequenceable(this.options);
            return true;
        }

        public String getAVPlaybackAction() {
            return this.action;
        }

        public CharSequence getAVPlaybackActionName() {
            return this.name;
        }

        public int getResourceIdOfIcon() {
            return this.icon;
        }

        public PacMap getOptions() {
            return this.options;
        }

        public String toString() {
            return "Action:name=" + ((Object) this.name) + ", icon=" + this.icon + ", options=" + this.options;
        }

        public static final class Builder {
            private final String action;
            private final int icon;
            private final CharSequence name;
            private PacMap options;

            public Builder(String str, CharSequence charSequence, int i) {
                if (str == null || str.length() == 0) {
                    throw new IllegalArgumentException("You must specify an action to build a AVPlaybackCustomAction.");
                } else if (charSequence == null || charSequence.length() == 0) {
                    throw new IllegalArgumentException("You must specify a name to build a AVPlaybackCustomAction.");
                } else if (i != 0) {
                    this.action = str;
                    this.name = charSequence;
                    this.icon = i;
                } else {
                    throw new IllegalArgumentException("You must specify an icon resource id to build a AVPlaybackCustomAction.");
                }
            }

            public Builder setOptions(PacMap pacMap) {
                this.options = pacMap;
                return this;
            }

            public AVPlaybackCustomAction build() {
                return new AVPlaybackCustomAction(this.action, this.name, this.icon, this.options);
            }
        }
    }

    public static final class Builder {
        private long actions;
        private long activeElementId = -1;
        private long bufferedPosition;
        private final List<AVPlaybackCustomAction> customActions = new ArrayList();
        private CharSequence errorMessage;
        private PacMap options;
        private long position;
        private float speed;
        private int state;
        private long updateTime;

        public Builder() {
        }

        public Builder(AVPlaybackState aVPlaybackState) {
            if (aVPlaybackState != null) {
                this.state = aVPlaybackState.state;
                this.position = aVPlaybackState.position;
                this.bufferedPosition = aVPlaybackState.bufferedPosition;
                this.speed = aVPlaybackState.speed;
                this.actions = aVPlaybackState.actions;
                if (aVPlaybackState.customActions != null) {
                    this.customActions.addAll(aVPlaybackState.customActions);
                }
                this.errorMessage = aVPlaybackState.errorMessage;
                this.updateTime = aVPlaybackState.updateTime;
                this.activeElementId = aVPlaybackState.activeElementId;
                this.options = aVPlaybackState.options;
            }
        }

        public Builder setAVPlaybackState(int i, long j, float f, long j2) {
            this.state = i;
            this.position = j;
            this.updateTime = j2;
            this.speed = f;
            return this;
        }

        public Builder setAVPlaybackState(int i, long j, float f) {
            return setAVPlaybackState(i, j, f, AVUtils.elapsedRealtime());
        }

        public Builder setAVPlaybackActions(long j) {
            this.actions = j;
            return this;
        }

        public Builder addAVPlaybackCustomAction(String str, String str2, int i) {
            return addAVPlaybackCustomAction(new AVPlaybackCustomAction(str, str2, i, null));
        }

        public Builder addAVPlaybackCustomAction(AVPlaybackCustomAction aVPlaybackCustomAction) {
            if (aVPlaybackCustomAction != null) {
                this.customActions.add(aVPlaybackCustomAction);
                return this;
            }
            throw new IllegalArgumentException("You may not add a null customAction to AVPlaybackState.");
        }

        public Builder setCurrentBufferedPosition(long j) {
            this.bufferedPosition = j;
            return this;
        }

        public Builder setActiveQueueElementId(long j) {
            this.activeElementId = j;
            return this;
        }

        public Builder setAVPlaybackErrorMessage(CharSequence charSequence) {
            this.errorMessage = charSequence;
            return this;
        }

        public Builder setOptions(PacMap pacMap) {
            this.options = pacMap;
            return this;
        }

        public AVPlaybackState build() {
            return new AVPlaybackState(this.state, this.position, this.updateTime, this.speed, this.bufferedPosition, this.actions, this.customActions, this.activeElementId, this.errorMessage, this.options);
        }
    }
}

package ohos.media.common.adapter;

import android.media.session.PlaybackState;
import java.util.List;
import ohos.media.common.sessioncore.AVPlaybackState;
import ohos.media.common.utils.AVUtils;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVPlaybackStateAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVPlaybackStateAdapter.class);

    public static PlaybackState.CustomAction getCustomAction(AVPlaybackState.AVPlaybackCustomAction aVPlaybackCustomAction) {
        if (aVPlaybackCustomAction != null) {
            return new PlaybackState.CustomAction.Builder(aVPlaybackCustomAction.getAVPlaybackAction(), aVPlaybackCustomAction.getAVPlaybackActionName(), aVPlaybackCustomAction.getResourceIdOfIcon()).setExtras(AVUtils.convert2Bundle(aVPlaybackCustomAction.getOptions())).build();
        }
        LOGGER.error("getCustomAction failed, avCustomAction is null", new Object[0]);
        return null;
    }

    public static PlaybackState getPlaybackState(AVPlaybackState aVPlaybackState) {
        if (aVPlaybackState == null) {
            LOGGER.error("getPlaybackState failed, avPlaybackState is null", new Object[0]);
            return null;
        }
        PlaybackState.Builder extras = new PlaybackState.Builder().setState(aVPlaybackState.getAVPlaybackState(), aVPlaybackState.getCurrentPosition(), aVPlaybackState.getAVPlaybackSpeed(), aVPlaybackState.getLastPositionChangedTime()).setActions(aVPlaybackState.getAVPlaybackActions()).setBufferedPosition(aVPlaybackState.getCurrentBufferedPosition()).setActiveQueueItemId(aVPlaybackState.getActiveQueueElementId()).setErrorMessage(aVPlaybackState.getAVPlaybackErrorMessage()).setExtras(AVUtils.convert2Bundle(aVPlaybackState.getOptions()));
        List<AVPlaybackState.AVPlaybackCustomAction> aVPlaybackCustomActions = aVPlaybackState.getAVPlaybackCustomActions();
        if (aVPlaybackCustomActions != null) {
            for (AVPlaybackState.AVPlaybackCustomAction aVPlaybackCustomAction : aVPlaybackCustomActions) {
                extras.addCustomAction(getCustomAction(aVPlaybackCustomAction));
            }
        }
        return extras.build();
    }

    public static AVPlaybackState.AVPlaybackCustomAction getAVPlaybackCustomAction(PlaybackState.CustomAction customAction) {
        if (customAction != null) {
            return new AVPlaybackState.AVPlaybackCustomAction.Builder(customAction.getAction(), customAction.getName(), customAction.getIcon()).setOptions(AVUtils.convert2PacMap(customAction.getExtras())).build();
        }
        LOGGER.error("getAVPlaybackCustomAction failed, customAction is null", new Object[0]);
        return null;
    }

    public static AVPlaybackState getAVPlaybackState(PlaybackState playbackState) {
        if (playbackState == null) {
            LOGGER.error("getAVPlaybackState failed, playbackState is null", new Object[0]);
            return null;
        }
        AVPlaybackState.Builder options = new AVPlaybackState.Builder().setAVPlaybackState(playbackState.getState(), playbackState.getPosition(), playbackState.getPlaybackSpeed(), playbackState.getLastPositionUpdateTime()).setAVPlaybackActions(playbackState.getActions()).setCurrentBufferedPosition(playbackState.getBufferedPosition()).setActiveQueueElementId(playbackState.getActiveQueueItemId()).setAVPlaybackErrorMessage(playbackState.getErrorMessage()).setOptions(AVUtils.convert2PacMap(playbackState.getExtras()));
        List<PlaybackState.CustomAction> customActions = playbackState.getCustomActions();
        if (customActions != null) {
            for (PlaybackState.CustomAction customAction : customActions) {
                options.addAVPlaybackCustomAction(getAVPlaybackCustomAction(customAction));
            }
        }
        return options.build();
    }
}

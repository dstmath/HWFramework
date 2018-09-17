package android.support.v4.media.session;

import android.media.session.PlaybackState;
import android.media.session.PlaybackState.CustomAction.Builder;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import java.util.Iterator;
import java.util.List;

@RequiresApi(21)
class PlaybackStateCompatApi21 {

    static final class CustomAction {
        CustomAction() {
        }

        public static String getAction(Object customActionObj) {
            return ((android.media.session.PlaybackState.CustomAction) customActionObj).getAction();
        }

        public static CharSequence getName(Object customActionObj) {
            return ((android.media.session.PlaybackState.CustomAction) customActionObj).getName();
        }

        public static int getIcon(Object customActionObj) {
            return ((android.media.session.PlaybackState.CustomAction) customActionObj).getIcon();
        }

        public static Bundle getExtras(Object customActionObj) {
            return ((android.media.session.PlaybackState.CustomAction) customActionObj).getExtras();
        }

        public static Object newInstance(String action, CharSequence name, int icon, Bundle extras) {
            Builder customActionObj = new Builder(action, name, icon);
            customActionObj.setExtras(extras);
            return customActionObj.build();
        }
    }

    PlaybackStateCompatApi21() {
    }

    public static int getState(Object stateObj) {
        return ((PlaybackState) stateObj).getState();
    }

    public static long getPosition(Object stateObj) {
        return ((PlaybackState) stateObj).getPosition();
    }

    public static long getBufferedPosition(Object stateObj) {
        return ((PlaybackState) stateObj).getBufferedPosition();
    }

    public static float getPlaybackSpeed(Object stateObj) {
        return ((PlaybackState) stateObj).getPlaybackSpeed();
    }

    public static long getActions(Object stateObj) {
        return ((PlaybackState) stateObj).getActions();
    }

    public static CharSequence getErrorMessage(Object stateObj) {
        return ((PlaybackState) stateObj).getErrorMessage();
    }

    public static long getLastPositionUpdateTime(Object stateObj) {
        return ((PlaybackState) stateObj).getLastPositionUpdateTime();
    }

    public static List<Object> getCustomActions(Object stateObj) {
        return ((PlaybackState) stateObj).getCustomActions();
    }

    public static long getActiveQueueItemId(Object stateObj) {
        return ((PlaybackState) stateObj).getActiveQueueItemId();
    }

    public static Object newInstance(int state, long position, long bufferedPosition, float speed, long actions, CharSequence errorMessage, long updateTime, List<Object> customActions, long activeItemId) {
        PlaybackState.Builder stateObj = new PlaybackState.Builder();
        stateObj.setState(state, position, speed, updateTime);
        stateObj.setBufferedPosition(bufferedPosition);
        stateObj.setActions(actions);
        stateObj.setErrorMessage(errorMessage);
        Iterator customAction$iterator = customActions.iterator();
        while (customAction$iterator.hasNext()) {
            stateObj.addCustomAction((android.media.session.PlaybackState.CustomAction) customAction$iterator.next());
        }
        stateObj.setActiveQueueItemId(activeItemId);
        return stateObj.build();
    }
}

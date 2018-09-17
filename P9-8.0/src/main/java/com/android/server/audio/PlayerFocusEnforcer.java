package com.android.server.audio;

public interface PlayerFocusEnforcer {
    boolean duckPlayers(FocusRequester focusRequester, FocusRequester focusRequester2);

    void mutePlayersForCall(int[] iArr);

    void unduckPlayers(FocusRequester focusRequester);

    void unmutePlayersForCall();
}

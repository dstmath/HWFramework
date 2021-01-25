package com.android.server.notification;

public interface RankingHandler {
    void requestReconsideration(RankingReconsideration rankingReconsideration);

    void requestSort();
}

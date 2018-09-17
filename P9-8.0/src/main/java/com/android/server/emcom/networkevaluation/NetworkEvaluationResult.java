package com.android.server.emcom.networkevaluation;

public class NetworkEvaluationResult {
    int bottleneck;
    int networkType;
    int quality;

    NetworkEvaluationResult(int quality, int bottleneck, int networkType) {
        this.quality = quality;
        this.bottleneck = bottleneck;
        this.networkType = networkType;
    }

    public int getNetworkType() {
        return this.networkType;
    }

    public int getBottleneck() {
        return this.bottleneck;
    }

    public int getQuality() {
        return this.quality;
    }
}

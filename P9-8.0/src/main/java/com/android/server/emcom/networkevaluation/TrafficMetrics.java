package com.android.server.emcom.networkevaluation;

/* compiled from: WifiInformationClass */
class TrafficMetrics {
    TrafficStatus inAirTrafficStatus;

    TrafficMetrics(TrafficStatus inAirTrafficStatus) {
        this.inAirTrafficStatus = inAirTrafficStatus;
    }

    public String toString() {
        return "traffic status : " + String.valueOf(this.inAirTrafficStatus.ordinal()) + "\n";
    }

    public void setStatus(TrafficStatus trafficStatus) {
        this.inAirTrafficStatus = trafficStatus;
    }
}

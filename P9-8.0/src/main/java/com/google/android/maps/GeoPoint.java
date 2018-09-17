package com.google.android.maps;

import android_maps_conflict_avoidance.com.google.map.MapPoint;

public class GeoPoint {
    private final MapPoint mMapPoint;

    GeoPoint(MapPoint mp) {
        this.mMapPoint = mp;
    }

    public GeoPoint(int latitudeE6, int longitudeE6) {
        if (longitudeE6 == -180000000) {
            longitudeE6 *= -1;
        }
        this.mMapPoint = new MapPoint(latitudeE6, longitudeE6);
    }

    public int getLatitudeE6() {
        return this.mMapPoint.getLatitude();
    }

    public int getLongitudeE6() {
        return this.mMapPoint.getLongitude();
    }

    public String toString() {
        return String.valueOf(this.mMapPoint.getLatitude()) + "," + String.valueOf(this.mMapPoint.getLongitude());
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if ((object instanceof GeoPoint) && ((GeoPoint) object).getMapPoint().equals(getMapPoint())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mMapPoint.hashCode();
    }

    MapPoint getMapPoint() {
        return this.mMapPoint;
    }
}

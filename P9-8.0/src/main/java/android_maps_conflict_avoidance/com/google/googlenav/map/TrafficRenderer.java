package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;

public class TrafficRenderer {

    public interface Path {
        void lineTo(int i, int i2);

        void moveTo(int i, int i2);
    }

    public interface TrafficPainter {
        void addTrafficLine(Path path, int i, int i2);

        Path createPathObject();
    }

    public void renderTrafficTile(TrafficTile tt, TrafficPainter painter) {
        tt.setLastAccess(Config.getInstance().getClock().relativeTimeMillis());
        drawRoadBackgroundLines(tt.getTrafficRoads(), painter);
        drawRoadForegroundLines(tt.getTrafficRoads(), painter);
    }

    private void drawRoadBackgroundLines(TrafficRoad[] roads, TrafficPainter painter) {
        for (TrafficRoad road : roads) {
            Path path = painter.createPathObject();
            addPathPoint(path, road, 0, false);
            int numPoints = road.getNumPoints();
            for (int dataPoint = 1; dataPoint < numPoints; dataPoint++) {
                addPathPoint(path, road, dataPoint, true);
            }
            painter.addTrafficLine(path, -788529153, road.getTrafficLineBackgroundWidth() << 8);
        }
    }

    private void drawRoadForegroundLines(TrafficRoad[] roads, TrafficPainter painter) {
        for (TrafficRoad road : roads) {
            Path path = initPath(painter, road, 0);
            int lastColor = getColor(road, 0);
            int numPoints = road.getNumPoints();
            for (int dataPoint = 1; dataPoint < numPoints; dataPoint++) {
                addPathPoint(path, road, dataPoint, true);
                int color = getColor(road, dataPoint);
                if (color != lastColor) {
                    painter.addTrafficLine(path, lastColor, road.getTrafficLineWidth() << 8);
                    path = initPath(painter, road, dataPoint);
                    lastColor = color;
                }
            }
            painter.addTrafficLine(path, lastColor, road.getTrafficLineWidth() << 8);
        }
    }

    protected void addPathPoint(Path path, TrafficRoad road, int pointIndex, boolean draw) {
        int x = road.getXOffset(pointIndex) << 8;
        int y = road.getYOffset(pointIndex) << 8;
        if (draw) {
            path.lineTo(x, y);
        } else {
            path.moveTo(x, y);
        }
    }

    protected Path initPath(TrafficPainter painter, TrafficRoad road, int pointIndex) {
        Path path = painter.createPathObject();
        addPathPoint(path, road, pointIndex, false);
        return path;
    }

    private static int getColor(TrafficRoad road, int pointIndex) {
        switch (road.getSpeedCategory(pointIndex)) {
            case 1:
                return -6553600;
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                return -6750208;
            case LayoutParams.LEFT /*3*/:
                return -8192;
            case OverlayItem.ITEM_STATE_FOCUSED_MASK /*4*/:
                return -13389568;
            default:
                return 1624363473;
        }
    }
}

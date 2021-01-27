package ohos.ivicommon.drivingsafety;

import java.util.Optional;
import ohos.app.Context;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.ivicommon.drivingsafety.model.Position;

public class DrivingSafetyClient {
    public static int getRestraint() {
        return 0;
    }

    public static boolean isDrivingMode() {
        return false;
    }

    public static boolean isDrivingSafety(Context context) {
        return true;
    }

    public static boolean isDrivingSafety(Context context, ControlItemEnum controlItemEnum) {
        return true;
    }

    public static boolean isDrivingSafety(Context context, ControlItemEnum controlItemEnum, Position position) {
        return true;
    }

    public static boolean isSecondaryScreenRange(Position position) {
        return true;
    }

    private DrivingSafetyClient() {
    }

    public static Optional<Position> getSecondaryScreenRange() {
        return Optional.empty();
    }
}

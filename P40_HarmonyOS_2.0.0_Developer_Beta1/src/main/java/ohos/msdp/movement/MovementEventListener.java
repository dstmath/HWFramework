package ohos.msdp.movement;

import java.util.List;

public interface MovementEventListener {
    void onMovementChanged(List<MovementEvent> list);
}

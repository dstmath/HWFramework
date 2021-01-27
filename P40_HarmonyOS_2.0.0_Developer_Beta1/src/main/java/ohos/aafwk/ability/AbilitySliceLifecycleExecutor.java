package ohos.aafwk.ability;

public class AbilitySliceLifecycleExecutor {

    public enum Action {
        CMD_START,
        CMD_ACTIVE,
        CMD_INACTIVE,
        CMD_BACKGROUND,
        CMD_FOREGROUND,
        CMD_STOP
    }

    public enum LifecycleState {
        INITIAL(0),
        INACTIVE(1),
        ACTIVE(2),
        BACKGROUND(3),
        SUSPENDED(4);
        
        private int value;

        private LifecycleState(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }
}

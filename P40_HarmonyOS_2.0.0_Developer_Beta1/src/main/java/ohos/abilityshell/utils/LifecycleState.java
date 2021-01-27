package ohos.abilityshell.utils;

public class LifecycleState {

    public enum AbilityState {
        INITIAL_STATE(0),
        INACTIVE_STATE(1),
        ACTIVE_STATE(2),
        BACKGROUND_STATE(3),
        END_STATE(4);
        
        private final int value;

        private AbilityState(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum AppState {
        CREATE_STATE(0),
        READY_STATE(1),
        FOREGROUND_STATE(2),
        BACKGROUND_STATE(3),
        TERMINATED_STATE(4),
        END_STATE(5);
        
        private final int value;

        private AppState(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }
}

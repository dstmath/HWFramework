package ohos.aafwk.ability;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class AbilityLifecycleExecutor {

    /* access modifiers changed from: package-private */
    public enum Action {
        STOP(Ability.STATE_INITIAL, "stop"),
        START(Ability.STATE_INACTIVE, "start"),
        INACTIVE(Ability.STATE_INACTIVE, "inactive"),
        ACTIVE(Ability.STATE_ACTIVE, "active"),
        BACKGROUND(Ability.STATE_BACKGROUND, "background"),
        FOREGROUND(Ability.STATE_INACTIVE, "foreground");
        
        private String actionName;
        private int targetState;

        private Action(int i, String str) {
            this.targetState = i;
            this.actionName = str;
        }

        @Override // java.lang.Enum, java.lang.Object
        public String toString() {
            return this.actionName;
        }

        /* access modifiers changed from: package-private */
        public int getTargetState() {
            return this.targetState;
        }
    }

    public enum LifecycleState {
        UNINITIALIZED(-1),
        INITIAL(0),
        INACTIVE(1),
        ACTIVE(2),
        BACKGROUND(3);
        
        private static final Map<Integer, LifecycleState> LIFECYCLE_STATE_MAP = ((Map) Arrays.stream(values()).collect(Collectors.toMap($$Lambda$AbilityLifecycleExecutor$LifecycleState$5u2OrJdb0hLEbpHdtAc2C2RvqA.INSTANCE, $$Lambda$AbilityLifecycleExecutor$LifecycleState$gMuwFw7_24x4ZYwFgGcrX3pHGnM.INSTANCE)));
        private int value;

        static /* synthetic */ LifecycleState lambda$static$1(LifecycleState lifecycleState) {
            return lifecycleState;
        }

        private LifecycleState(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }

        public static LifecycleState intToEnum(int i) {
            return LIFECYCLE_STATE_MAP.get(Integer.valueOf(i));
        }
    }
}

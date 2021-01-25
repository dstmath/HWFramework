package ohos.aafwk.ability;

class AbilityLifecycleExecutor {
    AbilityLifecycleExecutor() {
    }

    /* access modifiers changed from: package-private */
    public enum Action {
        STOP(0, "stop"),
        START(1, "start"),
        INACTIVE(1, "inactive"),
        ACTIVE(2, "active"),
        BACKGROUND(3, "background"),
        FOREGROUND(1, "foreground");
        
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
}

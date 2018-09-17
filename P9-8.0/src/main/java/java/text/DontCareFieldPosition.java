package java.text;

import java.text.Format.Field;

class DontCareFieldPosition extends FieldPosition {
    static final FieldPosition INSTANCE = new DontCareFieldPosition();
    private final FieldDelegate noDelegate = new FieldDelegate() {
        public void formatted(Field attr, Object value, int start, int end, StringBuffer buffer) {
        }

        public void formatted(int fieldID, Field attr, Object value, int start, int end, StringBuffer buffer) {
        }
    };

    private DontCareFieldPosition() {
        super(0);
    }

    FieldDelegate getFieldDelegate() {
        return this.noDelegate;
    }
}

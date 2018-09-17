package libcore.reflect;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;

final class ListOfVariables {
    final ArrayList<TypeVariable<?>> array;

    ListOfVariables() {
        this.array = new ArrayList();
    }

    void add(TypeVariable<?> elem) {
        this.array.add(elem);
    }

    TypeVariable<?>[] getArray() {
        return (TypeVariable[]) this.array.toArray(new TypeVariable[this.array.size()]);
    }
}

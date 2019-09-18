package org.junit.experimental.theories.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.runners.model.TestClass;

public class Assignments {
    private final List<PotentialAssignment> assigned;
    private final TestClass clazz;
    private final List<ParameterSignature> unassigned;

    private Assignments(List<PotentialAssignment> assigned2, List<ParameterSignature> unassigned2, TestClass clazz2) {
        this.unassigned = unassigned2;
        this.assigned = assigned2;
        this.clazz = clazz2;
    }

    public static Assignments allUnassigned(Method testMethod, TestClass testClass) {
        List<ParameterSignature> signatures = ParameterSignature.signatures(testClass.getOnlyConstructor());
        signatures.addAll(ParameterSignature.signatures(testMethod));
        return new Assignments(new ArrayList(), signatures, testClass);
    }

    public boolean isComplete() {
        return this.unassigned.size() == 0;
    }

    public ParameterSignature nextUnassigned() {
        return this.unassigned.get(0);
    }

    public Assignments assignNext(PotentialAssignment source) {
        List<PotentialAssignment> assigned2 = new ArrayList<>(this.assigned);
        assigned2.add(source);
        return new Assignments(assigned2, this.unassigned.subList(1, this.unassigned.size()), this.clazz);
    }

    public Object[] getActualValues(int start, int stop) throws PotentialAssignment.CouldNotGenerateValueException {
        Object[] values = new Object[(stop - start)];
        for (int i = start; i < stop; i++) {
            values[i - start] = this.assigned.get(i).getValue();
        }
        return values;
    }

    public List<PotentialAssignment> potentialsForNextUnassigned() throws Throwable {
        ParameterSignature unassigned2 = nextUnassigned();
        List<PotentialAssignment> assignments = getSupplier(unassigned2).getValueSources(unassigned2);
        if (assignments.size() == 0) {
            return generateAssignmentsFromTypeAlone(unassigned2);
        }
        return assignments;
    }

    private List<PotentialAssignment> generateAssignmentsFromTypeAlone(ParameterSignature unassigned2) {
        Class<?> paramType = unassigned2.getType();
        if (paramType.isEnum()) {
            return new EnumSupplier(paramType).getValueSources(unassigned2);
        }
        if (paramType.equals(Boolean.class) || paramType.equals(Boolean.TYPE)) {
            return new BooleanSupplier().getValueSources(unassigned2);
        }
        return Collections.emptyList();
    }

    private ParameterSupplier getSupplier(ParameterSignature unassigned2) throws Exception {
        ParametersSuppliedBy annotation = (ParametersSuppliedBy) unassigned2.findDeepAnnotation(ParametersSuppliedBy.class);
        if (annotation != null) {
            return buildParameterSupplierFromClass(annotation.value());
        }
        return new AllMembersSupplier(this.clazz);
    }

    private ParameterSupplier buildParameterSupplierFromClass(Class<? extends ParameterSupplier> cls) throws Exception {
        Constructor<?>[] supplierConstructors = cls.getConstructors();
        int length = supplierConstructors.length;
        int i = 0;
        while (i < length) {
            Constructor<?> constructor = supplierConstructors[i];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != 1 || !parameterTypes[0].equals(TestClass.class)) {
                i++;
            } else {
                return (ParameterSupplier) constructor.newInstance(new Object[]{this.clazz});
            }
        }
        return (ParameterSupplier) cls.newInstance();
    }

    public Object[] getConstructorArguments() throws PotentialAssignment.CouldNotGenerateValueException {
        return getActualValues(0, getConstructorParameterCount());
    }

    public Object[] getMethodArguments() throws PotentialAssignment.CouldNotGenerateValueException {
        return getActualValues(getConstructorParameterCount(), this.assigned.size());
    }

    public Object[] getAllArguments() throws PotentialAssignment.CouldNotGenerateValueException {
        return getActualValues(0, this.assigned.size());
    }

    private int getConstructorParameterCount() {
        return ParameterSignature.signatures(this.clazz.getOnlyConstructor()).size();
    }

    public Object[] getArgumentStrings(boolean nullsOk) throws PotentialAssignment.CouldNotGenerateValueException {
        Object[] values = new Object[this.assigned.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = this.assigned.get(i).getDescription();
        }
        return values;
    }
}

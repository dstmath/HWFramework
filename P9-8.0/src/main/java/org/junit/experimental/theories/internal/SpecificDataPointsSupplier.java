package org.junit.experimental.theories.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class SpecificDataPointsSupplier extends AllMembersSupplier {
    public SpecificDataPointsSupplier(TestClass testClass) {
        super(testClass);
    }

    protected Collection<Field> getSingleDataPointFields(ParameterSignature sig) {
        Collection<Field> fields = super.getSingleDataPointFields(sig);
        String requestedName = ((FromDataPoints) sig.getAnnotation(FromDataPoints.class)).value();
        List<Field> fieldsWithMatchingNames = new ArrayList();
        for (Field field : fields) {
            if (Arrays.asList(((DataPoint) field.getAnnotation(DataPoint.class)).value()).contains(requestedName)) {
                fieldsWithMatchingNames.add(field);
            }
        }
        return fieldsWithMatchingNames;
    }

    protected Collection<Field> getDataPointsFields(ParameterSignature sig) {
        Collection<Field> fields = super.getDataPointsFields(sig);
        String requestedName = ((FromDataPoints) sig.getAnnotation(FromDataPoints.class)).value();
        List<Field> fieldsWithMatchingNames = new ArrayList();
        for (Field field : fields) {
            if (Arrays.asList(((DataPoints) field.getAnnotation(DataPoints.class)).value()).contains(requestedName)) {
                fieldsWithMatchingNames.add(field);
            }
        }
        return fieldsWithMatchingNames;
    }

    protected Collection<FrameworkMethod> getSingleDataPointMethods(ParameterSignature sig) {
        Collection<FrameworkMethod> methods = super.getSingleDataPointMethods(sig);
        String requestedName = ((FromDataPoints) sig.getAnnotation(FromDataPoints.class)).value();
        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList();
        for (FrameworkMethod method : methods) {
            if (Arrays.asList(((DataPoint) method.getAnnotation(DataPoint.class)).value()).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        return methodsWithMatchingNames;
    }

    protected Collection<FrameworkMethod> getDataPointsMethods(ParameterSignature sig) {
        Collection<FrameworkMethod> methods = super.getDataPointsMethods(sig);
        String requestedName = ((FromDataPoints) sig.getAnnotation(FromDataPoints.class)).value();
        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList();
        for (FrameworkMethod method : methods) {
            if (Arrays.asList(((DataPoints) method.getAnnotation(DataPoints.class)).value()).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        return methodsWithMatchingNames;
    }
}

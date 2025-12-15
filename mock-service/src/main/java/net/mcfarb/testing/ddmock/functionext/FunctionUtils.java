package net.mcfarb.testing.ddmock.functionext;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.mcfarb.testing.ddmock.model.MockServiceInfo;

public class FunctionUtils {

    public UnaryOperator<String> toCamelOperator = s -> s.substring(0, 1).toLowerCase() + s.substring(1);

    public Function<Class<?>, String> classSimpleName = c -> c.getSimpleName();

    public Function<MockServiceInfo<?>, String> createBeanName = s -> {
        return (s.getBeanName() == null) ? classSimpleName.andThen(toCamelOperator).apply(s.getServiceClass())
                : s.getBeanName();
    };

}
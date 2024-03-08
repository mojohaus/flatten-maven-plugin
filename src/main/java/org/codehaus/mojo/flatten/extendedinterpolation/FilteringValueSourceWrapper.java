package org.codehaus.mojo.flatten.extendedinterpolation;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.codehaus.plexus.interpolation.ValueSource;

public class FilteringValueSourceWrapper {

    private static ClassLoader classLoader;
    private final Object delegate;
    private final Predicate<String> filter;
    private final Class<?> valueSourceClass;

    private FilteringValueSourceWrapper(Object delegate, Predicate<String> expressionFilter) {
        this.delegate = delegate;
        this.filter = expressionFilter;
        try {
            this.valueSourceClass = classLoader.loadClass(ValueSource.class.getName());
        } catch (Exception e) {
            throw new ExtendedModelInterpolatorException(e);
        }
    }

    public static void setClassLoader(ClassLoader classLoader) {
        FilteringValueSourceWrapper.classLoader = classLoader;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<ValueSource> wrap(List<ValueSource> valueSources, Predicate<String> expressionFilter) {
        return (List) new ArrayList<Object>(valueSources)
                .stream()
                        .map(vs -> FilteringValueSourceWrapper.wrap(vs, expressionFilter))
                        .collect(Collectors.toList());
    }

    public static Object wrap(Object valueSource, Predicate<String> expressionFilter) {

        return new FilteringValueSourceWrapper(valueSource, expressionFilter).asProxy();
    }

    public Object asProxy() {

        return Proxy.newProxyInstance(classLoader, new Class[] {this.valueSourceClass}, (proxy, method, args) -> {
            if (method.getName().equals("getValue")) {
                if (args.length != 1 || (args[0] != null && !(args[0] instanceof String))) {
                    throw new InternalError(
                            "The class " + valueSourceClass.getName() + " got a changed getValue method: " + method);
                }
                if (!filter.test((String) args[0])) {
                    return null;
                }
            }
            return method.invoke(this.delegate, args);
        });
    }
}

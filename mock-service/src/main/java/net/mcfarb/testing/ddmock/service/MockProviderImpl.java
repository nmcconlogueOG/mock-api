package net.mcfarb.testing.ddmock.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.mockito.Mockito;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.mcfarb.testing.TestParent;
import net.mcfarb.testing.ddmock.aspects.MockBuilderException;
import net.mcfarb.testing.ddmock.functionext.FunctionUtils;
import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockMethodInfo;
import net.mcfarb.testing.ddmock.model.MockObject;
import net.mcfarb.testing.ddmock.model.MockServiceInfo;
import reactor.core.publisher.Mono;

@Slf4j
public class MockProviderImpl<T extends TestParent, S> implements BeanProvider<T, S> {

    Map<String, Object> objectMap = new HashMap<>();
    Map<String, S> serviceMap = new HashMap<>();

    FunctionUtils utils = new FunctionUtils();

    private MockGeneratorInfo mockGeneratorInfo;

    @Autowired
    @Setter
    JsonProcessor jsonProcessor;

    boolean initialized = false;

    public S getBean(String beanName) {
        if (!initialized) {
            throw new BeanInitializationException(
                    "MockProviderImpl is not initialized. Please call initialize() before accessing beans.");
        }
        S bean = serviceMap.get(beanName);
        if (bean == null) {
            log.info("Bean with name {} not found in serviceMap", beanName);
        }
        return bean;
    }

    public void initialize(MockGeneratorInfo mockGeneratorInfo) throws BeanInitializationException {
        if (initialized) {
            log.warn("MockProviderImpl is already initialized. Skipping initialization.");
            return;
        }
        if (mockGeneratorInfo == null) {
            throw new BeanInitializationException("MockGeneratorInfo cannot be null");
        }
        // build objects from MockObject definitions
        mockGeneratorInfo.getMockObjects().stream().forEach(buildObject);
        // build services from MockServiceInfo definitions
        mockGeneratorInfo.getMockServices().stream().forEach(buildMock);

        initialized = true;
    }

    // build objects from MockObject definitions
    public Consumer<MockObject> buildObject = (mockObject) -> {
        try {
            if (mockObject.getFakeClass() != null) {
                Object fake = null;
                fake = jsonProcessor.buildObject(mockObject, mockObject.getFakeClass(), mockObject.getGenericClass(),
                        mockObject.getKeyClass(), mockObject.getValueClass(), mockObject.getVersion());
                objectMap.put(mockObject.getId(), fake);
            } else {
                throw new MockBuilderException(
                        " Class name must be specified when defining MockObjects. Mock Object with id %s has no associated class.",
                        mockObject.getId());
            }
        } catch (MockBuilderException e) {
            throw new RuntimeException(e);
        }
    };

    public BiConsumer<String, MockMethodInfo> mockMethods = (beanName, mockMethod) -> {
        try {
            S mockedService = serviceMap.get(beanName);
            Class<?>[] arguments = mockMethod.getMethodArguments();
            Method method = mockedService.getClass().getMethod(mockMethod.getMethodName(), arguments);
            // look up object to return for this method
            Class<?> methodReturnType = method.getReturnType();

            String returnString[] = mockMethod.getReturnId().split(",");
            Object returnObject[] = new Object[returnString.length];

            int guard = 0;
            for (String objectName : returnString) {
                returnObject[guard] = objectMap.get(objectName);
                guard++;
            }

            for (int i = 0; i < returnObject.length; i++) {
                if (!objectMap.containsKey(returnString[i])) {
                    throw new MockBuilderException(
                            " Object with name %s Not Found when processing service %s and method %s", returnString[i],
                            beanName, mockMethod.getMethodName());
                }
                if (returnObject[i] != null && methodReturnType.isAssignableFrom(returnObject[i].getClass())) {
                    continue;
                } else if (returnObject != null) {
                    throw new MockBuilderException(
                            "Wrong Object Definition, Class of type %s expecting %s from Method %s ",
                            returnObject.getClass().getName(), methodReturnType, mockMethod.getMethodName());
                }
            }
            mockedService = this.mockWhen(mockedService, method, returnObject);

        } catch (MockBuilderException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException(e);
        }
    };

    // build services from MockServiceInfo definitions
    public Consumer<MockServiceInfo<?>> buildMock = (serviceInfo) -> {
        String beanName = utils.createBeanName.apply(serviceInfo);
        Object mockedService = serviceMap.get(beanName);
        if (mockedService == null) {
            mockedService = Mockito.mock(serviceInfo.getServiceClass());
            serviceMap.put(beanName, (S) mockedService);
        }
        serviceInfo.getMethods().stream().forEach(m -> mockMethods.accept(beanName, m));
    };

    protected S mockWhen(S mockedObject, Method method, Object[] returnObject)
            throws IllegalAccessException, InvocationTargetException {
        Object[] array = new Object[method.getParameterCount()];

        for (int i = 0; i < method.getParameterCount(); i++) {
            Class<?> paramType = method.getParameterTypes()[i];
            if (paramType.isPrimitive()) {
                if (paramType == boolean.class) {
                    array[i] = anyBoolean();
                } else if (paramType == long.class) {
                    array[i] = anyLong();
                } else if (paramType == int.class) {
                    array[i] = anyInt();
                } else if (paramType == double.class) {
                    array[i] = anyDouble();
                } else {
                    throw new IllegalArgumentException("Primitive type " + paramType.getName() + " not accepted");
                }
            } else {
                array[i] = any();
            }
        }
        /*
         * thenReturn(vargargs) does not exist, but thenReturn(foo, vargargs) does exist
         * so we grab the first element and pass it as param 1, then pass the rest of
         * the array as the param 2
         */
        Mockito.when(method.invoke(mockedObject, array)).thenReturn(returnObject[0],
                Arrays.copyOfRange(returnObject, 1, returnObject.length));
        return mockedObject;
    }

}
package net.mcfarb.testing.ddmock.model;

import java.util.List;

public class MockServiceInfo<T> {
    private Class<T> serviceClass;
    private List<MockMethodInfo> methods;
    // may be null defaults to camelCase class name.
    private String beanName;

    public List<MockMethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(List<MockMethodInfo> methods) {
        this.methods = methods;
    }

    public Class<T> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

}
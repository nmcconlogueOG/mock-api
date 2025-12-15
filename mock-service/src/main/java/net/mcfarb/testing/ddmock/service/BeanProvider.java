package net.mcfarb.testing.ddmock.service;

import org.springframework.beans.factory.BeanInitializationException;

import net.mcfarb.testing.TestParent;

import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;

public interface BeanProvider<T extends TestParent, S> {

    public S getBean(String beanName);

    public default void initialize(MockGeneratorInfo mockGeneratorInfo) throws BeanInitializationException {
        // Default implementation does nothing.
    }

}

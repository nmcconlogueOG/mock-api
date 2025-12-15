package net.mcfarb.testing.ddmock.service;

import net.mcfarb.testing.TestParent;
import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;
import reactor.core.publisher.Mono;

public interface MonoBeanProvider<T extends TestParent, S> {

    /**
     * Retrieves a bean by its name.
     *
     * @param beanName the name of the bean to retrieve
     * @return the bean instance
     */
    Mono<S> getBean(String beanName);

}

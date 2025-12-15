package net.mcfarb.testing.ddmock.reactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.Setter;
import net.mcfarb.testing.TestParent;
import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;
import net.mcfarb.testing.ddmock.service.BeanProvider;
import reactor.core.publisher.Mono;

public class MockGenerator<T extends TestParent, S> {

    @Setter
    MockGeneratorInfo mockGeneratorInfo;

    @Autowired
    @Qualifier("mockProviderImpl")
    BeanProvider<T, S> mockProvider;

    @Autowired
    @Qualifier("springBeanProvider")
    BeanProvider<T, S> springBeanProvider;

    public Mono<S> generateMockBean(String beanName, MockGeneratorInfo mockGeneratorInfo) {
        return Mono.just(mockProvider.getBean(beanName))
                .switchIfEmpty(generateMonoMock(beanName))
                .switchIfEmpty(generateMonoBean(beanName));
    }

    public Mono<S> generateMonoMock(String beanName) {
        mockProvider.initialize(mockGeneratorInfo);
        return Mono.just(mockProvider.getBean(beanName));
    }

    public Mono<S> generateMonoBean(String beanName) {
        return Mono.just(springBeanProvider.getBean(beanName));
    }

}
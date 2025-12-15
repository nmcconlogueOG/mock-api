package net.mcfarb.testing.ddmock.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import net.mcfarb.testing.TestParent;
import reactor.core.publisher.Mono;

@Component
public class SpringBeanMonoProvider<S> implements MonoBeanProvider<TestParent, S>, ApplicationContextAware {

    @Autowired
    private ApplicationContext context;

    public SpringBeanMonoProvider() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<S> getBean(String beanName) {
        return Mono.just((S) context.getBean(beanName));

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
        return context.getBeanProvider(requiredType);
    }

}

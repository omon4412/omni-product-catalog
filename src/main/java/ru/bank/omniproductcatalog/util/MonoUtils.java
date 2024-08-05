package ru.bank.omniproductcatalog.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.bank.omniproductcatalog.model.exception.ServiceTimeoutException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class MonoUtils {
    private static final Logger logger = LoggerFactory.getLogger(MonoUtils.class);
    private static final Scheduler schedulerCommon = Schedulers.newParallel("workers", 8);


    public <T> Mono<T> oksServiceCallableRight(Mono<T> source, Long timeout) {
        return source
                .publishOn(schedulerCommon)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(TimeoutException.class, e -> logger.error("Service call timeout after {} ms", timeout))
                .onErrorResume(TimeoutException.class, e -> Mono.error(new ServiceTimeoutException("Service call timeout")));
    }

    public <T> Flux<T> oksServiceCallableRight(Flux<T> source, Long timeout) {
        return source
                .publishOn(schedulerCommon)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(TimeoutException.class, e -> logger.error("Service call timeout after {} ms", timeout))
                .onErrorResume(TimeoutException.class, e -> Flux.error(new ServiceTimeoutException("Service call timeout")));
    }
}

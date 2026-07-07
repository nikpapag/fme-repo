package com.harness.workshop.config;

import io.split.client.SplitClient;
import io.split.client.SplitClientConfig;
import io.split.client.SplitFactory;
import io.split.client.SplitFactoryBuilder;
import io.split.client.SplitManager;
import io.split.client.impressions.ImpressionsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeoutException;

@Configuration
public class SplitConfig {

    private static final Logger log = LoggerFactory.getLogger(SplitConfig.class);

    @Value("${split.sdkKey}")
    private String sdkKey;

    @Value("${split.blockUntilReady:true}")
    private boolean blockUntilReady;

    @Value("${split.blockUntilReadyTimeoutMs:10000}")
    private int readyTimeoutMs;

    @Value("${split.eventFlushIntervalMs:10000}")
    private int eventFlushIntervalMs;

    @Value("${split.eventsQueueSize:500}")
    private int eventsQueueSize;

    /** How often impressions are posted to Harness (seconds). DEBUG mode allows &lt; 60. */
    @Value("${split.impressionsRefreshRate:5}")
    private int impressionsRefreshRate;

    @Value("${split.impressionsMode:DEBUG}")
    private String impressionsMode;

    @Bean
    public SplitFactory splitFactory() throws Exception {
        if (sdkKey == null || sdkKey.isBlank() || "localhost".equals(sdkKey)) {
            log.warn("SPLIT_SDK_KEY is unset or 'localhost' — getTreatment will NOT send impressions to Harness FME cloud");
        }

        ImpressionsManager.Mode mode = parseImpressionsMode(impressionsMode);

        SplitClientConfig cfg = SplitClientConfig.builder()
                .setBlockUntilReadyTimeout(readyTimeoutMs)
                .streamingEnabled(false)
                .featuresRefreshRate(20)
                .impressionsMode(mode)
                .impressionsRefreshRate(impressionsRefreshRate)
                .impressionsQueueSize(5000)
                .eventsQueueSize(eventsQueueSize)
                .eventFlushIntervalInMillis(eventFlushIntervalMs)
                .build();

        log.info(
                "Split SDK config: impressionsMode={}, impressionsRefreshRate={}s, eventFlush={}ms",
                mode, impressionsRefreshRate, eventFlushIntervalMs);

        return SplitFactoryBuilder.build(sdkKey, cfg);
    }

    @Bean
    public SplitClient splitClient(SplitFactory factory) throws Exception {
        SplitClient client = factory.client();
        if (blockUntilReady) {
            try {
                client.blockUntilReady();
                log.info("Split SDK ready — getTreatment calls will register impressions in Harness FME");
            } catch (TimeoutException | InterruptedException e) {
                log.error(
                        "Split SDK NOT ready within {}ms — getTreatment returns 'control' and impressions may be missing",
                        readyTimeoutMs);
            }
        }
        return client;
    }

    @Bean
    public SplitManager splitManager(SplitFactory factory) {
        return factory.manager();
    }

    private static ImpressionsManager.Mode parseImpressionsMode(String value) {
        if (value == null) {
            return ImpressionsManager.Mode.DEBUG;
        }
        return switch (value.toUpperCase()) {
            case "OPTIMIZED" -> ImpressionsManager.Mode.OPTIMIZED;
            case "NONE" -> ImpressionsManager.Mode.NONE;
            default -> ImpressionsManager.Mode.DEBUG;
        };
    }
}

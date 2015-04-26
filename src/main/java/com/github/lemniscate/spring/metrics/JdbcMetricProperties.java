package com.github.lemniscate.spring.metrics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by dave on 4/24/15.
 */
@ConfigurationProperties(prefix = "lem.metrics.jdbc")
public class JdbcMetricProperties {

    @Getter @Setter
    private String findOne, findAll, count, increment, set, reset, insert,

        // keys used in our queries
        metricKey, valueKey,

        // database column names for extracting metrics
        metricNameCol, valueCol;

}

package com.github.lemniscate.spring.metrics;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 4/24/15.
 */
@Slf4j
@Transactional
public class JdbcMetricRepository implements MetricRepository {

    private MetricRowMapper mapper = new MetricRowMapper();

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private JdbcMetricProperties props;

    public Metric<?> findOne(final String metricName) {
        Map<String, Object> params = new HashMap<String, Object>(){{
            put(props.getMetricKey(), metricName);
        }};

        List<Metric<?>> results = jdbc.query(props.getFindOne(), params, mapper);
        return results.isEmpty() ? null : results.get(0);
    }

    public Iterable<Metric<?>> findAll() {
        return jdbc.query(props.getFindAll(), mapper);
    }

    public long count() {
        return jdbc.queryForObject(props.getCount(), new HashMap<String, Object>(), Long.class);
    }

    @Async
    public void increment(final Delta<?> value) {
        synchronized (getClass()) {
            Metric<?> existing = findOne(value.getName());
            if (existing == null) {
                insert(value.getName(), value.getValue());
            } else {
                final double d = value.getValue().doubleValue();
                int updated = jdbc.update(props.getIncrement(), new HashMap<String, Object>() {{
                    put(props.getValueKey(), d);
                    put(props.getMetricKey(), value.getName());
                }});
                log.debug("Updated " + updated + " records by " + d);
            }
        }
    }

    @Async
    public void set(final Metric<?> value) {
        synchronized (getClass()) {
            Metric<?> existing = findOne(value.getName());
            if (existing == null) {
                insert(value.getName(), value.getValue());
            } else {
                final double d = value.getValue().doubleValue();
                int updated = jdbc.update(props.getSet(), new HashMap<String, Object>() {{
                    put(props.getValueKey(), d);
                    put(props.getMetricKey(), value.getName());
                }});
                log.debug("Set " + updated + " records to " + d);
            }
        }
    }

    @Async
    public void reset(final String metricName) {
        int updated = jdbc.update(props.getReset(), new HashMap<String, Object> (){{
            put(props.getMetricKey(), metricName);
        }});
        log.debug("Reset " + updated + " records");
    }



    public class MetricRowMapper implements RowMapper<Metric<?>> {

        public Metric<BigDecimal> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            String name = rs.getString(props.getMetricNameCol());
            BigDecimal value = rs.getBigDecimal(props.getValueCol());

            Metric<BigDecimal> metric = new Metric<BigDecimal>(name, value);
            return metric;
        }
    }

    private void insert(final String name, final Number value) {
        try {
            int updated = jdbc.update(props.getInsert(), new HashMap<String, Object>() {{
                put(props.getValueKey(), value.doubleValue());
                put(props.getMetricKey(), name);
            }});
            log.debug("Inserted " + updated + " for " + name + " (" + value + ")");
        }catch(Exception e){
            log.warn("Failed inserting metric; name={}, number={}", name, value.doubleValue(), e);
        }
    }
}

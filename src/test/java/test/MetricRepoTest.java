package test;

import com.github.lemniscate.spring.metrics.JdbcMetricProperties;
import com.github.lemniscate.spring.metrics.JdbcMetricRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by dave on 4/25/15.
 */
@Slf4j
@EnableAutoConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties(JdbcMetricProperties.class)
@SpringApplicationConfiguration(classes = MetricRepoTest.DefaultConfig.class)
public class MetricRepoTest {

    @Autowired
    private JdbcMetricProperties props;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private MetricRepository repo;

    @BeforeClass
    public static void beforeClass() throws Exception{
        InputStream is = new DefaultResourceLoader().getResource("classpath:application.properties").getInputStream();
        StreamUtils.copy(is, System.out);
        System.out.println();
    }

    @Test
    public void foo() throws Exception{
        assertEquals(props.getValueCol(), "value");

        Metric<?> foo = repo.findOne("foo");
        assertEquals(12.5, foo.getValue().doubleValue(), 0);
    }

    @Test
    public void incrementNonExistingTest(){
        Metric<?> existing = repo.findOne("blah");
        assertNull("Could not verify metric didn't exist", existing);

        repo.increment(new Delta<Number>("blah", 2.1));
        Metric<BigDecimal> created = (Metric<BigDecimal>) repo.findOne("blah");
        assertEquals(2.1, created.getValue().doubleValue(), 0);
    }

    @EnableTransactionManagement
    public static class DefaultConfig {

        @Bean
        public JdbcMetricProperties props(){
            return new JdbcMetricProperties();
        }

        @Bean
        public MetricRepository metricRepository(){
            return new JdbcMetricRepository();
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScript("classpath:test-data.sql")
                    .build();
        }

        @Bean
        public ConfigurationPropertiesBindingPostProcessor propsPostProcessor(){
            return new ConfigurationPropertiesBindingPostProcessor();
        }

        @Bean
        public NamedParameterJdbcTemplate jdbcTemplate(){
            return new NamedParameterJdbcTemplate(dataSource());
        }

        @Bean
        public PlatformTransactionManager transactionManager(){
            DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource());
            return transactionManager;
        }

        @Bean
        public ConversionService conversionService(){
            return new DefaultConversionService();
        }
    }
}

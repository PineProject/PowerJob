package tech.powerjob.samples.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import tech.powerjob.common.utils.CommonUtils;
import tech.powerjob.official.processors.impl.sql.SpringDatasourceSqlProcessor;
import tech.powerjob.worker.PowerJobWorker;

import javax.sql.DataSource;

/**
 * @author Echo009
 * @since 2021/3/10
 */
@Configuration
@ConditionalOnBean(PowerJobWorker.class)
public class SqlProcessorConfiguration {


    @Bean
    @DependsOn({"initPowerJob"})
    public DataSource sqlProcessorDataSource() {
        String path = System.getProperty("user.home") + "/test/h2/" + CommonUtils.genUUID() + "/";
        String jdbcUrl = String.format("jdbc:h2:file:%spowerjob_sql_processor_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", path);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(Driver.class.getName());
        config.setJdbcUrl(jdbcUrl);
        config.setAutoCommit(true);
        // The minimum number of idle connections in the pool
        config.setMinimumIdle(1);
        // Maximum number of connections in the pool
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }


    @Bean
    public SpringDatasourceSqlProcessor simpleSpringSqlProcessor(@Qualifier("sqlProcessorDataSource") DataSource dataSource) {
        SpringDatasourceSqlProcessor springDatasourceSqlProcessor = new SpringDatasourceSqlProcessor(dataSource);
        // do nothing
        springDatasourceSqlProcessor.registerSqlValidator("fakeSqlValidator", sql -> true);
        // Exclude SQL containing drop
        springDatasourceSqlProcessor.registerSqlValidator("interceptDropValidator", sql -> sql.matches("^(?i)((?!drop).)*$"));
        // do nothing
        springDatasourceSqlProcessor.setSqlParser((sql, taskContext) -> sql);
        return springDatasourceSqlProcessor;
    }

}

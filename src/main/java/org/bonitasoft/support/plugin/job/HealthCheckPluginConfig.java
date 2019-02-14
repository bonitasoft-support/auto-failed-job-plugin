package org.bonitasoft.support.plugin.job;

import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.impl.APIAccessorExt;
import com.bonitasoft.engine.plugin.EnginePlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.bonitasoft.support.plugin.job")
public class HealthCheckPluginConfig implements EnginePlugin {

    @Bean
    public Integer maxResults(@Value("${org.bonitasoft.support.plugin.job.MAX_RESULTS:1000}") int maxResults) {
        return maxResults;
    }

}

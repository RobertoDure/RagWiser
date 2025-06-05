package ie.com.rag.config;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OllamaConfig {

    @Value("${spring.ai.ollama.base-url:}")
    private String baseUrl;

    @Value("${spring.ai.ollama.model:}")
    private String model;

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi(baseUrl);
    }

    @Bean
    @Primary
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {

        OllamaOptions options = new OllamaOptions();
        options.setModel(model);
        return new OllamaChatModel(ollamaApi, options);
    }
}

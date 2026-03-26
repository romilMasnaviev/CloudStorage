package ru.masnaviev.cloudstorage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
public class MockMvcHelperConfig {
    @Bean
    public MockMvcTestHelper mockMvcTestHelper(MockMvc mockMvc) {
        return new MockMvcTestHelper(mockMvc);
    }
}

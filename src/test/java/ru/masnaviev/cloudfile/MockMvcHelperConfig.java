package ru.masnaviev.cloudfile;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import ru.masnaviev.cloudfile.helpers.MockMvcTestHelper;

@TestConfiguration
public class MockMvcHelperConfig {
    @Bean
    public MockMvcTestHelper mockMvcTestHelper(MockMvc mockMvc) {
        return new MockMvcTestHelper(mockMvc);
    }
}

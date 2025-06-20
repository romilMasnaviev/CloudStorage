package ru.masnaviev.cloudfile.user;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
public class MockMvcHelperConfig {
    @Bean
    public MockMvcTestHelper mockMvcTestHelper(MockMvc mockMvc) {
        return new MockMvcTestHelper(mockMvc);
    }

//    @Bean
//    @Primary
//    public TestRestTemplate testRestTemplate(ApplicationContext applicationContext) {
//        TestRestTemplate testRestTemplate =
//                new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
//
//        LocalHostUriTemplateHandler handler =
//                new LocalHostUriTemplateHandler(applicationContext.getEnvironment(), "http");
//        testRestTemplate.setUriTemplateHandler(handler);
//
//        return testRestTemplate;
//    }
}

package com.abc.bank.onboarding;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = {
        "spring.mail.username=test@example.com",
        "spring.mail.password=dummyPassword",
        "spring.mail.host=localhost",
        "spring.mail.port=1025"
})
class CustomerOnboardingApplicationTests {


    @Test
    void contextLoads() {
    }


    @Test
    void should_run_main_method_without_exceptions() {
        CustomerOnboardingApplication.main(new String[]{});
    }
}

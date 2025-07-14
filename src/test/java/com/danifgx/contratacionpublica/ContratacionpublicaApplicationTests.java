package com.danifgx.contratacionpublica;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.springframework=DEBUG"
})
class ContratacionpublicaApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Running contextLoads test");
        try {
            // Just a placeholder to ensure the test runs
            System.out.println("[DEBUG_LOG] Test completed successfully");
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

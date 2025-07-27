package org.moyi_tech.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementBackendApplication.class, args);
    }
}

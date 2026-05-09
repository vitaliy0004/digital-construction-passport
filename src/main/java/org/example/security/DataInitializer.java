package org.example.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new AppUser("editor", passwordEncoder.encode("editor123"), Role.EDITOR));
                userRepository.save(new AppUser("reader", passwordEncoder.encode("reader123"), Role.READER));
            }
        };
    }
}

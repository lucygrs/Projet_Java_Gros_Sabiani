package fr.efrei.idflm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class HorlogeConfiguration {

    // Injectée partout à la place de LocalDateTime.now() pour pouvoir figer l'heure dans les tests
    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}

package fr.efrei.idflm.config;

import fr.efrei.idflm.modele.Profil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecuriteConfiguration {

    private static final String RESPONSABLE = Profil.RESPONSABLE_EXPLOITATION.name();

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder();
    }

    // L'API est authentifiée à chaque requête par HTTP Basic, sans cookie de session : la protection CSRF est inutile
    @Bean
    @Order(1)
    public SecurityFilterChain chaineDeSecuriteApi(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers(HttpMethod.GET, "/api/incidents/resolus").hasRole(RESPONSABLE)
                        .requestMatchers(HttpMethod.PUT, "/api/incidents/*/prestataire").hasRole(RESPONSABLE)
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    // Les droits du tableau §5.6 sont appliqués ici côté serveur, masquer les boutons ne suffit pas
    @Bean
    @Order(2)
    public SecurityFilterChain chaineDeSecurite(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers("/connexion", "/js/**", "/images/**", "/error",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/equipements", "/equipements/*/fiche",
                                "/lignes", "/lignes/*/stations").authenticated()
                        .requestMatchers("/tableau-de-bord", "/incidents/passes", "/incidents/*/modification",
                                "/incidents/*/affectation", "/equipements/**", "/lignes/**",
                                "/utilisateurs/**", "/prestataires/**").hasRole(RESPONSABLE)
                        .requestMatchers(HttpMethod.PUT, "/incidents/*").hasRole(RESPONSABLE)
                        .requestMatchers(HttpMethod.DELETE, "/incidents/*").hasRole(RESPONSABLE)
                        .anyRequest().authenticated())
                .formLogin(connexion -> connexion
                        .loginPage("/connexion")
                        .usernameParameter("identifiant")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/connexion?erreur")
                        .permitAll())
                .logout(deconnexion -> deconnexion
                        .logoutUrl("/deconnexion")
                        .logoutSuccessUrl("/connexion?deconnecte"))
                .exceptionHandling(erreurs -> erreurs.accessDeniedPage("/acces-refuse"));
        return http.build();
    }
}

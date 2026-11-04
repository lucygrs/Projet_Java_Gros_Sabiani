package fr.efrei.idflm.config;

import fr.efrei.idflm.modele.Profil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    // Les droits du tableau §5.6 sont appliqués ici côté serveur, masquer les boutons ne suffit pas
    @Bean
    public SecurityFilterChain chaineDeSecurite(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers("/connexion", "/css/**", "/js/**", "/images/**", "/error").permitAll()
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

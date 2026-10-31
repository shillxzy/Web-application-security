package chnu.edu.booksecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF is disabled: stateless REST API with Basic auth, so POST/PUT/DELETE are allowed
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        // hello routes: one per role, one shared, one open to any authenticated user
                        .requestMatchers("/api/v1/hello/admin").hasRole("ADMIN")
                        .requestMatchers("/api/v1/hello/user").hasRole("USER")
                        .requestMatchers("/api/v1/hello/man").hasAnyRole("USER", "ADMIN")
                        // books: USER reads, ADMIN also creates and updates, SUPERADMIN also deletes
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole("SUPERADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // {noop} = password stored as plain text, no encoder (for learning purposes only)
        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password("{noop}user")
                .roles("USER")
                .build();

        UserDetails superadmin = User.builder()
                .username("superadmin")
                .password("{noop}superadmin")
                .roles("SUPERADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin, user, superadmin);
    }
}

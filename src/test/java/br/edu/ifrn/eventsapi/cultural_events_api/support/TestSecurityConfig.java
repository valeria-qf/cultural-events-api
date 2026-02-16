@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}

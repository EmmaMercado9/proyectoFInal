package com.ema.practica.infrastructure.util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private static final String ADMIN = "ADMIN";
    private static final String COLABORADOR = "COLABORADOR";

    private final UserDetailsService userDetailServiceImp;
    private final JwtRequestFilter jwtRequestFilter;

    // Constructor para inyectar UserDetailsService
    public SecurityConfig(UserDetailsService userDetailServiceImp,JwtRequestFilter jwtRequestFilter) {
        this.userDetailServiceImp = userDetailServiceImp;
        this.jwtRequestFilter = jwtRequestFilter;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(http -> http
                        .requestMatchers("/login","/loggin","/api/auth/validate").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()

                        // ── Usuarios: solo ADMIN (cualquiera puede ver su propio perfil)
                        .requestMatchers(HttpMethod.GET, "/getUser/current-user").authenticated()
                        .requestMatchers("/usersAdd/**", "/deleteUser/**", "/getUser/**", "/users/**").hasRole(ADMIN)

                        // ── Ventas: el colaborador vende y consulta; cancelar es solo ADMIN
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/orders/*/cancelar").hasRole(ADMIN)
                        .requestMatchers("/api/admin/orders/**").hasAnyRole(ADMIN, COLABORADOR)
                        // ── Devoluciones y cambios: ambos roles
                        .requestMatchers("/api/admin/devoluciones/**").hasAnyRole(ADMIN, COLABORADOR)
                        // ── Facturación ARCA: ambos facturan; probar conexión solo ADMIN
                        .requestMatchers("/api/admin/facturacion/probar", "/api/admin/facturacion/config",
                                "/api/admin/facturacion/csr", "/api/admin/facturacion/certificado").hasRole(ADMIN)
                        .requestMatchers("/api/admin/facturacion/**", "/api/admin/facturas/**").hasAnyRole(ADMIN, COLABORADOR)
                        .requestMatchers("/api/orders/**").hasAnyRole(ADMIN, COLABORADOR)

                        // ── Clientes: el colaborador puede consultar y dar de alta
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAnyRole(ADMIN, COLABORADOR)
                        .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole(ADMIN, COLABORADOR)

                        // ── Catálogo: lectura para ambos, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**",
                                "/api/atributos/**", "/marca/**").hasAnyRole(ADMIN, COLABORADOR)
                        .requestMatchers("/api/productos/**", "/api/categorias/**",
                                "/api/atributos/**", "/marca/**", "/api/clientes/**").hasRole(ADMIN)

                        // Tienda online (rol CLIENTE aún no existe): la controla @PreAuthorize
                        .anyRequest().authenticated()

                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
    }
    // Orígenes permitidos: app.cors.allowed-origins (separados por coma)
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailServiceImp);
        return provider;
    }


}

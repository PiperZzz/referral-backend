package org.moyi_tech.usermanagement.config;

import org.moyi_tech.usermanagement.security.filter.JwtAuthTokenFilter;
import org.moyi_tech.usermanagement.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthTokenFilter jwtAuthTokenFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                         JwtAuthenticationEntryPoint unauthorizedHandler,
                         JwtAuthTokenFilter jwtAuthTokenFilter,
                         CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsServiceImpl = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder() {
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                System.out.println("=== 密码比对 ===");
                System.out.println("明文密码: " + rawPassword);
                System.out.println("数据库Hash: " + encodedPassword);
                boolean result = super.matches(rawPassword, encodedPassword);
                System.out.println("比对结果: " + result);
                System.out.println("================");
                return result;
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceImpl);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            // 禁用CSRF（对于API不需要）
            .csrf(csrf -> csrf.disable())
            // 异常处理
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            // 无状态会话管理
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 请求授权配置
            .authorizeHttpRequests(authz -> authz
                // 公开端点 - 不需要认证
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error").permitAll()
                
                // OPTIONS请求允许通过（用于CORS预检请求）
                .requestMatchers("OPTIONS", "/**").permitAll()
                
                // Help 页面端点 - 需要认证
                .requestMatchers("/api/help/**").authenticated()
                
                // 管理员端点
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 用户候选人管理端点 - 需要认证
                .requestMatchers("/api/candidates/**").authenticated()
                
                // 用户相关端点 - 需要认证
                .requestMatchers("/api/users/**").authenticated()

                .requestMatchers("/api/master/**").hasRole("MASTER")
                .requestMatchers("/api/master/reset-password").permitAll() // Master重置密码不需要认证
                
                // 会话相关端点 - 需要认证
                .requestMatchers("/api/session/**").authenticated()
                
                // 其他端点需要认证
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
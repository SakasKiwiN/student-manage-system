package com.example.studentcoursesystem.config;

import com.example.studentcoursesystem.service.CustomUserDetailsService;
import com.example.studentcoursesystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Spring Security 配置类
 * 适配学生选课系统的业务需求
 * 支持Session认证和方法级权限控制
 * 完全兼容Spring Security 6.1.5
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用 @PreAuthorize 注解
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（API开发阶段）
                .csrf(AbstractHttpConfigurer::disable)

                // 配置Headers（Spring Security 6.1.5兼容版本）
                .headers(headers -> headers
                        // 内容类型选项
                        .contentTypeOptions(Customizer.withDefaults())

                        // HSTS配置
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                        )

                        // 使用默认的referrerPolicy配置（避免API兼容性问题）
                        .referrerPolicy(Customizer.withDefaults())

                        // X-Frame-Options配置
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                // 配置URL访问权限
                .authorizeHttpRequests(auth -> auth
                        // === 公开访问的端点 ===
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/error").permitAll()

                        // === 开发环境数据库控制台 ===
                        .requestMatchers("/h2-console/**").permitAll()

                        // === 静态资源和文件下载 ===
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/uploads/**", "/files/**").permitAll() // 课件等文件访问

                        // === 系统管理员专用功能 ===
                        .requestMatchers("/api/admin/**").hasRole("SYS_ADMIN")
                        .requestMatchers("/api/statistics/export/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN")

                        // === 学院管理功能 ===
                        .requestMatchers("/api/colleges/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN")

                        // === 教师管理功能 ===
                        .requestMatchers("/api/teachers/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN")

                        // === 学生管理功能 ===
                        .requestMatchers("/api/students/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN", "TEACHER", "STUDENT")

                        // === 课程相关功能 ===
                        .requestMatchers("/api/courses/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/course-selections/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN", "TEACHER", "STUDENT")

                        // === 成绩管理功能 ===
                        .requestMatchers("/api/scores/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN", "TEACHER", "STUDENT")

                        // === 课件管理功能 ===
                        .requestMatchers("/api/coursewares/**").hasAnyRole("SYS_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/upload/**").hasAnyRole("SYS_ADMIN", "TEACHER") // 文件上传
                        .requestMatchers("/api/download/**").hasAnyRole("SYS_ADMIN", "TEACHER", "STUDENT") // 文件下载

                        // === 消息功能 ===
                        .requestMatchers("/api/messages/**").hasAnyRole("SYS_ADMIN", "TEACHER", "STUDENT")

                        // === 统计功能 ===
                        .requestMatchers("/api/statistics/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN", "TEACHER")

                        // === 抽签功能 ===
                        .requestMatchers("/api/lottery/**").hasAnyRole("SYS_ADMIN", "COLLEGE_ADMIN")

                        // 其他所有API请求都需要认证
                        .requestMatchers("/api/**").authenticated()

                        // 其他所有请求允许访问
                        .anyRequest().permitAll()
                )

                // 配置表单登录
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login") // 登录处理URL
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            PrintWriter writer = response.getWriter();

                            // 获取用户角色信息
                            String roles = authentication.getAuthorities().toString();

                            writer.write(String.format(
                                    "{\"code\":200,\"message\":\"登录成功\",\"data\":{" +
                                            "\"username\":\"%s\"," +
                                            "\"roles\":\"%s\"," +
                                            "\"loginTime\":\"%s\"" +
                                            "}}",
                                    authentication.getName(),
                                    roles,
                                    System.currentTimeMillis()
                            ));
                            writer.flush();
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            PrintWriter writer = response.getWriter();

                            String errorMessage = "用户名或密码错误";
                            if (exception.getMessage().contains("Bad credentials")) {
                                errorMessage = "用户名或密码错误";
                            } else if (exception.getMessage().contains("disabled")) {
                                errorMessage = "账号已被禁用";
                            } else if (exception.getMessage().contains("locked")) {
                                errorMessage = "账号已被锁定";
                            }

                            writer.write(String.format(
                                    "{\"code\":401,\"message\":\"%s\",\"data\":null}",
                                    errorMessage
                            ));
                            writer.flush();
                        })
                        .permitAll()
                )

                // 配置登出
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            PrintWriter writer = response.getWriter();
                            writer.write("{\"code\":200,\"message\":\"退出成功\",\"data\":null}");
                            writer.flush();
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID") // 删除Session Cookie
                        .permitAll()
                )

                // 配置Session管理
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .maximumSessions(1) // 同一用户最多1个Session
                                .maxSessionsPreventsLogin(false) // 新登录踢掉旧Session
                                .expiredUrl("/api/auth/login") // Session过期重定向
                )

                // 设置认证提供者
                .authenticationProvider(authenticationProvider())

                // 处理认证和授权异常
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            PrintWriter writer = response.getWriter();

                            String requestURI = request.getRequestURI();
                            writer.write(String.format(
                                    "{\"code\":401,\"message\":\"请先登录\",\"data\":{\"requestURI\":\"%s\"}}",
                                    requestURI
                            ));
                            writer.flush();
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            PrintWriter writer = response.getWriter();

                            String requestURI = request.getRequestURI();
                            String userRole = SecurityUtils.getCurrentUserRole();

                            writer.write(String.format(
                                    "{\"code\":403,\"message\":\"权限不足\",\"data\":{" +
                                            "\"requestURI\":\"%s\"," +
                                            "\"currentRole\":\"%s\"," +
                                            "\"requiredRole\":\"更高权限\"" +
                                            "}}",
                                    requestURI,
                                    userRole
                            ));
                            writer.flush();
                        })
                );

        return http.build();
    }
}
package com.example.SpringJWT.config;

import com.example.SpringJWT.jwt.JWTFilter;
import com.example.SpringJWT.jwt.JWTUtil;
import com.example.SpringJWT.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // LoginFilter()에 AuthenticationManager 인자를 넣음. -> AuthenticationManager도 인자가 있음.
    // 따라서 객체변수 선언후, 생성자 방식으로 SecurityConfig에서 주입받음.
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    //addFilterAt(new LoginFilter(), UsernamePasswordAuthenticationFilter.class);에서 LoginFilter()에 인자 필요.
    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration Configuration) throws Exception {

        return Configuration.getAuthenticationManager();
    }

    //암호화 진행하는데 사용하는 Method
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CORS 설정(Security 측 CORS 문제 해결)
        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                                CorsConfiguration configuration = new CorsConfiguration();

                                //프론트엔드단과 연결하기 위한 3000번 포트 연결
                                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                                configuration.setAllowedMethods(Collections.singletonList("*"));
                                configuration.setAllowCredentials(true);
                                configuration.setAllowedHeaders(Collections.singletonList("*"));
                                configuration.setMaxAge(3600L); //허용을 지속할 시간

                                configuration.setExposedHeaders(Collections.singletonList("Authorization")); //

                                return configuration;
                            }
                        }));


        // csrf(Cross-Site Request Forgery ; 교차 사이트 요청 위조) disable -> csrf protection(보안 수준을 향상시킴)을 적용하지 않음.
        // WHY? -> Non-browser clients 만을 위한 서비스라면 csrf 코드가 필요없음.(rest api에서는 csrf 공격으로부터 안전하여 해당기능 disable()함.)
        http
                .csrf((auth) -> auth.disable());

        //Form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login","/","/join").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated());
        http
                //특정한 필터 앞에 추가(addFilterBefor 이용)
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        http
                //addFilterAt -> 원하는 자리에 필터를 등록. |  구현한 LoginFilter()를 UsernamePasswordAuthenticationFilter에 등록
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration),jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // JWT 방식에서는 항상 Session을 STATELESS(클라이언트와 서버간의 통신을 상태유지 하지 않음.) 형태로 관리함.
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        return http.build();
    }
}

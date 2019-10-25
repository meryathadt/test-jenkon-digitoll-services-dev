package com.digitoll.erp.configuration;

import com.digitoll.commons.configuration.CustomAuthenticationEntryPoint;
import com.digitoll.commons.configuration.filter.JwtAuthorizationFilter;
import com.digitoll.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${security.jwt.header:Authorization}")
    private String TOKEN_HEADER;

    @Value("${security.jwt.prefix:Bearer }")
    private String TOKEN_PREFIX;

    @Value("${security.jwt.secret}")
    private String TOKEN_SECRET;

    @Autowired
    private UserService UserService;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(UserService);
    }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http
            .cors().and()
            .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint)
            .and()
                .authorizeRequests()
                .antMatchers(
                        "/v2/api-docs",
                        "/swagger-resources",
                        "/swagger-ui.html",
                        "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security",
                        "/configuration/ui",
                        "/configuration/security",
                        "/webjars/**",

                        "/actuator/health",
                        "/actuator/info",

                        "/user/authenticate"

                ).permitAll()
                .anyRequest().authenticated()
                .and()
                    .addFilter(new JwtAuthorizationFilter(authenticationManager(), TOKEN_HEADER, TOKEN_PREFIX, TOKEN_SECRET));

	}
}

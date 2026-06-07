package org.example.ecommerceapp.config;

import org.example.ecommerceapp.dto.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Autowired
  private  JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http.csrf(csrf->csrf.disable()).sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
               .authorizeHttpRequests(auth->auth.requestMatchers("/","/api/public/**","/api/products/**","/api/categories/**").permitAll()
               .requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers(
                               "/swagger-ui/**",
                               "/v3/api-docs/**",
                               "/swagger-ui.html"
                       ).permitAll().anyRequest().authenticated())
               .formLogin(form->form.disable());
       return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }
//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder encoder){
//        UserDetails user1= User.withUsername("Bhav").password(encoder.encode("123")).roles("USER").build();
//        UserDetails user2=User.withUsername("vendan").password(encoder.encode("321")).roles("USER").build();
//        return new InMemoryUserDetailsManager(user1,user2);
//    }
//    @Bean
//    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder){
//        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
//        provider.setPasswordEncoder(encoder);
//        return provider;
//    }
}
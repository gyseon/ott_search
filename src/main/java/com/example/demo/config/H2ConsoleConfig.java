package com.example.demo.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2servletRegistration() {
        // H2 콘솔용 Jakarta 서블릿 직접 등록
        ServletRegistrationBean<JakartaWebServlet> registrationBean =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
        return registrationBean;
    }
}
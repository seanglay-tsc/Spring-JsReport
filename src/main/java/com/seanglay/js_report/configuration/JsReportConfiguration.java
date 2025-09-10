package com.seanglay.js_report.configuration;

import net.jsreport.java.service.JsReportService;
import net.jsreport.java.service.JsReportServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsReportConfiguration {
    @Value("${jsreport.url}")
    private String jsReportUrl;

    @Value("${jsreport.username}")
    private String username;

    @Value("${jsreport.password}")
    private String password;

    @Bean
    public JsReportService jsReportService() {
        return new JsReportServiceImpl(jsReportUrl, username, password);
    }
}
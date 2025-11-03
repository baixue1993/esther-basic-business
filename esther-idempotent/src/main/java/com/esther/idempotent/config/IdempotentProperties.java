package com.esther.idempotent.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "esther.idempotent")
public class IdempotentProperties {

    //参数如何像springboot那样的配置

    private String tokenPrefix = "idempotent:token:";

    private String processedPrefix = "idempotent:processed:";

}
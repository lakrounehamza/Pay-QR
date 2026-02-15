package com.lakroune.backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.public-key}")
    private String publicKey;

    @Value("${stripe.currency}")
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}

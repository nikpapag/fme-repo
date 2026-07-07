package com.harness.workshop.config;

import com.harness.workshop.model.*;
import com.harness.workshop.service.FlagService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;



@Configuration
public class PreloadData {
    @Bean
    CommandLineRunner seed(FlagService svc) {
        return args -> {
            svc.putUser(new User("u001","Alice","alice@example.com","free","UK", Map.of("beta","true")));
            svc.putUser(new User("u002","Bob","bob@example.com","pro","US", Map.of("beta","false")));
            svc.putUser(new User("u003","Cara","cara@example.com","enterprise","IN", Map.of("region","apac")));
            svc.putUser(new User("u004","Diego","diego@example.com","pro","BR", Map.of()));
            svc.putUser(new User("u005","Ella","ella@example.com","free","UK", Map.of("region","emea")));
            svc.putUser(new User("u006","Farah","farah@example.com","pro","AE", Map.of("beta","true")));
            svc.putUser(new User("u007","Gita","gita@example.com","free","IN", Map.of()));
            svc.putUser(new User("u008","Hiro","hiro@example.com","enterprise","JP", Map.of()));
            svc.putUser(new User("u009","Ivan","ivan@example.com","pro","DE", Map.of("region","emea")));
            svc.putUser(new User("u010","Jade","jade@example.com","free","US", Map.of("beta","true")));
        };
    }
}

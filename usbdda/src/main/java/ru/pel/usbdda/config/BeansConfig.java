package ru.pel.usbdda.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class BeansConfig {
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}

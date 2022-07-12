package ru.pel.usbdda.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@RequestMapping("/")
public class GreetingController {
    @GetMapping("/")
    public String showGreetingPage(){
        return "welcome-page";
    }

    @GetMapping("/login")
    public String showLoginPage(){
        return "login";
    }
}

package ru.pel.usbdda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
//@RequestMapping("/welcome")
public class WelcomeController {
    @GetMapping("/welcome")
    public String welcomePage() {
        return "/index";
    }
}

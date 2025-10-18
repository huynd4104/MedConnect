package com.medconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageController {
    @GetMapping({"/", "/index"})
    public String showHomepage() {
        return "index";
    }
}
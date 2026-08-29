package com.salestrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/leads")
    public String leads() {
        return "leads";
    }

    @GetMapping("/deals")
    public String deals() {
        return "deals";
    }

    @GetMapping("/companies")
    public String companies() {
        return "companies";
    }

    @GetMapping("/contacts")
    public String contacts() {
        return "contacts";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping("/activities")
    public String activities() {
        return "activities";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin-users";
    }
    @GetMapping("/register") 
    public String register() { 
        return "register"; 
    }
    @GetMapping("/deals/{id}")
    public String dealDetail(@PathVariable Long id) {
        return "deal-detail";
    }
}

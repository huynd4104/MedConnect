package com.medconnect.controller;

import com.medconnect.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchDoctorsController {
    private final SearchService searchService;

    @GetMapping("/search-doctors")
    public String searchDoctors(@RequestParam(required = false) String name, @RequestParam(required = false) Integer specializationId, @RequestParam(required = false) String location, Model model) {
        model.addAttribute("doctors", searchService.searchDoctors(name, specializationId, location));
        return "search-doctors";
    }
}
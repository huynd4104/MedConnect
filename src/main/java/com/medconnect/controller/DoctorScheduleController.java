package com.medconnect.controller;

import com.medconnect.dto.ScheduleDTO;
import com.medconnect.entity.User;
import com.medconnect.exception.TimeSlotOverlapException;
import com.medconnect.repository.UserRepository; // 1. IMPORT UserRepository
import com.medconnect.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // 2. IMPORT UserDetails
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DoctorScheduleController {
    private final ScheduleService scheduleService;
    private final UserRepository userRepository; // 3. INJECT UserRepository

    @GetMapping("/doctor-schedule")
    public String showScheduleForm(Model model) {
        if (!model.containsAttribute("scheduleDTO")) {
            model.addAttribute("scheduleDTO", new ScheduleDTO());
        }
        return "doctor-schedule";
    }

    @PostMapping("/doctor-schedule")
    public String addSchedule(@Valid @ModelAttribute("scheduleDTO") ScheduleDTO dto,
                              BindingResult bindingResult,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.scheduleDTO", bindingResult);
            redirectAttributes.addFlashAttribute("scheduleDTO", dto);
            return "redirect:/doctor-schedule";
        }

        try {
            // 4. SỬA LẠI CÁCH LẤY USER ID
            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in database for email: " + email));

            scheduleService.addSchedule(currentUser.getUserId(), dto);
            redirectAttributes.addFlashAttribute("success", "Thêm lịch làm việc thành công.");
            return "redirect:/doctor-schedule";
        } catch (TimeSlotOverlapException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("scheduleDTO", dto);
            return "redirect:/doctor-schedule";
        } catch (Exception e) {
            // Log the exception for debugging
            // e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("scheduleDTO", dto);
            return "redirect:/doctor-schedule";
        }
    }

    @DeleteMapping("/api/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable("id") Integer scheduleId, Authentication auth) {
        try {
            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            scheduleService.deleteSchedule(scheduleId, currentUser.getUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Log lỗi ở đây
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/api/schedules/{id}")
    public ResponseEntity<Void> updateSchedule(@PathVariable("id") Integer scheduleId,
                                               @Valid @RequestBody ScheduleDTO dto,
                                               Authentication auth) {
        try {
            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            scheduleService.updateSchedule(scheduleId, dto, currentUser.getUserId());
            return ResponseEntity.ok().build();
        } catch (TimeSlotOverlapException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            // Log lỗi ở đây
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/schedules")
    @ResponseBody
    public ResponseEntity<List<ScheduleDTO>> getDoctorSchedules(Authentication auth) {
        // 5. ÁP DỤNG SỬA LỖI TƯƠNG TỰ CHO API ENDPOINT
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database for email: " + email));

        List<ScheduleDTO> schedules = scheduleService.getSchedulesByUserId(currentUser.getUserId());
        return ResponseEntity.ok(schedules);
    }
}
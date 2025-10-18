package com.medconnect.dto;

import com.medconnect.entity.Schedule.ConsultationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDTO implements Serializable {
    private Integer scheduleId;

    @NotNull(message = "Ngày trong tuần không được để trống.")
    @Min(value = 1, message = "Ngày không hợp lệ (phải từ 1-Chủ Nhật đến 7-Thứ Bảy).")
    @Max(value = 7, message = "Ngày không hợp lệ (phải từ 1-Chủ Nhật đến 7-Thứ Bảy).")
    private Integer dayOfWeek;

    @NotBlank(message = "Giờ bắt đầu không được để trống.")
    private String startTime;

    @NotBlank(message = "Giờ kết thúc không được để trống.")
    private String endTime;

    @NotNull(message = "Loại tư vấn không được để trống.")
    private ConsultationType consultationType;
}
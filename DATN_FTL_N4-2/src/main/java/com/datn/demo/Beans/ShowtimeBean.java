package com.datn.demo.Beans;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.validation.BindingResult;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeBean {

    private Integer showtimeId;

    @NotNull(message = "Phòng chiếu không được để trống.")
    private Integer roomId;

    @NotNull(message = "Phim không được để trống.")
    private Integer movieId;

    @NotNull(message = "Rạp chiếu không được để trống.")
    private Integer cinemaId;

    @NotNull(message = "Ngày chiếu không được để trống.")
    @FutureOrPresent(message = "Ngày chiếu phải là ngày hiện tại hoặc tương lai.")
    private LocalDate showDate;

    @NotNull(message = "Thời gian bắt đầu không được để trống.")
    private LocalTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống.")
    private LocalTime endTime;

    @FutureOrPresent(message = "Ngày bắt đầu đặt vé phải là ngày hiện tại hoặc tương lai.")
    private LocalDate bookingStartDate;

    @AssertTrue(message = "Ngày bắt đầu đặt vé phải trước hoặc cùng ngày với ngày chiếu.")
    public boolean isBookingStartDateValid() {
        if (bookingStartDate == null) {
            return true; // Nếu không có ngày bắt đầu đặt vé, bỏ qua kiểm tra.
        }
        if (showDate == null) {
            return false; // Nếu ngày chiếu là null, không hợp lệ.
        }
        return !bookingStartDate.isAfter(showDate);
    }


    public void validateTimeRange(BindingResult error) {
        if (startTime == null || endTime == null) {
            // Nếu thời gian bắt đầu hoặc kết thúc không được nhập
            error.rejectValue("startTime", "error.startTime", "Cả thời gian bắt đầu và kết thúc đều phải được nhập.");
            error.rejectValue("endTime", "error.endTime", "Cả thời gian bắt đầu và kết thúc đều phải được nhập.");
        } else if (!endTime.isAfter(startTime)) {
            // Nếu thời gian kết thúc không lớn hơn thời gian bắt đầu
            error.rejectValue("endTime", "error.endTime", "Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
    }


}

package com.datn.demo.Controllers;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.demo.DTO.CinemaStatisticDTO;
import com.datn.demo.DTO.MovieStatisticDTO;
import com.datn.demo.Services.StatisticService;


@Controller
@RequestMapping("/charts")
public class ChartController {

    @Autowired
    private StatisticService statisticService;
    @GetMapping("/cinemas/export")
    public String exportCinemaRevenueToExcel(RedirectAttributes redirectAttributes) {
        String filePath = "D:\\DoanhThuRap.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet
            Sheet sheet = workbook.createSheet("Doanh Thu Rạp");

            // Tạo header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Tên Rạp");
            header.createCell(2).setCellValue("Số Vé Bán");
            header.createCell(3).setCellValue("Doanh Thu (VNĐ)");

            // Tạo kiểu cell cho tiền tệ (VNĐ)
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0\" VND\""));

            // Lấy danh sách rạp từ service
            List<CinemaStatisticDTO> cinemas = statisticService.getCinemaStatistics();
            int rowIdx = 1;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalTickets = 0;

            // Thêm dữ liệu vào Excel
            for (int i = 0; i < cinemas.size(); i++) {
                CinemaStatisticDTO cinema = cinemas.get(i);
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(cinema.getCinemaName());
                row.createCell(2).setCellValue(cinema.getTicketCount());

                // Doanh thu của từng rạp, áp dụng định dạng VND
                Cell revenueCell = row.createCell(3);
                revenueCell.setCellValue(cinema.getTotalRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);

                // Tính tổng doanh thu và tổng số vé
                totalTickets += cinema.getTicketCount();
                totalRevenue = totalRevenue.add(cinema.getTotalRevenue() != null ? cinema.getTotalRevenue() : BigDecimal.ZERO);
            }

            // Thêm dòng tổng cộng
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(2).setCellValue("Tổng Doanh Thu:");
            Cell totalRevenueCell = totalRow.createCell(3);
            totalRevenueCell.setCellValue(totalRevenue.doubleValue());
            totalRevenueCell.setCellStyle(currencyStyle);  // Áp dụng kiểu tiền tệ cho tổng doanh thu

            // Thêm tổng số vé
            Row ticketRow = sheet.createRow(rowIdx + 1);
            ticketRow.createCell(2).setCellValue("Tổng Số Vé Bán:");
            ticketRow.createCell(3).setCellValue(totalTickets);

            // Ghi file vào ổ D
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            // Kiểm tra xem file có tồn tại không
            File file = new File(filePath);
            if (file.exists()) {
                redirectAttributes.addFlashAttribute("successMessage", "File đã được lưu tại: " + filePath);
                redirectAttributes.addFlashAttribute("filePath", filePath); // Truyền đường dẫn file cho frontend
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "File không tồn tại sau khi xuất.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xuất file Excel: " + e.getMessage());
            return "redirect:/charts/cinemas"; // Quay lại trang rạp
        }

        return "redirect:/charts/cinemas"; // Quay lại trang rạp
    }


    @GetMapping("/movies/export")
    public String exportMoviesToExcel(RedirectAttributes redirectAttributes) {
        String filePath = "D:\\DoanhThuPhim.xlsx"; // Path to save the Excel file

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doanh Thu Phim");
            int rowCount = 0;

            // Create header row
            Row headerRow = sheet.createRow(rowCount++);
            String[] headers = {"STT", "Tên Phim", "Số Vé Bán", "Doanh Thu"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create cell style for currency (VND)
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0\" VND\"")); // Format VND with comma separation

            // Fetch movie statistics
            List<MovieStatisticDTO> allMovies = statisticService.getAllMoviesInInvoices();
            int index = 1;

            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalTickets = 0;

            // Populate data rows and calculate totals
            for (MovieStatisticDTO movie : allMovies) {
                Row row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(movie.getMovieName());
                row.createCell(2).setCellValue(movie.getTicketCount());

                // Set cell value for revenue with currency format
                Cell revenueCell = row.createCell(3);
                revenueCell.setCellValue(movie.getTotalRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle); // Apply currency style

                // Accumulate totals
                totalTickets += movie.getTicketCount();
                totalRevenue = totalRevenue.add(movie.getTotalRevenue() != null ? movie.getTotalRevenue() : BigDecimal.ZERO);
            }

            // Add totals row
            Row totalRow = sheet.createRow(rowCount++);
            totalRow.createCell(0).setCellValue("Tổng Cộng:");
            totalRow.createCell(2).setCellValue(totalTickets); // Total tickets
            Cell totalRevenueCell = totalRow.createCell(3);
            totalRevenueCell.setCellValue(totalRevenue.doubleValue()); // Total revenue
            totalRevenueCell.setCellStyle(currencyStyle); // Apply currency style for total revenue

            // Style totals row
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            for (int i = 0; i <= 3; i++) {
                Cell cell = totalRow.getCell(i);
                if (cell == null) {
                    cell = totalRow.createCell(i); // Create it if it doesn't exist
                }
                cell.setCellStyle(totalStyle);
            }

            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save Excel file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            // Add success message
            redirectAttributes.addFlashAttribute("statusMessage", "File đã được lưu tại: " + filePath);
            redirectAttributes.addFlashAttribute("filePath", filePath);

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("statusMessage", "Lỗi khi lưu file Excel!");
        }

        return "redirect:/charts/movies";
    }

    @GetMapping("/files/open")
    public ResponseEntity<Resource> openFile(@RequestParam("filePath") String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists() || !file.canRead()) {
                throw new FileNotFoundException("File không tồn tại hoặc không thể đọc: " + file.getAbsolutePath());
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // MIME mặc định
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/movies")
    public String showAllMoviesInInvoices(Model model) {
        // Lấy danh sách tất cả các phim trong hóa đơn
        List<MovieStatisticDTO> allMovies = statisticService.getAllMoviesInInvoices();

        // Tính tổng số vé bán và tổng doanh thu của tất cả các phim
        long totalTickets = allMovies.stream()
                .mapToLong(MovieStatisticDTO::getTicketCount)
                .sum();

        BigDecimal totalRevenue = allMovies.stream()
                .map(movie -> movie.getTotalRevenue() != null ? movie.getTotalRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Định dạng doanh thu
        String totalRevenueFormatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(totalRevenue);

        // Thêm dữ liệu vào model
        model.addAttribute("allMovies", allMovies);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("totalRevenueFormatted", totalRevenueFormatted);

        return "admin/charts/movieChart";
    }


    @GetMapping("/movies/filter")
    public String filterMoviesByDate(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            Model model) {

        // Chuyển đổi String thành LocalDate
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        // Lấy tất cả phim trong khoảng thời gian
        List<MovieStatisticDTO> filteredMovies = statisticService.getMoviesByDateRange(startDate, endDate);

        // Lấy top 3 phim bán chạy nhất
        List<MovieStatisticDTO> top3Movies = statisticService.getTop3MoviesByDateRange(startDate, endDate);

        // Tính tổng vé và doanh thu
        long totalTickets = filteredMovies.stream().mapToLong(MovieStatisticDTO::getTicketCount).sum();
        BigDecimal totalRevenue = filteredMovies.stream()
                .map(MovieStatisticDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Định dạng doanh thu
        String totalRevenueFormatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(totalRevenue);

        // Thêm dữ liệu vào model
        model.addAttribute("allMovies", filteredMovies); // Cập nhật danh sách phim sau khi lọc
        model.addAttribute("top3Movies", top3Movies);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("totalRevenueFormatted", totalRevenueFormatted);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        return "admin/charts/movieChart";
    }



    @GetMapping("/movieindex")
    public String showMovieChart3(Model model) {
        // Lấy danh sách phim hôm nay
        List<MovieStatisticDTO> todayMovies = statisticService.getMovieStatisticsToday();

        // Tính tổng doanh thu và tổng vé bán ra
        long totalTickets = todayMovies.stream().mapToLong(MovieStatisticDTO::getTicketCount).sum();
        BigDecimal totalRevenue = todayMovies.stream()
                .map(MovieStatisticDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng doanh thu theo tháng
        BigDecimal monthlyRevenue = statisticService.getTotalRevenueByMonth();

        // Định dạng tiền tệ
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedTotalRevenue = currencyFormatter.format(totalRevenue);
        String formattedMonthlyRevenue = currencyFormatter.format(monthlyRevenue);

        model.addAttribute("todayMovies", todayMovies);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("totalRevenueFormatted", formattedTotalRevenue);
        model.addAttribute("monthlyRevenueFormatted", formattedMonthlyRevenue);
        model.addAttribute("currentDate", LocalDate.now());

        return "admin/charts/indexChart";
    }
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount).replace("₫", "VNĐ"); // Thay thế ký hiệu đồng bằng "VNĐ"
    }

    @GetMapping("/cinemas")
    public String showCinemaStatistics(Model model) {
        // Tổng doanh thu và vé của tất cả các rạp
        BigDecimal totalRevenue = statisticService.getTotalRevenueForAllCinemas();
        Long totalTickets = statisticService.getTotalTicketsForAllCinemas();

        // Định dạng tiền tệ
        String formattedRevenue = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalRevenue);

        model.addAttribute("totalRevenueFormatted", formattedRevenue);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("cinemas", statisticService.getCinemaStatistics());

        return "admin/charts/cinemaChart";
    }

    @GetMapping("/cinemas/filter")
    public String filterCinemaStatistics(@RequestParam("startDate") String startDateStr,
                                         @RequestParam("endDate") String endDateStr,
                                         Model model) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<CinemaStatisticDTO> cinemas = statisticService.getCinemasByDateRange(startDate, endDate);

        // Tính tổng doanh thu và vé trong khoảng thời gian
        BigDecimal totalRevenue = cinemas.stream()
                .map(cinema -> cinema.getTotalRevenue() != null ? cinema.getTotalRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalTickets = cinemas.stream()
                .mapToLong(CinemaStatisticDTO::getTicketCount)
                .sum();

        String formattedRevenue = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalRevenue);

        model.addAttribute("totalRevenueFormatted", formattedRevenue);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("cinemas", cinemas);

        return "admin/charts/cinemaChart";
    }

}
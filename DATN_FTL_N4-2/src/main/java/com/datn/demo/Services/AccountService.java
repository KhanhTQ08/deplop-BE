package com.datn.demo.Services;

import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.InvoiceEntity;
import com.datn.demo.Repositories.AccountRepository;

import com.datn.demo.Repositories.InvoiceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AccountService {


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private InvoiceRepository invoiceRepository;

    public AccountEntity findAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    public AccountEntity findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
    // Lấy tất cả tài khoản
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    // Tìm tài khoản theo ID
    public Optional<AccountEntity> getAccountById(Integer id) {
        return accountRepository.findById(id);
    }

    public AccountEntity getAccountById(int accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }


    // Cập nhật tài khoản
    public AccountEntity updateAccount(AccountEntity account) {
        return accountRepository.save(account);
    }

    public List<AccountEntity> getAllAccount() {
        // Sử dụng phương thức findAll() của JpaRepository để lấy danh sách tài khoản
        return accountRepository.findAll();
    }

    // Lấy tài khoản có roleId = 2
    public List<AccountEntity> getAccountsByRoleId(int roleId) {
        return accountRepository.findByRoleRoleId(roleId);
    }

    // Phương thức để lấy danh sách hóa đơn của tài khoản
    public List<InvoiceEntity> getInvoicesByAccountId(Integer accountId) {
        // Giả sử trong bảng hóa đơn (Invoice) có trường accountId (liên kết với Account)
        return invoiceRepository.findByAccount_AccountId(accountId);
    }
    public boolean sendOtpToEmail(String email, HttpSession session) {
        // Kiểm tra email trong cơ sở dữ liệu
        AccountEntity account = accountRepository.findByEmail(email);
        if (account == null) {
            return false; // Email không tồn tại trong hệ thống
        }

        // Kiểm tra thời gian lần gửi OTP cuối cùng
        LocalDateTime lastOtpTime = (LocalDateTime) session.getAttribute("lastOtpTime");
        if (lastOtpTime != null && lastOtpTime.plusMinutes(1).isAfter(LocalDateTime.now())) {
            // Nếu chưa đủ 1 phút kể từ lần gửi trước, từ chối gửi OTP mới
            return false;
        }

        // Tạo mã OTP mới
        String otp = generateOtp();

        // Lưu OTP và thời gian vào session
        session.setAttribute("otp", otp);
        session.setAttribute("email", email);
        session.setAttribute("otpTime", LocalDateTime.now()); // Lưu thời gian tạo OTP
        session.setAttribute("lastOtpTime", LocalDateTime.now()); // Lưu thời gian gửi OTP lần cuối

        // Gửi OTP qua email
        sendOtpEmail(email, otp);

        return true;
    }



    // Hàm kiểm tra OTP và thay đổi mật khẩu
    public boolean verifyOtpAndChangePassword(String otp, String newPassword, HttpSession session) {
        String sessionOtp = (String) session.getAttribute("otp");
        String sessionEmail = (String) session.getAttribute("email");
        LocalDateTime otpTime = (LocalDateTime) session.getAttribute("otpTime");

        if (otpTime == null || sessionOtp == null || sessionEmail == null) {
            return false; // Không tìm thấy thông tin OTP
        }

        // Kiểm tra thời gian OTP đã quá 5 phút chưa
        if (otpTime.plusMinutes(3).isBefore(LocalDateTime.now())) {
            session.removeAttribute("otp");
            session.removeAttribute("email");
            session.removeAttribute("otpTime");
            return false; // OTP đã hết hạn
        }

        // Kiểm tra xem OTP có đúng không
        if (sessionOtp.equals(otp)) {
            // Cập nhật mật khẩu mới cho tài khoản
            AccountEntity account = accountRepository.findByEmail(sessionEmail);
            if (account != null) {
                // Mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
                String encodedPassword = passwordEncoder.encode(newPassword);
                account.setPassword(encodedPassword); // Lưu mật khẩu đã mã hóa
                accountRepository.save(account);

                // Xóa OTP khỏi session
                session.removeAttribute("otp");
                session.removeAttribute("email");
                session.removeAttribute("otpTime");
                return true;
            }
        }

        return false; // OTP không đúng hoặc hết hạn
    }
    public boolean changePassword(String newPassword, HttpSession session) {
        String sessionEmail = (String) session.getAttribute("email");
        if (sessionEmail == null) {
            return false; // Không tìm thấy email trong session
        }

        // Lấy tài khoản dựa trên email từ session
        AccountEntity account = accountRepository.findByEmail(sessionEmail);
        if (account == null) {
            return false; // Tài khoản không tồn tại
        }

        // Mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
        String encodedPassword = passwordEncoder.encode(newPassword);
        account.setPassword(encodedPassword); // Lưu mật khẩu đã mã hóa

        // Lưu tài khoản đã thay đổi mật khẩu
        accountRepository.save(account);

        // Xóa các thông tin OTP và email khỏi session
        session.removeAttribute("otp");
        session.removeAttribute("email");
        session.removeAttribute("otpTime");

        return true; // Mật khẩu đã được thay đổi thành công
    }

    // Hàm tạo mã OTP ngẫu nhiên
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // OTP có 6 chữ số
        return String.valueOf(otp);
    }

    // Hàm gửi OTP qua email (cần cấu hình JavaMailSender)
    private void sendOtpEmail(String email, String otp) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Mã OTP để xác minh hoặc đặt lại mật khẩu");
            helper.setText(buildOtpEmailContent(otp), true); // Nội dung HTML

            mailSender.send(mimeMessage); // Gửi email
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }

    private String buildOtpEmailContent(String otp) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; min-width: 100%; background-color: rgba(250, 90, 90, 0.8); padding: 1.5rem; box-sizing: border-box;'>")
                .append("<div style='max-width: 600px; margin: auto; background-color: white; padding: 2rem; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);'>")
                
                // Logo và lời chào
                .append("<div style='text-align: center; margin-bottom: 2rem;'>")
                .append("<img src='https://drive.google.com/uc?id=1nFnGPdWnlFK1XRuZtB-8hJWFEVMp0TSF' alt='Logo' style='width: 100px; height: 100px;'>")
                .append("<h1 style='font-size: 1.8rem; color: #800080;'>Chào mừng bạn đến với For The Love!</h1>")
                .append("</div>")

                // Phần thông báo OTP
                .append("<div style='border-bottom: 1px solid #eaeaea; padding-bottom: 1.5rem; margin-bottom: 1.5rem;'>")
                .append("<p>Xin chào bạn,</p>")
                .append("<p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu từ bạn. Để hoàn tất, vui lòng sử dụng mã OTP dưới đây:</p>")
                .append("<div style='text-align: center; margin: 2rem 0;'>")
                .append("<span style='font-size: 1.8rem; font-weight: bold; color: #ff3d49; background-color: #f9f9f9; padding: 1rem 2rem; border: 2px dashed #ff3d49; border-radius: 10px; display: inline-block;'>")
                .append(otp)
                .append("</span>")
                .append("</div>")
                .append("<p style='text-align: center;'>Hãy nhập mã này vào ô xác nhận để tiếp tục. Mã sẽ hết hạn sau <strong>5 phút</strong>.</p>")
                .append("</div>")

                // Phần hướng dẫn
                .append("<div style='margin-bottom: 2rem;'>")
                .append("<h2 style='font-size: 1.4rem; color: #555;'>Hướng dẫn</h2>")
                .append("<ul style='padding-left: 20px;'>")
                .append("<li>Không chia sẻ mã OTP với bất kỳ ai.</li>")
                .append("<li>Nếu không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</li>")
                .append("</ul>")
                .append("</div>")

                // Cảm ơn và liên hệ
                .append("<div style='text-align: center; border-top: 1px solid #eaeaea; padding-top: 1.5rem;'>")
                .append("<p style='font-size: 0.9rem; color: #555;'>Cảm ơn bạn đã tin tưởng và sử dụng For The Love!</p>")
                .append("</div>")

                .append("</div>")
                .append("</div>");

        return htmlContent.toString();
    }



}

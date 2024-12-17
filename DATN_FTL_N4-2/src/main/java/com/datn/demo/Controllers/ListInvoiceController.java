package com.datn.demo.Controllers;

import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.InvoiceEntity;
import com.datn.demo.Services.InvoiceService;
import com.datn.demo.Services.AccountService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("")
public class ListInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/list")
    public String showListInvoice(HttpServletRequest request, Model model, HttpSession session) {
        AccountEntity acc = (AccountEntity) session.getAttribute("acc");

	    // Kiểm tra nếu đã đăng nhập và là admin
	    if (acc != null && acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	        return "redirect:/printErrorAdmin"; // Trả về trang 404 nếu là admin
	    }
        if (acc != null) {
            Integer accountId = acc.getAccountId();
            List<InvoiceEntity> invoices = invoiceService.getInvoicesByAccountId(accountId);

            // Map để lưu mã QR cho từng hóa đơn
            Map<Integer, String> qrCodeMap = new HashMap<>();

            for (InvoiceEntity invoice : invoices) {
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String encryptedInvoiceId = encryptMD5(invoice.getInvoiceId().toString());
                String qrCodeUrl = baseUrl + "/print/" + encryptedInvoiceId;

                // Tạo mã QR cho URL và lưu vào Map
                String qrCodeBase64 = generateQRCodeBase64(qrCodeUrl);
                qrCodeMap.put(invoice.getInvoiceId(), qrCodeBase64);
            }

            model.addAttribute("invoices", invoices);
            model.addAttribute("qrCodeMap", qrCodeMap); // Thêm map chứa mã QR vào model
        } else {
            return "main/user/user-login";
        }

        return "main/user/listInvoice";
    }

    private String encryptMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateQRCodeBase64(String qrCodeText) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Set the size of the QR code and margin
            int size = 1000;  // Larger size for the QR code
            int margin = 3;   // Thin border around the QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, getEncodeHints(margin));

            // Define the colors for the QR code
            int qrCodeColor = 0x000000;
            int backgroundColor = 0xFFFFFF;

            // Create a BufferedImage to render the QR code with custom colors
            BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? qrCodeColor : backgroundColor);
                }
            }

            // Convert BufferedImage to PNG byte array
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            // Convert PNG to Base64
            String base64Image = Base64.getEncoder().encodeToString(pngData);
            return base64Image;
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Map<EncodeHintType, Object> getEncodeHints(int margin) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, margin);  // Thiết lập viền mỏng
        return hints;
    }

}


package com.datn.demo.Controllers;


import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.InvoiceEntity;
import com.datn.demo.Services.AccountService;
import com.datn.demo.Services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InvoiceDetailsAllUserController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private InvoiceService invoiceService;


    @GetMapping("/invoiceDetailsAllUser")
    public String invoiceDetailsAllUser(Model model) {
        // Lấy danh sách tất cả các tài khoản có role là 2
        List<AccountEntity> accountEntities = accountService.getAccountsByRoleId(2);

        // Lấy danh sách tất cả các hóa đơn
        List<InvoiceEntity> listInvoiceAllUser = invoiceService.getAllInvoice();

        // Lọc các tài khoản có hóa đơn
        List<AccountEntity> accountsWithInvoices = accountEntities.stream()
                .filter(account -> listInvoiceAllUser.stream()
                        .anyMatch(invoice -> invoice.getAccount().getAccountId() == (account.getAccountId())))
                .collect(Collectors.toList());

        // Thêm danh sách tài khoản đã lọc vào model
        model.addAttribute("accountEntities", accountsWithInvoices);

        // Thêm danh sách hóa đơn vào model
        model.addAttribute("listInvoiceAllUser", listInvoiceAllUser);

        System.out.println("Accounts with invoices: " + accountsWithInvoices.size());
        System.out.println("Invoices: " + listInvoiceAllUser.size());

        return "admin/components/hoadon";
    }


}

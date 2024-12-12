package com.datn.demo.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.demo.Entities.InvoiceDetailsEntity;
import com.datn.demo.Entities.InvoiceEntity;
import com.datn.demo.Repositories.InvoiceDetailsRepository;
@Service
public class InvoiceDetailsService {
	 @Autowired
	    private InvoiceDetailsRepository detailRepository;

//	    public List<InvoiceDetailsEntity> getDetailsByInvoiceId(int invoiceId) {
//	        return detailRepository.findByInvoice_InvoiceId(invoiceId);
//	    }
	    
//	    public List<InvoiceDetailsEntity> getInvoiceDetailsByInvoiceId(Integer invoiceId) {
//	        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
//	            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
//	        return invoice.getInvoiceDetails(); // Giả sử bạn đã ánh xạ quan hệ hóa đơn-chi tiết
//	    }
}

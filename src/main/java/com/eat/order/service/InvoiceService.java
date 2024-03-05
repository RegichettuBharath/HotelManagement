package com.eat.order.service;

import java.time.LocalDate;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eat.order.entity.Invoice;
import com.eat.order.entity.InvoiceDetails;
import com.eat.order.repository.InvoiceDetailsRepository;
import com.eat.order.repository.InvoiceRepository;

import jakarta.annotation.PostConstruct;



@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailsRepository invoiceDetailsRepository;

    private static int invoiceCounter;

    @PostConstruct
    public void initializeInvoiceCounter() {
       
        Integer maxInvoiceNumber = invoiceRepository.findMaxInvoiceNumber();
       
        invoiceCounter = (maxInvoiceNumber != null) ? maxInvoiceNumber + 1 : 1;
    }

    public Invoice createInvoice(List<InvoiceDetails> invoiceDetailsList, double totalAmount) {
       
        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(generateInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setTotalAmount(totalAmount);
       
        invoice = invoiceRepository.save(invoice);
        
        final String invoiceNo = invoice.getInvoiceNo();
        final Invoice[] finalInvoice = {null};  
        
        invoiceDetailsList.forEach(invoiceDetails -> {
            invoiceDetails.setInvoiceNo(invoiceNo);
            invoiceDetails.setInvoice(finalInvoice[0]); 
        });

        finalInvoice[0] = invoice; 

        invoice.setInvoiceDetailsList(invoiceDetailsRepository.saveAll(invoiceDetailsList));

        return invoiceRepository.save(invoice);
    }
    
    private String generateInvoiceNumber() {
    	String sequentialInvoiceNumber = "INV" + String.format("%04d", invoiceCounter);
        invoiceCounter++;
        return sequentialInvoiceNumber;
    }

    public Invoice getInvoiceByInvoiceNo(String invoiceNo) {
        return invoiceRepository.findByInvoiceNo(invoiceNo);
    }

    public List<InvoiceDetails> getInvoiceDetailsByInvoiceNo(String invoiceNo) {
        return invoiceDetailsRepository.findByInvoiceNo(invoiceNo);
    }
}

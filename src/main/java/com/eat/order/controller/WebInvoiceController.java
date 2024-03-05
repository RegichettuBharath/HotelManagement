package com.eat.order.controller;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eat.order.entity.Invoice;
import com.eat.order.entity.InvoiceDetails;
import com.eat.order.entity.Item;
import com.eat.order.service.InvoiceService;
import com.eat.order.service.ItemService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/web/invoices")
public class WebInvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(WebInvoiceController.class);

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ItemService itemService;

    @GetMapping("/create")
    public String createInvoiceForm(Model model) {
        List<Item> items = itemService.getAllItems();
        model.addAttribute("items", items);
        model.addAttribute("invoice", new Invoice());
        return "invoice";
    }

    @PostMapping("/create")
    public String createInvoice(@ModelAttribute Invoice invoice, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            List<InvoiceDetails> invoiceDetailsList = invoice.getInvoiceDetailsList();

            if (invoiceDetailsList == null) {
                invoiceDetailsList = new ArrayList<>();
            }

            double totalAmount = 0.0;
            for (InvoiceDetails invoiceDetail : invoiceDetailsList) {
                totalAmount += invoiceDetail.getTotalAmount();
            }

            Invoice createdInvoice = invoiceService.createInvoice(invoiceDetailsList, totalAmount);        
            logger.info("Invoice created successfully with id: {}", createdInvoice.getId());

            redirectAttributes.addFlashAttribute("invoiceNo", createdInvoice.getInvoiceNo());
            redirectAttributes.addFlashAttribute("showSuccessPage", true);

            return "redirect:/web/invoices/success/"+ createdInvoice.getInvoiceNo();
            
        } catch (Exception e) {        
            logger.error("Error creating invoice", e);
            return "error"; 
        }
    }

    @GetMapping("/success/{invoiceNo}")
    public String showSuccessPage(@PathVariable String invoiceNo, Model model) {
        if (invoiceNo != null && !invoiceNo.isEmpty()) {
            Invoice invoice = invoiceService.getInvoiceByInvoiceNo(invoiceNo);
            List<InvoiceDetails> invoiceDetailsList = invoiceService.getInvoiceDetailsByInvoiceNo(invoiceNo);

            model.addAttribute("invoice", invoice);
            model.addAttribute("invoiceDetailsList", invoiceDetailsList);
            model.addAttribute("showSuccessPage", true);

            return "success";
        } else {
            return "error"; 
        }
    }
}


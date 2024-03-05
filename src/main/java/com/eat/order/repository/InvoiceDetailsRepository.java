package com.eat.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eat.order.entity.InvoiceDetails;

public interface InvoiceDetailsRepository extends JpaRepository<InvoiceDetails, Long> {
	
	List<InvoiceDetails> findByInvoiceNo(String invoiceNo);
}

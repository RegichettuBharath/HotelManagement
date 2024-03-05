package com.eat.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eat.order.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>{

	@Query("SELECT MAX(CAST(SUBSTRING(i.invoiceNo, 4) AS integer)) FROM Invoice i")
	Integer findMaxInvoiceNumber();

	Invoice findByInvoiceNo(String invoiceNo);

}

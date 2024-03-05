package com.eat.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eat.order.entity.Item;


public interface ItemRepository extends JpaRepository<Item, Long> 
{

	List<Item> findByItemNameIgnoreCase(String itemName);
    
}

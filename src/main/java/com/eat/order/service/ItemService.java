package com.eat.order.service;


import java.util.List;

import com.eat.order.entity.Item;

public interface ItemService {

    List<Item> getAllItems();   
    Item getItemById(Long id);
    Item saveItem(Item item);
    void deleteItem(Long id);
    Item updateItem(Long id, Item updatedItem);
    
}



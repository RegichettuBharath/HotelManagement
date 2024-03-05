package com.eat.order.service;



import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eat.order.entity.Item;
import com.eat.order.exception.DuplicateItemException;
import com.eat.order.exception.ItemNotFoundException;
import com.eat.order.repository.ItemRepository;


@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public Item saveItem(Item item) {
        List<Item> existingItemsByName = itemRepository.findByItemNameIgnoreCase(item.getItemName());
        if(!existingItemsByName.isEmpty()) {
            throw new DuplicateItemException("Item name already exists");
        }
        return itemRepository.save(item);
    }


    @Override
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        itemRepository.deleteById(id);
    }

    @Override
    public Item updateItem(Long id, Item updatedItem) {
    	List<Item> existingItems = itemRepository.findByItemNameIgnoreCase(updatedItem.getItemName());
        existingItems.removeIf(item -> item.getId().equals(id));

        if (!existingItems.isEmpty()) {
            throw new DuplicateItemException("Item name already exists");
        }
        Item existingItem = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        existingItem.setItemName(updatedItem.getItemName());
        existingItem.setPrice(updatedItem.getPrice());

        return itemRepository.save(existingItem);
    }

}

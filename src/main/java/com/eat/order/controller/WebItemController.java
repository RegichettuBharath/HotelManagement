package com.eat.order.controller;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.eat.order.entity.Item;
import com.eat.order.exception.DuplicateItemException;
import com.eat.order.exception.ItemNotFoundException;
import com.eat.order.service.ItemService;

import java.util.*;

@Controller
@RequestMapping("/web/items")
public class WebItemController {

	 private static final Logger logger = LoggerFactory.getLogger(WebItemController.class);

	    @Autowired
	    private ItemService itemService;

	    @GetMapping("/home")
	    public String home() {
	        logger.info("Home page accessed");
	        return "home";
	    }

	    @GetMapping("/fetch-all")
	    public String fetchAll(Model model, HttpSession session) {
	        try {
	        	@SuppressWarnings("unchecked")
				List<Item> sessionItems = (List<Item>) session.getAttribute("sessionItems");

	            if (sessionItems == null) {
	                sessionItems = itemService.getAllItems();
	                session.setAttribute("sessionItems", sessionItems);
	            }
	            model.addAttribute("items", sessionItems);
	            logger.info("Fetched all items successfully");
	            return "fetchAll";
	        } catch (Exception e) {
	            logger.error("Error fetching all items", e);
	            return "error";
	        }
	    }

	    @GetMapping("/insert")
	    public String insertPage(Model model) {
	        model.addAttribute("item", new Item());
	        logger.info("Insert page accessed");
	        return "insert";
	    }

	    @PostMapping("/save")
	    public String saveItem(@Valid @ModelAttribute Item item, BindingResult bindingResult, HttpSession session, Model model) {
	        if (bindingResult.hasErrors()) {
	            model.addAttribute("item", item);
	            logger.warn("Validation errors while saving item");
	            return "insert";
	        }

	        try {
	            itemService.saveItem(item);
	            updateSession(session);
	            logger.info("Item saved successfully: {}", item);
	        } catch (DuplicateItemException e) {
	            model.addAttribute("errorMessage", e.getMessage());
	            logger.warn("Duplicate item found: {}", e.getMessage());
	            return "insert";
	        } catch (Exception e) {
	            logger.error("Error saving item", e);
	            return "error";
	        }

	        return "redirect:/web/items/fetch-all";
	    }

	    @GetMapping("/edit/{id}")
	    public String editPage(@PathVariable Long id, Model model, HttpSession session) {
	        try {
	            Item item = itemService.getItemById(id);
	            model.addAttribute("item", item);
	            session.setAttribute("editedItemId", id);
	            logger.info("Edit page accessed for item id: {}", id);
	            return "edit";
	        } catch (ItemNotFoundException e) {
	            logger.warn("Item with id {} not found during edit", id);
	            return "error";
	        } catch (Exception e) {
	            logger.error("Error accessing edit page", e);
	            return "error";
	        }
	    }

	    @PostMapping("/update/{id}")
	    public String updateItem(@PathVariable Long id, @Valid @ModelAttribute Item updatedItem, BindingResult bindingResult, HttpSession session, Model model) {
	        if (bindingResult.hasErrors()) {
	            model.addAttribute("item", updatedItem);
	            logger.warn("Validation errors while updating item");
	            return "edit";
	        }

	        Long editedItemId = (Long) session.getAttribute("editedItemId");

	        try {
	            itemService.updateItem(editedItemId, updatedItem);
	            updateSession(session);
	            logger.info("Item updated successfully: {}", updatedItem);
	        } catch (DuplicateItemException e) {
	            model.addAttribute("errorMessage", e.getMessage());
	            logger.warn("Duplicate item found during update: {}", e.getMessage());
	            return "edit";
	        } catch (ItemNotFoundException e) {
	            logger.warn("Item with id {} not found during update", id);
	            return "error";
	        } catch (Exception e) {
	            logger.error("Error updating item", e);
	            return "error";
	        }

	        return "redirect:/web/items/fetch-all";
	    }

	    @GetMapping("/delete/{id}")
	    public String deleteItem(@PathVariable Long id, HttpSession session) {
	        try {
	            itemService.deleteItem(id);
	            updateSession(session);
	            logger.info("Item deleted successfully with id: {}", id);
	            return "redirect:/web/items/fetch-all";
	        } catch (ItemNotFoundException e) {
	            logger.warn("Item with id {} not found during delete", id);
	            return "error";
	        } catch (Exception e) {
	            logger.error("Error deleting item with id {}", id, e);
	            return "error";
	        }
	    }

	    private void updateSession(HttpSession session) {
	        try {
	            List<Item> items = itemService.getAllItems();
	            session.setAttribute("sessionItems", items);
	            logger.info("Session updated successfully with {} items", items.size());
	        } catch (Exception e) {
	            logger.error("Error updating session", e);
	        }
	    }

}


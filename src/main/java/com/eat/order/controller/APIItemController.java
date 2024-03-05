package com.eat.order.controller;



import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.eat.order.entity.Item;
import com.eat.order.exception.DuplicateItemException;
import com.eat.order.exception.ItemNotFoundException;
import com.eat.order.service.ItemService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class APIItemController {

	 private static final Logger logger = LoggerFactory.getLogger(APIItemController.class);

	    @Autowired
	    private ItemService itemService;

	    @GetMapping("/fetch-all")
	    public ResponseEntity<List<Item>> getAllItems(HttpSession session) {
	    	try {
	            @SuppressWarnings("unchecked")
				List<Item> sessionItems = (List<Item>) session.getAttribute("sessionItems");

	            if (sessionItems == null) {
	                sessionItems = itemService.getAllItems();
	                session.setAttribute("sessionItems", sessionItems);
	            }
	            logger.info("Fetched all items successfully");
	            return new ResponseEntity<>(sessionItems, HttpStatus.OK);
	        } catch (Exception e) {
	            logger.error("Error fetching all items", e);
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	    @GetMapping("/get/{id}")
	    public ResponseEntity<Object> getItemById(@PathVariable Long id, HttpSession session) {
	    	Map<String, String> response = new HashMap<>();
	    	try {
	            @SuppressWarnings("unchecked")
				List<Item> sessionItems = (List<Item>) session.getAttribute("sessionItems");

	            if (sessionItems != null) {
	                for (Item item : sessionItems) 
	                {
	                    if (item.getId().equals(id)) 
	                    {
	                        logger.info("Fetched item by id {} successfully from session", id);
	                        return new ResponseEntity<>(item, HttpStatus.OK);
	                    }
	                    break;
	                }
	                throw new ItemNotFoundException(id);
	            }else {
	                Item item = itemService.getItemById(id);
	                logger.info("Fetched item by id {} successfully from database", id);
	                return new ResponseEntity<>(item, HttpStatus.OK);
	            }
	        }catch (ItemNotFoundException e) {
	            logger.warn("Item with id {} not found", id);
	            response.put("error", e.getMessage());
	            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
	        } catch (Exception e) {
	            logger.error("Error fetching item by id {}", id, e);
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	    @PostMapping("/save")
	    public ResponseEntity<Object> saveItem(@Valid @RequestBody Item item,BindingResult bindingResult,  HttpSession session) {
	    	if (bindingResult.hasErrors())
	    	{
	            bindingResult.getAllErrors().forEach(error -> {
	                logger.warn("Validation error: {} - {}", ((FieldError) error).getField(), error.getDefaultMessage());
	            });

	            Map<String, String> validationErrors = new HashMap<>();
	            bindingResult.getAllErrors().forEach(error -> {
	                validationErrors.put(((FieldError) error).getField(), error.getDefaultMessage());
	            });
	            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
	        }

	    	
	        try {
	            itemService.saveItem(item);
	            updateSession(session);
	            logger.info("Item saved successfully: {}", item);
	            return new ResponseEntity<>(item, HttpStatus.CREATED);
	        } catch (DuplicateItemException e) {
	            logger.warn("Duplicate item found: {}", e.getMessage());
	            Map<String, String> errorResponse = new HashMap<>();
	            errorResponse.put("errorMessage", e.getMessage());
	            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	        } catch (Exception e) {
	            logger.error("Error saving item", e);
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	    @PutMapping("/update/{id}")
	    public ResponseEntity<Object> updateItem(@PathVariable Long id,@Valid @RequestBody Item updatedItem,BindingResult bindingResult,  HttpSession session) {
	       
	    	if (bindingResult.hasErrors()) {
	            bindingResult.getAllErrors().forEach(error -> {
	                logger.warn("Validation error: {} - {}", ((FieldError) error).getField(), error.getDefaultMessage());
	            });

	            Map<String, String> validationErrors = new HashMap<>();
	            bindingResult.getAllErrors().forEach(error -> {
	                validationErrors.put(((FieldError) error).getField(), error.getDefaultMessage());
	            });
	            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
	        }
	    	
	    	Map<String, String> errorResponse = new HashMap<>();
	    	try {
	            Item existingItem = itemService.getItemById(id);
	            if (updatedItem.getItemName() != null) {
	                existingItem.setItemName(updatedItem.getItemName());
	            }

	            if (updatedItem.getPrice() != 0.0) {
	                existingItem.setPrice(updatedItem.getPrice());
	            }

	            itemService.updateItem(id, existingItem);
	            updateSession(session);
	            logger.info("Item updated successfully: {}", existingItem);
	            return new ResponseEntity<>(existingItem, HttpStatus.OK);
	        } catch (DuplicateItemException e) {
	            logger.warn("Duplicate item found during update: {}", e.getMessage());
	            errorResponse.put("errorMessage", e.getMessage());
	            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	        } catch (ItemNotFoundException e) {
	            logger.warn("Item with id {} not found during update", id);
	            errorResponse.put("errorMessage", e.getMessage());
	            return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
	        } catch (Exception e) {
	            logger.error("Error updating item", e);
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	    @DeleteMapping("/delete/{id}")
	    public ResponseEntity<Object> deleteItem(@PathVariable Long id, HttpSession session) {
	        Map<String, String> response = new HashMap<>();
	        try {
	            itemService.deleteItem(id);
	            updateSession(session);
	            logger.info("Item deleted successfully with id: {}", id);
	            response.put("message", "Item deleted successfully");
	            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
	        } catch (ItemNotFoundException e) {
	            logger.warn("Item with id {} not found during delete", id);
	            response.put("error", e.getMessage());
	            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	        } catch (Exception e) {
	            logger.error("Error deleting item with id {}", id, e);
	            response.put("error", "Internal Server Error");
	            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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

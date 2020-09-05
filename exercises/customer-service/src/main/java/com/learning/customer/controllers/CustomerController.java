package com.learning.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.learning.customer.model.Customer;
import com.learning.customer.repository.CustomerRepository;

@CrossOrigin
@RestController
@RequestMapping("/customers")
public class CustomerController {

	@Autowired
	CustomerRepository repository;
	
	@PostMapping
	public Customer add(@RequestBody Customer customer) {
		return repository.save(customer);
	}
	
	@PutMapping
	public Customer update(@RequestBody Customer customer) {
		return repository.save(customer);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping("/{id}")
	public Customer findById(@PathVariable("id") Integer id) {
		return repository.findById(id).get();
	}
	
}

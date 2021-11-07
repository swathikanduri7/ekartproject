package com.swaraj.sportyshoes.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.swaraj.sportyshoes.entities.Role;
import com.swaraj.sportyshoes.entities.RoleName;
import com.swaraj.sportyshoes.entities.User;
import com.swaraj.sportyshoes.model.OrderInfo;
import com.swaraj.sportyshoes.model.ProductInfo;
import com.swaraj.sportyshoes.pagination.PaginationResult;
import com.swaraj.sportyshoes.repositories.OrderDAO;
import com.swaraj.sportyshoes.repositories.ProductDAO;
import com.swaraj.sportyshoes.repositories.RoleRepository;
import com.swaraj.sportyshoes.repositories.UserRepository;

@Controller
public class HomeController {

	@Autowired
	private ProductDAO productDAO;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private RoleRepository roleRepository;



	@RequestMapping(value = { "/user/orderList" }, method = RequestMethod.GET)
	public String userOrderList(HttpServletRequest request, HttpServletResponse response, Model model, //
			@RequestParam(value = "page", defaultValue = "1") String pageStr) {

		int page = 1;
		HttpSession sesion = request.getSession();
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String username = null;
		if (principal instanceof UserDetails) {
		   username = ((UserDetails)principal).getUsername();
		} else {
		   username = principal.toString();
		}
		
		User user = userRepository.findByEmail(username);
		try {
			page = Integer.parseInt(pageStr);
		} catch (Exception e) {
		}
		
		final int MAX_RESULT = 5;
		final int MAX_NAVIGATION_PAGE = 10;

		PaginationResult<OrderInfo> paginationResult //
				= orderDAO.listOrderInfoForUser(page, MAX_RESULT, MAX_NAVIGATION_PAGE,user.getId());

		model.addAttribute("paginationResult", paginationResult);
		return "orderList";
	}

	@GetMapping("/users/showForm")
	public String showUserForm(User user) {
		return "add-user";
	}

	@PostMapping("/users/add")
	public String addUser(@Valid User user, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "add-student";
		}
		List<Role> roles = new ArrayList<>();

		Role role = roleRepository.getRoleByName(RoleName.ROLE_USER.toString());

		user.setRoles(roles);
		this.userRepository.save(user);
		return "redirect:/admin/accountInfo";
	}

	@GetMapping("/users/edit/{id}")
	public String showUpdateForm(@PathVariable("id") int id, Model model) {
		User user = this.userRepository.findById(id);
		if (null != user) {
			model.addAttribute("user", user);
			return "update-student";
		}
		return null;
	}
	
	

	@PostMapping("/users/update/{id}")
	public String updateStudent(@PathVariable("id") int id, @Valid User user, BindingResult result, Model model) {
		if (result.hasErrors()) {
			user.setId(id);
			return "update-student";
		}

		// update student
		userRepository.save(user);

		// get all students ( with update)
		model.addAttribute("users", this.userRepository.findAll());
		return "redirect:/admin/accountInfo";
	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable("id") int id, Model model) {

		User user = this.userRepository.findById(id);
		if (null != user) {
			model.addAttribute("user", user);
			return "redirect:/admin/accountInfo";
		}
		return null;

	}

}

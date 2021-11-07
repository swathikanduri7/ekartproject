package com.swaraj.sportyshoes.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swaraj.sportyshoes.entities.Category;
import com.swaraj.sportyshoes.entities.Product;
import com.swaraj.sportyshoes.entities.User;
import com.swaraj.sportyshoes.form.CategoryForm;
import com.swaraj.sportyshoes.form.DateRange;
import com.swaraj.sportyshoes.form.ProductForm;
import com.swaraj.sportyshoes.model.OrderDetailInfo;
import com.swaraj.sportyshoes.model.OrderInfo;
import com.swaraj.sportyshoes.model.ProductInfo;
import com.swaraj.sportyshoes.pagination.PaginationResult;
import com.swaraj.sportyshoes.repositories.CategoryDAO;
import com.swaraj.sportyshoes.repositories.OrderDAO;
import com.swaraj.sportyshoes.repositories.ProductDAO;
import com.swaraj.sportyshoes.repositories.RoleRepository;
import com.swaraj.sportyshoes.repositories.UserRepository;
import com.swaraj.sportyshoes.validator.ProductFormValidator;





@Controller
@Transactional
public class AdminController {

   @Autowired
   private OrderDAO orderDAO;
   
   @Autowired
   private UserRepository userRepository;

   @Autowired
   private RoleRepository roleRepository;
   
   @Autowired
   private ProductDAO productDAO;
   
   @Autowired
   private CategoryDAO categoryDAO;
   
   

   @Autowired
   private ProductFormValidator productFormValidator;

   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
      Object target = dataBinder.getTarget();
      if (target == null) {
         return;
      }
      System.out.println("Target=" + target);

      if (target.getClass() == ProductForm.class) {
         dataBinder.setValidator(productFormValidator);
      }
   }

	@GetMapping("/home")
	public String home(Model model) {
		int page = 1;
		final int maxResult = 5;
		final int maxNavigationPage = 10;
		
       List<User> userDetails= userRepository.findAll();
	      model.addAttribute("users", userDetails);
		 
	     PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
				maxResult, maxNavigationPage, null);

		model.addAttribute("paginationProducts", result);
		return "userhome";
	}
	
	@GetMapping("/admin/users/search")
	public String home(@RequestParam String keyword,Model model) {
		int page = 1;
		final int maxResult = 5;
		final int maxNavigationPage = 10;
		
       List<User> userDetails= new ArrayList<>();
       User user =  userRepository.findByEmail(keyword);
       userDetails.add(user);
	   model.addAttribute("users", userDetails);
		 
	     PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
				maxResult, maxNavigationPage, null);

		model.addAttribute("paginationProducts", result);
		return "userhome";
	}
	
   // GET: Show Login Page
   @RequestMapping(value = { "/admin/login" }, method = RequestMethod.GET)
   public String login(Model model) {
      return "login";
   }

   @RequestMapping(value = { "/admin/accountInfo" }, method = RequestMethod.GET)
   public String accountInfo(Model model) {

      /*UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      System.out.println(userDetails.getPassword());
      System.out.println(userDetails.getUsername());
      System.out.println(userDetails.isEnabled());
*/
	   List<User> userDetails= userRepository.findAll();
      model.addAttribute("users", userDetails);
      return "accountInfo";
   }

   @RequestMapping(value = { "/admin/orderList" }, method = RequestMethod.GET)
   public String orderList(Model model, //
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
      int page = 1;
      try {
         page = Integer.parseInt(pageStr);
      } catch (Exception e) {
      }
      final int MAX_RESULT = 5;
      final int MAX_NAVIGATION_PAGE = 10;
/*
      PaginationResult<OrderInfo> paginationResult //
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);*/
      List<OrderInfo> orders  = orderDAO.listOrders(page, MAX_RESULT, MAX_NAVIGATION_PAGE);
      model.addAttribute("paginationResult", orders);
   
      return "orderList";
   }
   
   @RequestMapping(value = { "/admin/orders/search" }, method = RequestMethod.GET)
   public String orderListbyDateOrCategory(@RequestParam String keyword,Model model) {
     
/*
      PaginationResult<OrderInfo> paginationResult //
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);*/
      List<OrderInfo> orders  = orderDAO.orderListbyDateOrCategory(keyword);
      model.addAttribute("paginationResult", orders);
      
           
      return "orderList";
   }


   // GET: Show product.
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.GET)
   public String product(Model model, @RequestParam(value = "code", defaultValue = "") String code) {
      ProductForm productForm = null;

      if (code != null && code.length() > 0) {
         Product product = productDAO.findProduct(code);
         if (product != null) {
            productForm = new ProductForm(product);
         }
      }
      if (productForm == null) {
         productForm = new ProductForm();
         productForm.setNewProduct(true);
      }
      List<Category> categories = categoryDAO.findAll(); 
      productForm.setCategories(categories);
      model.addAttribute("productForm", productForm);
      return "product";
   }

   // POST: Save product
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.POST)
   public String productSave(HttpServletRequest request,Model model, //
         @ModelAttribute("productForm") @Validated ProductForm productForm, //
         BindingResult result, //
         final RedirectAttributes redirectAttributes) {
	   Product product = null;
      if (result.hasErrors()) {
         return "product";
      }
      try {
    	  product=productDAO.save(productForm);
    	  
    	  String categoriesId = request.getParameter("category");
          int id = Integer.parseInt(categoriesId);
  		  Category category = categoryDAO.findById(id);  		 
          category.getProducts().add(product); 		
 		 categoryDAO.saveCategory(category);
      } catch (Exception e) {
         Throwable rootCause = ExceptionUtils.getRootCause(e);
         String message = rootCause.getMessage();
         model.addAttribute("errorMessage", message);
         
         
 		 
         // Show product form.
         return "product";
      }

      return "redirect:/productList";
   }
   
   @RequestMapping(value = { "/admin/assignProductToACategory" }, method = RequestMethod.GET)
   public String assignProductToACategory(HttpServletRequest request, Model model) {
	   CategoryForm categoryForm = new CategoryForm();
	   List<Product> products = productDAO.findAll();
	   List<Category> categories = categoryDAO.findAll();   
      model.addAttribute("products", products);
      model.addAttribute("categories", categories);
      model.addAttribute("categoryForm", categoryForm);
      return "category_product";
   }
   
   
   @RequestMapping(value = { "/admin/saveProductCategory" }, method = RequestMethod.POST)
   public String shoppingCartFinalize( @ModelAttribute("categoryForm") @Validated CategoryForm categoryForm,HttpServletRequest request, HttpServletResponse response) {
	  /* List<Product> products = productDAO.findAll();
	   List<Category> categories = categoryDAO.findAll();   
*/
		String categoriesId = request.getParameter("category");
		String productsId = request.getParameter("product");
		
		int id = Integer.parseInt(categoriesId);
		Category category = categoryDAO.findById(id);
		
		
		Product product = productDAO.findProduct(productsId);
		
		category.getProducts().add(product);
		
		categoryDAO.saveCategory(category);
		
		 return "redirect:/admin/assignProductToACategory";
   }
   
   
   
   @RequestMapping(value = { "/admin/order" }, method = RequestMethod.GET)
   public String orderView(Model model, @RequestParam("orderId") String orderId) {
      OrderInfo orderInfo = null;
      if (orderId != null) {
         orderInfo = this.orderDAO.getOrderInfo(orderId);
      }
      if (orderInfo == null) {
         return "redirect:/admin/orderList";
      }
      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
      orderInfo.setDetails(details);

      model.addAttribute("orderInfo", orderInfo);

      return "order";
   }
   
  

}

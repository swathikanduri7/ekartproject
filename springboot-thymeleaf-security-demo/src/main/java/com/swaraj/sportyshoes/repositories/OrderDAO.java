package com.swaraj.sportyshoes.repositories;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swaraj.sportyshoes.entities.Order;
import com.swaraj.sportyshoes.entities.OrderDetail;
import com.swaraj.sportyshoes.entities.Product;
import com.swaraj.sportyshoes.entities.User;
import com.swaraj.sportyshoes.model.CartInfo;
import com.swaraj.sportyshoes.model.CartLineInfo;
import com.swaraj.sportyshoes.model.CustomerInfo;
import com.swaraj.sportyshoes.model.OrderDetailInfo;
import com.swaraj.sportyshoes.model.OrderInfo;
import com.swaraj.sportyshoes.pagination.PaginationResult;



@Transactional
@Repository
public class OrderDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ProductDAO productDAO;
	
	   @Autowired
	   private UserRepository userRepository;

	private int getMaxOrderNum() {
		String sql = "Select max(o.orderNum) from " + Order.class.getName() + " o ";
		Session session = this.sessionFactory.getCurrentSession();
		Query<Integer> query = session.createQuery(sql, Integer.class);
		Integer value = (Integer) query.getSingleResult();
		if (value == null) {
			return 0;
		}
		return value;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrder(CartInfo cartInfo) {
		Session session = this.sessionFactory.getCurrentSession();

		int orderNum = this.getMaxOrderNum() + 1;
		Order order = new Order();

		order.setId(UUID.randomUUID().toString());
		order.setOrderNum(orderNum);
		order.setOrderDate(new Date());
		order.setAmount(cartInfo.getAmountTotal());

		CustomerInfo customerInfo = cartInfo.getCustomerInfo();
		order.setCustomerName(customerInfo.getName());
		order.setCustomerEmail(customerInfo.getEmail());
		order.setCustomerPhone(customerInfo.getPhone());
		order.setCustomerAddress(customerInfo.getAddress());

		session.persist(order);

		List<CartLineInfo> lines = cartInfo.getCartLines();
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = null;
		if (principal instanceof UserDetails) {
		   username = ((UserDetails)principal).getUsername();
		} else {
		   username = principal.toString();
		}
		
		User user = userRepository.findByEmail(username);

		for (CartLineInfo line : lines) {
			OrderDetail detail = new OrderDetail();
			detail.setId(UUID.randomUUID().toString());
			detail.setOrder(order);
			detail.setAmount(line.getAmount());
			detail.setPrice(line.getProductInfo().getPrice());
			detail.setQuanity(line.getQuantity());

			String code = line.getProductInfo().getCode();
			Product product = this.productDAO.findProduct(code);
			detail.setProduct(product);
			
			detail.setUser(user);

			session.persist(detail);
		}

		// Order Number!
		cartInfo.setOrderNum(orderNum);
		// Flush
		session.flush();
	}

	// @page = 1, 2, ...
	public PaginationResult<OrderInfo> listOrderInfo(int page, int maxResult, int maxNavigationPage) {
		String sql = "Select new " + OrderInfo.class.getName()//
				+ "(ord.id, ord.orderDate, ord.orderNum, ord.amount, "
				+ " ord.customerName, ord.customerAddress, ord.customerEmail, ord.customerPhone) " + " from "
				+ Order.class.getName() + " ord "//
				+ " order by ord.orderNum desc";

		Session session = this.sessionFactory.getCurrentSession();
		Query<OrderInfo> query = session.createQuery(sql, OrderInfo.class);
		return new PaginationResult<OrderInfo>(query, page, maxResult, maxNavigationPage);
	}
	
	
	public List<OrderInfo> listOrders(int page, int maxResult, int maxNavigationPage) {
		String sql = "Select distinct ord.id, ord.order_date, ord.order_num, ord.amount,"
				+ "          ord.customer_name, ord.customer_address, ord.customer_email,"
				+ "          ord.customer_Phone  from orders ord"
				+ "        left  join order_details od on od.order_id=ord.id"
				+ "        left  join products pdt on pdt.code=od.product_id"
				+ "         left join category_products cp on cp.product_id=od.product_id"
				+ "        left  join categories cat on cat.id=cp.category_id order by ord.order_num desc;";

		Session session = this.sessionFactory.getCurrentSession();
		SQLQuery  query = session.createSQLQuery(sql);
		List<Object[]> rows = query.list();
		
		List<OrderInfo> orders= new ArrayList<>();
		for(Object[] row : rows){
			OrderInfo ord = new OrderInfo();
			ord.setId(row[0].toString());
			ord.setOrderDate(row[1].toString());
			/*String string = row[1].toString();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Date date;
			try {
				date = formatter.parse(string);
				System.out.println(date);				
				ord.setOrderDate(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			ord.setOrderNum(Integer.parseInt(row[2].toString()));
			ord.setAmount(Double.parseDouble(row[3].toString()));
			ord.setCustomerName(row[4].toString());
			ord.setCustomerAddress(row[5].toString());
			ord.setCustomerEmail(row[6].toString());			
			ord.setCustomerPhone(row[7].toString());			
			System.out.println(ord);
			orders.add(ord);
		}
		return orders;
	}

	public Order findOrder(String orderId) {
		Session session = this.sessionFactory.getCurrentSession();
		return session.find(Order.class, orderId);
	}

	public OrderInfo getOrderInfo(String orderId) {
		Order order = this.findOrder(orderId);
		if (order == null) {
			return null;
		}
		return new OrderInfo(order.getId(), order.getOrderDate().toString(), //
				order.getOrderNum(), order.getAmount(), order.getCustomerName(), //
				order.getCustomerAddress(), order.getCustomerEmail(), order.getCustomerPhone());
	}

	public List<OrderDetailInfo> listOrderDetailInfos(String orderId) {
		String sql = "Select new " + OrderDetailInfo.class.getName() //
				+ "(d.id, d.product.code, d.product.name , d.quanity,d.price,d.amount) "//
				+ " from " + OrderDetail.class.getName() + " d "//
				+ " where d.order.id = :orderId ";

		Session session = this.sessionFactory.getCurrentSession();
		Query<OrderDetailInfo> query = session.createQuery(sql, OrderDetailInfo.class);
		query.setParameter("orderId", orderId);

		return query.getResultList();
	}
	

	


	public PaginationResult<OrderInfo> listOrderInfoForUser(int page, int maxResult, int maxNavigationPage,
			Integer userId) {
		String sql = "Select new " + OrderDetailInfo.class.getName() //
				+ "(d.id, d.product.code, d.product.name , d.quanity,d.price,d.amount,d.order.id as orderId ) "//
				+ " from " + OrderDetail.class.getName() + " d "//
				+ " where d.user.id = :userId ";

		Session session = this.sessionFactory.getCurrentSession();
		Query<OrderDetailInfo> query = session.createQuery(sql, OrderDetailInfo.class);
		query.setParameter("userId", userId); 
		List<OrderDetailInfo> list = query.getResultList();
		List<String> orders = new ArrayList<>();
		for(OrderDetailInfo orderDetailInfo:list){
			orders.add(orderDetailInfo.getOrderId());
		}
		
		
		sql = "Select new " + OrderInfo.class.getName()//
				+ "(ord.id, ord.orderDate, ord.orderNum, ord.amount, "
				+ " ord.customerName, ord.customerAddress, ord.customerEmail, ord.customerPhone) " + " from "
				+ Order.class.getName() + " ord  where ord.id in (:orders)"//
				+ " order by ord.orderNum desc";

		session = this.sessionFactory.getCurrentSession();
		Query<OrderInfo> query1 = session.createQuery(sql, OrderInfo.class);
		query1.setParameter("orders", orders); 
		return new PaginationResult<OrderInfo>(query1, page, maxResult, maxNavigationPage);
	}

	public List<OrderInfo> orderListbyDateOrCategory(String keyword) {
		String sql = "Select distinct ord.id, ord.order_date, ord.order_num, ord.amount,"
				+ "          ord.customer_name, ord.customer_address, ord.customer_email,"
				+ "          ord.customer_Phone  from orders ord"
				+ "        left  join order_details od on od.order_id=ord.id"
				+ "        left  join products pdt on pdt.code=od.product_id"
				+ "         left join category_products cp on cp.product_id=od.product_id"
				+ "        left  join categories cat on cat.id=cp.category_id ";
		
		if(null!=keyword && !keyword.isEmpty()){
			sql = sql + " where cat.categoryname like '%"+keyword+"%'";
		}
		if(null!=keyword && keyword.contains("-")){
			sql = sql + " OR Date(order_date) =CAST('"+keyword+"' AS DATETIME);";
		}

		Session session = this.sessionFactory.getCurrentSession();
		SQLQuery  query = session.createSQLQuery(sql);
		List<Object[]> rows = query.list();
		
		List<OrderInfo> orders= new ArrayList<>();
		for(Object[] row : rows){
			OrderInfo ord = new OrderInfo();
			ord.setId(row[0].toString());
			ord.setOrderDate(row[1].toString());
			/*String string = row[1].toString();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Date date;
			try {
				date = formatter.parse(string);
				System.out.println(date);				
				ord.setOrderDate(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			ord.setOrderNum(Integer.parseInt(row[2].toString()));
			ord.setAmount(Double.parseDouble(row[3].toString()));
			ord.setCustomerName(row[4].toString());
			ord.setCustomerAddress(row[5].toString());
			ord.setCustomerEmail(row[6].toString());			
			ord.setCustomerPhone(row[7].toString());			
			System.out.println(ord);
			orders.add(ord);
		}
		return orders;
	}

}

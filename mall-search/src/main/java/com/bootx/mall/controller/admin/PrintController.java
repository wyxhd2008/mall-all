
package com.bootx.mall.controller.admin;

import javax.inject.Inject;

import com.bootx.mall.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller - 打印
 * 
 * @author BOOTX Team
 * @version 6.1
 */
@Controller("adminPrintController")
@RequestMapping("/admin/print")
public class PrintController extends BaseController {

	@Inject
	private OrderService orderService;

	/**
	 * 订单打印
	 */
	@GetMapping("/order")
	public String order(Long id, ModelMap model) {
		model.addAttribute("order", orderService.find(id));
		return "admin/print/order";
	}

}
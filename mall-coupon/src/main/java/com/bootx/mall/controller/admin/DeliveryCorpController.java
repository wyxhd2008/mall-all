
package com.bootx.mall.controller.admin;

import javax.inject.Inject;

import com.bootx.mall.common.Pageable;
import com.bootx.mall.common.Results;
import com.bootx.mall.entity.DeliveryCorp;
import com.bootx.mall.service.DeliveryCorpService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller - 物流公司
 * 
 * @author BOOTX Team
 * @version 6.1
 */
@Controller("adminDeliveryCorpController")
@RequestMapping("/admin/delivery_corp")
public class DeliveryCorpController extends BaseController {

	@Inject
	private DeliveryCorpService deliveryCorpService;

	/**
	 * 添加
	 */
	@GetMapping("/add")
	public String add() {
		return "admin/delivery_corp/add";
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public ResponseEntity<?> save(DeliveryCorp deliveryCorp) {
		if (!isValid(deliveryCorp)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		deliveryCorp.setShippingMethods(null);
		deliveryCorpService.save(deliveryCorp);
		return Results.OK;
	}

	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(Long id, ModelMap model) {
		model.addAttribute("deliveryCorp", deliveryCorpService.find(id));
		return "admin/delivery_corp/edit";
	}

	/**
	 * 更新
	 */
	@PostMapping("/update")
	public ResponseEntity<?> update(DeliveryCorp deliveryCorp) {
		if (!isValid(deliveryCorp)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		deliveryCorpService.update(deliveryCorp, "shippingMethods");
		return Results.OK;
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(Pageable pageable, ModelMap model) {
		model.addAttribute("page", deliveryCorpService.findPage(pageable));
		return "admin/delivery_corp/list";
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public ResponseEntity<?> delete(Long[] ids) {
		deliveryCorpService.delete(ids);
		return Results.OK;
	}

}
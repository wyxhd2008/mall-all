
package com.bootx.mall.controller.member;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.bootx.mall.common.Results;
import com.bootx.mall.common.Setting;
import com.bootx.mall.entity.BaseEntity;
import com.bootx.mall.entity.Member;
import com.bootx.mall.entity.MemberAttribute;
import com.bootx.mall.entity.SocialUser;
import com.bootx.mall.security.UserAuthenticationToken;
import com.bootx.mall.util.SystemUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bootx.mall.service.DistributorService;
import com.bootx.mall.service.MemberAttributeService;
import com.bootx.mall.service.MemberRankService;
import com.bootx.mall.service.MemberService;
import com.bootx.mall.service.PluginService;
import com.bootx.mall.service.SocialUserService;
import com.bootx.mall.service.UserService;

/**
 * Controller - 会员注册
 * 
 * @author BOOTX Team
 * @version 6.1
 */
@Controller("memberRegisterController")
@RequestMapping("/member/register")
public class RegisterController extends BaseController {

	@Inject
	private PluginService pluginService;
	@Inject
	private UserService userService;
	@Inject
	private MemberService memberService;
	@Inject
	private DistributorService distributorService;
	@Inject
	private MemberRankService memberRankService;
	@Inject
	private MemberAttributeService memberAttributeService;
	@Inject
	private SocialUserService socialUserService;

	/**
	 * 检查用户名是否存在
	 */
	@GetMapping("/check_username")
	public @ResponseBody boolean checkUsername(String username) {
		return StringUtils.isNotEmpty(username) && !memberService.usernameExists(username);
	}

	/**
	 * 检查E-mail是否存在
	 */
	@GetMapping("/check_email")
	public @ResponseBody boolean checkEmail(String email) {
		return StringUtils.isNotEmpty(email) && !memberService.emailExists(email);
	}

	/**
	 * 检查手机是否存在
	 */
	@GetMapping("/check_mobile")
	public @ResponseBody boolean checkMobile(String mobile) {
		return StringUtils.isNotEmpty(mobile) && !memberService.mobileExists(mobile);
	}

	/**
	 * 注册页面
	 */
	@GetMapping
	public String index(Long socialUserId, String uniqueId, HttpServletRequest request, ModelMap model) {
		if (socialUserId != null && StringUtils.isNotEmpty(uniqueId)) {
			SocialUser socialUser = socialUserService.find(socialUserId);
			if (socialUser == null || socialUser.getUser() != null || !StringUtils.equals(socialUser.getUniqueId(), uniqueId)) {
				return UNPROCESSABLE_ENTITY_VIEW;
			}
			model.addAttribute("socialUserId", socialUserId);
			model.addAttribute("uniqueId", uniqueId);
		}
		model.addAttribute("genders", Member.Gender.values());
		model.addAttribute("loginPlugins", pluginService.getActiveLoginPlugins(request));
		return "member/register/index";
	}

	/**
	 * 注册提交
	 */
	@PostMapping("/submit")
	public ResponseEntity<?> submit(String username, String password, String email, String mobile, String spreadMemberUsername, HttpServletRequest request) {
		Setting setting = SystemUtils.getSetting();
		if (!ArrayUtils.contains(setting.getAllowedRegisterTypes(), Setting.RegisterType.MEMBER)) {
			return Results.unprocessableEntity("member.register.disabled");
		}
		if (!isValid(Member.class, "username", username, BaseEntity.Save.class) || !isValid(Member.class, "password", password, BaseEntity.Save.class) || !isValid(Member.class, "email", email, BaseEntity.Save.class) || !isValid(Member.class, "mobile", mobile, BaseEntity.Save.class)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (memberService.usernameExists(username)) {
			return Results.unprocessableEntity("member.register.usernameExist");
		}
		if (memberService.emailExists(email)) {
			return Results.unprocessableEntity("member.register.emailExist");
		}
		if (memberService.mobileExists(mobile)) {
			return Results.unprocessableEntity("member.register.mobileExist");
		}

		Member member = new Member();
		member.removeAttributeValue();
		for (MemberAttribute memberAttribute : memberAttributeService.findList(true, true)) {
			String[] values = request.getParameterValues("memberAttribute_" + memberAttribute.getId());
			if (!memberAttributeService.isValid(memberAttribute, values)) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			Object memberAttributeValue = memberAttributeService.toMemberAttributeValue(memberAttribute, values);
			member.setAttributeValue(memberAttribute, memberAttributeValue);
		}

		member.setUsername(username);
		member.setPassword(password);
		member.setEmail(email);
		member.setMobile(mobile);
		member.setPoint(0L);
		member.setBalance(BigDecimal.ZERO);
		member.setFrozenAmount(BigDecimal.ZERO);
		member.setAmount(BigDecimal.ZERO);
		member.setIsEnabled(true);
		member.setIsLocked(false);
		member.setLockDate(null);
		member.setLastLoginIp(request.getRemoteAddr());
		member.setLastLoginDate(new Date());
		member.setSafeKey(null);
		member.setMemberRank(memberRankService.findDefault());
		member.setDistributor(null);
		member.setCart(null);
		member.setOrders(null);
		member.setPaymentTransactions(null);
		member.setMemberDepositLogs(null);
		member.setCouponCodes(null);
		member.setReceivers(null);
		member.setReviews(null);
		member.setConsultations(null);
		member.setProductFavorites(null);
		member.setProductNotifies(null);
		member.setSocialUsers(null);
		member.setPointLogs(null);
		userService.register(member);
		userService.login(new UserAuthenticationToken(Member.class, username, password, false, request.getRemoteAddr()));
		Member spreadMember = memberService.findByUsername(spreadMemberUsername);
		if (spreadMember != null) {
			distributorService.create(member, spreadMember);
		}
		return Results.OK;
	}

}
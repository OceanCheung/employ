package com.ph.activiti.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ph.activiti.model.Constant;
import com.ph.activiti.model.ReturnValue;
import com.ph.activiti.model.User;
import com.ph.activiti.service.UserService;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

	private Logger logger = Logger.getLogger(UserController.class);

	@Autowired
	private UserService UserService;

	@RequestMapping(value = "/login", method = { RequestMethod.POST })
	public ReturnValue login(User user, HttpServletRequest request, HttpSession session) {
		logger.debug("用户信息：" + user);
		User userInfo = UserService.userLogin(user);
		if (userInfo != null) {
			session.setAttribute("userName", userInfo.getUserName());
			System.err.println("yonghu:" + request.getSession().getAttribute("userName"));
			return new ReturnValue(Constant.SUCCESS, "登录成功", userInfo);
		} else {
			return new ReturnValue(Constant.ERROR, "登录失败", null);
		}
	}

	@RequestMapping(value = "/getUserInfo", method = { RequestMethod.POST })
	public ReturnValue getUser(HttpServletRequest request, HttpSession session) {
		String userName = session.getAttribute("userName") + "";
		System.out.println("用户名：" + userName);
		return new ReturnValue(Constant.SUCCESS, "获取用户信息成功", userName);
	}

}

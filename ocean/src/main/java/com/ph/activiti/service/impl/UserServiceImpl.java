package com.ph.activiti.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ph.activiti.dao.UserDao;
import com.ph.activiti.model.User;
import com.ph.activiti.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserDao userDao;

	public User userLogin(User user) {
		return userDao.userLogin(user);
	}
}

package com.ucpaas.sms.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class BlankPathController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(BlankPathController.class);

	@RequestMapping
	public void index(HttpServletResponse response) throws IOException {
		try {
			response.sendRedirect("/login");
		} catch (IOException e) {
			logger.error("springmvc转发/login失败", e);
			throw e;
		}
	}

}

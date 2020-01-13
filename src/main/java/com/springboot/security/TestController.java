package com.springboot.security;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.security.controller.LoginController;
import com.springboot.security.dao.SimpleUserDAO;

@Controller
public class TestController {

	@Autowired SimpleUserDAO sud;

	@RequestMapping("/")
	public String home(Model model, HttpSession session) throws Exception {

		Object currentAuth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserDetails principal = null;

		if(!(currentAuth instanceof String)) {
			principal = (UserDetails) currentAuth;
			List<Map<String, ?>> userInfo = sud.getUserInfo(principal.getUsername());
			String food = (String) userInfo.get(0).get("food");
			model.addAttribute("food", food);
			LoginController lc = new LoginController();

			// 메인 화면에 네이버 연동 버튼 추가
			List<Map<String, String>> infoOAuth = sud.getOAuthInfoByProviderAndUsername("naver", principal.getUsername());
			if(infoOAuth.size() == 0) {

				String apiURL = lc.getNaverOAuthURI(session);
				model.addAttribute("naverApiURL", apiURL);
			}
		}

		return "home";	
	}

	@ResponseBody
	@RequestMapping("/test")
	public String test() {
		return "OK";
	}

	@ResponseBody
	@RequestMapping("/adminOnly")
	public String adminOnly(Authentication auth) {
		return "Secret Page!!" 
				+ "<br>my roles: "+ auth.getAuthorities().toString();
	}


	@ResponseBody
	@RequestMapping("/userOnly")
	public String userOnly(Authentication auth) {
		return "USER ONLY"
				+ "<br>my roles: "+ auth.getAuthorities().toString();
	}

	@ResponseBody
	@RequestMapping("/userOnly/{sub}")
	public String userOnlySub(Authentication auth, @PathVariable("sub") String sub) {
		return "USER ONLY SUB PAGE (" + sub + ")"
				+ "<br>my roles: "+ auth.getAuthorities().toString();
	}

	@ResponseBody
	@RequestMapping("/everybodyOK")
	public String everybodyOK() {
		return "EVERYBODY OK";
	}




}
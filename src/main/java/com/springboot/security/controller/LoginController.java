package com.springboot.security.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.security.dao.SimpleUserDAO;
import com.springboot.security.util.UrlBuilder;

@Controller
public class LoginController {

	@Autowired SimpleUserDAO sud;
	@Autowired
	private Environment env;

	@Value("${naver.client.id}")	
	private String CLIENT_ID; // naver 애플리케이션 클라이언트 아이디값";
	
	@Value("${naver.client.secret}")	
	private String CLI_SECRET; // naver 애플리케이션 클라이언트 시크릿값";
	
	@Value("${naver.client.redirect-uri}")	
	private String REDIRECT_URI;

	/**
	 * 로그인 페이지
	 * @param session
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/login")
	public String loginForm(HttpSession session, Model model) throws UnsupportedEncodingException {
		String apiURL = getNaverOAuthURI(session);
		model.addAttribute("naverApiURL", apiURL);
		if(session.getAttribute("todoAssign") != null && session.getAttribute("todoAssign").equals("naver")) {
			model.addAttribute("isTodoAssignNaver", true);
			session.setAttribute("todoAssign", null); // 연동 메뉴는 최초 접속시에만 활성화하고 다시 접속시 바로 비활성화
		}
		System.out.println("env: " + env.getProperty("naver.client.secret"));
		return "login-form";
	}

	/**
	 * 네이버 로그인을 했는데 연동이 안되어있고 로그인 상태가 아닌 경우 로그인을 했을 때 연동 여부를 묻는 페이지로 이동하는 메소드  
	 * @param session
	 * @param model
	 * @param whereFrom
	 * @return
	 */
	@GetMapping("/login/oauth/naver")
	public String loginOAuth(HttpSession session, Model model, String whereFrom) {
		if(whereFrom.equals("naverOAuth-s")) {
			model.addAttribute("uniqueIdOfNaver", session.getAttribute("uniqueIdOfNaver"));
			return "assign-naver";
		} else {
			return "redirect:/";
		}
	}

	/**
	 * 로그인 폼을 거치지 않고 바로 로그인
	 * @param username
	 * @return
	 */
	@RequestMapping("/loginWithoutForm/{username}")
	public String loginWithoutForm(@PathVariable(value="username") String username) {

		String roleStr = "ROLE_" + sud.getRolesByUsername(username).toUpperCase();

		List<GrantedAuthority> roles = new ArrayList<>(1);
		//String roleStr = username.equals("admin") ? "ROLE_ADMIN" : "ROLE_GUEST";
		roles.add(new SimpleGrantedAuthority(roleStr));

		User user = new User(username, "", roles);

		Authentication auth = new UsernamePasswordAuthenticationToken(user, null, roles);
		SecurityContextHolder.getContext().setAuthentication(auth);
		return "redirect:/";
	}

	/**
	 * 현재 로그인한 사용자 정보 가져오기
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getUserInfo")
	public List<Map<String, ?>> getUserInfo(){
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			return sud.getUserInfo(principal.getUsername());
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	/**
	 * 로그인 화면이 있는 페이지 컨트롤
	 * @param session
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UnknownHostException 
	 */
	@RequestMapping("/naver")
	public String testNaver(HttpSession session, Model model) throws UnsupportedEncodingException, UnknownHostException {


		String apiURL = getNaverOAuthURI(session);
		model.addAttribute("apiURL", apiURL);
		return "test-naver";
	}

	/**
	 * getNaverOAuthURI (+ 세션의 "state" 속성에 값 부여)
	 * @param session
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getNaverOAuthURI(HttpSession session) throws UnsupportedEncodingException {
		String redirectURI = URLEncoder.encode(REDIRECT_URI, "UTF-8");

		SecureRandom random = new SecureRandom();
		String state = new BigInteger(130, random).toString();

		UrlBuilder ub = new UrlBuilder("https://nid.naver.com/oauth2.0/authorize");
		ub
		.add("response_type", "code")
		.add("client_id", CLIENT_ID)
		.add("redirect_uri", redirectURI)
		.add("state", state);
		String apiURL = ub.toString();
		session.setAttribute("state", state);

		return apiURL;
	}

	/**
	 * 콜백 페이지 컨트롤러
	 * @param session
	 * @param request
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/naver/callback1")
	public String naverCallback1(HttpSession session, HttpServletRequest request, Model model) throws IOException, ParseException {

		String code = request.getParameter("code");
		String state = request.getParameter("state");
		String redirectURI = URLEncoder.encode(REDIRECT_URI, "UTF-8");

		UrlBuilder ub = new UrlBuilder("https://nid.naver.com/oauth2.0/token");
		ub
		.add("grant_type", "authorization_code")
		.add("client_id", CLIENT_ID)
		.add("client_secret", CLI_SECRET)
		.add("redirect_uri", redirectURI)
		.add("code", code)
		.add("state", state);
		System.out.println(ub);

		String apiURL = ub.toString();

		String res = requestToServer(apiURL);

		if(res != null && !res.equals("")) {
			Map<String, Object> parsedJson = new JSONParser(res).parseObject();
			if(parsedJson.get("access_token") != null) {

				// 
				String infoStr = getProfileFromNaver(parsedJson.get("access_token").toString());
				Map<String, Object> infoMap = new JSONParser(infoStr).parseObject();
				if(infoMap.get("message").equals("success")) {
					Map<String, Object> infoResp = (Map<String, Object>) infoMap.get("response");
					String uniqueId = infoResp.get("id").toString();
					System.out.println(uniqueId);
					List<Map<String, String>> infoOAuth = sud.getOAuthInfoByProviderAndUniqueId("naver", uniqueId);
					if(infoOAuth.size() == 1) {
						System.out.println(infoOAuth);
						// 네아로 연동이 되어 있다면 연동 정보를 통해 로그인 처리
						// - 현재 로그인한 계정과 네아로 연결된 로그인 계정이 다른 경우, 현재 계정을 로그아웃하고 그 연결된 계정으로 재로그인
						loginWithoutForm(infoOAuth.get(0).get("username"));
						model.addAttribute("isConnectedToNaver", true);
					} else {
						System.out.println("네이버 연동 정보 없음");
						// 로그인이 되어 있다면 기존 아이디를 네아로와 연동할 것인지 확인 여부를 물음
						model.addAttribute("isConnectedToNaver", false);
						model.addAttribute("uniqueIdOfNaver", uniqueId);

						// 아로 연동 안되어 있고 로그인이 되어 있지 않다면, 로그인 창(+회원가입 링크)으로 리다이렉트
						// – 기존에 회원아이디로 로그인했다면 네아로 연동 과정을 계속 진행
						// – 회원가입이 되지 않은 상태이며 이 회원 가입링크를 통해 가입한 경우 가입 완료하자마자 네아로 연동 과정 진행
						String authName = SecurityContextHolder.getContext().getAuthentication().getName();

						if(authName.equals("anonymousUser")) {
							session.setAttribute("todoAssign", "naver");
							session.setAttribute("uniqueIdOfNaver", uniqueId);
							return "redirect:/login";
						}
					}
				}

				session.setAttribute("currentNaverUser", res);
				session.setAttribute("currentAT", parsedJson.get("access_token"));
				session.setAttribute("currentRT", parsedJson.get("refresh_token"));

				model.addAttribute("res", res);
			} else {
				model.addAttribute("res", "Login failed!");
			}
			System.out.println(parsedJson);
		} else {
			model.addAttribute("res", "Login failed!");
		}
		return "test-naver-callback";
	}

	/**
	 * 네이버 계정을 users_oauth 테이블에 할당
	 * @param session
	 * @param auth
	 * @param model
	 * @param uniqueId
	 * @return
	 */
	@PostMapping("/oauth/assign/naver")
	public String addRowToOAuthTableForNaver(HttpSession session, Authentication auth, Model model, String uniqueId, String username) {
		username = auth != null && !auth.getName().equals("anonymousUser") ? auth.getName() : username;
		String provider = "naver";
		List<Map<String, String>> infoOAuth = sud.getOAuthInfoByProviderAndUniqueId(provider, uniqueId);
		int resultCode = 0;
		if(infoOAuth.size() == 0) {
			Map<String, String> aRow = new HashMap<>();
			aRow.put("username", username);
			aRow.put("provider", provider);
			aRow.put("unique_id", uniqueId);
			resultCode = sud.insertAnUserOAuth(aRow);
			if(resultCode <= 0) {
				session.setAttribute("currentNaverUser", null);
			}
			model.addAttribute("task", "assign-naver");
			model.addAttribute("resultCode", resultCode);
		}
		return "redirect:/";
	}

	/**
	 * 토큰 갱신 요청 페이지 컨트롤러
	 * @param session
	 * @param request
	 * @param model
	 * @param refreshToken
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@RequestMapping("/naver/refreshToken")
	public String refreshToken(HttpSession session, HttpServletRequest request, Model model, String refreshToken) throws IOException, ParseException {

		UrlBuilder ub = new UrlBuilder("https://nid.naver.com/oauth2.0/token");
		ub
		.add("grant_type", "refresh_token")
		.add("client_id", CLIENT_ID)
		.add("client_secret", CLI_SECRET)
		.add("refresh_token", refreshToken);

		String apiURL = ub.toString();
		System.out.println("apiURL=" + apiURL);

		String res = requestToServer(apiURL);
		model.addAttribute("res", res);
		session.invalidate();
		return "test-naver-callback";
	}

	/**
	 * 토큰 삭제 컨트롤러
	 * @param session
	 * @param request
	 * @param model
	 * @param accessToken
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/naver/deleteToken")
	public String deleteToken(HttpSession session, HttpServletRequest request, Model model, String accessToken) throws IOException {

		UrlBuilder ub = new UrlBuilder("https://nid.naver.com/oauth2.0/token");
		ub
		.add("grant_type", "delete")
		.add("client_id", CLIENT_ID)
		.add("client_secret", CLI_SECRET)
		.add("access_token", accessToken)
		.add("service_provider", "NAVER");

		String apiURL = ub.toString();

		System.out.println("apiURL=" + apiURL);

		String res = requestToServer(apiURL);
		model.addAttribute("res", res);
		session.invalidate();
		return "test-naver-callback";
	}

	/**
	 * 액세스 토큰으로 네이버에서 프로필 받기
	 * @param accessToken
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping("/naver/getProfile")
	public String getProfileFromNaver(String accessToken) throws IOException {

		// 네이버 로그인 접근 토큰;
		String apiURL = "https://openapi.naver.com/v1/nid/me";
		String headerStr = "Bearer " + accessToken; // Bearer 다음에 공백 추가
		String res = requestToServer(apiURL, headerStr);
		return res;
	}

	/**
	 * 세션 무효화(로그아웃)
	 * @param session
	 * @return
	 */
	@RequestMapping("/naver/invalidate")
	public String invalidateSession(HttpSession session) {
		session.invalidate();
		return "redirect:/naver";
	}

	/**
	 * 서버 통신 메소드
	 * @param apiURL
	 * @return
	 * @throws IOException
	 */
	private String requestToServer(String apiURL) throws IOException {
		return requestToServer(apiURL, "");
	}

	/**
	 * 서버 통신 메소드
	 * @param apiURL
	 * @param headerStr
	 * @return
	 * @throws IOException
	 */
	private String requestToServer(String apiURL, String headerStr) throws IOException {
		URL url = new URL(apiURL);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");

		System.out.println("header Str: " + headerStr);
		if(headerStr != null && !headerStr.equals("") ) {
			con.setRequestProperty("Authorization", headerStr);
		}

		int responseCode = con.getResponseCode();
		BufferedReader br;

		System.out.println("responseCode="+responseCode);

		if(responseCode == 200) { // 정상 호출
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else {  // 에러 발생
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer res = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			res.append(inputLine);
		}
		br.close();
		if(responseCode==200) {
			return res.toString();
		} else {
			return null;
		}

	}



}

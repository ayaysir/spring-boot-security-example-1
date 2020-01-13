package com.springboot.security.util;

import java.util.ArrayList;
import java.util.List;

public class UrlBuilder {

	private List<String> keyValuePairs = new ArrayList<>();
	private String baseUrl, makeupUrl;
	
	public UrlBuilder(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public UrlBuilder add(String key, String value) {
		keyValuePairs.add(key + "=" + value);
		return this;
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	public String toString() {
		
		//		String apiURL;
		//		apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&";
		//		apiURL += "client_id=" + CLIENT_ID;
		//		apiURL += "&client_secret=" + CLI_SECRET;
		//		apiURL += "&redirect_uri=" + redirectURI;
		//		apiURL += "&code=" + code;
		//		apiURL += "&state=" + state;
		//		System.out.println("apiURL=" + apiURL);

		if(keyValuePairs.size() >= 1) {
			makeupUrl = baseUrl + "?";
			for(String aPair : keyValuePairs) {
				makeupUrl += aPair + "&"; 
			}
			if(makeupUrl.charAt(makeupUrl.length() - 1) == '&') {
				makeupUrl = makeupUrl.substring(0, makeupUrl.length() - 2);
			}
			System.out.println(makeupUrl);
		} else {
			makeupUrl = baseUrl;
		}

		return makeupUrl;
	}

}


package com.springboot.security.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.springboot.security.dao.SimpleBoardDAO;

@Controller
public class SimpleBoardController {
	
	@Autowired SimpleBoardDAO sbd;
	
	@RequestMapping("/board")
	public String board(Model model) {
		List<Map<String, Object>> list = sbd.getBoardList();
		// System.out.println(list);
		model.addAttribute("list", list);
		return "simple-board";
	}
	
	@RequestMapping(value="/board/proc/write", method=RequestMethod.POST)
	public String writeAnArticle(Authentication auth, String title, String content) {
		int result = 0;
		if(title != null && !title.equals("") && content != null && !content.equals("")) {
			Map<String, Object> article = new HashMap<>();
			article.put("username", auth.getName());
			article.put("title", title);
			article.put("content", content);
			result = sbd.insertAnArticle(article);

		} else {
			System.err.println("Do not write blank article!");
		}
		
		return "redirect:/board?writeResult=" + result;
	}
	
	@RequestMapping("/board/read/{articleId}")
	public String readAnArticle(Model model, @PathVariable("articleId") int articleId) {
		
		Map<String, Object> article = sbd.getAnArticle(articleId);
		model.addAttribute("article", article);
		return "simple-board-view-content";
	}
	
	@RequestMapping("/board/delete/{articleId}")
	public String deleteAnArticleView(Authentication auth, Model model, @PathVariable("articleId") int articleId) {
		
		model.addAttribute("articleId", articleId);
		return "simple-board-view-delete";
	}
	
	@RequestMapping(value="/board/proc/delete", method=RequestMethod.POST)
	public String deleteAnArticleProc(Authentication auth, Model model, int articleId) {
		
		int result = sbd.deleteAnArticle(articleId, auth.getName());
		
		return "redirect:/board?deleteResult=" + result;
	}
	
	@RequestMapping("/board/update/{articleId}")
	public String updateAnArticleView(Authentication auth, Model model, @PathVariable("articleId") int articleId) {
		Map<String, Object> article = sbd.getAnArticle(articleId);
		model.addAttribute("article", article);
		
		return "simple-board-view-modify";
	}
	
	@RequestMapping(value="/board/proc/update", method=RequestMethod.POST)
	public String updateAnArticleProc(Authentication auth, Model model, int articleId, String title, String content) {
		
		int result = 0;
		if(title != null && !title.equals("") && content != null && !content.equals("")) {
			Map<String, Object> article = new HashMap<>();
			article.put("articleId", articleId);
			article.put("username", auth.getName());
			article.put("title", title);
			article.put("content", content);
			result = sbd.updateAnArticle(article);

		} else {
			System.err.println("Do not write blank article!");
		}
		
		return "redirect:/board?writeResult=" + result;
	}
}

package com.springboot.security.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SimpleBoardDAO {
	@Autowired JdbcTemplate jt;
	
	public int insertAnArticle(Map<String, Object> article) {
		String sql = "insert into simple_board values(0, ?, sysdate(), ?, ?)";
		
		return jt.update(sql, 
				article.get("username"), 
				article.get("title"), 
				article.get("content"));
		
	}
	
	public List<Map<String, Object>> getBoardList(){
		String sql = "select * from simple_board order by write_date desc";
		
		return jt.query(sql, (rs, rowNum) -> {
			Map<String, Object> anArticle = new HashMap<>();
			anArticle.put("seq", rs.getString(1));
			anArticle.put("username", rs.getString(2));
			anArticle.put("writeDate", rs.getString(3));
			anArticle.put("title", rs.getString(4));
			anArticle.put("content", rs.getString(5));
			return anArticle;
		});
	}
	
	public Map<String, Object> getAnArticle(int articleId){
		String sql = "select * from simple_board where seq=?";
		
		return jt.queryForObject(sql, new Object[] {articleId}, (rs, rowNum) -> {
			Map<String, Object> anArticle = new HashMap<>();
			anArticle.put("seq", rs.getString(1));
			anArticle.put("username", rs.getString(2));
			anArticle.put("writeDate", rs.getString(3));
			anArticle.put("title", rs.getString(4));
			anArticle.put("content", rs.getString(5));
			return anArticle; 
		});
	}
	
	public int deleteAnArticle(int articleId, String username) {
		String sql = "delete from simple_board where seq = ? and username = ?";
		
		return jt.update(sql, articleId, username);
	}
	
	public int updateAnArticle(Map<String, Object> article) {
		String sql = "update simple_board set title = ?, content = ? where seq = ? and username = ?";
		
		return jt.update(sql, 
				article.get("title"), 
				article.get("content"), 
				article.get("articleId"), 
				article.get("username"));
		
	}

}

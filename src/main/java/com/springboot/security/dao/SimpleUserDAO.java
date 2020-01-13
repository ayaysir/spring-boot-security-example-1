package com.springboot.security.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SimpleUserDAO {
	@Autowired JdbcTemplate jt;

	public List<Map<String, ?>> getUserInfo(String username) {

		return jt.query("select * from simple_users where username=?", new Object[] {username} , (rs, rowNum) -> {
			Map<String, Object> anUser = new HashMap<>();
			anUser.put("username", rs.getString(2));
			anUser.put("password", rs.getString(3));
			anUser.put("role", rs.getString(4));
			anUser.put("food", rs.getString(5));
			return anUser;
		});
	}
	
	public int insertUserInfo(Map<String, String> user) {
		String sql = "insert into "
				+ "simple_users(iduser, username, password, role, food, email, real_name) "
				+ "values(0, ?, ?, 'GUEST', ?, ?, ?)";
		
		return jt.update(sql, 
				user.get("user-id"),
				user.get("user-password"),
				user.get("user-food"),
				user.get("user-email"),
				user.get("user-real-name")
			);
	}
	

	public String getRolesByUsername(String username) {
		return jt.queryForObject("select role from simple_users where username=?", new Object[] {username}, (rs, rowNum) -> {
			return rs.getString(1);
		});
	}
	
	/**
	 * 
	 * @param provider
	 * @param uniqueId
	 * @return
	 */
	public List<Map<String, String>> getOAuthInfoByProviderAndUniqueId(String provider, String uniqueId) {
		return jt.query("select * from users_oauth where provider=? and unique_id=?", 
				new Object[] { provider, uniqueId }, (rs, rowNum) -> {
					
			Map<String, String> aRow = new HashMap<>();
			aRow.put("seq", rs.getString("seq"));
			aRow.put("username", rs.getString("username"));
			aRow.put("provider", rs.getString("provider"));
			aRow.put("uniqueId", rs.getString("unique_id"));
			aRow.put("regDate", rs.getString("reg_date"));
			aRow.put("lastDate", rs.getString("last_date"));
			return aRow;
			
		});
	}
	
	public List<Map<String, String>> getOAuthInfoByProviderAndUsername(String provider, String username) {
		return jt.query("select * from users_oauth where provider=? and username=?", 
				new Object[] { provider, username }, (rs, rowNum) -> {
					
			Map<String, String> aRow = new HashMap<>();
			aRow.put("seq", rs.getString("seq"));
			aRow.put("username", rs.getString("username"));
			aRow.put("provider", rs.getString("provider"));
			aRow.put("uniqueId", rs.getString("unique_id"));
			aRow.put("regDate", rs.getString("reg_date"));
			aRow.put("lastDate", rs.getString("last_date"));
			return aRow;
			
		});
	}
	
	/**
	 * 
	 * @param aRow
	 * @return
	 */
	public int insertAnUserOAuth(Map<String, String> aRow) {
		String sql = "insert into users_oauth"
				+ "(seq, username, provider, unique_id, reg_date, last_date) "
				+ "values(0, ?, ?, ?, sysdate(), sysdate())";
		
		return jt.update(sql, 
				aRow.get("username"),
				aRow.get("provider"),
				aRow.get("unique_id")
			);
		
	}

}

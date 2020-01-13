package com.springboot.security.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityDAO {
	@Autowired JdbcTemplate jt;
	
	public List<Map<String, Object>> getAuthReq() {
		return jt.query("select * from security_authreq", (rs, rowNum) -> {
			Map<String, Object> aRow = new HashMap<>();
			aRow.put("id", rs.getInt(1));
			aRow.put("url", rs.getString(2));
			aRow.put("hasAuthority", rs.getString(3));
			aRow.put("date", rs.getString(4));
			return aRow;
		});
	}
	

}

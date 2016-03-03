package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * register a identity
 *
 */
public class RegisterInstruction extends Instruction {
	private String identity = null;
	private String password = null;

	public RegisterInstruction() {
		super();
	}

	public RegisterInstruction(String identity, String password) {
		super();
		this.identity = identity;
		this.password = password;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String Type() {
		return Instruction.REGISTER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		jo.put("identity", identity);
		jo.put("password", password);
		return jo.toJSONString();
	}

	@Override
	public void FromJSON(String jst) {
		JSONObject jo = null;
		try {
			jo = (JSONObject) parser.parse(jst);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (jo != null) {
			identity = (String) jo.get("identity");
			password = (String) jo.get("password");
		}
	}

}

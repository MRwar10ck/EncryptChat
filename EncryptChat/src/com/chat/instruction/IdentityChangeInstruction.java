package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to ask for changing identity
 */
public class IdentityChangeInstruction extends Instruction {
	private String identity = "";

	public IdentityChangeInstruction() {
		super();
	}

	public IdentityChangeInstruction(String identity) {
		super();
		this.identity = identity;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	@Override
	public String Type() {
		return Instruction.IDENTITYCHANGE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", Type());
		jsonObject.put("identity", identity);
		return jsonObject.toJSONString();
	}

	@Override
	public void FromJSON(String jst) {
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(jst);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (jsonObject != null) {
			identity = (String) jsonObject.get("identity");
		}
	}

}

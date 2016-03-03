package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * Th instruction is used to inform client the new identity
 */
public class NewIdentityInstruction extends Instruction {
	private String former = "";
	private String identity = "";

	public NewIdentityInstruction() {
		super();
	}

	public NewIdentityInstruction(String former, String identity) {
		super();
		this.former = former;
		this.identity = identity;
	}

	public String getFormer() {
		return former;
	}

	public void setFormer(String former) {
		this.former = former;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	@Override
	public String Type() {
		return Instruction.NEWIDENTITY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", Type());
		jsonObject.put("former", former);
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
			former = (String) jsonObject.get("former");
			identity = (String) jsonObject.get("identity");
		}
	}

}

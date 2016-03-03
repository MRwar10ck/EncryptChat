package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction conveys the message
 *
 *
 */
public class MessageInstruction extends Instruction {
	private String identity = "";
	private String content = "";

	public MessageInstruction() {
		super();
	}

	public MessageInstruction(String content) {
		super();
		this.content = content;
	}

	public MessageInstruction(String identity, String content) {
		super();
		this.identity = identity;
		this.content = content;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String Type() {
		// TODO Auto-generated method stub
		return Instruction.MESSAGE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		jo.put("identity", identity);
		jo.put("content", content);
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
			content = (String) jo.get("content");
		}
	}

}

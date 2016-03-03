package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * the instruction is used to inform quit message each other
 */
public class QuitInstruction extends Instruction {

	@Override
	public String Type() {
		return Instruction.QUIT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
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

		}
	}

}

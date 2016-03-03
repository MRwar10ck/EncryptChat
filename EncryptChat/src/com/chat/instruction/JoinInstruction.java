package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to ask for joining certain chatRoom
 */
public class JoinInstruction extends Instruction {
	private String roomID = "";

	public JoinInstruction() {
		super();
	}

	public JoinInstruction(String roomID) {
		super();
		this.roomID = roomID;
	}

	public String getRoomID() {
		return roomID;
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
	}

	@Override
	public String Type() {
		return Instruction.JOIN;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", Type());
		jsonObject.put("roomid", roomID);
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
			roomID = (String) jsonObject.get("roomid");
		}
	}

}

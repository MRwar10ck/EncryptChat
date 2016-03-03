package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to communicate for information of chat room
 */
public class WhoInstruction extends Instruction {
	private String roomID = "";

	public WhoInstruction() {
		super();
	}

	public WhoInstruction(String roomID) {
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
		// TODO Auto-generated method stub
		return Instruction.WHO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		jo.put("roomid", roomID);
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
			roomID = (String) jo.get("roomid");
		}
	}

}

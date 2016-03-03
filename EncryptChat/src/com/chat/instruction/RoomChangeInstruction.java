package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to ask change chatRoom
 */
public class RoomChangeInstruction extends Instruction {
	private String identity = "";
	private String formerRoom = "";
	private String nowRoom = "";

	public RoomChangeInstruction() {
		super();
	}

	public RoomChangeInstruction(String identity, String formerRoom, String nowRoom) {
		super();
		this.identity = identity;
		this.formerRoom = formerRoom;
		this.nowRoom = nowRoom;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getFormerRoom() {
		return formerRoom;
	}

	public void setFormerRoom(String formerRoom) {
		this.formerRoom = formerRoom;
	}

	public String getNowRoom() {
		return nowRoom;
	}

	public void setNowRoom(String nowRoom) {
		this.nowRoom = nowRoom;
	}

	@Override
	public String Type() {
		return Instruction.ROOMCHANGE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", Type());
		jsonObject.put("identity", identity);
		jsonObject.put("former", formerRoom);
		jsonObject.put("roomid", nowRoom);
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
			formerRoom = (String) jsonObject.get("former");
			nowRoom = (String) jsonObject.get("roomid");
		}
	}

}

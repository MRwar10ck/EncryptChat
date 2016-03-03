package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to kick some user from some chatRoom
 */
public class KickInstruction extends Instruction {
	private String roomID = "";
	private long time;
	private String identity;

	public KickInstruction() {
		super();
	}

	public KickInstruction(String roomID, long time, String identity) {
		super();
		this.roomID = roomID;
		this.time = time;
		this.identity = identity;
	}

	public String getRoomID() {
		return roomID;
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	@Override
	public String Type() {
		// TODO Auto-generated method stub
		return Instruction.KICK;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		jo.put("roomid", roomID);
		jo.put("time", time);
		jo.put("identity", identity);
		return jo.toJSONString();
	}

	@Override
	public void FromJSON(String jst) {
		JSONObject jo = null;
		try {
			jo = (JSONObject) parser.parse(jst);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (jo != null) {
			roomID = (String) jo.get("roomid");
			time = (long) jo.get("time");
			identity = (String) jo.get("identity");
		}
	}

}

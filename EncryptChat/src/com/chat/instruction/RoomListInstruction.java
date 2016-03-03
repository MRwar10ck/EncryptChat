package com.chat.instruction;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to communicate for chatRooms in chatServer
 */
public class RoomListInstruction extends Instruction {
	private HashMap<String, Long> rooms = null;

	public RoomListInstruction() {
		super();
	}

	public RoomListInstruction(HashMap<String, Long> rooms) {
		super();
		this.rooms = rooms;
	}

	public HashMap<String, Long> getRooms() {
		return rooms;
	}

	public void setRooms(HashMap<String, Long> rooms) {
		this.rooms = rooms;
	}

	@Override
	public String Type() {
		// TODO Auto-generated method stub
		return Instruction.ROOMLIST;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		JSONArray ja = new JSONArray();
		Set<Entry<String, Long>> entries = this.rooms.entrySet();
		JSONObject joTemp = null;
		for (Entry<String, Long> entry : entries) {
			joTemp = new JSONObject();
			joTemp.put("roomid", entry.getKey());
			joTemp.put("count", entry.getValue());
			ja.add(joTemp);
		}
		jo.put("rooms", ja);
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
		rooms = new HashMap<String, Long>();
		JSONArray ja = (JSONArray) jo.get("rooms");
		JSONObject joTemp = null;
		for (int i = 0; i < ja.size(); i++) {
			joTemp = (JSONObject) ja.get(i);
			rooms.put((String) joTemp.get("roomid"), (Long) joTemp.get("count"));
		}
	}

}

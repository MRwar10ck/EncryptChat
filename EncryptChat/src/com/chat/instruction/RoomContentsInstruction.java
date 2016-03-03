package com.chat.instruction;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The instruction is used to convey the information of chatRoom
 */
public class RoomContentsInstruction extends Instruction {
	private String roomID = "";
	private ArrayList<String> identities = null;
	private String owner = "";

	public RoomContentsInstruction() {
		super();
	}

	public RoomContentsInstruction(String roomID, ArrayList<String> identities, String owner) {
		super();
		this.roomID = roomID;
		this.identities = identities;
		this.owner = owner;
	}

	public String getRoomID() {
		return roomID;
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
	}

	public ArrayList<String> getIdentities() {
		return identities;
	}

	public void setIdentities(ArrayList<String> identities) {
		this.identities = identities;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public String Type() {
		// TODO Auto-generated method stub
		return Instruction.ROOMCONTENTS;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		jo.put("roomid", roomID);
		JSONArray ja = new JSONArray();
		for (String identity : identities) {
			ja.add(identity);
		}
		jo.put("identities", ja);
		jo.put("owner", owner);
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
			identities = new ArrayList<>();
			JSONArray array = (JSONArray) jo.get("identities");
			for (int i = 0; i < array.size(); i++) {
				identities.add((String) array.get(i));
			}
			owner = (String) jo.get("owner");
		}
	}

}

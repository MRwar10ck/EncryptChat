package com.chat.communication;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Message {
	private String encryptInstructionJson = null;
	private String hash = null;

	public Message() {
	}

	public Message(String encryptInstructionJson, String hash) {
		this.encryptInstructionJson = encryptInstructionJson;
		this.hash = hash;
	}

	@SuppressWarnings("unchecked")
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put(CommunicationUtil.INSTATRUCTIONJSON, encryptInstructionJson);
		jo.put(CommunicationUtil.HASH, hash);
		return jo.toJSONString();
	}

	public static Message FromJSON(String jst) {
		JSONObject jo = null;
		JSONParser jsonParser = new JSONParser();
		try {
			jo = (JSONObject) jsonParser.parse(jst);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (jo != null) {
			Message message = new Message();
			message.encryptInstructionJson = (String) jo.get(CommunicationUtil.INSTATRUCTIONJSON);
			message.hash = (String) jo.get(CommunicationUtil.HASH);
			return message;
		}
		return null;
	}

	public String getEncryptInstructionJson() {
		return encryptInstructionJson;
	}

	public void setEncryptInstructionJson(String encryptInstructionJson) {
		this.encryptInstructionJson = encryptInstructionJson;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}

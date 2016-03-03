package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class AsymeticalInstruction extends Instruction {
	private byte[] publicKey = null;

	public AsymeticalInstruction() {
		super();
	}

	public AsymeticalInstruction(byte[] publicKey) {
		super();
		this.publicKey = publicKey;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String Type() {
		// TODO Auto-generated method stub
		return Instruction.ASYMETRICAL;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String ToJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type", Type());
		jo.put("publicKey", publicKey);
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
			publicKey = (byte[]) jo.get("publicKey");
		}
	}
}

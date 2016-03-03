package com.chat.instruction;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * The factory tool to parse some Instruction to some XXXXInstruction.
 */
public class InstructionFactory {

	private static final JSONParser parser = new JSONParser();

	public static Instruction FromJSON(String jst) {
		JSONObject obj;
		try {
			obj = (JSONObject) parser.parse(jst);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		if (obj != null) {
			Instruction inst = null;
			if (obj.get("type").equals(Instruction.NEWIDENTITY))
				inst = new NewIdentityInstruction();
			else if (obj.get("type").equals(Instruction.IDENTITYCHANGE))
				inst = new IdentityChangeInstruction();
			else if (obj.get("type").equals(Instruction.JOIN))
				inst = new JoinInstruction();
			else if (obj.get("type").equals(Instruction.ROOMCHANGE))
				inst = new RoomChangeInstruction();
			else if (obj.get("type").equals(Instruction.ROOMCONTENTS))
				inst = new RoomContentsInstruction();
			else if (obj.get("type").equals(Instruction.WHO))
				inst = new WhoInstruction();
			else if (obj.get("type").equals(Instruction.ROOMLIST))
				inst = new RoomListInstruction();
			else if (obj.get("type").equals(Instruction.LIST))
				inst = new ListInstruction();
			else if (obj.get("type").equals(Instruction.CREATEROOM))
				inst = new CreateRoomInstruction();
			else if (obj.get("type").equals(Instruction.KICK))
				inst = new KickInstruction();
			else if (obj.get("type").equals(Instruction.DELETE))
				inst = new DeleteInstruction();
			else if (obj.get("type").equals(Instruction.MESSAGE))
				inst = new MessageInstruction();
			else if (obj.get("type").equals(Instruction.QUIT))
				inst = new QuitInstruction();
			else if (obj.get("type").equals(Instruction.ERROR))
				inst = new ErrorInstruction();
			else if(obj.get("type").equals(Instruction.ASYMETRICAL))
				inst = new AsymeticalInstruction();
			else if(obj.get("type").equals(Instruction.LOGIN))
				inst = new LogInInstruction();
			else if(obj.get("type").equals(Instruction.REGISTER))
				inst = new RegisterInstruction();
			inst.FromJSON(jst);
			return inst;
		} else
			return null;
	}
}

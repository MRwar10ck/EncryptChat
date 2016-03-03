package com.chat.instruction;

import org.json.simple.parser.JSONParser;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * All instructions have a Type which is a String name. All instructions can
 * produce a JSON String for network communication. Use the InstructionFactory
 * class to convert a JSON String back to an Instruction class.
 */

abstract public class Instruction {
	public static final String NEWIDENTITY = "newidentity";
	public static final String IDENTITYCHANGE = "identitychange";
	public static final String JOIN = "join";
	public static final String ROOMCHANGE = "roomchange";
	public static final String ROOMCONTENTS = "roomcontents";
	public static final String WHO = "who";
	public static final String ROOMLIST = "roomlist";
	public static final String LIST = "list";
	public static final String CREATEROOM = "createroom";
	public static final String KICK = "kick";
	public static final String DELETE = "delete";
	public static final String MESSAGE = "message";
	public static final String QUIT = "quit";
	public static final String ERROR = "error";
	public static final String ASYMETRICAL = "asymetrical";
	public static final String LOGIN = "login";
	public static final String REGISTER = "register";
	protected static final JSONParser parser = new JSONParser();

	abstract public String Type();

	abstract public String ToJSON();

	abstract public void FromJSON(String jst);
}

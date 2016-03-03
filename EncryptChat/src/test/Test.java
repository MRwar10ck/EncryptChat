package test;

import java.util.regex.Pattern;

import com.chat.chatserver.ChatServer;
import com.chat.chatserver.User;
import com.chat.instruction.ErrorInstruction;
import com.chat.instruction.Instruction;

public class Test {

	public static void main(String[] args) throws Exception {

		System.out.println(Pattern.matches("guest\\d+", "guest123012"));

		ErrorInstruction instruction = null;
		ChatServer chatServer = null;
		User user = null;
		if (!instruction.Type().equals(Instruction.QUIT) && !chatServer.isLogIn(user.getUserid())
				&& !instruction.Type().equals(Instruction.LOGIN) && !instruction.Type().equals(Instruction.REGISTER)) {
			chatServer.sendInstructionToSomeOne(new ErrorInstruction("please log in, firstly.").ToJSON(), user);
		} else {
		}
	}

}

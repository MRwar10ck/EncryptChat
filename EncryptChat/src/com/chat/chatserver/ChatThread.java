package com.chat.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;

import com.chat.communication.Message;
import com.chat.instruction.ErrorInstruction;
import com.chat.instruction.Instruction;
import com.chat.instruction.InstructionFactory;
import com.chat.instruction.MessageInstruction;
import com.chat.instruction.QuitInstruction;
import com.chat.instruction.WhoInstruction;
import com.chat.security.HashUtil;
import com.chat.security.SymmetricalKeyUtil;

/**
 * Thread is used to process the information from chat client
 *
 */
class ChatThread extends Thread {
	private Socket socket = null;
	// inputStream of this thread
	private BufferedReader br = null;
	// the client identify of this thread
	private User user = null;
	private ChatServer chatServer;

	private boolean isNoQuitFlag = true;

	public ChatThread(BufferedReader br, Socket socket, User user, ChatServer chatServer) {
		this.socket = socket;
		this.br = br;
		this.user = user;
		this.chatServer = chatServer;
	}

	public String getInstuctionFromMessage(String stringTemp) throws Exception {
		Message message = Message.FromJSON(stringTemp);
		SymmetricalKeyUtil symmetricalKeyUtil = user.getSymmetricalKeyUtil();
		String instructionJson = symmetricalKeyUtil
				.decryptText(Base64.decodeBase64(message.getEncryptInstructionJson()));
		// prevent you from attack
		if (!HashUtil.isUnchanged(instructionJson, message.getHash())) {
			System.out.println("There is someone attack you.");
			isNoQuitFlag = false;
			return null;
		}
		return instructionJson;
	}

	@Override
	public void run() {
		try {
			String stringLine = null;
			while (isNoQuitFlag) {
				// while the client is not quit
				stringLine = null;
				try {
					stringLine = br.readLine();
				} catch (IOException e) {
					System.out.println(user.getUserid() + " closed connection!");
					break;
				}
				if (stringLine != null) {
					String instructionJson = getInstuctionFromMessage(stringLine);
					if (instructionJson == null)
						continue;
					Instruction instruction = InstructionFactory.FromJSON(instructionJson);
					{
						switch (instruction.Type()) {
						case Instruction.CREATEROOM: {
							// the user want to create a chat room
							// check specified chat room is existed or
							// not,firstly
							// create specified chat room, secondly
							//
							chatServer.processCreateRoom(stringLine, instruction, user);
							break;
						}
						case Instruction.LOGIN: {
							chatServer.processLogIn(stringLine, instruction, user);
							break;
						}
						case Instruction.REGISTER: {
							chatServer.processRegister(stringLine, instruction, user);
							break;
						}
						case Instruction.DELETE: {
							// the user want to delete a chat room
							// check specified chat room is created by
							// itself or
							// not, firstly
							// if it is, delete the chat room and move the
							// users
							// of this room to MainHall
							//
							chatServer.deleteChatRoom(stringLine, instruction, user);
							break;
						}
						case Instruction.IDENTITYCHANGE: {
							// user want to change nick name of itself
							// check specified name is used by others or
							// not,
							// firstly
							// if it is not, change the name, secondly
							//
							chatServer.processIdentityChange(stringLine, instruction, user);
							break;
						}
						case Instruction.JOIN: {
							// check the designated chat room is existed or
							// not
							// firstly
							// if it existed, check the user is limited or
							// not
							// secondly
							// if not limited, add the user to designated
							// chat
							// room
							// send messages to related users
							chatServer.processJoinInstruction(stringLine, instruction, user);
							break;
						}
						case Instruction.KICK: {
							// when receive a kick instruction,
							// check the chat room is created by itself or
							// not
							// firstly
							// if it is,kick the designated user to MainHall
							// and limit the user can't back in stop time
							// send messages to related users
							chatServer.processKickInstruction(stringLine, instruction, user);
							break;
						}
						case Instruction.LIST:
							// user want to known the information of all
							// chat
							// rooms
							// get the information and send back to user
							System.out
									.println("client list: " + user.getUserid() + " want to list rooms ;" + stringLine);
							chatServer.sendInstructionToSomeOne(chatServer.getRoomListInstruction().ToJSON(), user);
							break;
						case Instruction.MESSAGE:
							// user chat with others in the same room with
							// itself
							// send the message to users in the same room
							// with
							// itself
							System.out.println("client message: " + stringLine);
							MessageInstruction message = (MessageInstruction) instruction;
							message.setIdentity(user.getUserid());
							chatServer.sendMessageToSomeRoom(user.getChatRoom(), message.ToJSON(), user);
							break;
						case Instruction.QUIT:
							// user want to quit
							// deal with things after user's quitting and
							// send
							// message to client
							System.out.println("client list: " + user.getUserid() + " want to quit ;" + stringLine);
							isNoQuitFlag = false;
							chatServer.sendInstructionToSomeOne(new QuitInstruction().ToJSON(), user);
							break;
						case Instruction.WHO: {
							// user want to known the information of some
							// chat
							// room
							// get the information and send back to user
							WhoInstruction whoInstruction = (WhoInstruction) instruction;
							String destRoomId = whoInstruction.getRoomID();
							System.out.println("client content: " + user.getUserid() + " want to kown " + destRoomId
									+ " ;" + stringLine);
							chatServer.sendInstructionToSomeOne(
									chatServer.getRoomContentsInstruction(destRoomId).ToJSON(), user);
							break;
						}
						default:
							break;
						}
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				chatServer.logoutUser(user);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			user = null;
			if (br != null) {
				try {
					if (socket != null)
						socket.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

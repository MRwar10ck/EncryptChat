package com.chat.chatclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.codec.binary.Base64;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.chat.communication.Message;
import com.chat.instruction.CreateRoomInstruction;
import com.chat.instruction.DeleteInstruction;
import com.chat.instruction.ErrorInstruction;
import com.chat.instruction.IdentityChangeInstruction;
import com.chat.instruction.Instruction;
import com.chat.instruction.InstructionFactory;
import com.chat.instruction.JoinInstruction;
import com.chat.instruction.KickInstruction;
import com.chat.instruction.ListInstruction;
import com.chat.instruction.LogInInstruction;
import com.chat.instruction.MessageInstruction;
import com.chat.instruction.NewIdentityInstruction;
import com.chat.instruction.QuitInstruction;
import com.chat.instruction.RegisterInstruction;
import com.chat.instruction.RoomChangeInstruction;
import com.chat.instruction.RoomContentsInstruction;
import com.chat.instruction.RoomListInstruction;
import com.chat.instruction.WhoInstruction;
import com.chat.security.AsymmetricalKeyUtil;
import com.chat.security.HashUtil;
import com.chat.security.SymmetricalKeyUtil;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * ChatClient
 */
public class ChatClient {
	// the file in which storing public keys of server
	private static final String CLIENT_KEY_STORE = "resource/client_ks";
	private static SocketFactory socketFactory = null;

	static {
		// ssl configuration
		System.setProperty("javax.net.ssl.trustStore", CLIENT_KEY_STORE);
		socketFactory = SSLSocketFactory.getDefault();
	}

	public static final String COMMANDFLAG = "#";
	public static final String BLANK = " ";
	private int port;
	private String hostName = "";
	private String identity = "";
	private String roomid = "";
	private Socket socket = null;
	private String creatingRoomId = null;
	private AsymmetricalKeyUtil asymmetricalKeyUtil = null;
	private SymmetricalKeyUtil symmetricalKeyUtil = null;

	public ChatClient() {
	}

	public ChatClient(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	/**
	 * Thread that is used to process the instruction from chat server
	 *
	 */
	class ClientThread extends Thread {
		private BufferedReader br = null;

		public ClientThread(BufferedReader br) {
			this.br = br;
		}

		@Override
		public void run() {
			String stringLine = null;
			boolean isNoQuitFlag = true;
			while (isNoQuitFlag && socket.isConnected()) {
				stringLine = null;
				try {
					try {
						// read a instruction from chatServer
						stringLine = br.readLine();
					} catch (IOException e) {
						break;
					}
					if (stringLine != null) {
						Instruction instruction = InstructionFactory.FromJSON(getInstuctionFromMessage(stringLine));
						switch (instruction.Type()) {
						case Instruction.ERROR: {
							// process the error instruction
							ErrorInstruction errorInstruction = (ErrorInstruction) instruction;
							System.out.println(errorInstruction.getError());
							break;
						}
						case Instruction.MESSAGE: {
							// process the message instruction
							MessageInstruction messageInstruction = (MessageInstruction) instruction;
							String message = null;
							message = messageInstruction.getIdentity() + ": " + messageInstruction.getContent();
							System.out.println(message);
							break;
						}
						case Instruction.NEWIDENTITY: {
							// process the new instruction from server and
							// change the identify
							NewIdentityInstruction newIdentityInstruction = (NewIdentityInstruction) instruction;
							String formerIdentity = newIdentityInstruction.getFormer();
							String newIdentity = newIdentityInstruction.getIdentity();
							if (formerIdentity.equals(newIdentity)) {
								System.out.println("Requested identity is invalid or in use!");
							} else {
								// if the new identity is asked by itself, then
								// change new identity
								if (identity.equals(formerIdentity))
									identity = newIdentity;
								System.out.println(formerIdentity + " is now " + newIdentity);
							}
							break;
						}
						case Instruction.QUIT:
							// quit
							isNoQuitFlag = false;
							break;
						case Instruction.ROOMCHANGE: {
							// process the roomChange instruction from server
							RoomChangeInstruction roomChangeInstruction = (RoomChangeInstruction) instruction;
							String identityTemp = roomChangeInstruction.getIdentity();
							String formerRoom = roomChangeInstruction.getFormerRoom();
							String nowRoom = roomChangeInstruction.getNowRoom();
							if (formerRoom.equals(nowRoom)) {
								System.out.println("The requested room is invalid or non existent.");
							} else if (nowRoom == null || nowRoom.equals("")) {
								System.out.println("The user " + identityTemp + " quite.");
							} else {
								System.out.println(identityTemp + " moved from " + formerRoom + " to " + nowRoom);
								if (identityTemp.equals(identity)) {
									roomid = nowRoom;
								}
							}
							break;
						}
						case Instruction.ROOMCONTENTS:
							// process roomContentsInstruction from chat server
							processRoomContentsInstruction((RoomContentsInstruction) instruction);
							break;
						case Instruction.ROOMLIST: {
							// process the ListInstruction from chat server
							// if it is the respond of createRoomInstruction,
							// process it
							// if it is the respond of RoomListInstruction,
							// process it
							RoomListInstruction roomListInstruction = (RoomListInstruction) instruction;
							HashMap<String, Long> rooms = roomListInstruction.getRooms();
							if (creatingRoomId != null) {
								processRoomListInstruction(roomListInstruction);
								if (rooms.containsKey(creatingRoomId)) {
									System.out.println("Room " + creatingRoomId + " created.");
								} else {
									System.out.println("Room " + creatingRoomId + " is invalid or already in use.");
								}
								// set creatingRoomId to null means that it
								// finished creating room
								creatingRoomId = null;
							} else {
								processRoomListInstruction(roomListInstruction);
							}
							break;
						}
						default:
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("closed!");
		}
	}

	/**
	 * get input from system input and send to chat server
	 * 
	 * @param br
	 *            the outputStream to chat server
	 */
	public void chat(BufferedWriter bw) {
		boolean isNoQuitFlag = true;
		Instruction instruction = null;
		Scanner scanner = new Scanner(System.in);
		String userInput = null;
		System.out.print("[" + roomid + "] " + identity + "> ");
		while (isNoQuitFlag && scanner.hasNext()) {

			try {
				instruction = null;
				userInput = scanner.nextLine();
				if (userInput.startsWith(COMMANDFLAG)) {
					// this statement is a command
					String commandStr = userInput.substring(COMMANDFLAG.length());
					if (userInput.indexOf(BLANK) >= 0) {
						commandStr = userInput.substring(COMMANDFLAG.length(), userInput.indexOf(BLANK));
					}
					switch (commandStr) {
					case Instruction.CREATEROOM: {
						// create a chat room
						String[] options = userInput.split(BLANK);
						String newRoomId = options[1];
						if (newRoomId.length() > 2 && newRoomId.length() < 32 && newRoomId.matches("[a-zA-Z]+")) {
							// the flag means that it is creating a room
							creatingRoomId = newRoomId;
							instruction = new CreateRoomInstruction(newRoomId);
						} else {
							System.out.println("Room " + newRoomId + " is invalid");
						}
						break;
					}
					case Instruction.LOGIN: {
						// log in
						String[] options = userInput.split(BLANK);
						String identity = options[1];
						String password = options[2];
						instruction = new LogInInstruction(identity, password);
						break;
					}
					case Instruction.REGISTER: {
						// register
						String[] options = userInput.split(BLANK);
						String identity = options[1];
						String password = options[2];
						instruction = new RegisterInstruction(identity, password);
						break;
					}
					case Instruction.DELETE: {
						// delete a chat room
						String[] options = userInput.split(BLANK);
						String destRoomId = options[1];
						if (destRoomId.length() > 2 && destRoomId.length() < 32 && destRoomId.matches("[a-zA-Z]+")) {
							// the flag means that it is creating a room
							instruction = new DeleteInstruction(destRoomId);
						} else {
							System.out.println("Room " + destRoomId + " is invalid");
						}
						break;
					}
					case Instruction.IDENTITYCHANGE: {
						// change itself's nickname
						String[] options = userInput.split(BLANK);
						String newIdentity = options[1];
						instruction = new IdentityChangeInstruction(newIdentity);
						break;
					}
					case Instruction.JOIN: {
						// change itself's chat room
						String[] options = userInput.split(BLANK);
						String destinationRoomId = options[1];
						if (destinationRoomId.length() > 2 && destinationRoomId.length() < 32
								&& destinationRoomId.matches("[a-zA-Z]+")) {
							instruction = new JoinInstruction(destinationRoomId);
						} else {
							System.out.println("Room " + destinationRoomId + " is invalid");
						}
						break;
					}
					case Instruction.KICK: {
						// kick some user from the chat room created by itself
						String[] options = userInput.split(BLANK);
						try {
							String destinationRoomId = options[1];
							if (destinationRoomId.length() > 2 && destinationRoomId.length() < 32
									&& destinationRoomId.matches("[a-zA-Z]+")) {
								long stopTime = Long.parseLong(options[2]);
								String destIdentity = options[3];
								instruction = new KickInstruction(destinationRoomId, stopTime, destIdentity);
							} else {
								System.out.println("Room " + destinationRoomId + " is invalid");
							}
						} catch (NumberFormatException e) {
							System.out.println(options[2] + " is invalid");
						}
						break;
					}
					case Instruction.LIST: {
						// list chat rooms
						instruction = new ListInstruction();
						break;
					}
					case Instruction.QUIT: {
						// quit from chat
						isNoQuitFlag = false;
						instruction = new QuitInstruction();
						break;
					}
					case Instruction.WHO: {
						// get information of some chat room

						String[] options = userInput.split(BLANK);
						String destinationRoomId = options[1];
						if (destinationRoomId.length() > 2 && destinationRoomId.length() < 32
								&& destinationRoomId.matches("[a-zA-Z]+")) {
							instruction = new WhoInstruction(destinationRoomId);
						} else {
							System.out.println("Room " + destinationRoomId + " is invalid");
						}
						break;
					}
					default:
						break;
					}
				} else {
					// message instruction
					instruction = new MessageInstruction(userInput);
					System.out.println(identity + ":" + userInput);
				}
				if (instruction != null) {
					try {
						sentInstructionJsonToServer(bw, instruction.ToJSON());
					} catch (Exception e) {
						System.out.println("closed!");
						break;
					}
				}
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Tips:COMMADN + OPTIONS");
			} catch (Exception e) {
				System.out.println("something failed!");
				break;
			}
			System.out.print("[" + roomid + "] " + identity + "> ");
		}
		if (scanner != null) {
			scanner.close();
//			System.exit(0);
		}
	}

	/**
	 * send a instructionJson to server
	 * 
	 * @param bw
	 * @param instructionJson
	 * @throws Exception
	 * @throws IOException
	 */
	public void sentInstructionJsonToServer(BufferedWriter bw, String instructionJson) throws Exception, IOException {
		// write the message to chat server
		Message message = new Message(new String(symmetricalKeyUtil.encryptText(instructionJson)),
				HashUtil.GenerateHash(instructionJson));
		bw.write(message.ToJSON() + System.lineSeparator());
		bw.flush();
	}

	/**
	 * start the chatClient
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		try {
			// create a ssl channel
			socket = socketFactory.createSocket(hostName, port);

			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
			BufferedReader br = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));

			// get the public key of chatServer
			String stringTemp = br.readLine();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(stringTemp));
			KeyFactory keyFactory = KeyFactory.getInstance(AsymmetricalKeyUtil.KEY_ALGORITHM);
			asymmetricalKeyUtil = new AsymmetricalKeyUtil();
			asymmetricalKeyUtil.setPublic_key(keyFactory.generatePublic(pubKeySpec));

			// create a symmetrical key for chat server and client self
			symmetricalKeyUtil = new SymmetricalKeyUtil(SymmetricalKeyUtil.getRandomKeyStr());
			bw.write(new String(Base64.encodeBase64(asymmetricalKeyUtil.encryptText(symmetricalKeyUtil.getKeyStr())),
					"UTF-8") + System.lineSeparator());
			bw.flush();

			// read the identify from instruction
			stringTemp = br.readLine();
			NewIdentityInstruction newIdentityInstruction = (NewIdentityInstruction) InstructionFactory
					.FromJSON(getInstuctionFromMessage(stringTemp));
			identity = newIdentityInstruction.getIdentity();
			System.out.println("Connected to " + hostName + " as " + identity);

			// process the roomChange instruction from server
			stringTemp = br.readLine();
			RoomChangeInstruction roomChangeInstruction = (RoomChangeInstruction) InstructionFactory
					.FromJSON(getInstuctionFromMessage(stringTemp));
			String identityTemp = roomChangeInstruction.getIdentity();
			String formerRoom = roomChangeInstruction.getFormerRoom();
			String nowRoom = roomChangeInstruction.getNowRoom();
			if (formerRoom.equals(nowRoom)) {
				System.out.println("   .");
			} else {
				System.out.println(identityTemp + " moved from " + formerRoom + " to " + nowRoom);
				if (identityTemp.equals(identity)) {
					roomid = nowRoom;
				}
			}

			// read the content of default chatRoom from instruction
			stringTemp = br.readLine();
			RoomContentsInstruction roomContentsInstruction = (RoomContentsInstruction) InstructionFactory
					.FromJSON(getInstuctionFromMessage(stringTemp));
			roomid = roomContentsInstruction.getRoomID();
			processRoomContentsInstruction(roomContentsInstruction);

			// // read chatRooms of server from instruction
			// stringTemp = br.readLine();
			// RoomListInstruction roomListInstruction = (RoomListInstruction)
			// InstructionFactory.FromJSON(stringTemp);
			// processRoomListInstruction(roomListInstruction);

			new ClientThread(br).start();
			chat(bw);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get the instructionJson from message
	 * 
	 * @param stringTemp
	 * @return
	 * @throws Exception
	 */
	public String getInstuctionFromMessage(String stringTemp) throws Exception {
		Message message = Message.FromJSON(stringTemp);
		String instructionJson = symmetricalKeyUtil
				.decryptText(Base64.decodeBase64(message.getEncryptInstructionJson()));
		// prevent communication from attack
		if (!HashUtil.isUnchanged(instructionJson, message.getHash())) {
			System.out.println("There is someone attack you.");
			System.exit(1);
		}
		return instructionJson;
	}

	/**
	 * process roomContentsInstruction from chat server
	 * 
	 * @param roomContentsInstruction
	 */
	public void processRoomContentsInstruction(RoomContentsInstruction roomContentsInstruction) {
		String message = roomContentsInstruction.getRoomID() + " contains ";
		String roomOwner = roomContentsInstruction.getOwner();
		for (String identityTemp : roomContentsInstruction.getIdentities()) {
			if (identityTemp.equals(roomOwner)) {
				message += identityTemp + "* ";
			} else {
				message += identityTemp + " ";
			}
		}
		System.out.println(message);
	}

	/**
	 * process roomListInstruction from chat server
	 * 
	 * @param roomListInstruction
	 */
	public void processRoomListInstruction(RoomListInstruction roomListInstruction) {
		Set<Entry<String, Long>> rooms = roomListInstruction.getRooms().entrySet();
		String message = "";
		for (Entry<String, Long> room : rooms) {
			message += room.getKey() + ": " + room.getValue() + " guests" + System.lineSeparator();
		}
		System.out.print(message);
	}

	public static void main(String[] args) throws Exception {
		ChatClientParser options = new ChatClient.ChatClientParser();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
			new ChatClient(options.getHost(), options.getPort()).start();
		} catch (CmdLineException e) {
			System.out.println(e.getMessage());
			System.out.println("USAGE:");
			parser.printUsage(System.err);
			System.exit(-1);
		}

	}

	static class ChatClientParser {
		@Argument(required = true)
		private String host;

		// Give it a default value of 4444
		@Option(required = false, name = "-p", aliases = { "--port" }, usage = "Port Address")
		private int port = 4444;

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}
	}

}

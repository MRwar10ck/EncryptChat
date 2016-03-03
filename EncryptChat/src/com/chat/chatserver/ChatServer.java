package com.chat.chatserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import org.apache.commons.codec.binary.Base64;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import com.chat.communication.Message;
import com.chat.instruction.CreateRoomInstruction;
import com.chat.instruction.DeleteInstruction;
import com.chat.instruction.ErrorInstruction;
import com.chat.instruction.IdentityChangeInstruction;
import com.chat.instruction.Instruction;
import com.chat.instruction.JoinInstruction;
import com.chat.instruction.KickInstruction;
import com.chat.instruction.LogInInstruction;
import com.chat.instruction.NewIdentityInstruction;
import com.chat.instruction.RegisterInstruction;
import com.chat.instruction.RoomChangeInstruction;
import com.chat.instruction.RoomContentsInstruction;
import com.chat.instruction.RoomListInstruction;
import com.chat.security.AsymmetricalKeyUtil;
import com.chat.security.HashUtil;
import com.chat.security.SymmetricalKeyUtil;

/**
 * 18 Sep Chat System Project Yu XIAO yxiao1 616882<br/>
 * chatServer
 */
public class ChatServer {
	private static final String SERVER_KEY_STORE = "resource/server_ks";
	private static final String SERVER_KEY_STORE_PASSWORD = "123123";
	private static ServerSocketFactory serverSocketFactory = null;
	private static AsymmetricalKeyUtil asymmetricalKeyUtil = null;

	static {
		try {
			// ssl configuration
			System.setProperty("javax.net.ssl.trustStore", SERVER_KEY_STORE);
			SSLContext context = SSLContext.getInstance("TLS");
			KeyStore ks = KeyStore.getInstance("jceks");
			ks.load(new FileInputStream(SERVER_KEY_STORE), null);
			KeyManagerFactory kf = KeyManagerFactory.getInstance("SunX509");
			kf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());
			context.init(kf.getKeyManagers(), null, null);
			serverSocketFactory = context.getServerSocketFactory();

			asymmetricalKeyUtil = new AsymmetricalKeyUtil();
			asymmetricalKeyUtil.createPair();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String defaultRoom = "MainHall";
	private AtomicInteger guestNumber = new AtomicInteger(1);
	private int port;
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<String, User>());
	private Map<String, ChatRoom> chatRooms = Collections.synchronizedMap(new HashMap<String, ChatRoom>());
	private ServerSocket serverSocket = null;
	private Map<String, User> registeredUsers = Collections.synchronizedMap(new HashMap<String, User>());

	public ChatServer(int port) {
		this.port = port;
	}

	/**
	 * clear a user from server
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void clearUser(User user) throws Exception {
		users.remove(user.getUserid());
		ChatRoom chatRoom = user.getChatRoom();
		chatRoom.getIdentities().remove(user);

		RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(user.getUserid(), chatRoom.getRoomid(),
				"");
		sendMessageToSomeRoom(chatRoom, roomChangeInstruction.ToJSON(), user);

		// if the user created room, delete the chat room
		if (user.getCreatedRooms().size() > 0) {
			for (ChatRoom createdRoom : user.getCreatedRooms()) {
				deleteOneChatRoom(createdRoom);
			}
		}
		// delete the user
		user = null;
	}

	/**
	 * clear a user from server
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void logoutUser(User user) throws Exception {
		User registeredUser = registeredUsers.get(user.getUserid());
		if (registeredUser != null) {
			registeredUser.setOnLine(false);

			users.remove(user.getUserid());
			ChatRoom chatRoom = user.getChatRoom();
			chatRoom.getIdentities().remove(user);
			chatRoom.getIdentities().add(registeredUser);
			registeredUser.setChatRoom(chatRoom);

			List<ChatRoom> userCreatedChatRooms = user.getCreatedRooms();
			for (ChatRoom userCreatedChatRoom : userCreatedChatRooms) {
				userCreatedChatRoom.setOwner(registeredUser);
			}
			registeredUser.setCreatedRooms(userCreatedChatRooms);
			// delete the user
			user.setOnLine(false);
			user = null;
		} else {
			users.remove(user.getUserid());
			ChatRoom chatRoom = user.getChatRoom();
			chatRoom.getIdentities().remove(user);
			user = null;
		}
	}

	/**
	 * delete a chatRoom from server and move the users of this chatRoom to
	 * default chatRoom
	 * 
	 * @param chatRoom
	 * @throws Exception
	 */
	public void deleteOneChatRoom(ChatRoom chatRoom) throws Exception {
		ChatRoom defaultRoomTemp = chatRooms.get(defaultRoom);

		for (User identity : chatRoom.getIdentities()) {
			RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(identity.getUserid(),
					identity.getChatRoom().getRoomid(), defaultRoomTemp.getRoomid());
			identity.setChatRoom(defaultRoomTemp);
			defaultRoomTemp.getIdentities().add(identity);
			sendMessageToAll(roomChangeInstruction.ToJSON());
		}

		chatRooms.remove(chatRoom.getRoomid());
	}

	/**
	 * move a user to another chatRoom
	 * 
	 * @param user
	 * @param chatRoom
	 */
	public void moveUserToAnotherRoom(User user, ChatRoom chatRoom) {
		user.getChatRoom().getIdentities().remove(user);
		user.setChatRoom(chatRoom);
		user.getChatRoom().getIdentities().add(user);
	}

	/**
	 * get the users id of certain chat room
	 * 
	 * @param roomID
	 * @return
	 */
	public ArrayList<String> getIdentitiesOfRoom(String roomID) {
		ArrayList<String> identities = new ArrayList<>();
		synchronized (chatRooms) {
			for (User user : chatRooms.get(roomID).getIdentities()) {
				identities.add(user.getUserid());
			}
		}
		return identities;
	}

	/**
	 * send a message to the users in certain chatRoom
	 * 
	 * @param roomID
	 * @param message
	 * @throws Exception
	 */
	public void sendMessageToSomeRoom(String roomID, String message, User localUser) throws Exception {
		ChatRoom chatRoom = chatRooms.get(roomID);
		sendMessageToSomeRoom(chatRoom, message, localUser);
	}

	/**
	 * send a message to users in certain chatRoom
	 * 
	 * @param roomID
	 * @param message
	 * @throws Exception
	 */
	public void sendMessageToSomeRoom(ChatRoom room, String message, User localUser) throws Exception {
		System.out.println("send room " + room.getRoomid() + " : " + message);
		for (User user : room.getIdentities()) {
			if (localUser != null && user == localUser) {
				continue;
			}
			sendInstructionToSomeOne(message, user);
		}
	}

	/**
	 * send message to all users
	 * 
	 * @param message
	 * @throws Exception
	 */
	public void sendMessageToAll(String message) throws Exception {
		System.out.println("send to all : " + message);
		Collection<User> currentUsers = users.values();
		for (User userTemp : currentUsers) {
			sendInstructionToSomeOne(message, userTemp);
		}
	}

	/**
	 * send instructionJson to someone
	 * 
	 * @param message
	 * @throws Exception
	 */
	public void sendInstructionToSomeOne(String instructionJson, User localUser) throws Exception {
		SymmetricalKeyUtil symmetricalKeyUtil = localUser.getSymmetricalKeyUtil();
		if (localUser.getOnLine() && symmetricalKeyUtil != null) {
			Message message = new Message(new String(symmetricalKeyUtil.encryptText(instructionJson)),
					HashUtil.GenerateHash(instructionJson));
			// if the user is logged in
			try {
				System.out.println("send to someone " + localUser.getUserid() + " : " + instructionJson);
				localUser.sendMessage(message.ToJSON());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * start the server
	 */
	public void start() {
		System.out.println("chat server start...");
		try {
			// listen specified port on a ssl channel
			serverSocket = serverSocketFactory.createServerSocket(port);
			// set the server is able to be connected by unauthorized client
			((SSLServerSocket) serverSocket).setNeedClientAuth(false);

			chatRooms.put(defaultRoom, new ChatRoom(defaultRoom, null));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		while (true) {
			try {
				// deal with the new connection
				Socket socket = serverSocket.accept();
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
				BufferedReader br = new BufferedReader(
						new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));

				// create a new nick name for new connection
				int number = this.guestNumber.getAndIncrement();
				User user = new User("guest" + number, chatRooms.get(defaultRoom), bw);
				users.put(user.getUserid(), user);
				chatRooms.get(defaultRoom).getIdentities().add(user);

				// send the public key to client
				bw.write(new String(Base64.encodeBase64(asymmetricalKeyUtil.getPublic_key().getEncoded()), "UTF-8")
						+ System.lineSeparator());
				bw.flush();

				// get the symmetrical key of this client for this connection
				String stringTemp = br.readLine();
				System.out.println(user.getUserid() + " symmetrical key: "
						+ asymmetricalKeyUtil.decryptText(Base64.decodeBase64(stringTemp)));
				user.setSymmetricalKeyUtil(
						new SymmetricalKeyUtil(asymmetricalKeyUtil.decryptText(Base64.decodeBase64(stringTemp))));

				// inform the client nick name for this connection
				NewIdentityInstruction newIdentityInstruction = new NewIdentityInstruction("", user.getUserid());
				System.out.println(newIdentityInstruction.ToJSON());
				sendInstructionToSomeOne(newIdentityInstruction.ToJSON(), user);
				System.out.println(user.getUserid() + " connected to " + defaultRoom);
				sendInstructionToSomeOne(new RoomChangeInstruction(user.getUserid(), "", defaultRoom).ToJSON(), user);
				sendInstructionToSomeOne(getRoomContentsInstruction(defaultRoom).ToJSON(), user);
				sendInstructionToSomeOne(getRoomListInstruction().ToJSON(), user);

				new ChatThread(br, socket, user, this).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get information of all chat rooms
	 * 
	 * @return
	 */
	public RoomListInstruction getRoomListInstruction() {
		HashMap<String, Long> rooms = new HashMap<>();
		for (Entry<String, ChatRoom> entry : this.chatRooms.entrySet()) {
			rooms.put(entry.getKey(), (long) entry.getValue().getIdentities().size());
		}
		RoomListInstruction roomListInstruction = new RoomListInstruction(rooms);
		return roomListInstruction;
	}

	/**
	 * get the information of certain chat room
	 * 
	 * @param roomId
	 * @return
	 */
	public Instruction getRoomContentsInstruction(String roomId) {
		if (!chatRooms.containsKey(roomId)) {
			return new ErrorInstruction(roomId + " is not existed!");
		} else {
			RoomContentsInstruction roomContentsInstruction = new RoomContentsInstruction(roomId,
					getIdentitiesOfRoom(roomId),
					chatRooms.get(roomId).getOwner() == null ? "" : chatRooms.get(roomId).getOwner().getUserid());
			return roomContentsInstruction;
		}
	}

	/**
	 * log in
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void processLogIn(String stringLine, Instruction instruction, User user) throws Exception {
		// process the logInInstruction
		String originIdentity = user.getUserid();
		LogInInstruction logInInstruction = (LogInInstruction) instruction;
		String loginIdentity = logInInstruction.getIdentity();
		String loginPassword = logInInstruction.getPassword();
		System.out.println(originIdentity + " want to log in as " + loginIdentity + " ; " + stringLine);

		if (registeredUsers.containsKey(originIdentity) && registeredUsers.get(originIdentity).getOnLine()) {
			sendInstructionToSomeOne(new ErrorInstruction("Your are logged.").ToJSON(), user);
			System.out.println(originIdentity + " want to log in as " + loginIdentity + ",but failed.");
			return;
		}

		if (!registeredUsers.containsKey(loginIdentity)) {
			// indicate the identity is not registered
			sendInstructionToSomeOne(new ErrorInstruction("This identity is not registered.").ToJSON(), user);
			System.out.println(originIdentity + " want to log in as " + loginIdentity + ",but failed.");
		} else if (registeredUsers.containsKey(loginIdentity) && registeredUsers.get(loginIdentity).getOnLine()) {
			sendInstructionToSomeOne(new ErrorInstruction("This identity is logged in").ToJSON(), user);
			System.out.println(originIdentity + " want to log in as " + loginIdentity + ",but failed.");
		} else if (registeredUsers.containsKey(loginIdentity) && !registeredUsers.get(loginIdentity).getOnLine()) {
			if (!registeredUsers.get(loginIdentity).getPassword().equals(loginPassword)) {
				sendInstructionToSomeOne(new ErrorInstruction("Password is error.").ToJSON(), user);
				System.out.println(originIdentity + " want to log in as " + loginIdentity + ",but failed.");
			} else {
				// get the registered user
				User registeredUser = registeredUsers.get(loginIdentity);
				registeredUser.setOnLine(true);
				// set the fields according to the registered user
				users.remove(user.getUserid());

				// change the information of registered user to current user
				if (user.getChatRoom() == registeredUser.getChatRoom()) {
					System.out.println("user.getChatRoom() :" + user.getChatRoom().getRoomid()
							+ "  registeredUser.getChatRoom():" + registeredUser.getChatRoom().getRoomid());
				} else {
					System.out.println("user.getChatRoom() :" + user.getChatRoom().getRoomid()
							+ "  registeredUser.getChatRoom():" + registeredUser.getChatRoom().getRoomid());
					ChatRoom userChatRoom = user.getChatRoom();
					userChatRoom.getIdentities().remove(user);
					ChatRoom registeredUserChatRoom = registeredUser.getChatRoom();
					registeredUserChatRoom.getIdentities().remove(registeredUser);
					registeredUserChatRoom.getIdentities().add(user);
					user.setChatRoom(registeredUserChatRoom);

					RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(originIdentity,
							userChatRoom.getRoomid(), registeredUserChatRoom.getRoomid());
					sendMessageToSomeRoom(userChatRoom, roomChangeInstruction.ToJSON(), user);
					sendMessageToSomeRoom(registeredUserChatRoom, roomChangeInstruction.ToJSON(), null);

				}

				List<ChatRoom> registeredCreatedChatRooms = registeredUser.getCreatedRooms();
				for (ChatRoom registeredCreatedChatRoom : registeredCreatedChatRooms) {
					registeredCreatedChatRoom.setOwner(user);
				}
				user.setCreatedRooms(registeredCreatedChatRooms);

				user.setUserid(loginIdentity);
				users.put(user.getUserid(), user);
				// inform related users
				NewIdentityInstruction newIdentityInstruction = new NewIdentityInstruction(originIdentity,
						loginIdentity);
				sendMessageToAll(newIdentityInstruction.ToJSON());
				System.out.println(originIdentity + " want to log in as " + loginIdentity + ", successfully.");
			}
		}
	}

	/**
	 * registered a new identity
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void processRegister(String stringLine, Instruction instruction, User user) throws Exception {
		// process the registerInstruction
		RegisterInstruction registerInstruction = (RegisterInstruction) instruction;
		String registerIdentity = registerInstruction.getIdentity();
		String registerPassword = registerInstruction.getPassword();
		System.out.println(user.getUserid() + " want to register " + registerIdentity + " ; " + stringLine);
		if (registeredUsers.containsKey(registerIdentity)) {
			// indicate the identity is in
			sendInstructionToSomeOne(new ErrorInstruction("This identity is in use.").ToJSON(), user);
			System.out.println(user.getUserid() + " want to register " + registerIdentity + ",but failed. ");
		} else {
			// register this identity
			User newUser = new User(registerIdentity, chatRooms.get(defaultRoom));
			newUser.setPassword(registerPassword);
			newUser.setOnLine(false);
			registeredUsers.put(registerIdentity, newUser);
			// inform related users
			sendInstructionToSomeOne(new ErrorInstruction("Register success.").ToJSON(), user);
			System.out.println(user.getUserid() + " want to register " + registerIdentity + ", successfully. ");
		}
	}

	/**
	 * the user want to create a chat room check specified chat room is existed
	 * or not,firstly create specified chat room, secondly
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void processCreateRoom(String stringLine, Instruction instruction, User user) throws Exception {
		CreateRoomInstruction createRoomInstruction = (CreateRoomInstruction) instruction;
		String newRoomId = createRoomInstruction.getRoomID();
		System.out.println(
				"client createroom: " + user.getUserid() + " want to create " + newRoomId + " ; " + stringLine);
		synchronized (chatRooms) {

			if (!chatRooms.containsKey(newRoomId)) {
				ChatRoom chatRoomTemp = new ChatRoom(newRoomId, user);
				user.getCreatedRooms().add(chatRoomTemp);
				chatRooms.put(newRoomId, chatRoomTemp);
			}
		}
		sendInstructionToSomeOne(getRoomListInstruction().ToJSON(), user);
	}

	/**
	 * the user want to delete a chat room check specified chat room is created
	 * by itself or not, firstly if it is, delete the chat room and move the
	 * users of this room to MainHall
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void deleteChatRoom(String stringLine, Instruction instruction, User user) throws Exception {
		DeleteInstruction deleteInstruction = (DeleteInstruction) instruction;
		ChatRoom delRoom = chatRooms.get(deleteInstruction.getRoomID());
		System.out.println("client deleteroom: " + user.getUserid() + " want to del " + deleteInstruction.getRoomID()
				+ " ; " + stringLine);
		for (int i = 0, len = user.getCreatedRooms().size(); i < len; i++) {
			ChatRoom chatRoom = user.getCreatedRooms().get(i);
			if (chatRoom == delRoom) {
				deleteOneChatRoom(chatRoom);
				break;
			}
		}
		user.getCreatedRooms().remove(delRoom);
		sendMessageToSomeRoom(delRoom, getRoomListInstruction().ToJSON(), null);
	}

	/**
	 * user want to change nick name of itself check specified name is used by
	 * others or not, firstly if it is not, change the name, secondly
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void processIdentityChange(String stringLine, Instruction instruction, User user) throws Exception {
		IdentityChangeInstruction identityChangeInstruction = (IdentityChangeInstruction) instruction;
		boolean changeableFlag = true;
		String newIdentity = identityChangeInstruction.getIdentity();
		System.out.println("client identityChage: " + user.getUserid() + " want to change identity " + newIdentity
				+ " ; " + stringLine);
		if (users.containsKey(newIdentity) || registeredUsers.containsKey(newIdentity)) {
			// indicate the identity is used by others
			changeableFlag = false;
			newIdentity = user.getUserid();
		}
		NewIdentityInstruction newIdentityInstruction = new NewIdentityInstruction(user.getUserid(), newIdentity);
		if (changeableFlag) {
			String tempId = user.getUserid();
			users.remove(tempId);

			User registeredUser = registeredUsers.get(tempId);
			if (registeredUser != null) {
				registeredUser.setUserid(newIdentity);
				registeredUsers.remove(tempId);
				registeredUsers.put(newIdentity, registeredUser);
			}

			user.setUserid(newIdentity);

			users.put(newIdentity, user);

			sendMessageToAll(newIdentityInstruction.ToJSON());
		} else {
			sendInstructionToSomeOne(newIdentityInstruction.ToJSON(), user);
		}
	}

	/**
	 * when receive a kick instruction, check the chat room is created by itself
	 * or not firstly if it is,kick the designated user to MainHall and limit
	 * the user can't back in stop time send messages to related users
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void processKickInstruction(String stringLine, Instruction instruction, User user) throws Exception {
		KickInstruction kickInstruction = (KickInstruction) instruction;
		String destRoomId = kickInstruction.getRoomID();
		ChatRoom destChatRoom = chatRooms.get(destRoomId);
		long stopTime = kickInstruction.getTime();
		String destUserId = kickInstruction.getIdentity();

		System.out.println("client kick: " + user.getUserid() + " want to kick user " + destUserId + " from room "
				+ destRoomId + " " + stopTime + " seconds; " + stringLine);

		for (ChatRoom chatRoom : user.getCreatedRooms()) {
			if (chatRoom == destChatRoom) {
				// if the chat room is created by itself
				Timer timerTemp = new Timer(System.currentTimeMillis(), stopTime);
				User destUser = users.get(destUserId);
				if (destUser != null) {
					destChatRoom.getDisabledUser().put(destUser, timerTemp);

					ChatRoom previousRoom = destUser.getChatRoom();
					moveUserToAnotherRoom(destUser, chatRooms.get(defaultRoom));
					ChatRoom nowRoom = destUser.getChatRoom();

					// send messages to related users
					RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(destUserId,
							previousRoom.getRoomid(), nowRoom.getRoomid());
					sendMessageToSomeRoom(previousRoom, roomChangeInstruction.ToJSON(), destUser);
					sendMessageToSomeRoom(nowRoom, roomChangeInstruction.ToJSON(), null);
					sendInstructionToSomeOne(getRoomListInstruction().ToJSON(), destUser);
					sendInstructionToSomeOne(getRoomContentsInstruction(destRoomId).ToJSON(), destUser);
					break;
				}
			}
		}
	}

	/**
	 * check the designated chat room is existed or not firstly if it existed,
	 * check the user is limited or not secondly if not limited, add the user to
	 * designated chat room send messages to related users
	 * 
	 * @param stringLine
	 * @param instruction
	 * @param user
	 * @throws Exception
	 */
	public void processJoinInstruction(String stringLine, Instruction instruction, User user) throws Exception {
		JoinInstruction joinInstruction = (JoinInstruction) instruction;
		String destRoomId = joinInstruction.getRoomID();
		ChatRoom destChatRoom = chatRooms.get(destRoomId);
		System.out
				.println("client join: " + user.getUserid() + " want to join room " + destRoomId + " ; " + stringLine);
		if (destChatRoom != null) {
			Map<User, Timer> disabledUserOfDestRoom = destChatRoom.getDisabledUser();
			Timer timer = disabledUserOfDestRoom.get(user);
			if (timer != null && !timer.isOutDeadLine()) {
				RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(user.getUserid(),
						user.getChatRoom().getRoomid(), user.getChatRoom().getRoomid());
				sendInstructionToSomeOne(roomChangeInstruction.ToJSON(), user);
			} else {
				if (timer != null)
					disabledUserOfDestRoom.remove(user);
				ChatRoom previousRoom = user.getChatRoom();
				moveUserToAnotherRoom(user, destChatRoom);
				ChatRoom nowRoom = user.getChatRoom();
				RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(user.getUserid(),
						previousRoom.getRoomid(), nowRoom.getRoomid());
				sendMessageToSomeRoom(previousRoom, roomChangeInstruction.ToJSON(), user);
				sendMessageToSomeRoom(nowRoom, roomChangeInstruction.ToJSON(), null);
				if (destRoomId.equals(defaultRoom)) {
					sendMessageToSomeRoom(nowRoom, getRoomContentsInstruction(destRoomId).ToJSON(), null);
					sendMessageToSomeRoom(nowRoom, getRoomListInstruction().ToJSON(), null);
				}
			}
		} else {
			RoomChangeInstruction roomChangeInstruction = new RoomChangeInstruction(user.getUserid(),
					user.getChatRoom().getRoomid(), user.getChatRoom().getRoomid());
			sendInstructionToSomeOne(roomChangeInstruction.ToJSON(), user);
		}
	}

	/**
	 * judge the user is logged or not
	 * 
	 * @return true: logged in; false: is not logged in
	 */
	public boolean isLogIn(String userName) {
		User registeredUser = registeredUsers.get(userName);
		if (registeredUser == null)
			return false;

		return registeredUser.getOnLine();
		// return !Pattern.matches("guest\\d+", userName);
	}

	/**
	 * entrance of chatServer
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ChatServerParser options = new ChatServer.ChatServerParser();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
			new ChatServer(options.getPort()).start();
		} catch (CmdLineException e) {
			System.out.println(e.getMessage());
			System.out.println("USAGE:");
			parser.printUsage(System.err);
			System.exit(-1);
		}

	}

	/**
	 * args parser class for chatServer
	 *
	 */
	static class ChatServerParser {
		@Option(required = false, name = "-p", aliases = { "--port" }, usage = "Port Address")
		private int port = 4444;

		public int getPort() {
			return port;
		}

	}
}

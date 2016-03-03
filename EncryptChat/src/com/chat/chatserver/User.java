package com.chat.chatserver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chat.security.SymmetricalKeyUtil;

/**
 * It presents a chat client
 */
public class User {
	// the identify of this user
	private String userid = "";
	// the password of this user
	private String password = null;
	// the chatRoom which this user belongs to
	private ChatRoom chatRoom;
	// the outputStream of this user
	private BufferedWriter bw;
	// this user created chatRooms
	private List<ChatRoom> createdRooms = null;
	// this user's symmetrical key
	private SymmetricalKeyUtil symmetricalKeyUtil = null;
	// the state of user: log in or log out
	private transient boolean onLine;

	public User(String userid, ChatRoom chatRoom, BufferedWriter bw) {
		this.userid = userid;
		this.chatRoom = chatRoom;
		this.bw = bw;
		this.createdRooms = Collections.synchronizedList(new ArrayList<>());
		this.onLine = true;
	}

	public User(String userid, ChatRoom chatRoom) {
		this.userid = userid;
		this.chatRoom = chatRoom;
		this.createdRooms = Collections.synchronizedList(new ArrayList<>());
		this.onLine = false;
	}

	public User(String userid) {
		this.userid = userid;
		this.createdRooms = Collections.synchronizedList(new ArrayList<>());
		this.onLine = false;
	}

	public boolean getOnLine() {
		return onLine;
	}

	public void setOnLine(boolean onLine) {
		this.onLine = onLine;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public SymmetricalKeyUtil getSymmetricalKeyUtil() {
		return symmetricalKeyUtil;
	}

	public void setSymmetricalKeyUtil(SymmetricalKeyUtil symmetricalKeyUtil) {
		this.symmetricalKeyUtil = symmetricalKeyUtil;
	}

	public List<ChatRoom> getCreatedRooms() {
		return createdRooms;
	}

	public boolean addCreatedRooms(ChatRoom chatRoom) {
		return this.createdRooms.add(chatRoom);
	}

	public String getUserid() {
		return userid;
	}

	public synchronized void setUserid(String userid) {
		this.userid = userid;
	}

	public ChatRoom getChatRoom() {
		return chatRoom;
	}

	public synchronized void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	public void setCreatedRooms(List<ChatRoom> createdRooms) {
		this.createdRooms = createdRooms;
	}

	/**
	 * get the writer channel of this user
	 * 
	 * @return
	 */
	public BufferedWriter getBw() {
		return bw;
	}

	public synchronized void sendMessage(String message) throws IOException {
		bw.write(message + System.lineSeparator());
		bw.flush();
	}

}
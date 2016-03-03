package com.chat.chatserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It presents a chat room
 *
 */
public class ChatRoom {
	private String roomid = "";// the name of this chat room
	private User owner;// the owner of this chat room
	private List<User> identities;// presents the users in this chat
									// room
	private Map<User, Timer> disabledUser;// some users is exclouded
											// from this room

	public ChatRoom(String roomid, User owner) {
		this.roomid = roomid;
		this.owner = owner;
		this.identities = Collections.synchronizedList(new ArrayList<>());
		this.disabledUser = Collections.synchronizedMap(new HashMap<>());
	}

	public String getRoomid() {
		return roomid;
	}

	public synchronized void setRoomid(String roomid) {
		this.roomid = roomid;
	}

	public User getOwner() {
		return owner;
	}

	public synchronized void setOwner(User owner) {
		this.owner = owner;
	}

	public List<User> getIdentities() {
		return identities;
	}

	public Map<User, Timer> getDisabledUser() {
		return disabledUser;
	}
}

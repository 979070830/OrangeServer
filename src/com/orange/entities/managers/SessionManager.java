package com.orange.entities.managers;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.orange.entities.User;
import com.orange.exceptions.OSException;

public class SessionManager {
	
	private static ConcurrentMap<ChannelHandlerContext, User> sessionUser = new ConcurrentHashMap();
	
	public static User getUser(ChannelHandlerContext session)
	{
		return sessionUser.get(session);
	}
	
	public static void addUser(ChannelHandlerContext session, User user)
	{
		if(!sessionUser.containsKey(session))
	    {
			sessionUser.put(session, user);
	    }
	    else
	    {
	    	throw new OSException("session重复");
	    }
	}
	
	public static boolean containsSession(ChannelHandlerContext session)
	{
		return sessionUser.containsKey(session);
	}
	
	public static boolean containsSession(User user)
	{
		return sessionUser.containsValue(user);
	}
}

package com.teamnine.zocial;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    // contains a static List of ChatRoom used to control the existing rooms and their users
    public static List<ChatRoom> listOfChatRooms = new ArrayList<ChatRoom>();

    // you may add other attributes as you see fit



    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        //session.getBasicRemote().sendText("First sample message to the client");
//        accessing the roomID parameter
        ChatRoom newChatRoomCreated = new ChatRoom(roomID, session.getId());
        listOfChatRooms.add(newChatRoomCreated);
        System.out.println(roomID);
    }
    public static String  FindUsernameInChatRoom(String givenChatRoomCode, String givenSessionID) throws IOException{
        String reFormattedChatRoomCode = givenChatRoomCode.replace("\r\n", "");
        ChatRoom result = listOfChatRooms.stream()
                .filter(givenChatRoom -> reFormattedChatRoomCode.toString().equalsIgnoreCase(givenChatRoom.getCode().toString()))
                .findAny()
                .orElse(null);
        if(result == null)
            throw new IOException("Chat room cannot be found");

        return result.getUsers().get(givenSessionID);

    }


    public static void  UpdateUsernameInChatRoom(String givenChatRoomCode, String givenSessionID, String givenUserName){
        String reFormattedChatRoomCode = givenChatRoomCode.replace("\r\n", "");
        ChatRoom result = listOfChatRooms.stream()
                .filter(givenChatRoom -> reFormattedChatRoomCode.toString().equalsIgnoreCase(givenChatRoom.getCode().toString()))
                .findAny()
                .orElse(null);

        result.getUsers().put(givenSessionID, givenUserName);
    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();
        // do things for when the connection closes
    }

    @OnMessage
    public void handleMessage(@PathParam("roomID") String roomID, String comm, Session session) throws IOException, EncodeException {
//        example getting unique userID that sent this message
        String userId = session.getId();
        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");
        String chatRoomCode = roomID;

        switch(type) {
            case "setUserName":
                UpdateUsernameInChatRoom(chatRoomCode, userId, message);
                for(Session peer: session.getOpenSessions()){
                    // only send my messages to those in the same room
                    peer.getBasicRemote().sendText("{\"type\": \"SetUserName\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + "Username successfully set to " + message + "\"}");

                }
                break;
            case "chat":
                System.out.println("Chat instruction called !");
                String senderUsername = FindUsernameInChatRoom(chatRoomCode, userId);
                for(Session peer: session.getOpenSessions()){
                    // only send my messages to those in the same room
                    peer.getBasicRemote().sendText("{\"type\": \"chat\"" + ","+ "\"username\": \"" + senderUsername + "\", \"message\":\"" + message + "\"}");

                }
                break;
            default:
                throw new IOException("error : client sent wrong instruction. ");
        }

//        Example conversion of json messages from the client
        //        JSONObject jsonmsg = new JSONObject(comm);
//        String val1 = (String) jsonmsg.get("attribute1");
//        String val2 = (String) jsonmsg.get("attribute2");

        // handle the messages


    }


}

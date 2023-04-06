package com.example.wschatserverdemo;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    private Map<String, String> usernames = new HashMap<String, String>();
    private static Map<String, String> roomList = new HashMap<String, String>();
    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {
        //session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome to the chat room. Please state your username to begin.\"}");
        System.out.println("Room number is " + roomID);
        System.out.println(" New client entered " + session.getId());
        //roomList.put(session.getId(), roomID); // adding userID to a room
    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        System.out.println("Called for no reason");
        String userId = session.getId();
        if (usernames.containsKey(userId)) {
            String username = usernames.get(userId);
            String roomID = roomList.get(userId);
            usernames.remove(userId);
            for (Session peer : session.getOpenSessions()){ //broadcast this person left the server
                if(roomList.get(peer.getId()).equals(roomID)) { // broadcast only to those in the same room
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + username + " left the chat room.\"}");
                }
            }
        }
    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        String userID = session.getId(); // my id
        String roomID = roomList.get(userID); // my room
        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = " bal";
        System.out.println(" GIVEN COMMAND - " + type);
        switch(type) {
            case "play":
                System.out.println(" Play command sent !");
                for(Session peer: session.getOpenSessions()){
                    // only send my messages to those in the same room
                    peer.getBasicRemote().sendText(comm);
                }
                break;
            case "pause":
                System.out.println(" pause command sent !");
                for(Session peer: session.getOpenSessions()){
                    // only send my messages to those in the same room
                    peer.getBasicRemote().sendText(comm);

                }
                break;
            case "seeked":
                System.out.println(" seeked command sent !");
                for(Session peer: session.getOpenSessions()){
                    // only send my messages to those in the same room
                    peer.getBasicRemote().sendText(comm);

                }
                break;
            case "chat":
                System.out.println("Chat instruction called !");
                if(usernames.containsKey(userID)){ // not their first message
                    String username = usernames.get(userID);
                    System.out.println(username);
                    for(Session peer: session.getOpenSessions()){
                        // only send my messages to those in the same room
                        if(roomList.get(peer.getId()).equals(roomID)) {
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message + "\"}");
                        }
                    }
                }else{ //first message is their username
                    usernames.put(userID, message);
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome, " + message + "!\"}");
                    for(Session peer: session.getOpenSessions()){
                        // only announce to those in the same room as me, excluding myself
                        if((!peer.getId().equals(userID)) && (roomList.get(peer.getId()).equals(roomID))){
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + message + " joined the chat room.\"}");
                        }
                    }
                }
                break;
            default:
                throw new IOException("error : client sent wrong instruction. ");
        }



    }

}

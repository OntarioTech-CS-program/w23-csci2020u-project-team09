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
    /** Chat Room has two attributes
        - code
        - List of people in the chat room
        - TODO - (Coordinate w/ Heather/ Jahanvi) Might have to implement a list of messages similar to WSChatServerDemo
    **/
    public static List<ChatRoom> listOfChatRooms = new ArrayList<ChatRoom>();

    // you may add other attributes as you see fit



    /**
    Function that executes whenever a new client connects to the web socket. 
    Params: 
        - roomID that the client is requesting to connect to
        - Session object that contatins properties of the client such as SessionID when the connection is newly created.
    Function creates a new chat room and adds it to the list of chat rooms (Static variable)
    TO-DO -> remove System.out.println when debugging process is complete and ready for submission. 
    **/
    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        //session.getBasicRemote().sendText("First sample message to the client");
//        accessing the roomID parameter
        ChatRoom newChatRoomCreated = new ChatRoom(roomID, session.getId());
        listOfChatRooms.add(newChatRoomCreated);
        System.out.println(roomID);
    }
    
    /*
    FindUsernameInChatRoom()
        @Params:
            - String givenChatRoomCode: The room code in which the user is supposedly in. 
            - String givenSessionID: The unique identifier that is associated with the client. 
        @returns (String): The username that is associated with the client in a given chat room. 
    */
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

    /*
    UpdateUsernameInChatRoom()
        @Params:
            - String givenChatRoomCode: The room code in which the user is supposedly in. 
            - String givenSessionID: The unique identifier that is associated with the client. 
            - String givenUserName: The username to be updated for the client. 
        Updates the username value of the client using the givenUserName. 
        TODO -> If time permits, ensure UpdateUsernameInChatRoom throws exception if result is null. 
    */
    public static void  UpdateUsernameInChatRoom(String givenChatRoomCode, String givenSessionID, String givenUserName){
        String reFormattedChatRoomCode = givenChatRoomCode.replace("\r\n", "");
        ChatRoom result = listOfChatRooms.stream()
                .filter(givenChatRoom -> reFormattedChatRoomCode.toString().equalsIgnoreCase(givenChatRoom.getCode().toString()))
                .findAny()
                .orElse(null);

        result.getUsers().put(givenSessionID, givenUserName);
    }

    /*
    Function that executes whenever the client disconnects from the websocket. 
    TODO -> Remove the < (String) Session.GetID , (String) username > item from the HashMap list data member in the ChatRoom class. 
    TODO -> Send a message to the client saying the client has successfully disconnected. 
            The message needs to be of the shape of the following JSON structure: 
            "{\"type\": \"Close\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + "Client successfully disconnected." + "\"}"
    TODO -> Implement try catch statements (time permitting and look at the rubric for expectations and stuff ...)
    */
    @OnClose
    public void close(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {
        String userId = session.getId();
        // do things for when the connection closes
        // announce to other people that the user has left --> call makeAnnouncement()
    }

    /*
    Function that executes whenever the client sends a message to the websocket. 
    TODO -> Should handle 2 cases where type is the following: 
            (setUsername) - Updates the username in the chat room for the client (indexed by Session.GetID())
            (chat) - Text messages to fellow users only in that room. 
    */
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
                
                // processMessage();
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
    
    /*
    void processMessage(){
    
    // User can newly enter a room // onOpen  (ignore) 
    // user creates the room  // onOpen (Ignore)
    
    // create a for loop to iterate over a list of clients (here clients can be in multiple rooms)
        // evaluate if the user is in the room (use the chatRoom.InRoom funcion) (utilize the path param, 
        
        // put the following lines in a function called makeAnnouncement()
        // find the room in the static variable - chat room list
            if its true, send the JSON BODY of the following outline: 
                type: chat
                message blah
              
    
    
    }
    */


}
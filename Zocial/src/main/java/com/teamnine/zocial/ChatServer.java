package com.teamnine.zocial;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.ResourceAPI.loadChatRoomHistory;
import static util.ResourceAPI.saveChatRoomHistory;

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
    private static Map<String, String> roomHistoryList = new HashMap<String, String>();


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
        ChatRoom result = FindChatRoom(roomID);
        if(result == null){
            ChatRoom newChatRoomCreated = new ChatRoom(roomID, session.getId());
            listOfChatRooms.add(newChatRoomCreated);
        }
        else{
            result.getUsers().put(session.getId(),"");
            System.out.println("Room already exists! Adding new client " + session.getId() + "to the chat room : " + result.getCode());

        }



        // loading the history chat
        String history = loadChatRoomHistory(roomID);
        System.out.println("Room joined ");
        if (history!=null && !(history.isBlank())){
            System.out.println(history);
            history = history.replaceAll(System.lineSeparator(), "\\\n");
            System.out.println(history);


            //session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\""+history+" \\n Chat room history loaded\"}"); //placeholder/debug text
            //BroadcastMessage(result,session,"{\"type\": \"chat\"" + ","+ "\"username\": \"" + "♦chatHistory♦" + "\", \"message\":\"" + history + "\"}");
            //roomHistoryList.put(roomID, history+" \\n "+roomID + " room resumed.");

            historyBuilder(history, result, session);


        }

//        if(!roomHistoryList.containsKey(roomID)) { // only if this room has no history yet
//            roomHistoryList.put(roomID, "Beginning of Chat History"); //initiating the room history
//        }
    }

    public static void historyBuilder(String history, ChatRoom result, Session session) throws IOException {
        StringBuilder usernameBuilder = new StringBuilder();
        StringBuilder messageBuilder = new StringBuilder();
        boolean buildingUsername = false;

        for (char c : history.toCharArray()) {
            if (c == '♣') {
                buildingUsername = true;
            } else if (c == '♠') {
                buildingUsername = false;
            } else if (c == '♦') {
                // Store username and message
                String username = usernameBuilder.toString();
                String message = messageBuilder.toString();
                System.out.println(username + ": " + message);

                // Run broadcast message
                BroadcastMessage(result, session, "{\"type\": \"chat\"" + ","+ "\"username\": \"" + username + "\", \"message\":\"" + message+ "\"}");

                // Reset builders
                usernameBuilder = new StringBuilder();
                messageBuilder = new StringBuilder();
            } else {
                if (buildingUsername) {
                    usernameBuilder.append(c);
                } else {
                    messageBuilder.append(c);
                }
            }
        }
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
        if(result == null)
            throw new RuntimeException("Client whose username is to be updated does not exist! Please try again! ");
        result.getUsers().put(givenSessionID, givenUserName);
    }

    public static ChatRoom FindChatRoom(String givenChatRoomCode){
        String reFormattedChatRoomCode = givenChatRoomCode.replace("\r\n", "");
        ChatRoom result = listOfChatRooms.stream()
                .filter(givenChatRoom -> reFormattedChatRoomCode.toString().equalsIgnoreCase(givenChatRoom.getCode().toString()))
                .findAny()
                .orElse(null);

        return result;

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

        ChatRoom result = FindChatRoom(roomID);
        if(result == null)
            throw new IOException("Server: Room does not exist");
        String username = FindUsernameInChatRoom(result.getCode(), session.getId());
        BroadcastMessage(result,session, "{\"type\": \"Close\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + username + " left the chat room." + "\"}");
        result.getUsers().remove(session.getId());
        boolean closedRoomSuccess = CloseChatRemove(result);
        if(closedRoomSuccess){
            System.out.println("Room closed successfully because no one is here");
        }
        else{
            System.out.println("Room is still occupied, only single user removed");
        }

        //saveChatRoomHistory(roomID, roomHistoryList.get(roomID));


    }

    public static boolean CloseChatRemove(ChatRoom givenRoom) throws IOException{
        boolean result = false;
        if(givenRoom.getUsers().size() == 0){
            saveChatRoomHistory(givenRoom.getCode(), roomHistoryList.get(givenRoom.getCode()));
            listOfChatRooms.remove(givenRoom);
            ZocialServlet.rooms.remove(givenRoom.getCode());
            result = true;
        }
        return  result;
    }
    public static int BroadcastMessage(ChatRoom givenChatRoom, Session session, String givenJSONObjToBeBroadcasted) throws IOException{
        int countPeers = 0;
        for (Session peer : session.getOpenSessions()){ //broadcast this person left the server
            System.out.println(" This function was called. It should only broadcst to " + givenChatRoom.getCode());
            System.out.println(" The length of the Chat room " + givenChatRoom.getCode() + " is : " + givenChatRoom.getUsers().size());
            if(givenChatRoom.getUsers().containsKey(peer.getId())) { // broadcast only to those in the same room
                peer.getBasicRemote().sendText(givenJSONObjToBeBroadcasted);
                countPeers++; // count how many peers are left in the room
            }
        }

        return countPeers;
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
        ChatRoom result = FindChatRoom(chatRoomCode);
        String updatedTimeStamp = "0";

        // adding event to the history of the room
        //String username = FindUsernameInChatRoom(chatRoomCode, userId);
        //System.out.println(username);


        if (result == null){
            throw new IOException(" Room is non-existent !");
        }

        switch(type) {
            case "setUserName":
                UpdateUsernameInChatRoom(chatRoomCode, userId, message);
                BroadcastMessage(result,session,"{\"type\": \"SetUserName\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + message + " joined the room." + "\"}");
                break;
            case "updateVideoTimeStamp":
                System.out.println("User requested to update timestamp they're playing at. ");
                updatedTimeStamp = jsonmsg.get("currentTime").toString();;
                break;
            case "broadcastVideo":
                result.setVideoCode(message);
                BroadcastMessage(result,session,"{\"type\": \"BroadcastVideo\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + result.getVideoCode() + "\"}");
                break;
            case "streamVideo":
                BroadcastMessage(result,session,"{\"type\": \"StreamVideo\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + result.getVideoCode() + "\",\"currentTime\": \"" + updatedTimeStamp  + "\"}");
                break;
            case "chat":
                String senderUsername = FindUsernameInChatRoom(chatRoomCode, userId);
                BroadcastMessage(result,session,"{\"type\": \"chat\"" + ","+ "\"username\": \"" + senderUsername + "\", \"message\":\"" + message + "\"}");
                //Save this message in chat history
                String logHistory = roomHistoryList.get(roomID);
                roomHistoryList.put(roomID, logHistory+" \\n " +"♣" + senderUsername + "♠ " + message + "♦");
                break;
            case "play":
                System.out.println(" Play command sent !");
                BroadcastMessage(result,session,"{\"type\": \"play\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + result.getVideoCode() +  " played." + "\"}");
                break;
            case "pause":
                System.out.println(" pause command sent !");
                BroadcastMessage(result,session,"{\"type\": \"pause\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + result.getVideoCode() +  " paused." + "\"}");
                break;
            case "seeked":
                System.out.println(" seeked command sent !");
                String currentTime = jsonmsg.get("currentTime").toString();
                BroadcastMessage(result,session,"{\"type\": \"seeked\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + result.getVideoCode() +  " synced." + "\",\"currentTime\":\"" + currentTime + "\"}");
                break;
            default:
                throw new IOException("error : client sent wrong instruction. ");
        }
    }
}
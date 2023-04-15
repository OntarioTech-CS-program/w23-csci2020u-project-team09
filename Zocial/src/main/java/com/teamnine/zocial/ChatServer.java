package com.teamnine.zocial;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
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
    /** ChatServer has three attributes
     - list of existing chat rooms
     - list of histories of rooms
     - moderator
     **/
    public static List<ChatRoom> listOfChatRooms = new ArrayList<ChatRoom>();
    private static Map<String, String> roomHistoryList = new HashMap<String, String>();

    Moderate mod;

    {
        try {
            mod = new Moderate();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     Function that executes whenever a new client connects to the web socket.
     Params:
     - roomID that the client is requesting to connect to
     - Session object that contains properties of the client such as SessionID when the connection is newly created.
     Function creates a new chat room and adds it to the list of chat rooms (Static variable)
     **/
    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        ChatRoom result = FindChatRoom(roomID);

        if (result == null) {
            ChatRoom newChatRoomCreated = new ChatRoom(roomID, session.getId());
            listOfChatRooms.add(newChatRoomCreated);

            // loading the history chat
            String history = loadChatRoomHistory(roomID);

            if (history != null && !history.isBlank()) {
                // Add loaded chat history to roomHistoryList
                roomHistoryList.put(roomID, history);
                // build the history
                historyBuilder(history, newChatRoomCreated, session);
            }

            if(!roomHistoryList.containsKey(roomID)) { // only if this room has no history yet
                roomHistoryList.put(roomID, " "); //initiating the room history
            }
        } else {
            result.getUsers().put(session.getId(), "");
            System.out.println("Room already exists! Adding new client " + session.getId() + "to the chat room : " + result.getCode());

            // loading the history chat
            String history = loadChatRoomHistory(roomID);

            if (history != null && !history.isBlank()) {
                // Add loaded chat history to roomHistoryList
                roomHistoryList.put(roomID, history);
                // build the history
                historyBuilder(history, result, session);
            }
            if(!roomHistoryList.containsKey(roomID)) { // only if this room has no history yet
                roomHistoryList.put(roomID, "[first message]"); //initiating the room history
            }
        }
    }

    /**
     Function that builds the room history and loads it as messages
     Params:
     - history of the chat room
     - result: the chatroom for which the history is to be built
     - Session object that contains properties of the client such as SessionID
     **/
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
                session.getBasicRemote().sendText( "{\"type\": \"chat\"" + ","+ "\"username\": \"" + username + "\", \"message\":\"" + message+ "\"}");

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

    /*
    FindChatRoom
        @Params:
            - String givenChatRoomCode: The room code in which the user is supposedly in.
        @returns (ChatRoom): The chat room associated with the room code
    */
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
     - roomID that the client is requesting to disconnect from
     - Session object that contains properties of the client such as SessionID
    */
    @OnClose
    public void close(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        ChatRoom result = FindChatRoom(roomID);

        if(result == null)
            throw new IOException("Server: Room does not exist");

        if(result.inRoom(session.getId())) {

            String username = FindUsernameInChatRoom(result.getCode(), session.getId());
            BroadcastMessage(result, session, "{\"type\": \"Close\"" + "," + "\"username\": \"" + "Server" + "\", \"message\":\"" + username + " left the chat room." + "\"}");
            result.getUsers().remove(session.getId());

            boolean closedRoomSuccess = CloseChatRemove(result);

            if (closedRoomSuccess) {
                System.out.println("Room closed successfully because no one is here");
            } else {
                System.out.println("Room is still occupied, only single user removed");
            }
        }
    }

    /*
    Function removes chatroom from list of chat rooms
    * */
    public static boolean CloseChatRemove(ChatRoom givenRoom) throws IOException{
        boolean result = false;
        if(givenRoom.getUsers().size() == 0){
            listOfChatRooms.remove(givenRoom);
            ZocialServlet.rooms.remove(givenRoom.getCode());
            result = true;
        }
        return  result;
    }

    /**
     Function that broadcasts a message to all the users in a chat room
     * */
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

        if (result == null){
            throw new IOException(" Room is non-existent !");
        }

        switch(type) {
            case "setUserName":
                // checking if the message contains any of the banned words
                for (String word: mod.listOfModeratedWords) {
                    if (message.contains(word.toLowerCase())){
                        message = message.replace(word, "***");
                    }
                }
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
                int strike = 0;
                // checking if the message contains any of the banned words
                for (String word: mod.listOfModeratedWords) {
                    if (message.contains(word.toLowerCase())){
                        message = message.replace(word, "***");
                        strike = 1;
                    }
                }

                result.updateStrike(userId, strike);
                String senderUsername = FindUsernameInChatRoom(chatRoomCode, userId);
                if(result.getStrike(userId) <= 3) {
                    BroadcastMessage(result, session, "{\"type\": \"chat\"" + "," + "\"username\": \"" + senderUsername + "\", \"message\":\"" + message + "\"}");
                    //Save this message in chat history
                    String logHistory = roomHistoryList.get(roomID);
                    roomHistoryList.put(roomID, logHistory + " \\n " + "♣" + senderUsername + "♠ " + message + "♦");
                    saveChatRoomHistory(result.getCode(), roomHistoryList.get(result.getCode()));
                } else { // if the user has entered a swear word more than 3 times
                    if(result.inRoom(userId)) { // if user is still in room, kick user out and broadcast to remaining users
                        BroadcastMessage(result,session,"{\"type\": \"KickUser\"" + ","+ "\"username\": \"" + "Server" + "\", \"message\":\"" + senderUsername + " was kicked out of the room." + "\"}");
                        kickUser(result, userId);
                    }
                }
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

    /*
    Function that takes care of removing the user from the chat room
    * */
    public void kickUser(ChatRoom room, String userID) throws IOException {
        room.getUsers().remove(userID); // remove user from room list
        room.updateStrike(userID, 0); //update user strikes
    }
}
package com.example.chatresourceapi;

import com.example.util.FileReaderWriter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static com.example.util.FileReaderWriter.readHistoryFile;

@Path("/history")
public class HistoryResource {
    @GET
    @Produces("text/plain")
    public String hello() {
        return "Hello, World!";
    }

    @GET
    @Path("/{roomID}")
    @Produces("application/json")
    /**
     * GET HTTP METHOD
     * This method will read the content of roomID.json file and send it back to requester
     * **/
    public Response getRoomHistory(@PathParam("roomID") String roomID) {

         //loading the resource directory
        URL url = this.getClass().getClassLoader().getResource("/chatHistory");
        String history = "";
        File mainDir = null;
        try {
            mainDir = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // loading the file content into history
        try {
            history = FileReaderWriter.readHistoryFile(mainDir, roomID+".json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        //build the json data for the response
        JSONObject mapper = new JSONObject();
        mapper.put("room", roomID);
        if(history!=null){
            mapper.put("log", history);
        }else{
            mapper.put("log", "");
        }

        // build the Response object sending json data in the entity
        Response myResp = Response.status(200) // success
                .header("Content-Type", "application/json")
                .entity(mapper.toString()) // adding the json data
                .build();
        return myResp;
    }


    @POST
    @Path("/{roomID}")
    @Consumes("application/json")
    @Produces("application/json")
    /**
     * POST HTTP METHOD
     * This method will receive the history log of a room and store it into a json file
     * **/
    public Response saveRoomHistory(@PathParam("roomID") String roomID, String content) {

        // parse the consumed json data
        System.out.println(content);
        JSONObject mapper = new JSONObject(content);
        Map<String,Object> result = mapper.toMap();
        String filename = (String) result.get("room");

        // loading the resource directory
        URL url = this.getClass().getClassLoader().getResource("/chatHistory");


        File data = null;
        try {
            System.out.println(url.toURI());
            data = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            // saving the chat log history to the roomID.json file in the resources folder
            FileReaderWriter.saveNewFile(data, filename+".json", (String) result.get("log"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        Response myResp = Response.status(200) // success
                .header("Content-Type", "application/json")
                .build();
        return myResp;
    }
}
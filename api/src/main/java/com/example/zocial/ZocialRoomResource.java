package com.example.zocial;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;


@Path("/rooms")

public class ZocialRoomResource {

    @GET
    @Produces("text/plain")
    public String hello() {
        return  " Name: Supriya Mutharasan, Mariyam Muhammad , Jake Sullivan \n" +
                " \n" +
                " Date : 01/03/2023 \n" +
                " Final Group Project : Zocial";
    }

    @GET
    @Produces("application/json")
    @Path("/data")
    public Response json(){
        String val = "{\"List Of Rooms\": " +
                "[" +
                "{\"name\": \"Never Have I ever\"," +
                " \"id\": 100000000," +
                " \"numberOfClients\": 4}" +
                "]}";
        System.out.println("Refresh Button Invoked");
        Response myResp = Response.status(200)
                .header("Content-Type", "application/json")
                .entity(val)
                .build();

        return myResp;
    }
}

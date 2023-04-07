package com.teamnine.zocial;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

@WebServlet(name = "zocialServlet", value = "/zocial-servlet")
public class ZocialServlet extends HttpServlet {
    private String message;

    //static so this set is unique
    public static Set<String> rooms = new HashSet<>();

    /**
     * Method generates unique room codes
     * **/
    public String addRoom(String givenCode) {
        // generating unique room code if the room code already exists
        while (rooms.contains(givenCode)){
            givenCode = RandomStringUtils.randomAlphanumeric(5).toUpperCase();
        }
        rooms.add(givenCode);
        return givenCode;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");

        // send the random code as the response's content
        PrintWriter out = response.getWriter();
        out.println("Zocial Server is up and running");

    }

    public void destroy() {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject jObj = new JSONObject(tokener);

        String owner = (String) jObj.get("owner");
        String roomCode = (String) jObj.get("roomCode");
        System.out.println(" Owner  : " + owner);
        System.out.println(" Room code : " + roomCode);

        PrintWriter out = response.getWriter();
        out.println(addRoom(roomCode));

    }
}

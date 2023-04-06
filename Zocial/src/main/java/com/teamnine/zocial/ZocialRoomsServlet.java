package com.teamnine.zocial;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ZocialRoomsServlet", value = "/zocial-servlet/rooms")
public class ZocialRoomsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String result = "[";
        if(!ZocialServlet.rooms.isEmpty()){
            for(String elem : ZocialServlet.rooms){
                result += "\"";
                result  += elem;
                result += "\"";
                result += ",";
            }
            result  = result.substring(0,result.length() - 1);
        }

        result += "]";
        response.setContentType("text/plain");

        // send the random code as the response's content
        PrintWriter out = response.getWriter();
        out.println(result);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

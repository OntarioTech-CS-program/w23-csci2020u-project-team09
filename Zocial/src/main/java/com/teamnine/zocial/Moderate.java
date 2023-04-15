package com.teamnine.zocial;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Moderate {

    // stores list of words that are to be bleeped if they appear in a message
    public List<String> listOfModeratedWords = new ArrayList<String>();

    // initializes the list with contents of swearWords.txt
    public Moderate() throws FileNotFoundException, URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("swearWords.txt");
        File file = new File(url.toURI());
        Scanner s = new Scanner(file);
        while (s.hasNext()){
            listOfModeratedWords.add(s.next());
        }
        s.close();
    }
}
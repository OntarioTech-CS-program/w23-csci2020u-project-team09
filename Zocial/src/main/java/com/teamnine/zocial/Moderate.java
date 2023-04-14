package com.teamnine.zocial;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Moderate {
    public List<String> listOfModeratedWords = new ArrayList<String>();

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
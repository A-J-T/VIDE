/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vide;

import java.util.Vector;

/**
 *
 * @author ajt
 */
public class Akash {
    public static void main(String[] args)
    {
        Vector<String> lol = new Vector<String>();
        lol.add("HC-05: 1497");
        lol.add("HC-05: 1151");
        lol.add("HC-05: 1349");
        lol.add("HC-05: 1203");
        lol.add("HC-05: 1231");
        lol.add("HC-05: 1031");
        lol.add("HC-05: 1014");
        String finalDisplayText="";
        for (Object o : lol.toArray()) {
             finalDisplayText = finalDisplayText + o.toString() + "\n";
        }
        System.out.println(finalDisplayText);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vide;

/**
 *
 * @author Amod Tawade
 */
public class VIDE {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        splash s = new splash();
        s.setVisible(true);
        try
        {
            Thread.sleep(2000);
        }catch(InterruptedException e){}
        s.setVisible(false);
        new window().setVisible(true);
    }
    
}

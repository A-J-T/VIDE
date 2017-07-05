/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vide;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;
import com.Ostermiller.Syntax.HighlightedDocument;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.KeyStroke;
import tts.TextToSpeech;
/**
 *I
 * @author Amod Tawade
 */
public class window extends javax.swing.JFrame {
    private ArrayList<JTextPane> textpanes = new ArrayList<JTextPane>();
    private ArrayList<UndoManager> undomanagers = new ArrayList<UndoManager>();
    private ArrayList<TextLineNumber> tlns = new ArrayList<TextLineNumber>();
    private FindReplace f;
    private prefs pfs;
    private KeyStroke k;
    private Thread recognizerThread;
    private Thread answerThread;
    private boolean textParsingMode;
    private Configuration configuration, a_configuration, c_configuration;
    private LiveSpeechRecognizer recognizer, a_recognizer, c_recognizer;
    private TextToSpeech tts;
    private boolean findBox, preferencesBox, shortcutBox, aboutBox, offlineHelp;
    private Map m;
    private Map<String,Runnable> cm;
    private String commandKey;
    private boolean recognizerStopped;
    public window() {
        initComponents();
        setMyIcons();
        setLocation();
        pfs = new prefs();
        findBox = preferencesBox = shortcutBox = aboutBox = offlineHelp = false;
        textParsingMode = false;
        tts = new TextToSpeech();
        setMyCursorForContainer(this);
        setMyCursorForContainer(jPanel1);
        setMyCursorForContainer(jPanel2);
        setMyCursorForContainer(jPanel3);
        setMyCursorForContainer(jTabbedPane1);
        setMyCursorForComponent(jTextField1);
        setMyCursorForComponent(jTextPane1);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800,600));
        initialiseCommandMap();
        initializeCommandMap();
        setAnswerConfiguration();
        setRecognizerConfiguration();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initTabComponent(jTabbedPane1.getSelectedIndex());
    }
    public void dateAndTime()
    {
        Date d = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1)
        {
            String text = textpanes.get(index).getText();
            text += dateFormat.format(d);
            textpanes.get(index).setText(text);
        }
    }
    private void initializeCommandMap()
    {
        cm = new HashMap<>();
        cm.put("NEW",() -> createNewTab());
        cm.put("OPEN",() -> openFile());
        cm.put("SAVE",() -> saveFile());
        cm.put("SAVE AS",() -> saveAs());
        cm.put("PRINT",() -> print());
        cm.put("CLOSE",() -> closeTab());
        cm.put("CLOSE ALL",() -> closeAllTabs());
        cm.put("DELETE",() -> deleteFile());
        cm.put("EXIT",() -> System.exit(0));
        cm.put("CUT",() -> cut());
        cm.put("COPY",() -> copy());
        cm.put("PASTE",() -> paste());
        cm.put("FIND",() -> find());
        cm.put("REPLACE",()->replace());
        cm.put("DATE",() -> dateAndTime());
        cm.put("TIME",() -> dateAndTime());
        cm.put("ENABLE TOOLBAR",() -> changeToolbar(true));
        cm.put("DISABLE TOOLBAR",() -> changeToolbar(false));
        cm.put("ENABLE STATUS",() -> changeStatus(true));
        cm.put("DISABLE STATUS",() -> changeStatus(false));
        cm.put("ENABLE COMMAND",() -> changeCommand(true));
        cm.put("DISABLE COMMAND",() -> changeCommand(false));
        cm.put("TOP",() -> changeOrientation(1));
        cm.put("RIGHT",() -> changeOrientation(2));
        cm.put("BOTTOM",() -> changeOrientation(3));
        cm.put("LEFT",() -> changeOrientation(4));
        cm.put("PREFS",() -> openPreferences());
        cm.put("CONVERT",() -> System.out.println("converter"));
        cm.put("PDF",() -> System.out.println("PDF"));
        cm.put("RTF",() -> System.out.println("RTF"));
        cm.put("LATEX",() -> System.out.println("LATEX"));
        cm.put("HTML",() -> System.out.println("HTML"));
        cm.put("GOOGLE",() -> searchGoogle());
        cm.put("WIKIPEDIA",() -> searchWikipedia());
        cm.put("HELP",() -> System.out.println("Help"));
        cm.put("COMMAND HELP",() -> System.out.println("Command Help"));
        cm.put("ABOUT US",() -> System.out.println("About Us"));
        cm.put("INSERT",() -> setInsertMode());
        cm.put("NORMAL",() -> setNormalMode());
    }
    private void setRecognizerConfiguration()
    {
        URL s1 = getClass().getResource("/lmndict/6456.dict");
        URL s2 = getClass().getResource("/lmndict/6456.lm");
        configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("file:"+s1.getPath());
        configuration.setLanguageModelPath("file:"+s2.getPath());
    }
    private void setAnswerConfiguration()
    {
        URL s1 = getClass().getResource("/lmndict/9443.dict");
        URL s2 = getClass().getResource("/lmndict/9443.lm");
        a_configuration = new Configuration();
        a_configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        a_configuration.setDictionaryPath("file:"+s1.getPath());
        a_configuration.setLanguageModelPath("file:"+s2.getPath());
    }
    private void setCConfiguration()
    {
        URL s1 = getClass().getResource("/lmndict/8238.dict");
        URL s2 = getClass().getResource("/lmndict/8238.lm");
        c_configuration = new Configuration();
        c_configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        c_configuration.setDictionaryPath("file:"+s1.getPath());
        c_configuration.setLanguageModelPath("file:"+s2.getPath());
    }
    private void startRecognizer()
    {
        recognizer = null;
        System.gc();
        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException ex) {}
        SpeechResult result;
	while ((result = recognizer.getResult()) != null) {
            System.out.format("Best Final Result No Filler: %s\n", result.getResult().getBestFinalResultNoFiller());
	    System.out.format("Best Pronounciation Result: %s\n", result.getResult().getBestPronunciationResult());
	    System.out.format("Best Result: %s\n", result.getResult().getBestResultNoFiller());
            parseCommand(result.getResult().getBestFinalResultNoFiller());
        }
    }
    private void startAnswer()
    {
        try
        {
            a_recognizer = new LiveSpeechRecognizer(a_configuration);
        }catch(IOException e){}
        SpeechResult result;
        while((result = a_recognizer.getResult())!=null)
        {
            parseAnswerCommand(result.getResult().getBestFinalResultNoFiller());
        }
    }
    private void setInsertMode()
    {
        textParsingMode = true;
    }
    private void setNormalMode()
    {
        textParsingMode = false;
    }
    private void changeToolbar(boolean action)
    {   
        jCheckBoxMenuItem1.setSelected(action);
        jPanel3.setVisible(true);
    }
    private void changeStatus(boolean action)
    {
        if(!jPanel1.isVisible()){
            jPanel1.setVisible(true);
        }
        jLabel16.setVisible(action);
        jCheckBoxMenuItem3.setSelected(action);
    }
    private void changeCommand(boolean action)
    {
        if(!jPanel1.isVisible()){
            jPanel1.setVisible(true);
        }
        jTextField1.setVisible(action);
        jCheckBoxMenuItem2.setSelected(action);
    }
    private void parseAnswerCommand(String result)
    {
        if(result.equals("YES"))
            cm.get(commandKey).run();
        else
            tts.speak("action not performed", 1.5f, false, true);
        a_recognizer.stopRecognition();
        a_recognizer = null;
        System.gc();
    }
    private void initialiseCommandMap()
    {
        m = new HashMap();
        m.put("NEW","Want me to open new file?");
        m.put("OPEN","Want me to open existing file?");
        m.put("SAVE","Want me to save current file?");
        m.put("SAVE AS","Want me to save current file as something?");
        m.put("PRINT","Want me to print current file?");
        m.put("CLOSE","Want me to close current tab?");
        m.put("CLOSE ALL","Want me to close all tabs?");
        m.put("DELETE","Want me to delete current file?");
        m.put("EXIT","Want me to exit?");
        m.put("CUT","Want me to cut selected text?");
        m.put("COPY","Want me to copy selected text?");
        m.put("PASTE","Want me to paste clipboard text?");
        m.put("FIND","Want me to find something?");
        m.put("DATE","Want me to insert date?");
        m.put("TIME","Want me to insert time?");
        m.put("ENABLE TOOLBAR","Want me to enable toolbar?");
        m.put("DISABLE TOOLBAR","Want me to disable toolbar?");
        m.put("ENABLE STATUS","Want me to enable status?");
        m.put("DISABLE STATUS","Want me to disable status?");
        m.put("ENABLE COMMAND","Want me to enable command line?");
        m.put("DISABLE COMMAND","Want me to disable command line?");
        m.put("TOP","Want me to change orientation to top?");
        m.put("RIGHT","Want me to change orientation to right?");
        m.put("BOTTOM","Want me to change orientation to bottom?");
        m.put("LEFT","Want me to change orientation to left?");
        m.put("PREFS","Want me to open preferences?");
        m.put("CONVERT","Want me to open converter?");
        m.put("PDF","Want me to export your file as PDF?");
        m.put("RTF","Want me to export your file as RTF?");
        m.put("LATEX","Want me to export your file as LATEX?");
        m.put("HTML","Want me to export your file as HTML?");
        m.put("GOOGLE","Want me to search Google?");
        m.put("WIKIPEDIA","Want me to search Wikipedia?");
        m.put("HELP","Want me to open help?");
        m.put("COMMAND HELP","Want me to tell you my commands?");
        m.put("ABOUT US","Want me to tell you about myself?");
        m.put("INSERT","Want me to change to insert mode?");
        m.put("NORMAL","Want me to change to normal mode?");
    }
    private String goForTextAction(String s)
    {
        String r = "";
        boolean b;
        switch(s)
        {
            case "NEW":
                createNewTab();
                break;
            case "OPEN":
                openFile();
                break;
            case "SAVE":
                saveFile();
                break;
            case "SAVE AS":
                saveAs();
                break;
            case "RENAME":
                rename();
                break;
            case "PRINT":
                print();
                break;
            case "CLOSE":
                closeTab();
                break;
            case "CLOSE ALL":
                closeAllTabs();
                break;
            case "DELETE FILE":
                deleteFile();
                break;
            case "EXIT":
                System.exit(0);
                break;
            case "UNDO":
                undo();
                break;
            case "REDO":
                redo();
                break;
            case "CUT":
                cut();
                break;
            case "COPY":
                copy();
                break;
            case "PASTE":
                paste();
                break;
            case "DELETE":
                delete();
                break;
            case "DATE":
                dateAndTime();
                break;
            case "FIND":
                find();
                break;
            case "REPLACE":
                replace();
                break;
            case "TOOLBAR":
                b = jCheckBoxMenuItem1.isSelected();
                jCheckBoxMenuItem1.setSelected(!b);
                showToolbar();
                break;
            case "STATUS LINE":
                b = jCheckBoxMenuItem2.isSelected();
                jCheckBoxMenuItem2.setSelected(!b);
                showStatus();
                break;
            case "COMMAND LINE":
                b = jCheckBoxMenuItem3.isSelected();
                jCheckBoxMenuItem2.setSelected(!b);
                showCommandLine();
                break;
            case "TOP":
                changeOrientation(1);
                break;
            case "RIGHT":
                changeOrientation(2);
                break;
            case "BOTTOM":
                changeOrientation(3);
                break;
            case "LEFT":
                changeOrientation(4);
                break;
            case "PREFS":
                openPreferences();
                break;
            case "SHORTCUT":
                shortcutMap();
                break;
            case "GOOGLE":
                searchGoogle();
                break;
            case "WIKI":
                searchWikipedia();
                break;
            case "BROWSER":
                launchInBrowser();
                break;
        }
        return r;
    }
    private void parseCommand(String result)
    {
	String array[] = result.split(" ");
        System.out.println(array.length);
        for(int i=0;i<array.length;i++)
        {
            if(textParsingMode)
            {
                int index = jTabbedPane1.getSelectedIndex();
                String s = goForTextAction(array[i]);
                String s2 = textpanes.get(index).getText();
                s2 = s2 + s;
                textpanes.get(index).setText(s2);
            }    
            else if(m.containsKey(array[i]) && !textParsingMode){
                tts.speak(m.get(array[i]).toString(),1.5f,false,true);
                commandKey = array[i];
                if(recognizer!=null)
                    recognizer.stopRecognition();
                recognizer = null;
                System.gc();
                startAnswer();
            }
        }
        checkForMultiStringCommand(result);
        startRecognizer();
    }
    private void checkForMultiStringCommand(String result)
    {
        commandKey="";
        if(checkForSubstring(result,"ENABLE TOOLBAR"))
        {
            if(m.containsKey("ENABLE TOOLBAR")){
                tts.speak(m.get("ENABLE TOOLBAR").toString(),1.5f,false,true);
                commandKey = "ENABLE TOOLBAR";
            }
        }else if(checkForSubstring(result,"DISABLE TOOLBAR"))
        {
            if(m.containsKey("DISABLE TOOLBAR")){
                tts.speak(m.get("ENABLE TOOLBAR").toString(),1.5f,false,true);
                commandKey = "ENABLE TOOLBAR";
            }
        }else if(checkForSubstring(result,"ENABLE STATUS"))
        {
            if(m.containsKey("ENABLE STATUS")){
                tts.speak(m.get("ENABLE STATUS").toString(),1.5f,false,true);
                commandKey = "ENABLE STATUS";
            }
        }else if(checkForSubstring(result,"DISABLE STATUS"))
        {
            if(m.containsKey("DISABLE STATUS")){
                tts.speak(m.get("DISABLE STATUS").toString(),1.5f,false,true);
                commandKey = "DISABLE STATUS";
            }
        }else if(checkForSubstring(result,"ENABLE COMMAND"))
        {
            if(m.containsKey("ENABLE COMMAND")){
                tts.speak(m.get("ENABLE COMMAND").toString(),1.5f,false,true);
                commandKey = "ENABLE COMMAND";   
            }
        }else if(checkForSubstring(result,"DISABLE COMMAND"))
        {
            if(m.containsKey("DISABLE COMMAND")){
                tts.speak(m.get("DISABLE COMMAND").toString(),1.5f,false,true);  
                commandKey = "DISABLE COMMAND";
            }
        }
        if(recognizer!=null)
            recognizer.stopRecognition();
        recognizer = null;
        System.gc();
        startAnswer();
    }
    private boolean checkForSubstring(String source,String key){
        int sl = source.length();
        int kl = key.length();
        boolean retVal = false;
        for(int i = 0;i<sl;i++)
        {
            if(source.charAt(i)==key.charAt(i))
            {
                int k = 0;
                for(int j=0;j<kl;j++)
                {
                    if(source.charAt(i+j)!=key.charAt(i+j))
                        k--;
                    else
                        k++;
                }
                retVal = (k==kl);
                if(retVal)
                    return true;
            }
        }
        return retVal;
    }
    private void openPreferences(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            JTextPane p = textpanes.get(index);
            HighlightedDocument d = new HighlightedDocument();
            pfs.setVisible(true);
            String s = pfs.returnLanguage();
            switch(s){
                case "C":
                    d.setHighlightStyle(HighlightedDocument.C_STYLE);
                    break;
                case "HTML":
                    d.setHighlightStyle(HighlightedDocument.HTML_STYLE);
                    break;
                case "Java":
                    d.setHighlightStyle(HighlightedDocument.JAVA_STYLE);
                    break;
                case "Javascript":
                    d.setHighlightStyle(HighlightedDocument.JAVASCRIPT_STYLE);
                    break;
                case "Latex":
                    d.setHighlightStyle(HighlightedDocument.LATEX_STYLE);
                    break;
                case "Plain":
                    d.setHighlightStyle(HighlightedDocument.PLAIN_STYLE);
                    break;
                case "SQL":
                    d.setHighlightStyle(HighlightedDocument.SQL_STYLE);
                    break;
            }
            String text = textpanes.get(index).getText();
            p.setStyledDocument(d);
            textpanes.get(index).setText(text);
        }
    }
    private void launchInBrowser(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            try {
                Process p = Runtime.getRuntime().exec("C:\\Program Files\\Internet Explorer\\iexplore.exe \""+textpanes.get(index).getName()+"\"");
            } catch (IOException ex) {}
        }
    }
    private void changeOrientation(int r){
        switch(r){
            case 1:
                jTabbedPane1.setTabPlacement(JTabbedPane.TOP);
                break;
            case 2:
                jTabbedPane1.setTabPlacement(JTabbedPane.RIGHT);
                break;
            case 3:
                jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
                break;
            case 4:
                jTabbedPane1.setTabPlacement(JTabbedPane.LEFT);
                break;
        }
    }
    private void showToolbar(){
        boolean b = jCheckBoxMenuItem1.isSelected();
        if(b){
            jPanel3.setVisible(true);
        }else{
            jPanel3.setVisible(false);
        }
    }
    private void showCommandLine(){
        boolean b = jCheckBoxMenuItem2.isSelected();
        if(b){
            if(!jPanel1.isVisible()){
                jPanel1.setVisible(true);
            }
            jPanel2.setVisible(true);
        }else{
            if(!jCheckBoxMenuItem3.isSelected()){
                jPanel1.setVisible(false);
            }
            jPanel2.setVisible(false);
        }
    }
    private void showStatus(){
        boolean b = jCheckBoxMenuItem3.isSelected();
        if(b){
            if(!jPanel1.isVisible()){
                jPanel1.setVisible(true);
            }
            jLabel16.setVisible(true);
        }else{
            if(jCheckBoxMenuItem2.isSelected()){
                jPanel1.setVisible(false);
            }
            jLabel16.setVisible(false);
        }
    }
    private void setLocation()
    {
        int screenHeight, screenWidth, frameHeight, frameWidth, screenHeightHalf, screenWidthHalf, frameHeightHalf, frameWidthHalf;
        
        screenHeight=Toolkit.getDefaultToolkit().getScreenSize().height;
        screenWidth=Toolkit.getDefaultToolkit().getScreenSize().width;
        screenHeightHalf=screenHeight/2;
        screenWidthHalf=screenWidth/2;
        
        frameHeight=getHeight();
        frameWidth=getWidth();
        frameHeightHalf=frameHeight/2;
        frameWidthHalf=frameWidth/2;
        
        int x, y;
        x=screenWidthHalf-frameWidthHalf;
        y=screenHeightHalf-frameHeightHalf;
        
        this.setLocation(x, y);
    }
    private void searchGoogle(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            String key = textpanes.get(index).getSelectedText();
            String keys[] = key.split(" ");
            String searchKey = keys[0];
            for(int i=1;i<keys.length;i++){
                searchKey = searchKey + "+"+keys[i];
            }
            try {
                Process p = Runtime.getRuntime().exec("C:\\Program Files\\Internet Explorer\\iexplore.exe \"www.google.com/#q="+searchKey+"\"");
            } catch (IOException ex) {}            
        }
    }
    private void searchWikipedia(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            String key = textpanes.get(index).getSelectedText();
            String keys[] = key.split(" ");
            String searchKey = keys[0];
            for(int i=1;i<keys.length;i++){
                searchKey = searchKey + "_"+keys[i];
            }
            try {
                Process p = Runtime.getRuntime().exec("C:\\Program Files\\Internet Explorer\\iexplore.exe \"en.wikipedia.org/wiki/"+searchKey+"\"");
            } catch (IOException ex) {}
        }
    }
    private void find(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            new FindReplace();
        }
    }
    private void replace(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            new FindReplace();
        }
    }
    private void createNewTab()
    {
        JTextPane p = new JTextPane();
        p.addMouseListener(new PopupListener());
        p.addKeyListener(new MyKeyListener());
        textpanes.add(p);
        UndoManager m = new UndoManager();
        p.getDocument().addUndoableEditListener(m);
        undomanagers.add(m);        
        JScrollPane pn = new JScrollPane();
        pn.setViewportView(p);
        TextLineNumber tln = new TextLineNumber(p);
        tlns.add(tln);
        pn.setRowHeaderView(tln);
        jTabbedPane1.insertTab("untitled", null, pn, "new document", jTabbedPane1.getTabCount());
        initTabComponent(jTabbedPane1.getTabCount()-1);
    }
    private void rename(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            String name = textpanes.get(index).getName();
            String new_name = JOptionPane.showInputDialog(this, "New name: ", "Rename File", JOptionPane.INFORMATION_MESSAGE);
            String fileName = jTabbedPane1.getSelectedComponent().getName();
            int i = fileName.indexOf(name);
            String my_name = fileName.substring(0, i);
            my_name = my_name + new_name;
            System.out.println(my_name);
            textpanes.get(index).setName(my_name);
            saveFile();
        }
    }
    private void startRecording()
    {
       startRecognizer();
    }
    private void stopRecording()
    {
        recognizer.stopRecognition();
        recognizer = null;
        System.gc();
    }
    private void openFile()
    {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null); //replace null with your swing container
        File file = null;
        if(returnVal == JFileChooser.APPROVE_OPTION){     
            file = chooser.getSelectedFile();
            String extension = "";
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                extension = file.getName().substring(i+1);
            }
            int fileType = 0;
            if(extension.equalsIgnoreCase("c")){
                fileType = 1;
            }else if(extension.equalsIgnoreCase("html")){
                fileType = 2;
            }else if(extension.equalsIgnoreCase("java")){
                fileType = 3;
            }else if(extension.equalsIgnoreCase("js")){
                fileType = 4;
            }else if(extension.equalsIgnoreCase("sql")){
                fileType = 5;
            }else if(extension.equalsIgnoreCase("dvi")||extension.equalsIgnoreCase("pdf")){
                fileType = 6;
            }else{
                fileType = 7;
            }
            try
            {
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line = in.readLine(), s = line;
                while(line != null){
                    line = in.readLine();
                    if(line == null)
                        break;
                    s = s + "\n" + line;
                }
            
                int index = jTabbedPane1.getSelectedIndex();
                //System.out.println(index);
                if(index == -1)
                {
                    createNewTab();
                    index = jTabbedPane1.getSelectedIndex();
                }
                HighlightedDocument d = new HighlightedDocument();
                switch(fileType){
                    case 1:
                        d.setHighlightStyle(HighlightedDocument.C_STYLE);
                        break;
                    case 2:
                        d.setHighlightStyle(HighlightedDocument.HTML_STYLE);
                        break;
                    case 3:
                        d.setHighlightStyle(HighlightedDocument.JAVA_STYLE);
                        break;
                    case 4:
                        d.setHighlightStyle(HighlightedDocument.JAVASCRIPT_STYLE);
                        break;
                    case 5:
                        d.setHighlightStyle(HighlightedDocument.SQL_STYLE);
                        break;
                    case 6:
                        d.setHighlightStyle(HighlightedDocument.LATEX_STYLE);
                        break;
                    case 7:
                        d.setHighlightStyle(HighlightedDocument.PLAIN_STYLE);
                        break;
                }
                textpanes.get(index).setStyledDocument(d);
                textpanes.get(index).setText(s);
                textpanes.get(index).setName(file.getAbsolutePath());
                jTabbedPane1.setTitleAt(index, file.getName());
                in.close();
            }catch(Exception e){}
        }
    }
    private void closeTab()
    {
        int index = jTabbedPane1.getSelectedIndex();
        textpanes.remove(index);
        tlns.remove(index);
        undomanagers.remove(index);
        jTabbedPane1.remove(index);
    }
    private void saveFile()
    {
        int index = jTabbedPane1.getSelectedIndex();
        String text = textpanes.get(index).getText();
        String filepath = textpanes.get(index).getName();
        if(filepath==null){
            filepath = "untitled.txt";
        }
        try
        {
            PrintWriter writer = new PrintWriter(filepath);
            writer.println(text);
            writer.close();
        }catch(Exception e){}
    }
    private void saveAs()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this); 
        File fileToSave = null;
        int i, fileType=0;
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile();
            String extension = "";
            i = fileToSave.getName().lastIndexOf('.');
            if (i > 0) {
                extension = fileToSave.getName().substring(i+1);
            }
            if(extension.equalsIgnoreCase("c")){
                fileType = 1;
            }else if(extension.equalsIgnoreCase("html")){
                fileType = 2;
            }else if(extension.equalsIgnoreCase("java")){
                fileType = 3;
            }else if(extension.equalsIgnoreCase("js")){
                fileType = 4;
            }else if(extension.equalsIgnoreCase("sql")){
                fileType = 5;
            }else if(extension.equalsIgnoreCase("dvi")||extension.equalsIgnoreCase("pdf")){
                fileType = 6;
            }else{
                fileType = 7;
            }
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
        }
        int index = jTabbedPane1.getSelectedIndex();
        HighlightedDocument d = new HighlightedDocument();
        switch(fileType){
            case 1:
                d.setHighlightStyle(HighlightedDocument.C_STYLE);
                break;
            case 2:
                d.setHighlightStyle(HighlightedDocument.HTML_STYLE);
                break;
            case 3:
                d.setHighlightStyle(HighlightedDocument.JAVA_STYLE);
                break;
            case 4:
                d.setHighlightStyle(HighlightedDocument.JAVASCRIPT_STYLE);
                break;
            case 5:
                d.setHighlightStyle(HighlightedDocument.SQL_STYLE);
                break;
            case 6:
                d.setHighlightStyle(HighlightedDocument.LATEX_STYLE);
                break;
            case 7:
                d.setHighlightStyle(HighlightedDocument.PLAIN_STYLE);
                break;
        }
        if(index!=-1){
            textpanes.get(index).setStyledDocument(d);
            String text = textpanes.get(index).getText();
            try
            {
                PrintWriter writer = new PrintWriter(fileToSave.getAbsolutePath());
                writer.println(text);
                writer.close();
            }catch(Exception e){}
        }        
    }
    private void delete()
    {
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
        textpanes.get(index).replaceSelection("");
        }
    }
    private void selectall()
    {
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            textpanes.get(index).selectAll();
        }
    }
    private void closeAllTabs()
    {
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            textpanes.clear();
            undomanagers.clear();
            tlns.clear();
            jTabbedPane1.removeAll();
        }
    }
    private void undo(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            if(undomanagers.get(index).canUndo()){
                undomanagers.get(index).undo();
            }
        }
    }
    private void redo(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            if(undomanagers.get(index).canRedo()){
                undomanagers.get(index).redo();
            }
        }
    }
    private void initTabComponent(int i) {
        jTabbedPane1.setTabComponentAt(i, new ButtonTabComponent(jTabbedPane1));
    }
    public final void setMyCursorForContainer(Container c)
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL url=getClass().getResource("/images/Arrow.png");
        ImageIcon i = new ImageIcon(url);
        Image image = i.getImage();
        Cursor cu = toolkit.createCustomCursor(image , new Point(0,0), "img");
        c.setCursor (cu);
    }
    public final void setMyCursorForComponent(Component c)
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL url=getClass().getResource("/images/Arrow.png");
        ImageIcon i = new ImageIcon(url);
        Image image = i.getImage();
        Cursor cu = toolkit.createCustomCursor(image , new Point(0,0), "img");
        c.setCursor (cu);
    }
    private void cut(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1)
        {
            textpanes.get(index).cut();
        }
    }
    private void copy(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1)
        {
            textpanes.get(index).copy();
        }
    }
    private void print(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            try {
                textpanes.get(index).print();
            } catch (PrinterException ex) {}
        }
    }
    private void paste(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1)
        {
            textpanes.get(index).paste();
        }
    }
    private void commentIt(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            textpanes.get(index);
        }
    }
    private void deleteFile(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            String fileName = textpanes.get(index).getName();
            if(!fileName.equals("untitled")){
                File f = new File(fileName);
                f.delete();
            }
        }
    }
    private void updateStatus()
    {
        int index = jTabbedPane1.getSelectedIndex(), totalCharacters = 0, ss=0, se=0;
        String fileName = "";
        Font f = null;
        if(index!=-1){
            totalCharacters = textpanes.get(index).getText().length();
            fileName = textpanes.get(index).getName();
            ss = textpanes.get(index).getSelectionStart();
            se = textpanes.get(index).getSelectionEnd();
            f = textpanes.get(index).getFont();
        }
        jLabel16.setText("Total Characters: "+totalCharacters+" FileName: "+fileName+" Selection Start: "+ss+" Selection End: "+se+" Font Size: "+f.getSize());
    }
    class MyKeyListener extends KeyAdapter
    {
        public void keyPressed(KeyEvent e){
            updateStatus();
        }
    }
    private void uncommentIt(){
        int index = jTabbedPane1.getSelectedIndex();
        if(index!=-1){
            textpanes.get(index);
        }
    }
    public void setMyIcons()
    {
        URL url=getClass().getResource("/images/main_icon.png");
        ImageIcon icon=new ImageIcon(url);
        Image image=icon.getImage();
        this.setIconImage(image);
        
        ImageIcon II=new ImageIcon(getClass().getResource("/images/new.png"));
        jLabel2.setIcon(II);
        jLabel2.setToolTipText("create new tab");
        jLabel2.setDisplayedMnemonic('n');
        jLabel2.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/new_1.png")),1));
        
        II=new ImageIcon(getClass().getResource("/images/open.png"));
        jLabel3.setIcon(II);
        jLabel3.setToolTipText("open a file");
        jLabel3.setDisplayedMnemonic('o');
        jLabel3.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/open_1.png")),2));
        
        
        II=new ImageIcon(getClass().getResource("/images/page_text_close.png"));
        jLabel4.setIcon(II);
        jLabel4.setToolTipText("close current tab");
        jLabel4.setDisplayedMnemonic('q');
        jLabel4.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/page_text_close_1.png")),3));
        
        II=new ImageIcon(getClass().getResource("/images/closeall.png"));
        jLabel5.setIcon(II);
        jLabel5.setToolTipText("close all tabs");
        jLabel5.setDisplayedMnemonic('r');
        jLabel5.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/closeall_1.png")),4));
        
        II=new ImageIcon(getClass().getResource("/images/save.png"));
        jLabel6.setIcon(II);
        jLabel6.setToolTipText("save current contents of tab");
        jLabel6.setDisplayedMnemonic('s');
        jLabel6.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/save_1.png")),5));
        
        II=new ImageIcon(getClass().getResource("/images/save_as.png"));
        jLabel7.setIcon(II);
        jLabel7.setToolTipText("save as some other file");
        jLabel7.setDisplayedMnemonic('d');
        jLabel7.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/save_as_1.png")),6));
        
        II=new ImageIcon(getClass().getResource("/images/cut.png"));
        jLabel8.setIcon(II);
        jLabel8.setToolTipText("cut selected text");
        jLabel8.setDisplayedMnemonic('x');
        jLabel8.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/cut_1.png")),7));
        
        
        II=new ImageIcon(getClass().getResource("/images/copy.png"));
        jLabel9.setIcon(II);
        jLabel9.setToolTipText("copy selected text");
        jLabel9.setDisplayedMnemonic('c');
        jLabel9.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/copy_1.png")),8));
        
        II=new ImageIcon(getClass().getResource("/images/paste.png"));
        jLabel10.setIcon(II);
        jLabel10.setToolTipText("paste text");
        jLabel10.setDisplayedMnemonic('v');
        jLabel10.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/paste_1.png")),9));
        
        II=new ImageIcon(getClass().getResource("/images/undo.png"));
        jLabel11.setIcon(II);
        jLabel11.setToolTipText("undo recent action");
        jLabel11.setDisplayedMnemonic('z');
        jLabel11.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/undo_1.png")),10));
        
        II=new ImageIcon(getClass().getResource("/images/redo.png"));
        jLabel12.setIcon(II);
        jLabel12.setToolTipText("redo recent action");
        jLabel12.setDisplayedMnemonic('y');
        jLabel12.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/redo_1.png")),11));
        
        II=new ImageIcon(getClass().getResource("/images/printer.png"));
        jLabel13.setIcon(II);
        jLabel13.setToolTipText("print current file");
        jLabel13.setDisplayedMnemonic('p');
        jLabel13.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/printer_1.png")),12));
        
        II=new ImageIcon(getClass().getResource("/images/start_rec.png"));
        jLabel14.setIcon(II);
        jLabel14.setToolTipText("start recording");
        jLabel14.setDisplayedMnemonic('+');
        jLabel14.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/start_rec_1.png")),13));
        
        II=new ImageIcon(getClass().getResource("/images/stop_rec.png"));
        jLabel15.setIcon(II);
        jLabel15.setToolTipText("stop recording");
        jLabel15.setDisplayedMnemonic('-');
        jLabel15.addMouseListener(new IconMouseListener(II,new ImageIcon(getClass().getResource("/images/stop_rec_1.png")),14));
    }
    
    class FindReplace extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextPane jTextPane;
	private int findPosn = 0; 
	/** the last text searched for */ 
	private String findText = null; 
	/** case sensitive find/replace */ 
	private boolean findCase = false; 
	/** user must confirm text replacement */ 
	private boolean replaceConfirm = true; 
	private JTextField Find_TextField, Replace_TextField;
        private int index;
	/**
	 * Launch the application.
	 */
	
	/**
	 * Create the frame.
	 */
	public FindReplace() {
            int t = jTabbedPane1.getSelectedIndex();
            jTextPane = textpanes.get(t);
            
            setTitle("Find Replace");            
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setBounds(100, 100, 440, 215);
            this.setLayout(new BorderLayout());
            JPanel p = new JPanel();
            p.setLayout(new GridLayout(3,2,10,10));
            JLabel lblNewLabel = new JLabel("Find What");
            //lblNewLabel.setBounds(10, 54, 75, 23);

            Find_TextField = new JTextField();
            //Find_TextField.setBounds(73, 48, 183, 34);
            Find_TextField.setColumns(10);
            
            JLabel rplLabel = new JLabel("Replace With");
            //rplLabel.setBounds(10,54,75,23);

            Replace_TextField = new JTextField();
            //Replace_TextField.setBounds(73, 48, 183, 34);
            Replace_TextField.setColumns(10);
            
            JButton Find = new JButton("Find");
            Find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
            });
            //Find.setBounds(312, 32, 89, 23);

            JButton Replace = new JButton("Replace");
            //Replace.setBounds(312,32,89,23);

            ButtonGroup bg=new ButtonGroup();
            JRadioButton Font_Radio = new JRadioButton("UP");
            //Font_Radio.setBounds(154, 129, 53, 23);
            bg.add(Font_Radio);

	    JRadioButton Font_Radio2 = new JRadioButton("Down");
            //Font_Radio2.setBounds(209, 129, 109, 23);
            bg.add(Font_Radio2);
    
            JCheckBox Find_CheckBox = new JCheckBox("Match Case");
            //Find_CheckBox.setBounds(10, 147, 97, 23);
        
            //JButton Find_Cancel = new JButton("Cancel");
            //Find_Cancel.setBounds(312, 80, 89, 23);
            p.add(lblNewLabel);
            p.add(Find_TextField);
            p.add(Find);
            p.add(rplLabel);
            p.add(Replace_TextField);
            p.add(Replace);
            p.add(Font_Radio);
            p.add(Find_CheckBox);
            p.add(Font_Radio2);
            //p.add(Find_Cancel);
            getContentPane().add(p,BorderLayout.CENTER);
            Find.addActionListener(this);
            //Find_Cancel.addActionListener(this);
            Replace.addActionListener(this);
            setVisible(true);
	}
	  /** * finds next occurrence of text in a string 
	  * @param find the text to locate in the string */ 
	public void doFindText(String find)
        { 
            int nextPosn = 0; 
            if (!find.equals(findText) ) // *** new find word 
                findPosn = 0; // *** start from top 
            nextPosn = nextIndex( jTextPane.getText(), find, findPosn, findCase );
            if ( nextPosn >= 0 ) 
            { 
		int l=getLineNumber(jTextPane,nextPosn+find.length());
		System.out.print(l);
                jTextPane.setSelectionStart( nextPosn-l); // position cursor at word start 
                jTextPane.setSelectionEnd( nextPosn+ find.length()-l+1);	 
                findPosn = nextPosn + find.length()+1; // reset for next search 
                findText = find; // save word & case 
                }
            else 
            { 
                findPosn = nextPosn; // set to -1 if not found 
                JOptionPane.showMessageDialog(null, find + " not Found!" ); 
            } 
	} 
	/** finds and replaces <B>ALL</B> occurrences of text in a string 
	* @param find the text to locate in the string 
	* @param replace the text to replace the find text with - if the find 
	text exists */ 
	public void doReplaceWords(String find, String replace)
        { 
            int nextPosn = 0; 
            StringBuffer str = new StringBuffer(); 
            findPosn = 0; // *** begin at start of text 
            while (nextPosn >= 0) { 
                nextPosn = nextIndex( jTextPane.getText(), find, findPosn, findCase ); 
                if ( nextPosn >= 0 ) { // if text is found 
                    int rtn = JOptionPane.YES_OPTION; // default YES for confirm 
                    jTextPane.grabFocus(); 
                    jTextPane.setSelectionStart( nextPosn ); // posn cursor at word start 
                    jTextPane.setSelectionEnd( nextPosn + find.length() ); //select found text 	 
                    if ( replaceConfirm ) { // user replace confirmation 
                        rtn = JOptionPane.showConfirmDialog(null, "Found: " + find + "\nReplace with: " + replace, "Text Find & Replace", JOptionPane.YES_NO_CANCEL_OPTION); 
                    } 
                    // if don't want confirm or selected yes 
                    if ( !replaceConfirm || rtn == JOptionPane.YES_OPTION ) { 
                        jTextPane.replaceSelection(replace); 
                    } else if ( rtn == javax.swing.JOptionPane.CANCEL_OPTION ) 
                        return; // cancelled replace - exit method 
                    findPosn = nextPosn + find.length(); // set for next search 
                } 
            } 
	} 
	public int getLineNumber(JTextPane component, int pos) 
	{
            int posLine;
	    int y = 0;
	    try
	    {
                Rectangle caretCoords = component.modelToView(pos);
                y = (int) caretCoords.getY();
	    }
	    catch (BadLocationException ex)
	    {
	    }
	    int lineHeight = component.getFontMetrics(component.getFont()).getHeight();
	    posLine = (y / lineHeight) + 1;
	    return posLine;
        }
	  /** returns next posn of word in text - forward search 
	  * @return next indexed position of start of found text or -1 
	  * @param input the string to search 
	  * @param find the string to find 
	  * @param start the character position to start the search 
	  * @param caseSensitive true for case sensitive. false to ignore case 
	  */ 
	public int nextIndex(String input, String find, int start, boolean caseSensitive ) { 
            int textPosn = -1; 
            if ( input != null && find != null && start < input.length() ) { 
                if ( caseSensitive == true ) { // indexOf() returns -1 if not found 
                    textPosn = input.indexOf( find, start ); 
                } else { 
                    textPosn = input.toLowerCase().indexOf( find.toLowerCase(), start ); 
                } 
            } 
            return textPosn; 
	}
	@Override
	public void actionPerformed(ActionEvent e){
            JButton b = (JButton)e.getSource();
            System.out.println(b.getText());
            if(b.getText().equals("Cancel"))
            {
                this.setVisible(false);
                this.dispose();
            }
            else if(b.getText().equals("Find"))
            {
                doFindText(Find_TextField.getText());
            }
            else if(b.getText().equals("Replace"))
            {
                System.out.println("Replcae");
                doReplaceWords(Find_TextField.getText(),Replace_TextField.getText());
            }
	}
    }
    class TextLineNumber extends JPanel	implements CaretListener, DocumentListener, PropertyChangeListener
    {
	public final static float LEFT = 0.0f;
	public final static float CENTER = 0.5f;
	public final static float RIGHT = 1.0f;
	private final Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);
	private final static int HEIGHT = Integer.MAX_VALUE - 1000000;
	//  Text component this TextTextLineNumber component is in sync with
	private JTextComponent component;
	//  Properties that can be changed
	private boolean updateFont;
	private int borderGap;
	private Color currentLineForeground;
	private float digitAlignment;
	private int minimumDisplayDigits;
	//  Keep history information to reduce the number of times the component
	//  needs to be repainted
        private int lastDigits;
        private int lastHeight;
        private int lastLine;
	private HashMap<String, FontMetrics> fonts;
	/**
	 *	Create a line number component for a text component. This minimum
	 *  display width will be based on 3 digits.
	 *
	 *  @param component  the related text component
	 */
	public TextLineNumber(JTextComponent component)
	{
		this(component, 6);
	}
	/**
	 *	Create a line number component for a text component.
	 *
	 *  @param component  the related text component
	 *  @param minimumDisplayDigits  the number of digits used to calculate
	 *                               the minimum width of the component
	 */
	public TextLineNumber(JTextComponent component, int minimumDisplayDigits)
	{
		this.component = component;
		setFont(new Font(Font.SANS_SERIF,1,10));
		setBorderGap( 1 );
		setCurrentLineForeground( Color.RED );
		setDigitAlignment(RIGHT -1);
		setMinimumDisplayDigits( minimumDisplayDigits );
		component.getDocument().addDocumentListener(this);
		component.addCaretListener( this );
		component.addPropertyChangeListener("font", this);
	}
	/**
	 *  Gets the update font property
	 *
	 *  @return the update font property
	 */
	public boolean getUpdateFont()
	{
            return updateFont;
	}
	/**
	 *  Set the update font property. Indicates whether this Font should be
	 *  updated automatically when the Font of the related text component
	 *  is changed.
	 *
	 *  @param updateFont  when true update the Font and repaint the line
	 *                     numbers, otherwise just repaint the line numbers.
	 */
	public void setUpdateFont(boolean updateFont)
	{
            this.updateFont = updateFont;
	}
	/**
	 *  Gets the border gap
	 *
	 *  @return the border gap in pixels
	 */
	public int getBorderGap()
	{
            return borderGap;
	}
	/**
	 *  The border gap is used in calculating the left and right insets of the
	 *  border. Default value is 5.
	 *
	 *  @param borderGap  the gap in pixels
	 */
	public void setBorderGap(int borderGap)
	{
            this.borderGap = borderGap;
            Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
            setBorder( new CompoundBorder(OUTER, inner) );
            lastDigits = 0;
            setPreferredWidth();
	}
	/**
	 *  Gets the current line rendering Color
	 *
	 *  @return the Color used to render the current line number
	 */
	public Color getCurrentLineForeground()
	{
            return currentLineForeground == null ? getForeground() : currentLineForeground;
	}
	/**
	 *  The Color used to render the current line digits. Default is Coolor.RED.
	 *
	 *  @param currentLineForeground  the Color used to render the current line
	 */
	public void setCurrentLineForeground(Color currentLineForeground)
	{
            this.currentLineForeground = currentLineForeground;
	}
	/**
	 *  Gets the digit alignment
	 *
	 *  @return the alignment of the painted digits
	 */
	public float getDigitAlignment()
	{
            return digitAlignment;
	}
	/**
	 *  Specify the horizontal alignment of the digits within the component.
	 *  Common values would be:
	 *  <ul>
	 *  <li>TextLineNumber.LEFT
	 *  <li>TextLineNumber.CENTER
	 *  <li>TextLineNumber.RIGHT (default)
	 *	</ul>
	 *  @param currentLineForeground  the Color used to render the current line
	 */
	public void setDigitAlignment(float digitAlignment)
	{
            this.digitAlignment = digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
	}
	/**
	 *  Gets the minimum display digits
	 *
	 *  @return the minimum display digits
	 */
	public int getMinimumDisplayDigits()
	{
            return minimumDisplayDigits;
	}
	/**
	 *  Specify the minimum number of digits used to calculate the preferred
	 *  width of the component. Default is 3.
	 *
	 *  @param minimumDisplayDigits  the number digits used in the preferred
	 *                               width calculation
	 */
	public void setMinimumDisplayDigits(int minimumDisplayDigits)
	{
            this.minimumDisplayDigits = minimumDisplayDigits;
            setPreferredWidth();
	}
	/**
	 *  Calculate the width needed to display the maximum line number
	 */
	private void setPreferredWidth()
	{
            Element root = component.getDocument().getDefaultRootElement();
            int lines = root.getElementCount();
            int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);
            //  Update sizes when number of digits in the line number changes
            if (lastDigits != digits)
            {
                lastDigits = digits;
		FontMetrics fontMetrics = getFontMetrics( getFont() );
		int width = fontMetrics.charWidth( '0' ) * digits;
		Insets insets = getInsets();
		int preferredWidth = insets.left + insets.right + width;
		Dimension d = getPreferredSize();
		d.setSize(preferredWidth, HEIGHT);
		setPreferredSize( d );
		setSize( d );
            }
	}
	/**
	 *  Draw the line numbers
	 */
	@Override
	public void paintComponent(Graphics g)
	{
            super.paintComponent(g);
            //	Determine the width of the space available to draw the line number
            FontMetrics fontMetrics = component.getFontMetrics( component.getFont() );
            Insets insets = getInsets();
            int availableWidth = getSize().width - insets.left - insets.right;
    	//  Determine the rows to draw within the clipped bounds.
            Rectangle clip = g.getClipBounds();
            int rowStartOffset = component.viewToModel( new Point(0, clip.y) );
            int endOffset = component.viewToModel( new Point(0, clip.y + clip.height) );
            while (rowStartOffset <= endOffset)
            {
                try
                {
                    if (isCurrentLine(rowStartOffset))
                        g.setColor( getCurrentLineForeground() );
                    else
    			g.setColor( getForeground() );
    			//  Get the line number as a string and then determine the
    			//  "X" and "Y" offsets for drawing the string.
    			String lineNumber = getTextLineNumber(rowStartOffset);
    			int stringWidth = fontMetrics.stringWidth( lineNumber );
    			int x = getOffsetX(availableWidth, stringWidth) + insets.left;
    			int y = getOffsetY(rowStartOffset, fontMetrics);
    			g.drawString(lineNumber, x, y);
    			//  Move to the next row
    			rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
		}
		catch(Exception e) {break;}
            }
	}
	/*
	 *  We need to know if the caret is currently positioned on the line we
	 *  are about to paint so the line number can be highlighted.
	 */
	private boolean isCurrentLine(int rowStartOffset)
	{
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		if (root.getElementIndex( rowStartOffset ) == root.getElementIndex(caretPosition))
                    return true;
		else
                    return false;
	}
	/*
	 *	Get the line number to be drawn. The empty string will be returned
	 *  when a line of text has wrapped.
	 */
	protected String getTextLineNumber(int rowStartOffset)
	{
		Element root = component.getDocument().getDefaultRootElement();
		int index = root.getElementIndex( rowStartOffset );
		Element line = root.getElement( index );
		if (line.getStartOffset() == rowStartOffset)
                    return String.valueOf(index + 1);
		else
                    return "";
	}
	/*
	 *  Determine the X offset to properly align the line number when drawn
	 */
	private int getOffsetX(int availableWidth, int stringWidth)
	{
            return (int)((availableWidth - stringWidth) * digitAlignment);
	}
	/*
	 *  Determine the Y offset for the current row
	 */
	private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics) throws BadLocationException
	{
            //  Get the bounding rectangle of the row
            Rectangle r = component.modelToView( rowStartOffset );
            int lineHeight = fontMetrics.getHeight();
            int y = r.y + r.height;
            int descent = 0;
            //  The text needs to be positioned above the bottom of the bounding
            //  rectangle based on the descent of the font(s) contained on the row.
            if (r.height == lineHeight)  // default font is being used
            {
                descent = fontMetrics.getDescent();
            }
            else  // We need to check all the attributes for font changes
            {
                if (fonts == null)
                    fonts = new HashMap<String, FontMetrics>();
                Element root = component.getDocument().getDefaultRootElement();
                int index = root.getElementIndex( rowStartOffset );
                Element line = root.getElement( index );
                for (int i = 0; i < line.getElementCount(); i++)
                {
                    Element child = line.getElement(i);
                    AttributeSet as = child.getAttributes();
                    String fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
                    Integer fontSize = (Integer)as.getAttribute(StyleConstants.FontSize);
                    String key = fontFamily + fontSize;
                    FontMetrics fm = fonts.get( key );
                    if (fm == null)
                    {
                        Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                        fm = component.getFontMetrics( font );
                        fonts.put(key, fm);
                    }
                    descent = Math.max(descent, fm.getDescent());
                }
            }
            return y - descent;
	}
//
//  Implement CaretListener interface
//
	@Override
	public void caretUpdate(CaretEvent e)
	{
            //  Get the line the caret is positioned on
            int caretPosition = component.getCaretPosition();
            Element root = component.getDocument().getDefaultRootElement();
            int currentLine = root.getElementIndex( caretPosition );
            //  Need to repaint so the correct line number can be highlighted
            if (lastLine != currentLine)
            {
                repaint();
		lastLine = currentLine;
            }            
            setPreferredWidth();
	}
//
//  Implement DocumentListener interface
//
	@Override
	public void changedUpdate(DocumentEvent e)
	{
            documentChanged();
	}
	@Override
	public void insertUpdate(DocumentEvent e)
	{
            documentChanged();
	}
	@Override
	public void removeUpdate(DocumentEvent e)
	{
            documentChanged();
	}
	/*
	 *  A document change may affect the number of displayed lines of text.
	 *  Therefore the lines numbers will also change.
	 */
	private void documentChanged()
	{
            //  View of the component has not been updated at the time
            //  the DocumentEvent is fired
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
		public void run()
		{
                    try
                    {
                        int endPos = component.getDocument().getLength();
                        Rectangle rect = component.modelToView(endPos);
			if (rect != null && rect.y != lastHeight)
			{
                            setPreferredWidth();
                            repaint();
                            lastHeight = rect.y;
                        }
                    }
                    catch (BadLocationException ex) { /* nothing to do */ }
		}
            });
	}
//
//  Implement PropertyChangeListener interface
//
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
            if (evt.getNewValue() instanceof Font)
            {
            	if (updateFont)
            	{
                    Font newFont = (Font) evt.getNewValue();
                    Font f = new Font(newFont.getName(),newFont.getStyle(),10);
                    setFont(f);
                    lastDigits = 0;
                    setPreferredWidth();
		}
		else
		{
                    repaint();
		}
            }
	}
    }
    class IconMouseListener implements MouseListener
    {
        private ImageIcon one, two;
        private JLabel l;
        private int buttonNumber;
        public IconMouseListener(ImageIcon i,ImageIcon i2,int bn)
        {
            one = i;
            two = i2;
            buttonNumber = bn;
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            switch(buttonNumber)
            {
                case 1:
                    createNewTab();
                    break;
                case 2:
                    openFile();
                    break;
                case 3:
                    closeTab();
                    break;
                case 4:
                    closeAllTabs();
                    break;
                case 5:
                    saveFile();
                    break;    
                case 6:
                    saveAs();
                    break;
                case 7:
                    cut();
                    break;
                case 8:
                    copy();
                    break;    
                case 9:
                    paste();
                    break;    
                case 10:
                    undo();
                    break;    
                case 11:
                    redo();
                    break;    
                case 12:
                    print();
                    break;    
                case 13:
                    startRecording();
                    break;    
                case 14:
                    stopRecording();
                    break;                
            }
            updateStatus();
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            l = (JLabel)e.getSource();
            l.setIcon(two);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            l = (JLabel)e.getSource();
            l.setIcon(one);
        }
    }
    private void shortcutMap(){
        shortcut_mapper m = new shortcut_mapper();
        m.setVisible(true);
    }
    
    class shortcut_mapper extends javax.swing.JFrame
    {
        public KeyStroke myStroke;
        public int stroker = 0;
        class LabelListener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e){
                shortcut_mapper.shortcut_creator c;
                c = new shortcut_mapper.shortcut_creator();
                c.setVisible(true);
                JLabel l = (JLabel)e.getSource();
                String ch = l.getName();
                stroker = Integer.parseInt(ch);
                System.out.println("S: "+stroker);
            }
        }
        class shortcut_creator extends javax.swing.JFrame {
            public shortcut_creator() {
                initComponents();
                setLocation();
                initialiseComboBox();  
                setIcons();
            }
            public void setIcons(){
                URL u=getClass().getResource("/images/shortcut_mapper.png");
                ImageIcon icon=new ImageIcon(u);
                Image imag=icon.getImage();
                setIconImage(imag);
                
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                URL url=getClass().getResource("/images/Arrow.png");
                ImageIcon i = new ImageIcon(url);
                Image image = i.getImage();
                Cursor cu = toolkit.createCustomCursor(image , new Point(0,0), "img");
                setCursor (cu);
            }
            public KeyStroke getKeyStroke(String s){
                KeyStroke k = null;
                int ctrl = 0, shift = 0, alt = 0, key = 0; 
                if(jCheckBox1.isSelected()){
                    ctrl = 1;
                }else{
                    ctrl = 0;
                }
                if(jCheckBox2.isSelected()){
                    shift = 1;
                }else{
                    shift = 0;
                }
                if(jCheckBox3.isSelected()){
                    alt = 1;
                }else{
                    alt = 0;
                }
                switch(s){
                    case "A":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_A,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "B":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_B,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "C":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_C,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "D":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_D,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "E":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_E,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "G":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_G,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "H":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_H,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "I":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_I,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "J":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_J,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "K":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_K,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "L":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_L,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "M":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_M,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "N":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_N,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "O":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_O,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "P":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_P,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "Q":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_Q,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "R":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_R,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "S":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_S,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "T":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_T,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "U":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_U,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;                
                    case "V":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_V,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "W":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_W,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "X":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_X,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "Y":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_Y,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "Z":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_Z,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "0":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_0,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "1":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_1,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "2":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_2,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "3":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_3,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "4":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_4,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "5":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_5,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;                
                    case "6":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_6,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "7":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_7,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "8":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_8,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "9":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_9,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F1":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F1,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F2":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F2,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;                
                    case "F3":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F3,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F4":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F4,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F5":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F5,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F6":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F6,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F7":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F7,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F8":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F8,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F9":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F9,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F10":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F10,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F11":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F11,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "F12":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_F12,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "+":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_ADD,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "-":  
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_MINUS,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "*":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_MULTIPLY,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                    case "/":
                        k = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_DIVIDE,(ctrl==1?InputEvent.CTRL_MASK:0)|(alt==1?InputEvent.ALT_MASK:0)|(shift==1?InputEvent.SHIFT_MASK:0));
                        break;
                }       
                return k;
            }
            private void setLocation()
            {
                int screenHeight, screenWidth, frameHeight, frameWidth, screenHeightHalf, screenWidthHalf, frameHeightHalf, frameWidthHalf;        
                screenHeight=Toolkit.getDefaultToolkit().getScreenSize().height;
                screenWidth=Toolkit.getDefaultToolkit().getScreenSize().width;
                screenHeightHalf=screenHeight/2;
                screenWidthHalf=screenWidth/2;
                frameHeight=getHeight();
                frameWidth=getWidth();
                frameHeightHalf=frameHeight/2;
                frameWidthHalf=frameWidth/2;
                int x, y;
                x=screenWidthHalf-frameWidthHalf;
                y=screenHeightHalf-frameHeightHalf;
                this.setLocation(x, y);
            }
            private void initialiseComboBox(){
                jComboBox1.removeAllItems();
                jComboBox1.addItem("A");
                jComboBox1.addItem("B");
                jComboBox1.addItem("C");
                jComboBox1.addItem("D");
                jComboBox1.addItem("E");
                jComboBox1.addItem("F");
                jComboBox1.addItem("G");
                jComboBox1.addItem("H");
                jComboBox1.addItem("I");
                jComboBox1.addItem("J");
                jComboBox1.addItem("K");
                jComboBox1.addItem("L");
                jComboBox1.addItem("M");
                jComboBox1.addItem("N");
                jComboBox1.addItem("O");
                jComboBox1.addItem("P");
                jComboBox1.addItem("Q");
                jComboBox1.addItem("R");
                jComboBox1.addItem("S");
                jComboBox1.addItem("T");
                jComboBox1.addItem("U");
                jComboBox1.addItem("V");
                jComboBox1.addItem("W");
                jComboBox1.addItem("X");
                jComboBox1.addItem("Y");
                jComboBox1.addItem("Z");
        
                jComboBox1.addItem("0");
                jComboBox1.addItem("1");
                jComboBox1.addItem("2");
                jComboBox1.addItem("3");
                jComboBox1.addItem("4");
                jComboBox1.addItem("5");
                jComboBox1.addItem("6");
                jComboBox1.addItem("7");
                jComboBox1.addItem("8");
                jComboBox1.addItem("9");
        
                jComboBox1.addItem("F1");
                jComboBox1.addItem("F2");
                jComboBox1.addItem("F3");
                jComboBox1.addItem("F4");
                jComboBox1.addItem("F5");
                jComboBox1.addItem("F6");
                jComboBox1.addItem("F7");
                jComboBox1.addItem("F8");
                jComboBox1.addItem("F9");
                jComboBox1.addItem("F10");
                jComboBox1.addItem("F11");
                jComboBox1.addItem("F12");
        
                jComboBox1.addItem("+");
                jComboBox1.addItem("-");
                jComboBox1.addItem("*");
                jComboBox1.addItem("/");
            }
            private void initComponents() {
                jComboBox1 = new javax.swing.JComboBox<>();
                jCheckBox1 = new javax.swing.JCheckBox();
                jCheckBox2 = new javax.swing.JCheckBox();
                jCheckBox3 = new javax.swing.JCheckBox();
                jButton1 = new javax.swing.JButton();
                setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                setTitle("Shortcut Creator");
                setAlwaysOnTop(true);
                setType(java.awt.Window.Type.UTILITY);
                getContentPane().setLayout(new java.awt.GridLayout(1, 5, 10, 10));
                jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
                getContentPane().add(jComboBox1);
                jCheckBox1.setText("Ctrl");
                getContentPane().add(jCheckBox1);
                jCheckBox2.setText("Alt");
                getContentPane().add(jCheckBox2);
                jCheckBox3.setText("Shift");
                getContentPane().add(jCheckBox3);
                jButton1.setText("Done");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jButton1ActionPerformed(evt);
                    }
                });
                getContentPane().add(jButton1);
                getAccessibleContext().setAccessibleDescription("Creates shortcut");
                pack();
            }
            private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
                String s = jComboBox1.getSelectedItem().toString();
                k = getKeyStroke(s);
                System.out.println("Inside shortcut_creator: "+k);
                myStroke = k;
                disposeIt();
            }
            public void disposeIt(){
                switch(stroker){
                    case 1:
                        jMenuItem9.setAccelerator(myStroke);
                        break;
                    case 2:
                        jMenuItem10.setAccelerator(myStroke);
                        break;
                    case 3:
                        jMenuItem11.setAccelerator(myStroke);
                        break;
                    case 4:
                        jMenuItem12.setAccelerator(myStroke);
                        break;
                    case 5:
                        jMenuItem13.setAccelerator(myStroke);
                        break;
                    case 6:
                        jMenuItem26.setAccelerator(myStroke);
                        break;
                    case 7:
                        jMenuItem14.setAccelerator(myStroke);
                        break;
                    case 8:
                        jMenuItem15.setAccelerator(myStroke);
                        break;
                    case 9:
                        jMenuItem27.setAccelerator(myStroke);
                        break;
                    case 10:
                        jMenuItem16.setAccelerator(myStroke);
                        break;
                    case 11:
                        jMenuItem17.setAccelerator(myStroke);
                        break;
                    case 12:
                        jMenuItem18.setAccelerator(myStroke);
                        break;
                    case 13:  
                        jMenuItem19.setAccelerator(myStroke);
                        break;
                    case 14:  
                        jMenuItem20.setAccelerator(myStroke);
                        break;
                    case 15:  
                        jMenuItem21.setAccelerator(myStroke);
                        break;
                    case 16:  
                        jMenuItem22.setAccelerator(myStroke);
                        break;
                    case 17:  
                        jMenuItem23.setAccelerator(myStroke);
                        break;
                    case 18:  
                        jMenuItem24.setAccelerator(myStroke);
                        break;
                    case 19:  
                        jMenuItem25.setAccelerator(myStroke);
                        break;
                    case 20:  
                        jCheckBoxMenuItem1.setAccelerator(myStroke);
                        break;
                    case 21:  
                        jCheckBoxMenuItem2.setAccelerator(myStroke);
                        break;
                    case 22:  
                        jCheckBoxMenuItem3.setAccelerator(myStroke);
                        break;
                    case 23:  
                        jMenuItem28.setAccelerator(myStroke);
                        break;
                    case 24:  
                        jMenuItem29.setAccelerator(myStroke);
                        break;
                    case 25:  
                        jMenuItem30.setAccelerator(myStroke);
                        break;
                    case 26:  
                        jMenuItem31.setAccelerator(myStroke);
                        break;
                    case 27:  
                        jMenuItem32.setAccelerator(myStroke);
                        break;
                    case 28:  
                        jMenuItem34.setAccelerator(myStroke);
                        break;
                    case 29:  
                        jMenuItem36.setAccelerator(myStroke);
                        break;
                    case 30:  
                        jMenuItem35.setAccelerator(myStroke);
                        break;
                    case 31:  
                        jMenuItem37.setAccelerator(myStroke);
                        break;
                    case 32:  
                        jMenuItem38.setAccelerator(myStroke);
                        break;
                    case 33:  
                        jMenuItem39.setAccelerator(myStroke);
                        break;
                    case 34:  
                        jMenuItem40.setAccelerator(myStroke);
                        break;
                    case 35:  
                        jMenuItem41.setAccelerator(myStroke);
                        break;
                    case 36:  
                        jMenuItem42.setAccelerator(myStroke);
                        break;
                    case 37:  
                        jMenuItem43.setAccelerator(myStroke);
                        break;
                    case 38:  
                        jMenuItem45.setAccelerator(myStroke);
                        break;
                    case 39:
                        jMenuItem44.setAccelerator(myStroke);
                        break;
                }
                this.dispose();
            }
            private javax.swing.JButton jButton1;
            private javax.swing.JCheckBox jCheckBox1;
            private javax.swing.JCheckBox jCheckBox2;
            private javax.swing.JCheckBox jCheckBox3;
            private javax.swing.JComboBox<String> jComboBox1;            
        }
        public shortcut_mapper() {
            initComponents();
            reloadLabels();
            borderLabels();
            setLocation();
            
            URL u=getClass().getResource("/images/shortcut_mapper.png");
            ImageIcon icon=new ImageIcon(u);
            Image imag=icon.getImage();
            setIconImage(imag);
            
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            URL url=getClass().getResource("/images/Arrow.png");
            ImageIcon i = new ImageIcon(url);
            Image image = i.getImage();
            Cursor cu = toolkit.createCustomCursor(image , new Point(0,0), "img");
            setCursor (cu);
            
            
            setTitle("Shortcut Mapper");
            setExtendedState(JFrame.MAXIMIZED_VERT);
            setPreferredSize(new Dimension(400,600));
            setSize(new Dimension(400,600));
            setMaximumSize(new Dimension(400,600));
            setMinimumSize(new Dimension(400,600));
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        } 
        private void setLocation()
        {
            int screenHeight, screenWidth, frameHeight, frameWidth, screenHeightHalf, screenWidthHalf, frameHeightHalf, frameWidthHalf;        
            screenHeight=Toolkit.getDefaultToolkit().getScreenSize().height;
            screenWidth=Toolkit.getDefaultToolkit().getScreenSize().width;
            screenHeightHalf=screenHeight/2;
            screenWidthHalf=screenWidth/2;
        
            frameHeight=getHeight();
            frameWidth=getWidth();
            frameHeightHalf=frameHeight/2;
            frameWidthHalf=frameWidth/2;
        
            int x, y;
            x=screenWidthHalf-frameWidthHalf;
            y=screenHeightHalf-frameHeightHalf;
        
            this.setLocation(x, y);
        }
        private void borderLabels(){
            jLabel1.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel2.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel3.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel4.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel5.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel6.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel7.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel8.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel9.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel10.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel11.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel12.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel13.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel14.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel15.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel16.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel17.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel18.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel19.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel20.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel21.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel22.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel23.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel24.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel25.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel26.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel27.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel28.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel29.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel30.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel31.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel32.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel33.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel34.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel35.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel36.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel37.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel38.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel39.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            
            jLabel51.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel51.addMouseListener(new LabelListener());
            jLabel51.setName("1");
            
            jLabel52.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel52.addMouseListener(new LabelListener());
            jLabel52.setName("2");
            
            jLabel53.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel53.addMouseListener(new LabelListener());
            jLabel53.setName("3");
            
            jLabel54.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel54.addMouseListener(new LabelListener());
            jLabel54.setName("4");
            
            jLabel55.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel55.addMouseListener(new LabelListener());
            jLabel55.setName("5");
            
            jLabel56.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel56.addMouseListener(new LabelListener());
            jLabel56.setName("6");
            
            jLabel57.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel57.addMouseListener(new LabelListener());
            jLabel57.setName("7");
            
            jLabel58.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel58.addMouseListener(new LabelListener());
            jLabel58.setName("8");
            
            jLabel59.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel59.addMouseListener(new LabelListener());
            jLabel59.setName("9");
            
            jLabel60.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel60.addMouseListener(new LabelListener());
            jLabel60.setName("10");
            
            jLabel61.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel61.addMouseListener(new LabelListener());
            jLabel61.setName("11");
            
            jLabel62.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel62.addMouseListener(new LabelListener());
            jLabel62.setName("12");
            
            jLabel63.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel63.addMouseListener(new LabelListener());
            jLabel63.setName("13");
            
            jLabel64.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel64.addMouseListener(new LabelListener());
            jLabel64.setName("14");
            
            jLabel65.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel65.addMouseListener(new LabelListener());
            jLabel65.setName("15");
            
            jLabel66.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel66.addMouseListener(new LabelListener());
            jLabel66.setName("16");
            
            jLabel67.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel51.addMouseListener(new LabelListener());
            jLabel67.setName("17");
            
            jLabel68.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel68.addMouseListener(new LabelListener());
            jLabel68.setName("18");
            
            jLabel69.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel69.addMouseListener(new LabelListener());
            jLabel69.setName("19");
            
            jLabel70.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel70.addMouseListener(new LabelListener());
            jLabel70.setName("20");
            
            jLabel71.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel71.addMouseListener(new LabelListener());
            jLabel71.setName("21");
            
            jLabel72.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel72.addMouseListener(new LabelListener());
            jLabel72.setName("22");
            
            jLabel73.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel73.addMouseListener(new LabelListener());
            jLabel73.setName("23");
            
            jLabel74.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel74.addMouseListener(new LabelListener());
            jLabel74.setName("24");
            
            jLabel75.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel75.addMouseListener(new LabelListener());
            jLabel75.setName("25");
            
            jLabel76.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel76.addMouseListener(new LabelListener());
            jLabel76.setName("26");
            
            jLabel77.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel77.addMouseListener(new LabelListener());
            jLabel77.setName("27");
            
            jLabel78.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel78.addMouseListener(new LabelListener());
            jLabel78.setName("28");
            
            jLabel79.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel79.addMouseListener(new LabelListener());
            jLabel79.setName("29");
            
            jLabel80.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel80.addMouseListener(new LabelListener());
            jLabel80.setName("30");
            
            jLabel81.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel81.addMouseListener(new LabelListener());
            jLabel81.setName("31");
            
            jLabel82.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel82.addMouseListener(new LabelListener());
            jLabel82.setName("32");
            
            jLabel83.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel83.addMouseListener(new LabelListener());
            jLabel83.setName("33");
            
            jLabel84.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel84.addMouseListener(new LabelListener());
            jLabel84.setName("34");
            
            jLabel85.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel85.addMouseListener(new LabelListener());
            jLabel85.setName("35");
            
            jLabel86.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel86.addMouseListener(new LabelListener());
            jLabel86.setName("36");
            
            jLabel87.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel87.addMouseListener(new LabelListener());
            jLabel87.setName("37");
            
            jLabel88.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel88.addMouseListener(new LabelListener());
            jLabel88.setName("38");
            
            jLabel89.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            jLabel89.addMouseListener(new LabelListener());
            jLabel89.setName("39");
        }
        private void reloadLabels(){
            jLabel51.setText(jMenuItem9.getAccelerator().toString());
            jLabel52.setText(jMenuItem10.getAccelerator().toString());
            jLabel53.setText(jMenuItem11.getAccelerator().toString());
            jLabel54.setText(jMenuItem12.getAccelerator().toString());
            jLabel55.setText(jMenuItem13.getAccelerator().toString());
            jLabel56.setText(jMenuItem26.getAccelerator().toString());
            jLabel57.setText(jMenuItem14.getAccelerator().toString());
            jLabel58.setText(jMenuItem15.getAccelerator().toString());
            jLabel59.setText(jMenuItem27.getAccelerator().toString());;
            jLabel60.setText(jMenuItem16.getAccelerator().toString());
            jLabel61.setText(jMenuItem17.getAccelerator().toString());
            jLabel62.setText(jMenuItem18.getAccelerator().toString());
            jLabel63.setText(jMenuItem19.getAccelerator().toString());
            jLabel64.setText(jMenuItem20.getAccelerator().toString());
            jLabel65.setText(jMenuItem21.getAccelerator().toString());
            jLabel66.setText(jMenuItem22.getAccelerator().toString());
            jLabel67.setText(jMenuItem23.getAccelerator().toString());
            jLabel68.setText(jMenuItem24.getAccelerator().toString());
            jLabel69.setText(jMenuItem25.getAccelerator().toString());
            jLabel70.setText(jCheckBoxMenuItem1.getAccelerator().toString());
            jLabel71.setText(jCheckBoxMenuItem2.getAccelerator().toString());
            jLabel72.setText(jCheckBoxMenuItem3.getAccelerator().toString());
            jLabel73.setText(jMenuItem28.getAccelerator().toString());
            jLabel74.setText(jMenuItem29.getAccelerator().toString());
            jLabel75.setText(jMenuItem30.getAccelerator().toString());
            jLabel76.setText(jMenuItem31.getAccelerator().toString());
            jLabel77.setText(jMenuItem32.getAccelerator().toString());
            jLabel78.setText(jMenuItem34.getAccelerator().toString());
            jLabel79.setText(jMenuItem36.getAccelerator().toString());
            jLabel80.setText(jMenuItem35.getAccelerator().toString());
            jLabel81.setText(jMenuItem37.getAccelerator().toString());
            jLabel82.setText(jMenuItem38.getAccelerator().toString());
            jLabel83.setText(jMenuItem39.getAccelerator().toString());
            jLabel84.setText(jMenuItem40.getAccelerator().toString());
            jLabel85.setText(jMenuItem41.getAccelerator().toString());
            jLabel86.setText(jMenuItem42.getAccelerator().toString());
            jLabel87.setText(jMenuItem43.getAccelerator().toString());
            jLabel88.setText(jMenuItem45.getAccelerator().toString());
            jLabel89.setText(jMenuItem44.getAccelerator().toString());
        }
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
        private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jPanel1.setLayout(new java.awt.GridLayout(1, 2));
        jPanel2.setLayout(new java.awt.GridLayout(40, 1, 5, 5));
        jLabel1.setText("New");
        jPanel2.add(jLabel1);
        jLabel2.setText("Open");
        jPanel2.add(jLabel2);
        jLabel3.setText("Save");
        jPanel2.add(jLabel3);
        jLabel4.setText("Save As");
        jPanel2.add(jLabel4);
        jLabel5.setText("Print");
        jPanel2.add(jLabel5);
        jLabel6.setText("Rename");
        jPanel2.add(jLabel6);
        jLabel7.setText("Close");
        jPanel2.add(jLabel7);
        jLabel8.setText("Close All");
        jPanel2.add(jLabel8);
        jLabel9.setText("Delete File");
        jPanel2.add(jLabel9);
        jLabel10.setText("Exit");
        jPanel2.add(jLabel10);
        jLabel11.setText("Undo");
        jPanel2.add(jLabel11);
        jLabel12.setText("Redo");
        jPanel2.add(jLabel12);
        jLabel13.setText("Cut");
        jPanel2.add(jLabel13);
        jLabel14.setText("Copy");
        jPanel2.add(jLabel14);
        jLabel15.setText("Paste");
        jPanel2.add(jLabel15);
        jLabel16.setText("Delete Text");
        jPanel2.add(jLabel16);
        jLabel17.setText("Find");
        jPanel2.add(jLabel17);
        jLabel18.setText("Replace");
        jPanel2.add(jLabel18);
        jLabel19.setText("Date and Time");
        jPanel2.add(jLabel19);
        jLabel20.setText("Show Toolbar");
        jPanel2.add(jLabel20);
        jLabel21.setText("Show Command Line");
        jPanel2.add(jLabel21);
        jLabel22.setText("Show Status Line");
        jPanel2.add(jLabel22);
        jLabel23.setText("Tab Orientation Top");
        jPanel2.add(jLabel23);
        jLabel24.setText("Tab Orientation Right");
        jPanel2.add(jLabel24);
        jLabel25.setText("Tab Orientation Bottom");
        jPanel2.add(jLabel25);
        jLabel26.setText("Tab Orientation Left");
        jPanel2.add(jLabel26);
        jLabel27.setText("Preferences");
        jPanel2.add(jLabel27);
        jLabel28.setText("Shortcut Mapper");
        jPanel2.add(jLabel28);
        jLabel29.setText("Converter");
        jPanel2.add(jLabel29);
        jLabel30.setText("Export as PDF");
        jPanel2.add(jLabel30);
        jLabel31.setText("Export as RTF");
        jPanel2.add(jLabel31);
        jLabel32.setText("Export as LATEX");
        jPanel2.add(jLabel32);
        jLabel33.setText("Export as HTML");
        jPanel2.add(jLabel33);
        jLabel34.setText("Google Search");
        jPanel2.add(jLabel34);
        jLabel35.setText("Wikipedia Search");
        jLabel35.setToolTipText("");
        jPanel2.add(jLabel35);
        jLabel36.setText("Launch In Browser");
        jPanel2.add(jLabel36);
        jLabel37.setText("Online Help");
        jPanel2.add(jLabel37);
        jLabel38.setText("Command Line Arguments");
        jPanel2.add(jLabel38);
        jLabel39.setText("About VIDE");
        jPanel2.add(jLabel39);
        jPanel1.add(jPanel2);
        jPanel3.setLayout(new java.awt.GridLayout(40, 1, 5, 5));
        jPanel3.add(jLabel51);
        jPanel3.add(jLabel52);
        jPanel3.add(jLabel53);
        jPanel3.add(jLabel54);
        jPanel3.add(jLabel55);
        jPanel3.add(jLabel56);
        jPanel3.add(jLabel57);
        jPanel3.add(jLabel58);
        jPanel3.add(jLabel59);
        jPanel3.add(jLabel60);
        jPanel3.add(jLabel61);
        jPanel3.add(jLabel62);
        jPanel3.add(jLabel63);
        jPanel3.add(jLabel64);
        jPanel3.add(jLabel65);
        jPanel3.add(jLabel66);
        jPanel3.add(jLabel67);
        jPanel3.add(jLabel68);
        jPanel3.add(jLabel69);
        jPanel3.add(jLabel70);
        jPanel3.add(jLabel71);
        jPanel3.add(jLabel72);
        jPanel3.add(jLabel73);
        jPanel3.add(jLabel74);
        jPanel3.add(jLabel75);
        jPanel3.add(jLabel76);
        jPanel3.add(jLabel77);
        jPanel3.add(jLabel78);
        jPanel3.add(jLabel79);
        jPanel3.add(jLabel80);
        jPanel3.add(jLabel81);
        jPanel3.add(jLabel82);
        jPanel3.add(jLabel83);
        jPanel3.add(jLabel84);
        jPanel3.add(jLabel85);
        jPanel3.add(jLabel86);
        jPanel3.add(jLabel87);
        jPanel3.add(jLabel88);
        jPanel3.add(jLabel89);
        jPanel1.add(jPanel3);
        jScrollPane1.setViewportView(jPanel1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
        }// </editor-fold>                        
        
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel10;
        private javax.swing.JLabel jLabel11;
        private javax.swing.JLabel jLabel12;
        private javax.swing.JLabel jLabel13;
        private javax.swing.JLabel jLabel14;
        private javax.swing.JLabel jLabel15;
        private javax.swing.JLabel jLabel16;
        private javax.swing.JLabel jLabel17;
        private javax.swing.JLabel jLabel18;
        private javax.swing.JLabel jLabel19;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel20;
        private javax.swing.JLabel jLabel21;
        private javax.swing.JLabel jLabel22;
        private javax.swing.JLabel jLabel23;
        private javax.swing.JLabel jLabel24;
        private javax.swing.JLabel jLabel25;
        private javax.swing.JLabel jLabel26;
        private javax.swing.JLabel jLabel27;
        private javax.swing.JLabel jLabel28;
        private javax.swing.JLabel jLabel29;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel30;
        private javax.swing.JLabel jLabel31;
        private javax.swing.JLabel jLabel32;
        private javax.swing.JLabel jLabel33;
        private javax.swing.JLabel jLabel34;
        private javax.swing.JLabel jLabel35;
        private javax.swing.JLabel jLabel36;
        private javax.swing.JLabel jLabel37;
        private javax.swing.JLabel jLabel38;
        private javax.swing.JLabel jLabel39;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel51;
        private javax.swing.JLabel jLabel52;
        private javax.swing.JLabel jLabel53;
        private javax.swing.JLabel jLabel54;
        private javax.swing.JLabel jLabel55;
        private javax.swing.JLabel jLabel56;
        private javax.swing.JLabel jLabel57;
        private javax.swing.JLabel jLabel58;
        private javax.swing.JLabel jLabel59;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel60;
        private javax.swing.JLabel jLabel61;
        private javax.swing.JLabel jLabel62;
        private javax.swing.JLabel jLabel63;
        private javax.swing.JLabel jLabel64;
        private javax.swing.JLabel jLabel65;
        private javax.swing.JLabel jLabel66;
        private javax.swing.JLabel jLabel67;
        private javax.swing.JLabel jLabel68;
        private javax.swing.JLabel jLabel69;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel70;
        private javax.swing.JLabel jLabel71;
        private javax.swing.JLabel jLabel72;
        private javax.swing.JLabel jLabel73;
        private javax.swing.JLabel jLabel74;
        private javax.swing.JLabel jLabel75;
        private javax.swing.JLabel jLabel76;
        private javax.swing.JLabel jLabel77;
        private javax.swing.JLabel jLabel78;
        private javax.swing.JLabel jLabel79;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JLabel jLabel80;
        private javax.swing.JLabel jLabel81;
        private javax.swing.JLabel jLabel82;
        private javax.swing.JLabel jLabel83;
        private javax.swing.JLabel jLabel84;
        private javax.swing.JLabel jLabel85;
        private javax.swing.JLabel jLabel86;
        private javax.swing.JLabel jLabel87;
        private javax.swing.JLabel jLabel88;
        private javax.swing.JLabel jLabel89;
        private javax.swing.JLabel jLabel9;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration                   
    }
    private void callForAction(int a){
        switch(a)
            {
                case 1:
                    cut();
                    break;
                case 2:
                    copy();
                    break;
                case 3:
                    paste();
                    break;
                case 4:
                    delete();
                    break;
                case 5:
                    selectall();
                    break;    
                case 6:
                    print();
                    break;
                case 7:
                    commentIt();
                    break;
                case 8:
                    uncommentIt();
                    break;                 
            }
        updateStatus();
    }
    class PopupListener extends MouseAdapter
    {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger())
            {
                jPopupMenu1.show(e.getComponent(),e.getX(), e.getY());
            }
        }
    }
    class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;
    
    public ButtonTabComponent(final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);
        
        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        
        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                textpanes.remove(i);
                undomanagers.remove(i);
                tlns.remove(i);
                pane.remove(i);
            }
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    private final MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem26 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem27 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenuItem22 = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        jMenuItem25 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem3 = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem28 = new javax.swing.JMenuItem();
        jMenuItem29 = new javax.swing.JMenuItem();
        jMenuItem30 = new javax.swing.JMenuItem();
        jMenuItem31 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem32 = new javax.swing.JMenuItem();
        jMenuItem34 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem36 = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jMenuItem35 = new javax.swing.JMenuItem();
        jMenuItem37 = new javax.swing.JMenuItem();
        jMenuItem38 = new javax.swing.JMenuItem();
        jMenuItem39 = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMenuItem40 = new javax.swing.JMenuItem();
        jMenuItem41 = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        jMenuItem42 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem43 = new javax.swing.JMenuItem();
        jMenuItem45 = new javax.swing.JMenuItem();
        jMenuItem44 = new javax.swing.JMenuItem();

        jMenuItem1.setMnemonic((int)'x');
        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("cut");
        jMenuItem1.setToolTipText("cut selected text");
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cut.png")));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setMnemonic((int)'c');
        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("copy");
        jMenuItem2.setToolTipText("copy selected text");
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/copy.png")));
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem2);

        jMenuItem3.setMnemonic((int)'v');
        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("paste");
        jMenuItem3.setToolTipText("paste selected text");
        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/paste.png")));
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem3);
        jPopupMenu1.add(jSeparator3);

        jMenuItem5.setMnemonic((int)'d');
        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("delete");
        jMenuItem5.setToolTipText("delete selected text");
        jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete_file.png")));
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem5);

        jMenuItem4.setMnemonic((int)'a');
        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("select all");
        jMenuItem4.setToolTipText("select all text");
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_all.png")));
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem4);
        jPopupMenu1.add(jSeparator2);

        jMenuItem6.setMnemonic((int)'p');
        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setText("print");
        jMenuItem6.setToolTipText("print this document");
        jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/printer.png")));
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem6);
        jPopupMenu1.add(jSeparator1);

        jMenuItem7.setText("comment it");
        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setToolTipText("comment current line");
        jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/comment.png")));
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem7);

        jMenuItem8.setText("uncomment line");
        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem8.setToolTipText("uncomment current line");
        jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/uncomment.png")));
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem8);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("VIDE");

        jPanel1.setLayout(new java.awt.GridLayout(2, 1));

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jTextField1, java.awt.BorderLayout.CENTER);

        jButton1.setText("Run");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, java.awt.BorderLayout.LINE_END);

        jPanel1.add(jPanel2);
        jPanel1.add(jLabel16);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setToolTipText("toolbar");
        jPanel3.setPreferredSize(new java.awt.Dimension(87, 45));
        jPanel3.setRequestFocusEnabled(false);
        jPanel3.setLayout(new java.awt.GridLayout(1, 14));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel2);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel3);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel4);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel5);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel6);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel7);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel8);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel9);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel10);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel11);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel12);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel13);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel14);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel3.add(jLabel15);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_START);
        jPanel3.getAccessibleContext().setAccessibleName("");

        jScrollPane1.setViewportView(jTextPane1);
        TextLineNumber tln = new TextLineNumber(jTextPane1);
        tlns.add(tln);
        jScrollPane1.setRowHeaderView(tln);
        textpanes.add(jTextPane1);
        UndoManager m = new UndoManager();
        jTextPane1.getDocument().addUndoableEditListener(m);
        undomanagers.add(m);
        jTextPane1.addMouseListener(new PopupListener());
        jTextPane1.addKeyListener(new MyKeyListener());

        jTabbedPane1.addTab("tab1", jScrollPane1);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new.png"))); // NOI18N
        jMenuItem9.setText("New");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem9);

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        jMenuItem10.setText("Open");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem10);
        jMenu1.add(jSeparator4);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save.png"))); // NOI18N
        jMenuItem11.setText("Save");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem11);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_as.png"))); // NOI18N
        jMenuItem12.setText("Save As");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem12);
        jMenu1.add(jSeparator5);

        jMenuItem13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/printer.png"))); // NOI18N
        jMenuItem13.setText("Print");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem13);

        jMenuItem26.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename.png"))); // NOI18N
        jMenuItem26.setText("Rename");
        jMenuItem26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem26ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem26);
        jMenu1.add(jSeparator6);

        jMenuItem14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/page_text_close.png"))); // NOI18N
        jMenuItem14.setText("Close");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem14);

        jMenuItem15.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/closeall.png"))); // NOI18N
        jMenuItem15.setText("Close All");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem15);
        jMenu1.add(jSeparator7);

        jMenuItem27.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete_file.png"))); // NOI18N
        jMenuItem27.setText("Delete File");
        jMenuItem27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem27ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem27);

        jMenuItem16.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit.png"))); // NOI18N
        jMenuItem16.setText("Exit");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem16);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem17.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undo.png"))); // NOI18N
        jMenuItem17.setText("Undo");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem17);

        jMenuItem18.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redo.png"))); // NOI18N
        jMenuItem18.setText("Redo");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem18);
        jMenu2.add(jSeparator8);

        jMenuItem19.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cut.png"))); // NOI18N
        jMenuItem19.setText("Cut");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem19);

        jMenuItem20.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/copy.png"))); // NOI18N
        jMenuItem20.setText("Copy");
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem20);

        jMenuItem21.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/paste.png"))); // NOI18N
        jMenuItem21.setText("Paste");
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem21);

        jMenuItem22.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete_file.png"))); // NOI18N
        jMenuItem22.setText("Delete");
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem22);
        jMenu2.add(jSeparator9);

        jMenuItem23.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        jMenuItem23.setText("Find");
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem23);

        jMenuItem24.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/replace.png"))); // NOI18N
        jMenuItem24.setText("Replace");
        jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem24ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem24);
        jMenu2.add(jSeparator10);

        jMenuItem25.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/date_time.png"))); // NOI18N
        jMenuItem25.setText("Date and Time");
        jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem25ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem25);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("View");

        jCheckBoxMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Toolbar");
        jCheckBoxMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/toolbar.png"))); // NOI18N
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxMenuItem1);

        jCheckBoxMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText("Command Line");
        jCheckBoxMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/commandline.png"))); // NOI18N
        jCheckBoxMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxMenuItem2);

        jCheckBoxMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        jCheckBoxMenuItem3.setSelected(true);
        jCheckBoxMenuItem3.setText("Status Line");
        jCheckBoxMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/status.png"))); // NOI18N
        jCheckBoxMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxMenuItem3);

        jMenu4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/orientation.png"))); // NOI18N
        jMenu4.setText("Change Tab Orientation");

        jMenuItem28.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/top.png"))); // NOI18N
        jMenuItem28.setText("Top");
        jMenuItem28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem28ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem28);

        jMenuItem29.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/right.png"))); // NOI18N
        jMenuItem29.setText("Right");
        jMenuItem29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem29ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem29);

        jMenuItem30.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bottom.png"))); // NOI18N
        jMenuItem30.setText("Bottom");
        jMenuItem30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem30ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem30);

        jMenuItem31.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/left.png"))); // NOI18N
        jMenuItem31.setText("Left");
        jMenuItem31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem31ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem31);

        jMenu3.add(jMenu4);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Settings");

        jMenuItem32.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/preferences.png"))); // NOI18N
        jMenuItem32.setText("Preferences");
        jMenuItem32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem32ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem32);

        jMenuItem34.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/shortcut_mapper.png"))); // NOI18N
        jMenuItem34.setText("Shortcut Mapper");
        jMenuItem34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem34ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem34);

        jMenuBar1.add(jMenu5);

        jMenu6.setText("Tools");

        jMenuItem36.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/converter.png"))); // NOI18N
        jMenuItem36.setText("Converter");
        jMenu6.add(jMenuItem36);
        jMenu6.add(jSeparator11);

        jMenuItem35.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pdf.png"))); // NOI18N
        jMenuItem35.setText("Export as PDF");
        jMenu6.add(jMenuItem35);

        jMenuItem37.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rtf.png"))); // NOI18N
        jMenuItem37.setText("Export as RTF");
        jMenu6.add(jMenuItem37);

        jMenuItem38.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem38.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/latex.png"))); // NOI18N
        jMenuItem38.setText("Export as Latex");
        jMenu6.add(jMenuItem38);

        jMenuItem39.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem39.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/html.png"))); // NOI18N
        jMenuItem39.setText("Export as HTML");
        jMenu6.add(jMenuItem39);
        jMenu6.add(jSeparator12);

        jMenuItem40.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/google.png"))); // NOI18N
        jMenuItem40.setText("Google Search");
        jMenuItem40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem40ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem40);

        jMenuItem41.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/wikipedia.png"))); // NOI18N
        jMenuItem41.setText("Wikipedia Search");
        jMenuItem41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem41ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem41);
        jMenu6.add(jSeparator13);

        jMenuItem42.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem42.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browser.png"))); // NOI18N
        jMenuItem42.setText("Launch In Browser");
        jMenuItem42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem42ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem42);

        jMenuBar1.add(jMenu6);

        jMenu7.setText("Help");

        jMenuItem43.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png"))); // NOI18N
        jMenuItem43.setText("Offline Help");
        jMenu7.add(jMenuItem43);

        jMenuItem45.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem45.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/command_line_argument.png"))); // NOI18N
        jMenuItem45.setText("Command Line Arguments");
        jMenu7.add(jMenuItem45);

        jMenuItem44.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about_us.png"))); // NOI18N
        jMenuItem44.setText("About VIDE");
        jMenu7.add(jMenuItem44);

        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        getAccessibleContext().setAccessibleDescription("Voice Based Integrated Development Environment");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        callForAction(1);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        callForAction(2);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        callForAction(3);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // TODO add your handling code here:
        callForAction(4);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
        callForAction(5);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        // TODO add your handling code here:
        callForAction(6);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        // TODO add your handling code here:
        callForAction(7);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        // TODO add your handling code here:
        callForAction(8);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        // TODO add your handling code here:
        createNewTab();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        // TODO add your handling code here:
        openFile();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        // TODO add your handling code here:
        saveFile();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        // TODO add your handling code here:
        saveAs();
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        // TODO add your handling code here:
        print();
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed
        // TODO add your handling code here:
        rename();
    }//GEN-LAST:event_jMenuItem26ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        // TODO add your handling code here:
        closeTab();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        // TODO add your handling code here:
        closeAllTabs();
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem27ActionPerformed
        // TODO add your handling code here:
        deleteFile();
    }//GEN-LAST:event_jMenuItem27ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        // TODO add your handling code here:
        undo();
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        // TODO add your handling code here:
        redo();
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        // TODO add your handling code here:
        cut();
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed
        // TODO add your handling code here:
        copy();
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed
        // TODO add your handling code here:
        paste();
    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        // TODO add your handling code here:
        find();
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed
        // TODO add your handling code here:
        replace();
    }//GEN-LAST:event_jMenuItem24ActionPerformed

    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
        // TODO add your handling code here:
        showToolbar();
    }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

    private void jCheckBoxMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem2ActionPerformed
        // TODO add your handling code here:
        showCommandLine();
    }//GEN-LAST:event_jCheckBoxMenuItem2ActionPerformed

    private void jCheckBoxMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem3ActionPerformed
        // TODO add your handling code here:
        showStatus();
    }//GEN-LAST:event_jCheckBoxMenuItem3ActionPerformed

    private void jMenuItem28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem28ActionPerformed
        // TODO add your handling code here:
        changeOrientation(1);
    }//GEN-LAST:event_jMenuItem28ActionPerformed

    private void jMenuItem29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem29ActionPerformed
        // TODO add your handling code here:
        changeOrientation(2);
    }//GEN-LAST:event_jMenuItem29ActionPerformed

    private void jMenuItem30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem30ActionPerformed
        // TODO add your handling code here:
        changeOrientation(3);
    }//GEN-LAST:event_jMenuItem30ActionPerformed

    private void jMenuItem31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem31ActionPerformed
        // TODO add your handling code here:
        changeOrientation(4);
    }//GEN-LAST:event_jMenuItem31ActionPerformed

    private void jMenuItem32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem32ActionPerformed
        // TODO add your handling code here:
        openPreferences();
    }//GEN-LAST:event_jMenuItem32ActionPerformed

    private void jMenuItem42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem42ActionPerformed
        // TODO add your handling code here:
        launchInBrowser();
    }//GEN-LAST:event_jMenuItem42ActionPerformed

    private void jMenuItem34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem34ActionPerformed
        // TODO add your handling code here:
        shortcutMap();
    }//GEN-LAST:event_jMenuItem34ActionPerformed

    private void jMenuItem40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem40ActionPerformed
        // TODO add your handling code here:
        searchGoogle();
    }//GEN-LAST:event_jMenuItem40ActionPerformed

    private void jMenuItem41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem41ActionPerformed
        // TODO add your handling code here:
        searchWikipedia();
    }//GEN-LAST:event_jMenuItem41ActionPerformed

    private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed
        // TODO add your handling code here:
        dateAndTime();
    }//GEN-LAST:event_jMenuItem25ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        goForTextAction(jTextField1.getText());
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new window().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem3;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
    private javax.swing.JMenuItem jMenuItem26;
    private javax.swing.JMenuItem jMenuItem27;
    private javax.swing.JMenuItem jMenuItem28;
    private javax.swing.JMenuItem jMenuItem29;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem30;
    private javax.swing.JMenuItem jMenuItem31;
    private javax.swing.JMenuItem jMenuItem32;
    private javax.swing.JMenuItem jMenuItem34;
    private javax.swing.JMenuItem jMenuItem35;
    private javax.swing.JMenuItem jMenuItem36;
    private javax.swing.JMenuItem jMenuItem37;
    private javax.swing.JMenuItem jMenuItem38;
    private javax.swing.JMenuItem jMenuItem39;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem40;
    private javax.swing.JMenuItem jMenuItem41;
    private javax.swing.JMenuItem jMenuItem42;
    private javax.swing.JMenuItem jMenuItem43;
    private javax.swing.JMenuItem jMenuItem44;
    private javax.swing.JMenuItem jMenuItem45;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
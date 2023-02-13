package clientserver;

import java.io.*;
import java.net.*;


class FileOperations {

    public static void performFileTransfer(File filepathToRead, File filepathToWrite) throws FileNotFoundException, IOException{
        try {
            FileReader ins = null;  
            FileWriter outs = null;
            ins = new FileReader(filepathToRead);  
            outs = new FileWriter(filepathToWrite);  
            int ch;
            while ((ch = ins.read()) != -1) {  
                outs.write(ch);  
            }
            ins.close();  
            outs.close();
        } catch (IOException e) {
            System.out.println("File Operation IO exception");
            e.printStackTrace();
            throw new IOException();
        }

    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_serial;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import com.pi4j.io.serial.impl.SerialImpl;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialDataEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *This class implements a driver for the RS-232 channel
 * @author Federico
 */
public class RS_232_Driver extends SerialImpl{
    public Serial serial = null;
    private SerialConfig config = null;
    private PrintStream pos = null;
    
    
    /**
     * Class constructor
     */
    public RS_232_Driver() throws IOException, InterruptedException{
    //Instantiate Serial port
        serial = SerialFactory.createInstance();
        
        //Configure Serial Port
        config = new SerialConfig();
        config.device(SerialPort.getDefaultPort())
                  .baud(Baud._9600)
                  .dataBits(DataBits._8)
                  .parity(Parity.NONE)
                  .stopBits(StopBits._1)
                  .flowControl(FlowControl.NONE);
            
        serial.open(config);
        serial.discardAll();
        pos = new PrintStream(serial.getOutputStream());
       }
    
    /**
     * Add a Callback routine for handle received serial data
     * SerialDataEventListener interface implementation
     * @param e SerialDataEventListener interface implementation
     */
    public void addListener(SerialDataEventListener e){
        
        serial.addListener(e);
    }
    /**
     * Transmit a String on the serial channel. The method waits until all
     * data has been transmitted.
     * @param  data  Data String to be transmitted.
     */
    public void send(String data) {
        pos.print(data);
        pos.flush();
        }
    
    /**
     * Transmit a byte buffer on the serial channel
     * @param data byte[]. Buffer to be transmitted
     */
    public void send(byte[] data){
        pos.print(data);
        pos.flush();
        
    }
    
    /**
     * Returns a byte array with the receive data
     * @return byte[] Received data
     */
    public byte[] receive() {
        
        byte[] rx=null;
        try{
            rx=serial.read();
        }catch(IOException ex){
            System.out.println("Error reading Serial data buffer");
            
        }
        return rx;
    }
    
   
    /**
     * Clears the TX and RX buffers
     * 
     */
    public void discardBuffer() {
        
        try{
        serial.discardInput();
        serial.discardData();
        }catch(IOException ex){
            System.out.println("Error discarding serial data");
        }
    }
}

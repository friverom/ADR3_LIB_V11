/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_io;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Federico
 */
public final class LTC2309 {
    
    private static final int LTC2309_ADDR = 0x08;
    
    private static final int AI_1 = 0x88;
    private static final int AI_2 = 0xc8;
    private static final int AI_3 = 0x98;
    private static final int AI_4 = 0xd8;
    private static final int AI_5 = 0xa8;
    private static final int AI_6 = 0xe8;
    private static final int AI_7 = 0xb8;
    private static final int AI_8 = 0xf8;
    
    I2CBus i2c = null;
    I2CDevice ADC = null;
    
    /**
     * Constructor class
     * @param i2c
     *  
     */
    public LTC2309(I2CBus i2c) {
        this.i2c = i2c;
        try {
            ADC = i2c.getDevice(LTC2309_ADDR);
            System.out.println("Connection to LTC2309. OK");
        } catch (IOException ex) {
            System.out.println("Could not get access to LTC2309");
            Logger.getLogger(LTC2309.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void setChannel(int channel){
        
    }
    /**
     * Analog to digital conversion
     * @param Analog_in channel input 1-7
     * @return int 12 bit result
     * 
     */
    public synchronized int getAnalogIn(int Analog_in){
        
        int data =0;
        switch(Analog_in){
            case 1:
                data = getConversion(AI_1);
                break;
            case 2:
                data = getConversion(AI_2);
                break;
            case 3:
                data = getConversion(AI_3);
                break;
            case 4:
                data = getConversion(AI_4);
                break;
            case 5:
                data = getConversion(AI_5);
                break;
            case 6:
                data = getConversion(AI_6);
                break;
            case 7:
                data = getConversion(AI_7);
                break;
            case 8:
                data = getConversion(AI_8);
                break;
            default:
                data = 0;
        }
        return data;
    }
    /**
     * This method start a conversion on the selected channel and returns the
     * result as and INT.
     * @param channel
     * @return
     * @throws IOException
     */
    private int getConversion(int channel){
        
        int data = 0;
        try {
            byte[] buffer = new byte[]{0x00,0x00,0x00,0x00}; //Holds and int
            
           // ADC.write((byte) channel); //Start conversion on selected channel
            
            int len = ADC.read(channel,buffer, 2, 2); //read bytes and store MSB and LSB in buffer
            
            data = ByteBuffer.wrap(buffer).getInt(); //convert to int
            data = data>>>4; // shift 4 places to get result

            
        } catch (IOException ex) {
            System.out.println("Error accessing LTC2309");
            Logger.getLogger(LTC2309.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
}

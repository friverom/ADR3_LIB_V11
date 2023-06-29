/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_io;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Rivero
 */
public final class MCP23017 {
    
    private static final int MCP23017_ADDR = 0x20;
    
    //Control Register IOCON
    private static final int IOCON_REG = 0x05;
    private static final int IOCON = 0xa4; //Segregated banks non sequential
    //and interrupt out is Open Collector
    
    //Port A Registers. Port A is all Outputs
    private static final int IODIRA_REG = 0x00; 
    private static final int GPIOA_REG = 0x09;
    private static final int OLATA_REG = 0x0a;
    
    //Port B Registers. All inputs
    private static final int IODIRB_REG = 0x10;
    private static final int IPOLB_REG = 0x11;
    private static final int GPINTENB_REG = 0x12;
    private static final int DEFVALB = 0x13;
    private static final int INTCONB_REG = 0x14;
    private static final int INTFB_REG = 0x17;
    private static final int INTCAP_REG = 0x18;
    private static final int GPIOB_REG = 0x19;
    private static final int OLATB_REG = 0x1a;
    
    I2CBus i2c = null;
    I2CDevice GPIO = null;
    
    /**
     * Constructor class. Initialize MCP23017 PORTA as output and PORTB
     * as Input with inverted polarity.
     * @param i2c
     * @throws IOException 
     */
    public MCP23017(I2CBus i2c) {
        try {
            this.i2c=i2c;
            GPIO = this.i2c.getDevice(MCP23017_ADDR);
            System.out.println("Connection to MCP23017. OK");
            GPIO.write(IOCON_REG, (byte)IOCON); //Segregated banks non seq.
            GPIO.write(IODIRA_REG, (byte) 0x00); //Port A all outputs
            GPIO.write(OLATA_REG, (byte) 0xff);
            GPIO.write(GPIOA_REG, (byte) 0x00);
            GPIO.write(IODIRB_REG, (byte) 0xff);
            GPIO.write(IPOLB_REG, (byte) 0xff); //Inverted Polarity
            GPIO.write(DEFVALB, (byte)0x00); 
            GPIO.write(INTCONB_REG, (byte) 0x00);
            GPIO.write(GPINTENB_REG, (byte) 0xff); //Enable interrupt on Port B
            GPIO.read(INTFB_REG); //Discard any interrupt on startup
        } catch (IOException ex) {
            System.out.println("Error accessing MCP23017");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Return the Interrupt Flag Register. a one indicates the source of interrupt
     * @return int Interrupt Register Flag
     */
    public synchronized int getIntFlag(){
        int data=0;
        
        try{
            data=GPIO.read(INTFB_REG);
        }catch(IOException ex){
            System.out.println("MCP23017 Error. Can't read Interrupt Flag Register");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    /**
     * Return the input capture register at the instant of the interrupt
     * @return int. Input port at interrupt moment
     */
    public synchronized int getIntCaptureReg(){
        int data=0;
        try{
            data=GPIO.read(INTCAP_REG);
        }catch(IOException ex){
            System.out.println("MCP23017 Error. Can't read Interrupt Capture Register");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    /**
     * Turns On all relays
     */
    public synchronized void setON_All(){
        try {
            GPIO.write(GPIOA_REG, (byte)0xff);
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't set relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Turns Off all relays
     */
    public synchronized void setOFF_All(){
        try {
            GPIO.write(GPIOA_REG, (byte)0x00);
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't set relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Gets Relays Status
     * @return
     * 
     */
    public synchronized int getRLYS(){
        
        int data =0;
        try {
            data = GPIO.read(OLATA_REG);
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't set relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    /**
     * Sets On indicated rly
     * @param rly Relay position 1-8
     * 
     */
    public synchronized void setON_Rly(int rly){
        try {
            int bit = getBit(rly);
            bit = bit | GPIO.read(OLATA_REG);
            GPIO.write(GPIOA_REG,(byte) bit);
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't set relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Sets Off indicated rly
     * @param rly Relay position 1-8
     * 
     */
    public synchronized void setOFF_Rly(int rly){
        try {
            int bit = getBit(rly);
            bit = ~bit & GPIO.read(OLATA_REG);
            GPIO.write(GPIOA_REG, (byte) bit);
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't set relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Toggles Relay position
     * @param rly Relay position 1-8
     * 
     */
    public synchronized void toggle_Rly(int rly){
        try {
            int bit = getBit(rly);
            bit = bit & GPIO.read(OLATA_REG);
            if(bit == 0){
                setON_Rly(rly);
            } else {
                setOFF_Rly(rly);
            }
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't set relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Turns On indicated rly position for time duration
     * @param rly Relay position 1-8
     * @param time Timer in 1/10 of seconds
     * 
     */
    public void pulseON_Rly(int rly, int time){
        setON_Rly(rly);
        try {
            Thread.sleep(time * 1);
        } catch (InterruptedException ex) {
            System.out.println("MCP23017 Error. Can't pulse relay output");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        setOFF_Rly(rly);
    }
    /**
     * Toggles relay position for time duration
     * @param rly Relay position 1-8
     * @param time Time in milliseconds
     * 
     */
    public void pulseToggle(int rly, int time) {
        toggle_Rly(rly);
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            System.out.println("MCP23017 Error. Can't pulse toggle relay outputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        toggle_Rly(rly);
    }
    
    public void setPort(int value){
        try {
            GPIO.write(GPIOA_REG, (byte) value);
        } catch (IOException ex) {
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Gets Inputs
     * @return
     * @throws IOException 
     */
    public synchronized int getInputs(){
        
        int data = 0;
        try {
            data = GPIO.read(GPIOB_REG);
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't read digital inputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    /**
     * Gets status on Input
     * @param in Input position 1-8
     * @return Boolean status
     */
    public synchronized boolean getInput(int in) {
        
        boolean data = false;
        try {
            int bit = getBit(in);
            bit = bit & GPIO.read(GPIOB_REG);
            if(bit == 0)
                data = false;
            else
                data = true;
        } catch (IOException ex) {
            System.out.println("MCP23017 Error. Can't read digital inputs");
            Logger.getLogger(MCP23017.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    private int getBit(int rly){
        
        switch(rly){
            case 1:
              return(0x01);
              
            case 2:
                return(0x02);
                
            case 3:
                return(0x04);
                
            case 4:
                return(0x08);
                
            case 5:
                return(0x10);
                
            case 6:
                return(0x20);
                
            case 7:
                return(0x40);
                
            case 8:
                return(0x80);
                
            default:
                return(0x00);
                
                
        }
        
    }
    
   
}


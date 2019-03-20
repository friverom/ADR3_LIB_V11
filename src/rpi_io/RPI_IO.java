/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_io;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico
 */
public class RPI_IO {
    
    private int flags=0;
    private double[] setpoints = new double[8];
    private DS1307 rtc = null;
    private LTC2309 adc = null;
    private MCP23017 gpio = null;
    
    public RPI_IO() {

        I2CBus i2c;
        try {
            i2c = I2CFactory.getInstance(I2CBus.BUS_1);
            rtc = new DS1307(i2c);
            gpio = new MCP23017(i2c);
            adc = new LTC2309(i2c);
            
            for(int i=0;i<8;i++){
                setpoints[i]=0.0;
            }
                        
        } catch (I2CFactory.UnsupportedBusNumberException ex) {
            System.out.println("I2C Error. RPI_IO board not active");
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("I2C Error. RPI_IO board not active");
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        gpio.setOFF_All();
    }
    
    
    public void allRly_off(){
        gpio.setOFF_All();
    }

   
    public void allRly_on(){
        gpio.setON_All();
    }

    
    public int getRlyStatus(){
        int data = 0;
        data = gpio.getRLYS();
        return data;
    }

    
       public void setRly(int r) {
           gpio.setON_Rly(r);
    }

    
    public void resetRly(int r) {
        gpio.setOFF_Rly(r);
    }

    
    public void toggleRly(int r) {
        gpio.toggle_Rly(r);
    }

    public void pulseRly(int r, int t) {
        gpio.pulseON_Rly(r, t);
    }

    
    public void pulseToggle(int r, int time) {
        gpio.pulseToggle(r, time);
    }

    //New getInput command
    public int getInputs() {
        int data = 0;
        data = gpio.getInputs();
        return data;
    }
    
    public void setPort(int value){
       gpio.setPort(value);
    }
    
    public boolean getInput(int input){
        boolean data=false;
        data=gpio.getInput(input);
        return data;
    }
    
    public int getChannel(int c) {
        int data = 0;
        data = adc.getAnalogIn(c);
        return data;
    }

    
    public String getTime() {
        String time = null;
        time = rtc.getTime();
        return time;
    }

    public String getDate() {
        String date = null;
        date = rtc.getDate();
        return date;
    }

    
    public void setTime(String s) {
        rtc.setTime(s);
    }

    
    public void setDate(String s) {
        rtc.setDate(s);
    }

    
    public void blink_1Hz() {
        try {
            rtc.blink();
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public void out_on() {
        try {
            rtc.out_on();
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public void out_off() {
        try {
            rtc.out_off();
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getControlReg() throws IOException{
        return rtc.getControlReg();
    }
    
    public void writeString(int a, String s) {
        try {
            rtc.writeString(a, s);
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public String readString(int a) {
        String data = null;
        try {
            data = rtc.readString(a);
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    
    public void writeInt(int a, int i) {
        try {
            rtc.writeInt(a, i);
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public int readInt(int a) {
        int data = 0;
        try {
            data = rtc.readInt(a);
        } catch (IOException ex) {
            Logger.getLogger(RPI_IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    
    public void pulseRly(int r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public Calendar getCalendarRTC() {
        
        return rtc.getCalendarRTC();
    }

    
    public void setCalendarRTC(Calendar d) {
        rtc.setCalendarRTC(d);
    }
    
    public int getFlags(){
        return flags;
    }
    
    public boolean getFlag(int flag){
        if((getBit(flag)&flags)==0){
            return false;
        } else {
            return true;
        }
                
    }
    
    public void setFlags(int flags){
        this.flags=flags;
    }
    
    public void setFlag(int flag){
        flags=getBit(flag)|flags;
    }
    
    public void resetFlag(int flag){
        flags=~getBit(flag)&flags;
    }
    
    public double[] getSetpoints(){
        return setpoints;
    }
    
    public double getSetpoint(int setpoint) {
        if (setpoint > 0 && setpoint < 9) {
            return setpoints[setpoint - 1];
        } else {
            return 0.0;
        }
    }
    
    public void setSetpoint(int setpoint, double value){
        if(setpoint>0 && setpoint<9){
            setpoints[setpoint-1]=value;
        }    
    }
    private int getBit(int rly){
        
        if(rly==1){
            return 1;
        } else {
            return 1<<(rly-1);
        }
        
    }
    
    
}

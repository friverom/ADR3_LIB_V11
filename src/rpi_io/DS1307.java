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
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Class for managing DS1307 Real Time Clock
 * @author Federico Rivero
 */
public final class DS1307 {
    
    private static final int DS1307_ADDR=0x68; //RTC addr on RPI_IO Board
    
    //DS1307 Internal Registers
    private static final int SECONDS_REG=0x00;
    private static final int MINUTES_REG=0x01;
    private static final int HOUR_REG=0x02;
    private static final int DAY_REG=0x03; //Day of the week 1=Sun
    private static final int DATE_REG=0x04;
    private static final int MONTH_REG=0x05;
    private static final int YEAR_REG=0x06;
    private static final int ZONE=0x08;
    
    //Control Register
    private static final int CONTROL_REG=0x07; //Address
    private static final int OUT_1HZ=0x10; //1Hz Output
    private static final int OUT_OFF=0x80; //Output Off
    private static final int OUT_ON=0x00;   //Output On
    
    I2CBus i2c = null;
    I2CDevice rtc = null;
    /**
     * Class Constructor
     * @param i2c I2CBus
     * 
     */
    public DS1307 (I2CBus i2c){
        this.i2c=i2c;
        try {
            rtc = i2c.getDevice(DS1307_ADDR);
            System.out.println("Connection to DS1307. OK");
        } catch (IOException ex) {
            System.out.println("Error accessing DS1307");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * 
     * @return String with current time in format hh:mm:ss
     * 
     */
    public synchronized String getTime(){
        
        String data = null;
        
        try {
            String hour = String.format("%02x", getHour());
            String minutes = String.format("%02x", getMinutes());
            String seconds = String.format("%02x", getSeconds());
            data = hour+":"+minutes+":"+seconds;
        } catch (IOException ex) {
            System.out.println("DS1307 Error. Can't read time");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
   
    /**
     * 
     * @return RTC date in Calendar format
     */
   public Calendar getCalendarRTC(){
   
       Calendar date=Calendar.getInstance();
       date.clear();
              
        try {
           
            int year=Integer.parseInt(String.format("%02x", getYear()))+2000;
            int month=Integer.parseInt(String.format("%02x", getMonth()));
            int day=Integer.parseInt(String.format("%02x", getDate1()));
            int hour=Integer.parseInt(String.format("%02x", getHour()));
            int minutes=Integer.parseInt(String.format("%02x", getMinutes()));
            int seconds=Integer.parseInt(String.format("%02x", getSeconds()));
            
            date.set(year,month,day,hour,minutes,seconds);
            date.set(Calendar.ZONE_OFFSET, readInt(ZONE));
            
            } catch (IOException ex) {
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return date;
   }
   /**
    * Sets RTC date 
    * @param date Calendar format
    */  
   public void setCalendarRTC(Calendar date){
   
        try {
            String year=Integer.toString(date.get(Calendar.YEAR)-2000);
            String month=Integer.toString(date.get(Calendar.MONTH));
            String day=Integer.toString(date.get(Calendar.DAY_OF_MONTH));
            String dayw=Integer.toString(date.get(Calendar.DAY_OF_WEEK));
            int zone=date.get(Calendar.ZONE_OFFSET);
                        
            String hour=Integer.toString(date.get(Calendar.HOUR_OF_DAY));
            String minutes=Integer.toString(date.get(Calendar.MINUTE));
            String seconds=Integer.toString(date.get(Calendar.SECOND));
            
            setDay(StringToBCD(dayw));
            setDate(StringToBCD(day));
            setMonth(StringToBCD(month));
            setYear(StringToBCD(year));
            
            setSeconds(StringToBCD(seconds));
            setMinutes(StringToBCD(minutes));
            setHour(StringToBCD(hour));
            writeInt(ZONE,zone);
            
        } catch (IOException ex) {
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
       
   }
    
    /**
     * 
     * @return Date in mm/dd/yyyy format
     * 
     */
    public synchronized String getDate(){
        
        String data = null;
        
        try {
            String day = String.format("%02x", getDate1());
            String month = String.format("%02x", getMonth());
            String year = String.format("%02x", getYear());
            data = month+"/"+day+"/"+year;
        } catch (IOException ex) {
            System.out.println("DS1307 Error. Can't read date");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    /**
     * 
     * @param date dd/mm/yy format
     * 
     */
    public synchronized void setDate(String date){
        String[] parts = date.split("/");
        
        if (parts.length == 3) {
            try {
                String day = parts[0];
                String month = parts[1];
                String year = parts[2];
                
                setDate(StringToBCD(day));
                setMonth(StringToBCD(month)); 
                setYear(StringToBCD(year));
            } catch (IOException ex) {
                System.out.println("DS1307 Error. Can't set date");
                Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * 
     * @param time hh:mm:ss format
     *
     */
    public synchronized void setTime(String time){
        try {
            String[] parts = time.split(":");
            String seconds = null;
            String hour = parts[0];
            String minutes = parts[1];
            
            if(parts.length < 3){
                seconds = "0";
            }else{
                seconds = parts[2];
            }
            
            setSeconds(StringToBCD(seconds));
            setMinutes(StringToBCD(minutes));
            setHour(StringToBCD(hour));
        } catch (IOException ex) {
            System.out.println("DS1307 Error. Can't set date");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * 
     * @param secs int < 60
     * @throws IOException 
     */
    private void setSeconds(int secs) throws IOException{
        
        if(secs<96){
         rtc.write(SECONDS_REG, (byte)secs);   
        }
        
    } 
    
    /**
     * 
     * @param min int < 60
     * @throws IOException 
     */
    private void setMinutes(int min) throws IOException{
        
        if(min<96){
            rtc.write(MINUTES_REG,(byte)min);
        }
    }
    /**
     * 
     * @param hour int < 24
     * @throws IOException 
     */
    private void setHour(int hour) throws IOException{
    
        if(hour<36){
            rtc.write(HOUR_REG,(byte)hour);
        }
    }
    /**
     * 
     * @param day int between 1-7
     * @throws IOException 
     */
    private void setDay(int day) throws IOException {
        
        if(day<8){
            rtc.write(DAY_REG,(byte)day);
        }
    }
    /**
     * 
     * @param date int < 32
     * @throws IOException 
     */
    private void setDate(int date) throws IOException{
        
        if(date<50){
            rtc.write(DATE_REG,(byte)date);   
        }
    }
    /**
     * 
     * @param month int < 13
     * @throws IOException 
     */
    private void setMonth(int month) throws IOException{
    
        if(month<19){
            month=month;
            rtc.write(MONTH_REG, (byte)month);
        }
    }
    
    private void setYear(int year) throws IOException{
    
        rtc.write(YEAR_REG,(byte)year);
    }
    
    private int getSeconds() throws IOException {
    
        return( rtc.read(SECONDS_REG));
    }
    
    private int getMinutes() throws IOException{
    
        return(rtc.read(MINUTES_REG));
    }
    
    private int getHour() throws IOException{
    
        return(rtc.read(HOUR_REG));
    }
    
    private int getDay() throws IOException{
    
        return(rtc.read(DAY_REG));
    }
    
    private int getDate1() throws IOException{
    
        return(rtc.read(DATE_REG));
    }
    
    private int getMonth() throws IOException {
    
        return(rtc.read(MONTH_REG));
    }
    
    private int getYear() throws IOException {
    
        return(rtc.read(YEAR_REG));
    }
    
    // Methods to handle DS1307 OUT pin
    
    /**
     * Turns On OUT pin 1Hz
     * @throws IOException 
     */
    public void blink() throws IOException{
        rtc.write(CONTROL_REG,(byte)OUT_1HZ);
    }
    /**
     * Turns Off OUT pin
     * @throws IOException 
     */
    public void out_off() throws IOException{
        rtc.write(CONTROL_REG,(byte)OUT_OFF);
    }
    /**
     * Turns On OUT pin
     * @throws IOException 
     */
    public void out_on() throws IOException{
        rtc.write(CONTROL_REG,(byte)OUT_ON);
    }
    public int getControlReg() throws IOException{
        return (int) rtc.read(CONTROL_REG);
    }
    //methods to read and write to DS1307 memory
    
    /**
     * Write a byte at addr
     * @param addr
     * @param data
     * @throws IOException 
     */
    public void writeByte(int addr, byte data) throws IOException{
        //Check if address within range 0x08-0x3F
        if(addr<0x40 && addr>0x07){
            rtc.write(addr,data);
        }
    }
    /**
     * Writes an Int as 4 byte in addr
     * @param addr
     * @param data
     * @throws IOException 
     */
    public void writeInt(int addr, int data) throws IOException{
        if(addr<0x40 && addr>0x07){
            byte[] bytes = intToBytes(data);
            for(int i=0;i<4;i++)
                writeByte(addr++, bytes[i]);
        }
    }
    /**
     * Returns byte from memory addr
     * @param addr
     * @return byte
     * @throws IOException 
     */
    public byte readByte(int addr) throws IOException{
        if(addr<0x40 && addr>0x07){
            return((byte) rtc.read(addr));
        } else 
            return(0);
    }
    /**
     * Returns int from memory addr
     * @param addr
     * @return
     * @throws IOException 
     */
    public int readInt(int addr) throws IOException {
        if (addr < 0x40 && addr > 0x07) {
            byte[] bytes = new byte[]{0x00,0x00,0x00,0x00};
            for(int i=0;i<4;i++){
                bytes[i]=(byte)rtc.read(addr++);
            }
            return (ByteBuffer.wrap(bytes).getInt());
        } else {
            return (0);
        }
    }
    /**
     * Writes a string to memory addr
     * @param addr
     * @param data
     * @throws IOException 
     */
    public void writeString(int addr,String data) throws IOException{
        if(addr < 0x40 && addr > 0x07){
            byte[] bytes = new byte[data.length()];
            bytes = data.getBytes();
            for(int i=0; i < data.length(); i++){
                writeByte(addr++,bytes[i]);
            }
            writeByte(addr,(byte)0x00);
            
        }
    }
    /**
     * Reads a String from memory addr
     * @param addr
     * @return
     * @throws IOException 
     */
    public String readString(int addr) throws IOException {
        if (addr < 0x40 && addr > 0x07) {
            byte[] bytes = new byte[56];
            int i = 0;
            byte data = (byte) rtc.read(addr++);
            while (data != 0x00) {
                bytes[i++] = data;
                data = (byte) rtc.read(addr++);
            }
            String s = new String(bytes,"UTF-8");
            return (s);
        } else
            return(null);
    }
    
    private byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }
    
    public int StringToBCD(String s) {
        int len = s.length();
        int num = Character.getNumericValue(s.charAt(0));
        
        for (int i = 0; i < len-1; i++) {
            num = num + num << 3;
            num = num + Character.getNumericValue(s.charAt(i+1));
        }
        return num;
    }
 
}


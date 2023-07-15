
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
     * Get time
     * @return String with current time in format hh:mm:ss
     * 
     */
    public synchronized String getTime(){
        
        String data = null;
        
     
            String hour = String.format("%02x", getHour());
            String minutes = String.format("%02x", getMinutes());
            String seconds = String.format("%02x", getSeconds());
            data = hour+":"+minutes+":"+seconds;
        
        return data;
    }
   
    /**
     * Return RTC date in Calendar format
     * @return Calendar date format
     */
   public Calendar getCalendarRTC(){
   
       Calendar date=Calendar.getInstance();
       date.clear();
              
        
            int year=Integer.parseInt(String.format("%02x", getYear()))+2000;
            int month=Integer.parseInt(String.format("%02x", getMonth()));
            int day=Integer.parseInt(String.format("%02x", getDate1()));
            int hour=Integer.parseInt(String.format("%02x", getHour()));
            int minutes=Integer.parseInt(String.format("%02x", getMinutes()));
            int seconds=Integer.parseInt(String.format("%02x", getSeconds()));
            
            date.set(year,month,day,hour,minutes,seconds);
            date.set(Calendar.ZONE_OFFSET, readInt(ZONE));
            
           
        
        return date;
   }
   /**
    * Sets RTC date 
    * @param date Calendar format
    */  
   public void setCalendarRTC(Calendar date){
   
        
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
            
        
       
   }
    
    /**
     * Return RTC date as String
     * @return String Date in "mm/dd/yyyy" format
     * 
     */
    public synchronized String getDate(){
        
        String data = null;
        
       
            String day = String.format("%02x", getDate1());
            String month = String.format("%02x", getMonth());
            String year = String.format("%02x", getYear());
            data = month+"/"+day+"/"+year;
        
        return data;
    }
    /**
     * Set RTC date
     * @param date String "dd/mm/yy" format
     * 
     */
    public synchronized void setDate(String date){
        String[] parts = date.split("/");
        
        if (parts.length == 3) {
            
                String day = parts[0];
                String month = parts[1];
                String year = parts[2];
                
                setDate(StringToBCD(day));
                setMonth(StringToBCD(month)); 
                setYear(StringToBCD(year));
            
        }
    }
    
    /**
     * Set RTC time 
     * @param time hh:mm:ss format
     *
     */
    public synchronized void setTime(String time){
        
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
       
    }
    /**
     * Set seconds
     * @param secs int 0..59
     * 
     */
    private void setSeconds(int secs) {
        
        try{
        if(secs<96){
         rtc.write(SECONDS_REG, (byte)secs);   
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set seconds");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } 
    
    /**
     * Set minute
     * @param min int 0..59
     * 
     */
    private void setMinutes(int min){
        
        try{
        if(min<96){
            rtc.write(MINUTES_REG,(byte)min);
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set minutes");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Set hour
     * @param hour int 0..23
     * 
     */
    private void setHour(int hour){
    
        try{
        if(hour<36){
            rtc.write(HOUR_REG,(byte)hour);
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set hour");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Set day of week
     * @param day int between 1-7
     * 
     */
    private void setDay(int day) {
        
        try{
        if(day<8){
            rtc.write(DAY_REG,(byte)day);
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set days");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Set day of month
     * @param date int 1..31
     * 
     */
    private void setDate(int date){
        
        try{
        if(date<50){
            rtc.write(DATE_REG,(byte)date);   
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set date");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Set month
     * @param month int < 13
     * 
     */
    private void setMonth(int month) {
        
        try{
        if(month<19){
            month=month;
            rtc.write(MONTH_REG, (byte)month);
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set month");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Set Year
     * @param year
     */
    private void setYear(int year){
    
        try{
        rtc.write(YEAR_REG,(byte)year);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot set year");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int getSeconds() {
    
        int sec=0;
        try{
            sec=rtc.read(SECONDS_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read seconds register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sec;
    }
    
    private int getMinutes(){
    
        int min=0;
        try{
            min=rtc.read(MINUTES_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read minutes register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return min;
    }
    
    private int getHour(){
        
        int hour=0;
        try{
            hour=rtc.read(HOUR_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read hour register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hour;
    }
    
    private int getDay(){
    
        int day=0;
        try{
            day=rtc.read(DAY_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read day register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return day;
    }
    
    private int getDate1(){
    
        int date=0;
        try{
            date=rtc.read(DATE_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read date register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
    }
    
    private int getMonth(){
    
        int mon=0;
        try{
            mon=rtc.read(MONTH_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read month register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mon;
    }
    
    private int getYear(){
    
        int year=0;
        try{
            year=rtc.read(YEAR_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read year register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return year;
    }
    
    // Methods to handle DS1307 OUT pin
    
    /**
     * Turns On OUT pin 1Hz
     * 
     */
    public void blink(){
        
        try{
            rtc.write(CONTROL_REG,(byte)OUT_1HZ);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot turn on 1 sec LED");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Turns Off OUT pin
     * 
     */
    public void out_off(){
        try{
            rtc.write(CONTROL_REG,(byte)OUT_OFF);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot turn off 1 sec LED");
        }
    }
    /**
     * Turns On OUT pin
     * 
     */
    public void out_on(){
        
        try{
            rtc.write(CONTROL_REG,(byte)OUT_ON);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot turn on 1 sec LED");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int getControlReg(){
        
        int reg=0;
        try{
            reg=rtc.read(CONTROL_REG);
        }catch(IOException ex){
            System.out.println("DS1307 Error. Cannot read Control Register");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return reg;
    }
    //methods to read and write to DS1307 memory
    
    /**
     * Write a byte at addr memory of DS1307
     * @param addr Memory address 0x08..0x3F
     * @param data Byte to be saved
     * 
     */
    public void writeByte(int addr, byte data){
        
        try{
        //Check if address within range 0x08-0x3F
        if(addr<0x40 && addr>0x07){
            rtc.write(addr,data);
        }
        }catch(IOException ex){
            System.out.println("DS1307 Error writing to memory");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Writes an Int as 4 byte in addr
     * @param addr Memory address
     * @param data int to be saved
     * 
     */
    public void writeInt(int addr, int data) {
        
        
        if(addr<0x40 && addr>0x07){
            byte[] bytes = intToBytes(data);
            for(int i=0;i<4;i++)
                writeByte(addr++, bytes[i]);
        }
       
    }
    /**
     * Returns byte from memory addr
     * @param addr memory address
     * @return byte byte read
     * 
     */
    public byte readByte(int addr){
        
        byte data=0;
        try{
        if(addr<0x40 && addr>0x07){
            data=(byte)rtc.read(addr);
        } 
        }catch(IOException ex){
            System.out.println("DS1307 Error reading memory");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    /**
     * Returns int from memory addr
     * @param addr memory address
     * @return integer
     * 
     */
    public int readInt(int addr) {
        
        int data=0;
        
        try{
        if (addr < 0x40 && addr > 0x07) {
            byte[] bytes = new byte[]{0x00,0x00,0x00,0x00};
            for(int i=0;i<4;i++){
                bytes[i]=(byte)rtc.read(addr++);
            }
            data= ByteBuffer.wrap(bytes).getInt();
        } 
        }catch(IOException ex){
            System.out.println("DS1307 Error reading memory");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    /**
     * Writes a string to memory addr
     * @param addr memory address
     * @param data String
     * 
     */
    public void writeString(int addr,String data){
        
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
     * @param addr memory address 0x08..0x3F
     * @return String
     *
     */
    public String readString(int addr) {
        
        String str=null;
        try{
        if (addr < 0x40 && addr > 0x07) {
            byte[] bytes = new byte[56];
            int i = 0;
            byte data = (byte) rtc.read(addr++);
            while (data != 0x00) {
                bytes[i++] = data;
                data = (byte) rtc.read(addr++);
            }
            str= new String(bytes,"UTF-8");
            return (str);
        } 
        }catch(IOException ex){
            System.out.println("DS1307 Error reading memory");
            Logger.getLogger(DS1307.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return str;
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


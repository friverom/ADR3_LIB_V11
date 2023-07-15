
package rpi_io;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import static com.pi4j.io.gpio.PinState.LOW;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *This class implements all methods necessary to handle all features of the 
 * RPI board. The board has a real time clock "DS1307", a Digital IO expander 
 * "MCP23017. This IC is configure to have 8 digital inputs and 8 digital
 * relays, and an analog to digital converter LTC2309 that can take 0..20ma, 
 * 0..5 Volts and 0..10 Volts signals
 * @author Federico
 */
public class RPI_IO {
    //Flags is use to implement boolean operation on bit status. It is a software
    //based digital port
    private int flags=0;
    //Setpoints are use to compare values in software
    private double[] setpoints = new double[8];
    private DS1307 rtc = null; //Real time clock
    private LTC2309 adc = null; //Analog to digital converter
    private MCP23017 gpio = null; //Digital IO expander
    
    //This variables handles the interrupt signal from de MCP23017
    private GpioController rpio = null;
    private GpioPinDigitalInput interrupt = null; //GPIO_05 will listen to the 
    //interrupt from MCP 23017
    private ArrayList<ArrayList<DigitalInputTask>> Listeners= null; //This is a callback class that
 
    //has methods to run interrupts on status of digital inputs
    
    public RPI_IO() {

        I2CBus i2c;
        try {
            //Create controllers for all IC's of RPI_Board
            i2c = I2CFactory.getInstance(I2CBus.BUS_1);
            rtc = new DS1307(i2c);
            gpio = new MCP23017(i2c);
            adc = new LTC2309(i2c);
            
            // create gpio controller
            rpio = GpioFactory.getInstance();

            // provision gpio pin #05 as an input pin with its internal pull up resistor enabled
            //This pin will input the interrupt from MCP23017
            interrupt = rpio.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_UP);

            // set shutdown state for this input pin
            interrupt.setShutdownOptions(true);
            
            //Create an ArrayList of Arraylist to save the listeners to all
            //8 digital inputs. This array holds the callback routines to handle
            //The interrupts of each digital input
           
            Listeners=new ArrayList<ArrayList<DigitalInputTask>>();
            for(int i=0;i<9;i++){
                Listeners.add(new ArrayList<>());
            }
            //Discard any interrupt on start up
            gpio.getIntFlag();
            gpio.getIntCaptureReg();
            
            //Initialize setpoints to 0
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
    
    /**
     * This method implements a listener for MCP23017 interrupt signal.
     * It takes a DigitalInputTask class that provide methods to callback routines
     * for each digital input on interrupt
     * @param input to be listened
     *  @param t DigitalInputTask Interface implementation
     */
    public void addIntListener(int input, DigitalInputTask t){
            //Add the listener to its corresponding ArrayList
            Listeners.get(input).add(t);
        
        
    // create and register gpio pin listener
        interrupt.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                //MCP23017 interrupt is active low
                if(event.getState()==LOW){
                    int int_flag=gpio.getIntFlag(); //Get Interrupt flag register
                    int input_port=gpio.getIntCaptureReg(); //Get capture port on interrput
                    process_interrupt(int_flag, input_port);
                }else{
                    //discard the low to high event on interrupt pin
                    int int_flag=gpio.getIntFlag();
                    int input_port=gpio.getIntCaptureReg();
                    
                }
            }

        });
    }
    // Determine which digital input has generated the interrupt
    private void process_interrupt(int flag, int register){
        int in=getDigitalInputNumber(flag); //Digital input that generates the interrupt
        int input=0;
        
        //Digital Input status at the interrupt moment
        if((flag & register) > 0){
            input=1;
        }
        //Loop through the Digital input arraylist to run the callback routines
        for(DigitalInputTask t:Listeners.get(in)){
            t.call_interrupt_task(input);
        }
        
    }
    
    //Get the digital input that generates the interrupt
    private int getDigitalInputNumber(int flag){
        
        switch(flag){
            case 1:
                return 1;
                
            case 2:
                return 2;
                
            case 4:
                return 3;
                
            case 8:
                return 4;
                
            case 16:
                return 5;
                
            case 32:
                return 6;
                
            case 64:
                return 7;
                
            case 128:
                return 8;
               
            default:
                return 0;
        }
    }
    
    /**
     * Turns OFF all relays
     */
    public void allRly_off(){
        gpio.setOFF_All();
    }

   /**
    * Turns ON all relays
    */
    public void allRly_on(){
        gpio.setON_All();
    }

    /**
     * Returns the output port status
     * @return int Output Port value
     */
    public int getRlyStatus(){
        int data = 0;
        data = gpio.getRLYS();
        return data;
    }

    /**
     * Turns ON specified relay output
     * @param r int Relay output. 1..8
     */
    public void setRly(int r) {
        gpio.setON_Rly(r);
    }

    /**
     * Turns OFF specified relay output
     * @param r int Relay output. 1..8
     */
    public void resetRly(int r) {
        gpio.setOFF_Rly(r);
    }

    /**
     * Toggles the output of the specified relay
     * @param r int Relay output. 1..8
     */
    public void toggleRly(int r) {
        gpio.toggle_Rly(r);
    }

    /**
     * Turns ON the specified relay for the specified time
     * @param r int Relay output. 1..8
     * @param t int Time in milliseconds
     */
    public void pulseRly(int r, int t) {
        gpio.pulseON_Rly(r, t);
    }

    /**
     * Toggles the relay for the specified time
     * @param r int Relay output. 1..8
     * @param time  int time in milliseconds
     */
    public void pulseToggle(int r, int time) {
        gpio.pulseToggle(r, time);
    }

   /**
    * Returns the digital input port value
    * @return Input port value
    */
    public int getInputs() {
        int data = 0;
        data = gpio.getInputs();
        return data;
    }
    
    /**
     * Sets the Relays output to the supplied value
     * @param value Port Value
     */
    public void setPort(int value){
       gpio.setPort(value);
    }
    
    /**
     * Returns the status of digital input supplied 
     * @param input Digital input. 1..8
     * @return boolean value
     */
    public boolean getInput(int input){
        boolean data=false;
        data=gpio.getInput(input);
        return data;
    }
    
    /**
     * Returns the analog conversion of supplied channel 
     * @param c Channel number. 1..8
     * @return int Analog conversion. 0..4096
     */
    public int getChannel(int c) {
        int data = 0;
        data = adc.getAnalogIn(c);
        return data;
    }

    /**
     * Get the rtc time in hh:mm:ss format
     * @return String format hh:mm:ss
     */
    public String getTime() {
        String time = null;
        time = rtc.getTime();
        return time;
    }

    /**
     * Returns the rtc date in Sring format dd/mm/yyyy
     * @return String format dd/mm/yyyy
     */
    public String getDate() {
        String date = null;
        date = rtc.getDate();
        return date;
    }

    /**
     * Sets the rtc time
     * @param s String format hh:mm:ss
     */
    public void setTime(String s) {
        rtc.setTime(s);
    }

    /**
     * Sets rtc date
     * @param s String format dd/mm/yy
     */
    public void setDate(String s) {
        rtc.setDate(s);
    }

    /**
     * Enables rtc Led to blink every second
     */
    public void blink_1Hz() {
        
            rtc.blink();
        
    }

    /**
     * Turns ON rtc led
     */
    public void out_on() {
        
            rtc.out_on();
        
    }

    /**
     * Turns OFF rtc led
     */
    public void out_off() {
        
            rtc.out_off();
        
    }
    /**
     * Returns rtc control register. See DS1307 datasheet
     * @return int Control Register value
     */
    public int getControlReg() {
        
        return rtc.getControlReg();
       
        }
    
    /** Saves String into DS1307 memory
     * @param a starting address
     * @param s String
     */
    public void writeString(int a, String s) {
       
            rtc.writeString(a, s);
        
    }

    /**
     * Retrieves STring from rtc memory
     * @param a integer address of String 0x08..0x3F
     * @return String
     */
    public String readString(int a) {
        String data = null;
        
            data = rtc.readString(a);
        
        return data;
    }

    /**
     * Saves an integer number to rtc memory
     * @param a starting address
     * @param i Integer value
     */
    public void writeInt(int a, int i) {
        
            rtc.writeInt(a, i);
       
    }

    /**
     * Retrieves an Integer value from memory
     * @param a address of integer 0x08..0x3F
     * @return int value
     */
    public int readInt(int a) {
                
        return rtc.readInt(a);
    }

    
    public void pulseRly(int r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Get the current Date in Calendar format
     * @return Calendar date
     */
    public Calendar getCalendarRTC() {
        
        return rtc.getCalendarRTC();
    }

    /**
     * Sets the rtc date 
     * @param d Date in Calendar format
     */
    public void setCalendarRTC(Calendar d) {
        rtc.setCalendarRTC(d);
    }
    
    /**
     * Get the flags register
     * @return Flags register value
     */
    public int getFlags(){
        return flags;
    }
    
    /**
     * Get status of flag
     * @param flag int Flag 1..8
     * @return boolean value
     */
    public boolean getFlag(int flag){
        if((getBit(flag)&flags)==0){
            return false;
        } else {
            return true;
        }
                
    }
    
    /**
     * Set the flag register to value
     * @param flags int value
     */
    public void setFlags(int flags){
        this.flags=flags;
    }
    
    /** Set individual flag to 1
     * @param flag int value of flag. 1..8
     */
    public void setFlag(int flag){
        flags=getBit(flag)|flags;
    }
    
    /**
     * Reset individual flag to 0
     * @param flag 1..8
     */
    public void resetFlag(int flag){
        flags=~getBit(flag)&flags;
    }
    
    /**
     * gets the setpoint array values
     * @return array of double
     */
    public double[] getSetpoints(){
        return setpoints;
    }
    
    /**
     * Get individual setpoint to value
     * @param setpoint double value
     * @return double value
     */
    public double getSetpoint(int setpoint) {
        if (setpoint > 0 && setpoint < 9) {
            return setpoints[setpoint - 1];
        } else {
            return 0.0;
        }
    }
    
    /**
     * Sets individual setpoint 
     * @param setpoint 1..8
     * @param value double
     */
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

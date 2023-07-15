/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_points;
import rpi_io.RPI_IO;
import rpi_points.AnalogInputAlerts;
import rpi_points.AnalogInputEvent;
import java.util.ArrayList;

/**
 * This class extends AnalogInput class. Add Warnings, alarms and listener 
 * capabilities.
 * @author Federico
 */
public class AnalogInputExt extends AnalogInput{

    private double hyst;
    private double low_warning;
    private double high_warning;
    private double low_alarm;
    private double high_alarm;
    private AnalogInputAlerts state;
    
    private ArrayList<AnalogInputEvent> Listeners;
    
    /**
     * Class Constructor.
     * @param num  Analog Input channel. 1..8
     * @param type AnalogInputType, input Sensing type. 0..20ma, 4..20ma, 0..5V, 0..10V
     * @param min Sensor minimum reading
     * @param max Sensor maximum reading
     * @param rpio RPI_IO Board instance
     */
    public AnalogInputExt(int num, AnalogInputType type, double min, double max, RPI_IO rpio){
        super(num, type, min, max,rpio);
        Listeners=new ArrayList<>();
        this.hyst=0.0; //Hysteresis value for change state
        this.low_warning=0.0;
        this.high_warning=0.0;
        this.low_alarm=0.0;
        this.high_alarm=0.0;
        this.state=AnalogInputAlerts.OK;
    }
    /**
     * Add Listener method for Warning and alarm detection
     * @param e AnalogInputEvent Listener callback routine
     */
    public void addListener(AnalogInputEvent e){
        Listeners.add(e);
    }
    /**
     * Remove Listener
     */
    public void removeListener(){
        Listeners.clear();
    }
    /**
     * Set Warning limits and no hysteresis
     * @param low warning limit
     * @param high warning limit
     */
    public void setWarnings(double low, double high){
        this.low_warning=low;
        this.high_warning=high;
    }
    /**
     * Set Warning limits and hysteresis value for changing state
     * @param low warning limit
     * @param high warning limit
     * @param hyst hysteresis value
     */
    public void setWarnings(double low, double high, double hyst){
        this.low_warning=low;
        this.high_warning=high;   
        this.hyst=hyst;
    }
    
    /**
     * Set Alarm limits and no hysteresis
     * @param low alarm limit
     * @param high alarm limit
     */
    public void setAlarms(double low, double high){
        this.low_alarm=low;
        this.high_alarm=high;
    }
    
    /**
     * Set Alarms limits and hysteresis value for changing states
     * @param low alarm limit
     * @param high alarm limit
     * @param hyst hysteresis value
     */
    public void setAlarms(double low, double high, double hyst){
        this.low_alarm=low;
        this.high_alarm=high;
        this.hyst=hyst;
    }
    /**
     * Returns the state of the last reading
     * @return AnalogInutAlerts state, OK, LOW_WARNING, LOW_ALARM, HIGH_Warning, HIGH_ALARM
     */
    public AnalogInputAlerts get_state(){
        return this.state;
    }
    /**
     * Reads the analog channel
     * @return double reading
     */
    public  double analog_read(){
        double read=super.analog_read();
        //Check if value trigger any warning or limits
        this.state=checkState(read);
        //Call listener routine to handle warning or alarm
        if(this.state!=AnalogInputAlerts.OK){
            for(AnalogInputEvent e:Listeners){
                e.call_analogInput_event(read,this.state);
            }
        }
        return read;
    }
    // State machine implementation to cycle through all posible states of
    //analog readings
    private AnalogInputAlerts checkState(double value){
        
        AnalogInputAlerts state=this.state;
        
        switch(this.state){
            case OK:
                state=stateOKcheck(value);
                break;
                
            case LOW_WARNING:
                state=stateLWcheck(value);
                break;
                
            case HIGH_WARNING:
                state=stateHWcheck(value);
                break;
                
            case LOW_ALARM:
                state=stateLAcheck(value);
                break;
                
            case HIGH_ALARM:
                state=stateHAcheck(value);
                break;
           
        }
        return state;
    }
    
    //State OK. Check if readings is in Warning
    private AnalogInputAlerts stateOKcheck(double value){
        
        AnalogInputAlerts state=AnalogInputAlerts.OK;
        
        if((value-this.low_warning)<0.0){
            state=AnalogInputAlerts.LOW_WARNING;
        }else if((value-this.high_warning)>0.0){
            state=AnalogInputAlerts.HIGH_WARNING;
        }
        return state;
    }
    //Low Warning state
    private AnalogInputAlerts stateLWcheck(double value){
        
        AnalogInputAlerts state=AnalogInputAlerts.LOW_WARNING;
        
        if((value-this.low_alarm)<0.0){
            state=AnalogInputAlerts.LOW_ALARM;
        }else if((value-(this.low_warning+this.hyst))>0.0){
            state=AnalogInputAlerts.OK;
        }
        return state;
    }
    //Low Alarm state
    private AnalogInputAlerts stateLAcheck(double value){
        
        AnalogInputAlerts state=AnalogInputAlerts.LOW_ALARM;
        
        if((value-(this.low_alarm+this.hyst))>0.0){
            state=AnalogInputAlerts.LOW_WARNING;
        }
        return state;
    }
    //High Warning state
    private AnalogInputAlerts stateHWcheck(double value){
        
        AnalogInputAlerts state=AnalogInputAlerts.HIGH_WARNING;
        
        if((value-this.high_alarm)>0.0){
            state=AnalogInputAlerts.HIGH_ALARM;
        }else if((value-(this.high_warning-this.hyst))<0.0){
            state=AnalogInputAlerts.OK;
        }
    
        return state;
    }
    //High Alarm state
    private AnalogInputAlerts stateHAcheck(double value){
        
        AnalogInputAlerts state=AnalogInputAlerts.HIGH_ALARM;
        
        if((value-(this.high_alarm-this.hyst))<0.0){
            state=AnalogInputAlerts.HIGH_WARNING;
        }
        return state;
    }
    
    
    
}

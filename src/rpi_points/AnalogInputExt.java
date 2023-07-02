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
 *
 * @author Federico
 */
public class AnalogInputExt extends AnalogInput{

    private final double delta=0.02;
    private double hyst;
    private double low_warning;
    private double high_warning;
    private double low_alarm;
    private double high_alarm;
    private AnalogInputAlerts state;
    
    private ArrayList<AnalogInputEvent> Listeners;
    
    public AnalogInputExt(int num, AnalogInputType type, double min, double max, RPI_IO rpio){
        super(num, type, min, max,rpio);
        Listeners=new ArrayList<>();
        hyst=(max-min)*delta;
        low_warning=0.0;
        high_warning=0.0;
        low_alarm=0.0;
        high_alarm=0.0;
        state=AnalogInputAlerts.OK;
    }
    
    public void addListener(AnalogInputEvent e){
        Listeners.add(e);
    }
    public void setWarnings(double low, double high){
        this.low_warning=low;
        this.high_warning=high;
    }
    
    public void setAlarms(double low, double high){
        this.low_alarm=low;
        this.high_alarm=high;
    }
    
    public  double analog_read(){
        double read=super.analog_read();
        this.state=checkState(read);
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
        }else if((value-(this.low_warning-this.hyst))<0.0){
            state=AnalogInputAlerts.OK;
        }
        return state;
    }
    //High Alarm state
    private AnalogInputAlerts stateHAcheck(double value){
        
        AnalogInputAlerts state=AnalogInputAlerts.HIGH_ALARM;
        
        if((value-(this.high_warning-this.hyst))<0.0){
            state=AnalogInputAlerts.HIGH_WARNING;
        }
        return state;
    }
    
    
    
}

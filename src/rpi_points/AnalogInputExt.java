/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_points;
import rpi_io.RPI_IO;
import rpi_points.AnalogInputWarning;
import rpi_points.AnalogInputAlarms;
/**
 *
 * @author Federico
 */
public class AnalogInputExt extends AnalogInput{

    private double low_warning = 0.0;
    private double high_warning = 0.0;
    private double low_alarm = 0.0;
    private double high_alarm = 0.0;
    
    public AnalogInputExt(int num, AnalogInputType type, double min, double max, RPI_IO rpio){
        super(num, type, min, max,rpio);
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
        checkWarnings(read);
        return read;
    }
    
    private AnalogInputWarning checkWarnings(double value){
        
        if((value-this.low_warning)<0.0){
            return AnalogInputWarning.LOW;
        }
        
        if((this.high_warning-value)<0.0){
            return AnalogInputWarning.HIGH;
        }
        
        return AnalogInputWarning.OK;
    }
    
     private AnalogInputAlarms checkAlarms(double value){
        
        if((value-this.low_alarm)<0.0){
            return AnalogInputAlarms.LOW;
        }
        
        if((this.high_alarm-value)<0.0){
            return AnalogInputAlarms.HIGH;
        }
        
        return AnalogInputAlarms.OK;
    }
    
}

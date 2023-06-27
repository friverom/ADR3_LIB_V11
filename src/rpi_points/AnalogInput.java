

package rpi_points;
import rpi_io.RPI_IO;


/**
 *
 * @author Federico
 */
public class AnalogInput {
    
    private final int analog_input_num;
    private final AnalogInputType type;
    private final double sensor_min;
    private final double sensor_max;
    private final double sensor_span;
    private double span;
    private double factor=1.0;
    private double zero=0;
    private RPI_IO rpio;
    
    public AnalogInput(int num, AnalogInputType type, double min, double max, RPI_IO rpio){
        this.analog_input_num=num;
        this.type=type;
        this.sensor_min=min;
        this.sensor_max=max;
        this.sensor_span=max-min;
        this.rpio=rpio;
        adjust_input();
    }
    
    private void adjust_input(){
        switch(this.type){
            case TYPE_4_20mA:
                this.factor=16/(4096-820);
                this.span=4096-820;
                this.zero=4.0;
                break;
                
            case TYPE_0_20mA:
                this.factor=20/4096;
                this.span=4096;
                this.zero=0.0;
                break;
                
            case TYPE_0_5V:
                this.factor=5/4096;
                this.span=4096;
                this.zero=0.0;
                break;
                
            case TYPE_0_10V:
                this.factor=10/4096;
                this.span=4096;
                this.zero=0.0;
                break;
                
            default:
                this.factor=1.0;
                this.span=4096;
                this.zero=0.0;
        }
    }
    
    public double analog_read(){
        //Get analog reading and convert to sensor type
        int read=this.rpio.getChannel(this.analog_input_num);
        double value=read*this.factor+this.zero;
        
        //Convert reading to sensor value
        double sensor_value=(value/(this.factor*this.span))*this.sensor_span+this.sensor_min;
        
        return sensor_value;
    }
}

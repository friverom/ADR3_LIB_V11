

package rpi_points;
import rpi_io.RPI_IO;


/**
 * Implements an Analog data point
 * @author Federico
 */
public class AnalogInput {
    
    private final int analog_input_num; //Channel number
    private final AnalogInputType type; //Type of sensing transmitter
    //Variables of sensing range
    private final double sensor_min;
    private final double sensor_max;
    private final double sensor_span;
    //Variables of sensing transmitter
    private double tx_min;
    private double tx_span;
    //Variables of ADC conversion
    private double adc_min;
    private double adc_span;
    
    private RPI_IO rpio;
    
    /**
     * Analog Input Constructor.
     * @param num Analog Channel 1..8
     * @param type AnalogInputType, 0..20mA, 4..20mA, 0..5V, 0..10V
     * @param min double Sensor minimum sensing value
     * @param max double Sensor maximum sensing value
     * @param rpio RPI_IO Board implementation
     */
    public AnalogInput(int num, AnalogInputType type, double min, double max, RPI_IO rpio){
        this.analog_input_num=num;
        this.type=type;
        this.sensor_min=min;
        this.sensor_max=max;
        this.sensor_span=max-min;
        this.rpio=rpio;
        adjust_input();
    }
    
    //Compute conversion values depending on type of transmitter
    private void adjust_input(){
        switch(this.type){
            case TYPE_4_20mA:
                this.tx_min=4;
                this.tx_span=16;
                this.adc_min=820;
                this.adc_span=4096-820;
                break;
                
            case TYPE_0_20mA:
                this.tx_min=0;
                this.tx_span=20;
                this.adc_min=0;
                this.adc_span=4096;
                break;
                
            case TYPE_0_5V:
                this.tx_min=0;
                this.tx_span=5;
                this.adc_min=0;
                this.adc_span=4096;
                break;
                
            case TYPE_0_10V:
                this.tx_min=0;
                this.tx_span=10;
                this.adc_min=0;
                this.adc_span=4096;
                break;
                
            default:
                this.tx_min=0;
                this.tx_span=1;
                this.adc_min=0;
                this.adc_span=4096;
        }
    }
    
    /**
     * Returns a double value with reading adjusted to sensing range
     * @return double Reading
     */
    public double analog_read(){
        //Get analog reading and convert to sensor type
        int read=this.rpio.getChannel(this.analog_input_num);
        double value=((read-this.adc_min)/this.adc_span)*this.tx_span+this.tx_min;
        
        //Convert reading to sensor value
        double sensor_value=((value-this.tx_min)/this.tx_span)*this.sensor_span+this.sensor_min;
        
        return sensor_value;
    }
}

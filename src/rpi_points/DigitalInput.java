

package rpi_points;
import rpi_io.RPI_IO;

/**
 *
 * @author Federico
 */
public class DigitalInput {
    private int input;
    private RPI_IO rpio;
    
    public DigitalInput(int in, RPI_IO rpio){
        this.input=in;
        this.rpio=rpio;
    }
    
    public boolean value(){
        boolean value=this.rpio.getInput(this.input);
        return value;
    }

}

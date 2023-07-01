

package rpi_points;
import rpi_io.RPI_IO;
import rpi_io.DigitalInputTask;

/**
 *
 * @author Federico
 */
public class DigitalInput {
    public int input;
    private RPI_IO rpio;
    
    public DigitalInput(int in, RPI_IO rpio){
        this.input=in;
        this.rpio=rpio;
    }
    
    public void addListener(DigitalInputTask i){
        rpio.addIntListener(this.input, i);
    }
    
    public boolean value(){
        boolean value=this.rpio.getInput(this.input);
        
        return value;
    }

}

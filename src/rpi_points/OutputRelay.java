

package rpi_points;
import rpi_io.RPI_IO;

/**
 *
 * @author Federico
 */
public class OutputRelay {
    
    private int Relay;
    private RPI_IO rpio;
    
    public void OutputRelay(int rly,RPI_IO rpio){
        this.Relay=rly;
        this.rpio=rpio;
    }
    
    public void set(){
        rpio.setRly(this.Relay);
    }
    
    public void reset(){
        rpio.resetRly(this.Relay);
    }

}

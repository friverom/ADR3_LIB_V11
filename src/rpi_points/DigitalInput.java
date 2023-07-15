

package rpi_points;
import rpi_io.RPI_IO;
import rpi_io.DigitalInputTask;

/**
 *This class implements a Digital Input point.
 * @author Federico
 * 
 */
public class DigitalInput {
    public int input; //Digital Input number 1..8
    private RPI_IO rpio;
   
    /**
     * Constructor
     * @param in Digital Input number. 1..8
     * @param rpio RPI_IO Board instance
     */
    public DigitalInput(int in, RPI_IO rpio){
        this.input=in;
        this.rpio=rpio;
    }
    
    /**
     * Adds a Listener to digital input. RPI Board generates an interrupt on
     * input going HIGH and other interrupt when going LOW
     * @param i DigitalInputTask callback routine
     */
    public void addListener(DigitalInputTask i){
        rpio.addIntListener(this.input, i);
    }
    
    /**
     * Returns the status of the digital input
     * @return boolean, status
     */
    public boolean value(){
        boolean value=this.rpio.getInput(this.input);
        
        return value;
    }

}

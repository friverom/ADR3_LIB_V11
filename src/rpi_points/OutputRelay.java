

package rpi_points;
import rpi_io.RPI_IO;

/**
 *Implements a Relay Output point
 * @author Federico
 */
public class OutputRelay {

    private int relay; //Relay number 1..8
    private RPI_IO rpio;
    
    /**
     * Constructor.
     * @param int rly, Relay number 1..8
     * @param RPI_IO rpio, board instance 
     */
    public OutputRelay(int rly, RPI_IO rpio){
        this.relay=rly;
        this.rpio=rpio;
    }
    
    /**
     * Set Relay ON
     */
    public void set(){
        this.rpio.setRly(this.relay);
    }
    
    /**
     * Set Relay ON for time provided
     * @param int time, time in milliseconds
     */
    public void set(int time){
        this.rpio.pulseRly(this.relay,time);
        
    }
    
    /**
     * Reset Relay to OFF 
     */
    public void reset(){
        this.rpio.resetRly(this.relay);
    }
    
    /**
     * Toggles Relay for time provided
     * @param int time, time in milliseconds
     */
    public void toggle(int time){
        this.rpio.pulseToggle(this.relay,time);
    }
    
    /**
     * Toggle Relay output
     */
    public void toggle(){
        this.rpio.toggleRly(this.relay);
    }
}

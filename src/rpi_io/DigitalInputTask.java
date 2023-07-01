

package rpi_io;

/**
 * This interface is a callback to the application interrupt routine.
 * @param int input is the number of the physical input 1..8
 * @author Federico
 */
public interface DigitalInputTask {
    
    public void call_interrupt_task(int input);

}

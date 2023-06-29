/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_points;
import rpi_io.RPI_IO;

/**
 *
 * @author Federico
 */
public class OutputRelay {

    private int relay;
    private RPI_IO rpio;
    
    public OutputRelay(int rly, RPI_IO rpio){
        this.relay=rly;
        this.rpio=rpio;
    }
    
    public void set(){
        this.rpio.setRly(this.relay);
    }
    
    public void set(int time){
        this.rpio.pulseRly(this.relay,time);
        
    }
    
    public void reset(){
        this.rpio.resetRly(this.relay);
    }
    
    public void toggle(int time){
        this.rpio.pulseToggle(this.relay,time);
    }
    
    public void toggle(){
        this.rpio.toggleRly(this.relay);
    }
}

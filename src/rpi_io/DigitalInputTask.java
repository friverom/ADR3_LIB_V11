/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_io;

/**
 *
 * @author Federico
 */
public interface DigitalInputTask {
    
    public void call_interrupt_task(int flag, int capture);

}

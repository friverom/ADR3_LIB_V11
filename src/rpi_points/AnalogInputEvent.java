/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_points;

/**
 *
 * @author Federico
 */
public interface AnalogInputEvent {
    
    public void call_analogInput_event(double value, AnalogInputAlerts e);

}



package rpi_serial;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.*;
import rpi_serial.RS_232_Driver;
import java.io.IOException;

/**
 * This class implements a driver for the RS-485 channel
 * @author Federico
 */
public class RS_485_Driver extends RS_232_Driver{
    private GpioController gpio = null;
    private GpioPinDigitalOutput tx_en = null;   
    /**
     * Class constructor.
     */
    public RS_485_Driver() throws IOException, InterruptedException{
        //Instantiate Serial port
        super();
       
        //Instantiate GPIO 04 to drive RS-485 TX Enable
        gpio = GpioFactory.getInstance();
        tx_en = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "TX_EN", PinState.HIGH);
                
    }
    
    /**
     * Transmit a String on the serial channel.The method waits until all data
     * has been tranmitted
     * @param String data. Data String to be transmitted.
     */
    public void send(String data) {
        
        tx_en.low(); //Take RS85 bus
        super.send(data);
        tx_en.high(); //Release bus
    }
    
    /**
     * Returns a byte array of data receive
     */
    public byte[] receive() throws IOException{
        return super.receive();
    }
    
    /**
     * Adds a Callback routine to handle serial receptions.
     * @param SerialDataEventListener, Must implement this interface
     */
    public void addListener(SerialDataEventListener e){
        super.addListener(e);
    }
    
    
    /**
     * Clears the TX and RX buffers
     * 
     */
    public void discardBuffer() throws IOException{
        serial.discardInput();
        serial.discardData();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;

/**
 *
 * @author admin
 */
public abstract class ImageProcessingTask extends Thread
{

    public abstract void run(); 
    public abstract long getSize();
    public abstract long getPosition(); 
    public abstract boolean isFinished();
  
}

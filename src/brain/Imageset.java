/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author admin
 */
public class Imageset 
{
     private ArrayList<ImageInst> inst;

    public Imageset() 
    {
        this.inst = new ArrayList<ImageInst>();
    }

    
    public int getSize() 
    {
        return inst.size();
    }

    public void add(ImageInst in) 
    {
        inst.add(in);
    }
    
    public List<ImageInst> getImages() 
    {
        return inst;
    }
}

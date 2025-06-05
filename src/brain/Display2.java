/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;

import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.RenderedImage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author admin
 */
public class Display2 extends JPanel implements AdjustmentListener
{
  
    protected PixelInfo dj1;
 
    protected PixelInfo dj2;
 
    protected JScrollPane jsp1;
 
    protected JScrollPane jsp2;
  
 
    public Display2(RenderedImage im1,RenderedImage im2)
    {
        super();
        setLayout(new GridLayout(1,2));
        dj1 = new PixelInfo(im1); 
        dj2 = new PixelInfo(im2); 
        jsp1 = new JScrollPane(dj1); 
        jsp2 = new JScrollPane(dj2); 
        add(jsp1);
        add(jsp2);
   
        jsp1.getHorizontalScrollBar().addAdjustmentListener(this);
    
        jsp1.getVerticalScrollBar().addAdjustmentListener(this);
    
        jsp2.getHorizontalScrollBar().addAdjustmentListener(this);
    
        jsp2.getVerticalScrollBar().addAdjustmentListener(this);
    }

 
    public void setImage1(RenderedImage newimage)
    {
        dj1.set(newimage);
        repaint();
    }
  
 
    public void setImage2(RenderedImage newimage)
    {
        dj2.set(newimage);
        repaint();
    }

 
    public RenderedImage getImage1()
    {
        return dj1.getSource();
    }

 
    public RenderedImage getImage2()
    {
        return dj2.getSource();
    } 

    public PixelInfo getDisplayJAIComponent1()
    {
        return dj1;
    }
  
 
    public PixelInfo getDisplayJAIComponent2()
    {
        return dj2;
    }

 
    public void adjustmentValueChanged(AdjustmentEvent e)
    {
    
        if (e.getSource() == jsp1.getHorizontalScrollBar())
        {    
            jsp2.getHorizontalScrollBar().setValue(e.getValue());
        }
    
        if (e.getSource() == jsp1.getVerticalScrollBar())
        {
    
            jsp2.getVerticalScrollBar().setValue(e.getValue());
        }
    
        if (e.getSource() == jsp2.getHorizontalScrollBar())
        {
    
            jsp1.getHorizontalScrollBar().setValue(e.getValue());
        }
    
        if (e.getSource() == jsp2.getVerticalScrollBar())
        {
    
            jsp1.getVerticalScrollBar().setValue(e.getValue());
        }
    } 

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.Timer;

/**
 *
 * @author admin
 */
public class Display1 extends JFrame implements ActionListener
{
  
    private PlanarImage input,fuzzy;
  
    private Display2 display;
  
    private JButton start;
  
    private JSlider numClustersSlider;
  
    private JSlider fuzzinessSlider;
  
    private float[] fuzzinessValues = {1,1.2f,1.5f,2,2.5f,3,5,10,25,75};
  
    private JSlider maxIterationsSlider;
  
    private int[] maxIterationsValues = {2,5,10,20,50,100,200,500,1000};
  
    private JSlider epsilonSlider;
  
    private int[] epsilonValues = {0,1,5,10,50,100,500,1000};
  
    private Font labelsFont = new Font("Dialog",0,9);
  
    private JProgressBar progressBar;
  
    private JLabel infoLabel;
  
    private Timer monitor;
    private JSeparator     jSeperator1,jSeperator2;
  
    private IterateImage1 task;

  
  
    private File f;
    private JFileChooser fileChooser;

 
    public Display1(String ifile)
    {
    
        input = JAI.create("fileload", ifile);
   
        jSeperator1 = new JSeparator();
        jSeperator2 = new JSeparator();
                
        fileChooser= new JFileChooser("./");
        fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
        fileChooser.setDialogType(fileChooser.CUSTOM_DIALOG);

        fuzzy = new TiledImage(input,false);
   
        display = new Display2(input,fuzzy);
        getContentPane().add(display,BorderLayout.CENTER);
    
        start = new JButton("");
        start.addActionListener(this);
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setMaximumSize(new Dimension(250,25));
    
        numClustersSlider = new JSlider(2,50,8);
        Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
        for(int label=2;label<=50;label+=4)
        {
            JLabel aLabel = new JLabel(""+label);
            aLabel.setFont(labelsFont);
            labels.put(new Integer(label),aLabel);
        }
        numClustersSlider.setValue(10);
   
        fuzzinessSlider = new JSlider(0,9,3);
        labels = new Hashtable<Integer,JLabel>();
        for(int label=0;label<10;label++)
        {
            JLabel aLabel = new JLabel(""+fuzzinessValues[label]);
            aLabel.setFont(labelsFont);
            labels.put(new Integer(label),aLabel);
        }
        fuzzinessSlider.setValue(5);
        
        maxIterationsSlider = new JSlider(0,8,5);
        labels = new Hashtable<Integer,JLabel>();
        for(int label=0;label<9;label++)
        {
            JLabel aLabel = new JLabel(""+maxIterationsValues[label]);
            aLabel.setFont(labelsFont);
            labels.put(new Integer(label),aLabel);
        }
    
        maxIterationsSlider.setValue(2);
    
        epsilonSlider = new JSlider(0,7,1);
        labels = new Hashtable<Integer,JLabel>();
        for(int label=0;label<8;label++)
        {
            JLabel aLabel = new JLabel(""+epsilonValues[label]);
            aLabel.setFont(labelsFont);
            labels.put(new Integer(label),aLabel);
        }
        epsilonSlider.setValue(1);
   
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        
        infoLabel = new JLabel(" ");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setPreferredSize(new Dimension(250,25));
        
        Box controlPanel = Box.createVerticalBox();
        controlPanel.add(numClustersSlider);
        controlPanel.add(Box.createRigidArea(new Dimension(0,10)));
        controlPanel.add(fuzzinessSlider);
        controlPanel.add(Box.createRigidArea(new Dimension(0,10)));
        controlPanel.add(maxIterationsSlider);
        controlPanel.add(Box.createRigidArea(new Dimension(0,10)));
        controlPanel.add(epsilonSlider);
        controlPanel.add(Box.createRigidArea(new Dimension(0,10)));
        controlPanel.add(start);
        controlPanel.add(Box.createRigidArea(new Dimension(0,10)));
        controlPanel.add(progressBar);
        controlPanel.add(infoLabel);
        
        getContentPane().add(controlPanel,BorderLayout.EAST);
    
        monitor = new Timer(2000,this);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack(); 
        //setVisible(true); 
        
        numClustersSlider.setVisible(false);
        maxIterationsSlider.setVisible(false);
        fuzzinessSlider.setVisible(false);
        epsilonSlider.setVisible(false);
        progressBar.setVisible(false);
        start.setVisible(false);
        
        
        display();
    }
  
  
    public void display()
    {
        monitor.start();
      
        int nClusters = numClustersSlider.getValue();
        float fuzziness = fuzzinessValues[fuzzinessSlider.getValue()];
        int maxIter = maxIterationsValues[maxIterationsSlider.getValue()];
        task = new IterateImage1(input,nClusters,maxIter,fuzziness,0);
        progressBar.setMaximum(100);
      
        task.start();
      
        start.setEnabled(false);
      
      
        numClustersSlider.setEnabled(false);
        maxIterationsSlider.setEnabled(false);
        fuzzinessSlider.setEnabled(false);
        epsilonSlider.setEnabled(false);
        infoLabel.setVisible(false);
    }
    public void actionPerformed(ActionEvent e)
    {
    
        if (e.getSource() == start)
        {
      
            monitor.start();
      
            int nClusters = numClustersSlider.getValue();
            float fuzziness = fuzzinessValues[fuzzinessSlider.getValue()];
            int maxIter = maxIterationsValues[maxIterationsSlider.getValue()];
      
            task = new IterateImage1(input,nClusters,maxIter,fuzziness,0);
            progressBar.setMaximum(100); 
      
            task.start();
      
            start.setEnabled(false);
      
      
            numClustersSlider.setEnabled(false);
            maxIterationsSlider.setEnabled(false);
            fuzzinessSlider.setEnabled(false);
            epsilonSlider.setEnabled(false);
        }    
        else if (e.getSource() == monitor)
        {
      
            display.setImage2(task.getRankedMFImage(0));
            
            int percentage = (int)(100*task.getPosition()/task.getSize());
            progressBar.setValue(percentage);          
            
            if (task.isFinished())
            {
                start.setEnabled(true);        
                numClustersSlider.setEnabled(true);
                maxIterationsSlider.setEnabled(true);
                fuzzinessSlider.setEnabled(true);
                epsilonSlider.setEnabled(true);

                monitor.stop();

                try          
                {
                    ImageIO.write(task.getRankedMFImage(0),"PNG",new File("seg.jpg"));
                    ResultFrame rf=new ResultFrame();
                    rf.setVisible(true);
                    rf.setTitle("Segmented");
                    rf.setResizable(false);
                    rf.jLabel2.setIcon(new ImageIcon("seg.jpg"));
          
                } 
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }        
            }
        }
    }   
    public void windowClosing(WindowEvent w)
    {
	
        dispose();
    }
 
  
}

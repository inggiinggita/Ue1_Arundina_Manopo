// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// Vorgabe Übung 1 Bild- und Videokompression SS2014


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;


public class RLE extends JPanel {
	
	private static final String name = "<Arundina_Manopo>";	// TODO: insert your name(s) here
	
	private static final long serialVersionUID = 1L;
	private static final int borderWidth = 5;
	private static final int maxWidth = 400;
	private static final int maxHeight = maxWidth;
	private static final String initialFilename = "dilbert_8.png";
	private File openPath = new File(".");
	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView recView;			// reconstructed image view
	
	private JLabel mseLabel;			// to display calculated MSE
	
	private JLabel statusLine;			// to print some status text
	
	private enum FileType {
		NORMAL,
		RLE
	}
	private enum DialogType {
		OPEN,
		SAVE
	}
	
	//private HashMap<Integer, Integer> hm;
	//private ArrayList<Integer> al;
		
	public RLE() {
        super(new BorderLayout(borderWidth, borderWidth));

        setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
 
        // load the default image
        File input = new File(initialFilename);
        if(!input.canRead()) 
        	input = fileDialog(DialogType.OPEN, FileType.NORMAL); // file not found, choose another image
        
        // create image views
        srcView = new ImageView();
        recView = new ImageView();
				
        // control panel
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,0,0,0);

		// open image button
        JButton loadSrc = new JButton("Open Source Image");
        loadSrc.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		loadSrcFile(fileDialog(DialogType.OPEN, FileType.NORMAL));
        	}        	
        });
                 
		// save RLE image button
        JButton saveRle = new JButton("Save RLE Image");
        saveRle.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		saveRleFile(fileDialog(DialogType.SAVE, FileType.RLE));
        	}        	
        });
		
        // open RLE image button
        JButton loadRle = new JButton("Open RLE Image");
        loadRle.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		loadRleFile(fileDialog(DialogType.OPEN, FileType.RLE));
        	}        	
        });
                
        controls.add(loadSrc, c);
        controls.add(saveRle, c);
        controls.add(loadRle, c);
        
        // image panel
        JPanel images = new JPanel(new GridLayout(1,2));
        images.add(srcView);
        images.add(recView);
        
        // status panel
        JPanel status = new JPanel(new GridLayout());
        
        // some status text
        statusLine = new JLabel(" ");
        mseLabel = new JLabel(" ");
        status.add(statusLine, c);
        status.add(mseLabel, c);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        
        loadSrcFile(input);
	}
	
	
	private File fileDialog(DialogType dType, FileType fType) {
        JFileChooser chooser = new JFileChooser() {
			private static final long serialVersionUID = 1L;
			@Override
            public void approveSelection() {
                File file = getSelectedFile();
                if(file.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this,"Overwrite existing file?", "Existing file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        default:
                            return;
                    }
                }
                super.approveSelection();
            }        
        };
        
        FileNameExtensionFilter filter;
        if(fType == FileType.RLE)
        	filter = new FileNameExtensionFilter("RLE Images (*.run)", "run");
        else
        	filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        
        int ret;
        if(dType == DialogType.OPEN) 
        	ret = chooser.showOpenDialog(this);
        else
        	ret = chooser.showSaveDialog(this);
        
        if(ret == JFileChooser.APPROVE_OPTION) {
        	openPath = chooser.getSelectedFile().getParentFile();
        	return chooser.getSelectedFile();
        }
        
        return null;		
	}
	
	private void loadSrcFile(File file) {
		if(file == null) return;
		
		srcView.loadImage(file);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
		// create empty destination images
		recView.resetToSize(srcView.getImgWidth(), srcView.getImgHeight());
		
		displayMse();
		
		frame.pack();
	}
	
	private void loadRleFile(File file) {
		if(file == null) return;
		
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			decodeImage(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		recView.applyChanges();
		recView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
		displayMse();
		
		frame.pack();
	}
	
	private void saveRleFile(File file) {
		if(file == null) return;
		
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			encodeImage(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("RLE " + name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new RLE();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	/*
	 * Need to be encoded. Make the palette, index for the Run Length
	 * encoding.
	 */
	
	private void encodeImage(DataOutputStream out) throws IOException {
		
		
		long startTime = System.currentTimeMillis();
		
		int pixels[] = srcView.getPixels();
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		int index = 0; //index for color
		
		//int count = 0;
		int length = -1;
		int prePic = -1;
		int tmpPix = 0;
		
		HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
		ArrayList<Integer> al = new ArrayList<Integer>();
		
		//write width and height in writeInt
		out.writeInt(width);
		out.writeInt(height);
		
		
		for (int i= 0;i<pixels.length;i++){
			if(hm.containsKey(pixels[i])){
				
			}
			else{
				hm.put(pixels[i],index++); //put the argb and index to HashMap
				al.add(pixels[i]); //add the rgb to pallete in arraylist
			}
		}
		
		//write the pallete
		out.writeInt(al.size());
		
		//iterate all colors in arraylist
		for(Iterator<Integer>it = al.iterator(); it.hasNext();){
			out.writeInt(it.next());
		}
	
		//int tmpPix = -1;
		
		//write RLE
		for(int i = 0; i<pixels.length; i++){
			tmpPix = pixels[i]; //the first one
			//count = 1;
			
			//max 256
			if(i<pixels.length && tmpPix == prePic && length < 255){
				length++;
				//count++;
			}
			else{
				int current = hm.get(prePic);
				out.writeByte(current);
				out.writeByte(length);

				length = 0; // 
			}
			prePic= pixels[i];
		}
		
				out.close();

		
		long time = System.currentTimeMillis() - startTime;
    	statusLine.setText("Encoding time " + time + " ms.");
    	
	}

	private void decodeImage(DataInputStream in) throws IOException {
		long startTime = System.currentTimeMillis();
		
		// TODO: read width and height from DataInputStream

//		recView.resetToSize(width, height); // prepare view for given image size
		
		int pixels[] = recView.getPixels();
		int width = in.readInt();
		int height = in.readInt();
		recView.resetToSize(width, height);
		
		int numRuns = in.readInt();
		int index, length;
		int current = 0;
		
		// TODO: read remaining RLE data from DataInputStream and reconstruct image
		ArrayList<Integer> al2 = new ArrayList<Integer>();
		
		for(int i=0; i<numRuns;i++){
			al2.add(in.readInt());
		}
		
		while((index = in.read()) >= 0){
			length = in.read();
			for (int i = 0; i<length; i++){
				int x = al2.get(index);
				pixels[current++] = x;
				}
			
		}
		recView.applyChanges();
		
	}
	void displayMse() {
		
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		
		if(width != recView.getImgWidth() || height != recView.getImgHeight()) {
			mseLabel.setText(" ");
			return;
		}

		int srcPixels[] = srcView.getPixels();
		int recPixels[] = recView.getPixels();
		
		double mse = 0.0;
		
		//int sum = 0;
		
		//int rgbSrc, rgbRec;
		int redSrc, greenSrc, blueSrc;
		int redRec, greenRec, blueRec;
		
		for (int i=0; i<srcPixels.length;i++){
				
				//r,g,b values in srcPixels
				//rgbSrc = srcPixels.length;
				redSrc = srcPixels[i] >> 16 & 0xff;
				greenSrc = srcPixels[i]>> 8 & 0xff;
				blueSrc = srcPixels[i]  & 0xff;
				
				//rgb values in recPixels
				//rgbRec = recPixels.length;
				redRec = recPixels[i] >> 16 & 0xff;
				greenRec = recPixels[i] >> 8 & 0xff;
				blueRec = recPixels[i] & 0xff;
				
				//calculate mse
				mse += ((double)((redSrc-redRec)*(redSrc-redRec)+(greenSrc-greenRec)*
						(greenSrc-greenRec)+(blueSrc-blueRec)*(blueSrc-blueRec)))/3;
			}
				mse = mse/srcPixels.length;
				
				
		
		// TODO: calculate MSE between source image and reconstructed image
		
		// hint for RGB color images: 
		// calculate individual MSE values for R, B and G color planes
		// finally take the mean of the three calculated MSE values

		mseLabel.setText(String.format("MSE = %.1f", mse));
	}
}


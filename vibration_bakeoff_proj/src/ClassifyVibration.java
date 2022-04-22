import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import processing.core.PApplet;
import processing.sound.AudioIn;
import processing.sound.FFT;
import processing.sound.Sound;
import processing.sound.Waveform;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

/* A class with the main function and Processing visualizations to run the demo */

public class ClassifyVibration extends PApplet {

	FFT fft;
	AudioIn in;
	Waveform waveform;
	int bands = 2048; //512
	int nsamples = 2048; //1024
	float[] spectrum = new float[bands];
	float[] fftFeatures = new float[bands];
	String[] classNames = {"Quiet", "Writing on Board", "Erasing Marker"};
	int classIndex = 0;
	int dataCount = 0;

	MLClassifier classifier;
	
	Map<String, List<DataInstance>> trainingData = new HashMap<>();
	{for (String className : classNames){
		trainingData.put(className, new ArrayList<DataInstance>());
	}}
	
	DataInstance captureInstance (String label){
		DataInstance res = new DataInstance();
		res.label = label;
		res.measurements = fftFeatures.clone();
		return res;
	}
	
	public static void main(String[] args) {
		PApplet.main("ClassifyVibration");
	}
	
	public void settings() {
		size(512, 400);
	}

	public void setup() {
		
		/* list all audio devices */
		Sound.list();
		Sound s = new Sound(this);
		  
		/* select microphone device */
		s.inputDevice(5);
		    
		/* create an Input stream which is routed into the FFT analyzer */
		fft = new FFT(this, bands);
		in = new AudioIn(this, 0);
		waveform = new Waveform(this, nsamples);
		waveform.input(in);
		
		/* start the Audio Input */
		in.start();
		  
		/* patch the AudioIn */
		fft.input(in);
	}

	public void draw() {
		background(0);
		fill(0);
		stroke(255);
		
		waveform.analyze();

		beginShape();
		  
		for(int i = 0; i < nsamples; i++)
		{
			vertex(
					map(i, 0, nsamples, 0, width),
					map(waveform.data[i], -1, 1, 0, height)
					);
		}
		
		endShape();

		fft.analyze(spectrum);

		for(int i = 0; i < bands; i++){
			/* the result of the FFT is normalized */
			/* draw the line for frequency band i scaling it up by 40 to get more amplitude */
			line( i, height, i, height - spectrum[i]*height*40);
			fftFeatures[i] = spectrum[i];
		} 

		fill(255);
		textSize(30);
		if(classifier != null) {
			String guessedLabel = classifier.classify(captureInstance(null));
			text("classified as: " + guessedLabel, 20, 30);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Guessed label: " + (guessedLabel));
			
		}else {
			text(classNames[classIndex], 20, 30);
			dataCount = trainingData.get(classNames[classIndex]).size();
			text("Data collected: " + dataCount, 20, 60);
		}
	}
	
	public void keyPressed() {
		if (key == '.') {
			classIndex = (classIndex + 1) % classNames.length;
		}
		
		else if (key == 't') {
			// Train model and save test data
			if(classifier == null) {
				println("Start training ...");
				classifier = new MLClassifier();
				classifier.train(trainingData);
			}else {
				classifier = null;
			}
		}
		
		else if (key == 'l') {
	    	// Load model from saved test set
	    	 try {
	    		classifier = new MLClassifier();
	    		classifier.classifier = new SMO();
	    		ArrayList<String> class_names = new ArrayList<String>();
	    		class_names.add("Writing");
	    		class_names.add("Quiet");
	    		class_names.add("Erasing");
	    		classifier.featureCalc = new FeatureCalc(class_names); 
	   	    	 Instances inst = new Instances(
	   	    	                    new BufferedReader(
	   	    	                      new FileReader("./data/test_data.arff")));
	   	    	 inst.setClassIndex(inst.numAttributes() - 1);
	   	    	classifier.classifier.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 "
	   			         + "-P 1.0E-12 -N 0 -V -1 -W 1 "
	   			         + "-K \"weka.classifiers.functions.supportVector.PolyKernel "
	   			         + "-C 0 -E 1.0\""));
	   	    	classifier.classifier.buildClassifier(inst);
	   	    	classifier.classattr = inst.classAttribute();
	   	   	 }
	   	   	catch (Exception e) {
	   			e.printStackTrace();
	   		}
	    	        
	    	 
		}
			
		else {
			trainingData.get(classNames[classIndex]).add(captureInstance(classNames[classIndex]));
		}
	}

}

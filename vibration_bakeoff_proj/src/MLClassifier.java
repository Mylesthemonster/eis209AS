import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;



/* A wrapper class to use Weka's classifiers */

public class MLClassifier {
	FeatureCalc featureCalc = null;
    SMO classifier = null;
    Attribute classattr;
    Filter filter = new Normalize();

    public MLClassifier() {
    	
    }

    public void train(Map<String, List<DataInstance>> instances) {
    	
    	/* generate instances using the collected map of DataInstances */
    	
    	/* pass on labels */
    	featureCalc = new FeatureCalc(new ArrayList<>(instances.keySet()));
    	System.out.println("featureCalc: " + instances.keySet());
    	
    	/* pass on data */
    	List<DataInstance> trainingData = new ArrayList<>();
    	 
    	for(List<DataInstance> v : instances.values()) {
    		trainingData.addAll(v);
    	}
         
    	/* prepare the training dataset */
    	Instances dataset = featureCalc.calcFeatures(trainingData);
     
    	//save to test_data.arff file
	   	 ArffSaver saver = new ArffSaver();
	   	 saver.setInstances(dataset);
	   	 try{
	   		 saver.setFile(new File("./data/test_data.arff"));
	       	 saver.setDestination(new File("./data/test_data.arff"));
	       	 saver.writeBatch();
	   	 }
	   	
	   	 catch (Exception e) {
				e.printStackTrace();
		 }
    	
	   	 //load the training data
	   	 try {
	   		 classifier = new SMO();
	    	 Instances inst = new Instances(
	    	                    new BufferedReader(
	    	                      new FileReader("./data/test_data.arff")));
	    	 inst.setClassIndex(inst.numAttributes() - 1);
	    	 classifier.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 "
			         + "-P 1.0E-12 -N 0 -V -1 -W 1 "
			         + "-K \"weka.classifiers.functions.supportVector.PolyKernel "
			         + "-C 0 -E 1.0\""));
	    	 classifier.buildClassifier(inst);
	    	 this.classattr = inst.classAttribute();
	   	 }
	   	catch (Exception e) {
			e.printStackTrace();
		}
    }

    public String classify(DataInstance data) {
        if(classifier == null || classattr == null) {
            return "Unknown";
        }
    	// System.out.println("data " + data);
        Instance instance = featureCalc.calcFeatures(data);
        
        try {
			TimeUnit.MILLISECONDS.sleep(100);
            int result = (int) classifier.classifyInstance(instance);
            // System.out.println("Result: " + (result));
            return classattr.value((int)result);
            
        } catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
    
}
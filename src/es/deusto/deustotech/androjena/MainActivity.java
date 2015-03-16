package es.deusto.deustotech.androjena;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class MainActivity extends Activity {
	private ProgressDialog progressDialog;
	private Timer timer;
	private float draw;
	private float drained;
	private float Reasonerdrained;
	private float OntologyLoaderDrained;
	
	private String ontologyName, queryName;
	private long startCountingTime;
	private long stopCountingTime;
	private float timeElapsed;
	
	private BroadcastReceiver batteryInfoReceiver;
	private int mvoltage;
	private float watts;
	private float ReasonerdrainedWatts;
	private float OntologyLoaderDrainedWatts;



	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		progressDialog = new ProgressDialog(this); 
		// spinner (wheel) style dialog
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		// better yet - use a string resource getString(R.string.your_message)
		progressDialog.setMessage("Loading data"); 
		progressDialog.setCanceledOnTouchOutside(false);
		// display dialog
		progressDialog.show(); 
		Intent myIntent = getIntent(); // gets the previously created intent
		ontologyName = myIntent.getStringExtra("ontologyName"); // will return "ontologyName"
		queryName = myIntent.getStringExtra("queryName"); // will return "queryName"

		// start async task
		new MyAsyncTaskClass().execute();  
		
	}
	protected void onStop(){
		super.onStop();

	}
	
	
	private class MyAsyncTaskClass extends AsyncTask<Void, Void, Void> {
		 
        @Override
        protected Void doInBackground(Void... params) {
        	
        	
        	    // create lots of objects here and stash them somewhere
        	
           // do your thing
        	OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
    		//String inputFileName="univ-bench.owl";
        	File file = new File("storage/emulated/0/Download/" +ontologyName);

    		InputStream in = null;
			try {
				in = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    		
    		start();//Starts timer that calculates the mAh drained
    		startCountingTime= System.currentTimeMillis();
    		
    		
    		
    		
    		model.read(in, null);

    		String q1 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    				"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
    				"select * "+
    				"where {?X rdf:type ub:GraduateStudent . "+
    				"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";
    		
    		
    		String q2 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    				"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
    				"select * "+
    				"where {?X rdf:type ub:Student . "+
    				"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";
    				
    		String q3 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    				"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
    				"select *"+
    				"where {"
    				+ "?X rdf:type ub:Student"
    				+ "}";
    		
    		String q4 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    				"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "
    				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
    				"select *"+
    				"where {"
    				//+ "?x rdfs:subClassOf ub:Employee"
    				+ "?X rdfs:subClassOf ?Y"
    				+ "}";
    		

    			
    		String[]	queries		= null;
    		 
    	     if(queryName.equals("Query1")){
    	     	queries = new String[] {q1};
    	     }
    	     if(queryName.equals("Query2")){
    	      	queries = new String[] {q2};
    	     }
    	     if(queryName.equals("Query3")){
    	      	queries = new String[] {q3};
    	     }
    	     if(queryName.equals("Query4")){
    	        	queries = new String[] {q4};
    	       }
    				
    		boolean NOTmeasured = true;
	 		float PrewReasonerDrained = 0;
	 		float PrewReasonerDrainedWatts = 0;
	 		int qlength = queries.length;
	 		
	 		
	 		
    		for(int i= 0; i<qlength; i++){
    			try {	
	    				
	    			String queryString = queries[i];
	    			System.out.println(queryString);
		    		Query query = QueryFactory.create(queryString);
		    		QueryExecution qe = QueryExecutionFactory.create(query, model);
		    		//records how much loader drained of a battery
		    		if(NOTmeasured){
		   				//records how much loader drained of a battery
		   				OntologyLoaderDrained = drained;
		   				OntologyLoaderDrainedWatts = watts;	
		   				write("ontLoader",""+ OntologyLoaderDrained);
		   				write("PowerLoader",""+ OntologyLoaderDrainedWatts);
		   				NOTmeasured = false;
		   			}	    
		    		stopCountingTime = System.currentTimeMillis()-startCountingTime;	
					float timeElapsed2 = stopCountingTime;
					timeElapsed = timeElapsed2/1000;
					write("LoaderTime", "" +timeElapsed);
		    		startCountingTime= System.currentTimeMillis();
		    		
		    		
		    		com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
		    		
		    		//converts results to the string
		    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    		PrintStream ps = new PrintStream(baos);
		    		ResultSetFormatter.out(ps, results, query) ;
		    		String s = "";
		    		try {
						 s = new String(baos.toByteArray(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
		    		System.out.println(s);
		    		
		    		//records how much mAh reasoner drained.
					Reasonerdrained = drained - OntologyLoaderDrained- PrewReasonerDrained;
					//records how much watts reasoner drained
					ReasonerdrainedWatts = watts - OntologyLoaderDrainedWatts- PrewReasonerDrainedWatts;

					//keeps record of previous reasoner
					PrewReasonerDrained = PrewReasonerDrained + Reasonerdrained;
					PrewReasonerDrainedWatts = PrewReasonerDrainedWatts + ReasonerdrainedWatts;

					
					
					
		    		//System.out.println("There was " + OntologyLoaderDrained + "mAh" + " drained by ontology loader");
		    		//System.out.println("There was " + Reasonerdrained + "mAh" + " drained by reasoner");
		    		//System.out.println("Running : " + ontologyName);
		    		
		    		
		    		write("log", "________________________________________\n"+"Query: "+ queryName + "\n"+"AndroJena Reasoner " +Reasonerdrained+"mAh"+"\n"
		    		+ "AndroJena ont loader " + OntologyLoaderDrained +"mAh"+"\n" + "AndroJena Total: " +drained+"mAh"+ "\n"
		    		+"AndroJena Running : " + ontologyName+"\n Time Elapsed: "+timeElapsed+"s"+"WattsDrained"+watts+"W"+"\n________________________");
		    		write("justdata",""+ Reasonerdrained );
		    		write("PowerReasoner", ""+ ReasonerdrainedWatts);
		    		write("Results", s );
	
	
		    		qe.close();
    			} catch (OutOfMemoryError E) {
					System.err.println(E);
					quiteAnApp();
	    		}
    		}
			stopCountingTime = System.currentTimeMillis()-startCountingTime;	
			float timeElapsed2 = stopCountingTime;
			timeElapsed = timeElapsed2/1000;			//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
			write("ReasonerTime", "" +timeElapsed );
        	
			return null;
        }
 
        @Override
        protected void onPostExecute(Void results) {
            // put here everything that needs to be done after your async task finishes
            progressDialog.dismiss();
            stop();
            finishWithResult();
            finish();
            System.exit(0);
        }
        
}
	public  float bat(){		
        
				BatteryManager mBatteryManager =
						(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
						Long energy =
						mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);					
				float currentdraw = energy;
				draw = currentdraw;		
				return draw;
	}
	
	
	public void start() {
	    if(timer != null) {
	        return;
	    }
	    timer = new Timer();	   
	    timer.schedule(new TimerTask() {
	        public void run() {	            
	        	final float curret =bat();
	        	drained =drained +(curret/3300);//3300s instead 3600s because after calculations there 
	        	//were some error rate determined and diviation from 3300 covers the loss of data that
	        	//was missed to be recorded. Calculated by measuring amount of current drained per 1% and finding 
	        	//the constant that derives 31mah
	        	watts = (drained*mvoltage/1000)*3600/1000;
	        	runOnUiThread(new Runnable() {

	        	    @Override
	        	    public void run() {
	        	    	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	    				float timeElapsed = (float) (stopCountingTime/1000.0);	
	    				((TextView)findViewById(R.id.textView)).setText("Capacity Drained = " + drained + "mAh \n"+ 
			        			"Time elapsed : " +timeElapsed + "s\n"+"Voltage: "+mvoltage+"V"
			        					+ "\nPower used: "+watts+"W");
		        		//This if ABORTS the reasoning task because it took too long,
		        		if(timeElapsed>300||drained>45){
		        			quiteAnApp();
		        		}
	        	    }
	        	 });
	        	
	       }
	   }, 0, 1000);
	}
	public void stop() {
	    timer.cancel();
	    timer = null;
	}
	
	
	//File writter
	public void write(String fname, String fcontent){
        String filename= "storage/emulated/0/Download/"+fname+".txt";
        String temp = read(fname);
        BufferedWriter writer = null;
        try {
            //create a temporary file
            File logFile = new File(filename);

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            
            writer.write(temp + fcontent );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
   }
	
	//File reader
	   @SuppressWarnings("resource")
	public String read(String fname){
	     BufferedReader br = null;
	     String response = null;
	      try {
	        StringBuffer output = new StringBuffer();
	        String fpath = "storage/emulated/0/Download/"+fname+".txt";
	        br = new BufferedReader(new FileReader(fpath));
	        String line = "";
	        while ((line = br.readLine()) != null) {
	          output.append(line +"\n");
	        }
	        response = output.toString();
	      } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	      }
	      return response;
	   }
	   private void finishWithResult()
	   {
	      Bundle conData = new Bundle();
	      conData.putInt("results", 1);
	      Intent intent = new Intent();
	      intent.putExtras(conData);
	      setResult(RESULT_OK, intent);
	   }
	   public void quiteAnApp(){
		   
		   Reasonerdrained = drained-OntologyLoaderDrained;
		   ReasonerdrainedWatts = watts-OntologyLoaderDrainedWatts;

			write("log", "ABORTED due to Out Of Memory/Time \n"+"________________________________________\n"+"Query: "+ queryName + "\n"+"AndroJena Reasoner " +Reasonerdrained+"mAh"+"\n"
		    		+ "AndroJena ont loader " + OntologyLoaderDrained +"mAh"+"\n" + "AndroJena Total: " +drained+"mAh"+ "\n"
		    		+"AndroJena Running : " + ontologyName+"\n Time Elapsed: "+timeElapsed+"s"+"WattsDrained"+watts+"W"+"\n________________________");
		    		write("justdata", ""+Reasonerdrained);
		    		write("Results", "Results Aborted ");
		    		stopCountingTime = System.currentTimeMillis()-startCountingTime;	
					float timeElapsed2 = stopCountingTime;
					timeElapsed = timeElapsed2/1000;			//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
					write("ReasonerTime", "" +timeElapsed );
		            progressDialog.dismiss();
		    		stop();
		            finishWithResult();
		            finish();
		            System.exit(0);
	   }
	   
	   public void getVoltage(){
	       batteryInfoReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {			
					mvoltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);				
				}
			};
			registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	   
}
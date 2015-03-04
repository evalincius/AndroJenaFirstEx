package es.deusto.deustotech.androjena;

import java.io.IOException;
import java.io.InputStream;
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
	private BroadcastReceiver batteryInfoReceiver;
	private String firstKeyName;
	private String secondKeyName;



	

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
		 firstKeyName = myIntent.getStringExtra("firstKeyName"); // will return "FirstKeyValue"
		 secondKeyName= myIntent.getStringExtra("secondKeyName");
		// start async task
		new MyAsyncTaskClass().execute();  
		
	}
	
	private class MyAsyncTaskClass extends AsyncTask<Void, Void, Void> {
		 
        @Override
        protected Void doInBackground(Void... params) {
           // do your thing
        	OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
    		String inputFileName="univ-bench.owl";
    		//InputStream in = FileManager.get().open( inputFileName );
    		InputStream in = null;
    		try {
    			in = getAssets().open("b.owl");
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}

    		if (in == null) {
    		    throw new IllegalArgumentException(
    		         "File: " + inputFileName + " not found");
    		}
    		model.read(in, null);

    				String queryString = 
    				
    				"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    				"prefix ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> "+
    				"select * "+
    				"where {?X rdf:type ub:GraduateStudent . "+
    				"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";

    		Query query = QueryFactory.create(queryString);
    		QueryExecution qe = QueryExecutionFactory.create(query, model);
    		start();
    		com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
    		ResultSetFormatter.out(System.out, results, query);
    		Reasonerdrained = drained;
    		System.out.println("There was " + Reasonerdrained + "mAh" + " drained");
    		System.out.println("Intent" + firstKeyName);
    		qe.close();
    		
    		//finish();
    		
        	
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            // put here everything that needs to be done after your async task finishes
            progressDialog.dismiss();
            stop();
            finish();
            
        }
        
}
	public  float bat(){		
        registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryInfoReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {			
				int  plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
				String  technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
				int  temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
				int  voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);				
				
				BatteryManager mBatteryManager =
						(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
						Long energy =
						mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);					
				float currentdraw = energy;
				draw = currentdraw;		
				((TextView)findViewById(R.id.textView)).setText("              ANDROJENA REASONER"+"\n"+"Plugged: "+plugged+"\n"+
						"Technology: "+technology+"\n"+
						"Temperature: "+temperature+"\n"+
						"Voltage: "+voltage+"\n"+
						"Current mA = " + energy + "mA"+ "\n"+
						"AndroJena reasoner Drained = " + Reasonerdrained + "mA"+ "\n"+
						"Currentlly Drained = " + drained + "mAh"+ "\n");

			}
		};
		return draw;
	}
	
	
	public void start() {
	    if(timer != null) {
	        return;
	    }
	    timer = new Timer();	   
	    timer.schedule(new TimerTask() {
	        public void run() {	            
	           // draw = draw + (bat());
	        	float curret =bat(); 
	        	drained =drained +(curret/7200);
	            		//System.out.println("Current mA = " + curret + "mA"+ "\n"+
						//"Capacity Drained = " + drained + "mAh"+ "\n");
						
	    		//batteryInfo=(TextView)findViewById(R.id.textView);

	       }
	   }, 0, 500 );
	}
	public void stop() {
	    timer.cancel();
	    timer = null;
	}
}
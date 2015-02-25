package es.deusto.deustotech.androjena;

import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressDialog = new ProgressDialog(this); 
		// spinner (wheel) style dialog
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		// better yet - use a string resource getString(R.string.your_message)
		progressDialog.setMessage("Loading data"); 
		// display dialog
		progressDialog.show(); 
		 
		 
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
    			in = getAssets().open("full-lubm.owl");
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

    				
    				
    		        //"prefix pizza: <www.co-ode.org/ontologies/pizza/pizza.owl#> "+        
    		        //"prefix rdfs: <" + RDFS.getURI() + "> "           +
    		        //"prefix owl: <" + OWL.getURI() + "> "             +
    		        //"select *"+ 
    		        //"FROM <http://www.co-ode.org/ontologies/pizza/pizza.owl#>"+
    		        //"where {?Y rdfs:subClassOf ?X ."+
    		        //"?X owl:someValuesFrom pizza:MushroomTopping"+
    		        //"}";
    		Query query = QueryFactory.create(queryString);
    		QueryExecution qe = QueryExecutionFactory.create(query, model);
    		com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();

    		ResultSetFormatter.out(System.out, results, query);
    		
    		qe.close();
    		
    		finish();
    		
        	
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            // put here everything that needs to be done after your async task finishes
            progressDialog.dismiss();
        }
}
}
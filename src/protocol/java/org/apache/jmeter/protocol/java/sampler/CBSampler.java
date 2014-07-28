package com.bigstep;

 
import com.couchbase.client.CouchbaseClient;

import java.io.Serializable;
import java.io.File;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;



import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
//import java.util.logging.Logger;
 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
 
public class CBSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();
    private CouchbaseClient client = null;
   private  byte[] putContents=null;
 
    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("method", "GET");
        defaultParameters.addArgument("servers", "http://127.0.0.1:8091/pools");
        defaultParameters.addArgument("bucket", "default");
        defaultParameters.addArgument("password", "");
        defaultParameters.addArgument("key", "");
        defaultParameters.addArgument("local_file_path", "");
        return defaultParameters;
    }
   	
    @Override 
    public void setupTest(JavaSamplerContext context)
    {
/*	Properties systemProperties = System.getProperties();
	System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
        System.setProperties(systemProperties);

        java.util.logging.Logger.getLogger("net.spy.memcached").setLevel(Level.SEVERE);
        java.util.logging.Logger.getLogger("com.couchbase.client").setLevel(Level.SEVERE);
        java.util.logging.Logger.getLogger("com.couchbase.client.vbucket").setLevel(Level.SEVERE);
*/	
	try
	{
		String servers = context.getParameter( "servers" );
		String password = context.getParameter( "password" );
		String bucket = context.getParameter( "bucket" );
		String method = context.getParameter( "method" );
		String file = context.getParameter( "local_file_path" );

		if(method.equals("PUT"))
			putContents= Files.readAllBytes(Paths.get(file));	

		    // (Subset) of nodes in the cluster to establish a connection

		List<URI> hosts= new ArrayList<URI>();
		
		String[] arrServers=servers.split(","); 
		for(String server: arrServers)
			hosts.add(new URI(server));
				
		client =  new CouchbaseClient(hosts, bucket, password);
	}
	catch(Exception ex)
	{
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            ex.printStackTrace( new java.io.PrintWriter( stringWriter ) );

	     log.error("setupTest:"+ex.getMessage()+stringWriter.toString());
 
	}
			
    }
    
    @Override	
    public void teardownTest(JavaSamplerContext context)
    {
	if(null!=client)
		client.shutdown();		
    }
 
    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        String key = context.getParameter( "key" );
        String method = context.getParameter( "method" );
        
	SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch

         
        try {

		if(null==client)
			throw new Exception("CB Client not initialised");

	    if(method.equals("GET"))	
	 	   client.get(key);
	    else 
		if(method.equals("PUT"))
		{
		    client.set(key,putContents);
		}
	
    
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( true );
            result.setResponseMessage( "OK on object "+key );
            result.setResponseCodeOK(); // 200 code

        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( false );
            result.setResponseMessage( "Exception: " + e );
 
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace( new java.io.PrintWriter( stringWriter ) );
            result.setResponseData( stringWriter.toString() );
            result.setDataType( org.apache.jmeter.samplers.SampleResult.TEXT );
            result.setResponseCode( "500" );

	     log.error("runTest:"+e.getMessage()+" "+stringWriter.toString());
        }
 
        return result;
    }
}


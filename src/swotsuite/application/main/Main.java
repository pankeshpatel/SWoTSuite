package swotsuite.application.main;

import java.io.FileWriter;
import java.util.ArrayList;

import semantic.data.annotator.SemanticAnnotator;
import swotsuite.application.framework.ExecuteQueryEngine;
import swotsuite.application.framework.GenericApplication;
import swotsuite.application.framework.ReadFile;
import swotsuite.application.framework.VariableSparql;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author : Pankesh Patel, Amelie Gyrard
 * 
 * SWoTSuite Framework is originated from M3 framework developed by Amelie Gyrard.
 * In collaboration with Insight/NUIG-DERI, Ireland. * 
 *   
 */

public class Main { 
	
	
	  // IoT Data - Data from IoT devices
	   public static final String SENSOR_DATA = "./Input/senMLHealthData.xml";
	
		// Namespace for Ontologies
		public static final String NAMESPACE_M3= "http://sensormeasurement.appspot.com/m3#";
	
	
		// Domain Folder - contains domain-specific ontologies,  data sets, rules. 
		public static final String NATUROPATHY_ONTOLOGY = "./SupportedFiles/Domain/naturopathy.owl";
		public static final String HEALTH_ONTOLOGY = "./SupportedFiles/Domain/health.owl";
		public static final String NATUROPATHY_DATASET = "./SupportedFiles/Domain/naturopathy-dataset.rdf";
		public static final String HEALTH_DATASET = "./SupportedFiles/Domain/health-dataset.rdf"; 
		public static final String LINKED_OPEN_RULES_HEALTH = "./SupportedFiles/Domain/health-rules.txt";
	
		
		// Common Folder - contain ontologies that is used across any application.
	    public static final String M3_ONTOLOGY = "./SupportedFiles/Common/m3.owl";	
	    public static String RULES_M3_SEMANTIC_ANNOTATION = "./SupportedFiles/Common/rulesM3SemanticAnnotation.txt";
	    
	   // Query Folder -- contains SPARQL queries that is used to extract suggestions.
	   public static final String SPARQL_QUERY_NATUROPATHY = "./SupportedFiles/Query/m3SparqlNaturopathyScenario.sparql";
	   public static final String SPARQL_QUERY_NATUROPATHY_MINIMAL = "./SupportedFiles/Query/m3SparqlNaturopathyScenarioMinimal.sparql";
			 
	   
	   // Annotated - contains annotated data	   
	   public static final String GENERATED_SEMANTIC_SENSOR_DATA = "./SupportedFiles/Annotated/generated_semantic_sensor_data.rdf";
	   
	   
	   public static void main(String[] args) {	
		

		try {

			// STEP: LOAD RAW DATA (SENML/XML) in this tutorial
			String sensorMeasurements = ReadFile.readContentFile(SENSOR_DATA);
					
			// STEP: SEMANTIC ANNOTATION
			
			//TODO: show that we are loading the rules for semantic annotation
			SemanticAnnotator semanticAnnotator = new SemanticAnnotator();
			semanticAnnotator.convertXMLSenMLIntoRDF(sensorMeasurements);
					
			// WRITE SEMANTIC SENSOR DATA IN A FILE
			String fileName = GENERATED_SEMANTIC_SENSOR_DATA;
			FileWriter out = new FileWriter(fileName);
			semanticAnnotator.model.write(out,"RDF/XML-ABBREV");
									
			// STEP: LOAD SEMANTIC SENSOR DATA
			Model model = ModelFactory.createDefaultModel();
			
			ReadFile.enrichJenaModelOntologyDataset(model, GENERATED_SEMANTIC_SENSOR_DATA);
									
			// works with semantic sensor data 
			// (file already generated, useful to use in case of some issues with the semantic annotation)
			// ReadFile.enrichJenaModelOntologyDataset(model, HEALTH_SEMANTIC_SENSOR_DATA);
			
			// GENERIC APPLICATION
			GenericApplication generic_appli = new GenericApplication(model);

			// STEP: SPECIFIC DOMAIN ONTOLOGIES AND DATASETS
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, M3_ONTOLOGY);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, NATUROPATHY_ONTOLOGY);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, NATUROPATHY_DATASET);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, HEALTH_ONTOLOGY);
			ReadFile.enrichJenaModelOntologyDataset(generic_appli.model, HEALTH_DATASET);

		
			// STEP: EXECUTING REASONING ENGINE
			Model deduceMeaningfulInformationFromSensorData = 
					generic_appli.executeReasoningEngine(LINKED_OPEN_RULES_HEALTH);

			//deduceMeaningfulInformationFromSensorData.write(System.out);

			
			// STEP: TO MODIFY THE GENERIC SPARQL QUERY BY ASKING SPECIFIC INFORMATION
			ArrayList<VariableSparql> var = new ArrayList<VariableSparql>();
			var.add(new VariableSparql("inferTypeUri", NAMESPACE_M3 + "BodyTemperature", false));// we look for body temperature measurement

			// STEP: EXECUTING QUERY ENGINE
			ExecuteQueryEngine resultQueryEngine = new ExecuteQueryEngine(
					deduceMeaningfulInformationFromSensorData, SPARQL_QUERY_NATUROPATHY);
			String result = resultQueryEngine.getSelectResultAsXML(var);

			// DISPLAY SMARTER DATA 
			// USER TO DO: DISPLAY THE RESULT IN A USER FRIENDLY INTERFACE
			System.out.println(result);	

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

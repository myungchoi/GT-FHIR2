package edu.gatech.chai.omoponfhir.local.dao;

import java.sql.Connection;
import java.util.List;

import edu.gatech.chai.omoponfhir.local.model.FhirOmopVocabularyMapEntry;

public interface FhirOmopVocabularyMap {
	public Connection connect();
	
	public int save(FhirOmopVocabularyMapEntry conceptMapEntry);
	public void update(FhirOmopVocabularyMapEntry conceptMapEntry);
	public void delete(String omopConceptCodeName);
	public List<FhirOmopVocabularyMapEntry> get();
	public String getOmopVocabularyFromFhirSystemName(String fhirSystemName);
	public String getFhirSystemNameFromOmopVocabulary(String omopConceptCodeName);

}

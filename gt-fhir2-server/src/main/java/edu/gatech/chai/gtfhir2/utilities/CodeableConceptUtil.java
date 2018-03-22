package edu.gatech.chai.gtfhir2.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.exceptions.FHIRException;

import edu.gatech.chai.gtfhir2.mapping.OmopCodeableConceptMapping;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.service.ConceptService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class CodeableConceptUtil {
	public static Concept getOmopConceptWith(ConceptService conceptService, String omopVocabularyId, String code) {
		if (omopVocabularyId == null) return null;
		
		ParameterWrapper param = new ParameterWrapper(
				"String",
				Arrays.asList("vocabulary.id", "conceptCode"),
				Arrays.asList("like", "like"),
				Arrays.asList(omopVocabularyId, code),
				"and"
				);
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();

		List<ParameterWrapper> params = new ArrayList<ParameterWrapper>();
		params.add(param);
		map.put(Observation.SP_CODE, params);

		List<Concept> conceptIds = conceptService.searchWithParams(0, 0, map);
		if (conceptIds.isEmpty()) {
			return null;
		}
		
		// We should have only one entry... so... 
		return conceptIds.get(0);
	}
	
	public static Concept getOmopConceptWithFhirConcept(ConceptService conceptService, Coding fhirCoding) throws FHIRException {
		String system = fhirCoding.getSystem();
		String code = fhirCoding.getCode();
		
		String omopVocabularyId = OmopCodeableConceptMapping.omopVocabularyforFhirUri(system);
		return getOmopConceptWith(conceptService, omopVocabularyId, code);
	}
	
	public static Concept searchConcept(ConceptService conceptService, CodeableConcept codeableConcept) throws FHIRException {
		List<Coding> codings = codeableConcept.getCoding();
		for (Coding coding : codings) {
			// get OMOP Vocabulary from mapping.
			Concept ret = getOmopConceptWithFhirConcept(conceptService, coding);
			if (ret != null) return ret;
		}
		return null;
	}

	/**
	 * Creates a {@link CodeableConcept} from a {@link Concept}
	 * @param concept the {@link Concept} to use to generate the {@link CodeableConcept}
	 * @return a {@link CodeableConcept} generated from the passed in {@link Concept}
	 * @throws FHIRException if the {@link Concept} vocabulary cannot be mapped by the {@link OmopCodeableConceptMapping} fhirUriforOmopVocabularyi method.
     */
	public static CodeableConcept createFromConcept(Concept concept) throws FHIRException{
		String conceptVocab = concept.getVocabulary().getId();
		String conceptFhirUri = OmopCodeableConceptMapping.fhirUriforOmopVocabularyi(conceptVocab);
		String conceptCode = concept.getConceptCode();
		String conceptName = concept.getName();

		Coding conceptCoding = new Coding();
		conceptCoding.setSystem(conceptFhirUri);
		conceptCoding.setCode(conceptCode);
		conceptCoding.setDisplay(conceptName);

		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(conceptCoding);
		return codeableConcept;
	}

}

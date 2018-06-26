package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Medication.MedicationIngredientComponent;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.gtfhir2.provider.MedicationResourceProvider;
import edu.gatech.chai.gtfhir2.utilities.CodeableConceptUtil;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.service.ConceptService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class OmopMedication extends BaseOmopResource<Medication, Concept, ConceptService>
		implements IResourceMapping<Medication, Concept> {

	private static OmopMedication omopMedication = new OmopMedication();
	
	public OmopMedication(WebApplicationContext context) {
		super(context, Concept.class, ConceptService.class, MedicationResourceProvider.getType());
		
		initialize(context);
	}

	public OmopMedication() {
		super(ContextLoaderListener.getCurrentWebApplicationContext(), Concept.class, ConceptService.class, MedicationResourceProvider.getType());
		
		initialize(ContextLoaderListener.getCurrentWebApplicationContext());
	}
	
	private void initialize(WebApplicationContext context) {
	}

	
	public static OmopMedication getInstance() {
		return omopMedication;
	}
	
	@Override
	public Long toDbase(Medication fhirResource, IdType fhirId) throws FHIRException {
		throw new FHIRException("Medication Resource is Read-Only");
	}

	@Override
	public Medication constructFHIR(Long fhirId, Concept entity) {
		Medication medication = new Medication();
		
		medication.setId(new IdType(fhirId));
		CodeableConcept medicationCodeableConcept;
		try {
			medicationCodeableConcept = CodeableConceptUtil.getCodeableConceptFromOmopConcept(entity);
		} catch (FHIRException e1) {
			e1.printStackTrace();
			return null;
		}		
		
		medication.setCode(medicationCodeableConcept);

		// See if we can add ingredient version of this medication.
		List<Concept> ingredients = getMyOmopService().getIngredient(entity);
		if (ingredients.size() > 0) {
			CodeableConcept ingredientCodeableConcept;
			try {
				for (Concept ingredient: ingredients) {
					ingredientCodeableConcept = CodeableConceptUtil.getCodeableConceptFromOmopConcept(ingredient);
					if (!ingredientCodeableConcept.isEmpty()) {
						MedicationIngredientComponent medIngredientComponent = new MedicationIngredientComponent();
						medIngredientComponent.setItem(ingredientCodeableConcept);
						medication.addIngredient(medIngredientComponent);
					}
				}
			} catch (FHIRException e) {
				e.printStackTrace();
				return null;
			}
		} 
		
		return medication;
	}
	
	@Override
	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
		ParameterWrapper paramWrapper = new ParameterWrapper();
		switch (parameter) {
		case Medication.SP_RES_ID:
			String medicationId = ((TokenParam) value).getValue();
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(medicationId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case Medication.SP_CODE:
			String system = ((TokenParam) value).getSystem();
			String code = ((TokenParam) value).getValue();
			
			if ((system == null || system.isEmpty()) && (code == null || code.isEmpty()))
				break;
			
			String omopVocabulary = "None";
			if (system != null && !system.isEmpty()) {
				try {
					omopVocabulary = OmopCodeableConceptMapping.omopVocabularyforFhirUri(system);
				} catch (FHIRException e) {
					e.printStackTrace();
				}
			} 

			paramWrapper.setParameterType("String");
			if ("None".equals(omopVocabulary) && code != null && !code.isEmpty()) {
				paramWrapper.setParameters(Arrays.asList("conceptCode"));
				paramWrapper.setOperators(Arrays.asList("like"));
				paramWrapper.setValues(Arrays.asList(code));
			} else if (!"None".equals(omopVocabulary) && (code == null || code.isEmpty())) {
				paramWrapper.setParameters(Arrays.asList("vocabulary.id"));
				paramWrapper.setOperators(Arrays.asList("like"));
				paramWrapper.setValues(Arrays.asList(omopVocabulary));				
			} else {
				paramWrapper.setParameters(Arrays.asList("vocabulary.id", "conceptCode"));
				paramWrapper.setOperators(Arrays.asList("like","like"));
				paramWrapper.setValues(Arrays.asList(omopVocabulary, code));
			}
			paramWrapper.setRelationship("and");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}
		
		return mapList;
	}
	
	final ParameterWrapper filterParam = new ParameterWrapper(
			"String",
			Arrays.asList("domain"),
			Arrays.asList("="),
			Arrays.asList("Drug"),
			"or"
			);

	@Override
	public Long getSize() {
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();
		return getSize(map);
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(filterParam);
		map.put(MAP_EXCEPTION_FILTER, exceptions);

		return getMyOmopService().getSize(map);
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {

		// This is read all. But, since we will add an exception conditions to add filter.
		// we will call the search with params method.
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();
		searchWithParams (fromIndex, toIndex, map, listResources, includes);
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(filterParam);
		map.put(MAP_EXCEPTION_FILTER, exceptions);

		List<Concept> entities = getMyOmopService().searchWithParams(fromIndex, toIndex, map);

		for (Concept entity : entities) {
			Long omopId = entity.getIdAsLong();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, getMyFhirResourceType());
			Medication fhirResource = constructResource(fhirId, entity, includes);
			if (fhirResource != null) {
				listResources.add(fhirResource);			
				// Do the rev_include and add the resource to the list.
				addRevIncludes(omopId, includes, listResources);
			}

		}
	}
}

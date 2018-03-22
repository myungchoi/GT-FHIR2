package edu.gatech.chai.gtfhir2.mapping;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.gtfhir2.provider.EncounterResourceProvider;
import edu.gatech.chai.gtfhir2.provider.PatientResourceProvider;
import edu.gatech.chai.gtfhir2.provider.PractitionerResourceProvider;
import edu.gatech.chai.gtfhir2.utilities.CodeableConceptUtil;
import edu.gatech.chai.omopv5.jpa.entity.*;
import edu.gatech.chai.omopv5.jpa.service.*;

import edu.gatech.chai.gtfhir2.provider.ConditionResourceProvider;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class OmopCondition extends BaseOmopResource<Condition, ConditionOccurrence, ConditionOccurrenceService> implements IResourceMapping<Condition, ConditionOccurrence> {

    private static final Logger logger = LoggerFactory.getLogger(OmopCondition.class);

    private static OmopCondition omopCondition = new OmopCondition();

    private ConditionOccurrenceService conditionOccurrenceService;
    private FPersonService fPersonService;
    private ProviderService providerService;
    private ConceptService conceptService;
    private VisitOccurrenceService visitOccurrenceService;

    public OmopCondition(WebApplicationContext context) {
        super(context, ConditionOccurrence.class, ConditionOccurrenceService.class, ConditionResourceProvider.getType());
        initialize(context);
    }

    public OmopCondition() {
        super(ContextLoaderListener.getCurrentWebApplicationContext(), ConditionOccurrence.class, ConditionOccurrenceService.class, ConditionResourceProvider.getType());
        initialize(ContextLoaderListener.getCurrentWebApplicationContext());
    }

    private void initialize(WebApplicationContext context) {
        // Get bean for other services that we need for mapping.
        conditionOccurrenceService = context.getBean(ConditionOccurrenceService.class);
        fPersonService = context.getBean(FPersonService.class);
        providerService = context.getBean(ProviderService.class);
        conceptService = context.getBean(ConceptService.class);
        visitOccurrenceService = context.getBean(VisitOccurrenceService.class);
    }

    public static OmopCondition getInstance() {
        return omopCondition;
    }

    @Override
    public Condition constructFHIR(Long fhirId, ConditionOccurrence conditionOccurrence){
        Condition condition = new Condition();
        condition.setId(new IdType(fhirId));

        addPersonToCondition(conditionOccurrence, condition);
        addCodeToCondition(conditionOccurrence, condition);
        addStartAndEndDateToCondition(conditionOccurrence, condition);
        addTypeToCondition(conditionOccurrence, condition);
        addAsserterToCondition(conditionOccurrence, condition);
        addContextToCondition(conditionOccurrence, condition);

        //TODO: Need to map the following
        //??Condition.abatement.abatementString, but we are using abatementDateTime for the end date and Abatement[x] has a 0..1 cardinality.
        String stopReason = conditionOccurrence.getStopReason();
        //??
        String sourceValue = conditionOccurrence.getConditionSourceValue();
        //??
        Concept sourceConceptId = conditionOccurrence.getSourceConceptId();

        return condition;
    }

    @Override
    public Long toDbase(Condition fhirResource, IdType fhirId) throws FHIRException {
        //things to update Condition_Occurrence, Concept, FPerson, Provider, VisitOccurrence
        Long retval;
        ConditionOccurrence conditionOccurrence;
        FPerson fPerson;
        Provider provider;
        VisitOccurrence visitOccurrence;

        //check for an existing condition
        Long fhirIdLong = null;
        if (fhirId != null) {
            fhirIdLong = fhirId.getIdPartAsLong();
            //find existing condition
            Long omopId = IdMapping.getOMOPfromFHIR(fhirIdLong, ConditionResourceProvider.getType());
            conditionOccurrence = conditionOccurrenceService.findById(omopId);
        }
        else{
            //create new condition
            //create Condition
            throw new FHIRException("FHIR Resource does not contain a Condition.");
        }

        //get the Subject
        if( fhirResource.getSubject() != null ){
            Long subjectId = fhirResource.getSubject().getReferenceElement().getIdPartAsLong();
            Long subjectFhirId = IdMapping.getOMOPfromFHIR(subjectId, PatientResourceProvider.getType());
            fPerson = fPersonService.findById(subjectFhirId);
        }
        else{
            //throw an error
            throw new FHIRException("FHIR Resource does not contain a Subject.");
        }
        conditionOccurrence.setfPerson(fPerson);

        //get the Provider
        if( fhirResource.getAsserter() != null ){
            Long providerId = fhirResource.getAsserter().getReferenceElement().getIdPartAsLong();
            Long providerOmopId = IdMapping.getOMOPfromFHIR(providerId, PractitionerResourceProvider.getType());
            provider = providerService.findById(providerOmopId);
        }
        else {
            //else create provider
            throw new FHIRException("FHIR Resource does not contain a Provider.");
        }
        conditionOccurrence.setProvider(provider);

        //get the concept code
        if( fhirResource.getCode() != null ){
            List<Coding> codes = fhirResource.getCode().getCoding();
            Concept omopConcept;
            //there is only one so get the first
            omopConcept = CodeableConceptUtil.getOmopConceptWithFhirConcept(conceptService, codes.get(0));
            //set the concept
            conditionOccurrence.setConceptId(omopConcept);
        }
        else{
            //is there a generic condition concept to use?
            throw new FHIRException("FHIR Resource does not contain a Condition Code.");
        }

        //get the start and end date. We are expecting both to be of type DateTimeType
        if( fhirResource.getOnset() != null &&
            fhirResource.getOnset() instanceof DateTimeType ){
            conditionOccurrence.setStartDate(((DateTimeType)fhirResource.getOnset()).toCalendar().getTime());
        }
        else{
            //create a start date
            throw new FHIRException("FHIR Resource does not contain a start date.");
        }

        if( fhirResource.getAbatement() != null &&
            fhirResource.getAbatement() instanceof DateTimeType ){
            conditionOccurrence.setEndDate(((DateTimeType)fhirResource.getAbatement()).toCalendar().getTime());
        }
        else{
            //leave alone, end date not required
        }

        //set the conditions
        if( fhirResource.getCategory() != null ) {
            List<CodeableConcept> categories = fhirResource.getCategory();
            Concept omopTypeConcept;
            //there is only one so get the first
            omopTypeConcept = CodeableConceptUtil.getOmopConceptWithFhirConcept(conceptService, categories.get(0).getCodingFirstRep());
            conditionOccurrence.setTypeConceptId(omopTypeConcept);
        }
        else{
            //is there a generic condition type concept to use?
            throw new FHIRException("FHIR Resource does not contain a Condition category.");
        }

        //set the context
        if( fhirResource.getContext() != null ){
            Long visitId = fhirResource.getContext().getReferenceElement().getIdPartAsLong();
            Long visitFhirId = IdMapping.getOMOPfromFHIR(visitId, EncounterResourceProvider.getType());
            visitOccurrence = visitOccurrenceService.findById(visitFhirId);
            conditionOccurrence.setVisitOccurrence(visitOccurrence);
        }
        else{
            //do nothing
        }

        //TODO: Do you need to call other services to update links resources.

        if (conditionOccurrence.getId() != null) {
            retval = conditionOccurrenceService.update(conditionOccurrence).getId();
        } else {
            retval = conditionOccurrenceService.create(conditionOccurrence).getId();
        }

        return retval;
    }

    public List<ParameterWrapper> mapParameter(String parameter, Object value) {
        List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
        ParameterWrapper paramWrapper = new ParameterWrapper();
        switch (parameter) {
            case Condition.SP_ABATEMENT_AGE:
                //not supporting
                break;
            case Condition.SP_ABATEMENT_BOOLEAN:
                //not supporting
                break;
            case Condition.SP_ABATEMENT_DATE:
                //Condition.abatementDate -> Omop ConditionOccurrence.conditionEndDate
                putDateInParamWrapper(paramWrapper, value, "conditionEndDate");
                mapList.add(paramWrapper);
                break;
            case Condition.SP_ABATEMENT_STRING:
                //not supporting
                break;
            case Condition.SP_ASSERTED_DATE:
                //Condition.assertedDate -> Omop ConditionOccurrence.conditionStartDate
                putDateInParamWrapper(paramWrapper, value, "conditionStartDate");
                mapList.add(paramWrapper);
                break;
            case Condition.SP_ASSERTER:
                //Condition.asserter -> Omop Provider
                ReferenceParam patientReference = ((ReferenceParam) value);
                String patientId = String.valueOf(patientReference.getIdPartAsLong());

                paramWrapper.setParameterType("Long");
                paramWrapper.setParameters(Arrays.asList("provider.id"));
                paramWrapper.setOperators(Arrays.asList("="));
                paramWrapper.setValues(Arrays.asList(patientId));
                paramWrapper.setRelationship("or");
                mapList.add(paramWrapper);
                break;
            case Condition.SP_BODY_SITE:
                //not supporting
                break;
            case Condition.SP_CATEGORY:
                //Condition.category
                putConditionInParamWrapper(paramWrapper, value);
                mapList.add(paramWrapper);
                break;
            case Condition.SP_CLINICAL_STATUS:
                break;
            case Condition.SP_CODE:
                //Condition.code -> Omop Concept
                putConditionInParamWrapper(paramWrapper, value);
                mapList.add(paramWrapper);
                break;
            case Condition.SP_CONTEXT:
                //Condition.context -> Omop VisitOccurrence
                ReferenceParam visitReference = (ReferenceParam)value;
                String visitId = String.valueOf(visitReference.getIdPartAsLong());
                paramWrapper.setParameterType("Long");
                paramWrapper.setParameters(Arrays.asList("visitOccurrence.id"));
                paramWrapper.setOperators(Arrays.asList("="));
                paramWrapper.setValues(Arrays.asList(visitId));
                paramWrapper.setRelationship("or");
                mapList.add(paramWrapper);
                break;
            case Condition.SP_ENCOUNTER:
                //not supporting
                break;
            case Condition.SP_EVIDENCE:
                //not supporting
                break;
            case Condition.SP_EVIDENCE_DETAIL:
                //not supporting
                break;
            case Condition.SP_IDENTIFIER:
                //not supporting
                break;
            case Condition.SP_ONSET_AGE:
                //not supporting
                break;
            case Condition.SP_ONSET_DATE:
                //not supporting
                break;
            case Condition.SP_ONSET_INFO:
                //not supporting
                break;
            case Condition.SP_PATIENT:
                //not supporting
                break;
            case Condition.SP_SEVERITY:
                //not supporting
                break;
            case Condition.SP_STAGE:
                //not supporting
                break;
            case Condition.SP_SUBJECT:
                //Condition.subject -> Omop FPerson
                ReferenceParam subjectReference = ((ReferenceParam) value);
                String subjectId = String.valueOf(subjectReference.getIdPartAsLong());
                paramWrapper.setParameterType("Long");
                paramWrapper.setParameters(Arrays.asList("fPerson.id"));
                paramWrapper.setOperators(Arrays.asList("="));
                paramWrapper.setValues(Arrays.asList(subjectId));
                paramWrapper.setRelationship("or");
                mapList.add(paramWrapper);
                break;
            case Condition.SP_VERIFICATION_STATUS:
                //not supporting
                break;
            default:
                mapList = null;
        }

        return mapList;
    }

    /*======================================================================*/
    /* PRIVATE METHODS */
    /*======================================================================*/

    private void putConditionInParamWrapper(ParameterWrapper paramWrapper, Object value){
        String system = ((TokenParam) value).getSystem();
        String code = ((TokenParam) value).getValue();

        paramWrapper.setParameterType("String");
        paramWrapper.setParameters(Arrays.asList("concept.vocabularyId", "concept.conceptCode"));
        paramWrapper.setParameters(Arrays.asList("like", "like"));
        paramWrapper.setValues(Arrays.asList(system, code));
        paramWrapper.setRelationship("and");
    }

    private void putDateInParamWrapper(ParameterWrapper paramWrapper, Object value, String omopTableColumn){
        DateParam dateParam = (DateParam)value;
        ParamPrefixEnum apiOperator = dateParam.getPrefix();
        String sqlOperator = null;
        if (apiOperator.equals(ParamPrefixEnum.GREATERTHAN)) {
            sqlOperator = ">";
        } else if (apiOperator.equals(ParamPrefixEnum.GREATERTHAN_OR_EQUALS)) {
            sqlOperator = ">=";
        } else if (apiOperator.equals(ParamPrefixEnum.LESSTHAN)) {
            sqlOperator = "<";
        } else if (apiOperator.equals(ParamPrefixEnum.LESSTHAN_OR_EQUALS)) {
            sqlOperator = "<=";
        } else if (apiOperator.equals(ParamPrefixEnum.NOT_EQUAL)) {
            sqlOperator = "!=";
        } else {
            sqlOperator = "=";
        }
        Date effectiveDate = dateParam.getValue();

        paramWrapper.setParameterType("Date");
        paramWrapper.setParameters(Arrays.asList(omopTableColumn));
        paramWrapper.setOperators(Arrays.asList(sqlOperator));
        paramWrapper.setValues(Arrays.asList(String.valueOf(effectiveDate.getTime())));
        paramWrapper.setRelationship("or");
    }

    private CodeableConcept retrieveCodeableConcept(Concept concept){
        CodeableConcept conditionCodeableConcept = null;
        try {
            conditionCodeableConcept = CodeableConceptUtil.createFromConcept(concept);
        }
        catch(FHIRException fe){
            logger.error("Could not generate CodeableConcept from Concept.", fe);
        }
        return conditionCodeableConcept;
    }

    private void addPersonToCondition(ConditionOccurrence conditionOccurrence, Condition condition){
        //Condition.subject
        FPerson fPerson = conditionOccurrence.getfPerson();
        //set the person
        Reference subjectRef = new Reference(new IdType(PatientResourceProvider.getType(), fPerson.getId()));
        subjectRef.setDisplay(fPerson.getNameAsSingleString());
        condition.setSubject(subjectRef);
    }

    private void addCodeToCondition(ConditionOccurrence conditionOccurrence, Condition condition){
        //Condition.code SNOMED-CT
        Concept conceptId = conditionOccurrence.getConceptId();
        CodeableConcept conditionCodeableConcept = retrieveCodeableConcept(conceptId);
        if(conditionCodeableConcept != null ) {
            condition.setCode(conditionCodeableConcept);
        }
    }

    private void addStartAndEndDateToCondition(ConditionOccurrence conditionOccurrence, Condition condition){
        //Condition.onsetDateTime
        Date startDate = conditionOccurrence.getStartDate();
        if( startDate != null ){
            DateTimeType onsetDateTime = new DateTimeType(startDate);
            condition.setOnset(onsetDateTime);
        }
        //Condition.abatementDateTime
        Date endDate = conditionOccurrence.getEndDate();
        if( endDate != null ){
            DateTimeType abatementDateTime = new DateTimeType(endDate);
            condition.setAbatement(abatementDateTime);
        }
    }

    private void addTypeToCondition(ConditionOccurrence conditionOccurrence, Condition condition){
        //Condition.category
        Concept typeConceptId = conditionOccurrence.getTypeConceptId();
        CodeableConcept typeCodeableConcept = retrieveCodeableConcept(typeConceptId);
        if(typeCodeableConcept != null){
            List<CodeableConcept> typeList = new ArrayList<CodeableConcept>();
            typeList.add(typeCodeableConcept);
            condition.setCategory(typeList);
        }
    }

    private void addAsserterToCondition(ConditionOccurrence conditionOccurrence, Condition condition){
        //Condition.asserter
        Provider provider = conditionOccurrence.getProvider();
        Reference providerRef = new Reference(new IdType(PractitionerResourceProvider.getType(), provider.getId()));
        providerRef.setDisplay(provider.getProviderName());
        condition.setAsserter(providerRef);
    }

    private void addContextToCondition(ConditionOccurrence conditionOccurrence, Condition condition){
        //Condition.context
        VisitOccurrence visitOccurrence = conditionOccurrence.getVisitOccurrence();
        Reference visitRef = new Reference(new IdType(visitOccurrence.getId()));
        condition.setContext(visitRef);
    }
}
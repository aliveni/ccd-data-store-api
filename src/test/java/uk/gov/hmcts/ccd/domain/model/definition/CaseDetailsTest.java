package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CASE_REFERENCE_PARAM;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CASE_TYPE_PARAM;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CREATED_DATE_PARAM;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.JURISDICTION_PARAM;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.LAST_MODIFIED_PARAM;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.SECURITY_CLASSIFICATION_PARAM;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.STATE_PARAM;

class CaseDetailsTest {

    private static final JsonNodeFactory JSON_NODE_FACTORY = new JsonNodeFactory(false);
    private static final String CASE_DETAIL_FIELD = "dataTestField1";

    private CaseDetails caseDetails;

    @BeforeEach
    public void setup() {
        caseDetails = new CaseDetails();
        Map<String, JsonNode> dataMap = buildData(CASE_DETAIL_FIELD);
        caseDetails.setData(dataMap);
    }

    @Test
    void testExistsInDataIsAlwaysTrueForLabels() {
        CaseTypeTabField tabField = createCaseTypeTabField("someId", "Label");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void testExistsInDataIsAlwaysTrueForCasePaymentHistoryViewer() {
        CaseTypeTabField tabField = createCaseTypeTabField("someId", "CasePaymentHistoryViewer");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void testExistsInDataIsFalseIfTabFieldDoesNotBelongToCase() {
        CaseTypeTabField tabField = createCaseTypeTabField("someId2", "YesOrNo");

        assertThat(caseDetails.existsInData(tabField), equalTo(false));
    }

    @Test
    void testExistsInDataIsTrueIfTabFieldBelongsToCase() {
        CaseTypeTabField tabField = createCaseTypeTabField(CASE_DETAIL_FIELD, "YesOrNo");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void shouldReturnMetadataFieldMap() {
        LocalDateTime now = LocalDateTime.now();
        caseDetails.setJurisdiction("jurisdiction");
        caseDetails.setCaseTypeId("caseType");
        caseDetails.setState("state");
        caseDetails.setReference(1234567L);
        caseDetails.setCreatedDate(now);
        caseDetails.setLastModified(now);
        caseDetails.setSecurityClassification(SecurityClassification.PUBLIC);

        Map<String, Object> metadata = caseDetails.getMetadata();
        
        assertThat(metadata.get(JURISDICTION_PARAM), equalTo(caseDetails.getJurisdiction()));
        assertThat(metadata.get(CASE_TYPE_PARAM), equalTo(caseDetails.getCaseTypeId()));
        assertThat(metadata.get(STATE_PARAM), equalTo(caseDetails.getState()));
        assertThat(metadata.get(CASE_REFERENCE_PARAM), equalTo(caseDetails.getReference()));
        assertThat(metadata.get(CREATED_DATE_PARAM), equalTo(caseDetails.getCreatedDate()));
        assertThat(metadata.get(LAST_MODIFIED_PARAM), equalTo(caseDetails.getLastModified()));
        assertThat(metadata.get(SECURITY_CLASSIFICATION_PARAM), equalTo(caseDetails.getSecurityClassification()));
    }

    private Map<String, JsonNode> buildData(String... dataFieldIds) {
        Map<String, JsonNode> dataMap = Maps.newHashMap();
        Lists.newArrayList(dataFieldIds).forEach(dataFieldId -> {
            dataMap.put(dataFieldId, JSON_NODE_FACTORY.textNode(dataFieldId));
        });
        return dataMap;
    }

    private CaseTypeTabField createCaseTypeTabField(String id, String type) {
        CaseTypeTabField tabField = new CaseTypeTabField();
        CaseField caseField = new CaseField();
        caseField.setId(id);
        FieldType labelFieldType = new FieldType();
        labelFieldType.setType(type);
        caseField.setFieldType(labelFieldType);
        tabField.setCaseField(caseField);
        return tabField;
    }


}

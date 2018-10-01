package uk.gov.hmcts.ccd.domain.service.search.elasticsearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_READ;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.user.UserRepository;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseType;
import uk.gov.hmcts.ccd.domain.model.search.CaseSearchResult;
import uk.gov.hmcts.ccd.domain.service.common.AccessControlService;
import uk.gov.hmcts.ccd.domain.service.common.ObjectMapperService;
import uk.gov.hmcts.ccd.domain.service.common.SecurityClassificationService;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.security.AuthorisedCaseSearchOperation;
import uk.gov.hmcts.ccd.domain.service.security.AuthorisedCaseDefinitionDataService;

class AuthorisedCaseSearchOperationTest {

    private static final String CASE_TYPE_ID = "caseType";
    private static final String QUERY = "{}";

    @Mock
    private CaseSearchOperation caseSearchOperation;
    @Mock
    private AuthorisedCaseDefinitionDataService authorisedCaseDefinitionDataService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SecurityClassificationService classificationService;
    @Mock
    private ObjectMapperService objectMapperService;

    @InjectMocks
    private AuthorisedCaseSearchOperation authorisedCaseDetailsSearchOperation;

    private final CaseType caseType = new CaseType();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        caseType.setId(CASE_TYPE_ID);
    }

    @Test
    @DisplayName("should filter fields and return search results for valid query")
    void shouldFilterFieldsReturnSearchResults() {
        CaseSearchRequest request = new CaseSearchRequest(objectMapperService, CASE_TYPE_ID, QUERY);
        CaseDetails caseDetails = new CaseDetails();
        CaseSearchResult searchResult = new CaseSearchResult(singletonList(caseDetails), 1L);
        when(authorisedCaseDefinitionDataService.getAuthorisedCaseType(CASE_TYPE_ID, CAN_READ)).thenReturn(Optional.of(caseType));
        when(caseSearchOperation.execute(request)).thenReturn(searchResult);

        Map<String, JsonNode> unFilteredData = new HashMap<>();
        caseDetails.setData(unFilteredData);
        JsonNode jsonNode = mock(JsonNode.class);
        Set<String> userRoles = new HashSet<>();
        when(userRepository.getUserRoles()).thenReturn(userRoles);
        when(objectMapperService.convertObjectToJsonNode(unFilteredData)).thenReturn(jsonNode);
        CaseType caseType = new CaseType();
        when(accessControlService.filterCaseFieldsByAccess(jsonNode, caseType.getCaseFields(), userRoles, CAN_READ)).thenReturn(jsonNode);
        Map<String, JsonNode> filteredData = new HashMap<>();
        when(objectMapperService.convertJsonNodeToMap(jsonNode)).thenReturn(filteredData);

        CaseSearchResult result = authorisedCaseDetailsSearchOperation.execute(request);

        assertAll(
            () -> assertThat(result, is(searchResult)),
            () -> assertThat(caseDetails.getData(), Matchers.is(filteredData)),
            () -> assertThat(result.getTotal(), is(1L)),
            () -> verify(authorisedCaseDefinitionDataService).getAuthorisedCaseType(CASE_TYPE_ID, CAN_READ),
            () -> verify(caseSearchOperation).execute(request),
            () -> verify(userRepository).getUserRoles(),
            () -> verify(objectMapperService).convertObjectToJsonNode(unFilteredData),
            () -> verify(accessControlService).filterCaseFieldsByAccess(jsonNode, caseType.getCaseFields(), userRoles, CAN_READ),
            () -> verify(objectMapperService).convertJsonNodeToMap(jsonNode),
            () -> verify(classificationService).applyClassification(caseDetails)
        );
    }

    @Test
    @DisplayName("should return empty list of cases when user is not authorised to access case type")
    void shouldReturnEmptyCaseList() {
        CaseSearchRequest request = new CaseSearchRequest(objectMapperService, CASE_TYPE_ID, QUERY);
        when(authorisedCaseDefinitionDataService.getAuthorisedCaseType(CASE_TYPE_ID, CAN_READ)).thenReturn(Optional.empty());

        CaseSearchResult result = authorisedCaseDetailsSearchOperation.execute(request);

        assertAll(
            () -> assertThat(result.getCases(), hasSize(0)),
            () -> assertThat(result.getTotal(), is(0L)),
            () -> verify(authorisedCaseDefinitionDataService).getAuthorisedCaseType(CASE_TYPE_ID, CAN_READ),
            () -> verifyZeroInteractions(caseSearchOperation),
            () -> verifyZeroInteractions(objectMapperService),
            () -> verifyZeroInteractions(accessControlService),
            () -> verifyZeroInteractions(userRepository),
            () -> verifyZeroInteractions(classificationService)
        );
    }
}

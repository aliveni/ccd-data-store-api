package uk.gov.hmcts.ccd.domain.model.std;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class CaseDataContentBuilder {
    private final CaseDataContent caseDataContent;

    private CaseDataContentBuilder() {
        this.caseDataContent = new CaseDataContent();
    }

    public CaseDataContentBuilder withEvent(Event event) {
        this.caseDataContent.setEvent(event);
        return this;
    }

    public CaseDataContentBuilder withData(Map<String, JsonNode> data) {
        this.caseDataContent.setData(data);
        return this;
    }

    public CaseDataContentBuilder withSecurityClassification(String securityClassification) {
        this.caseDataContent.setSecurityClassification(securityClassification);
        return this;
    }

    public CaseDataContentBuilder withDataClassification(Map<String,JsonNode> dataClassification) {
        this.caseDataContent.setDataClassification(dataClassification);
        return this;
    }

    public CaseDataContentBuilder withToken(String token) {
        this.caseDataContent.setToken(token);
        return this;
    }

    public CaseDataContentBuilder withIgnoreWarning(Boolean ignoreWarning) {
        this.caseDataContent.setIgnoreWarning(ignoreWarning);
        return this;
    }

    public static CaseDataContentBuilder anCaseDataContent() {
        return new CaseDataContentBuilder();
    }

    public CaseDataContent build() {
        return this.caseDataContent;
    }
}
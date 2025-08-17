package com.cvshealth.digital.microservice.iqe.utils;


import com.cvshealth.digital.microservice.iqe.config.ConsentConfig;
import com.cvshealth.digital.microservice.iqe.enums.ConsentsEnum;
import com.cvshealth.digital.microservice.iqe.model.ConsentData;
import com.cvshealth.digital.microservice.iqe.model.GetConsentInput;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.IntStream;

@Component
@AllArgsConstructor
public class ConsentServiceHelper {

    public List<ConsentData> summarizeConsentsForGroup(List<ConsentData> consentDataList, GetConsentInput consentInput) {
        List<ConsentData> finalConsentDataList = new ArrayList<>();
        List<ConsentData> summarizedConsents = new ArrayList<>();
        List<ConsentData> notSummarizedConsents = new ArrayList<>();

        if (consentInput.getConsentContextInput() == null ||  consentInput.getConsentContextInput().isEmpty() || consentInput.getConsentContextInput().stream().noneMatch(GetConsentInput.ConsentContextInput::isSummarize)) {
            // If the input list is empty, return an empty list
            return consentDataList;
        }

        for (ConsentData consentData : consentDataList) {
            List<ConsentData.Consent> reviewList = consentData.getConsents().stream()
                    .filter(consent -> consentInput.getConsentContextInput().stream()
                            .anyMatch(consentContextInput -> consent.getConsentContext().equalsIgnoreCase(consentContextInput.getConsentContext()) && consentContextInput.isSummarize()))
                    .toList();
            List<ConsentData.Consent> nonReviewList = consentData.getConsents().stream()
                    .filter(consent -> consentInput.getConsentContextInput().stream()
                            .noneMatch(consentContextInput -> consent.getConsentContext().equalsIgnoreCase(consentContextInput.getConsentContext()) && consentContextInput.isSummarize()))
                    .toList();

            if (!reviewList.isEmpty()) {
                summarizedConsents.add(ConsentData.builder()
                        .isSummarized(consentData.isSummarized())
                        .patientReferenceId(consentData.getPatientReferenceId())
                        .consents(reviewList)
                        .build());
            }

            if (!nonReviewList.isEmpty()) {
                notSummarizedConsents.add(ConsentData.builder()
                        .isSummarized(consentData.isSummarized())
                        .patientReferenceId(consentData.getPatientReferenceId())
                        .consents(nonReviewList)
                        .build());
            }
        }
        finalConsentDataList.addAll(notSummarizedConsents);

        Map<String, Map<String, ConsentData.Consent>> groupedData = groupByPatientAndContext(summarizedConsents);
        groupedData.forEach((context, value) -> {
            Map<String, ConsentData.Consent> innerMap = groupedData.get(context);
            if (innerMap != null) {
                ConsentData.Consent consolidatedConsent = consolidateConsent(innerMap);
                ConsentData consentDataObj = new ConsentData();
                consentDataObj.setSummarized(true);
                consentDataObj.setConsents(List.of(consolidatedConsent));
                finalConsentDataList.add(consentDataObj);
            }
        });

        return finalConsentDataList;

    }


    private Map<String, Map<String, ConsentData.Consent>> groupByPatientAndContext(List<ConsentData> consentDataList) {
        Map<String, Map<String, ConsentData.Consent>> groupedConsents = new HashMap<>();
        Map<String, ConsentData.Consent> groupedByPatientReferenceId = new HashMap<>();
        consentDataList.forEach(consentData -> {
            String patientReferenceId = consentData.getPatientReferenceId();
            consentData.getConsents().forEach(consent -> {

                        if (groupedConsents.containsKey(consent.getConsentContext())) {
                            Map<String, ConsentData.Consent> groupedByPatientReferenceIdMap = groupedConsents.get(consent.getConsentContext());
                            groupedByPatientReferenceIdMap.put(patientReferenceId, consent);
                            groupedConsents.put(consent.getConsentContext(), groupedByPatientReferenceIdMap);
                        } else {
                            groupedByPatientReferenceId.put(patientReferenceId, consent);
                            groupedConsents.put(consent.getConsentContext(), groupedByPatientReferenceId);
                        }
                    }
            );
        });


        return groupedConsents;

    }


    private ConsentData.Consent consolidateConsent(Map<String, ConsentData.Consent> consentMap) {
        ConsentData.Consent consolidatedConsent = consentMap.entrySet().stream().findFirst().get().getValue();

        consentMap.forEach((key, consent) -> {

            // Recursively process related consents
            if (consent.getConsent() != null) {
                for (ConsentData.Consent consent1 : consent.getConsent().getConsents()) {
                    ConsentData.Consent foundConsent = findConsentRecursively(consolidatedConsent, consent1);
                    if(foundConsent != null) {
                        if (foundConsent.getPatientReferenceIds() == null) {
                            foundConsent.setPatientReferenceIds(new HashSet<>());
                        }
                        Set<String> patientReferenceIds = new HashSet<>(foundConsent.getPatientReferenceIds());
                        patientReferenceIds.add(key);
                        foundConsent.setPatientReferenceIds(patientReferenceIds);
                    }
                    else {
                        consent1.setPatientReferenceIds(Set.of(key));
                        // Add the not found consent to the consolidatedConsent
                        int foundIndex = IntStream.iterate(
                                        consolidatedConsent.getConsent().getConsents().size() - 1,
                                        i -> i >= 0,
                                        i -> i - 1)
                                .filter(index -> consolidatedConsent.getConsent().getConsents()
                                        .get(index).getConsentName().equalsIgnoreCase(consent1.getConsentName()))
                                .findFirst()
                                .orElse(-1);
                        // Add the same consent just after the found index
                        if(foundIndex >= 0) {
                            consolidatedConsent.getConsent().getConsents().add(foundIndex+1,consent1);
                        } else {
                            consolidatedConsent.getConsent().getConsents().add(consent1);
                        }
                    }
                    processNestedConsent(consent1, consolidatedConsent, key);

                }
            }

        });
        return consolidatedConsent;
    }

    private ConsentData.Consent findConsentRecursively(ConsentData.Consent consent, ConsentData.Consent targetConsent) {
        // Base case: Check if the current consent matches the targetConsentName
        if (StringUtils.isNotBlank(consent.getConsentName()) && consent.getConsentName().equalsIgnoreCase(targetConsent.getConsentName())
                &&  (consent.getValue() == null || consent.getValue().equals(targetConsent.getValue()))) {

            return consent;
        }

        // Recursive case: Search in related consents
        if (consent.getConsent() != null && !CollectionUtils.isEmpty(consent.getConsent().getConsents())) {
            for (ConsentData.Consent child : consent.getConsent().getConsents()) {
                ConsentData.Consent found = findConsentRecursively(child, targetConsent);

                if (found != null) {
                    return found;
                }
            }
        }
        // If not found, return null
        return null;
    }

    private void processNestedConsent(ConsentData.Consent nestedConsent, ConsentData.Consent consolidatedConsent, String patientReferenceId) {
        if (nestedConsent.getPatientReferenceIds() == null) {
            nestedConsent.setPatientReferenceIds(new HashSet<>());
        }

        // Add parent consent's patientReferenceIds to the nested consent
        if (consolidatedConsent.getPatientReferenceIds() != null) {
            nestedConsent.getPatientReferenceIds().addAll(consolidatedConsent.getPatientReferenceIds());
        }

        // Recursively process related consents
        if (nestedConsent.getConsent() != null && !CollectionUtils.isEmpty(nestedConsent.getConsent().getConsents())) {
            for (ConsentData.Consent consent : nestedConsent.getConsent().getConsents()) {

                if(consent != null && consent.getConsentName().equalsIgnoreCase(ConsentsEnum.patientRelationshipName.name())) {
                    ConsentData.Consent foundConsent1 = findConsentRecursively(consolidatedConsent, nestedConsent);
                    if(foundConsent1 != null) {

                        // add the
                        ConsentData.Consent childConsent = foundConsent1.getConsent().getConsents().getFirst();
                        if(childConsent != null) {
                            Set<String> patientReferenceIds = new HashSet<>(foundConsent1.getPatientReferenceIds());
                            patientReferenceIds.add(patientReferenceId);
                            childConsent.setPatientReferenceIds(patientReferenceIds);
                            boolean isHidden = childConsent.getIsHidden() != null && childConsent.getIsHidden() && consent.getIsHidden() != null && consent.getIsHidden();
                            childConsent.setIsHidden(isHidden);
                            break;
                        }
                    }
                }
                ConsentData.Consent foundConsent = findConsentRecursively(consolidatedConsent, consent);
                if (foundConsent != null) {
                    if (foundConsent.getPatientReferenceIds() == null) {
                        foundConsent.setPatientReferenceIds(new HashSet<>());
                    }
                    Set<String> patientReferenceIds = new HashSet<>(foundConsent.getPatientReferenceIds());
                    patientReferenceIds.add(patientReferenceId);
                    foundConsent.setPatientReferenceIds(patientReferenceIds);
                } else {
                    consent.setPatientReferenceIds(Set.of(patientReferenceId));
                    ConsentData.Consent foundConsent1 = findConsentRecursively(consolidatedConsent, nestedConsent);
                    if (foundConsent1 != null) {
                        consent.setPatientReferenceIds(Set.of(patientReferenceId));
                        if(foundConsent1.getConsent() == null) {
                            foundConsent1.setConsent(ConsentData.ConsentDetailsInfo.builder().type(consent.getConsentType()).consents(new ArrayList<>()).build());
                        }
                        // If the consent is for minor age restriction acknowledgement, add it to the front of the list
                        if(consent.getConsentName().equalsIgnoreCase(ConsentsEnum.isMinorAgeRestrictionAcknowledgement.name())) {

                            foundConsent1.getConsent().getConsents().addFirst(consent);
                        }
                        else {
                            foundConsent1.getConsent().getConsents().add(consent);
                        }
                        if (foundConsent1.getPatientReferenceIds() == null) {
                            foundConsent1.setPatientReferenceIds(new HashSet<>());
                        }
                        Set<String> patientReferenceIds = new HashSet<>(foundConsent1.getPatientReferenceIds());
                        patientReferenceIds.add(patientReferenceId);
                        foundConsent1.setPatientReferenceIds(patientReferenceIds);

                    } else {
                        // Add the not found consent to the consolidatedConsent
                        if (consolidatedConsent.getConsent() == null) {
                            consolidatedConsent.setConsent(new ConsentData.ConsentDetailsInfo());
                            consolidatedConsent.getConsent().setConsents(new ArrayList<>());
                        }
                        consolidatedConsent.getConsent().getConsents().add(nestedConsent);
                    }
                    processNestedConsent(consent, consolidatedConsent, patientReferenceId);
                }
            }
        }

    }


    /**
     * Filters consents based on the provided rule and variables.
     * Recursively processes combined consents and applies the rule evaluation.
     *
     * @param consent   The consent to filter.
     * @param variables The variables for rule evaluation.
     * @return Filtered consent or null if the consent is invalid.
     */

    public ConsentConfig.Consent filterConsents(ConsentConfig.Consent consent, Map<String, Object> variables) {
        // Evaluate the rule for the current consent
        boolean isValid = !consent.isConditional() || MVEL.evalToBoolean(consent.getRule(), variables);

        if (isValid) {
            // Recursively filter combined consents
            if (consent.getConsent() != null && !CollectionUtils.isEmpty(consent.getConsent().getConsents())) {
                List<ConsentConfig.Consent> filteredChildConsents = consent.getConsent().getConsents().stream()
                        .map(ConsentConfig.Consent::new)
                        .map(childConsent -> filterConsents(childConsent, variables))
                        .filter(Objects::nonNull)
                        .toList();

                if (!filteredChildConsents.isEmpty()) {
                    if(consent.getConsent() != null && StringUtils.isNotBlank(consent.getConsent().getType())
                            && consent.getConsent().getType().equalsIgnoreCase("combined")) {
                        consent.setText(consent.getText().replaceAll("\\. \\(required\\)", "")
                                .replaceAll("\\. \\(optional\\)", ""));


                        if(consent.getRequired() != null) {
                            filteredChildConsents.getLast().setText(filteredChildConsents.getLast().getText() + ". " + (consent.getRequired()? "(required)":"(optional)")); // Append required/optional text to the last consent
                        }
                    }
                    consent.setConsent(ConsentConfig.ConsentDetailsInfo.builder()
                            .type(consent.getConsent().getType())
                            .consents(filteredChildConsents)
                            .build());
                } else {
                    consent.setConsent(null);
                }
            }
            return consent;
        }
        return null; // Exclude invalid consents
    }

}
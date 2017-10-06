package eu.europa.esig.dss.signature.policy;

import java.util.List;

public interface CommitmentRule extends SignPolExtensions {

	List<CommitmentType> getSelCommitmentTypes();

	SignerAndVerifierRules getSignerAndVeriferRules();

	SigningCertTrustCondition getSigningCertTrustCondition();

	TimestampTrustCondition getTimeStampTrustCondition();

	AttributeTrustCondition getAttributeTrustCondition();

	AlgorithmConstraintSet getAlgorithmConstraintSet();

	List<SignPolExtn> getSignPolExtensions();

}
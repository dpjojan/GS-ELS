package com.mutualfund.mutual_fund_backend;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Talks to AWS database
 */
public interface MutualFundRepository extends JpaRepository<MutualFund, String> {
}

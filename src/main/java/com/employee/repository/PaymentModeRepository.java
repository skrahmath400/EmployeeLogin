package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.common.entity.PaymentMode;


public interface PaymentModeRepository extends JpaRepository<PaymentMode, Integer> {

}

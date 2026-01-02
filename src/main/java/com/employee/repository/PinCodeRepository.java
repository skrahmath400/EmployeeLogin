package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.common.entity.PinCode;



public interface PinCodeRepository extends JpaRepository<PinCode, Integer> {

}

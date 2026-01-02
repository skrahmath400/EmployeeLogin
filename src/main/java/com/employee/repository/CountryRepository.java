package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.common.entity.Country;




@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {

}

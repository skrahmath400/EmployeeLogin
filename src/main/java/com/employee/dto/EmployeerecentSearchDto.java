package com.employee.dto;

import java.io.Serializable;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeerecentSearchDto{
 
	//private static final long serialVersionUID = 1L;
	private int empId;
	private String empName;
	private String payRollId;
	private String departmentName;
	private String modeOfHiringName;
}
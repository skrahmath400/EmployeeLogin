package com.employee.dto;



import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillTestDetailsResultDto {

	private String employeeName;
	private String tempPayrollId;
	
	private  LocalDateTime joinDate;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate leftDate;
	private String gender;
	private String Remarks;

}

package com.employee.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.AdvancedEmployeeSearchRequestDTO;
import com.employee.dto.EmployeeSearchRequestDTO;
import com.employee.dto.EmployeeSearchResponseDTO;
import com.employee.dto.EmployeerecentSearchDto;
import com.employee.entity.Department;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.Employee;
import com.employee.entity.ModeOfHiring;
import com.employee.repository.EmployeeRepository;

/**
 * Service for flexible employee search Supports searching by cityId,
 * employeeTypeId, and payrollId in various combinations
 * 
 * IMPORTANT: payrollId is REQUIRED for all searches. Without payrollId, no data
 * will be returned. PayrollId must be combined with at least one other filter
 * (cityId or employeeTypeId).
 */
@Service
@Transactional(readOnly = true)
public class EmployeeSearchService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeSearchService.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	/**
	 * Search employees with flexible filters (automatic pagination - max 50
	 * records)
	 * 
	 * NOTE: payrollId is REQUIRED for all searches. It must be combined with at
	 * least one other filter (cityId or employeeTypeId). Without payrollId, an
	 * empty list will be returned.
	 * 
	 * Pagination is handled internally (page=0, size=50) for performance. Only
	 * first 50 records are returned.
	 * 
	 * @param searchRequest Search request with filters (cityId, employeeTypeId,
	 *                      payrollId - payrollId is required)
	 * @param pageable      Pagination parameters (automatically set to page=0,
	 *                      size=50)
	 * @return List of EmployeeSearchResponseDTO containing employee name,
	 *         department, employee id, temp payroll id (max 50 records)
	 */
	public List<EmployeeSearchResponseDTO> searchEmployees(EmployeeSearchRequestDTO searchRequest, Pageable pageable) {
		logger.info(
				"Searching employees with filters - cityId: {}, employeeTypeId: {}, payrollId: {}, page: {}, size: {}",
				searchRequest.getCityId(), searchRequest.getEmployeeTypeId(), searchRequest.getPayrollId(),
				pageable.getPageNumber(), pageable.getPageSize());

		// Validation: payrollId is REQUIRED for all searches
		if (searchRequest.getPayrollId() == null || searchRequest.getPayrollId().trim().isEmpty()) {
			logger.warn("PayrollId is required for all searches. No data will be returned without payrollId.");
			throw new IllegalArgumentException(
					"PayrollId is required for all searches. Please provide a valid payrollId.");
		}

		// Validation: payrollId must be combined with at least one other filter
		if (searchRequest.getCityId() == null && searchRequest.getEmployeeTypeId() == null) {
			logger.warn("PayrollId must be combined with at least one other filter (cityId or employeeTypeId).");
			throw new IllegalArgumentException(
					"PayrollId must be combined with at least one other filter. Please provide either cityId or employeeTypeId along with payrollId.");
		}

		// Use dynamic query method instead of 31 individual methods
		Page<EmployeeSearchResponseDTO> resultPage = employeeRepository.searchEmployeesDynamic(searchRequest, pageable);

		// Extract content from Page (already DTOs, no mapping needed)
		// Optimize: Use direct list access instead of stream
		List<EmployeeSearchResponseDTO> results = new ArrayList<>(resultPage.getContent());

		// Result size validation (safety check)
		if (results.size() > 50) {
			logger.warn("Result size exceeds expected limit: {}. Truncating to 50 records.", results.size());
			return results.subList(0, 50);
		}

		return results;
	}

	/**
	 * Advanced search employees with multiple filters (automatic pagination - max
	 * 50 records)
	 * 
	 * NOTE: payrollId is REQUIRED for all searches. It must be combined with at
	 * least one other filter. Without payrollId, an empty list will be returned.
	 * 
	 * Supported filters: stateId, cityId, campusId, employeeTypeId, departmentId,
	 * payrollId
	 * 
	 * Pagination is handled internally (page=0, size=50) for performance. Only
	 * first 50 records are returned.
	 * 
	 * @param searchRequest Advanced search request with filters (payrollId is
	 *                      required)
	 * @param pageable      Pagination parameters (automatically set to page=0,
	 *                      size=50)
	 * @return List of EmployeeSearchResponseDTO containing: empId, empName,
	 *         payRollId, departmentName, modeOfHiringName, tempPayrollId (max 50
	 *         records)
	 */
	public List<EmployeeSearchResponseDTO> advancedSearchEmployees(AdvancedEmployeeSearchRequestDTO searchRequest,
			Pageable pageable) {
		logger.info(
				"Advanced searching employees with filters - stateId: {}, cityId: {}, campusId: {}, employeeTypeId: {}, departmentId: {}, payrollId: {}, page: {}, size: {}",
				searchRequest.getStateId(), searchRequest.getCityId(), searchRequest.getCampusId(),
				searchRequest.getEmployeeTypeId(), searchRequest.getDepartmentId(), searchRequest.getPayrollId(),
				pageable.getPageNumber(), pageable.getPageSize());

		// Validation: payrollId is REQUIRED for all searches
		if (searchRequest.getPayrollId() == null || searchRequest.getPayrollId().trim().isEmpty()) {
			logger.warn("PayrollId is required for all searches. No data will be returned without payrollId.");
			throw new IllegalArgumentException(
					"PayrollId is required for all searches. Please provide a valid payrollId.");
		}

		// Validation: payrollId must be combined with at least one other filter
		boolean hasOtherFilter = searchRequest.getStateId() != null || searchRequest.getCityId() != null
				|| searchRequest.getCampusId() != null || searchRequest.getEmployeeTypeId() != null
				|| searchRequest.getDepartmentId() != null;

		if (!hasOtherFilter) {
			logger.warn(
					"PayrollId must be combined with at least one other filter (stateId, cityId, campusId, employeeTypeId, or departmentId).");
			throw new IllegalArgumentException(
					"PayrollId must be combined with at least one other filter. Please provide at least one of the following: stateId, cityId, campusId, employeeTypeId, or departmentId along with payrollId.");
		}

		// Use dynamic query method instead of 28 individual methods
		Page<EmployeeSearchResponseDTO> resultPage = employeeRepository.searchEmployeesAdvancedDynamic(searchRequest,
				pageable);

		// Extract content from Page (already DTOs from repository, no mapping needed)
		// Optimize: Use direct list access instead of stream
		List<EmployeeSearchResponseDTO> results = new ArrayList<>(resultPage.getContent());

		// Result size validation (safety check)
		if (results.size() > 50) {
			logger.warn("Result size exceeds expected limit: {}. Truncating to 50 records.", results.size());
			return results.subList(0, 50);
		}

		return results;
	}

	@Cacheable(value = "storeing_recent_searchs", key = "#emp_id")
	public EmployeerecentSearchDto getallemployeedat(int emp_id) {

		// 1. Fetch the primary employee
		Optional<Employee> employeeOptional = employeeRepository.findById(emp_id);

		if (employeeOptional.isEmpty()) {
			// Return null. The controller can wrap this in ResponseEntity.notFound() if
			// needed.
			return null;
		}

		Employee employee = employeeOptional.get();

		// 2. Fetch related data
		Optional<Department> departmentOptional = employeeRepository.findDepartmentByEmployeeId(emp_id);
		Optional<ModeOfHiring> gradeOptional = employeeRepository.findByModeOfHiring_id(emp_id);

		// 3. Initialize DTO
		EmployeerecentSearchDto recentsearchdto = new EmployeerecentSearchDto();
		recentsearchdto.setEmpId(emp_id);

		// --- Data Mapping ---

		// 4. Set Department
		departmentOptional.ifPresent(department -> {
			recentsearchdto.setDepartmentName(department.getDepartment_name());
		});

		// 5. Set Mode of Hiring
		if (gradeOptional.isPresent()) {
			ModeOfHiring hiringObj = gradeOptional.get();

			if (employee.getModeOfHiring_id() != null
					&& employee.getModeOfHiring_id().getMode_of_hiring_id() == hiringObj.getMode_of_hiring_id()) {
				recentsearchdto.setModeOfHiringName(hiringObj.getMode_of_hiring_name());
			}
		}

		// 6. Set Payroll ID
		if (employee.getTempPayrollId() != null && employee.getPayRollId() == null) {
			recentsearchdto.setPayRollId(employee.getTempPayrollId());
		} else {
			recentsearchdto.setPayRollId(employee.getPayRollId());
		}

		// 7. Set Full Name
		String fullname = employee.getFirst_name() + " ." + employee.getLast_name();
		recentsearchdto.setEmpName(fullname);

		// 8. Return the object directly (Not Optional)
		return recentsearchdto;
	}

}

package com.scemplogin.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.common.dto.JwtResponse;
import com.common.service.PasswordDecryptService;
import com.common.service.TokenAuthorization;
import com.scemplogin.dto.LoginDto;
import com.scemplogin.dto.LoginResponse;
import com.scemplogin.entity.SCEmployeeEntity;
import com.scemplogin.repository.SCEmployeeLoginRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SCEmpLoginService {

	@Autowired
	private SCEmployeeLoginRepository scEmpLoginRepository;

	@Autowired
	private PasswordDecryptService passwordDecryptService;

	@Autowired
	private TokenAuthorization tokenAuthorization;

	public LoginResponse empLogin(LoginDto login) throws Exception {
		// Log the start of the login attempt (avoid logging sensitive data like
		// passwords)
		log.info("Processing login request for email: {}", login.getEmail());

		// Query the repository to find employee by email
		Optional<SCEmployeeEntity> empLogin = scEmpLoginRepository.findByCrendentails(login.getEmail());
		log.debug("Employee lookup result for email {}: isPresent={}", login.getEmail(), empLogin.isPresent());

		if (empLogin.isPresent()) {
			SCEmployeeEntity employee = empLogin.get();
			log.debug("Employee details retrieved: empId={}, isActive={}", employee.getEmpId(), employee.getIsActive());

			// Check if the user is active
			if (employee.getIsActive() == 1) {
				log.debug("User is active for email: {}", login.getEmail());

				// Decrypt the provided password
				String decryptedPassword = passwordDecryptService.decryptPassword(login.getPassword());
				log.debug("Password decryption completed for email: {}", login.getEmail());

				// Compare the decrypted password with the stored password
				if (employee.getPassword().matches(decryptedPassword)) {
					log.info("Password verification successful for email: {}", login.getEmail());

					// Generate JWT token
					int empId = employee.getEmpId();
					String designation = employee.getDesignationName();
					String campusName = employee.getCampusName();
					String category = employee.getCategory();
					String campusCategory = employee.getCmpsCategory();
					try {
						JwtResponse jwt = tokenAuthorization.generateJwtForUser(empId);
						log.info("JWT token generated successfully for empId: {}", empId);

						// Concatenate firstName and lastName for empName
						String empName = employee.getFirstName() + " " + employee.getLastName();
						// Return successful login response
						return new LoginResponse(empName, empId, designation, campusName, campusCategory, category, jwt,
								true, "Login successful");
					} catch (Exception e) {
						log.error("Failed to generate JWT token for empId: {}. Error: {}", empId, e.getMessage(), e);
						return new LoginResponse(null, 0, null, null, null, null, null, false,
								"Failed to generate JWT token: " + e.getMessage());
					}
				} else {
					log.warn("Password verification failed for email: {}", login.getEmail());
					return new LoginResponse(null, 0, null, null, null, null, null, false, "Password Incorrect");
				}
			} else {
				log.warn("User is not active for email: {}", login.getEmail());
				return new LoginResponse(null, 0, null, null, null, null, null, false, "User is not active");
			}
		} else {
			log.warn("No employee found for email: {}", login.getEmail());
			return new LoginResponse(null, 0, null, null, null, null, null, false, "Email is not existed");
		}
	}
}

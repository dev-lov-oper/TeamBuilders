package com.campus.matching;

import com.campus.matching.controller.StudentController;
import com.campus.matching.dao.StudentDAO;
import com.campus.matching.dao.StudentDAOImpl;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.exception.BadRequestException;
import com.campus.matching.exception.GlobalExceptionHandler;
import com.campus.matching.exception.ResourceNotFoundException;
import com.campus.matching.model.ErrorResponse;
import com.campus.matching.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationExceptionTest {

    private StudentController studentController;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        StudentDAO studentDAO = new StudentDAOImpl();
        studentController = new StudentController(studentDAO);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testInvalidStudentIdValidation() {
        assertThrows(BadRequestException.class, () -> {
            studentController.getStudentById(-1L);
        });
    }

    @Test
    public void testStudentNotFoundValidation() {
        assertThrows(ResourceNotFoundException.class, () -> {
            studentController.getStudentById(999999L);
        });
    }

    @Test
    public void testMissingRequiredStudentFields() {
        assertThrows(BadRequestException.class, () -> {
            studentController.createStudent(new Student(null, null, "email@test.com", "CS", 1, "BEGINNER", "Bio"));
        });
    }

    @Test
    public void testGlobalExceptionHandlerMapping() {
        ResourceNotFoundException notFound = new ResourceNotFoundException("Student ID 999999 not found.");
        ResponseEntity<ErrorResponse> response404 = exceptionHandler.handleNotFound(notFound);

        assertEquals(HttpStatus.NOT_FOUND, response404.getStatusCode());
        assertEquals("NOT_FOUND", response404.getBody().error());
        assertEquals("Student ID 999999 not found.", response404.getBody().details());

        BadRequestException badReq = new BadRequestException("Invalid team size: -5.");
        ResponseEntity<ErrorResponse> response400 = exceptionHandler.handleBadRequest(badReq);

        assertEquals(HttpStatus.BAD_REQUEST, response400.getStatusCode());
        assertEquals("BAD_REQUEST", response400.getBody().error());
    }
}

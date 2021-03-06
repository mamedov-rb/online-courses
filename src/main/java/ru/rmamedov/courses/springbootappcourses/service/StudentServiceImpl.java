package ru.rmamedov.courses.springbootappcourses.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.rmamedov.courses.springbootappcourses.exception.EntityNotFoundException;
import ru.rmamedov.courses.springbootappcourses.exception.EntityNotSaved;
import ru.rmamedov.courses.springbootappcourses.model.Course;
import ru.rmamedov.courses.springbootappcourses.model.Role;
import ru.rmamedov.courses.springbootappcourses.model.Student;
import ru.rmamedov.courses.springbootappcourses.model.User;
import ru.rmamedov.courses.springbootappcourses.repository.DTO.StudentDTO;
import ru.rmamedov.courses.springbootappcourses.repository.RoleRep;
import ru.rmamedov.courses.springbootappcourses.repository.StudentRepo;
import ru.rmamedov.courses.springbootappcourses.service.interfaces.IStudentService;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements IStudentService {

    private StudentRepo studentRepo;
    private CourseServiceImpl courseService;
    private UserServiceImpl userService;
    private RoleRep roleRep;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public StudentServiceImpl(StudentRepo studentRepo, CourseServiceImpl courseService,
                              UserServiceImpl userService, RoleRep roleRep, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.studentRepo = studentRepo;
        this.courseService = courseService;
        this.userService = userService;
        this.roleRep = roleRep;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public List<Student> findAll() {
        return studentRepo.findAll();
    }

    @Override
    public List<StudentDTO> findAllDTO() {
        return studentRepo.findAllDTO();
    }

    @Override
    public List<StudentDTO> findAllDTOByCourseId(Long id) {
        List<StudentDTO> list = studentRepo.findStudentDTOByCourseId(id);
        if (list.size() > 0) {
            return list;
        }
        throw new EntityNotFoundException("Students with course id: " + id + " not found");
    }

    @Override
    public Student findById(Long id) {
        Optional<Student> student = studentRepo.findById(id);
        if (student.isPresent()) {
            return student.get();
        }
        throw new EntityNotFoundException("Student with id: " + id + " not found");
    }

    @Override
    public StudentDTO findDTOById(Long id) {
        Optional<StudentDTO> student = studentRepo.findStudentDTOById(id);
        if (student.isPresent()) {
            return student.get();
        }
        throw new EntityNotFoundException("Student with id: " + id + " not found");
    }

    @Override
    public Student findByUsername(String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            Student student = studentRepo.findByUser(user);
            if (student != null) {
                return student;
            }
        }
        throw new EntityNotFoundException("Student with username: '" + "', not found");
    }

    @Override
    public Student save(Student student) {
        return studentRepo.save(student);
    }

    @Override
    public Student save(User user) {
        Role roleFromRepo = roleRep.findByName(user.getRole().getName());
        if (roleFromRepo != null) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setRole(roleFromRepo);
            Student student = new Student();
            student.setUser(user);
            return studentRepo.save(student);
        } else {
            throw new EntityNotSaved("Can't manage to save User: " + user);
        }
    }

    @Override
    public Student update(Student student) {
        if (student.getUser().getRole() == null) {
            throw new EntityNotFoundException("Updating Student mast contains a Role!");
        }
        return save(student);
    }

    @Override
    public Student enroll(Long id, User user) {
        Student student = findByUsername(user.getUsername());
        Course course = courseService.findById(id);
        if (student.enroll(course)) {
            return update(student);
        }
        throw new EntityNotSaved("Can't enroll to this course: " + course.getTitle());
    }

    @Override
    public Student leave(Long id, User user) {
        Student student = findByUsername(user.getUsername());
        Course course = courseService.findById(id);
        if (student.leave(course)) {
            return update(student);
        }
        throw new EntityNotSaved("Can't leave the course: " + course.getTitle());
    }

    @Override
    public void deleteOneById(Long id) {
        Student student = findById(id);
        User user = userService.findByUsername(student.getUser().getUsername());
        user.setRole(roleRep.findByName("ROLE_USER"));
        studentRepo.delete(student);
    }

}

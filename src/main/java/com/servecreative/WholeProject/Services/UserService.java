package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.User;
import com.servecreative.WholeProject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;



import org.springframework.stereotype.Service;



import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


//    @Autowired
//    private PasswordEncoder passwordEncoder;




    public User saveUser(User user) {
//        String encryptedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(encryptedPassword);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserByEmailAndPassword(String email,String password)
    {
        return userRepository.findByEmailAndPassword(email,password);
    }

}

package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // You can define custom queries if needed, such as:
    User findByEmail(String email);
    User findByEmailAndPassword(String email,String password);



    //for custom query
//    @Query(value = "SELECT u FROM users u WHERE u.email = :email AND u.password = :password",nativeQuery = true)
//    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password);


}


package com.lakroune.backend.service.impl;

import com.lakroune.backend.entity.User;
import com.lakroune.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOp = userRepository.findByEmail(email);
        if(!userOp.isPresent())
            throw  new UsernameNotFoundException("not found user  by  name "+ email);
        User  user  = userOp.get();
        List<GrantedAuthority>   authorities  = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));
        return   new org.springframework.security.core.userdetails.User(email,user.getPassword(),authorities);
    }
}
